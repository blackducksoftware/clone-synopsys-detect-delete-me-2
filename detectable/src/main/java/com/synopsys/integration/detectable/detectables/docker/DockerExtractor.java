/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.docker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.bdio.BdioReader;
import com.synopsys.integration.bdio.BdioTransformer;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.BdioId;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.ExtractionMetadata;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectables.docker.model.DockerImageInfo;

public class DockerExtractor {
    public static final ExtractionMetadata<File> DOCKER_TAR_META_DATA = new ExtractionMetadata<>("dockerTar", File.class);
    public static final ExtractionMetadata<String> DOCKER_IMAGE_NAME_META_DATA = new ExtractionMetadata<>("dockerImage", String.class);
    public static final ExtractionMetadata<String> DOCKER_IMAGE_ID_META_DATA = new ExtractionMetadata<>("dockerImageId", String.class);

    public static final String CONTAINER_FILESYSTEM_FILENAME_PATTERN = "*_containerfilesystem.tar.gz";
    public static final String SQUASHED_IMAGE_FILENAME_PATTERN = "*_squashedimage.tar.gz";
    public static final String RESULTS_FILENAME_PATTERN = "results.json";
    public static final String DEPENDENCIES_PATTERN = "*bdio.jsonld";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileFinder fileFinder;
    private final ExecutableRunner executableRunner;
    private final BdioTransformer bdioTransformer;
    private final ExternalIdFactory externalIdFactory;
    private final Gson gson;
    private final BashDockerRunner bashDockerRunner;

    public DockerExtractor(final FileFinder fileFinder, final ExecutableRunner executableRunner, final BdioTransformer bdioTransformer, final ExternalIdFactory externalIdFactory, final Gson gson,
        final BashDockerRunner bashDockerRunner) {
        this.fileFinder = fileFinder;
        this.executableRunner = executableRunner;
        this.bdioTransformer = bdioTransformer;
        this.externalIdFactory = externalIdFactory;
        this.gson = gson;
        this.bashDockerRunner = bashDockerRunner;
    }

