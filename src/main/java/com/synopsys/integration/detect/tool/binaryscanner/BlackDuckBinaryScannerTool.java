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
package com.synopsys.integration.detect.tool.binaryscanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScan;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatch;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanOutput;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanUploadService;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.util.DetectZipUtil;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckBinaryScannerTool {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckBinaryScannerTool.class);
    private static final String STATUS_KEY = "BINARY_SCAN";

    private final CodeLocationNameManager codeLocationNameManager;
    private final DirectoryManager directoryManager;
    private final FileFinder fileFinder;
    private final BinaryScanOptions binaryScanOptions;
    private final BlackDuckServicesFactory blackDuckServicesFactory;
    private final EventSystem eventSystem;

    public BlackDuckBinaryScannerTool(EventSystem eventSystem, CodeLocationNameManager codeLocationNameManager, DirectoryManager directoryManager, FileFinder fileFinder, BinaryScanOptions binaryScanOptions,
        BlackDuckServicesFactory blackDuckServicesFactory) {
        this.codeLocationNameManager = codeLocationNameManager;
        this.directoryManager = directoryManager;
        this.fileFinder = fileFinder;
        this.binaryScanOptions = binaryScanOptions;
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.eventSystem = eventSystem;
    }

    public boolean shouldRun() {
        if (binaryScanOptions.getSingleTargetFilePath().isPresent()) {
            logger.info("Binary scan will upload the single provided binary file path.");
            return true;
        } else if (binaryScanOptions.getMultipleTargetFileNamePatterns().stream().anyMatch(StringUtils::isNotBlank)) {
            logger.info("Binary scan will upload all files in the source directory that match the provided name patterns.");
            return true;
        }
        return false;
    }

    public BinaryScanToolResult performBinaryScanActions(NameVersion projectNameVersion) throws DetectUserFriendlyException {

        File binaryUpload = null;
        Optional<Path> singleTargetFilePath = binaryScanOptions.getSingleTargetFilePath();
        if (singleTargetFilePath.isPresent()) {
            binaryUpload = singleTargetFilePath.get().toFile();
        } else if (binaryScanOptions.getMultipleTargetFileNamePatterns().stream().anyMatch(StringUtils::isNotBlank)) {
            List<File> multipleTargets = fileFinder.findFiles(directoryManager.getSourceDirectory(), binaryScanOptions.getMultipleTargetFileNamePatterns(), 0);
            if (multipleTargets != null && multipleTargets.size() > 0) {
                logger.info("Binary scan found {} files to archive for binary scan upload.", multipleTargets.size());
                try {
                    final String zipPath = "binary-upload.zip";
                    File zip = new File(directoryManager.getBinaryOutputDirectory(), zipPath);
                    Map<String, Path> uploadTargets = multipleTargets.stream().collect(Collectors.toMap(File::getName, File::toPath));
                    DetectZipUtil.zip(zip, uploadTargets);
                    logger.info("Binary scan created the following zip for upload: " + zip.toPath());
                    binaryUpload = zip;
                } catch (IOException e) {
                    throw new DetectUserFriendlyException("Unable to create binary scan archive for upload.", e, ExitCodeType.FAILURE_UNKNOWN_ERROR);
                }
            }
        }

        if (binaryUpload != null && binaryUpload.isFile() && binaryUpload.canRead()) {
            String name = projectNameVersion.getName();
            String version = projectNameVersion.getVersion();
            BinaryScanUploadService uploadService = blackDuckServicesFactory.createBinaryScanUploadService();
            CodeLocationCreationData<BinaryScanBatchOutput> codeLocationCreationData = uploadBinaryScanFile(uploadService, binaryUpload, name, version);
            return BinaryScanToolResult.SUCCESS(codeLocationCreationData);
        } else {
            logger.warn("Binary scan file did not exist, is not a file or can't be read.");
            eventSystem.publishEvent(Event.StatusSummary, new Status(STATUS_KEY, StatusType.FAILURE));
            eventSystem.publishEvent(Event.Issue, new DetectIssue(DetectIssueType.BINARY_SCAN, Arrays.asList("Binary scan file did not exist, is not a file or can't be read.")));
            eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_BLACKDUCK_FEATURE_ERROR, STATUS_KEY));
            return BinaryScanToolResult.FAILURE();
        }
    }

    public CodeLocationCreationData<BinaryScanBatchOutput> uploadBinaryScanFile(BinaryScanUploadService binaryScanUploadService, File binaryScanFile, String projectName, String projectVersionName)
        throws DetectUserFriendlyException {
        String prefix = binaryScanOptions.getCodeLocationPrefix();
        String suffix = binaryScanOptions.getCodeLocationSuffix();
        String codeLocationName = codeLocationNameManager.createBinaryScanCodeLocationName(binaryScanFile, projectName, projectVersionName, prefix, suffix);
        try {
            logger.info("Preparing to upload binary scan file: " + codeLocationName);
            BinaryScan binaryScan = new BinaryScan(binaryScanFile, projectName, projectVersionName, codeLocationName);
            BinaryScanBatch binaryScanBatch = new BinaryScanBatch(binaryScan);
            CodeLocationCreationData<BinaryScanBatchOutput> codeLocationCreationData = binaryScanUploadService.uploadBinaryScan(binaryScanBatch);

            BinaryScanBatchOutput binaryScanBatchOutput = codeLocationCreationData.getOutput();
            // The throwExceptionForError() in BinaryScanBatchOutput has a bug, so doing that work here
            throwExceptionForError(binaryScanBatchOutput);

            logger.info("Successfully uploaded binary scan file: " + codeLocationName);
            eventSystem.publishEvent(Event.StatusSummary, new Status(STATUS_KEY, StatusType.SUCCESS));
            return codeLocationCreationData;
        } catch (IntegrationException e) {
            logger.error("Failed to upload binary scan file: " + e.getMessage());
            eventSystem.publishEvent(Event.StatusSummary, new Status(STATUS_KEY, StatusType.FAILURE));
            eventSystem.publishEvent(Event.Issue, new DetectIssue(DetectIssueType.EXCEPTION, Arrays.asList(e.getMessage())));
            throw new DetectUserFriendlyException("Failed to upload binary scan file.", e, ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY);
        }
    }

    private void throwExceptionForError(BinaryScanBatchOutput binaryScanBatchOutput) throws BlackDuckIntegrationException {
        for (BinaryScanOutput binaryScanOutput : binaryScanBatchOutput) {
            if (binaryScanOutput.getResult() == Result.FAILURE) {
                String uploadErrorMessage = String.format("Error when uploading binary scan: %s (Black Duck response: %s)",
                    binaryScanOutput.getErrorMessage().orElse(binaryScanOutput.getStatusMessage()),
                    binaryScanOutput.getResponse());
                logger.error(uploadErrorMessage);
                throw new BlackDuckIntegrationException(uploadErrorMessage);
            }
        }
    }
}
