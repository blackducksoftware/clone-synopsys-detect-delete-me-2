/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.operation.input.BdioInput;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;

public class BdioFileGenerationOperation {
    private static final String OPERATION_NAME = "BLACK_DUCK_BDIO_GENERATION";
    private final RunOptions runOptions;
    private final BdioOptions bdioOptions;
    private final BdioManager bdioManager;
    private final EventSystem eventSystem;

    public BdioFileGenerationOperation(RunOptions runOptions, BdioOptions bdioOptions, BdioManager bdioManager, EventSystem eventSystem) {
        this.runOptions = runOptions;
        this.bdioOptions = bdioOptions;
        this.bdioManager = bdioManager;
        this.eventSystem = eventSystem;
    }

    public OperationResult<BdioResult> execute(BdioInput bdioInput) throws OperationException {
        OperationResult<BdioResult> result;
        try {
            BdioResult bdioResult = bdioManager.createBdioFiles(bdioOptions, bdioInput.getAggregateOptions(), bdioInput.getNameVersion(), bdioInput.getCodeLocations(), runOptions.shouldUseBdio2());
            eventSystem.publishEvent(Event.DetectCodeLocationNamesCalculated, bdioResult.getCodeLocationNamesResult());
            result = OperationResult.success(OPERATION_NAME, bdioResult);
        } catch (Exception ex) {
            result = OperationResult.fail(OPERATION_NAME);
            throw new OperationException("Error occurred generating BDIO files.", ex, result);
        }
        return result;
    }
}
