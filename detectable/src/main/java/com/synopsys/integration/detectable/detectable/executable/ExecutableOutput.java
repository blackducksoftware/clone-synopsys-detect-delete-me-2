/**
 * detectable
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.detectable.detectable.executable;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutableOutput {
    private int returnCode = 0;
    private final String standardOutput;
    private final String errorOutput;
    private final String commandDescription;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ExecutableOutput(final String commandDescription, final int returnCode, final String standardOutput, final String errorOutput) {
        this.commandDescription = commandDescription;
        this.returnCode = returnCode;
        this.standardOutput = standardOutput;
        this.errorOutput = errorOutput;
    }

    public List<String> getStandardOutputAsList() {
        return Arrays.asList(standardOutput.split(System.lineSeparator()));
    }

    public List<String> getErrorOutputAsList() {
        return Arrays.asList(errorOutput.split(System.lineSeparator()));
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void logExecutionInfo() {
        if (StringUtils.isNotBlank(standardOutput)) {
            logger.info(standardOutput);
        }

        if (StringUtils.isNotBlank(errorOutput)) {
            logger.info(errorOutput);
        }
    }

    public String getCommandDescription() {
        return commandDescription;
    }
}
