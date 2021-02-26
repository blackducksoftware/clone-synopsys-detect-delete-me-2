/*
 * configuration
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.configuration.config.value;

import java.util.Optional;

import com.synopsys.integration.configuration.config.resolution.PropertyResolutionInfo;
import com.synopsys.integration.configuration.parse.ValueParseException;

public abstract class PropertyValue<T> {
    public abstract Optional<T> getValue();

    public abstract Optional<PropertyResolutionInfo> getResolutionInfo();

    public abstract Optional<ValueParseException> getException();
}
