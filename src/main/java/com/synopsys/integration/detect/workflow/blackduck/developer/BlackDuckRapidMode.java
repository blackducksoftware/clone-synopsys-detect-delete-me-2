/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.blackduck.developer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.developermode.RapidScanService;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckRapidMode {
    private static final String OPERATION_NAME = "BLACK_DUCK_RAPID_SCAN";
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 1;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BlackDuckRunData blackDuckRunData;
    private RapidScanService rapidScanService;
    private Long timeoutInSeconds;

    public BlackDuckRapidMode(BlackDuckRunData blackDuckRunData, RapidScanService rapidScanService, Long timeoutInSeconds) {
        this.blackDuckRunData = blackDuckRunData;
        this.rapidScanService = rapidScanService;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public OperationResult<List<DeveloperScanComponentResultView>> run(BdioResult bdioResult) throws OperationException {
        logger.info("Begin Rapid Mode Scan");
        if (!blackDuckRunData.isOnline()) {
            logger.warn("Black Duck isn't online skipping rapid mode scan.");
            return OperationResult.success(OPERATION_NAME, Collections.emptyList());
        }

        List<DeveloperScanComponentResultView> results = new LinkedList<>();
        try {
            for (UploadTarget uploadTarget : bdioResult.getUploadTargets()) {
                results.addAll(rapidScanService.performDeveloperScan(uploadTarget.getUploadFile(), timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS));
            }
            logger.debug("Rapid scan result count: {}", results.size());
        } catch (IllegalArgumentException e) {
            String reason = String.format("Your Black Duck configuration is not valid: %s", e.getMessage());
            OperationResult<List<DeveloperScanComponentResultView>> operationResult = OperationResult.fail(OPERATION_NAME);
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY, reason));
            throw new OperationException(reason, e, operationResult);
        } catch (IntegrationRestException e) {
            OperationResult<List<DeveloperScanComponentResultView>> operationResult = OperationResult.fail(OPERATION_NAME);
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY, e.getMessage()));
            throw new OperationException(e.getMessage(), e, operationResult);
        } catch (BlackDuckIntegrationException e) {
            OperationResult<List<DeveloperScanComponentResultView>> operationResult = OperationResult.fail(OPERATION_NAME);
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_TIMEOUT, e.getMessage()));
            throw new OperationException(e.getMessage(), e, operationResult);
        } catch (Exception e) {
            String reason = String.format("There was a problem: %s", e.getMessage());
            OperationResult<List<DeveloperScanComponentResultView>> operationResult = OperationResult.fail(OPERATION_NAME);
            operationResult.addExitCode(new ExitCodeRequest(ExitCodeType.FAILURE_GENERAL_ERROR, reason));
            throw new OperationException(reason, e, operationResult);
        }
        return OperationResult.success(OPERATION_NAME, results);
    }
}
