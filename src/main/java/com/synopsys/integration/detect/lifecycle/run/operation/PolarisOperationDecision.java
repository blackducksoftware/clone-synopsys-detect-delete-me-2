package com.synopsys.integration.detect.lifecycle.run.operation;

public class PolarisOperationDecision {
    public PolarisOperationDecision(final boolean shouldExecute) {
        this.shouldExecute = shouldExecute;
    }

    private final boolean shouldExecute;

    public boolean isShouldExecute() {
        return shouldExecute;
    }

}
