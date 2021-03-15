/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.tool.detector.inspectors;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.workflow.ArtifactResolver;
import com.synopsys.integration.detect.workflow.ArtifactoryConstants;
import com.synopsys.integration.exception.IntegrationException;

public class DockerInspectorInstaller {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ArtifactResolver artifactResolver;

    public DockerInspectorInstaller(final ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public File installJar(final File dockerDirectory, final Optional<String> dockerVersion) throws IntegrationException, IOException, DetectUserFriendlyException {
        logger.info("Determining the location of the Docker inspector.");
        final String location = artifactResolver.resolveArtifactLocation(ArtifactoryConstants.ARTIFACTORY_URL, ArtifactoryConstants.DOCKER_INSPECTOR_REPO, ArtifactoryConstants.DOCKER_INSPECTOR_PROPERTY, dockerVersion.orElse(""),
            ArtifactoryConstants.DOCKER_INSPECTOR_VERSION_OVERRIDE);
        return download(location, dockerDirectory);
    }

    public File installAirGap(final File dockerDirectory) throws IntegrationException, IOException, DetectUserFriendlyException {
        logger.info("Determining the location of the Docker inspector.");
        final String location = artifactResolver.resolveArtifactLocation(ArtifactoryConstants.ARTIFACTORY_URL, ArtifactoryConstants.DOCKER_INSPECTOR_REPO, ArtifactoryConstants.DOCKER_INSPECTOR_AIR_GAP_PROPERTY, "", "");
        return download(location, dockerDirectory);
    }

    private File download(final String location, final File dockerDirectory) throws IntegrationException, IOException, DetectUserFriendlyException {
        logger.info("Finding or downloading the docker inspector.");
        logger.debug(String.format("Downloading docker inspector from '%s' to '%s'.", location, dockerDirectory.getAbsolutePath()));
        final File jarFile = artifactResolver.downloadOrFindArtifact(dockerDirectory, location);
        logger.info("Found online docker inspector: " + jarFile.getAbsolutePath());
        return jarFile;
    }

}
