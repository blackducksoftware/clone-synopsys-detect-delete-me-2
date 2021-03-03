/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.workflow.status.Status;
import com.synopsys.integration.detect.workflow.status.StatusType;

public class OperationResult<T> {
    private String name;
    @Nullable
    private T content;
    private List<Status> statuses;
    private List<ExitCodeRequest> exitCodes;
    @Nullable
    private Exception operationException;

    public static <T> OperationResult<T> empty() {
        return new OperationResult("", null, new LinkedList<>(), new LinkedList<>(), null);
    }

    public static <T> OperationResult<T> success(String name) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.SUCCESS));
        return new OperationResult(name, null, statuses, new LinkedList<>(), null);
    }

    public static <T> OperationResult<T> success(String name, T content) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.SUCCESS));
        return new OperationResult(name, content, statuses, new LinkedList<>(), null);
    }

    public static <T> OperationResult<T> fail(String name) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.FAILURE));
        return new OperationResult(name, null, statuses, new LinkedList<>(), null);
    }

    public static <T> OperationResult<T> fail(String name, T content) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.FAILURE));
        return new OperationResult(name, content, statuses, new LinkedList<>(), null);
    }

    public static <T> OperationResult<T> fail(String name, Exception operationException) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.FAILURE));
        return new OperationResult(name, null, statuses, new LinkedList<>(), operationException);
    }

    public OperationResult(String name, @Nullable T content, List<Status> statuses, List<ExitCodeRequest> exitCodes, @Nullable Exception operationException) {
        this.name = name;
        this.content = content;
        this.statuses = statuses;
        this.exitCodes = exitCodes;
        this.operationException = operationException;
    }

    public boolean anyFailed() {
        return statuses.stream()
                   .map(Status::getStatusType)
                   .anyMatch(StatusType.FAILURE::equals);
    }

    public boolean allFailed() {
        return statuses.stream()
                   .map(Status::getStatusType)
                   .allMatch(StatusType.FAILURE::equals);
    }

    public boolean anySuccess() {
        return statuses.stream()
                   .map(Status::getStatusType)
                   .anyMatch(StatusType.SUCCESS::equals);
    }

    public boolean allSuccess() {
        return statuses.stream()
                   .map(Status::getStatusType)
                   .allMatch(StatusType.SUCCESS::equals);
    }

    public boolean shouldHaltExecution() {
        return getOperationException().isPresent();
    }

    public void addStatus(Status status) {
        this.statuses.add(status);
    }

    public void addExitCode(ExitCodeRequest exitCodeRequest) {
        this.exitCodes.add(exitCodeRequest);
    }

    public <T> void aggregateResultData(OperationResult<T> otherResult) {
        for (Status status : otherResult.getStatuses()) {
            addStatus(status);
        }

        for (ExitCodeRequest exitCodeRequest : otherResult.getExitCodes()) {
            addExitCode(exitCodeRequest);
        }

        // preserve first exception encountered
        if (!shouldHaltExecution()) {
            setOperationException(otherResult.getOperationException().orElse(null));
        }
    }

    public String getName() {
        return name;
    }

    public Optional<T> getContent() {
        return Optional.ofNullable(content);
    }

    public List<ExitCodeRequest> getExitCodes() {
        return exitCodes;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public Optional<Exception> getOperationException() {
        return Optional.ofNullable(operationException);
    }

    public void setOperationException(@Nullable Exception operationException) {
        this.operationException = operationException;
    }
}
