/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detect.configuration;

import org.antlr.v4.runtime.misc.NotNull;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.configuration.util.Category;
import com.synopsys.integration.configuration.util.Group;
import com.synopsys.integration.configuration.util.ProductMajorVersion;

public class PropertyBuilder<T extends Property> {
    private final T property;

    public PropertyBuilder(T property) {
        this.property = property;
    }

    public PropertyBuilder<T> setInfo(String name, String fromVersion) {
        property.setInfo(name, fromVersion);
        return this;
    }

    public PropertyBuilder<T> setHelp(@NotNull String shortText) {
        property.setHelp(shortText);
        return this;
    }

    public PropertyBuilder<T> setHelp(@NotNull String shortText, @Nullable String longText) {
        property.setHelp(shortText, longText);
        return this;
    }

    public PropertyBuilder<T> setGroups(Group primaryGroup, Group... additionalGroups) {
        property.setGroups(primaryGroup, additionalGroups);
        return this;
    }

    public PropertyBuilder<T> setCategory(Category category) {
        property.setCategory(category);
        return this;
    }

    public PropertyBuilder<T> setDeprecated(String description, ProductMajorVersion failInVersion, ProductMajorVersion removeInVersion) {
        property.setDeprecated(description, failInVersion, removeInVersion);
        return this;
    }

    public T build() {
        return property;
    }
}
