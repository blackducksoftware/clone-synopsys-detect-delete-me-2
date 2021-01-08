/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detect.lifecycle.run.steps;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.workflow.bdio.AggregateMode;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.developer.BlackDuckDeveloperMode;
import com.synopsys.integration.detect.workflow.blackduck.developer.BlackDuckDeveloperPostActions;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class DeveloperModeRunStep {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ProductRunData productRunData;
    private DetectConfigurationFactory detectConfigurationFactory;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private CodeLocationNameManager codeLocationNameManager;
    private BdioCodeLocationCreator bdioCodeLocationCreator;
    private DetectInfo detectInfo;

    public DeveloperModeRunStep(ProductRunData productRunData, DetectConfigurationFactory detectConfigurationFactory, DirectoryManager directoryManager,
        EventSystem eventSystem, CodeLocationNameManager codeLocationNameManager, BdioCodeLocationCreator bdioCodeLocationCreator, DetectInfo detectInfo) {
        this.productRunData = productRunData;
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.codeLocationNameManager = codeLocationNameManager;
        this.bdioCodeLocationCreator = bdioCodeLocationCreator;
        this.detectInfo = detectInfo;
    }

    public void run(RunResult runResult, RunOptions runOptions, NameVersion projectNameVersion, boolean priorStepsSucceeded) throws DetectUserFriendlyException, IntegrationException {
        if (!productRunData.shouldUseBlackDuckProduct()) {
            logger.info("Black Duck tools will not be run.");
        } else {
            logger.debug("Black Duck tools will run.");
            AggregateOptions aggregateOptions = determineAggregationStrategy(runOptions, !priorStepsSucceeded);
            BlackDuckRunData blackDuckRunData = productRunData.getBlackDuckRunData();

            blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

            BdioOptions bdioOptions = detectConfigurationFactory.createBdioOptions();
            BdioManager bdioManager = new BdioManager(detectInfo, new SimpleBdioFactory(), new ExternalIdFactory(), new Bdio2Factory(), new IntegrationEscapeUtil(), codeLocationNameManager, bdioCodeLocationCreator, directoryManager);
            BdioResult bdioResult = bdioManager.createBdioFiles(bdioOptions, aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations(), runOptions.shouldUseBdio2());
            eventSystem.publishEvent(Event.DetectCodeLocationNamesCalculated, bdioResult.getCodeLocationNamesResult());

            BlackDuckDeveloperMode developerMode = new BlackDuckDeveloperMode(blackDuckRunData, blackDuckServicesFactory, detectConfigurationFactory);
            List<DeveloperScanComponentResultView> results = developerMode.run(bdioResult);
            BlackDuckDeveloperPostActions postActions = new BlackDuckDeveloperPostActions(eventSystem);
            postActions.perform(results);
        }
    }

    private AggregateOptions determineAggregationStrategy(RunOptions runOptions, boolean anythingFailed) {
        String aggregateName = runOptions.getAggregateName().orElse(null);
        AggregateMode aggregateMode = runOptions.getAggregateMode();
        if (StringUtils.isNotBlank(aggregateName)) {
            if (anythingFailed) {
                return AggregateOptions.aggregateButSkipEmpty(aggregateName, aggregateMode);
            } else {
                return AggregateOptions.aggregateAndAlwaysUpload(aggregateName, aggregateMode);
            }
        } else {
            return AggregateOptions.doNotAggregate();
        }
    }
}
