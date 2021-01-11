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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.DetectorRuleFactory;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;
import com.synopsys.integration.detector.evaluation.ApplicableEvaluator;
import com.synopsys.integration.detector.evaluation.DetectorEvaluationOptions;
import com.synopsys.integration.detector.evaluation.ExtractableEvaluator;
import com.synopsys.integration.detector.finder.DetectorFinder;
import com.synopsys.integration.detector.finder.DetectorFinderDirectoryListException;
import com.synopsys.integration.detector.finder.DetectorFinderOptions;
import com.synopsys.integration.detector.rule.DetectorRuleSet;

public class AnalyzeManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectorFinder detectorFinder;
    private final ExtractionEnvironmentProvider extractionEnvironmentProvider;

    public AnalyzeManager(final DetectorFinder detectorFinder, final ExtractionEnvironmentProvider extractionEnvironmentProvider) {
        this.detectorFinder = detectorFinder;
        this.extractionEnvironmentProvider = extractionEnvironmentProvider;
    }

    public void runAnalyze(DetectDetectableFactory detectDetectableFactory, File sourceDirectory, AnalyzeOptions analyzeOptions, DetectorEvaluationOptions detectorEvaluationOptions) throws DetectUserFriendlyException {

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
        DetectorRuleSet buildRules = detectorRuleFactory.createBuildlessRules(detectDetectableFactory);
        DetectorRuleSet buildlessRules = detectorRuleFactory.createBuildlessRules(detectDetectableFactory);

        int depth = analyzeOptions.getUserProvidedSearchDepth().orElseGet(() -> {
            logger.info("Analysis will be completed up to depth 99. This can be customized by setting search depth.");
            return 99;
        });

        DetectorFinderOptions detectorFinderOptions = new DetectorFinderOptions(analyzeOptions.getSearchFilter(), depth);
        DetectorEvaluationTree buildRootEvaluation;
        try {
            buildRootEvaluation = detectorFinder.findDetectors(sourceDirectory, buildRules, detectorFinderOptions)
                                      .orElseThrow(() -> new DetectUserFriendlyException("Detect was unable to find a root evaluation.", ExitCodeType.FAILURE_CONFIGURATION));
        } catch (DetectorFinderDirectoryListException e) {
            throw new DetectUserFriendlyException("Detect was unable to list a directory while searching for detectors.", e, ExitCodeType.FAILURE_DETECTOR);
        }

        ApplicableEvaluator applicableEvaluator = new ApplicableEvaluator(detectorEvaluationOptions);
        applicableEvaluator.evaluate(buildRootEvaluation);
        ExtractableEvaluator extractableEvaluator = new ExtractableEvaluator(detectorEvaluationOptions, extractionEnvironmentProvider::createExtractionEnvironment);
        extractableEvaluator.evaluate(buildRootEvaluation);
.;
        //Now we have partially evaluated tree.
        //lets say we print 10 lines full, then we publish some depth counters?
        //lets get all apllicable
        List<DetectorEvaluationTree> trees = buildRootEvaluation.asFlatList();
        trees.forEach(tree -> {
            List<DetectorEvaluation> evaluations = Bds.of(tree.getOrderedEvaluations()).filter(DetectorEvaluation::isApplicable).toList();
            if (evaluations.size() > 0) {

            }
        });
        //group by folder
    }
}
