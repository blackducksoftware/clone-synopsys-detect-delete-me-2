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
package com.synopsys.integration.detect.lifecycle.run.runnables;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.detect.lifecycle.run.RunOptions;
import com.synopsys.integration.detect.lifecycle.run.RunResult;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.util.Stringable;

public class RunnableState extends Stringable {
    private final RunResult currentRunResult;
    private final RunOptions runOptions;
    private final NameVersion projectNameVersion;
    private final boolean failure;

    public static final RunnableState success(RunResult currentRunResult, RunOptions runOptions, @Nullable NameVersion projectNameVersion) {
        return new RunnableState(currentRunResult, runOptions, projectNameVersion, false);
    }

    public static final RunnableState fail(RunResult currentRunResult, RunOptions runOptions, @Nullable NameVersion projectNameVersion) {
        return new RunnableState(currentRunResult, runOptions, projectNameVersion, true);
    }

    public static final RunnableState success(RunnableState runnableState) {
        return new RunnableState(runnableState.getCurrentRunResult(), runnableState.getRunOptions(), runnableState.getProjectNameVersion(), false);
    }

    public static final RunnableState fail(RunnableState runnableState) {
        return new RunnableState(runnableState.getCurrentRunResult(), runnableState.getRunOptions(), runnableState.getProjectNameVersion(), true);
    }

    public static final RunnableState of(RunnableState runnableState) {
        return new RunnableState(runnableState.getCurrentRunResult(), runnableState.getRunOptions(), runnableState.getProjectNameVersion(), runnableState.isFailure());
    }

    private RunnableState(RunResult currentRunResult, RunOptions runOptions, NameVersion projectNameVersion, boolean failure) {
        this.currentRunResult = currentRunResult;
        this.runOptions = runOptions;
        this.projectNameVersion = projectNameVersion;
        this.failure = failure;
    }

    public RunResult getCurrentRunResult() {
        return currentRunResult;
    }

    public NameVersion getProjectNameVersion() {
        return projectNameVersion;
    }

    public RunOptions getRunOptions() {
        return runOptions;
    }

    public boolean isFailure() {
        return failure;
    }
}
