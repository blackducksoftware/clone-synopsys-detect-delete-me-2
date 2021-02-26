/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.pip.model;

import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;

public class NameVersionCodeLocation {
    private final String projectName;
    private final String projectVersion;
    private final CodeLocation codeLocation;

    public NameVersionCodeLocation(String projectName, String projectVersion, CodeLocation codeLocation) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.codeLocation = codeLocation;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public CodeLocation getCodeLocation() {
        return codeLocation;
    }
}
