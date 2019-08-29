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
package com.synopsys.integration.detect.workflow.codelocation;

import java.io.File;
import java.util.Optional;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocationId;

public class DetectCodeLocation extends CodeLocation {
    private final String creatorName;

    public DetectCodeLocation(final DependencyGraph dependencyGraph, final CodeLocationId codeLocationId, final File sourcePath, final String creatorName) {
        super(dependencyGraph, codeLocationId, sourcePath);
        this.creatorName = creatorName;
    }

    public DetectCodeLocation copy(final DependencyGraph dependencyGraph) {
        return new DetectCodeLocation(dependencyGraph, codeLocationId, sourcePath, creatorName);
    }

    public Optional<String> getCreatorName() {
        return Optional.ofNullable(creatorName);
    }
}
