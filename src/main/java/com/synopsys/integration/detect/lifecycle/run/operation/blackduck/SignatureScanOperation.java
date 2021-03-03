/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import java.util.Arrays;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationService;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.operation.input.SignatureScanInput;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.tool.signaturescanner.SignatureScannerToolResult;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.exception.IntegrationException;

public class SignatureScanOperation {
    private static final String OPERATION_NAME = "SIGNATURE_SCAN";
    private final BlackDuckRunData blackDuckRunData;
    private final BlackDuckSignatureScannerTool signatureScannerTool;
    private final EventSystem eventSystem;

    public SignatureScanOperation(BlackDuckRunData blackDuckRunData, BlackDuckSignatureScannerTool signatureScannerTool,
        EventSystem eventSystem) {
        this.blackDuckRunData = blackDuckRunData;
        this.signatureScannerTool = signatureScannerTool;
        this.eventSystem = eventSystem;
    }

    public OperationResult<CodeLocationCreationData<ScanBatchOutput>> execute(SignatureScanInput signatureScanInput) throws DetectUserFriendlyException, IntegrationException {
        OperationResult<CodeLocationCreationData<ScanBatchOutput>> result = OperationResult.success(OPERATION_NAME);
        try {
            BlackDuckServerConfig blackDuckServerConfig = null;
            CodeLocationCreationService codeLocationCreationService = null;
            if (null != blackDuckRunData && blackDuckRunData.isOnline()) {
                BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory();
                codeLocationCreationService = blackDuckServicesFactory.createCodeLocationCreationService();
                blackDuckServerConfig = blackDuckRunData.getBlackDuckServerConfig();
            }
            SignatureScannerToolResult signatureScannerToolResult = signatureScannerTool.runScanTool(codeLocationCreationService, blackDuckServerConfig, signatureScanInput.getProjectNameVersion(), signatureScanInput.getDockerTar());
            if (signatureScannerToolResult.getResult() == Result.SUCCESS && signatureScannerToolResult.getCreationData().isPresent()) {
                result = OperationResult.success(OPERATION_NAME, signatureScannerToolResult.getCreationData().get());
            } else if (signatureScannerToolResult.getResult() != Result.SUCCESS) {
                result = OperationResult.fail(OPERATION_NAME);
                //eventSystem.publishEvent(Event.StatusSummary, new Status(OPERATION_NAME, StatusType.FAILURE));
                eventSystem.publishEvent(Event.Issue, new DetectIssue(DetectIssueType.SIGNATURE_SCANNER, Arrays.asList(signatureScannerToolResult.getResult().toString())));
            }
        } catch (Exception ex) {
            result = OperationResult.fail(OPERATION_NAME, ex);
        }
        return result;
    }
}
