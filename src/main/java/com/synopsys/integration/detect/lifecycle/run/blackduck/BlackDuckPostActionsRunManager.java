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
package com.synopsys.integration.detect.lifecycle.run.blackduck;

import java.util.Optional;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
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

public class BlackDuckPostActionsRunManager {
    private final BlackDuckPostActions blackDuckPostActions;
    private final BlackDuckPostOptions blackDuckPostOptions;
    private final Long timeoutInSeconds;
    private final EventSystem eventSystem;
    private final DetectToolFilter detectToolFilter;

    public BlackDuckPostActionsRunManager(BlackDuckPostActions blackDuckPostActions, BlackDuckPostOptions blackDuckPostOptions, Long timeoutInSeconds,
        EventSystem eventSystem, DetectToolFilter detectToolFilter) {
        this.blackDuckPostActions = blackDuckPostActions;
        this.blackDuckPostOptions = blackDuckPostOptions;
        this.timeoutInSeconds = timeoutInSeconds;
        this.eventSystem = eventSystem;
        this.detectToolFilter = detectToolFilter;
    }

    public void performBlackDuckPostActions(NameVersion projectNameVersion, ProjectVersionWrapper projectVersionWrapper, BdioResult bdioResult, CodeLocationResults codeLocationResults) throws DetectUserFriendlyException {
        blackDuckPostActions.perform(blackDuckPostOptions, codeLocationResults.getCodeLocationWaitData(), projectVersionWrapper, projectNameVersion, timeoutInSeconds);

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
    }
}
