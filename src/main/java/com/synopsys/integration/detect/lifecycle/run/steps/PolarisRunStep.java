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

import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.tool.detector.executable.DetectExecutableRunner;
import com.synopsys.integration.detect.tool.polaris.PolarisTool;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;

public class PolarisRunStep extends AbstractStep {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ProductRunData productRunData;
    private PropertyConfiguration detectConfiguration;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private DetectToolFilter detectToolFilter;

    public PolarisRunStep(ProductRunData productRunData, PropertyConfiguration detectConfiguration, DirectoryManager directoryManager, EventSystem eventSystem, DetectToolFilter detectToolFilter) {
        this.productRunData = productRunData;
        this.detectConfiguration = detectConfiguration;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.detectToolFilter = detectToolFilter;
    }

    @Override
    protected boolean shouldRun() {
        return productRunData.shouldUsePolarisProduct() && detectToolFilter.shouldInclude(DetectTool.POLARIS);
    }

    @Override
    public String getStepName() {
        return "Polaris";
    }

    @Override
    protected boolean run(RunResult runResult) throws DetectUserFriendlyException, IntegrationException {
        PolarisServerConfig polarisServerConfig = productRunData.getPolarisRunData().getPolarisServerConfig();
        DetectableExecutableRunner polarisExecutableRunner = DetectExecutableRunner.newInfo(eventSystem);
        PolarisTool polarisTool = new PolarisTool(eventSystem, directoryManager, polarisExecutableRunner, detectConfiguration, polarisServerConfig);
        polarisTool.runPolaris(new Slf4jIntLogger(logger), directoryManager.getSourceDirectory());
        return true;
    }
}
