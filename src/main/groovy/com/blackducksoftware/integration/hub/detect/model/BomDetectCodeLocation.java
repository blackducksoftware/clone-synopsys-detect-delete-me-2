package com.blackducksoftware.integration.hub.detect.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class BomDetectCodeLocation extends DetectCodeLocation {
    private final BomToolType bomToolType;
    private final String relativePath;

    public BomDetectCodeLocation(final BomToolType bomToolType, final String relativePath, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        super(externalId, dependencyGraph);
        this.bomToolType = bomToolType;
        this.relativePath = relativePath;
    }

    @Override
    public String getName(final String projectName, final String projectVersionName, final String prefix, final String suffix) {
        final List<String> pieces = Arrays.asList(getExternalId().getExternalIdPieces());
        final String externalIdName = pieces.stream().collect(Collectors.joining("/"));

        return createCommonName(Arrays.asList(relativePath, externalIdName), prefix, prefix, "bom", bomToolType.toString());

    }

    public String getRelativePath() {
        return relativePath;
    }

    public BomToolType getBomToolType() {
        return bomToolType;
    }
}
