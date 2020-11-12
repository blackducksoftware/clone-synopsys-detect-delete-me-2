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

import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.util.NameVersion;

public class ImpactAnalysisToolManager implements ToolRunManager {
    private final BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;
    private final DetectToolFilter detectToolFilter;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ImpactAnalysisToolManager(BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool, DetectToolFilter detectToolFilter) {
        this.blackDuckImpactAnalysisTool = blackDuckImpactAnalysisTool;
        this.detectToolFilter = detectToolFilter;
    }

    public void runImpactAnalysis(NameVersion projectNameVersion, ProjectVersionWrapper projectVersionWrapper, CodeLocationAccumulator codeLocationAccumulator) throws DetectUserFriendlyException {
        ImpactAnalysisToolResult impactAnalysisToolResult = blackDuckImpactAnalysisTool.performImpactAnalysisActions(projectNameVersion, projectVersionWrapper);

        /* TODO: There is currently no mechanism within Black Duck for checking the completion status of an Impact Analysis code location. Waiting should happen here when such a mechanism exists. See HUB-25142. JM - 08/2020 */
        codeLocationAccumulator.addNonWaitableCodeLocation(impactAnalysisToolResult.getCodeLocationNames());

        if (impactAnalysisToolResult.isSuccessful()) {
            logger.info("Vulnerability Impact Analysis successful.");
        } else {
            logger.warn("Something went wrong with the Vulnerability Impact Analysis tool.");
        }
    }

    @Override
    public boolean shouldRun() {
        return blackDuckImpactAnalysisTool.shouldRun();
    }

    @Override
    public boolean isIncluded() {
        return detectToolFilter.shouldInclude(DetectTool.IMPACT_ANALYSIS);
    }

}
