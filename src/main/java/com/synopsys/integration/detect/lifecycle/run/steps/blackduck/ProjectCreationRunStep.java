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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.blackduck.DetectCodeLocationUnmapService;
import com.synopsys.integration.detect.workflow.blackduck.DetectCustomFieldService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectServiceOptions;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class ProjectCreationRunStep {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DetectConfigurationFactory detectConfigurationFactory;

    public ProjectCreationRunStep(DetectConfigurationFactory detectConfigurationFactory) {
        this.detectConfigurationFactory = detectConfigurationFactory;
    }

    public Optional<ProjectVersionWrapper> run(RunOptions runOptions, BlackDuckRunData blackDuckRunData, BlackDuckServicesFactory blackDuckServicesFactory, NameVersion projectNameVersion)
        throws DetectUserFriendlyException, IntegrationException {
        ProjectVersionWrapper projectVersionWrapper = null;
        if (!runOptions.shouldPerformDeveloperModeScan() && blackDuckRunData.isOnline() && blackDuckServicesFactory != null) {
            logger.debug("Getting or creating project.");
            DetectProjectServiceOptions options = detectConfigurationFactory.createDetectProjectServiceOptions();
            ProjectMappingService detectProjectMappingService = blackDuckServicesFactory.createProjectMappingService();
            DetectCustomFieldService detectCustomFieldService = new DetectCustomFieldService();
            DetectProjectService detectProjectService = new DetectProjectService(blackDuckServicesFactory, options, detectProjectMappingService, detectCustomFieldService);
            projectVersionWrapper = detectProjectService.createOrUpdateBlackDuckProject(projectNameVersion);

            if (null != projectVersionWrapper && runOptions.shouldUnmapCodeLocations()) {
                logger.debug("Unmapping code locations.");
                DetectCodeLocationUnmapService detectCodeLocationUnmapService = new DetectCodeLocationUnmapService(blackDuckServicesFactory.getBlackDuckApiClient(), blackDuckServicesFactory.createCodeLocationService());
                detectCodeLocationUnmapService.unmapCodeLocations(projectVersionWrapper.getProjectVersionView());
            } else {
                logger.debug("Will not unmap code locations: Project view was not present, or should not unmap code locations.");
            }
        } else {
            logger.debug("Detect is not online, and will not create the project.");
        }
        logger.debug("Completed project and version actions.");
        return Optional.ofNullable(projectVersionWrapper);
    }
}
