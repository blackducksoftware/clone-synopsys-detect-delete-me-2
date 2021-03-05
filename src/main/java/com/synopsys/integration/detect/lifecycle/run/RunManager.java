/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationFactory;
import com.synopsys.integration.detect.lifecycle.run.operation.blackduck.ImpactAnalysisOperation;
import com.synopsys.integration.detect.lifecycle.run.operation.input.BdioInput;
import com.synopsys.integration.detect.lifecycle.run.operation.input.FullScanPostProcessingInput;
import com.synopsys.integration.detect.lifecycle.run.operation.input.ImpactAnalysisInput;
import com.synopsys.integration.detect.lifecycle.run.operation.input.RapidScanInput;
import com.synopsys.integration.detect.lifecycle.run.operation.input.SignatureScanInput;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.tool.UniversalToolsResult;
import com.synopsys.integration.detect.tool.detector.DetectorToolResult;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocationNamesResult;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class RunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExitCodeManager exitCodeManager;
    private OperationResult<Void> overallStatus;

    public RunManager(ExitCodeManager exitCodeManager) {
        this.exitCodeManager = exitCodeManager;
    }

    public void run(RunContext runContext) {
        overallStatus = OperationResult.empty();
        EventSystem eventSystem = runContext.getEventSystem();
        try {
            RunResult runResult = new RunResult();
            ProductRunData productRunData = runContext.getProductRunData();
            OperationFactory operationFactory = new OperationFactory(runContext);
            RunOptions runOptions = runContext.createRunOptions();
            DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

            logger.info(ReportConstants.RUN_SEPARATOR);
            if (runContext.getProductRunData().shouldUsePolarisProduct()) {
                runPolarisProduct(operationFactory, detectToolFilter, runOptions);
            } else {
                logger.info("Polaris tools will not be run.");
            }

            UniversalToolsResult universalToolsResult = runUniversalProjectTools(operationFactory, runOptions, detectToolFilter, eventSystem, runResult);

            if (productRunData.shouldUseBlackDuckProduct()) {
                OperationResult<AggregateOptions> aggregateOptions = operationFactory.createAggregateOptionsOperation().execute(universalToolsResult.anyFailed());
                if (aggregateOptions.getContent().isPresent()) {
                    runBlackDuckProduct(productRunData.getBlackDuckRunData(), operationFactory, runOptions, detectToolFilter, runResult,
                        universalToolsResult.getNameVersion(), aggregateOptions.getContent().get());
                }
            } else {
                logger.info("Black Duck tools will not be run.");
            }

            logger.info("All tools have finished.");
            logger.info(ReportConstants.RUN_SEPARATOR);
        } catch (OperationException ex) {
            overallStatus.aggregateResultData(ex.getOperationResult());
        } catch (Exception e) {
            if (e.getMessage() != null) {
                logger.error("Detect run failed: {}", e.getMessage());
            } else {
                logger.error("Detect run failed: {}", e.getClass().getSimpleName());
            }
            logger.debug("An exception was thrown during the detect run.", e);
            exitCodeManager.requestExitCode(e);
        }
        publishStatus(eventSystem, overallStatus.getStatuses());
        publishExitCodes(eventSystem, overallStatus.getExitCodes());
    }

    private void publishStatus(EventSystem eventSystem, List<Status> statusList) {
        for (Status status : statusList) {
            eventSystem.publishEvent(Event.StatusSummary, status);
        }
    }

    private void publishExitCodes(EventSystem eventSystem, List<ExitCodeRequest> exitCodeRequests) {
        for (ExitCodeRequest exitCodeRequest : exitCodeRequests) {
            eventSystem.publishEvent(Event.ExitCode, exitCodeRequest);
        }
    }

    private UniversalToolsResult runUniversalProjectTools(
        OperationFactory operationFactory,
        RunOptions runOptions,
        DetectToolFilter detectToolFilter,
        EventSystem eventSystem,
        RunResult runResult
    ) throws DetectUserFriendlyException, IntegrationException, OperationException {
        boolean anythingFailed = false;

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (!runOptions.shouldPerformRapidModeScan() && detectToolFilter.shouldInclude(DetectTool.DOCKER)) {
            logger.info("Will include the Docker tool.");
            OperationResult<DetectableToolResult> operationResult = operationFactory.createDockerOperation().execute();
            anythingFailed = anythingFailed || operationResult.anyFailed();
            operationResult.getContent().ifPresent(runResult::addDetectableToolResult);
            overallStatus.aggregateResultData(operationResult);
            logger.info("Docker actions finished.");
        } else {
            logger.info("Docker tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (!runOptions.shouldPerformRapidModeScan() && detectToolFilter.shouldInclude(DetectTool.BAZEL)) {
            logger.info("Will include the Bazel tool.");
            OperationResult<DetectableToolResult> operationResult = operationFactory.createBazelOperation().execute();
            anythingFailed = anythingFailed || operationResult.anyFailed();
            operationResult.getContent().ifPresent(runResult::addDetectableToolResult);
            overallStatus.aggregateResultData(operationResult);
            logger.info("Bazel actions finished.");
        } else {
            logger.info("Bazel tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.DETECTOR)) {
            logger.info("Will include the detector tool.");
            OperationResult<DetectorToolResult> operationResult = operationFactory.createDetectorOperation().execute();
            anythingFailed = anythingFailed || operationResult.anyFailed();
            operationResult.getContent().ifPresent(detectorToolResult -> {
                detectorToolResult.getBomToolProjectNameVersion().ifPresent(it -> runResult.addToolNameVersion(DetectTool.DETECTOR, new NameVersion(it.getName(), it.getVersion())));
                runResult.addDetectCodeLocations(detectorToolResult.getBomToolCodeLocations());
            });
            overallStatus.aggregateResultData(operationResult);
            logger.info("Detector actions finished.");
        } else {
            logger.info("Detector tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        logger.debug("Completed code location tools.");

        logger.debug("Determining project info.");

        OperationResult<NameVersion> projectNameVersionResult = operationFactory.createProjectDecisionOperation().execute(runResult.getDetectToolProjectInfo());
        overallStatus.aggregateResultData(projectNameVersionResult);
        NameVersion projectNameVersion = projectNameVersionResult.getContent().orElse(null);

        logger.info(String.format("Project name: %s", projectNameVersion.getName()));
        logger.info(String.format("Project version: %s", projectNameVersion.getVersion()));

        eventSystem.publishEvent(Event.ProjectNameVersionChosen, projectNameVersion);

        if (anythingFailed) {
            return UniversalToolsResult.failure(projectNameVersion);
        } else {
            return UniversalToolsResult.success(projectNameVersion);
        }
    }

    private void runPolarisProduct(OperationFactory operationFactory, DetectToolFilter detectToolFilter, RunOptions runOptions) {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.POLARIS) && !runOptions.shouldPerformRapidModeScan()) {
            logger.info("Will include the Polaris tool.");
            OperationResult<Void> operationResult = operationFactory.createPolarisOperation().execute();
            overallStatus.aggregateResultData(operationResult);
            logger.info("Polaris actions finished.");
        } else {
            logger.info("Polaris CLI tool will not be run.");
        }
    }

    private void runBlackDuckProduct(BlackDuckRunData blackDuckRunData, OperationFactory operationFactory, RunOptions runOptions, DetectToolFilter detectToolFilter, RunResult runResult, NameVersion projectNameVersion,
        AggregateOptions aggregateOptions)
        throws DetectUserFriendlyException, OperationException {

        logger.debug("Black Duck tools will run.");

        ProjectVersionWrapper projectVersionWrapper = null;

        BdioInput bdioInput = new BdioInput(aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations());
        OperationResult<BdioResult> bdioOperationResult = operationFactory.createBdioFileGenerationOperation().execute(bdioInput);
        overallStatus.aggregateResultData(bdioOperationResult);
        BdioResult emptyBdioResult = new BdioResult(Collections.emptyList(), new DetectCodeLocationNamesResult(Collections.emptyMap()), false);
        if (runOptions.shouldPerformRapidModeScan() && blackDuckRunData.isOnline()) {
            logger.info(ReportConstants.RUN_SEPARATOR);
            RapidScanInput rapidScanInput = new RapidScanInput(projectNameVersion, bdioOperationResult.getContent().orElse(emptyBdioResult));
            OperationResult<Void> rapidScanResult = operationFactory.createRapidScanOperation().execute(blackDuckRunData, blackDuckRunData.getBlackDuckServicesFactory(), rapidScanInput);
            overallStatus.aggregateResultData(rapidScanResult);
        } else {
            if (blackDuckRunData.isOnline()) {
                blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory();
                logger.debug("Getting or creating project.");
                OperationResult<ProjectVersionWrapper> projectCreationResult = operationFactory.createProjectCreationOperation().execute(blackDuckServicesFactory, projectNameVersion);
                projectVersionWrapper = projectCreationResult.getContent().orElse(null);
                overallStatus.aggregateResultData(projectCreationResult);
            } else {
                logger.debug("Detect is not online, and will not create the project.");
            }

            logger.debug("Completed project and version actions.");
            logger.debug("Processing Detect Code Locations.");

            CodeLocationAccumulator codeLocationAccumulator = new CodeLocationAccumulator<>();
            OperationResult<CodeLocationCreationData<UploadBatchOutput>> uploadResult = operationFactory.createBdioUploadOperation().execute(blackDuckRunData, bdioOperationResult.getContent().orElse(emptyBdioResult));
            uploadResult.getContent().ifPresent(codeLocationAccumulator::addWaitableCodeLocation);
            overallStatus.aggregateResultData(uploadResult);

            logger.debug("Completed Detect Code Location processing.");

            logger.info(ReportConstants.RUN_SEPARATOR);
            if (detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN)) {
                logger.info("Will include the signature scanner tool.");
                SignatureScanInput signatureScanInput = new SignatureScanInput(projectNameVersion, runResult.getDockerTar().orElse(null));
                OperationResult<CodeLocationCreationData<ScanBatchOutput>> signatureScanResult = operationFactory.createSignatureScanOperation().execute(signatureScanInput);
                signatureScanResult.getContent().ifPresent(codeLocationAccumulator::addWaitableCodeLocation);
                overallStatus.aggregateResultData(signatureScanResult);
                logger.info("Signature scanner actions finished.");
            } else {
                logger.info("Signature scan tool will not be run.");
            }

            logger.info(ReportConstants.RUN_SEPARATOR);
            if (detectToolFilter.shouldInclude(DetectTool.BINARY_SCAN)) {
                logger.info("Will include the binary scanner tool.");
                if (blackDuckRunData.isOnline()) {
                    OperationResult<CodeLocationCreationData<BinaryScanBatchOutput>> binaryScanResult = operationFactory.createBinaryScanOperation().execute(projectNameVersion);
                    binaryScanResult.getContent().ifPresent(codeLocationAccumulator::addWaitableCodeLocation);
                    overallStatus.aggregateResultData(binaryScanResult);
                }
                logger.info("Binary scanner actions finished.");
            } else {
                logger.info("Binary scan tool will not be run.");
            }
            ImpactAnalysisOperation impactAnalysisOperation = operationFactory.createImpactAnalysisOperation();
            logger.info(ReportConstants.RUN_SEPARATOR);
            if (detectToolFilter.shouldInclude(DetectTool.IMPACT_ANALYSIS) && impactAnalysisOperation.shouldImpactAnalysisToolRun()) {
                logger.info("Will include the Vulnerability Impact Analysis tool.");
                ImpactAnalysisInput impactAnalysisInput = new ImpactAnalysisInput(projectNameVersion, projectVersionWrapper);
                OperationResult<ImpactAnalysisToolResult> impactAnalysisToolResult = impactAnalysisOperation.execute(impactAnalysisInput);
                /* TODO: There is currently no mechanism within Black Duck for checking the completion status of an Impact Analysis code location. Waiting should happen here when such a mechanism exists. See HUB-25142. JM - 08/2020 */
                impactAnalysisToolResult.getContent().ifPresent(impactAnalysisResult ->
                                                                    codeLocationAccumulator.addNonWaitableCodeLocation(impactAnalysisResult.getCodeLocationNames()));
                overallStatus.aggregateResultData(impactAnalysisToolResult);
                logger.info("Vulnerability Impact Analysis tool actions finished.");
            } else if (impactAnalysisOperation.shouldImpactAnalysisToolRun()) {
                logger.info("Vulnerability Impact Analysis tool is enabled but will not run due to tool configuration.");
            } else {
                logger.info("Vulnerability Impact Analysis tool will not be run.");
            }

            logger.info(ReportConstants.RUN_SEPARATOR);
            //We have finished code locations.
            OperationResult<CodeLocationResults> codeLocationResults = operationFactory.createCodeLocationResultCalculationOperation().execute(codeLocationAccumulator);
            overallStatus.aggregateResultData(codeLocationResults);

            if (blackDuckRunData.isOnline() && codeLocationResults.getContent().isPresent()) {
                logger.info("Will perform Black Duck post actions.");
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory();
                FullScanPostProcessingInput fullScanPostProcessingInput = new FullScanPostProcessingInput(projectNameVersion, bdioOperationResult.getContent().orElse(emptyBdioResult), codeLocationResults.getContent().get(),
                    projectVersionWrapper);
                OperationResult<Void> postActionsResult = operationFactory.createFullScanPostProcessingOperation().execute(blackDuckServicesFactory, fullScanPostProcessingInput);
                overallStatus.aggregateResultData(postActionsResult);
                logger.info("Black Duck actions have finished.");
            } else {
                logger.debug("Will not perform Black Duck post actions: Detect is not online.");
            }
        }
    }
}
