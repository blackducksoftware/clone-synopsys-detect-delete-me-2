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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.workflow.status.StatusType;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorResultStatusCodeLookup;
import com.synopsys.integration.detector.base.DetectorStatusCode;
import com.synopsys.integration.detector.base.DetectorStatusType;

public class DetectorEvaluationStatusCreator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DetectorEvaluationStatus statusForEvaluation(DetectorEvaluation evaluation) {
        return new DetectorEvaluationStatus(evaluation, determineStatusCode(evaluation), determineStatusType(evaluation), determineExtractionStatusType(evaluation), determineStatusReason(evaluation));
    }

    @Nullable
    public StatusType determineStatusType(DetectorEvaluation evaluation) {
        StatusType statusType = null;
        if (evaluation.isApplicable()) {
            if (evaluation.isExtractable()) {
                if (evaluation.wasExtractionSuccessful()) {
                    statusType = StatusType.SUCCESS;
                } else {
                    statusType = StatusType.FAILURE;

                    boolean extractionUnknownFailure = !evaluation.wasExtractionFailure() && !evaluation.wasExtractionException();
                    if (extractionUnknownFailure) {
                        logger.warn("An issue occurred in the detector system, an unknown evaluation status was created. Please contact support.");
                    }
                }
            } else if (evaluation.isFallbackExtractable() || evaluation.isPreviousExtractable()) {
                statusType = StatusType.SUCCESS;
            } else {
                statusType = StatusType.FAILURE;
            }
        }
        return statusType;
    }

    public DetectorStatusType determineExtractionStatusType(DetectorEvaluation evaluation) {
        if (evaluation.getExtraction() != null && evaluation.getExtraction().getResult().equals(Extraction.ExtractionResultType.SUCCESS)) {
            return DetectorStatusType.SUCCESS;
        } else if (evaluation.getFallbackFrom() != null && evaluation.getFallbackFrom().isExtractable()) {
            return DetectorStatusType.DEFERRED;
        }
        return DetectorStatusType.FAILURE;
    }

    @Nullable
    public DetectorStatusCode determineStatusCode(DetectorEvaluation evaluation) {
        Class resultClass = null;
        if (!evaluation.isSearchable()) {
            resultClass = evaluation.getSearchable().getResultClass();
        } else if (!evaluation.isApplicable()) {
            resultClass = evaluation.getApplicable().getResultClass();
        } else if (!evaluation.isExtractable()) {
            resultClass = evaluation.getExtractable().getResultClass();
        }
        if (resultClass != null) {
            return DetectorResultStatusCodeLookup.standardLookup.getStatusCode(resultClass);
        } else if (!evaluation.getExtraction().isSuccess()) {
            if (evaluation.getExtraction().getError() instanceof ExecutableFailedException) {
                return DetectorStatusCode.EXECUTABLE_FAILED;
            } else {
                return DetectorStatusCode.EXTRACTION_FAILED;
            }
        } else {
            return DetectorStatusCode.PASSED;
        }
    }

    @NotNull
    public String determineStatusReason(DetectorEvaluation evaluation) {
        if (!evaluation.isSearchable()) {
            return evaluation.getSearchable().getDescription();
        }
        if (!evaluation.isApplicable()) {
            return evaluation.getApplicable().getDescription();
        }
        if (!evaluation.isExtractable()) {
            return evaluation.getExtractable().getDescription();
        }
        if (evaluation.getExtraction().getResult() != Extraction.ExtractionResultType.SUCCESS) {
            if (evaluation.getExtraction().getError() instanceof ExecutableFailedException) {
                ExecutableFailedException failedException = (ExecutableFailedException) evaluation.getExtraction().getError();
                if (failedException.hasReturnCode()) {
                    return "Failed to execute command, returned non-zero: " + failedException.getExecutableDescription();
                } else if (failedException.getExecutableException() != null) {
                    return "Failed to execute command, " + failedException.getExecutableException().getMessage() + " : " + failedException.getExecutableDescription();
                } else {
                    return "Failed to execute command, unknown reason: " + failedException.getExecutableDescription();
                }
            } else {
                return "See logs for further explanation";
            }
        }
        return "Passed";
    }
}
