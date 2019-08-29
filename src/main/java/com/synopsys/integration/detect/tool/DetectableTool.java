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
package com.synopsys.integration.detect.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.DetectTool;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.impl.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.project.DetectToolProjectInfo;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.ExtractionEnvironment;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.ExceptionDetectableResult;
import com.synopsys.integration.detectable.detectables.docker.DockerExtractor;
import com.synopsys.integration.detector.base.DetectableCreatable;
import com.synopsys.integration.util.NameVersion;

public class DetectableTool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectableCreatable detectableCreatable;
    private final ExtractionEnvironmentProvider extractionEnvironmentProvider;
    private final CodeLocationConverter codeLocationConverter;
    private final String name;
    private final DetectTool detectTool;
    private final EventSystem eventSystem;

    public DetectableTool(final DetectableCreatable detectableCreatable, final ExtractionEnvironmentProvider extractionEnvironmentProvider, final CodeLocationConverter codeLocationConverter,
        final String name, final DetectTool detectTool, final EventSystem eventSystem) {
        this.codeLocationConverter = codeLocationConverter;
        this.name = name;
        this.detectableCreatable = detectableCreatable;
        this.extractionEnvironmentProvider = extractionEnvironmentProvider;
        this.detectTool = detectTool;
        this.eventSystem = eventSystem;
    }

    public DetectableToolResult execute(final File sourcePath) throws DetectUserFriendlyException { //TODO: Caller publishes result.
        logger.trace("Starting a detectable tool.");

        final DetectableEnvironment detectableEnvironment = new DetectableEnvironment(sourcePath);
        final Detectable detectable = detectableCreatable.createDetectable(detectableEnvironment);

        logger.info("Initializing " + detectable.getDescriptiveName());

        final DetectableResult applicable = detectable.applicable();

        if (!applicable.getPassed()) {
            logger.info("Was not applicable.");
            return DetectableToolResult.skip();
        }

        logger.info("Applicable passed.");

        DetectableResult extractable;
        Exception extractableException = null; //TODO: Move into DetectableResult base class.
        try {
            extractable = detectable.extractable();
        } catch (final DetectableException e) {
            extractable = new ExceptionDetectableResult(e);
            extractableException = e;
        }

        if (!extractable.getPassed()) {
            logger.error("Was not extractable: " + extractable.toDescription());
            eventSystem.publishEvent(Event.StatusSummary, new Status(name, StatusType.FAILURE));
            eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_GENERAL_ERROR, extractable.toDescription()));
            return DetectableToolResult.failed(Optional.ofNullable(extractableException));
        }

        logger.info("Extractable passed.");

        final ExtractionEnvironment extractionEnvironment = extractionEnvironmentProvider.createExtractionEnvironment(name);
        final Extraction extraction = detectable.extract(extractionEnvironment);

        if (!extraction.isSuccess()) {
            logger.error("Extraction was not success.");
            eventSystem.publishEvent(Event.StatusSummary, new Status(name, StatusType.FAILURE));
            eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_GENERAL_ERROR, extractable.toDescription()));
            return DetectableToolResult.failed(Optional.empty());
        } else {
            logger.info("Extraction success.");
            eventSystem.publishEvent(Event.StatusSummary, new Status(name, StatusType.SUCCESS));
        }

        final Map<CodeLocation, DetectCodeLocation> detectCodeLocationMap = codeLocationConverter.toDetectCodeLocation(extraction, sourcePath, name);
        final List<DetectCodeLocation> detectCodeLocations = new ArrayList<>(detectCodeLocationMap.values());

        Optional<DetectToolProjectInfo> detectToolProjectInfo = Optional.empty();
        if (StringUtils.isNotBlank(extraction.getProjectName()) || StringUtils.isNotBlank(extraction.getProjectVersion())) {
            final NameVersion nameVersion = new NameVersion(extraction.getProjectName(), extraction.getProjectVersion());
            detectToolProjectInfo = Optional.of(new DetectToolProjectInfo(detectTool, nameVersion));
        }

        final Optional<File> dockerTar = extraction.getMetaData(DockerExtractor.DOCKER_TAR_META_DATA);

        logger.info("Tool finished.");

        return DetectableToolResult.success(detectCodeLocations, detectToolProjectInfo, dockerTar);
    }
}
