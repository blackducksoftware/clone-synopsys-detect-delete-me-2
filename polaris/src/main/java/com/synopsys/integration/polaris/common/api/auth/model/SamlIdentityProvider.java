/*
 * polaris
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.polaris.common.api.auth.model;

import com.synopsys.integration.polaris.common.api.PolarisComponent;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

// this file should not be edited - if changes are necessary, the generator should be updated, then this file should be re-created

public class SamlIdentityProvider extends PolarisComponent {
    @SerializedName("attributes")
    private SamlIdentityProviderAttributes attributes = null;

    @SerializedName("id")
    private UUID id;

    @SerializedName("relationships")
    private SamlIdentityProviderRelationships relationships = null;

    @SerializedName("type")
    private String type;

    /**
     * Get attributes
     * @return attributes
     */
    public SamlIdentityProviderAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final SamlIdentityProviderAttributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Get id
     * @return id
     */
    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * Get relationships
     * @return relationships
     */
    public SamlIdentityProviderRelationships getRelationships() {
        return relationships;
    }

    public void setRelationships(final SamlIdentityProviderRelationships relationships) {
        this.relationships = relationships;
    }

    /**
     * Get type
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

}

