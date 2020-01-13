/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.tool.detector.inspectors;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.workflow.ArtifactResolver;
import com.synopsys.integration.detect.workflow.ArtifactoryConstants;
import com.synopsys.integration.exception.IntegrationException;

public class GradleInspectorInstaller {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ArtifactResolver artifactResolver;

    public GradleInspectorInstaller(final ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    public Optional<String> findVersion(final String suppliedGradleInspectorVersion) {
        try {
            return Optional.of(artifactResolver.resolveArtifactVersion(ArtifactoryConstants.ARTIFACTORY_URL, ArtifactoryConstants.GRADLE_INSPECTOR_REPO, ArtifactoryConstants.GRADLE_INSPECTOR_PROPERTY, suppliedGradleInspectorVersion));
        } catch (final IntegrationException | IOException | DetectUserFriendlyException e) {
            logger.debug("Failed to fetch Gradle inspector version from Artifactory", e);
        }

        return Optional.empty();
    }
}
