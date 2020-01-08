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
package com.synopsys.integration.detectable.detectables.gradle.inspection;

import com.synopsys.integration.detectable.detectables.gradle.inspection.inspector.GradleInspectorScriptOptions;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class GradleInspectorOptions {
    private final String gradleBuildCommand;
    private final GradleInspectorScriptOptions gradleInspectorScriptOptions;
    private final ProxyInfo proxyInfo;

    public GradleInspectorOptions(final String gradleBuildCommand, final GradleInspectorScriptOptions gradleInspectorScriptOptions, final ProxyInfo proxyInfo) {
        this.gradleBuildCommand = gradleBuildCommand;
        this.gradleInspectorScriptOptions = gradleInspectorScriptOptions;
        this.proxyInfo = proxyInfo;
    }

    public String getGradleBuildCommand() {
        return gradleBuildCommand;
    }

    public GradleInspectorScriptOptions getGradleInspectorScriptOptions() {
        return gradleInspectorScriptOptions;
    }

    public ProxyInfo getproxyInfo() {
        return proxyInfo;
    }
}
