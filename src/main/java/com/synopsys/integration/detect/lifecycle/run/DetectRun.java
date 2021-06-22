/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationFactory;
import com.synopsys.integration.detect.lifecycle.run.singleton.BootSingletons;
import com.synopsys.integration.detect.lifecycle.run.singleton.EventSingletons;
import com.synopsys.integration.detect.lifecycle.run.singleton.SingletonFactory;
import com.synopsys.integration.detect.lifecycle.run.singleton.UtilitySingletons;
import com.synopsys.integration.detect.lifecycle.run.step.IntelligentModeStepRunner;
import com.synopsys.integration.detect.lifecycle.run.step.RapidModeStepRunner;
import com.synopsys.integration.detect.lifecycle.run.step.UniversalStepRunner;
import com.synopsys.integration.detect.lifecycle.run.step.utility.StepHelper;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.tool.UniversalToolsResult;
import com.synopsys.integration.detect.tool.detector.factory.DetectorFactory;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.status.OperationSystem;
import com.synopsys.integration.util.NameVersion;

public class DetectRun {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExitCodeManager exitCodeManager;

    public DetectRun(ExitCodeManager exitCodeManager) {
        this.exitCodeManager = exitCodeManager;
    }

    private OperationFactory createOperationFactory(BootSingletons bootSingletons, UtilitySingletons utilitySingletons, EventSingletons eventSingletons) throws DetectUserFriendlyException {
        DetectorFactory detectorFactory = new DetectorFactory(bootSingletons, utilitySingletons);
        DetectFontLoaderFactory detectFontLoaderFactory = new DetectFontLoaderFactory(bootSingletons, utilitySingletons);
        return new OperationFactory(detectorFactory.detectDetectableFactory(), detectFontLoaderFactory, bootSingletons, utilitySingletons, eventSingletons, exitCodeManager);
    }

    public void run(BootSingletons bootSingletons) {
        OperationSystem operationSystem = null;
        try {
            SingletonFactory singletonFactory = new SingletonFactory(bootSingletons);
            EventSingletons eventSingletons = singletonFactory.createEventSingletons();
            UtilitySingletons utilitySingletons = singletonFactory.createUtilitySingletons(eventSingletons, exitCodeManager);
            operationSystem = utilitySingletons.getOperationSystem();

            ProductRunData productRunData = bootSingletons.getProductRunData(); //TODO: Remove run data from boot singletons
            OperationFactory operationFactory = createOperationFactory(bootSingletons, utilitySingletons, eventSingletons);

            UniversalStepRunner stepRunner = new UniversalStepRunner(operationFactory, productRunData.getDetectToolFilter()); //Product independent tools
            UniversalToolsResult universalToolsResult = stepRunner.runUniversalTools();

            // combine: processProjectInformation() -> ProjectResult (nameversion, bdio)
            NameVersion nameVersion = stepRunner.determineProjectInformation(universalToolsResult);
            operationFactory.publishProjectNameVersionChosen(nameVersion);
            BdioResult bdio = stepRunner.generateBdio(universalToolsResult, nameVersion);
            StepHelper stepHelper = new StepHelper(operationSystem, utilitySingletons.getOperationWrapper(), productRunData.getDetectToolFilter());

            if (productRunData.shouldUseBlackDuckProduct()) {
                BlackDuckRunData blackDuckRunData = productRunData.getBlackDuckRunData();
                if (blackDuckRunData.isRapid()) {
                    if (blackDuckRunData.isOnline()) {
                        RapidModeStepRunner rapidModeSteps = new RapidModeStepRunner(operationFactory);
                        rapidModeSteps.runOnline(blackDuckRunData, nameVersion, bdio);
                    } else {
                        logger.info("Rapid Scan is offline, nothing to do.");
                    }
                } else {
                    if (blackDuckRunData.isOnline()) {
                        IntelligentModeStepRunner intelligentModeSteps = new IntelligentModeStepRunner(operationFactory, stepHelper);
                        intelligentModeSteps.runOnline(blackDuckRunData, bdio, nameVersion, productRunData.getDetectToolFilter(), universalToolsResult.getDockerTargetData());
                    } else {
                        IntelligentModeStepRunner intelligentModeSteps = new IntelligentModeStepRunner(operationFactory, stepHelper);
                        intelligentModeSteps.runOffline(nameVersion, universalToolsResult.getDockerTargetData());
                    }
                }
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                logger.error("Detect run failed: {}", e.getMessage());
            } else {
                logger.error("Detect run failed: {}", e.getClass().getSimpleName());
            }
            logger.debug("An exception was thrown during the detect run.", e);
            exitCodeManager.requestExitCode(e);
            if (e instanceof InterruptedException) {
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }
        } finally {
            if (operationSystem != null) {
                operationSystem.publishOperations();
            }
        }
    }
}
