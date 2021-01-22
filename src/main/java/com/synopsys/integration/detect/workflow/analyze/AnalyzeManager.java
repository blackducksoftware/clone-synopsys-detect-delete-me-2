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
import com.synopsys.integration.detect.workflow.report.util.DetectorEvaluationUtils;
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
        logger.info("The following (" + buildDetectors.size() + ") detector types were found: " + Bds.of(buildDetectors).joining(", "));
        logger.info("Min depth : " + Bds.of(detectorsAtDepths).map(DetectorsAtDepths::getDepth).minBy(Integer::compareTo).map(Object::toString).orElse("N/A"));
        logger.info("Max depth : " + Bds.of(detectorsAtDepths).map(DetectorsAtDepths::getDepth).maxBy(Integer::compareTo).map(Object::toString).orElse("N/A"));

        boolean anyAtDepthZero = Bds.of(detectorsAtDepths).filter(it -> it.getDepth() == 0).filter(it -> it.getDetectorTypes().isEmpty()).toList().isEmpty();
        if (anyAtDepthZero) {
            logger.info("Detectors were found in the root folder.");
        } else {
            logger.info("NO detectors were found in the root folder. Search depth must be increased for any detector results.");
        }
        //OK so predicting what detect will pick is not as easy as I thought...
        //ProjectNameVersionDecider projectNameVersionDecider = new ProjectNameVersionDecider(new ProjectNameVersionOptions())
        // logger.info("Detect believes this is most likely a " + + " project.");

        //Next, let's verify all 'Applicable' detectors were 'Extractable', if not, we should try running the buildless rules and compare the detectors.
        Set<DetectorType> applicableButNotExtractable = findApplicableButNotExtractable(buildEvaluation);
        if (applicableButNotExtractable.isEmpty()) {
            logger.info("All detectors were extractable, no further configuration recommended.");
        } else { //TODO: Handle fallbacks.
            logger.info(ReportConstants.RUN_SEPARATOR);
            logger.info("Issues were found! The following detectors will not be extractable: " + Bds.of(applicableButNotExtractable).joining(", "));

            Bds.of(DetectorEvaluationUtils.filteredChildren(buildEvaluation, it -> it.isApplicable() && !it.isExtractable())).forEach(it -> {
                logger.info("\t" + it.getDetectorType() + ": " + it.getExtractabilityMessage());
            });

            logger.info("Detect will now check if buildless mode is viable for this project.");
            DetectorRuleSet buildlessRules = detectorRuleFactory.createBuildlessRules(detectDetectableFactory);
            DetectorEvaluationTree buildlessEvaluation = evaluateDetectorsRulesToExtractable(buildlessRules, detectorEvaluationOptions, sourceDirectory, detectorFinderOptions, extractionEnvironmentProvider);
            Set<DetectorType> buildlessDetectors = findAllApplicableDetectorTypes(buildlessEvaluation);
            logger.info(ReportConstants.RUN_SEPARATOR);

            logger.info("The following (" + buildlessDetectors.size() + ") detector types were found in buildless: " + Bds.of(buildlessDetectors).joining(", "));

            Set<DetectorType> difference = SetUtils.difference(buildDetectors, buildlessDetectors);
            if (difference.isEmpty()) {
                logger.info("The same set of detectors is applicable for both buildless and build - buildless could be used.");
            } else {
                logger.info("The following (" + difference.size() + ") detector types would be MISSING if ran in buildless: " + Bds.of(difference).joining(", "));
            }
        }
        //Now we have partially evaluated tree. Let's do some summaries about what we found. Ideally the reporters for diagnostics are involved.
        //Let's start simply. Lets try counting the
        //
        logger.info(ReportConstants.RUN_SEPARATOR);
    }

    public void compareBuildAndBuildless(Set<DetectorType> build, Set<DetectorType> buildless) {

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
        return Bds.of(DetectorEvaluationUtils.filteredChildren(rootEvaluation, DetectorEvaluation::isApplicable))
                   .map(DetectorEvaluation::getDetectorType)
                   .toSet();

    }

    public Set<DetectorType> findApplicableButNotExtractable(DetectorEvaluationTree rootEvaluation) {
        return Bds.of(DetectorEvaluationUtils.filteredChildren(rootEvaluation, it -> it.isApplicable() && !it.isExtractable()))
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
