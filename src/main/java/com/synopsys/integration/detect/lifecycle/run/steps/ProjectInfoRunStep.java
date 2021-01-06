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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionOptions;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class ProjectInfoRunStep implements DetectRunStep {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private DetectConfigurationFactory detectConfigurationFactory;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;

    public ProjectInfoRunStep(DetectConfigurationFactory detectConfigurationFactory, DirectoryManager directoryManager, EventSystem eventSystem) {
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    @Override
    public DetectRunState run(DetectRunState previousState) throws DetectUserFriendlyException, IntegrationException {
        RunResult runResult = previousState.getCurrentRunResult();
        RunOptions runOptions = previousState.getRunOptions();

        logger.info(ReportConstants.RUN_SEPARATOR);
        logger.debug("Completed code location tools.");

        logger.debug("Determining project info.");

        ProjectNameVersionOptions projectNameVersionOptions = detectConfigurationFactory.createProjectNameVersionOptions(directoryManager.getSourceDirectory().getName());
        ProjectNameVersionDecider projectNameVersionDecider = new ProjectNameVersionDecider(projectNameVersionOptions);
        NameVersion projectNameVersion = projectNameVersionDecider.decideProjectNameVersion(runOptions.getPreferredTools(), runResult.getDetectToolProjectInfo());

        logger.info(String.format("Project name: %s", projectNameVersion.getName()));
        logger.info(String.format("Project version: %s", projectNameVersion.getVersion()));

        eventSystem.publishEvent(Event.ProjectNameVersionChosen, projectNameVersion);

        if (previousState.isFailure()) {
            return DetectRunState.fail(previousState.getCurrentRunResult(), previousState.getRunOptions(), projectNameVersion);
        } else {
            return DetectRunState.success(previousState.getCurrentRunResult(), previousState.getRunOptions(), projectNameVersion);
        }
    }
}
