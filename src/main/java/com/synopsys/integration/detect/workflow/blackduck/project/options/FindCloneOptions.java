/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.blackduck.project.options;

public class FindCloneOptions {
    private final String cloneVersionName;
    private final Boolean cloneLatestProjectVersion;

    public FindCloneOptions(String cloneVersionName, Boolean cloneLatestProjectVersion) {
        this.cloneVersionName = cloneVersionName;
        this.cloneLatestProjectVersion = cloneLatestProjectVersion;
    }

    public String getCloneVersionName() {
        return cloneVersionName;
    }

    public Boolean getCloneLatestProjectVersion() {
        return cloneLatestProjectVersion;
    }
}
