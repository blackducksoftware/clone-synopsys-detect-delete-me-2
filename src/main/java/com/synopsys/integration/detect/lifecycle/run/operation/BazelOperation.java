/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation;

import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.tool.DetectableTool;
import com.synopsys.integration.detect.tool.DetectableToolResult;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;

public class BazelOperation {
    private static final String OPERATION_NAME = "BAZEL";
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private DetectDetectableFactory detectDetectableFactory;
    private ExtractionEnvironmentProvider extractionEnvironmentProvider;
    private CodeLocationConverter codeLocationConverter;

    public BazelOperation(DirectoryManager directoryManager, EventSystem eventSystem, DetectDetectableFactory detectDetectableFactory, DetectToolFilter detectToolFilter,
        ExtractionEnvironmentProvider extractionEnvironmentProvider, CodeLocationConverter codeLocationConverter) {
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;
        this.detectDetectableFactory = detectDetectableFactory;
        this.extractionEnvironmentProvider = extractionEnvironmentProvider;
        this.codeLocationConverter = codeLocationConverter;
    }

    public OperationResult<DetectableToolResult> execute() {
        DetectableTool detectableTool = new DetectableTool(detectDetectableFactory::createBazelDetectable,
            extractionEnvironmentProvider, codeLocationConverter, "BAZEL", DetectTool.BAZEL,
            eventSystem);
        DetectableToolResult detectableToolResult = detectableTool.execute(directoryManager.getSourceDirectory());
        OperationResult<DetectableToolResult> operationResult = OperationResult.success(OPERATION_NAME, detectableToolResult);
        if (detectableToolResult.isFailure()) {
            OperationResult.fail(OPERATION_NAME, detectableToolResult);
        }
        return operationResult;
    }
}
