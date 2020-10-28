/**
 * detectable
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
package com.synopsys.integration.detectable.detectable.executable;

import com.synopsys.integration.executable.Executable;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class ExecutableFailedException extends Exception {
    private static final long serialVersionUID = -4117278710469900787L;
    private final ExecutableOutput executableOutput;
    private final Executable executable;
    private final ExecutableRunnerException executableException;

    public ExecutableFailedException(final Executable executable, final ExecutableRunnerException executableException) {
        super(executableException);
        this.executableException = executableException;
        this.executable = executable;
        executableOutput = null;
    }

    public ExecutableFailedException(final Executable executable, ExecutableOutput executableOutput) {
        this.executableOutput = executableOutput;
        this.executable = executable;
        executableException = null;
    }

    public ExecutableOutput getExecutableOutput() {
        return executableOutput;
    }

    public Executable getExecutable() {
        return executable;
    }

    public ExecutableRunnerException getExecutableException() {
        return executableException;
    }
}