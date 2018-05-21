/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;

public abstract class DetectCodeLocation {
    private final ExternalId externalId;
    private final DependencyGraph dependencyGraph;

    public DetectCodeLocation(final ExternalId externalId, final DependencyGraph dependencyGraph) {
        this.externalId = externalId;
        this.dependencyGraph = dependencyGraph;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public DependencyGraph getDependencyGraph() {
        return dependencyGraph;
    }

    protected String createCommonName(final List<String> primaryPieces, final String prefix, final String suffix, final String codeLocationType) {
        return createCommonName(primaryPieces, prefix, suffix, codeLocationType, "");
    }

    protected String createCommonName(final List<String> primaryPieces, final String prefix, final String suffix, final String codeLocationType, final String bomToolType) {
        String name = primaryPieces.stream().collect(Collectors.joining("/"));

        if (StringUtils.isNotBlank(prefix)) {
            name = String.format("%s/%s", prefix, name);
        }
        if (StringUtils.isNotBlank(suffix)) {
            name = String.format("%s/%s", name, suffix);
        }

        String endPiece = codeLocationType;
        if (StringUtils.isNotBlank(bomToolType)) {
            endPiece = String.format("%s/%s", bomToolType, endPiece);
        }

        name = String.format("%s %s", name, endPiece);
        return name;
    }
}
