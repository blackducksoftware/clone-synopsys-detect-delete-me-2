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
package com.synopsys.integration.detect.lifecycle.run.steps.blackduck;

import java.util.Optional;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostActions;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostOptions;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.result.BlackDuckBomDetectResult;
import com.synopsys.integration.detect.workflow.result.DetectResult;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckPostProcessingRunStep {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DetectConfigurationFactory detectConfigurationFactory;
    private final EventSystem eventSystem;
    private final DetectToolFilter detectToolFilter;

    public BlackDuckPostProcessingRunStep(DetectConfigurationFactory detectConfigurationFactory, EventSystem eventSystem, DetectToolFilter detectToolFilter) {
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.eventSystem = eventSystem;
        this.detectToolFilter = detectToolFilter;
    }

    public void run(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, CodeLocationResults codeLocationResults, BdioResult bdioResult, NameVersion projectNameVersion, ProjectVersionWrapper projectVersionWrapper)
        throws DetectUserFriendlyException {
        if (null != blackDuckServicesFactory) {
            logger.info("Will perform Black Duck post actions.");
            BlackDuckPostOptions blackDuckPostOptions = detectConfigurationFactory.createBlackDuckPostOptions();
            BlackDuckPostActions blackDuckPostActions = new BlackDuckPostActions(blackDuckServicesFactory, eventSystem);
            blackDuckPostActions.perform(blackDuckPostOptions, codeLocationResults.getCodeLocationWaitData(), projectVersionWrapper, projectNameVersion, detectConfigurationFactory.findTimeoutInSeconds());

            if ((!bdioResult.getUploadTargets().isEmpty() || detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN))) {
                Optional<String> componentsLink = Optional.ofNullable(projectVersionWrapper)
                                                      .map(ProjectVersionWrapper::getProjectVersionView)
                                                      .flatMap(projectVersionView -> projectVersionView.getFirstLinkSafely(ProjectVersionView.COMPONENTS_LINK))
                                                      .map(HttpUrl::string);

                if (componentsLink.isPresent()) {
                    DetectResult detectResult = new BlackDuckBomDetectResult(componentsLink.get());
                    eventSystem.publishEvent(Event.ResultProduced, detectResult);
                }
            }
            logger.info("Black Duck actions have finished.");
        } else {
            logger.debug("Will not perform Black Duck post actions: Detect is not online.");
        }
    }
}
