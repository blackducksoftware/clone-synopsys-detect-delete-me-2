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
package com.synopsys.integration.detect.workflow.blackduck;

public class DetectProjectServiceOptions {
    private final String projectVersionPhase;
    private final String projectVersionDistribution;
    private final Integer projectTier;
    private final String projectDescription;
    private final String projectVersionNotes;
    private final String[] cloneCategories;
    private final Boolean projectLevelAdjustments;
    private final Boolean forceProjectVersionUpdate;
    private final String cloneVersionName;
    private final String projectVersionNickname;
    private final String applicationId;
    private final String[] tags;
    private final String[] groups;
    private final String parentProjectName;
    private final String parentProjectVersion;
    private final Boolean cloneLatestProjectVersion;
    private final CustomFieldDocument customFields;

    public DetectProjectServiceOptions(final String projectVersionPhase, final String projectVersionDistribution, final Integer projectTier, final String projectDescription, final String projectVersionNotes,
        final String[] cloneCategories, final Boolean projectLevelAdjustments, final Boolean forceProjectVersionUpdate, final String cloneVersionName, final String projectVersionNickname, final String applicationId,
        final String[] tags, final String[] groups, final String parentProjectName, final String parentProjectVersion, final Boolean cloneLatestProjectVersion, final CustomFieldDocument customFields) {
        this.projectVersionPhase = projectVersionPhase;
        this.projectVersionDistribution = projectVersionDistribution;
        this.projectTier = projectTier;
        this.projectDescription = projectDescription;
        this.projectVersionNotes = projectVersionNotes;
        this.cloneCategories = cloneCategories;
        this.projectLevelAdjustments = projectLevelAdjustments;
        this.forceProjectVersionUpdate = forceProjectVersionUpdate;
        this.cloneVersionName = cloneVersionName;
        this.projectVersionNickname = projectVersionNickname;
        this.applicationId = applicationId;
        this.tags = tags;
        this.groups = groups;
        this.parentProjectName = parentProjectName;
        this.parentProjectVersion = parentProjectVersion;
        this.cloneLatestProjectVersion = cloneLatestProjectVersion;
        this.customFields = customFields;
    }

    public String getProjectVersionPhase() {
        return projectVersionPhase;
    }

    public String getProjectVersionDistribution() {
        return projectVersionDistribution;
    }

    public Integer getProjectTier() {
        return projectTier;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public String getProjectVersionNotes() {
        return projectVersionNotes;
    }

    public String[] getCloneCategories() {
        return cloneCategories;
    }

    public Boolean isProjectLevelAdjustments() {
        return projectLevelAdjustments;
    }

    public Boolean isForceProjectVersionUpdate() {
        return forceProjectVersionUpdate;
    }

    public String getCloneVersionName() {
        return cloneVersionName;
    }

    public String getProjectVersionNickname() {
        return projectVersionNickname;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String[] getTags() {
        return tags;
    }

    public String[] getGroups() {
        return groups;
    }

    public String getParentProjectName() {
        return parentProjectName;
    }

    public String getParentProjectVersion() {
        return parentProjectVersion;
    }

    public Boolean getCloneLatestProjectVersion() {
        return cloneLatestProjectVersion;
    }

    public CustomFieldDocument getCustomFields() {
        return customFields;
    }
}
