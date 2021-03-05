/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import java.util.List;

import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.project.DetectToolProjectInfo;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.util.NameVersion;

public class ProjectDecisionOperation {
    private static final String OPERATION_NAME = "BLACK_DUCK_PROJECT_DECISION";
    private final RunOptions runOptions;
    private final ProjectNameVersionDecider projectNameVersionDecider;

    public ProjectDecisionOperation(RunOptions runOptions, ProjectNameVersionDecider projectNameVersionDecider) {
        this.runOptions = runOptions;
        this.projectNameVersionDecider = projectNameVersionDecider;
    }

    public OperationResult<NameVersion> execute(List<DetectToolProjectInfo> detectToolProjectInfoList) throws OperationException {
        OperationResult<NameVersion> operationResult;
        try {
            operationResult = OperationResult.success(OPERATION_NAME, projectNameVersionDecider.decideProjectNameVersion(runOptions.getPreferredTools(), detectToolProjectInfoList));
        } catch (Exception ex) {
            operationResult = OperationResult.fail(OPERATION_NAME);
            throw new OperationException("Error deciding project name and version", ex, operationResult);
        }
        return operationResult;
    }
}
