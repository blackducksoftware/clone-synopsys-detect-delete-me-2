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
package com.synopsys.integration.detect.lifecycle.run.runnables;

import java.util.ArrayList;
import java.util.List;

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

public class UniversalProjectToolsRunnable implements DetectRunnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private DetectConfigurationFactory detectConfigurationFactory;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private DockerToolRunnable dockerToolRunnable;
    private BazelToolRunnable bazelToolRunnable;
    private DetectorToolRunnable detectorToolRunnable;
    private final List<DetectRunnable> toolRunnables;

    public UniversalProjectToolsRunnable(DetectConfigurationFactory detectConfigurationFactory, DirectoryManager directoryManager, EventSystem eventSystem,
        DockerToolRunnable dockerToolRunnable, BazelToolRunnable bazelToolRunnable, DetectorToolRunnable detectorToolRunnable) {
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.dockerToolRunnable = dockerToolRunnable;
        this.bazelToolRunnable = bazelToolRunnable;
        this.detectorToolRunnable = detectorToolRunnable;
        toolRunnables = new ArrayList<>();
        toolRunnables.add(dockerToolRunnable);
        toolRunnables.add(bazelToolRunnable);
        toolRunnables.add(detectorToolRunnable);
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    @Override
    public RunnableState run(RunnableState previousState) throws DetectUserFriendlyException, IntegrationException {
        RunOptions runOptions = previousState.getRunOptions();
        RunResult runResult = previousState.getCurrentRunResult();

        RunnableState runState = previousState;
        for (DetectRunnable toolRunnable : toolRunnables) {
            logger.info(ReportConstants.RUN_SEPARATOR);
            runState = toolRunnable.run(runState);
        }

        logger.info(ReportConstants.RUN_SEPARATOR);
        logger.debug("Completed code location tools.");

        logger.debug("Determining project info.");

        ProjectNameVersionOptions projectNameVersionOptions = detectConfigurationFactory.createProjectNameVersionOptions(directoryManager.getSourceDirectory().getName());
        ProjectNameVersionDecider projectNameVersionDecider = new ProjectNameVersionDecider(projectNameVersionOptions);
        NameVersion projectNameVersion = projectNameVersionDecider.decideProjectNameVersion(runOptions.getPreferredTools(), runResult.getDetectToolProjectInfo());

        logger.info(String.format("Project name: %s", projectNameVersion.getName()));
        logger.info(String.format("Project version: %s", projectNameVersion.getVersion()));

        eventSystem.publishEvent(Event.ProjectNameVersionChosen, projectNameVersion);

        if (runState.isFailure()) {
            return RunnableState.fail(runResult, runOptions, projectNameVersion);
        } else {
            return RunnableState.success(runResult, runOptions, projectNameVersion);
        }
    }
}
