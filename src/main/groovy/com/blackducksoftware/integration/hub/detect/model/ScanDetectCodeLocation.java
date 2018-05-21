package com.blackducksoftware.integration.hub.detect.model;

import java.util.Arrays;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class ScanDetectCodeLocation extends DetectCodeLocation {
    private final String relativePath;

    public ScanDetectCodeLocation(final String relativePath, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        super(externalId, dependencyGraph);
        this.relativePath = relativePath;
    }

    public String getName(final String projectName, final String projectVersionName,  final String prefix, final String suffix) {
        return createCommonName(Arrays.asList(relativePath, projectName, projectVersionName), prefix, suffix, "scan");
    }

}
