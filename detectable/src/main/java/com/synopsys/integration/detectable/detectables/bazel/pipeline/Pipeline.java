/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.bazel.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.FinalStep;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStep;
import com.synopsys.integration.exception.IntegrationException;

public class Pipeline {
    private final List<IntermediateStep> intermediateSteps;
    private final FinalStep finalStep;

    public Pipeline(final List<IntermediateStep> intermediateSteps, final FinalStep finalStep) {
        this.intermediateSteps = intermediateSteps;
        this.finalStep = finalStep;
    }

    public MutableDependencyGraph run() throws IntegrationException {
        // Execute pipeline steps (like linux cmd piping with '|'); each step processes the output of the previous step
        List<String> pipelineData = new ArrayList<>();
        for (final IntermediateStep pipelineStep : intermediateSteps) {
            pipelineData = pipelineStep.process(pipelineData);
        }
        return finalStep.finish(pipelineData);
    }
}
