package com.synopsys.integration.detect.tool.signaturescanner;

import java.util.Optional;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.ScanBatchOutput;
import com.synopsys.integration.blackduck.service.model.NotificationTaskRange;

public class SignatureScannerToolResult {
    private final Optional<CodeLocationCreationData<ScanBatchOutput>> creationData;
    private final ScanBatchOutput scanBatchOutput;
    private final Result result;

    public static SignatureScannerToolResult createOnlineResult(NotificationTaskRange notificationTaskRange, ScanBatchOutput scanBatchOutput) {
        CodeLocationCreationData<ScanBatchOutput> creationData = new CodeLocationCreationData<>(notificationTaskRange, scanBatchOutput);
        return new SignatureScannerToolResult(creationData, creationData.getOutput(), Result.SUCCESS);
    }

    public static SignatureScannerToolResult createOfflineResult(ScanBatchOutput scanBatchOutput) {
        return new SignatureScannerToolResult(null, scanBatchOutput, Result.SUCCESS);
    }

    public static SignatureScannerToolResult createFailureResult() {
        return new SignatureScannerToolResult(null, null, Result.FAILURE);
    }

    private SignatureScannerToolResult(CodeLocationCreationData<ScanBatchOutput> creationData, ScanBatchOutput scanBatchOutput, Result result) {
        this.creationData = Optional.ofNullable(creationData);
        this.scanBatchOutput = scanBatchOutput;
        this.result = result;
    }

    public Optional<CodeLocationCreationData<ScanBatchOutput>> getCreationData() {
        return creationData;
    }

    public ScanBatchOutput getScanBatchOutput() {
        return scanBatchOutput;
    }

    public Result getResult() {
        return result;
    }

}
