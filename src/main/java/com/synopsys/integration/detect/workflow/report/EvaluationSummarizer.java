package com.synopsys.integration.detect.workflow.report;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.detect.workflow.report.util.DetectorEvaluationUtils;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;

public class EvaluationSummarizer {
    public EvaluationSummary summarize(DetectorEvaluationTree detectorEvaluationTree) {
        List<DetectorEvaluation> applicable = DetectorEvaluationUtils.applicableChildren(detectorEvaluationTree);
        List<DetectorEvaluation> ready = applicable.stream().filter(DetectorEvaluation::isExtractable).collect(Collectors.toList());
        List<DetectorEvaluation> notExtractable = applicable.stream().filter(it -> !it.isExtractable()).collect(Collectors.toList());
        List<DetectorEvaluation> failedNoFallback = notExtractable.stream().filter(it -> !it.isFallbackExtractable()).collect(Collectors.toList());
        List<DetectorEvaluation> failedWithFallback = notExtractable.stream().filter(DetectorEvaluation::isFallbackExtractable).collect(Collectors.toList());

        List<DetectorEvaluation> skippedFallbacks = ready.stream().flatMap(it -> it.getFallbacks().stream()).collect(Collectors.toList());
        List<DetectorEvaluation> failedNotSkipped = failedNoFallback.stream()
                                                        .filter(it -> !skippedFallbacks.contains(it))
                                                        .collect(Collectors.toList());

        return new EvaluationSummary(ready, notExtractable, failedWithFallback, failedNotSkipped, skippedFallbacks);

    }
}
