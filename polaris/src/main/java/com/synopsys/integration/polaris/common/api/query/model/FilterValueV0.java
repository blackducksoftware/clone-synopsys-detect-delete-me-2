/*
 * polaris
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.polaris.common.api.query.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

// this file should not be edited - if changes are necessary, the generator should be updated, then this file should be re-created

public class FilterValueV0 extends PolarisComponent {
    @SerializedName("type")
    private String type = "filter-value";

    @SerializedName("id")
    private String id;

    @SerializedName("attributes")
    private FilterValueV0Attributes attributes = null;

    /**
     * &#x60;Automatic&#x60;, &#x60;Non-null&#x60;.  The literal string &#x60;filter-value&#x60;.
     * @return type
     */
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * &#x60;Automatic&#x60;. &#x60;Non-null&#x60;.  The unique identifier of this entity.
     * @return id
     */
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get attributes
     * @return attributes
     */
    public FilterValueV0Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final FilterValueV0Attributes attributes) {
        this.attributes = attributes;
    }

}

