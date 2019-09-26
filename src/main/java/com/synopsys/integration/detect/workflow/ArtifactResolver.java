/**
 * synopsys-detect
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.detect.workflow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.detect.configuration.ConnectionManager;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class ArtifactResolver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConnectionManager connectionManager;
    private final Gson gson;

    public ArtifactResolver(final ConnectionManager connectionManager, final Gson gson) {
        this.connectionManager = connectionManager;
        this.gson = gson;
    }

    /**
     * Communicates with Artifactory to find the location of an artifact.
     * Will either return the url of the given artifactory property or will calculate url the given version would point to.
     * @param artifactoryBaseUrl      The base url of artifactory, such as http://int-jfrog01.dc1.lan:8081/artifactory/
     * @param repositoryUrl           The url of the repository with the artifact, such as bds-integrations-release/com/blackducksoftware/integration/integration-gradle-inspector
     * @param propertyKey             The property to find, such as DETECT_GRADLE_INSPECTOR_LATEST_0
     * @param overrideVersion         The version to use, if provided, overrides the property tag.
     * @param overrideArtifactPattern The pattern to use when the override version is provided of the full artifact location.
     * @return the location of the artifact
     */
    public String resolveArtifactLocation(final String artifactoryBaseUrl, final String repositoryUrl, final String propertyKey, final String overrideVersion, final String overrideArtifactPattern)
        throws IntegrationException, DetectUserFriendlyException, IOException {
        if (StringUtils.isNotBlank(overrideVersion) && StringUtils.isNotBlank(overrideArtifactPattern)) {
            logger.debug("An override version was provided, will resolve using the given version.");
            String repoUrl = artifactoryBaseUrl + repositoryUrl;
            String versionUrl = overrideArtifactPattern.replace(ArtifactoryConstants.VERSION_PLACEHOLDER, overrideVersion);
            String artifactUrl = repoUrl + versionUrl;
            logger.debug("Determined the artifact url is: " + artifactUrl);
            return artifactUrl;
        } else {
            logger.debug("Will find version from artifactory.");
            String apiUrl = artifactoryBaseUrl + "api/storage/" + repositoryUrl;
            logger.debug(String.format("Checking '%s' for property '%s'.", apiUrl, propertyKey));
            return downloadProperty(apiUrl, propertyKey);
        }
    }

    /**
     * Communicates with Artifactory to find the actual version of an artifact.
     * @param artifactoryBaseUrl The base url of artifactory, such as http://int-jfrog01.dc1.lan:8081/artifactory/
     * @param repositoryUrl      The url of the repository with the artifact, such as bds-integrations-release/com/blackducksoftware/integration/integration-gradle-inspector
     * @param propertyKey        The property to find, such as DETECT_GRADLE_INSPECTOR_LATEST_0
     * @param overrideVersion    The version to use, if provided, overrides the property tag.
     * @return the calculated version of the artifact
     */
    public String resolveArtifactVersion(final String artifactoryBaseUrl, final String repositoryUrl, final String propertyKey, final String overrideVersion) throws IntegrationException, DetectUserFriendlyException, IOException {
        if (StringUtils.isNotBlank(overrideVersion)) {
            logger.debug("Resolved version from override: " + overrideVersion);
            return overrideVersion;
        } else {
            logger.debug(String.format("Resolving artifact version from repository %s with property %s", repositoryUrl, propertyKey));
            String apiUrl = artifactoryBaseUrl + "api/storage/" + repositoryUrl;
            String artifactVersion = downloadProperty(apiUrl, propertyKey);
            logger.debug("Resolved version online: " + artifactVersion);
            return artifactVersion;
        }
    }

    private String downloadProperty(String apiUrl, String propertyKey) throws IntegrationException, DetectUserFriendlyException, IOException {
        String propertyUrl = apiUrl + "?properties=" + propertyKey;
        logger.debug("Downloading property: " + propertyUrl);
        final Request request = new Request.Builder().uri(propertyUrl).build();
        final IntHttpClient restConnection = connectionManager.createUnauthenticatedRestConnection(propertyUrl);
        try (final Response response = restConnection.execute(request)) {
            try (final InputStreamReader reader = new InputStreamReader(response.getContent())) {
                logger.debug("Downloaded property, attempting to parse response.");
                JsonObject json = gson.fromJson(reader, JsonElement.class).getAsJsonObject();
                JsonObject propertyMap = json.getAsJsonObject("properties");
                JsonArray propertyUrls = propertyMap.getAsJsonArray(propertyKey);
                String foundProperty = propertyUrls.get(0).getAsString();
                logger.debug("Successfully parsed property: " + propertyUrls);
                return foundProperty;
            }
        }
    }

    public String parseFileName(String source) {
        String[] pieces = source.split("/");
        String filename = pieces[pieces.length - 1];
        return filename;
    }

    public File downloadOrFindArtifact(File targetDir, String source) throws IntegrationException, DetectUserFriendlyException, IOException {
        logger.debug("Downloading or finding artifact.");
        String fileName = parseFileName(source);
        logger.debug("Determined filename would be: " + fileName);
        File fileTarget = new File(targetDir, fileName);
        logger.debug(String.format("Looking for artifact at '%s' or downloading from '%s'.", fileTarget.getAbsolutePath(), source));
        if (fileTarget.exists()) {
            logger.debug("Artifact exists. Returning existing file.");
            return fileTarget;
        } else {
            logger.debug("Artifact does not exist. Will attempt to download it.");
            return downloadArtifact(fileTarget, source);
        }
    }

    public File downloadArtifact(File target, String source) throws DetectUserFriendlyException, IntegrationException, IOException {
        logger.debug(String.format("Downloading for artifact to '%s' from '%s'.", target.getAbsolutePath(), source));
        final Request request = new Request.Builder().uri(source).build();
        final IntHttpClient restConnection = connectionManager.createUnauthenticatedRestConnection(source);
        try (Response response = restConnection.execute(request)) {
            logger.debug("Deleting existing file.");
            target.delete();
            logger.debug("Writing to file.");
            final InputStream jarBytesInputStream = response.getContent();
            FileUtils.copyInputStreamToFile(jarBytesInputStream, target);
            logger.debug("Successfully wrote response to file.");
            return target;
        }
    }

}
