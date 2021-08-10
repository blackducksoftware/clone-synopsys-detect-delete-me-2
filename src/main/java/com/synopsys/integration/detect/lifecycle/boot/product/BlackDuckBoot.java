/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.boot.product;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.phonehome.BlackDuckPhoneHomeHelper;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.connection.BlackDuckConfigFactory;
import com.synopsys.integration.detect.configuration.connection.BlackDuckConnectionDetails;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.boot.decision.BlackDuckDecision;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.blackduck.analytics.AnalyticsConfigurationService;
import com.synopsys.integration.detect.workflow.blackduck.analytics.AnalyticsSetting;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.phonehome.OnlinePhoneHomeManager;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.SilentIntLogger;

public class BlackDuckBoot {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BlackDuckConnectivityChecker blackDuckConnectivityChecker;
    private final AnalyticsConfigurationService analyticsConfigurationService;
    private final DetectInfo detectInfo;
    private final EventSystem eventSystem;
    private final DetectConfigurationFactory detectConfigurationFactory;

    public BlackDuckBoot(BlackDuckConnectivityChecker blackDuckConnectivityChecker, AnalyticsConfigurationService analyticsConfigurationService,
        DetectInfo detectInfo, EventSystem eventSystem, DetectConfigurationFactory detectConfigurationFactory) {
        this.blackDuckConnectivityChecker = blackDuckConnectivityChecker;
        this.analyticsConfigurationService = analyticsConfigurationService;
        this.detectInfo = detectInfo;
        this.eventSystem = eventSystem;
        this.detectConfigurationFactory = detectConfigurationFactory;
    }

    public BlackDuckRunData boot(BlackDuckDecision blackDuckDecision, BlackDuckBootOptions bootOptions) throws DetectUserFriendlyException {
        if (!blackDuckDecision.shouldRun()) {
            throw new DetectUserFriendlyException(
                "Your environment was not sufficiently configured to run Black Duck or Polaris. Please configure your environment for at least one product.  See online help at: https://detect.synopsys.com/doc/",
                ExitCodeType.FAILURE_CONFIGURATION);

        }

        logger.debug("Detect Black Duck boot start.");

        if (blackDuckDecision.isOffline()) {
            return BlackDuckRunData.offline();
        }

        BlackDuckRunData blackDuckRunData = bootOnline(blackDuckDecision, blackDuckConnectivityChecker, bootOptions, analyticsConfigurationService);

        if (bootOptions.isTestConnections()) {
            logger.debug(String.format("%s is set to 'true' so Detect will not run.", DetectProperties.DETECT_TEST_CONNECTION.getProperty().getName()));
            return null;
        }

        logger.debug("Detect Black Duck boot completed.");
        return blackDuckRunData;
    }

    @Nullable
    private BlackDuckRunData bootOnline(BlackDuckDecision blackDuckDecision, BlackDuckConnectivityChecker blackDuckConnectivityChecker, BlackDuckBootOptions bootOptions,
        AnalyticsConfigurationService analyticsConfigurationService) throws DetectUserFriendlyException {

        logger.debug("Will boot Black Duck product online.");
        BlackDuckServerConfig blackDuckServerConfig = createBlackDuckServerConfig();
        BlackDuckConnectivityResult blackDuckConnectivityResult = blackDuckConnectivityChecker.determineConnectivity(blackDuckServerConfig);

        if (blackDuckConnectivityResult.isSuccessfullyConnected()) {
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckConnectivityResult.getBlackDuckServicesFactory();

            if (shouldUsePhoneHome(analyticsConfigurationService, blackDuckServicesFactory.getApiDiscovery(), blackDuckServicesFactory.getBlackDuckApiClient())) {
                PhoneHomeManager phoneHomeManager = createPhoneHomeManager(blackDuckServicesFactory);
                return BlackDuckRunData.online(blackDuckDecision.scanMode(), blackDuckServicesFactory, phoneHomeManager, blackDuckConnectivityResult.getBlackDuckServerConfig());
            } else {
                logger.debug("Skipping phone home due to Black Duck global settings.");
                return BlackDuckRunData.onlineNoPhoneHome(blackDuckDecision.scanMode(), blackDuckServicesFactory, blackDuckConnectivityResult.getBlackDuckServerConfig());
            }
        } else {
            if (bootOptions.isIgnoreConnectionFailures()) {
                logger.info(String.format("Failed to connect to Black Duck: %s", blackDuckConnectivityResult.getFailureReason()));
                logger.info(String.format("%s is set to 'true' so Detect will simply do nothing.", DetectProperties.DETECT_IGNORE_CONNECTION_FAILURES.getProperty().getName()));
                return null;
            } else {
                throw new DetectUserFriendlyException("Could not communicate with Black Duck: " + blackDuckConnectivityResult.getFailureReason(), ExitCodeType.FAILURE_BLACKDUCK_CONNECTIVITY);
            }
        }
    }

    public PhoneHomeManager createPhoneHomeManager(BlackDuckServicesFactory blackDuckServicesFactory) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        BlackDuckPhoneHomeHelper blackDuckPhoneHomeHelper = BlackDuckPhoneHomeHelper.createAsynchronousPhoneHomeHelper(blackDuckServicesFactory, executorService);
        return new OnlinePhoneHomeManager(detectConfigurationFactory.createPhoneHomeOptions().getPassthrough(), detectInfo, eventSystem, blackDuckPhoneHomeHelper);
    }

    public BlackDuckServerConfig createBlackDuckServerConfig() throws DetectUserFriendlyException {
        BlackDuckConnectionDetails connectionDetails = detectConfigurationFactory.createBlackDuckConnectionDetails();
        BlackDuckConfigFactory blackDuckConfigFactory = new BlackDuckConfigFactory(detectInfo, connectionDetails);
        return blackDuckConfigFactory.createServerConfig(new SilentIntLogger());
    }

    private boolean shouldUsePhoneHome(AnalyticsConfigurationService analyticsConfigurationService, ApiDiscovery apiDiscovery, BlackDuckApiClient blackDuckService) {
        try {
            AnalyticsSetting analyticsSetting = analyticsConfigurationService.fetchAnalyticsSetting(apiDiscovery, blackDuckService);
            return analyticsSetting.isEnabled();
        } catch (IntegrationException | IOException e) {
            logger.trace("Failed to check analytics setting on Black Duck. Likely this Black Duck instance does not support it.", e);
            return true; // Skip phone home will be applied at the library level.
        }
    }
}
