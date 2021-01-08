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

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.util.NameVersion;

public class ImpactAnalysisRunStep {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DirectoryManager directoryManager;
    private final CodeLocationNameManager codeLocationNameManager;
    private final EventSystem eventSystem;
    private final DetectToolFilter detectToolFilter;

    public ImpactAnalysisRunStep(DirectoryManager directoryManager, CodeLocationNameManager codeLocationNameManager, EventSystem eventSystem, DetectToolFilter detectToolFilter) {
        this.directoryManager = directoryManager;
        this.codeLocationNameManager = codeLocationNameManager;
        this.eventSystem = eventSystem;
        this.detectToolFilter = detectToolFilter;
    }

    public void run(@Nullable BlackDuckServicesFactory blackDuckServicesFactory, ImpactAnalysisOptions impactAnalysisOptions, CodeLocationAccumulator codeLocationAccumulator, NameVersion projectNameVersion,
        ProjectVersionWrapper projectVersionWrapper) throws DetectUserFriendlyException {
        logger.info(ReportConstants.RUN_SEPARATOR);
        BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;
        if (null != blackDuckServicesFactory) {
            blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.ONLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, blackDuckServicesFactory, eventSystem);
        } else {
            blackDuckImpactAnalysisTool = BlackDuckImpactAnalysisTool.OFFLINE(directoryManager, codeLocationNameManager, impactAnalysisOptions, eventSystem);
        }
        if (detectToolFilter.shouldInclude(DetectTool.IMPACT_ANALYSIS) && blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Will include the Vulnerability Impact Analysis tool.");
            ImpactAnalysisToolResult impactAnalysisToolResult = blackDuckImpactAnalysisTool.performImpactAnalysisActions(projectNameVersion, projectVersionWrapper);

            /* TODO: There is currently no mechanism within Black Duck for checking the completion status of an Impact Analysis code location. Waiting should happen here when such a mechanism exists. See HUB-25142. JM - 08/2020 */
            codeLocationAccumulator.addNonWaitableCodeLocation(impactAnalysisToolResult.getCodeLocationNames());

            if (impactAnalysisToolResult.isSuccessful()) {
                logger.info("Vulnerability Impact Analysis successful.");
            } else {
                logger.warn("Something went wrong with the Vulnerability Impact Analysis tool.");
            }

            logger.info("Vulnerability Impact Analysis tool actions finished.");
        } else if (blackDuckImpactAnalysisTool.shouldRun()) {
            logger.info("Vulnerability Impact Analysis tool is enabled but will not run due to tool configuration.");
        } else {
            logger.info("Vulnerability Impact Analysis tool will not be run.");
        }
    }
}
