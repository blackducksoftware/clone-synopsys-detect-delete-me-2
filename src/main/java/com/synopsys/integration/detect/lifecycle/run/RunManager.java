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
package com.synopsys.integration.detect.lifecycle.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.workflow.WorkFlowFactory;
import com.synopsys.integration.detect.lifecycle.run.workflow.Workflow;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.workflow.DetectRun;
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;

public class RunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectContext detectContext;
    private final DetectRun detectRun;
    private final ExitCodeManager exitCodeManager;

    public RunManager(DetectContext detectContext, DetectRun detectRun, ExitCodeManager exitCodeManager) {
        this.detectContext = detectContext;
        this.detectRun = detectRun;
        this.exitCodeManager = exitCodeManager;
    }

    public RunResult run(RunContext runContext) {
        RunResult runResult = new RunResult();
        try {
            logger.debug("Detect run begin: {}", detectRun.getRunId());
            RunOptions runOptions = runContext.createRunOptions();
            Workflow workflow = WorkFlowFactory.createWorkflow(runContext);

            runResult = workflow.execute();

            logger.info("All tools have finished.");
            logger.info(ReportConstants.RUN_SEPARATOR);
            logger.debug("Detect run completed.");
        } catch (Exception e) {
            if (e.getMessage() != null) {
                logger.error("Detect run failed: {}", e.getMessage());
            } else {
                logger.error("Detect run failed: {}", e.getClass().getSimpleName());
            }
            logger.debug("An exception was thrown during the detect run.", e);
            exitCodeManager.requestExitCode(e);
        }
        return runResult;
    }
}
