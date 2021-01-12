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
package com.synopsys.integration.detect.lifecycle.run.steps;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanOptions;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanToolResult;
import com.synopsys.integration.detect.tool.binaryscanner.BlackDuckBinaryScannerTool;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerOptions;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.tool.signaturescanner.SignatureScannerToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostActions;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostOptions;
import com.synopsys.integration.detect.workflow.blackduck.DetectBdioUploadService;
import com.synopsys.integration.detect.workflow.blackduck.DetectCodeLocationUnmapService;
import com.synopsys.integration.detect.workflow.blackduck.DetectCustomFieldService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectServiceOptions;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResultCalculator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detect.workflow.result.BlackDuckBomDetectResult;
import com.synopsys.integration.detect.workflow.result.DetectResult;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.detectable.file.WildcardFileFinder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckStep extends AbstractBlackDuckStep {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final DetectContext detectContext;
    private final DetectToolFilter detectToolFilter;
    private final ImpactAnalysisOptions impactAnalysisOptions;

    public BlackDuckStep(DetectInfo detectInfo, ProductRunData productRunData, DirectoryManager directoryManager, EventSystem eventSystem, DetectConfigurationFactory detectConfigurationFactory,
        CodeLocationNameManager codeLocationNameManager, BdioCodeLocationCreator bdioCodeLocationCreator, RunOptions runOptions, boolean priorStepsSucceeded, DetectContext detectContext,
        DetectToolFilter detectToolFilter, ImpactAnalysisOptions impactAnalysisOptions) {
        super(detectInfo, productRunData, directoryManager, eventSystem, detectConfigurationFactory, codeLocationNameManager, bdioCodeLocationCreator, runOptions, priorStepsSucceeded);
        this.detectContext = detectContext;
        this.detectToolFilter = detectToolFilter;
        this.impactAnalysisOptions = impactAnalysisOptions;
    }

    @Override
    protected boolean run(RunResult runResult) throws DetectUserFriendlyException, IntegrationException {
        NameVersion projectNameVersion = getProjectInfo(runResult, getRunOptions());
        AggregateOptions aggregateOptions = determineAggregationStrategy(getRunOptions(), !havePriorStepsSucceeded());
        BlackDuckRunData blackDuckRunData = getProductRunData().getBlackDuckRunData();

        blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);

        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

        Optional<ProjectVersionWrapper> projectVersionWrapper = createProjectVersion(blackDuckRunData, blackDuckServicesFactory, projectNameVersion);
        BdioResult bdioResult = createBdioFiles(runResult, getRunOptions(), aggregateOptions, projectNameVersion);
        CodeLocationAccumulator codeLocationAccumulator = processCodeLocations(blackDuckServicesFactory, bdioResult);
        checkAndExecuteSignatureScanner(runResult, blackDuckRunData, projectNameVersion, codeLocationAccumulator);
        checkAndExecuteBinaryScanner(blackDuckServicesFactory, projectNameVersion, codeLocationAccumulator);
        performImpactAnalysis(blackDuckServicesFactory, projectNameVersion, projectVersionWrapper.orElse(null), codeLocationAccumulator);
        CodeLocationResults codeLocationResults = getCodeLocationResults(codeLocationAccumulator);
        performPostProcessing(blackDuckServicesFactory, projectVersionWrapper.orElse(null), projectNameVersion, bdioResult, codeLocationResults);

        return true;
    }

    private Optional<ProjectVersionWrapper> createProjectVersion(BlackDuckRunData blackDuckRunData, BlackDuckServicesFactory blackDuckServicesFactory, NameVersion projectNameVersion)
        throws DetectUserFriendlyException, IntegrationException {
        ProjectVersionWrapper projectVersionWrapper = null;
        if (blackDuckRunData.isOnline() && blackDuckServicesFactory != null) {
            logger.debug("Getting or creating project.");
            DetectProjectServiceOptions options = getDetectConfigurationFactory().createDetectProjectServiceOptions();
            ProjectMappingService detectProjectMappingService = blackDuckServicesFactory.createProjectMappingService();
            DetectCustomFieldService detectCustomFieldService = new DetectCustomFieldService();
            DetectProjectService detectProjectService = new DetectProjectService(blackDuckServicesFactory, options, detectProjectMappingService, detectCustomFieldService);
            projectVersionWrapper = detectProjectService.createOrUpdateBlackDuckProject(projectNameVersion);

            if (null != projectVersionWrapper && getRunOptions().shouldUnmapCodeLocations()) {
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
        return Optional.ofNullable(projectVersionWrapper);
    }

    private CodeLocationAccumulator processCodeLocations(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, BdioResult bdioResult) throws DetectUserFriendlyException, IntegrationException {
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
        return codeLocationAccumulator;
    }

    private void checkAndExecuteSignatureScanner(RunResult runResult, BlackDuckRunData blackDuckRunData, NameVersion projectNameVersion, CodeLocationAccumulator codeLocationAccumulator) throws DetectUserFriendlyException {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN)) {
            logger.info("Will include the signature scanner tool.");
            BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = getDetectConfigurationFactory().createBlackDuckSignatureScannerOptions();
            BlackDuckSignatureScannerTool blackDuckSignatureScannerTool = new BlackDuckSignatureScannerTool(blackDuckSignatureScannerOptions, detectContext);
            SignatureScannerToolResult signatureScannerToolResult = blackDuckSignatureScannerTool.runScanTool(blackDuckRunData, projectNameVersion, runResult.getDockerTar());
            if (signatureScannerToolResult.getResult() == Result.SUCCESS && signatureScannerToolResult.getCreationData().isPresent()) {
                codeLocationAccumulator.addWaitableCodeLocation(signatureScannerToolResult.getCreationData().get());
            } else if (signatureScannerToolResult.getResult() != Result.SUCCESS) {
                getEventSystem().publishEvent(Event.StatusSummary, new Status("SIGNATURE_SCAN", StatusType.FAILURE));
                getEventSystem().publishEvent(Event.Issue, new DetectIssue(DetectIssueType.SIGNATURE_SCANNER, Arrays.asList(signatureScannerToolResult.getResult().toString())));
            }
            logger.info("Signature scanner actions finished.");
        } else {
            logger.info("Signature scan tool will not be run.");
        }
    }

    private void checkAndExecuteBinaryScanner(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, NameVersion projectNameVersion, CodeLocationAccumulator codeLocationAccumulator) throws DetectUserFriendlyException {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.BINARY_SCAN)) {
            logger.info("Will include the binary scanner tool.");
            if (null != blackDuckServicesFactory) {
                BinaryScanOptions binaryScanOptions = getDetectConfigurationFactory().createBinaryScanOptions();
                BlackDuckBinaryScannerTool blackDuckBinaryScanner = new BlackDuckBinaryScannerTool(getEventSystem(), getCodeLocationNameManager(), getDirectoryManager(), new WildcardFileFinder(), binaryScanOptions,
                    blackDuckServicesFactory);
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
    }

    private void performImpactAnalysis(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, NameVersion projectNameVersion, @Nullable ProjectVersionWrapper projectVersionWrapper, CodeLocationAccumulator codeLocationAccumulator)
        throws DetectUserFriendlyException {
        logger.info(ReportConstants.RUN_SEPARATOR);
        BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;
        if (null != blackDuckServicesFactory) {
            blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.ONLINE(getDirectoryManager(), getCodeLocationNameManager(), impactAnalysisOptions, blackDuckServicesFactory, getEventSystem());
        } else {
            blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.OFFLINE(getDirectoryManager(), getCodeLocationNameManager(), impactAnalysisOptions, getEventSystem());
        }
        if (detectToolFilter.shouldInclude(DetectTool.IMPACT_ANALYSIS) && blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Will include the Vulnerability Impact Analysis tool.");
            ImpactAnalysisToolResult impactAnalysisToolResult = blackDuckImpactAnalysisTool.performImpactAnalysisActions(projectNameVersion, projectVersionWrapper);

            /* TODO: There is currently no mechanism within Black Duck for checking the completion status of an Impact Analysis code location. Waiting should happen here when such a mechanism exists. See HUB-25142. JM - 08/2020 */
            codeLocationAccumulator.addNonWaitableCodeLocation(impactAnalysisToolResult.getCodeLocationNames());

            if (impactAnalysisToolResult.isSuccessful()) {
                logger.info("Vulnerability Impact Analysis successful.");
            } else {
                logger.warn("Something went wrong with the Vulnerability Impact Analysis tool.");
            }

            logger.info("Vulnerability Impact Analysis tool actions finished.");
        } else if (blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Vulnerability Impact Analysis tool is enabled but will not run due to tool configuration.");
        } else {
            logger.info("Vulnerability Impact Analysis tool will not be run.");
        }
    }

    private CodeLocationResults getCodeLocationResults(CodeLocationAccumulator codeLocationAccumulator) {
        logger.info(ReportConstants.RUN_SEPARATOR);
        //We have finished code locations.
        CodeLocationResultCalculator waitCalculator = new CodeLocationResultCalculator();
        CodeLocationResults codeLocationResults = waitCalculator.calculateCodeLocationResults(codeLocationAccumulator);
        getEventSystem().publishEvent(Event.CodeLocationsCompleted, codeLocationResults.getAllCodeLocationNames());
        return codeLocationResults;
    }

    private void performPostProcessing(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, @Nullable ProjectVersionWrapper projectVersionWrapper, NameVersion projectNameVersion, BdioResult bdioResult,
        CodeLocationResults codeLocationResults) throws DetectUserFriendlyException {
        if (null != blackDuckServicesFactory) {
            logger.info("Will perform Black Duck post actions.");
            BlackDuckPostOptions blackDuckPostOptions = getDetectConfigurationFactory().createBlackDuckPostOptions();
            BlackDuckPostActions blackDuckPostActions = new BlackDuckPostActions(blackDuckServicesFactory, getEventSystem());
            blackDuckPostActions.perform(blackDuckPostOptions, codeLocationResults.getCodeLocationWaitData(), projectVersionWrapper, projectNameVersion, getDetectConfigurationFactory().findTimeoutInSeconds());

            if ((!bdioResult.getUploadTargets().isEmpty() || detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN))) {
                Optional<String> componentsLink = Optional.ofNullable(projectVersionWrapper)
                                                      .map(ProjectVersionWrapper::getProjectVersionView)
                                                      .flatMap(projectVersionView -> projectVersionView.getFirstLinkSafely(ProjectVersionView.COMPONENTS_LINK))
                                                      .map(HttpUrl::string);

                if (componentsLink.isPresent()) {
                    DetectResult detectResult = new BlackDuckBomDetectResult(componentsLink.get());
                    getEventSystem().publishEvent(Event.ResultProduced, detectResult);
                }
            }
            logger.info("Black Duck actions have finished.");
        } else {
            logger.debug("Will not perform Black Duck post actions: Detect is not online.");
        }
    }
}
