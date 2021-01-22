package com.synopsys.integration.detect.workflow.analyze;

import java.util.Set;

import com.synopsys.integration.detector.base.DetectorType;

public class DetectorsAtDepths {
    private final Set<DetectorType> detectorTypes;
    private final int depth;

    public DetectorsAtDepths(final Set<DetectorType> detectorTypes, final int depth) {
        this.detectorTypes = detectorTypes;
        this.depth = depth;
    }

    public Set<DetectorType> getDetectorTypes() {
        return detectorTypes;
    }

    public int getDepth() {
        return depth;
    }
}
