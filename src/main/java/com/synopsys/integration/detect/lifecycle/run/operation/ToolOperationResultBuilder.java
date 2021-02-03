package com.synopsys.integration.detect.lifecycle.run.operation;

import com.synopsys.integration.detect.workflow.status.Status;

public class ToolOperationResultBuilder<T> {
    public OperationResult<T> build() {
        return OperationResult.success();//TODO
    }

    public void addStatusCode(Status status) {

    }

    public void requestExitCode(ExitCode code) {

    }
}
