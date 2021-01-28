package com.synopsys.integration.detect.workflow.report;

import java.util.List;

import com.synopsys.integration.detector.base.DetectorEvaluation;

public class EvaluationSummary {
    private final List<DetectorEvaluation> ready;
    private final List<DetectorEvaluation> notExtractable;
    private final List<DetectorEvaluation> failedWithFallback;
    private final List<DetectorEvaluation> failedNotSkipped;
    private final List<DetectorEvaluation> skippedFallbacks;

    public EvaluationSummary(final List<DetectorEvaluation> ready, final List<DetectorEvaluation> notExtractable, final List<DetectorEvaluation> failedWithFallback,
        final List<DetectorEvaluation> failedNotSkipped, final List<DetectorEvaluation> skippedFallbacks) {
        this.ready = ready;
        this.notExtractable = notExtractable;
        this.failedWithFallback = failedWithFallback;
        this.failedNotSkipped = failedNotSkipped;
        this.skippedFallbacks = skippedFallbacks;
    }

    public List<DetectorEvaluation> getReady() {
        return ready;
    }

    public List<DetectorEvaluation> getNotExtractable() {
        return notExtractable;
    }

    public List<DetectorEvaluation> getFailedWithFallback() {
        return failedWithFallback;
    }

    public List<DetectorEvaluation> getFailedNotSkipped() {
        return failedNotSkipped;
    }

    public List<DetectorEvaluation> getSkippedFallbacks() {
        return skippedFallbacks;
    }
}
