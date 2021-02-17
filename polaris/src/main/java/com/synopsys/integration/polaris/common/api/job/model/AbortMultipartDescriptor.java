/*
 * polaris
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
package com.synopsys.integration.polaris.common.api.job.model;

import java.time.OffsetDateTime;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

// this file should not be edited - if changes are necessary, the generator should be updated, then this file should be re-created

public class AbortMultipartDescriptor extends PolarisComponent {
    @SerializedName("url")
    private String url;

    @SerializedName("created")
    private OffsetDateTime created;

    @SerializedName("expiration")
    private OffsetDateTime expiration;

    @SerializedName("uploadId")
    private String uploadId;

    /**
     * Get url
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get created
     * @return created
     */
    public OffsetDateTime getCreated() {
        return created;
    }

    /**
     * Get expiration
     * @return expiration
     */
    public OffsetDateTime getExpiration() {
        return expiration;
    }

    /**
     * Get uploadId
     * @return uploadId
     */
    public String getUploadId() {
        return uploadId;
    }

}
