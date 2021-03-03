/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.operation.input.ImpactAnalysisInput;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisToolResult;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.exception.IntegrationException;

public class ImpactAnalysisOperation {
    private static final String OPERATION_NAME = "BLACK_DUCK_IMPACT_ANALYSIS";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool;

    public ImpactAnalysisOperation(BlackDuckImpactAnalysisTool blackDuckImpactAnalysisTool) {
        this.blackDuckImpactAnalysisTool = blackDuckImpactAnalysisTool;
    }

    public OperationResult<ImpactAnalysisToolResult> execute(ImpactAnalysisInput impactAnalysisInput) throws DetectUserFriendlyException, IntegrationException {
        OperationResult<ImpactAnalysisToolResult> operationResult;
        try {
            ImpactAnalysisToolResult impactAnalysisToolResult = blackDuckImpactAnalysisTool.performImpactAnalysisActions(impactAnalysisInput.getProjectNameVersion(), impactAnalysisInput.getProjectVersionWrapper());

            if (impactAnalysisToolResult.isSuccessful()) {
                logger.info("Vulnerability Impact Analysis successful.");
            } else {
                logger.warn("Something went wrong with the Vulnerability Impact Analysis tool.");
            }

            operationResult = OperationResult.success(OPERATION_NAME, impactAnalysisToolResult);
        } catch (Exception ex) {
            operationResult = OperationResult.fail(OPERATION_NAME, ex);
        }

        return operationResult;
    }

    public boolean shouldImpactAnalysisToolRun() {
        return blackDuckImpactAnalysisTool.shouldRun();
    }
}
