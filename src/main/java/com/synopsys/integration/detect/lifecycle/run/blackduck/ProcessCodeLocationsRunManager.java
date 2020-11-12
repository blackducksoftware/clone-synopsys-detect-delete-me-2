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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.DetectBdioUploadService;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.codelocation.DetectCodeLocation;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class ProcessCodeLocationsRunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AggregateOptions aggregateOptions;
    private final BdioOptions bdioOptions;
    private final BdioManager bdioManager;
    private final EventSystem eventSystem;
    private final BlackDuckServicesFactory blackDuckServicesFactory;

    public ProcessCodeLocationsRunManager(AggregateOptions aggregateOptions, BdioOptions bdioOptions, BdioManager bdioManager, EventSystem eventSystem, BlackDuckServicesFactory blackDuckServicesFactory) {
        this.aggregateOptions = aggregateOptions;
        this.bdioOptions = bdioOptions;
        this.bdioManager = bdioManager;
        this.eventSystem = eventSystem;
        this.blackDuckServicesFactory = blackDuckServicesFactory;
    }

    public BdioResult processCodeLocations(NameVersion projectNameVersion, List<DetectCodeLocation> detectCodeLocations, boolean shouldUseBdio2, CodeLocationAccumulator codeLocationAccumulator)
        throws DetectUserFriendlyException, IntegrationException {
        BdioResult bdioResult = bdioManager.createBdioFiles(bdioOptions, aggregateOptions, projectNameVersion, detectCodeLocations, shouldUseBdio2);
        eventSystem.publishEvent(Event.DetectCodeLocationNamesCalculated, bdioResult.getCodeLocationNamesResult());

        if (!bdioResult.getUploadTargets().isEmpty()) {
            logger.info(String.format("Created %d BDIO files.", bdioResult.getUploadTargets().size()));
            if (null != blackDuckServicesFactory) {
                logger.debug("Uploading BDIO files.");
                DetectBdioUploadService detectBdioUploadService = new DetectBdioUploadService();
                CodeLocationCreationData<UploadBatchOutput> uploadBatchOutputCodeLocationCreationData = detectBdioUploadService.uploadBdioFiles(bdioResult, blackDuckServicesFactory);
                codeLocationAccumulator.addWaitableCodeLocation(uploadBatchOutputCodeLocationCreationData);
            }
        } else {
            logger.debug("Did not create any BDIO files.");
        }

        return bdioResult;
    }
}
