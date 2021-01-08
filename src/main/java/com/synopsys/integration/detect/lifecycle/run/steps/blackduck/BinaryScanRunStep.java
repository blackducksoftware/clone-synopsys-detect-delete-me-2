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

import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanOptions;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanToolResult;
import com.synopsys.integration.detect.tool.binaryscanner.BlackDuckBinaryScannerTool;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detectable.detectable.file.WildcardFileFinder;
import com.synopsys.integration.util.NameVersion;

public class BinaryScanRunStep {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DetectConfigurationFactory detectConfigurationFactory;
    private final DirectoryManager directoryManager;
    private final EventSystem eventSystem;
    private final CodeLocationNameManager codeLocationNameManager;
    private final DetectToolFilter detectToolFilter;

    public BinaryScanRunStep(DetectConfigurationFactory detectConfigurationFactory, DirectoryManager directoryManager, EventSystem eventSystem,
        CodeLocationNameManager codeLocationNameManager, DetectToolFilter detectToolFilter) {
        this.detectConfigurationFactory = detectConfigurationFactory;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.codeLocationNameManager = codeLocationNameManager;
        this.detectToolFilter = detectToolFilter;
    }

    public void run(BlackDuckServicesFactory blackDuckServicesFactory, CodeLocationAccumulator codeLocationAccumulator, NameVersion projectNameVersion) throws DetectUserFriendlyException {
        logger.info(ReportConstants.RUN_SEPARATOR);
        if (detectToolFilter.shouldInclude(DetectTool.BINARY_SCAN)) {
            logger.info("Will include the binary scanner tool.");
            if (null != blackDuckServicesFactory) {
                BinaryScanOptions binaryScanOptions = detectConfigurationFactory.createBinaryScanOptions();
                BlackDuckBinaryScannerTool blackDuckBinaryScanner = new BlackDuckBinaryScannerTool(eventSystem, codeLocationNameManager, directoryManager, new WildcardFileFinder(), binaryScanOptions, blackDuckServicesFactory);
                if (blackDuckBinaryScanner.shouldRun()) {
                    BinaryScanToolResult result = blackDuckBinaryScanner.performBinaryScanActions(projectNameVersion);
                    if (result.isSuccessful()) {
                        codeLocationAccumulator.addWaitableCodeLocation(result.getCodeLocationCreationData());
                    }
                }
            }
            logger.info("Binary scanner actions finished.");
        } else {
            logger.info("Binary scan tool will not be run.");
        }
    }
}
