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
package com.synopsys.integration.detect.boot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.configuration.connection.BlackDuckConnectionDetails;
import com.synopsys.integration.detect.configuration.enumeration.BlackduckScanMode;
import com.synopsys.integration.detect.lifecycle.boot.decision.BlackDuckDecider;
import com.synopsys.integration.detect.lifecycle.boot.decision.BlackDuckDecision;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;

class BlackDuckDeciderTest {
    private final String VALID_URL = "http://example";

    @Test
    public void shouldRunBlackDuckOfflineWhenOverride() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunOfflineEvenWhenUrlProvided() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, "http://example.com");
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOfflineWhenInstallUrl() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOfflineWhenInstallPath() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldNotRunBlackDuckOfflineWhenUserProvidedHostUrl() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOnline() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(true, true));

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertFalse(productDecision.isOffline());
    }

    @Test
    public void shouldNotRunBlackduckRapidModeAndOffline() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.RAPID, createBdioOptions(false, true));

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckRapidModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.RAPID, createBdioOptions(false, true));

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckIntelligentModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, false));

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckRapidModeAndBDIO2Enabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.RAPID, createBdioOptions(true, true));

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckIntelligentModeAndBDIO2Enabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(true, false));

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckLegacyEnabledAndIntelligentModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.INTELLIGENT, createBdioOptions(false, true));

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckURLMissing() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, null);
        BlackDuckDecision productDecision = new BlackDuckDecider().decideBlackDuck(blackDuckConnectionDetails, BlackduckScanMode.RAPID, createBdioOptions(true, true));

        Assertions.assertFalse(productDecision.shouldRun());
    }

    private BdioOptions createBdioOptions(boolean useBdio2, boolean enableLegacyUpload) {
        return new BdioOptions(useBdio2, null, null, enableLegacyUpload);
    }

    private BlackDuckConnectionDetails blackDuckConnectionDetails(boolean offline, String blackduckUrl) {
        return new BlackDuckConnectionDetails(offline, blackduckUrl, null, null, null);
    }
}
