/**
 * synopsys-detect
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
package com.synopsys.integration.detect.lifecycle.run;

import com.synopsys.integration.detect.util.filter.DetectToolFilter;

public class RunOptions {
    private final boolean unmapCodeLocations;
    private final String aggregateName;
    private final boolean aggregateDirect;
    private final String preferredTools;
    private final DetectToolFilter detectToolFilter;
    private final boolean useBdio2;

    public RunOptions(final boolean unmapCodeLocations, final String aggregateName, final boolean aggregateDirect, final String preferredTools, final DetectToolFilter detectToolFilter, final boolean useBdio2) {
        this.unmapCodeLocations = unmapCodeLocations;
        this.aggregateName = aggregateName;
        this.aggregateDirect = aggregateDirect;
        this.preferredTools = preferredTools;
        this.detectToolFilter = detectToolFilter;
        this.useBdio2 = useBdio2;
    }

    public boolean shouldUnmapCodeLocations() {
        return unmapCodeLocations;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public boolean shouldAggregateDirect() {
        return aggregateDirect;
    }

    public String getPreferredTools() {
        return preferredTools;
    }

    public DetectToolFilter getDetectToolFilter() {
        return detectToolFilter;
    }

    public boolean shouldUseBdio2() {
        return useBdio2;
    }
}
