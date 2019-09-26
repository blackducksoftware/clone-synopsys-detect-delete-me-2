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
package com.synopsys.integration.detect.lifecycle.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.detect.DetectTool;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detect.workflow.project.DetectToolProjectInfo;
import com.synopsys.integration.util.NameVersion;

public class RunResult {
    private Optional<File> dockerTar = Optional.empty();
    private final List<DetectToolProjectInfo> detectToolProjectInfo = new ArrayList<>();
    private final List<DetectCodeLocation> detectCodeLocations = new ArrayList<>();

    public void addToolNameVersionIfPresent(final DetectTool detectTool, final Optional<NameVersion> toolNameVersion) {
        if (toolNameVersion.isPresent()) {
            final DetectToolProjectInfo dockerProjectInfo = new DetectToolProjectInfo(detectTool, new NameVersion(toolNameVersion.get().getName(), toolNameVersion.get().getVersion()));
            detectToolProjectInfo.add(dockerProjectInfo);
        }
    }

    public void addDetectCodeLocations(final List<DetectCodeLocation> codeLocations) {
        detectCodeLocations.addAll(codeLocations);
    }

    public void addDetectableToolResult(final DetectableToolResult detectableToolResult) {
        detectableToolResult.getDetectToolProjectInfo().ifPresent(detectToolProjectInfo1 -> addToolNameVersionIfPresent(detectToolProjectInfo1.getDetectTool(), Optional.of(detectToolProjectInfo1.getSuggestedNameVersion())));
        detectableToolResult.getDockerTar().ifPresent(dockerTar -> addDockerFile(Optional.of(dockerTar)));
        detectCodeLocations.addAll(detectableToolResult.getDetectCodeLocations());
    }

    public void addDockerFile(final Optional<File> dockerFile) {
        dockerTar = dockerFile;
    }

    public Optional<File> getDockerTar() {
        return dockerTar;
    }

    public List<DetectToolProjectInfo> getDetectToolProjectInfo() {
        return detectToolProjectInfo;
    }

    public List<DetectCodeLocation> getDetectCodeLocations() {
        return detectCodeLocations;
    }
}
