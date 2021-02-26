/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectable.result;

public class GoPkgLockfileNotFoundDetectableResult extends FailedDetectableResult {
    private final String directoryPath;

    public GoPkgLockfileNotFoundDetectableResult(final String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public String toDescription() {
        return String.format("A Gopkg.toml was located in %s, but the Gopkg.lock file was NOT located. Please run 'go dep init' and 'go dep ensure' in that location and try again.", directoryPath);
    }
}