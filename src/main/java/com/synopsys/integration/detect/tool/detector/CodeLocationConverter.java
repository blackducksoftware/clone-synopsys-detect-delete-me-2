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
package com.synopsys.integration.detect.tool.detector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detector.base.DetectorEvaluation;

public class CodeLocationConverter {
    public Map<CodeLocation, DetectCodeLocation> toDetectCodeLocation(final DetectorEvaluation evaluation) {
        final Map<CodeLocation, DetectCodeLocation> detectCodeLocations = new HashMap<>();
        if (evaluation.wasExtractionSuccessful()) {
            final Extraction extraction = evaluation.getExtraction();
            final String name = evaluation.getDetectorRule().getDetectorType().toString();
            return toDetectCodeLocation(extraction, evaluation.getDetectableEnvironment().getDirectory(), name);
        }
        return detectCodeLocations;
    }

    public Map<CodeLocation, DetectCodeLocation> toDetectCodeLocation(final Extraction extraction, final File overridePath, final String creatorName) {
        final Map<CodeLocation, DetectCodeLocation> detectCodeLocations = new HashMap<>();

        for (final CodeLocation codeLocation : extraction.getCodeLocations()) {
            final File sourcePath = codeLocation.getSourcePath().orElse(overridePath);
            final DetectCodeLocation detectCodeLocation = new DetectCodeLocation(codeLocation.getDependencyGraph(), codeLocation.getCodeLocationId().orElse(null), sourcePath, creatorName);
            detectCodeLocations.put(codeLocation, detectCodeLocation);
        }

        return detectCodeLocations;
    }
}
