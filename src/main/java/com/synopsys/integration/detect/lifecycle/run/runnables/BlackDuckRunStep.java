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
package com.synopsys.integration.detect.lifecycle.run.runnables;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
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
import com.synopsys.integration.detect.workflow.bdio.AggregateMode;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
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
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckRunStep implements DetectRunStep {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final DetectContext detectContext;
    private ProductRunData productRunData;
    private DetectConfigurationFactory detectConfigurationFactory;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private CodeLocationNameManager codeLocationNameManager;
    private BdioCodeLocationCreator bdioCodeLocationCreator;
    private DetectInfo detectInfo;
    private DetectToolFilter detectToolFilter;
    private ImpactAnalysisOptions impactAnalysisOptions;

    public BlackDuckRunStep(DetectContext detectContext, ProductRunData productRunData, DetectConfigurationFactory detectConfigurationFactory, DirectoryManager directoryManager,
        EventSystem eventSystem, CodeLocationNameManager codeLocationNameManager, BdioCodeLocationCreator bdioCodeLocationCreator, DetectInfo detectInfo, DetectToolFilter detectToolFilter, ImpactAnalysisOptions impactAnalysisOptions) {
        this.detectContext = detectContext;
        this.productRunData = productRunData;
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.codeLocationNameManager = codeLocationNameManager;
        this.bdioCodeLocationCreator = bdioCodeLocationCreator;
        this.detectInfo = detectInfo;
        this.detectToolFilter = detectToolFilter;
        this.impactAnalysisOptions = impactAnalysisOptions;
    }

    @Override
    public boolean isApplicable() {
        return productRunData.shouldUseBlackDuckProduct();
    }

    @Override
    public DetectRunState run(DetectRunState previousState) throws DetectUserFriendlyException, IntegrationException {
        if (!isApplicable()) {
            logger.info("Black Duck tools will not be run.");
        } else {
            logger.debug("Black Duck tools will run.");
            RunOptions runOptions = previousState.getRunOptions();
            RunResult runResult = previousState.getCurrentRunResult();
            NameVersion projectNameVersion = previousState.getProjectNameVersion();
            AggregateOptions aggregateOptions = determineAggregationStrategy(previousState);
            BlackDuckRunData blackDuckRunData = productRunData.getBlackDuckRunData();

            blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);

            ProjectVersionWrapper projectVersionWrapper = null;

            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

            if (blackDuckRunData.isOnline() && blackDuckServicesFactory != null) {
                logger.debug("Getting or creating project.");
                DetectProjectServiceOptions options = detectConfigurationFactory.createDetectProjectServiceOptions();
                ProjectMappingService detectProjectMappingService = blackDuckServicesFactory.createProjectMappingService();
                DetectCustomFieldService detectCustomFieldService = new DetectCustomFieldService();
                DetectProjectService detectProjectService = new DetectProjectService(blackDuckServicesFactory, options, detectProjectMappingService, detectCustomFieldService);
                projectVersionWrapper = detectProjectService.createOrUpdateBlackDuckProject(projectNameVersion);

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

            BdioOptions bdioOptions = detectConfigurationFactory.createBdioOptions();
            BdioManager bdioManager = new BdioManager(detectInfo, new SimpleBdioFactory(), new ExternalIdFactory(), new Bdio2Factory(), new IntegrationEscapeUtil(), codeLocationNameManager, bdioCodeLocationCreator, directoryManager);
            BdioResult bdioResult = bdioManager.createBdioFiles(bdioOptions, aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations(), runOptions.shouldUseBdio2());
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
                BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = detectConfigurationFactory.createBlackDuckSignatureScannerOptions();
                BlackDuckSignatureScannerTool blackDuckSignatureScannerTool = new BlackDuckSignatureScannerTool(blackDuckSignatureScannerOptions, detectContext);
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
                    BinaryScanOptions binaryScanOptions = detectConfigurationFactory.createBinaryScanOptions();
                    BlackDuckBinaryScannerTool blackDuckBinaryScanner = new BlackDuckBinaryScannerTool(eventSystem, codeLocationNameManager, directoryManager, new WildcardFileFinder(), binaryScanOptions, blackDuckServicesFactory);
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
            BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;
            if (null != blackDuckServicesFactory) {
                blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.ONLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, blackDuckServicesFactory, eventSystem);
            } else {
                blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.OFFLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, eventSystem);
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

            logger.info(ReportConstants.RUN_SEPARATOR);
            //We have finished code locations.
            CodeLocationResultCalculator waitCalculator = new CodeLocationResultCalculator();
            CodeLocationResults codeLocationResults = waitCalculator.calculateCodeLocationResults(codeLocationAccumulator);
            eventSystem.publishEvent(Event.CodeLocationsCompleted, codeLocationResults.getAllCodeLocationNames());

            if (null != blackDuckServicesFactory) {
                logger.info("Will perform Black Duck post actions.");
                BlackDuckPostOptions blackDuckPostOptions = detectConfigurationFactory.createBlackDuckPostOptions();
                BlackDuckPostActions blackDuckPostActions = new BlackDuckPostActions(blackDuckServicesFactory, eventSystem);
                blackDuckPostActions.perform(blackDuckPostOptions, codeLocationResults.getCodeLocationWaitData(), projectVersionWrapper, projectNameVersion, detectConfigurationFactory.findTimeoutInSeconds());

                if ((!bdioResult.getUploadTargets().isEmpty() || detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN))) {
                    Optional<String> componentsLink = Optional.ofNullable(projectVersionWrapper)
                                                          .map(ProjectVersionWrapper::getProjectVersionView)
                                                          .flatMap(projectVersionView -> projectVersionView.getFirstLinkSafely(ProjectVersionView.COMPONENTS_LINK))
                                                          .map(HttpUrl::string);

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

        return previousState;
    }

    private AggregateOptions determineAggregationStrategy(DetectRunState currentRunState) {
        String aggregateName = currentRunState.getRunOptions().getAggregateName().orElse(null);
        AggregateMode aggregateMode = currentRunState.getRunOptions().getAggregateMode();
        if (StringUtils.isNotBlank(aggregateName)) {
            if (currentRunState.isFailure()) {
                return AggregateOptions.aggregateButSkipEmpty(aggregateName, aggregateMode);
            } else {
                return AggregateOptions.aggregateAndAlwaysUpload(aggregateName, aggregateMode);
            }
        } else {
            return AggregateOptions.doNotAggregate();
        }
    }
}
