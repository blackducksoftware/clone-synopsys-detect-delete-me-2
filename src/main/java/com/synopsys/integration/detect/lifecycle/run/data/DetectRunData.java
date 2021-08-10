/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.data;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.detect.util.filter.DetectToolFilter;

public class DetectRunData {
    private final BlackDuckRunData blackDuckRunData;
    private final DetectToolFilter detectToolFilter;

    public DetectRunData(@NotNull final BlackDuckRunData blackDuckRunData, @NotNull final DetectToolFilter detectToolFilter) {
        this.blackDuckRunData = blackDuckRunData;
        this.detectToolFilter = detectToolFilter;
    }

    @NotNull
    public BlackDuckRunData getBlackDuckRunData() {
        return blackDuckRunData;
    }

    @NotNull
    public DetectToolFilter getDetectToolFilter() {
        return detectToolFilter;
    }
}
