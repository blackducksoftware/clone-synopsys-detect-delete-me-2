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
package com.synopsys.integration.detect.lifecycle.run.workflow;

import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.lifecycle.run.RunContext;
import com.synopsys.integration.detect.lifecycle.run.steps.BazelToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.BlackDuckRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DetectorToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DockerToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.PolarisRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.ProjectInfoRunStep;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;

public class WorkflowStepFactory {
    private final RunContext runContext;

    public WorkflowStepFactory(RunContext runContext) {
        this.runContext = runContext;
    }

    public RunContext getRunContext() {
        return runContext;
    }

    public final PolarisRunStep createPolarisRunnable(DetectToolFilter detectToolFilter) {
        return new PolarisRunStep(runContext.getProductRunData(), runContext.getDetectConfiguration(), runContext.getDirectoryManager(), runContext.getEventSystem(), detectToolFilter);
    }

    public final DockerToolRunStep createDockerToolRunnable(DetectToolFilter detectToolFilter) {
        return new DockerToolRunStep(runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(), detectToolFilter, runContext.getExtractionEnvironmentProvider(),
            runContext.getCodeLocationConverter());
    }

    public final BazelToolRunStep createBazelToolRunnable(DetectToolFilter detectToolFilter) {
        return new BazelToolRunStep(runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(), detectToolFilter, runContext.getExtractionEnvironmentProvider(),
            runContext.getCodeLocationConverter());
    }

    public final DetectorToolRunStep createDetectorToolRunnable(DetectToolFilter detectToolFilter) {
        return new DetectorToolRunStep(runContext.getDetectConfiguration(), runContext.getDetectConfigurationFactory(), runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(),
            detectToolFilter,
            runContext.getExtractionEnvironmentProvider(), runContext.getCodeLocationConverter());
    }

    public final ProjectInfoRunStep createProjectInfoRunnable() {
        return new ProjectInfoRunStep(runContext.getDetectConfigurationFactory(), runContext.getDirectoryManager(), runContext.getEventSystem());
    }

    public final BlackDuckRunStep createBlackDuckRunnable(DetectToolFilter detectToolFilter) {
        DetectConfigurationFactory detectConfigurationFactory = runContext.getDetectConfigurationFactory();
        ImpactAnalysisOptions impactAnalysisOptions = detectConfigurationFactory.createImpactAnalysisOptions();
        return new BlackDuckRunStep(runContext.getDetectContext(), runContext.getProductRunData(), detectConfigurationFactory, runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getCodeLocationNameManager(),
            runContext.getBdioCodeLocationCreator(), runContext.getDetectInfo(), detectToolFilter, impactAnalysisOptions);
    }
}
