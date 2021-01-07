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
import com.synopsys.integration.detect.lifecycle.run.steps.ProjectInfoRunStep;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class DefaultWorkflow implements Workflow {
    private WorkflowStepFactory workflowStepFactory;

    public DefaultWorkflow(WorkflowStepFactory workflowStepFactory) {
        this.workflowStepFactory = workflowStepFactory;
    }

    @Override
    public RunResult execute() throws DetectUserFriendlyException, IntegrationException {
        RunResult runResult = new RunResult();
        RunOptions runOptions = workflowStepFactory.getRunContext().createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

        PolarisRunStep polarisRunnable = workflowStepFactory.createPolarisRunnable(detectToolFilter);
        DockerToolRunStep dockerToolRunnable = workflowStepFactory.createDockerToolRunnable(detectToolFilter);
        BazelToolRunStep bazelToolRunnable = workflowStepFactory.createBazelToolRunnable(detectToolFilter);
        DetectorToolRunStep detectorToolRunnable = workflowStepFactory.createDetectorToolRunnable(detectToolFilter);
        BlackDuckRunStep blackDuckRunnable = workflowStepFactory.createBlackDuckRunnable(detectToolFilter);
        ProjectInfoRunStep projectInfoRunStep = workflowStepFactory.createProjectInfoRunnable();

        // define the order of the runnables. Polaris, projectTools i.e. detectors, BlackDuck
        boolean success = true;
        polarisRunnable.run();
        success = success && dockerToolRunnable.run(runResult);
        success = success && bazelToolRunnable.run(runResult);
        success = success && detectorToolRunnable.run(runResult);
        // this will set the projectNameVersion in the RunnableState object for BlackDuckRunnable to use.  It must execute before BlackDuck.
        NameVersion projectNameVersion = projectInfoRunStep.run(runResult, runOptions);
        blackDuckRunnable.run(runResult, runOptions, projectNameVersion, success);

        return runResult;
    }
}
