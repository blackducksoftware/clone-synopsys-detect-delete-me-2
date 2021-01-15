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
package com.synopsys.integration.detect.lifecycle.run;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.tool.DetectableTool;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.tool.UniversalToolsResult;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanToolResult;
import com.synopsys.integration.detect.tool.binaryscanner.BlackDuckBinaryScannerTool;
import com.synopsys.integration.detect.tool.detector.DetectorTool;
import com.synopsys.integration.detect.tool.detector.DetectorToolResult;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.tool.polaris.PolarisTool;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.tool.signaturescanner.SignatureScannerToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.bdio.AggregateMode;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostActions;
import com.synopsys.integration.detect.workflow.blackduck.DetectBdioUploadService;
import com.synopsys.integration.detect.workflow.blackduck.DetectCodeLocationUnmapService;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResultCalculator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detect.workflow.result.BlackDuckBomDetectResult;
import com.synopsys.integration.detect.workflow.result.DetectResult;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class RunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectContext detectContext;

    public RunManager(DetectContext detectContext) {
        this.detectContext = detectContext;
    }

    public RunResult run(ProductRunData productRunData) throws DetectUserFriendlyException, IntegrationException {
        //TODO: Better way for run manager to get dependencies so he can be tested. (And better ways of creating his objects)
        RunFactory runFactory = new RunFactory(detectContext);
        RunResult runResult = new RunResult();
        RunOptions runOptions = runFactory.createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();
        File sourceDirectory = runFactory.getSourceDirectory();
        EventSystem eventSystem = runFactory.getEventSystem();

        logger.info(ReportConstants.RUN_SEPARATOR);

        if (productRunData.shouldUsePolarisProduct()) {
            runPolarisProduct(productRunData, runFactory, sourceDirectory, detectToolFilter);
        } else {
            logger.info("Polaris tools will not be run.");
        }

        UniversalToolsResult universalToolsResult = runUniversalProjectTools(runFactory, runResult, runOptions, detectToolFilter, sourceDirectory);

        if (productRunData.shouldUseBlackDuckProduct()) {
            AggregateOptions aggregateOptions = determineAggregationStrategy(runOptions.getAggregateName().orElse(null), runOptions.getAggregateMode(), universalToolsResult);
            runBlackDuckProduct(productRunData, runFactory, runResult, runOptions, detectToolFilter, universalToolsResult.getNameVersion(), aggregateOptions, eventSystem);
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
        RunFactory runFactory,
        RunResult runResult,
        RunOptions runOptions,
        DetectToolFilter detectToolFilter,
        File sourceDirectory
    ) throws DetectUserFriendlyException {
        boolean anythingFailed = false;
        EventSystem eventSystem = runFactory.getEventSystem();

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.DOCKER)) {
            logger.info("Will include the Docker tool.");
            DetectableTool detectableTool = runFactory.createDockerTool();
            DetectableToolResult detectableToolResult = detectableTool.execute(sourceDirectory);
            runResult.addDetectableToolResult(detectableToolResult);
            anythingFailed = anythingFailed || detectableToolResult.isFailure();
            logger.info("Docker actions finished.");
        } else {
            logger.info("Docker tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.BAZEL)) {
            logger.info("Will include the Bazel tool.");
            DetectableTool detectableTool = runFactory.createBazelTool();
            DetectableToolResult detectableToolResult = detectableTool.execute(sourceDirectory);
            runResult.addDetectableToolResult(detectableToolResult);
            anythingFailed = anythingFailed || detectableToolResult.isFailure();
            logger.info("Bazel actions finished.");
        } else {
            logger.info("Bazel tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.DETECTOR)) {
            logger.info("Will include the detector tool.");
            DetectorTool detectorTool = runFactory.createDetectorTool();
            DetectorToolResult detectorToolResult = detectorTool.performDetectors(sourceDirectory);

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

        ProjectNameVersionDecider projectNameVersionDecider = runFactory.createNameVersionDecider();
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

    private void runPolarisProduct(ProductRunData productRunData, RunFactory runFactory, File sourceDirectory, DetectToolFilter detectToolFilter) {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.POLARIS)) {
            logger.info("Will include the Polaris tool.");
            PolarisTool polarisTool = runFactory.createPolarisTool(productRunData.getPolarisRunData().getPolarisServerConfig());
            polarisTool.runPolaris(new Slf4jIntLogger(logger), sourceDirectory);
            logger.info("Polaris actions finished.");
        } else {
            logger.info("Polaris CLI tool will not be run.");
        }
    }

    private void runBlackDuckProduct(ProductRunData productRunData, RunFactory runFactory, RunResult runResult, RunOptions runOptions,
        DetectToolFilter detectToolFilter, NameVersion projectNameVersion, AggregateOptions aggregateOptions, EventSystem eventSystem) throws IntegrationException, DetectUserFriendlyException {

        logger.debug("Black Duck tools will run.");

        BlackDuckRunData blackDuckRunData = productRunData.getBlackDuckRunData();

        blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);

        ProjectVersionWrapper projectVersionWrapper = null;

        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

        if (blackDuckRunData.isOnline() && blackDuckServicesFactory != null) {
            logger.debug("Getting or creating project.");
            projectVersionWrapper = runFactory.createDetectProjectService(blackDuckServicesFactory).createOrUpdateBlackDuckProject(projectNameVersion);

            if (null != projectVersionWrapper && runOptions.shouldUnmapCodeLocations()) {
                logger.debug("Unmapping code locations.");
                DetectCodeLocationUnmapService detectCodeLocationUnmapService = new DetectCodeLocationUnmapService(blackDuckServicesFactory.getBlackDuckApiClient(), blackDuckServicesFactory.createCodeLocationService());
                detectCodeLocationUnmapService.unmapCodeLocations(projectVersionWrapper.getProjectVersionView());
            } else {
                logger.debug("Will not unmap code locations: Project view was not present, or should not unmap code locations.");
            }
        } else {
            logger.debug("Detect is not online, and will not create the project.");
        }

        logger.debug("Completed project and version actions.");

        logger.debug("Processing Detect Code Locations.");

        BdioManager bdioManager = runFactory.createBdioManager();
        BdioResult bdioResult = bdioManager.createBdioFiles(aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations(), runOptions.shouldUseBdio2());
        eventSystem.publishEvent(Event.DetectCodeLocationNamesCalculated, bdioResult.getCodeLocationNamesResult());

        CodeLocationAccumulator codeLocationAccumulator = new CodeLocationAccumulator();
        if (!bdioResult.getUploadTargets().isEmpty()) {
            logger.info(String.format("Created %d BDIO files.", bdioResult.getUploadTargets().size()));
            if (null != blackDuckServicesFactory) {
                logger.debug("Uploading BDIO files.");
                DetectBdioUploadService detectBdioUploadService = new DetectBdioUploadService();
                CodeLocationCreationData<UploadBatchOutput> uploadBatchOutputCodeLocationCreationData = detectBdioUploadService.uploadBdioFiles(bdioResult, blackDuckServicesFactory);
                codeLocationAccumulator.addWaitableCodeLocation(uploadBatchOutputCodeLocationCreationData);
            }
        } else {
            logger.debug("Did not create any BDIO files.");
        }

        logger.debug("Completed Detect Code Location processing.");

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN)) {
            logger.info("Will include the signature scanner tool.");
            BlackDuckSignatureScannerTool blackDuckSignatureScannerTool = runFactory.createBlackDuckSignatureScanTool();
            SignatureScannerToolResult signatureScannerToolResult = blackDuckSignatureScannerTool.runScanTool(blackDuckRunData, projectNameVersion, runResult.getDockerTar());
            if (signatureScannerToolResult.getResult() == Result.SUCCESS && signatureScannerToolResult.getCreationData().isPresent()) {
                codeLocationAccumulator.addWaitableCodeLocation(signatureScannerToolResult.getCreationData().get());
            } else if (signatureScannerToolResult.getResult() != Result.SUCCESS) {
                eventSystem.publishEvent(Event.StatusSummary, new Status("SIGNATURE_SCAN", StatusType.FAILURE));
                eventSystem.publishEvent(Event.Issue, new DetectIssue(DetectIssueType.SIGNATURE_SCANNER, Arrays.asList(signatureScannerToolResult.getResult().toString())));
            }
            logger.info("Signature scanner actions finished.");
        } else {
            logger.info("Signature scan tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.BINARY_SCAN)) {
            logger.info("Will include the binary scanner tool.");
            if (null != blackDuckServicesFactory) {
                BlackDuckBinaryScannerTool blackDuckBinaryScanner = runFactory.createBinaryScannerTool(blackDuckServicesFactory);
                if (blackDuckBinaryScanner.shouldRun()) {
                    BinaryScanToolResult result = blackDuckBinaryScanner.performBinaryScanActions(projectNameVersion);
                    if (result.isSuccessful()) {
                        codeLocationAccumulator.addWaitableCodeLocation(result.getCodeLocationCreationData());
                    }
                }
            }
            logger.info("Binary scanner actions finished.");
        } else {
            logger.info("Binary scan tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool = runFactory.createBlackDuckImpactAnalysisTool(blackDuckServicesFactory);
        if (detectToolFilter.shouldInclude(DetectTool.IMPACT_ANALYSIS) && blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Will include the Vulnerability Impact Analysis tool.");
            ImpactAnalysisToolResult impactAnalysisToolResult = blackDuckImpactAnalysisTool.performImpactAnalysisActionsWithLogs(projectNameVersion, projectVersionWrapper);

            /* TODO: There is currently no mechanism within Black Duck for checking the completion status of an Impact Analysis code location. Waiting should happen here when such a mechanism exists. See HUB-25142. JM - 08/2020 */
            codeLocationAccumulator.addNonWaitableCodeLocation(impactAnalysisToolResult.getCodeLocationNames());

            logger.info("Vulnerability Impact Analysis tool actions finished.");
        } else if (blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Vulnerability Impact Analysis tool is enabled but will not run due to tool configuration.");
        } else {
            logger.info("Vulnerability Impact Analysis tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);

        //We have finished code locations.
        CodeLocationResults codeLocationResults = new CodeLocationResultCalculator().calculateCodeLocationResults(codeLocationAccumulator);
        eventSystem.publishEvent(Event.CodeLocationsCompleted, codeLocationResults.getAllCodeLocationNames());

        if (null != blackDuckServicesFactory) {
            logger.info("Will perform Black Duck post actions.");
            BlackDuckPostActions blackDuckPostActions = runFactory.createBlackDuckPostActions(blackDuckServicesFactory);
            blackDuckPostActions.perform(codeLocationResults.getCodeLocationWaitData(), projectVersionWrapper, projectNameVersion);

            if ((!bdioResult.getUploadTargets().isEmpty() || detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN))) {
                Optional<String> componentsLink = findComponentLink(projectVersionWrapper);

                if (componentsLink.isPresent()) {
                    DetectResult detectResult = new BlackDuckBomDetectResult(componentsLink.get());
                    eventSystem.publishEvent(Event.ResultProduced, detectResult);
                }
            }
            logger.info("Black Duck actions have finished.");
        } else {
            logger.debug("Will not perform Black Duck post actions: Detect is not online.");
        }
    }

    private Optional<String> findComponentLink(final ProjectVersionWrapper projectVersionWrapper) {
        return Optional.ofNullable(projectVersionWrapper)
                   .map(ProjectVersionWrapper::getProjectVersionView)
                   .flatMap(projectVersionView -> projectVersionView.getFirstLinkSafely(ProjectVersionView.COMPONENTS_LINK))
                   .map(HttpUrl::string);
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
