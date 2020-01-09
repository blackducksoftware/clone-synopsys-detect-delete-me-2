/**
 * detect-configuration
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
package com.synopsys.integration.detect.property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

public class SpringPropertySource implements PropertySource {
    private final ConfigurableEnvironment configurableEnvironment;

    public SpringPropertySource(final ConfigurableEnvironment configurableEnvironment) {
        this.configurableEnvironment = configurableEnvironment;
    }

    public boolean containsProperty(final String key) {
        return configurableEnvironment.containsProperty(key);
    }

    public String getProperty(final String key, final String defaultValue) {
        return configurableEnvironment.getProperty(key, defaultValue);
    }

    public String getProperty(final String key) {
        return configurableEnvironment.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> keys = new HashSet<>();
        final MutablePropertySources mutablePropertySources = configurableEnvironment.getPropertySources();
        for (final org.springframework.core.env.PropertySource<?> propertySource : mutablePropertySources) {
            if (propertySource instanceof EnumerablePropertySource) {
                final EnumerablePropertySource<?> enumerablePropertySource = (EnumerablePropertySource<?>) propertySource;
                for (final String propertyName : enumerablePropertySource.getPropertyNames()) {
                    keys.add(propertyName);
                }
            }
        }
        return keys;
    }

    public Map<String, String> getPropertyMap() {
        Map<String, String> map = new HashMap<>();
        for (String property : getPropertyKeys()) {
            map.put(property, getProperty(property));
        }
        return map;
    }
}
