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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunContext;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.run.operation.DetectorOperation;
import com.synopsys.integration.detect.lifecycle.run.operation.DockerToolOperation;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationFactory;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationResult;
import com.synopsys.integration.detect.lifecycle.run.operation.PolarisOperation;
import com.synopsys.integration.detect.lifecycle.run.operation.input.FullScanPostProcessingInput;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.bdio.AggregateOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationResults;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class SourceWorkflow implements Workflow {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private <T> OperationResult<T> runTool(String displayName, boolean operationConditional, Supplier<OperationResult<T>> operation) {
        if (operationConditional) {
            logger.info("Will include the {} tool.", displayName);
            OperationResult<T> result = operation.get();
            logger.info("{} actions finished.", displayName);
            return result;
        } else {
            logger.info("{} tool will not be run.", displayName);
            return OperationResult.success();
        }
    }

    private void runDetectableTool(RunResult runResult, String displayName, boolean operationConditional, Supplier<OperationResult<DetectableToolResult>> operation) {
        OperationResult<DetectableToolResult> result = runTool(displayName, operationConditional, operation);
        if (result.hasContent() && result.getContent() != null) {
            runResult.addDetectableToolResult(result.getContent());
            if (result.requestedExitCode()) {
                eventSystem.publishExitCode(result.exitCode());
            }
        }
    }

    private void runDetectorTool(RunResult runResult, String displayName, boolean operationConditional, Supplier<OperationResult<DetectableToolResult>> operation) {
        OperationResult<DetectableToolResult> result = runTool(displayName, operationConditional, operation);
        if (result.hasContent() && result.getContent() != null) {
            runResult.addDetectableToolResult(result.getContent());
            if (result.requestedExitCode()) {
                eventSystem.publishExitCode(result.exitCode());
            }
        }
    }

    @Override
    public WorkflowResult execute(RunContext runContext, RunOptions runOptions, ProductRunData productRunData) throws DetectUserFriendlyException, IntegrationException {
        RunResult runResult = new RunResult();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();
        OperationFactory operationFactory = new OperationFactory(runContext);

        runTool("Polaris", PolarisOperation.shouldExecute(detectToolFilter, productRunData), () -> operationFactory.createPolarisOperation().execute());

        runDetectableTool(runResult, "Docker", DockerToolOperation.shouldExecute(detectToolFilter), () -> operationFactory.createDockerOperation().execute());
        runDetectableTool(runResult, "Bazel", DockerToolOperation.shouldExecute(detectToolFilter), () -> operationFactory.createDockerOperation().execute());

        runDetectorTool("Detector", DetectorOperation.shouldExecute(detectToolFilter, productRunData), () -> operationFactory.createDetectorOperation().execute());

        if (productRunData.shouldUseBlackDuckProduct() && productRunData.getBlackDuckRunData().isOnline()) {
            BlackDuckServicesFactory blackDuckServicesFactory = productRunData.getBlackDuckRunData().getBlackDuckServicesFactory();

            NameVersion projectNameVersion = operationFactory.createProjectDecisionOperation().execute(runResult.getDetectToolProjectInfo());
            ProjectVersionWrapper projectVersionWrapper = operationFactory.createProjectCreationOperation().execute(projectNameVersion);

            AggregateOptions aggregateOptions = operationFactory.createAggregateDecisionOperation().execute(runOptions, runResult.anyFailed());

            BdioResult bdioResult = operationFactory.createBdioFileGenerationOperation().execute(aggregateOptions, projectNameVersion, runResult.getDetectCodeLocations());
            CodeLocationAccumulator<UploadOutput, UploadBatchOutput> uploadResult = operationFactory.createBdioUploadOperation().execute(blackDuckServicesFactory, bdioResult);

            runTool("Signature  Scan", true, () -> operationFactory.createSignatureScanOperation().executeOperation());
            runTool("Binary  Scan", true, () -> operationFactory.createSignatureScanOperation().executeOperation());
            runTool("Impact Analysis  Scan", true, () -> operationFactory.createSignatureScanOperation().executeOperation());

            CodeLocationResults codeLocationResults = operationFactory.createCodeLocationResultOperation().executeOperation(uploadResult);

            FullScanPostProcessingInput postProcessingInput = new FullScanPostProcessingInput(projectNameVersion, bdioResult, codeLocationResults, projectVersionWrapper);
            operationFactory.createFullScanPostProcessingOperation().execute(blackDuckServicesFactory, postProcessingInput);
        }

        return WorkflowResult.success();
    }

}

