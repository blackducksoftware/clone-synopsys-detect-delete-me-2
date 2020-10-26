package com.synopsys.integration.detect.tool.detector.search;

import java.util.ArrayList;
import java.util.List;

public class DetectorSearchResults {
    public DetectorSearchResults(final List<DetectorSearchEntry> entries) {
        this.entries = entries;
    }

    public List<DetectorSearchEntry> entries = new ArrayList<>();
}

