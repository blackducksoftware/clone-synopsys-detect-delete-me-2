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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class BDIOCreationRunStep {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DetectConfigurationFactory detectConfigurationFactory;
    private final DetectInfo detectInfo;
    private final AggregateOptions aggregateOptions;
    private final CodeLocationNameManager codeLocationNameManager;
    private final BdioCodeLocationCreator bdioCodeLocationCreator;
    private final DirectoryManager directoryManager;
    private final EventSystem eventSystem;

    public BDIOCreationRunStep(DetectConfigurationFactory detectConfigurationFactory, DetectInfo detectInfo, AggregateOptions aggregateOptions,
        CodeLocationNameManager codeLocationNameManager, BdioCodeLocationCreator bdioCodeLocationCreator, DirectoryManager directoryManager, EventSystem eventSystem) {
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.detectInfo = detectInfo;
        this.aggregateOptions = aggregateOptions;
        this.codeLocationNameManager = codeLocationNameManager;
        this.bdioCodeLocationCreator = bdioCodeLocationCreator;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
    }

    public BdioResult run(RunResult runResult, RunOptions runOptions, NameVersion projectNameVersion) throws DetectUserFriendlyException {
        BdioOptions bdioOptions = detectConfigurationFactory.createBdioOptions();
        BdioManager bdioManager = new BdioManager(detectInfo, new SimpleBdioFactory(), new ExternalIdFactory(), new Bdio2Factory(), new IntegrationEscapeUtil(), codeLocationNameManager, bdioCodeLocationCreator, directoryManager);
        BdioResult bdioResult = bdioManager.createBdioFiles(bdioOptions, aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations(), runOptions.shouldUseBdio2());
        eventSystem.publishEvent(Event.DetectCodeLocationNamesCalculated, bdioResult.getCodeLocationNamesResult());
        return bdioResult;
    }
}
