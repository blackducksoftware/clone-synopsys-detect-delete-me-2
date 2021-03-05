/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow;

public class OperationException extends Exception {
    private static final long serialVersionUID = 1L;

    // if this exception is being thrown we don't need to know about the content. The other fields are more important.
    private final OperationResult<?> operationResult;

    public OperationException(String message, OperationResult<?> operationResult) {
        super(message);
        this.operationResult = operationResult;
    }

    public OperationException(String message, Throwable cause, OperationResult<?> operationResult) {
        super(message, cause);
        this.operationResult = operationResult;
    }

    public OperationException(Throwable cause, OperationResult<?> operationResult) {
        super(cause);
        this.operationResult = operationResult;
    }

    public OperationResult<?> getOperationResult() {
        return operationResult;
    }
}
