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
package com.synopsys.integration.detect.tool;

import com.synopsys.integration.detect.tool.detector.DetectorToolResult;
import com.synopsys.integration.util.NameVersion;

public class UniversalToolsResult {
    private final UniversalToolsResultType resultType;
    private final NameVersion nameVersion;
    private final DetectorToolResult detectorToolResult;
    private final DetectableToolResult dockerResult;
    private final DetectableToolResult bazelResult;

    public UniversalToolsResult(final UniversalToolsResultType resultType, final NameVersion nameVersion, final DetectorToolResult detectorToolResult, final DetectableToolResult dockerResult,
        final DetectableToolResult bazelResult) {
        this.resultType = resultType;
        this.nameVersion = nameVersion;
        this.detectorToolResult = detectorToolResult;
        this.dockerResult = dockerResult;
        this.bazelResult = bazelResult;
    }

    public static UniversalToolsResult failure(final NameVersion nameVersion, final DetectorToolResult detectorToolResult, final DetectableToolResult dockerResult,
        final DetectableToolResult bazelResult) {
        return new UniversalToolsResult(UniversalToolsResultType.FAILED, nameVersion, detectorToolResult, dockerResult, bazelResult);
    }

    public static UniversalToolsResult success(final NameVersion nameVersion, final DetectorToolResult detectorToolResult, final DetectableToolResult dockerResult,
        final DetectableToolResult bazelResult) {
        return new UniversalToolsResult(UniversalToolsResultType.SUCCESS, nameVersion, detectorToolResult, dockerResult, bazelResult);
    }

    public boolean anyFailed() {
        return resultType == UniversalToolsResultType.FAILED;
    }

    public NameVersion getNameVersion() {
        return nameVersion;
    }

    public DetectorToolResult getDetectorToolResult() {
        return detectorToolResult;
    }

    public DetectableToolResult getDockerResult() {
        return dockerResult;
    }

    public DetectableToolResult getBazelResult() {
        return bazelResult;
    }

    private enum UniversalToolsResultType {
        FAILED,
        SUCCESS
    }
}
