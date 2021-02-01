/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.workflow.analyze;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.DetectorRuleFactory;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.tool.detector.file.DetectDetectorFileFilter;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.nameversion.DetectorNameVersionHandler;
import com.synopsys.integration.detect.workflow.nameversion.DetectorProjectInfo;
import com.synopsys.integration.detect.workflow.nameversion.DetectorProjectInfoMetadata;
import com.synopsys.integration.detect.workflow.nameversion.decision.NameVersionDecision;
import com.synopsys.integration.detect.workflow.report.EvaluationSummarizer;
import com.synopsys.integration.detect.workflow.report.EvaluationSummary;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.detector.evaluation.ApplicableEvaluator;
import com.synopsys.integration.detector.evaluation.DetectorEvaluationOptions;
import com.synopsys.integration.detector.evaluation.ExtractableEvaluator;
import com.synopsys.integration.detector.finder.DetectorFinder;
import com.synopsys.integration.detector.finder.DetectorFinderDirectoryListException;
import com.synopsys.integration.detector.finder.DetectorFinderOptions;
import com.synopsys.integration.detector.rule.DetectorRuleSet;
import com.synopsys.integration.util.NameVersion;

public class AnalyzeManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectContext detectContext;

    public AnalyzeManager(DetectContext detectContext) {
        this.detectContext = detectContext;
    }

    public void run() throws DetectUserFriendlyException { //Theoretically we might care about the ProductRunData.
        PropertyConfiguration detectConfiguration = detectContext.getBean(PropertyConfiguration.class);
        DetectConfigurationFactory detectConfigurationFactory = detectContext.getBean(DetectConfigurationFactory.class);
        DirectoryManager directoryManager = detectContext.getBean(DirectoryManager.class);
        DetectDetectableFactory detectDetectableFactory = detectContext.getBean(DetectDetectableFactory.class);

        ExtractionEnvironmentProvider extractionEnvironmentProvider = new ExtractionEnvironmentProvider(directoryManager);
        File sourceDirectory = directoryManager.getSourceDirectory();
        /**
         *
         * Package Managers Found
         *      Found Package Managers up to depth (2): GRADLE, NUGET, GIT
         *
         *      (1) C:\\Users\\jordanp\\Repositories\\blackduck-alert-folder
         *          Gradle Inspector, Git Cli
         *              *ISSUE* [GRADLE] No gradle executable found, provide a gradle executable using 'detect.grable.executable=gradle.exe'.
         * **/

        DetectorRuleFactory detectorRuleFactory = new DetectorRuleFactory();
        DetectorRuleSet buildRules = detectorRuleFactory.createBuildRules(detectDetectableFactory);

        logger.info(ReportConstants.RUN_SEPARATOR);

        int depth = Optional.ofNullable(detectConfigurationFactory.findAnalyzeDepthOrNull()).orElseGet(() -> {
            logger.info("Analysis will be completed up to depth 99. This can be customized by setting search depth.");
            return 99;
        });

        //TODO: Account for DETECT_DETECTOR_SEARCH_CONTINUE and whether we want to recommend it.
        DetectorEvaluationOptions detectorEvaluationOptions = detectConfigurationFactory.createDetectorEvaluationOptions();
        DetectDetectorFileFilter filter = detectConfigurationFactory.createSearchFilter(directoryManager.getSourceDirectory().toPath()); //huh?
        DetectorFinderOptions detectorFinderOptions = new DetectorFinderOptions(filter, depth);

        DetectorEvaluationTree buildEvaluation = evaluateDetectorsRulesToExtractable(buildRules, detectorEvaluationOptions, sourceDirectory, detectorFinderOptions, extractionEnvironmentProvider);

        Set<DetectorType> buildDetectors = findAllApplicableDetectorTypes(buildEvaluation);
        List<DetectorsAtDepths> detectorsAtDepths = detectorsAtDepths(buildEvaluation.asFlatList());

        logger.info("");
        logger.info(ReportConstants.RUN_SEPARATOR);

        //First, let's print some info about the detectors were found.
        if (buildDetectors.size() > 0) {
            logger.info("The following (" + buildDetectors.size() + ") detector types were found: " + Bds.of(buildDetectors).joining(", "));

            Set<DetectorType> detectorsAtRoot = Bds.of(detectorsAtDepths).filter(it -> it.getDepth() == 0).flatMap(DetectorsAtDepths::getDetectorTypes).toSet();
            Set<DetectorType> detectorsDeeper = Bds.of(detectorsAtDepths).filter(it -> it.getDepth() > 0).flatMap(DetectorsAtDepths::getDetectorTypes).toSet();

            int maxDepth = Bds.of(detectorsAtDepths).map(DetectorsAtDepths::getDepth).maxBy(Integer::compareTo).orElseGet(() -> 0);
            boolean anyAtDepthZero = !detectorsAtRoot.isEmpty();

            if (anyAtDepthZero && maxDepth == 0) {
                logger.info("Detectors were ONLY found in the root folder. The default search depth is sufficient for this project.");
            } else if (anyAtDepthZero && maxDepth > 0) {
                logger.info("Detectors were found in the root folder, but also up to depth {}. While not required, increasing the search depth will include additional detectors.", maxDepth);
                Set<DetectorType> difference = SetUtils.difference(detectorsDeeper, detectorsAtRoot);
                if (difference.size() > 0) {
                    logger.info("Detectors of the following types are only present at a depth greater than root: {}", Bds.of(difference).joining(", "));
                }
            } else if (!anyAtDepthZero && maxDepth > 0) {
                logger.info("NO detectors were found in the root folder, but were found up to depth {}. Search depth must be increased for any detector results.", maxDepth);
            }

            //We should at this point guess which project we think it will be.

        } else {
            logger.info("No detectors were found at any depth. Detectors are not required to scan this project and could be disabled with no data loss.");
        }
        logger.info("");
        logger.info(ReportConstants.RUN_SEPARATOR);

        //Lets also try searching with search.continue
        if (!detectorEvaluationOptions.isForceNested()) {
            logger.info("Detect will check to see if additional detectors can be found if detector search was forced to continue.");
            DetectorEvaluationOptions options = new DetectorEvaluationOptions(true, detectorEvaluationOptions.getDetectorFilter());
            DetectorEvaluationTree forcedBuildEvaluation = evaluateDetectorsRulesToExtractable(buildRules, options, sourceDirectory, detectorFinderOptions, extractionEnvironmentProvider);
            Set<DetectorType> forcedTypes = findAllApplicableDetectorTypes(forcedBuildEvaluation);
            Set<DetectorType> difference = SetUtils.difference(forcedTypes, buildDetectors);
            if (difference.size() > 0) {
                logger.info("The following detectors would also be found if forced continue was enabled: {}", Bds.of(difference).joining(", "));
            } else {
                logger.info("No additional detectors would be found if forced continue was enabled.");
            }
        }

        logger.info("");
        logger.info(ReportConstants.RUN_SEPARATOR);

        //It would also be nice to try to predict which detector is the Project/Version.
        //To accurately predict, we would need to know if the detector will produce a Project/Version which we can't currently know until evaluation....
        DetectorNameVersionHandler nameVersionHandler = new DetectorNameVersionHandler(Collections.singletonList(DetectorType.GIT));
        for (DetectorsAtDepths detectorsAtDepth : detectorsAtDepths) {
            for (DetectorType type : detectorsAtDepth.getDetectorTypes()) {
                if (nameVersionHandler.willAccept(new DetectorProjectInfoMetadata(type, detectorsAtDepth.getDepth()))) {
                    nameVersionHandler.accept(new DetectorProjectInfo(type, detectorsAtDepth.getDepth(), new NameVersion("Example Name", "Example Version")));
                }
            }
        }

        NameVersionDecision decision = nameVersionHandler.finalDecision();
        decision.printDescription(logger::info, logger::info);

        logger.info("");
        logger.info(ReportConstants.RUN_SEPARATOR);

        //Next, let's verify all 'Applicable' detectors were 'Extractable', if not, we should try running the buildless rules and compare the detectors.
        EvaluationSummarizer evaluationSummarizer = new EvaluationSummarizer();
        EvaluationSummary summary = evaluationSummarizer.summarize(buildEvaluation);

        if (summary.getFailedNotSkipped().isEmpty()) {
            logger.info("All detectors were extractable.");
            logger.info("Build mode should be used and should succeed.");
        } else {
            logger.info(ReportConstants.RUN_SEPARATOR);
            logger.info("Issues were found! The following detectors will not be extractable: " + Bds.of(mapToDetectorTypes(summary.getFailedNotSkipped())).joining(", "));

            summary.getFailedNotSkipped().forEach(it -> {
                logger.info("\t" + it.getDetectorType() + ": " + it.getExtractabilityMessage());
            });

            logger.info("Detect will now check if buildless mode is viable for this project.");
            DetectorRuleSet buildlessRules = detectorRuleFactory.createBuildlessRules(detectDetectableFactory);
            DetectorEvaluationTree buildlessEvaluation = evaluateDetectorsRulesToExtractable(buildlessRules, detectorEvaluationOptions, sourceDirectory, detectorFinderOptions, extractionEnvironmentProvider);
            Set<DetectorType> buildlessDetectors = findAllApplicableDetectorTypes(buildlessEvaluation);
            logger.info(ReportConstants.RUN_SEPARATOR);

            if (buildDetectors.size() == 0) {
                logger.info("No detectors were applicable in buildless. Buildless should not be used.");
            } else {
                logger.info("The following (" + buildlessDetectors.size() + ") detector types were found in buildless: " + Bds.of(buildlessDetectors).joining(", "));

                Set<DetectorType> difference = SetUtils.difference(buildDetectors, buildlessDetectors);
                if (difference.isEmpty()) {
                    logger.info("The same set of detectors is applicable for both buildless and build.");
                    logger.info("While the results will not have the same fidelity, buildless could be used without decreasing detector counts.");
                } else {
                    logger.info("The following (" + difference.size() + ") detector types would be MISSING if ran in buildless: " + Bds.of(difference).joining(", "));
                    logger.info("While it is not preferred to run in buildless, it could be used to generate some results.");
                }
            }

        }
        //Now we have partially evaluated tree. Let's do some summaries about what we found. Ideally the reporters for diagnostics are involved.
        //Let's start simply. Lets try counting the
        //
        logger.info(ReportConstants.RUN_SEPARATOR);
    }

    public DetectorEvaluationTree evaluateDetectorsRulesToExtractable(DetectorRuleSet ruleSet, DetectorEvaluationOptions detectorEvaluationOptions, File sourceDirectory, DetectorFinderOptions detectorFinderOptions,
        ExtractionEnvironmentProvider extractionEnvironmentProvider) throws DetectUserFriendlyException {
        DetectorEvaluationTree rootEvaluation;
        try {
            rootEvaluation = new DetectorFinder().findDetectors(sourceDirectory, ruleSet, detectorFinderOptions)
                                 .orElseThrow(() -> new DetectUserFriendlyException("Detect was unable to find a root evaluation.", ExitCodeType.FAILURE_CONFIGURATION));
        } catch (DetectorFinderDirectoryListException e) {
            throw new DetectUserFriendlyException("Detect was unable to list a directory while searching for detectors.", e, ExitCodeType.FAILURE_DETECTOR);
        }

        ApplicableEvaluator applicableEvaluator = new ApplicableEvaluator(detectorEvaluationOptions);
        applicableEvaluator.evaluate(rootEvaluation);
        ExtractableEvaluator extractableEvaluator = new ExtractableEvaluator(detectorEvaluationOptions, extractionEnvironmentProvider::createExtractionEnvironment);
        extractableEvaluator.evaluate(rootEvaluation);
        return rootEvaluation;
    }

    public Set<DetectorType> findAllApplicableDetectorTypes(DetectorEvaluationTree rootEvaluation) {
        return Bds.of(rootEvaluation.allDescendentEvaluations())
                   .filter(DetectorEvaluation::isApplicable)
                   .map(DetectorEvaluation::getDetectorType)
                   .toSet();

    }

    public Set<DetectorType> mapToDetectorTypes(List<DetectorEvaluation> evaluations) {
        return Bds.of(evaluations)
                   .map(DetectorEvaluation::getDetectorType)
                   .toSet();

    }

    public List<DetectorsAtDepths> detectorsAtDepths(List<DetectorEvaluationTree> trees) {
        List<DetectorsAtDepths> detectorsAtDepths = new ArrayList<>();

        for (DetectorEvaluationTree tree : trees) {
            int depth = tree.getDepthFromRoot();
            Set<DetectorType> detectorTypeSet = Bds.of(tree.getOrderedEvaluations())
                                                    .filter(DetectorEvaluation::isApplicable)
                                                    .map(DetectorEvaluation::getDetectorType)
                                                    .toSet();
            if (detectorTypeSet.size() > 0) {
                detectorsAtDepths.add(new DetectorsAtDepths(detectorTypeSet, depth));
            }
        }
        return detectorsAtDepths;
    }
}
