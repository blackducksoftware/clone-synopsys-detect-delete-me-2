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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.CodeLocationService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.blackduck.DetectCodeLocationUnmapService;
import com.synopsys.integration.detect.workflow.blackduck.DetectCustomFieldService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectServiceOptions;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class ProjectActionsRunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectProjectService detectProjectService;
    private final DetectCodeLocationUnmapService detectCodeLocationUnmapService;

    public static ProjectActionsRunManager createFromRunData(BlackDuckRunData blackDuckRunData, DetectProjectServiceOptions detectProjectServiceOptions) {
        if (blackDuckRunData.isOnline() && blackDuckRunData.getBlackDuckServicesFactory().isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory().get();

            ProjectMappingService projectMappingService = blackDuckServicesFactory.createProjectMappingService();
            DetectCustomFieldService detectCustomFieldService = new DetectCustomFieldService();
            DetectProjectService detectProjectService = new DetectProjectService(blackDuckServicesFactory, detectProjectServiceOptions, projectMappingService, detectCustomFieldService);

            BlackDuckApiClient blackDuckApiClient = blackDuckServicesFactory.getBlackDuckService();
            CodeLocationService codeLocationService = blackDuckServicesFactory.createCodeLocationService();
            DetectCodeLocationUnmapService detectCodeLocationUnmapService = new DetectCodeLocationUnmapService(blackDuckApiClient, codeLocationService);

            return new ProjectActionsRunManager(detectProjectService, detectCodeLocationUnmapService);
        }

        return new ProjectActionsRunManager(null, null);
    }

    public ProjectActionsRunManager(DetectProjectService detectProjectService, DetectCodeLocationUnmapService detectCodeLocationUnmapService) {
        this.detectProjectService = detectProjectService;
        this.detectCodeLocationUnmapService = detectCodeLocationUnmapService;
    }

    public ProjectVersionWrapper projectAndVersionActions(NameVersion projectNameVersion, boolean shouldUnmapCodeLocations) throws DetectUserFriendlyException, IntegrationException {
        logger.debug("Getting or creating project.");
        ProjectVersionWrapper projectVersionWrapper = detectProjectService.createOrUpdateBlackDuckProject(projectNameVersion);

        if (null != projectVersionWrapper && shouldUnmapCodeLocations) {
            logger.debug("Unmapping code locations.");
            detectCodeLocationUnmapService.unmapCodeLocations(projectVersionWrapper.getProjectVersionView());
        } else {
            logger.debug("Will not unmap code locations: Project view was not present, or should not unmap code locations.");
        }

        return projectVersionWrapper;
    }

}
