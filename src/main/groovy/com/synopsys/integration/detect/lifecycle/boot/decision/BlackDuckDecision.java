/**
 * synopsys-detect
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.detect.lifecycle.boot.decision;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;

public class BlackDuckDecision {
    private boolean shouldRun;
    private boolean successfullyConnected;
    private boolean isOffline;

    private BlackDuckServicesFactory blackDuckServicesFactory;
    private BlackDuckServerConfig blackDuckServerConfig;

    public static BlackDuckDecision forSkipBlackduck() {
        return new BlackDuckDecision(false, false, true, null, null);
    }

    public static BlackDuckDecision forOffline() {
        return new BlackDuckDecision(true, false, true, null, null);
    }

    public static BlackDuckDecision forOnlineConnected(final BlackDuckServicesFactory blackDuckServicesFactory,
        final BlackDuckServerConfig blackDuckServerConfig) {
        return new BlackDuckDecision(true, true, false, blackDuckServicesFactory, blackDuckServerConfig);
    }

    public static BlackDuckDecision forOnlineNotConnected() {
        return new BlackDuckDecision(true, false, false, null, null);
    }

    public BlackDuckDecision(final boolean shouldRun, final boolean successfullyConnected, final boolean isOffline, final BlackDuckServicesFactory blackDuckServicesFactory,
        final BlackDuckServerConfig blackDuckServerConfig) {
        this.shouldRun = shouldRun;
        this.successfullyConnected = successfullyConnected;
        this.isOffline = isOffline;
        this.blackDuckServicesFactory = blackDuckServicesFactory;
        this.blackDuckServerConfig = blackDuckServerConfig;
    }

    public BlackDuckServicesFactory getBlackDuckServicesFactory() {
        return blackDuckServicesFactory;
    }

    public BlackDuckServerConfig getBlackDuckServerConfig() {
        return blackDuckServerConfig;
    }

    public boolean isSuccessfullyConnected() {
        return successfullyConnected;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public boolean shouldRun() {
        return shouldRun;
    }
}
