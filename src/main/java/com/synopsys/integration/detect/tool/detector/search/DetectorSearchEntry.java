package com.synopsys.integration.detect.tool.detector.search;

import com.synopsys.integration.detector.base.DetectorType;

public class DetectorSearchEntry {
    public DetectorSearchEntry(final String location, final DetectorType detector, final Object detected) {
        this.location = location;
        this.detector = detector;
        this.detected = detected;
    }

    public String location;
    public DetectorType detector;
    public Object detected;
}