    public Extraction extract(final File directory, final File outputDirectory, final File bashExe, final File javaExe, final String image, final String imageId, final String tar, final DockerInspectorInfo dockerInspectorInfo,
        DockerProperties dockerProperties) {
        try {
            String imageArgument = null;
            String imagePiece = null;
            ImageIdentifierType imageIdentifierType = null;
            if (StringUtils.isNotBlank(tar)) {
                final File dockerTarFile = new File(tar);
                imageArgument = String.format("--docker.tar=%s", dockerTarFile.getCanonicalPath());
                imagePiece = dockerTarFile.getName();
                imageIdentifierType = ImageIdentifierType.TAR;
            } else if (StringUtils.isNotBlank(image)) {
                imagePiece = image;
                imageArgument = String.format("--docker.image=%s", image);
                imageIdentifierType = ImageIdentifierType.IMAGE_NAME;
            } else if (StringUtils.isNotBlank(imageId)) {
                imagePiece = imageId;
                imageArgument = String.format("--docker.image.id=%s", imageId);
                imageIdentifierType = ImageIdentifierType.IMAGE_ID;
            }

            if (StringUtils.isBlank(imageArgument) || StringUtils.isBlank(imagePiece)) {
                return new Extraction.Builder().failure("No docker image found.").build();
            } else {
                bashDockerRunner.executeDocker(outputDirectory, imageArgument, javaExe, bashExe, dockerInspectorInfo, dockerProperties);
                File scanFile = findScanFile(outputDirectory, tar);
                final Extraction.Builder extractionBuilder = findCodeLocations(outputDirectory, directory);
                final String imageIdentifier = getImageIdentifierFromOutputDirectoryIfImageIdPresent(outputDirectory, imagePiece, imageIdentifierType);
                extractionBuilder.metaData(DOCKER_TAR_META_DATA, scanFile).metaData(DOCKER_IMAGE_NAME_META_DATA, imageIdentifier);
                return extractionBuilder.build();
            }
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

    private File findScanFile(File outputDirectory, String dockerTarFilePath) throws IOException {
        final File producedSquashedImageFile = fileFinder.findFile(outputDirectory, SQUASHED_IMAGE_FILENAME_PATTERN);
        final File producedContainerFileSystemFile = fileFinder.findFile(outputDirectory, CONTAINER_FILESYSTEM_FILENAME_PATTERN);
        if (null != producedSquashedImageFile && producedSquashedImageFile.isFile()) {
            logger.debug(String.format("Will signature scan: %s", producedSquashedImageFile.getAbsolutePath()));
            return producedSquashedImageFile;
        } else if (null != producedContainerFileSystemFile && producedContainerFileSystemFile.isFile()) {
            logger.debug(String.format("Will signature scan: %s", producedContainerFileSystemFile.getAbsolutePath()));
            return producedContainerFileSystemFile;
        } else {
            logger.debug(String.format("No files found matching pattern [%s]. Expected docker-inspector to produce file in %s", CONTAINER_FILESYSTEM_FILENAME_PATTERN, outputDirectory.getCanonicalPath()));
            if (StringUtils.isNotBlank(dockerTarFilePath)) {
                final File dockerTarFile = new File(dockerTarFilePath);
                if (dockerTarFile.isFile()) {
                    logger.debug(String.format("Will scan the provided Docker tar file %s", dockerTarFile.getCanonicalPath()));
                    return dockerTarFile;
                }
            }
        }
        return null;
    }

    private Extraction.Builder findCodeLocations(final File directoryToSearch, final File directory) {
        final File bdioFile = fileFinder.findFile(directoryToSearch, DEPENDENCIES_PATTERN);
        if (bdioFile != null) {
            SimpleBdioDocument simpleBdioDocument = null;

            try (final InputStream dockerOutputInputStream = new FileInputStream(bdioFile); final BdioReader bdioReader = new BdioReader(gson, dockerOutputInputStream)) {
                simpleBdioDocument = bdioReader.readSimpleBdioDocument();
            } catch (final Exception e) {
                return new Extraction.Builder().exception(e);
            }

            if (simpleBdioDocument != null) {
                final DependencyGraph dependencyGraph = bdioTransformer.transformToDependencyGraph(simpleBdioDocument.getProject(), simpleBdioDocument.getComponents());

                final String projectName = simpleBdioDocument.getProject().name;
                final String projectVersionName = simpleBdioDocument.getProject().version;

                // TODO ejk - update this when project external id is not req'd anymore
                final Forge dockerForge = new Forge(BdioId.BDIO_ID_SEPARATOR, simpleBdioDocument.getProject().bdioExternalIdentifier.forge);
                final String externalIdPath = simpleBdioDocument.getProject().bdioExternalIdentifier.externalId;
                final ExternalId projectExternalId = externalIdFactory.createPathExternalId(dockerForge, externalIdPath);

                final CodeLocation detectCodeLocation = new CodeLocation(dependencyGraph, projectExternalId);

                return new Extraction.Builder().success(detectCodeLocation).projectName(projectName).projectVersion(projectVersionName);
            }
        }

        return new Extraction.Builder().failure("No files found matching pattern [" + DEPENDENCIES_PATTERN + "]. Expected docker-inspector to produce file in " + directory.toString());
    }

    public String getImageIdentifierFromOutputDirectoryIfImageIdPresent(File outputDirectory, String suppliedImagePiece, ImageIdentifierType imageIdentifierType) {
        final File producedResultFile = fileFinder.findFile(outputDirectory, RESULTS_FILENAME_PATTERN);
        if (imageIdentifierType.equals(ImageIdentifierType.IMAGE_ID) && producedResultFile != null) {
            String jsonText;
            try {
                jsonText = FileUtils.readFileToString(producedResultFile, StandardCharsets.UTF_8);
                DockerImageInfo dockerImageInfo = gson.fromJson(jsonText, DockerImageInfo.class);
                final String imageRepo = dockerImageInfo.getImageRepo();
                final String imageTag = dockerImageInfo.getImageTag();
                if (StringUtils.isNotBlank(imageRepo) && StringUtils.isNotBlank(imageTag)) {
                    return imageRepo + ":" + imageTag;
                }
            } catch (final IOException | JsonSyntaxException e) {
                logger.debug("Failed to parse results file from run of Docker Inspector, thus could not get name of image.  The code location name for this scan will be derived from the passed image ID");
            }
        }
        return suppliedImagePiece;
    }

}
