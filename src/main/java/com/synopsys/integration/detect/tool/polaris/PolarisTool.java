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
package com.synopsys.integration.detect.tool.polaris;

import com.synopsys.integration.detect.configuration.ConnectionManager;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.detectable.executable.Executable;
import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.cli.PolarisDownloadUtility;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class PolarisTool {
    private final DirectoryManager directoryManager;
    private final ExecutableRunner executableRunner;
    private final ConnectionManager connectionManager;
    private final EventSystem eventSystem;
    private DetectConfiguration detectConfiguration;
    private final PolarisServerConfig polarisServerConfig;

    public PolarisTool(EventSystem eventSystem, final DirectoryManager directoryManager, final ExecutableRunner executableRunner, ConnectionManager connectionManager,
        final DetectConfiguration detectConfiguration, PolarisServerConfig polarisServerConfig) {
        this.directoryManager = directoryManager;
        this.executableRunner = executableRunner;
        this.connectionManager = connectionManager;
        this.eventSystem = eventSystem;
        this.detectConfiguration = detectConfiguration;
        this.polarisServerConfig = polarisServerConfig;
    }

    public void runPolaris(final IntLogger logger, File projectDirectory) throws DetectUserFriendlyException {
        logger.info("Polaris determined it should attempt to run.");
        String polarisUrl = detectConfiguration.getProperty(DetectProperty.POLARIS_URL, PropertyAuthority.None);
        logger.info("Will use the following polaris url: " + polarisUrl);

        AccessTokenPolarisHttpClient polarisHttpClient = polarisServerConfig.createPolarisHttpClient(logger);
        File toolsDirectory = directoryManager.getPermanentDirectory();

        PolarisDownloadUtility polarisDownloadUtility = PolarisDownloadUtility.fromPolaris(logger, polarisHttpClient, toolsDirectory);
        Optional<String> polarisCliPath = polarisDownloadUtility.retrievePolarisCliExecutablePath();

        //TODO this should be revised to use PolarisCliExecutable and PolarisCliRunner
        if (polarisCliPath.isPresent()) {
            Map<String, String> environmentVariables = new HashMap<>();
            environmentVariables.put("COVERITY_UNSUPPORTED", "1");
            environmentVariables.put("POLARIS_USER_INPUT_TIMEOUT_MINUTES", "1");
            polarisServerConfig.populateEnvironmentVariables(environmentVariables::put);

            logger.info("Found polaris cli: " + polarisCliPath.get());
            List<String> arguments = new ArrayList<>();
            arguments.add("analyze");

            String additionalArgs = detectConfiguration.getProperty(DetectProperty.POLARIS_ARGUMENTS, PropertyAuthority.None);
            if (StringUtils.isNotBlank(additionalArgs)) {
                arguments.addAll(Arrays.asList(additionalArgs.split(" ")));
            }

            Executable swipExecutable = new Executable(projectDirectory, environmentVariables, polarisCliPath.get(), arguments);
            try {
                ExecutableOutput output = executableRunner.execute(swipExecutable);
                if (output.getReturnCode() == 0) {
                    eventSystem.publishEvent(Event.StatusSummary, new Status("POLARIS", StatusType.SUCCESS));
                } else {
                    logger.error("Polaris returned a non-zero exit code.");
                    eventSystem.publishEvent(Event.StatusSummary, new Status("POLARIS", StatusType.FAILURE));
                }

            } catch (ExecutableRunnerException e) {
                eventSystem.publishEvent(Event.StatusSummary, new Status("POLARIS", StatusType.FAILURE));
                logger.error("Couldn't run the executable: " + e.getMessage());
            }
        } else {
            logger.error("Check the logs - the Polaris CLI could not be found.");
        }
    }

}