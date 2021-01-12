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

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.steps.BazelToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.BlackDuckRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DetectorToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DockerToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.PolarisRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.StepFactory;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.exception.IntegrationException;

public class DefaultWorkflow implements Workflow {
    private StepFactory stepFactory;

    public DefaultWorkflow(StepFactory stepFactory) {
        this.stepFactory = stepFactory;
    }

    @Override
    public RunResult execute() throws DetectUserFriendlyException, IntegrationException {
        RunResult runResult = new RunResult();
        RunOptions runOptions = stepFactory.getRunContext().createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

        PolarisRunStep polarisStep = stepFactory.createPolarisStep(detectToolFilter);
        DockerToolRunStep dockerToolStep = stepFactory.createDockerToolStep(detectToolFilter);
        BazelToolRunStep bazelToolStep = stepFactory.createBazelToolStep(detectToolFilter);
        DetectorToolRunStep detectorToolStep = stepFactory.createDetectorToolStep(detectToolFilter);

        // define the order of the runnables. Polaris, projectTools i.e. detectors, BlackDuck
        boolean success;
        success = polarisStep.execute(runResult);
        success = success && dockerToolStep.execute(runResult);
        success = success && bazelToolStep.execute(runResult);
        success = success && detectorToolStep.execute(runResult);
        BlackDuckRunStep blackDuckStep = stepFactory.createBlackDuckStep(detectToolFilter, runOptions, success);
        blackDuckStep.execute(runResult);

        return runResult;
    }
}
