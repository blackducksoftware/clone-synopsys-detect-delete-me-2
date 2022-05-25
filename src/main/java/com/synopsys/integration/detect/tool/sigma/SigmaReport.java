package com.synopsys.integration.detect.tool.sigma;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

public class SigmaReport {
    private final String scanTarget;
    @Nullable
    private final String errorMessage;

    public SigmaReport(String scanTarget, String errorMessage) {
        this.scanTarget = scanTarget;
        this.errorMessage = errorMessage;
    }

    public String getScanTarget() {
        return scanTarget;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
