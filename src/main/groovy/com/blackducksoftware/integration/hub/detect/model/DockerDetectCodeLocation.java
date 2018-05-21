package com.blackducksoftware.integration.hub.detect.model;

import java.util.Arrays;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public class DockerDetectCodeLocation extends DetectCodeLocation {
    private final String dockerImage;
    private final String relativePath;

    public DockerDetectCodeLocation(final String relativePath, final String dockerImage, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        super(externalId, dependencyGraph);
        this.dockerImage = dockerImage;
        this.relativePath = relativePath;
    }

    public String getName(final String projectName, final String projectVersionName,  final String prefix, final String suffix) {
        return createCommonName(Arrays.asList(relativePath, dockerImage), prefix, suffix, "docker", BomToolType.DOCKER.toString());
    }
}
