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
package com.synopsys.integration.detect.lifecycle.run.blackduck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResultCalculator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckRunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProjectActionsRunManager projectActionsRunManager;
    private final ProcessCodeLocationsRunManager processCodeLocationsRunManager;
    private final SignatureScannerToolManager signatureScannerToolManager;
    private final BinaryScannerToolManager binaryScannerToolManager;
    private final ImpactAnalysisToolManager impactAnalysisToolManager;
    private final BlackDuckPostActionsRunManager blackDuckPostActionsRunManager;

    public BlackDuckRunManager(ProjectActionsRunManager projectActionsRunManager, ProcessCodeLocationsRunManager processCodeLocationsRunManager, SignatureScannerToolManager signatureScannerToolManager,
        BinaryScannerToolManager binaryScannerToolManager, ImpactAnalysisToolManager impactAnalysisToolManager, BlackDuckPostActionsRunManager blackDuckPostActionsRunManager) {
        this.projectActionsRunManager = projectActionsRunManager;
        this.processCodeLocationsRunManager = processCodeLocationsRunManager;
        this.signatureScannerToolManager = signatureScannerToolManager;
        this.binaryScannerToolManager = binaryScannerToolManager;
        this.impactAnalysisToolManager = impactAnalysisToolManager;
        this.blackDuckPostActionsRunManager = blackDuckPostActionsRunManager;
    }

    public void runBlackDuckProduct(BlackDuckRunData blackDuckRunData, BlackDuckServicesFactory blackDuckServicesFactory, EventSystem eventSystem, RunResult runResult, RunOptions runOptions, NameVersion projectNameVersion,
        CodeLocationAccumulator codeLocationAccumulator)
        throws IntegrationException, DetectUserFriendlyException {

        logger.debug("Black Duck tools will run.");

        blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);

        ProjectVersionWrapper projectVersionWrapper = null;
        if (blackDuckRunData.isOnline()) {
            projectVersionWrapper = projectActionsRunManager.projectAndVersionActions(projectNameVersion, runOptions.shouldUnmapCodeLocations());
        } else {
            logger.debug("Detect is not online, and will not create the project.");
        }
        logger.debug("Completed project and version actions.");

        logger.debug("Processing Detect Code Locations.");
        BdioResult bdioResult = processCodeLocationsRunManager.processCodeLocations(projectNameVersion, runResult.getDetectCodeLocations(), runOptions.shouldUseBdio2(), codeLocationAccumulator);
        logger.debug("Completed Detect Code Location processing.");

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (signatureScannerToolManager.isIncluded()) {
            logger.info("Will include the signature scanner tool.");
            signatureScannerToolManager.runSignatureScanner(projectNameVersion, runResult.getDockerTar(), codeLocationAccumulator);
            logger.info("Signature scanner actions finished.");
        } else {
            logger.info("Signature scan tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (binaryScannerToolManager.isIncluded()) {
            logger.info("Will include the binary scanner tool.");
            binaryScannerToolManager.runBinaryScanner(projectNameVersion, codeLocationAccumulator);
            logger.info("Binary scanner actions finished.");
        } else {
            logger.info("Binary scan tool will not be run.");
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        if (impactAnalysisToolManager.isIncluded() && impactAnalysisToolManager.shouldRun()) {
            logger.info("Will include the Vulnerability Impact Analysis tool.");
            impactAnalysisToolManager.runImpactAnalysis(projectNameVersion, projectVersionWrapper, codeLocationAccumulator);
            logger.info("Vulnerability Impact Analysis tool actions finished.");
        } else if (impactAnalysisToolManager.shouldRun()) {
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
            blackDuckPostActionsRunManager.performBlackDuckPostActions(projectNameVersion, projectVersionWrapper, bdioResult, codeLocationResults);
            logger.info("Black Duck post actions have finished.");
        } else {
            logger.debug("Will not perform Black Duck post actions: Detect is not online.");
        }

    }
}
