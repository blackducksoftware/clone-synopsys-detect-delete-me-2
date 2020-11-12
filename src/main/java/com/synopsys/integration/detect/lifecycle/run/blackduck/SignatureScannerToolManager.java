/**
 * synopsys-detect
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
package com.synopsys.integration.detect.lifecycle.run.blackduck;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.tool.signaturescanner.SignatureScannerToolResult;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.blackduck.codelocation.CodeLocationAccumulator;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.status.DetectIssue;
import com.synopsys.integration.detect.workflow.status.DetectIssueType;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.util.NameVersion;

public class SignatureScannerToolManager implements ToolRunManager {
    private final BlackDuckSignatureScannerTool blackDuckSignatureScannerTool;
    private final BlackDuckRunData blackDuckRunData;
    private final EventSystem eventSystem;
    private DetectToolFilter detectToolFilter;

    public SignatureScannerToolManager(BlackDuckSignatureScannerTool blackDuckSignatureScannerTool, BlackDuckRunData blackDuckRunData, EventSystem eventSystem, DetectToolFilter detectToolFilter) {
        this.blackDuckSignatureScannerTool = blackDuckSignatureScannerTool;
        this.blackDuckRunData = blackDuckRunData;
        this.eventSystem = eventSystem;
        this.detectToolFilter = detectToolFilter;
    }

    public void runSignatureScanner(NameVersion projectNameVersion, Optional<File> dockerTar, CodeLocationAccumulator codeLocationAccumulator) throws DetectUserFriendlyException {
        SignatureScannerToolResult signatureScannerToolResult = blackDuckSignatureScannerTool.runScanTool(blackDuckRunData, projectNameVersion, dockerTar);
        if (signatureScannerToolResult.getResult() == Result.SUCCESS && signatureScannerToolResult.getCreationData().isPresent()) {
            codeLocationAccumulator.addWaitableCodeLocation(signatureScannerToolResult.getCreationData().get());
        } else if (signatureScannerToolResult.getResult() != Result.SUCCESS) {
            eventSystem.publishEvent(Event.StatusSummary, new Status("SIGNATURE_SCAN", StatusType.FAILURE));
            eventSystem.publishEvent(Event.Issue, new DetectIssue(DetectIssueType.SIGNATURE_SCANNER, Arrays.asList(signatureScannerToolResult.getResult().toString())));
        }
    }

    @Override
    public boolean shouldRun() {
        return true;
    }

    @Override
    public boolean isIncluded() {
        return detectToolFilter.shouldInclude(DetectTool.SIGNATURE_SCAN);
    }
}
