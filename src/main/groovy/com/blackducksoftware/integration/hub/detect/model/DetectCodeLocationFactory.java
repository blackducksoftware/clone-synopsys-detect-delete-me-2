package com.blackducksoftware.integration.hub.detect.model;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.detect.DetectConfiguration;

@Component
public class DetectCodeLocationFactory {

    @Autowired
    DetectConfiguration detectConfiguration;

    public BomDetectCodeLocation createBomCodeLocation(final BomToolType bomToolType, final File directory, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        final String relative = relativize(directory);
        final BomDetectCodeLocation codeLocation = new BomDetectCodeLocation(bomToolType, relative, externalId, dependencyGraph);
        return codeLocation;
    }

    public DockerDetectCodeLocation createDockerCodeLocation(final BomToolType bomToolType, final File directory, final String dockerImage, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        final String relative = relativize(directory);
        final DockerDetectCodeLocation codeLocation = new DockerDetectCodeLocation(relative, dockerImage, externalId, dependencyGraph);
        return codeLocation;
    }

    public ScanDetectCodeLocation createScanCodeLocation(final File directory, final ExternalId externalId, final DependencyGraph dependencyGraph) {
        final String relative = relativize(directory);
        final ScanDetectCodeLocation codeLocation = new ScanDetectCodeLocation(relative, externalId, dependencyGraph);
        return codeLocation;
    }

    private String relativize(final File file) {
        final Path path = file.toPath();
        final Path sourcePathPath = detectConfiguration.getSourceDirectory().getParentFile().toPath();
        final Path relativePath = sourcePathPath.relativize(path);
        final List<String> relativePieces = new ArrayList<>();
        for (int i = 0; i < relativePath.getNameCount(); i++) {
            relativePieces.add(relativePath.getName(i).toFile().getName());
        }
        final String relativePiece = relativePieces.stream().collect(Collectors.joining("/"));
        return relativePiece;
    }

}
