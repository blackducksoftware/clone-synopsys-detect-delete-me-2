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
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ReportService;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.workflow.blackduck.policy.PolicyChecker;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.result.ReportDetectResult;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckPostActions {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final EventSystem eventSystem;

    public BlackDuckPostActions(BlackDuckServicesFactory blackDuckServicesFactory, EventSystem eventSystem) {
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.eventSystem = eventSystem;
    }

    public void perform(BlackDuckPostOptions blackDuckPostOptions, CodeLocationWaitController codeLocationWaitController, ProjectVersionWrapper projectVersionWrapper, long timeoutInSeconds)
        throws DetectUserFriendlyException {
        try {
            if (blackDuckPostOptions.shouldWaitForResults()) {
                waitForCodeLocations(codeLocationWaitController, timeoutInSeconds);
            }
            if (blackDuckPostOptions.shouldPerformPolicyCheck()) {
                checkPolicy(blackDuckPostOptions, projectVersionWrapper.getProjectVersionView());
            }
            if (blackDuckPostOptions.shouldGenerateAnyReport()) {
                generateReports(blackDuckPostOptions, projectVersionWrapper, timeoutInSeconds);
            }
        } catch (DetectUserFriendlyException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new DetectUserFriendlyException(String.format("Your Black Duck configuration is not valid: %s", e.getMessage()), e, ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY);
        } catch (IntegrationRestException e) {
            throw new DetectUserFriendlyException(e.getMessage(), e, ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY);
        } catch (BlackDuckTimeoutExceededException e) {
            throw new DetectUserFriendlyException(e.getMessage(), e, ExitCodeType.FAILURE_TIMEOUT);
        } catch (Exception e) {
            throw new DetectUserFriendlyException(String.format("There was a problem: %s", e.getMessage()), e, ExitCodeType.FAILURE_GENERAL_ERROR);
        }
    }

    private void waitForCodeLocations(CodeLocationWaitController codeLocationWaitController, long timeoutInSeconds) throws DetectUserFriendlyException, InterruptedException, IntegrationException {
        logger.info("Detect must wait for bom tool calculations to finish.");
        CodeLocationCreationService codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
        if (codeLocationWaitController.getExpectedNotificationCount() > 0) {
            //TODO fix this when NotificationTaskRange doesn't include task start time
            //ekerwin - The start time of the task is the earliest time a code location was created.
            // In order to wait the full timeout, we have to not use that start time and instead use now().
            NotificationTaskRange notificationTaskRange = new NotificationTaskRange(System.currentTimeMillis(), codeLocationWaitController.getNotificationRange().getStartDate(),
                codeLocationWaitController.getNotificationRange().getEndDate());
            CodeLocationWaitResult result = codeLocationCreationService.waitForCodeLocations(
                notificationTaskRange,
                codeLocationWaitController.getProjectNameVersion(),
                codeLocationWaitController.getCodeLocationNames(),
                codeLocationWaitController.getExpectedNotificationCount(),
                timeoutInSeconds
            );
            if (result.getStatus() == CodeLocationWaitResult.Status.PARTIAL) {
                throw new DetectUserFriendlyException(result.getErrorMessage().orElse("Timed out waiting for code locations to finish on the Black Duck server."), ExitCodeType.FAILURE_TIMEOUT);
            }
        }
    }

    private void checkPolicy(BlackDuckPostOptions blackDuckPostOptions, ProjectVersionView projectVersionView) throws IntegrationException {
        logger.info("Detect will check policy for violations.");
        PolicyChecker policyChecker = new PolicyChecker(eventSystem, blackDuckServicesFactory.getBlackDuckService(), blackDuckServicesFactory.createProjectBomService());
        policyChecker.checkPolicy(blackDuckPostOptions.getSeveritiesToFailPolicyCheck(), projectVersionView);
    }

    private void generateReports(BlackDuckPostOptions blackDuckPostOptions, ProjectVersionWrapper projectVersionWrapper, long timeoutInSeconds) throws IntegrationException, IOException, InterruptedException {
        long timeoutInMillisec = 1000L * timeoutInSeconds;
        ProjectView projectView = projectVersionWrapper.getProjectView();
        ProjectVersionView projectVersionView = projectVersionWrapper.getProjectVersionView();

        ReportService reportService = blackDuckServicesFactory.createReportService(timeoutInMillisec);
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
