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

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.developer.BlackDuckDeveloperMode;
import com.synopsys.integration.detect.workflow.blackduck.developer.BlackDuckDeveloperPostActions;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class DeveloperModeRunStep extends AbstractBlackDuckStep {
    public DeveloperModeRunStep(DetectInfo detectInfo, ProductRunData productRunData, DirectoryManager directoryManager, EventSystem eventSystem,
        DetectConfigurationFactory detectConfigurationFactory, CodeLocationNameManager codeLocationNameManager, BdioCodeLocationCreator bdioCodeLocationCreator, RunOptions runOptions,
        boolean priorStepsSucceeded) {
        super(detectInfo, productRunData, directoryManager, eventSystem, detectConfigurationFactory, codeLocationNameManager, bdioCodeLocationCreator, runOptions, priorStepsSucceeded);
    }

    @Override
    protected boolean run(RunResult runResult) throws DetectUserFriendlyException, IntegrationException {
        NameVersion projectNameVersion = getProjectInfo(runResult, getRunOptions());
        AggregateOptions aggregateOptions = determineAggregationStrategy(getRunOptions(), !havePriorStepsSucceeded());
        BlackDuckRunData blackDuckRunData = getProductRunData().getBlackDuckRunData();

        blackDuckRunData.getPhoneHomeManager().ifPresent(PhoneHomeManager::startPhoneHome);
        BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().orElse(null);

        BdioResult bdioResult = createBdioFiles(runResult, getRunOptions(), aggregateOptions, projectNameVersion);

        BlackDuckDeveloperMode developerMode = new BlackDuckDeveloperMode(blackDuckRunData, blackDuckServicesFactory, getDetectConfigurationFactory());
        List<DeveloperScanComponentResultView> results = developerMode.run(bdioResult);
        BlackDuckDeveloperPostActions postActions = new BlackDuckDeveloperPostActions(getEventSystem());
        postActions.perform(results);
        return true;
    }
}
