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
package com.blackducksoftware.integration.hub.detect.bomtool.npm;

import java.io.File;

import com.blackducksoftware.integration.hub.detect.bomtool.BomToolApplicableResult;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;

public class NpmApplicableResult extends BomToolApplicableResult {
    private String npmExePath;
    private final File packageLockJson;
    private final File shrinkwrapJson;

    public NpmApplicableResult(final File directory, final String npmExePath, final File packageLockJson, final File shrinkwrapJson) {
        super(directory, BomToolType.NPM);
        this.npmExePath = npmExePath;
        this.packageLockJson = packageLockJson;
        this.shrinkwrapJson = shrinkwrapJson;
    }

    public void setNpmExePath(final String exe) {
        npmExePath = exe;
    }

    public String getNpmExePath() {
        return npmExePath;
    }

    public File getPackageLockJson() {
        return packageLockJson;
    }

    public File getShrinkwrapJson() {
        return shrinkwrapJson;
    }

}
