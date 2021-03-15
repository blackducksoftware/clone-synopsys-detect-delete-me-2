/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import java.util.List;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.workflow.project.DetectToolProjectInfo;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.detect.workflow.status.Operation;
import com.synopsys.integration.detect.workflow.status.StatusEventPublisher;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class ProjectDecisionOperation {
    private static final String OPERATION_NAME = "Detect Project Decision";
    private final RunOptions runOptions;
    private final ProjectNameVersionDecider projectNameVersionDecider;
    private final StatusEventPublisher statusEventPublisher;

    public ProjectDecisionOperation(RunOptions runOptions, ProjectNameVersionDecider projectNameVersionDecider, StatusEventPublisher statusEventPublisher) {
        this.runOptions = runOptions;
        this.projectNameVersionDecider = projectNameVersionDecider;
        this.statusEventPublisher = statusEventPublisher;
    }

    public NameVersion execute(List<DetectToolProjectInfo> detectToolProjectInfoList) throws DetectUserFriendlyException, IntegrationException {
        NameVersion projectNameVersion = projectNameVersionDecider.decideProjectNameVersion(runOptions.getPreferredTools(), detectToolProjectInfoList);
        statusEventPublisher.publishOperation(new Operation(OPERATION_NAME, StatusType.SUCCESS));
        return projectNameVersion;
    }
}
