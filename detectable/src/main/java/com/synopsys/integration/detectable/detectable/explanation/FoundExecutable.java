/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectable.explanation;

import java.io.File;

import com.synopsys.integration.detectable.ExecutableTarget;

public class FoundExecutable extends Explanation {
    private final String executableString;

    public FoundExecutable(File file) {
        this.executableString = file.toString();
    }

    public FoundExecutable(ExecutableTarget executableTarget) {
        this.executableString = executableTarget.toCommand();
    }

    public FoundExecutable(String name) {
        this.executableString = name;
    }

    @Override
    public String describeSelf() {
        return "Found executable: " + executableString;
    }
}
