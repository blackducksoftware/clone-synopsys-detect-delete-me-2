/*
 * polaris
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.polaris.common.api.job.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

// this file should not be edited - if changes are necessary, the generator should be updated, then this file should be re-created

public class Job extends PolarisComponent {
    @SerializedName("attributes")
    private JobAttributes attributes = null;

    @SerializedName("id")
    private String id;

    @SerializedName("relationships")
    private JobRelationships relationships = null;

    @SerializedName("type")
    private String type;

    /**
     * Get attributes
     * @return attributes
     */
    public JobAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(JobAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Get id
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get relationships
     * @return relationships
     */
    public JobRelationships getRelationships() {
        return relationships;
    }

    public void setRelationships(JobRelationships relationships) {
        this.relationships = relationships;
    }

    /**
     * Get type
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
