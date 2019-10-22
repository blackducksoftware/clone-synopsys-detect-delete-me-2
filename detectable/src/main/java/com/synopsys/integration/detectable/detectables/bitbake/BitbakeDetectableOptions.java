/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.bitbake;

public class BitbakeDetectableOptions {
    private final String buildEnvName;
    private final String[] sourceArguments;
    private final String[] packageNames;
    private final Integer searchDepth;

    public BitbakeDetectableOptions(final String buildEnvName, final String[] sourceArguments, final String[] packageNames, final Integer searchDepth) {
        this.buildEnvName = buildEnvName;
        this.sourceArguments = sourceArguments;
        this.packageNames = packageNames;
        this.searchDepth = searchDepth;
    }

    public String getBuildEnvName() {
        return buildEnvName;
    }

    public String[] getSourceArguments() {
        return sourceArguments;
    }

    public String[] getPackageNames() {
        return packageNames;
    }

    public Integer getSearchDepth() {
        return searchDepth;
    }
}
