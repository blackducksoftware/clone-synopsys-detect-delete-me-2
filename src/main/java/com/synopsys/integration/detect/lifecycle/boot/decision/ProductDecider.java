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
package com.synopsys.integration.detect.lifecycle.boot.decision;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.detect.DetectTool;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;

public class ProductDecider {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PolarisServerConfigBuilder createPolarisServerConfigBuilder(final DetectConfiguration detectConfiguration, final File userHome) {
        final PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        final Set<String> allPolarisKeys = polarisServerConfigBuilder.getPropertyKeys();
        final Map<String, String> polarisProperties = detectConfiguration.getProperties(allPolarisKeys);
        polarisServerConfigBuilder.setLogger(new SilentIntLogger());
        polarisServerConfigBuilder.setProperties(polarisProperties.entrySet());
        polarisServerConfigBuilder.setUserHome(userHome.getAbsolutePath());
        polarisServerConfigBuilder.setTimeoutInSeconds(detectConfiguration.getIntegerProperty(DetectProperty.BLACKDUCK_TIMEOUT, PropertyAuthority.None));
        return polarisServerConfigBuilder;
    }

    public PolarisDecision determinePolaris(final DetectConfiguration detectConfiguration, final File userHome, final DetectToolFilter detectToolFilter) {
        if (!detectToolFilter.shouldInclude(DetectTool.POLARIS)) {
            logger.debug("Polaris will NOT run because it is excluded.");
            return PolarisDecision.skip();
        }
        final PolarisServerConfigBuilder polarisServerConfigBuilder = createPolarisServerConfigBuilder(detectConfiguration, userHome);
        final BuilderStatus builderStatus = polarisServerConfigBuilder.validateAndGetBuilderStatus();
        final boolean polarisCanRun = builderStatus.isValid();

        if (!polarisCanRun) {
            final String polarisUrl = detectConfiguration.getProperty(DetectProperty.POLARIS_URL, PropertyAuthority.None);
            if (StringUtils.isBlank(polarisUrl)) {
                logger.debug("Polaris will NOT run: The Polaris url must be provided.");
            } else {
                logger.debug("Polaris will NOT run: " + builderStatus.getFullErrorMessage());
            }
            return PolarisDecision.skip();
        } else {
            logger.debug("Polaris will run: An access token and url were found.");
            return PolarisDecision.runOnline(polarisServerConfigBuilder.build());
        }
    }

    public BlackDuckDecision determineBlackDuck(final DetectConfiguration detectConfiguration) {
        final boolean offline = detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_OFFLINE_MODE, PropertyAuthority.None);
        final String blackDuckUrl = detectConfiguration.getProperty(DetectProperty.BLACKDUCK_URL, PropertyAuthority.None);
        if (offline) {
            logger.debug("Black Duck will run: Black Duck offline mode was set to true.");
            return BlackDuckDecision.runOffline();
        } else if (StringUtils.isNotBlank(blackDuckUrl)) {
            logger.debug("Black Duck will run: A Black Duck url was found.");
            return BlackDuckDecision.runOnline();
        } else {
            logger.debug("Black Duck will NOT run: The Black Duck url must be provided or offline mode must be set to true.");
            return BlackDuckDecision.skip();
        }
    }

    public ProductDecision decide(final DetectConfiguration detectConfiguration, final File userHome, final DetectToolFilter detectToolFilter) throws DetectUserFriendlyException {
        return new ProductDecision(determineBlackDuck(detectConfiguration), determinePolaris(detectConfiguration, userHome, detectToolFilter));
    }

}
