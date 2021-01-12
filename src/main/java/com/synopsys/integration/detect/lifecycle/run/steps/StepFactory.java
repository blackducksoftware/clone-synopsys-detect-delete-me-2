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

import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.lifecycle.run.RunContext;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;

public class StepFactory {
    private final RunContext runContext;

    public StepFactory(RunContext runContext) {
        this.runContext = runContext;
    }

    public RunContext getRunContext() {
        return runContext;
    }

    public final PolarisStep createPolarisStep(DetectToolFilter detectToolFilter) {
        return new PolarisStep(runContext.getProductRunData(), runContext.getDetectConfiguration(), runContext.getDirectoryManager(), runContext.getEventSystem(), detectToolFilter);
    }

    public final DockerToolStep createDockerToolStep(DetectToolFilter detectToolFilter) {
        return new DockerToolStep(runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(), detectToolFilter, runContext.getExtractionEnvironmentProvider(),
            runContext.getCodeLocationConverter());
    }

    public final BazelToolStep createBazelToolStep(DetectToolFilter detectToolFilter) {
        return new BazelToolStep(runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(), detectToolFilter, runContext.getExtractionEnvironmentProvider(),
            runContext.getCodeLocationConverter());
    }

    public final DetectorToolStep createDetectorToolStep(DetectToolFilter detectToolFilter) {
        return new DetectorToolStep(runContext.getDetectConfiguration(), runContext.getDetectConfigurationFactory(), runContext.getDirectoryManager(), runContext.getEventSystem(), runContext.getDetectDetectableFactory(),
            detectToolFilter,
            runContext.getExtractionEnvironmentProvider(), runContext.getCodeLocationConverter());
    }

    public final BlackDuckStep createBlackDuckStep(DetectToolFilter detectToolFilter, RunOptions runOptions, boolean priorStepsSucceeded) {
        DetectConfigurationFactory detectConfigurationFactory = runContext.getDetectConfigurationFactory();
        ImpactAnalysisOptions impactAnalysisOptions = detectConfigurationFactory.createImpactAnalysisOptions();
        return new BlackDuckStep(runContext.getDetectInfo(), runContext.getProductRunData(), runContext.getDirectoryManager(), runContext.getEventSystem(), detectConfigurationFactory, runContext.getCodeLocationNameManager(),
            runContext.getBdioCodeLocationCreator(), runOptions, priorStepsSucceeded, runContext.getDetectContext(), detectToolFilter, impactAnalysisOptions);
    }

    public final DeveloperModeStep createDeveloperModeStep(RunOptions runOptions, boolean priorStepsSucceeded) {
        DetectConfigurationFactory detectConfigurationFactory = runContext.getDetectConfigurationFactory();
        return new DeveloperModeStep(runContext.getDetectInfo(), runContext.getProductRunData(), runContext.getDirectoryManager(), runContext.getEventSystem(), detectConfigurationFactory, runContext.getCodeLocationNameManager(),
            runContext.getBdioCodeLocationCreator(), runOptions, priorStepsSucceeded);
    }
}
