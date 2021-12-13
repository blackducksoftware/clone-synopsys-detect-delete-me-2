package com.synopsys.integration.detector.evaluation;

import com.synopsys.integration.detector.base.DetectorEvaluation;

public interface DetectorEvaluatorListener {
    void applicableStarted(DetectorEvaluation detectorEvaluation);

    void applicableEnded(DetectorEvaluation detectorEvaluation);

    void extractableStarted(DetectorEvaluation detectorEvaluation);

    void extractableEnded(DetectorEvaluation detectorEvaluation);

    void extractionStarted(DetectorEvaluation detectorEvaluation);

    void extractionEnded(DetectorEvaluation detectorEvaluation);
}
