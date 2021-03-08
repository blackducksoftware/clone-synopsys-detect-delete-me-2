/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.status;

import java.time.Instant;

public class Status {
    private final String descriptionKey;
    private final StatusType statusType;
    private final Instant creationDate;

    public Status(String descriptionKey, StatusType statusType) {
        this(descriptionKey, statusType, Instant.now());
    }

    public Status(String descriptionKey, StatusType statusType, Instant creationDate) {
        this.descriptionKey = descriptionKey;
        this.statusType = statusType;
        this.creationDate = creationDate;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
}
