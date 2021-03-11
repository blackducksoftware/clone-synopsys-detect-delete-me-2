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

    public static <T> OperationResult<T> empty() {
        return new OperationResult("", null, new LinkedList<>(), new LinkedList<>());
    }

    public static <T> OperationResult<T> success(String name) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.SUCCESS));
        return new OperationResult(name, null, statuses, new LinkedList<>());
    }

    public static <T> OperationResult<T> success(String name, T content) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.SUCCESS));
        return new OperationResult(name, content, statuses, new LinkedList<>());
    }

    public static <T> OperationResult<T> fail(String name) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.FAILURE));
        return new OperationResult(name, null, statuses, new LinkedList<>());
    }

    public static <T> OperationResult<T> fail(String name, T content) {
        List<Status> statuses = new LinkedList<>();
        statuses.add(new Status(name, StatusType.FAILURE));
        return new OperationResult(name, content, statuses, new LinkedList<>());
    }

    private OperationResult(String name, @Nullable T content, List<Status> statuses, List<ExitCodeRequest> exitCodes) {
        this.name = name;
        this.content = content;
        this.statuses = statuses;
        this.exitCodes = exitCodes;
    }

    public boolean anyFailed() {
        return statuses.stream()
                   .map(Status::getStatusType)
                   .anyMatch(StatusType.FAILURE::equals);
    }

    public void addStatus(Status status) {
        this.statuses.add(status);
    }

    public void addExitCode(ExitCodeRequest exitCodeRequest) {
        this.exitCodes.add(exitCodeRequest);
    }

    public <T> void aggregateResultData(OperationResult<T> otherResult) {
        // TODO use addAll
        for (Status status : otherResult.getStatuses()) {
            addStatus(status);
        }

        for (ExitCodeRequest exitCodeRequest : otherResult.getExitCodes()) {
            addExitCode(exitCodeRequest);
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
}
