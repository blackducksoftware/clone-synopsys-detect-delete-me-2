/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detect.workflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.util.ResourceUtil;

/**
 * NOTICE: A copy of this class exists in buildSrc, copy all changes to build source!
 **/
public class ArtifactoryDetails {
    public static final String VERSION_PLACEHOLDER = "<VERSION>";

    public static ArtifactoryDetails fromResources(Gson gson) throws IOException {
        String json = ResourceUtil.getResourceAsString(ArtifactoryDetails.class, "/artifactory.json", StandardCharsets.UTF_8.toString());
        return gson.fromJson(json, ArtifactoryDetails.class);
    }

    @SerializedName("ARTIFACTORY_URL")
    public String artifactoryUrl;

    @SerializedName("GRADLE_INSPECTOR_REPO")
    public String gradleInspectorRepo;
    @SerializedName("GRADLE_INSPECTOR_PROPERTY")
    public String gradleInspectorProperty;
    @SerializedName("GRADLE_INSPECTOR_MAVEN_REPO")
    public String gradleInspectorMavenRepo;

    @SerializedName("NUGET_DOTNET3_INSPECTOR_REPO")
    public String nugetDotnet3InspectorRepo;
    @SerializedName("NUGET_DOTNET3_INSPECTOR_PROPERTY")
    public String nugetDotnet3InspectorProperty;
    @SerializedName("NUGET_DOTNET3_INSPECTOR_VERSION_OVERRIDE")
    public String nugetDotnet3InspectorVersionOverride;

    @SerializedName("NUGET_INSPECTOR_REPO")
    public String nugetInspectorRepo;
    @SerializedName("NUGET_INSPECTOR_PROPERTY")
    public String nugetInspectorProperty;
    @SerializedName("NUGET_INSPECTOR_VERSION_OVERRIDE")
    public String nugetInspectorVersionOverride;

    @SerializedName("CLASSIC_NUGET_INSPECTOR_REPO")
    public String classicNugetInspectorRepo;
    @SerializedName("CLASSIC_NUGET_INSPECTOR_PROPERTY")
    public String classicNugetInspectorProperty;
    @SerializedName("CLASSIC_NUGET_INSPECTOR_VERSION_OVERRIDE")
    public String classicNugetInspectorVersionOverride;

    @SerializedName("DOCKER_INSPECTOR_REPO")
    public String dockerInspectorRepo;
    @SerializedName("DOCKER_INSPECTOR_PROPERTY")
    public String dockerInspectorProperty;
    @SerializedName("DOCKER_INSPECTOR_AIR_GAP_PROPERTY")
    public String dockerInspectorAirGapProperty;
    @SerializedName("DOCKER_INSPECTOR_VERSION_OVERRIDE")
    public String dockerInspectorVersionOverride;
}
