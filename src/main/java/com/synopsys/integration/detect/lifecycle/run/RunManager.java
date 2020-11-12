/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detect.lifecycle.run;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.blackduck.BinaryScannerToolManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.BlackDuckPostActionsRunManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.BlackDuckRunManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.ImpactAnalysisToolManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.ProcessCodeLocationsRunManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.ProjectActionsRunManager;
import com.synopsys.integration.detect.lifecycle.run.blackduck.SignatureScannerToolManager;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.tool.DetectableTool;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.tool.UniversalToolsResult;
import com.synopsys.integration.detect.tool.binaryscanner.BlackDuckBinaryScannerTool;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.DetectorIssuePublisher;
import com.synopsys.integration.detect.tool.detector.DetectorRuleFactory;
import com.synopsys.integration.detect.tool.detector.DetectorTool;
import com.synopsys.integration.detect.tool.detector.DetectorToolResult;
import com.synopsys.integration.detect.tool.detector.executable.DetectExecutableRunner;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.tool.polaris.PolarisTool;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.bdio.AggregateMode;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostActions;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostOptions;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionOptions;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.file.WildcardFileFinder;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.detector.evaluation.DetectorEvaluationOptions;
import com.synopsys.integration.detector.finder.DetectorFinder;
import com.synopsys.integration.detector.finder.DetectorFinderOptions;
import com.synopsys.integration.detector.rule.DetectorRuleSet;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class RunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectContext detectContext;

    public RunManager(DetectContext detectContext) {
        this.detectContext = detectContext;
    }

    public RunResult run(ProductRunData productRunData) throws DetectUserFriendlyException, IntegrationException {
        //TODO: Better way for run manager to get dependencies so he can be tested. (And better ways of creating his objects)
        PropertyConfiguration detectConfiguration = detectContext.getBean(PropertyConfiguration.class);
        DetectConfigurationFactory detectConfigurationFactory = detectContext.getBean(DetectConfigurationFactory.class);
        DirectoryManager directoryManager = detectContext.getBean(DirectoryManager.class);
        EventSystem eventSystem = detectContext.getBean(EventSystem.class);
        CodeLocationNameManager codeLocationNameManager = detectContext.getBean(CodeLocationNameManager.class);
        BdioCodeLocationCreator bdioCodeLocationCreator = detectContext.getBean(BdioCodeLocationCreator.class);
        DetectInfo detectInfo = detectContext.getBean(DetectInfo.class);
        DetectDetectableFactory detectDetectableFactory = detectContext.getBean(DetectDetectableFactory.class);

        RunResult runResult = new RunResult();
        RunOptions runOptions = detectConfigurationFactory.createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

        logger.info(ReportConstants.RUN_SEPARATOR);

        if (productRunData.shouldUsePolarisProduct()) {
            runPolarisProduct(productRunData, detectConfiguration, directoryManager, eventSystem, detectToolFilter);
        } else {
            logger.info("Polaris tools will not be run.");
        }

        UniversalToolsResult universalToolsResult = runUniversalProjectTools(detectConfiguration, detectConfigurationFactory, directoryManager, eventSystem, detectDetectableFactory, runResult, runOptions, detectToolFilter,
            codeLocationNameManager);

        if (productRunData.shouldUseBlackDuckProduct()) {
            BlackDuckRunData blackDuckRunData = productRunData.getBlackDuckRunData();
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

            ProjectActionsRunManager projectActionsRunManager = ProjectActionsRunManager.createFromRunData(blackDuckRunData, detectConfigurationFactory.createDetectProjectServiceOptions());

            AggregateOptions aggregateOptions = determineAggregationStrategy(runOptions.getAggregateName().orElse(null), runOptions.getAggregateMode(), universalToolsResult);
            BdioManager bdioManager = new BdioManager(detectInfo, new SimpleBdioFactory(), new ExternalIdFactory(), new Bdio2Factory(), new IntegrationEscapeUtil(), codeLocationNameManager, bdioCodeLocationCreator, directoryManager);
            ProcessCodeLocationsRunManager processCodeLocationsRunManager = new ProcessCodeLocationsRunManager(aggregateOptions, detectConfigurationFactory.createBdioOptions(), bdioManager, eventSystem, blackDuckServicesFactory);

            BlackDuckSignatureScannerTool blackDuckSignatureScannerTool = new BlackDuckSignatureScannerTool(detectConfigurationFactory.createBlackDuckSignatureScannerOptions(), detectContext);
            SignatureScannerToolManager signatureScannerToolManager = new SignatureScannerToolManager(blackDuckSignatureScannerTool, blackDuckRunData, eventSystem, detectToolFilter);

            BlackDuckBinaryScannerTool blackDuckBinaryScanner = new BlackDuckBinaryScannerTool(eventSystem, codeLocationNameManager, directoryManager, new WildcardFileFinder(), detectConfigurationFactory.createBinaryScanOptions(),
                blackDuckServicesFactory);
            BinaryScannerToolManager binaryScannerToolManager = new BinaryScannerToolManager(blackDuckBinaryScanner, blackDuckServicesFactory, detectToolFilter);

            ImpactAnalysisOptions impactAnalysisOptions = detectConfigurationFactory.createImpactAnalysisOptions();
            BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;
            if (null != blackDuckServicesFactory) {
                blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.ONLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, blackDuckServicesFactory, eventSystem);
            } else {
                blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.OFFLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, eventSystem);
            }
            ImpactAnalysisToolManager impactAnalysisToolManager = new ImpactAnalysisToolManager(blackDuckImpactAnalysisTool, detectToolFilter);

            BlackDuckPostOptions blackDuckPostOptions = detectConfigurationFactory.createBlackDuckPostOptions();
            BlackDuckPostActions blackDuckPostActions = new BlackDuckPostActions(blackDuckServicesFactory, eventSystem);
            BlackDuckPostActionsRunManager blackDuckPostActionsRunManager = new BlackDuckPostActionsRunManager(blackDuckPostActions, blackDuckPostOptions, detectConfigurationFactory.findTimeoutInSeconds(), eventSystem,
                detectToolFilter);

            BlackDuckRunManager blackDuckRunManager = new BlackDuckRunManager(projectActionsRunManager, processCodeLocationsRunManager, signatureScannerToolManager, binaryScannerToolManager, impactAnalysisToolManager,
                blackDuckPostActionsRunManager);
            blackDuckRunManager.runBlackDuckProduct(blackDuckRunData, blackDuckServicesFactory, eventSystem, runResult, runOptions, universalToolsResult.getNameVersion(), new CodeLocationAccumulator());
        } else {
            logger.info("Black Duck tools will not be run.");
        }

        logger.info("All tools have finished.");
        logger.info(ReportConstants.RUN_SEPARATOR);

        return runResult;
    }

    private AggregateOptions determineAggregationStrategy(@Nullable String aggregateName, AggregateMode aggregateMode, UniversalToolsResult universalToolsResult) {
        if (StringUtils.isNotBlank(aggregateName)) {
            if (universalToolsResult.anyFailed()) {
                return AggregateOptions.aggregateButSkipEmpty(aggregateName, aggregateMode);
            } else {
                return AggregateOptions.aggregateAndAlwaysUpload(aggregateName, aggregateMode);
            }
        } else {
            return AggregateOptions.doNotAggregate();
        }
    }

    private UniversalToolsResult runUniversalProjectTools(
        PropertyConfiguration detectConfiguration,
        DetectConfigurationFactory detectConfigurationFactory,
        DirectoryManager directoryManager,
        EventSystem eventSystem,
        DetectDetectableFactory detectDetectableFactory,
        RunResult runResult,
        RunOptions runOptions,
        DetectToolFilter detectToolFilter,
        CodeLocationNameManager codeLocationNameManager
    ) throws DetectUserFriendlyException {

        ExtractionEnvironmentProvider extractionEnvironmentProvider = new ExtractionEnvironmentProvider(directoryManager);
        CodeLocationConverter codeLocationConverter = new CodeLocationConverter(new ExternalIdFactory());

        boolean anythingFailed = false;

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.DOCKER)) {
            logger.info("Will include the Docker tool.");
            DetectableTool detectableTool = new DetectableTool(detectDetectableFactory::createDockerDetectable,
                extractionEnvironmentProvider, codeLocationConverter, "DOCKER", DetectTool.DOCKER,
                eventSystem);

            DetectableToolResult detectableToolResult = detectableTool.execute(directoryManager.getSourceDirectory());

            runResult.addDetectableToolResult(detectableToolResult);
            anythingFailed = anythingFailed || detectableToolResult.isFailure();
            logger.info("Docker actions finished.");
        } else {
            logger.info("Docker tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.BAZEL)) {
            logger.info("Will include the Bazel tool.");
            DetectableTool detectableTool = new DetectableTool(detectDetectableFactory::createBazelDetectable,
                extractionEnvironmentProvider, codeLocationConverter, "BAZEL", DetectTool.BAZEL,
                eventSystem);
            DetectableToolResult detectableToolResult = detectableTool.execute(directoryManager.getSourceDirectory());
            runResult.addDetectableToolResult(detectableToolResult);
            anythingFailed = anythingFailed || detectableToolResult.isFailure();
            logger.info("Bazel actions finished.");
        } else {
            logger.info("Bazel tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.DETECTOR)) {
            logger.info("Will include the detector tool.");
            String projectBomTool = detectConfiguration.getValueOrEmpty(DetectProperties.DETECT_PROJECT_DETECTOR.getProperty()).orElse(null);
            List<DetectorType> requiredDetectors = detectConfiguration.getValueOrDefault(DetectProperties.DETECT_REQUIRED_DETECTOR_TYPES.getProperty());
            boolean buildless = detectConfiguration.getValueOrDefault(DetectProperties.DETECT_BUILDLESS.getProperty());

            DetectorRuleFactory detectorRuleFactory = new DetectorRuleFactory();
            DetectorRuleSet detectRuleSet = detectorRuleFactory.createRules(detectDetectableFactory, buildless);

            Path sourcePath = directoryManager.getSourceDirectory().toPath();
            DetectorFinderOptions finderOptions = detectConfigurationFactory.createSearchOptions(sourcePath);
            DetectorEvaluationOptions detectorEvaluationOptions = detectConfigurationFactory.createDetectorEvaluationOptions();

            DetectorIssuePublisher detectorIssuePublisher = new DetectorIssuePublisher();
            DetectorTool detectorTool = new DetectorTool(new DetectorFinder(), extractionEnvironmentProvider, eventSystem, codeLocationConverter, detectorIssuePublisher);
            DetectorToolResult detectorToolResult = detectorTool.performDetectors(directoryManager.getSourceDirectory(), detectRuleSet, finderOptions, detectorEvaluationOptions, projectBomTool, requiredDetectors);

            detectorToolResult.getBomToolProjectNameVersion().ifPresent(it -> runResult.addToolNameVersion(DetectTool.DETECTOR, new NameVersion(it.getName(), it.getVersion())));
            runResult.addDetectCodeLocations(detectorToolResult.getBomToolCodeLocations());

            if (!detectorToolResult.getFailedDetectorTypes().isEmpty()) {
                eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_DETECTOR, "A detector failed."));
                anythingFailed = true;
            }
            logger.info("Detector actions finished.");
        } else {
            logger.info("Detector tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        logger.debug("Completed code location tools.");

        logger.debug("Determining project info.");

        ProjectNameVersionOptions projectNameVersionOptions = detectConfigurationFactory.createProjectNameVersionOptions(directoryManager.getSourceDirectory().getName());
        ProjectNameVersionDecider projectNameVersionDecider = new ProjectNameVersionDecider(projectNameVersionOptions);
        NameVersion projectNameVersion = projectNameVersionDecider.decideProjectNameVersion(runOptions.getPreferredTools(), runResult.getDetectToolProjectInfo());

        logger.info(String.format("Project name: %s", projectNameVersion.getName()));
        logger.info(String.format("Project version: %s", projectNameVersion.getVersion()));

        eventSystem.publishEvent(Event.ProjectNameVersionChosen, projectNameVersion);

        if (anythingFailed) {
            return UniversalToolsResult.failure(projectNameVersion);
        } else {
            return UniversalToolsResult.success(projectNameVersion);
        }
    }

    private void runPolarisProduct(ProductRunData productRunData, PropertyConfiguration detectConfiguration, DirectoryManager directoryManager, EventSystem eventSystem,
        DetectToolFilter detectToolFilter) {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.POLARIS)) {
            logger.info("Will include the Polaris tool.");
            PolarisServerConfig polarisServerConfig = productRunData.getPolarisRunData().getPolarisServerConfig();
            DetectableExecutableRunner polarisExecutableRunner = DetectExecutableRunner.newInfo(eventSystem);
            PolarisTool polarisTool = new PolarisTool(eventSystem, directoryManager, polarisExecutableRunner, detectConfiguration, polarisServerConfig);
            polarisTool.runPolaris(new Slf4jIntLogger(logger), directoryManager.getSourceDirectory());
            logger.info("Polaris actions finished.");
        } else {
            logger.info("Polaris CLI tool will not be run.");
        }
    }

    private Set<String> createCodeLocationNames(DetectableToolResult detectableToolResult, CodeLocationNameManager codeLocationNameManager, DirectoryManager directoryManager) {
        if (detectableToolResult.getDetectToolProjectInfo().isPresent()) {
            NameVersion projectNameVersion = detectableToolResult.getDetectToolProjectInfo().get().getSuggestedNameVersion();
            return detectableToolResult.getDetectCodeLocations().stream()
                       .map(detectCodeLocation -> codeLocationNameManager.createCodeLocationName(detectCodeLocation, directoryManager.getSourceDirectory(), projectNameVersion.getName(), projectNameVersion.getVersion(), null, null))
                       .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

}
