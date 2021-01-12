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
package com.synopsys.integration.detect.lifecycle.run.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.exception.IntegrationException;

public abstract class AbstractStep {
    private Logger logger = LoggerFactory.getLogger(getClass());

    protected abstract boolean shouldRun();

    public final boolean execute(RunResult runResult) throws DetectUserFriendlyException, IntegrationException {
        String stepName = getStepName();
        boolean success = true;
        if (shouldRun()) {
            logger.info("Will include the {} tool.", stepName);
            success = run(runResult);
            logger.info("{} actions finished.", stepName);
        } else {
            logger.info("{} tool will not be run.", stepName);
        }
        return success;
    }

    public abstract String getStepName();

    protected abstract boolean run(RunResult runResult) throws DetectUserFriendlyException, IntegrationException;
}
