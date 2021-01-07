/**
 * detector
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
package com.synopsys.integration.detect.tool.detector.status;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorStatusCode;
import com.synopsys.integration.detector.base.DetectorStatusType;

public class DetectorEvaluationStatus {
    @NotNull
    private final DetectorEvaluation evaluation;
    @Nullable
    private final DetectorStatusType extractionStatusCode;
    @Nullable
    private final DetectorStatusCode statusCode;
    @Nullable
    private final StatusType statusType;

    @NotNull
    private final String statusReason;

    public DetectorEvaluationStatus(final @NotNull DetectorEvaluation evaluation, final @Nullable DetectorStatusCode statusCode, @Nullable final StatusType statusType, @Nullable final DetectorStatusType extractionStatusCode,
        final @NotNull String statusReason) {
        this.evaluation = evaluation;
        this.extractionStatusCode = extractionStatusCode;
        this.statusCode = statusCode;
        this.statusType = statusType;
        this.statusReason = statusReason;
    }

    public DetectorEvaluation getEvaluation() {
        return evaluation;
    }

    public DetectorStatusCode getStatusCode() {
        return statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public DetectorStatusType getExtractionStatusCode() {
        return extractionStatusCode;
    }
}
