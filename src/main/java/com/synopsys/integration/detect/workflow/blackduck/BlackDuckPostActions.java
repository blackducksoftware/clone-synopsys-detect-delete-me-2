/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.blackduck;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.CodeLocationWaitResult;
import com.synopsys.integration.blackduck.exception.BlackDuckTimeoutExceededException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.dataservice.ProjectBomService;
import com.synopsys.integration.blackduck.service.dataservice.ReportService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationWaitData;
import com.synopsys.integration.detect.workflow.blackduck.policy.PolicyChecker;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.result.ReportDetectResult;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckPostActions {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CodeLocationCreationService codeLocationCreationService;
    private final EventSystem eventSystem;
    private final BlackDuckApiClient blackDuckApiClient;
    private final ProjectBomService projectBomService;
    private final ReportService reportService;

    public BlackDuckPostActions(CodeLocationCreationService codeLocationCreationService, EventSystem eventSystem, BlackDuckApiClient blackDuckApiClient, ProjectBomService projectBomService, ReportService reportService) {
        this.codeLocationCreationService = codeLocationCreationService;
        this.eventSystem = eventSystem;
        this.blackDuckApiClient = blackDuckApiClient;
        this.projectBomService = projectBomService;
        this.reportService = reportService;
    }

    public OperationResult<Void> perform(BlackDuckPostOptions blackDuckPostOptions, CodeLocationWaitData codeLocationWaitData, ProjectVersionWrapper projectVersionWrapper, NameVersion projectNameVersion, long timeoutInSeconds)
        throws OperationException {
        OperationResult operationResult = OperationResult.empty();
        Status lastOperation = null;
        try {
            if (blackDuckPostOptions.shouldWaitForResults()) {
                lastOperation = new Status("BLACK_DUCK_CODE_LOCATION_WAIT", StatusType.SUCCESS);
                waitForCodeLocations(codeLocationWaitData, timeoutInSeconds, projectNameVersion);
                operationResult.addStatus(lastOperation);
            }
            if (blackDuckPostOptions.shouldPerformPolicyCheck()) {
                lastOperation = new Status("BLACK_DUCK_POLICY_CHECK", StatusType.SUCCESS);
                checkPolicy(blackDuckPostOptions, projectVersionWrapper.getProjectVersionView());
                operationResult.addStatus(lastOperation);
            }
            if (blackDuckPostOptions.shouldGenerateAnyReport()) {
                lastOperation = new Status("BLACK_DUCK_REPORT_GENERATION", StatusType.SUCCESS);
                generateReports(blackDuckPostOptions, projectVersionWrapper);
                operationResult.addStatus(lastOperation);
            }
        } catch (DetectUserFriendlyException e) {
            operationResult.addStatus(new Status(lastOperation.getDescriptionKey(), StatusType.FAILURE));
            operationResult.addExitCode(new ExitCodeRequest(e.getExitCodeType(), e.getMessage()));
            throw new OperationException(e.getMessage(), e, operationResult);
        } catch (IllegalArgumentException e) {
            String reason = String.format("Your Black Duck configuration is not valid: %s", e.getMessage());
            operationResult.addStatus(new Status(lastOperation.getDescriptionKey(), StatusType.FAILURE));
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY, reason));
            throw new OperationException(reason, e, operationResult);
        } catch (IntegrationRestException e) {
            operationResult.addStatus(new Status(lastOperation.getDescriptionKey(), StatusType.FAILURE));
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY, e.getMessage()));
            throw new OperationException(e.getMessage(), e, operationResult);
        } catch (BlackDuckTimeoutExceededException e) {
            operationResult.addStatus(new Status(lastOperation.getDescriptionKey(), StatusType.FAILURE));
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_TIMEOUT));
            throw new OperationException(e.getMessage(), e, operationResult);
        } catch (Exception e) {
            if (null != lastOperation) {
                operationResult.addStatus(new Status(lastOperation.getDescriptionKey(), StatusType.FAILURE));
            }
            String reason = String.format("There was a problem: %s", e.getMessage());
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_GENERAL_ERROR, reason));
            throw new OperationException(reason, e, operationResult);
        }

        return operationResult;
    }

    private void waitForCodeLocations(CodeLocationWaitData codeLocationWaitData, long timeoutInSeconds, NameVersion projectNameVersion) throws DetectUserFriendlyException, InterruptedException, IntegrationException {
        logger.info("Detect must wait for bom tool calculations to finish.");
        if (codeLocationWaitData.getExpectedNotificationCount() > 0) {
            //TODO fix this when NotificationTaskRange doesn't include task start time
            //ekerwin - The start time of the task is the earliest time a code location was created.
            // In order to wait the full timeout, we have to not use that start time and instead use now().
            //TODO: Handle the possible null pointer here.
            NotificationTaskRange notificationTaskRange = new NotificationTaskRange(System.currentTimeMillis(), codeLocationWaitData.getNotificationRange().getStartDate(),
                codeLocationWaitData.getNotificationRange().getEndDate());
            CodeLocationWaitResult result = codeLocationCreationService.waitForCodeLocations(
                notificationTaskRange,
                projectNameVersion,
                codeLocationWaitData.getCodeLocationNames(),
                codeLocationWaitData.getExpectedNotificationCount(),
                timeoutInSeconds
            );
            if (result.getStatus() == CodeLocationWaitResult.Status.PARTIAL) {
                throw new DetectUserFriendlyException(result.getErrorMessage().orElse("Timed out waiting for code locations to finish on the Black Duck server."), ExitCodeType.FAILURE_TIMEOUT);
            }
        }
    }

    private void checkPolicy(BlackDuckPostOptions blackDuckPostOptions, ProjectVersionView projectVersionView) throws IntegrationException {
        logger.info("Detect will check policy for violations.");
        PolicyChecker policyChecker = new PolicyChecker(eventSystem, blackDuckApiClient, projectBomService);
        policyChecker.checkPolicy(blackDuckPostOptions.getSeveritiesToFailPolicyCheck(), projectVersionView);
    }

    private void generateReports(BlackDuckPostOptions blackDuckPostOptions, ProjectVersionWrapper projectVersionWrapper) throws IntegrationException, IOException, InterruptedException {
        ProjectView projectView = projectVersionWrapper.getProjectView();
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        if (blackDuckPostOptions.shouldGenerateRiskReport()) {
            logger.info("Creating risk report pdf");
            File reportDirectory = blackDuckPostOptions.getRiskReportPdfPath().toFile();

            if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
                logger.warn(String.format("Failed to create risk report pdf directory: %s", blackDuckPostOptions.getRiskReportPdfPath().toString()));
            }

            DetectFontLoader detectFontLoader = new DetectFontLoader();
            File createdPdf = reportService.createReportPdfFile(reportDirectory, projectView, projectVersionView, detectFontLoader::loadFont, detectFontLoader::loadBoldFont);

            logger.info(String.format("Created risk report pdf: %s", createdPdf.getCanonicalPath()));
            eventSystem.publishEvent(Event.ResultProduced, new ReportDetectResult("Risk Report", createdPdf.getCanonicalPath()));
        }

        if (blackDuckPostOptions.shouldGenerateNoticesReport()) {
            logger.info("Creating notices report");
            File noticesDirectory = blackDuckPostOptions.getNoticesReportPath().toFile();

            if (!noticesDirectory.exists() && !noticesDirectory.mkdirs()) {
                logger.warn(String.format("Failed to create notices directory at %s", blackDuckPostOptions.getNoticesReportPath().toString()));
            }

            File noticesFile = reportService.createNoticesReportFile(noticesDirectory, projectView, projectVersionView);
            logger.info(String.format("Created notices report: %s", noticesFile.getCanonicalPath()));

            eventSystem.publishEvent(Event.ResultProduced, new ReportDetectResult("Notices Report", noticesFile.getCanonicalPath()));

        }
    }
}
