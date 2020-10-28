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
package com.synopsys.integration.detect.interactive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.configuration.source.MapPropertySource;

public class InteractivePropertySourceBuilder {
    private final Map<Property, InteractiveOption> propertyToOptionMap = new HashMap<>();
    private final InteractiveWriter interactiveWriter;
    public static final String PROPERTY_SOURCE_NAME = "interactive";

    public InteractivePropertySourceBuilder(InteractiveWriter interactiveWriter) {
        this.interactiveWriter = interactiveWriter;
    }

    public void setPropertyFromQuestion(Property detectProperty, String question) {
        String value = interactiveWriter.askQuestion(question);
        setProperty(detectProperty, value);
    }

    public void setPropertyFromSecretQuestion(Property detectProperty, String question) {
        String value = interactiveWriter.askSecretQuestion(question);
        setProperty(detectProperty, value);
    }

    public void setProperty(Property detectProperty, String value) {
        InteractiveOption option;
        if (!propertyToOptionMap.containsKey(detectProperty)) {
            option = new InteractiveOption();
            option.setDetectProperty(detectProperty);
            propertyToOptionMap.put(detectProperty, option);
        } else {
            option = propertyToOptionMap.get(detectProperty);
        }
        option.setInteractiveValue(value);
    }

    public Properties optionsToProperties() {
        Properties properties = new Properties();
        for (InteractiveOption interactiveOption : propertyToOptionMap.values()) {
            properties.put(interactiveOption.getDetectProperty().getKey(), interactiveOption.getInteractiveValue());
        }

        return properties;
    }

    public void saveToApplicationProperties() {
        saveToApplicationProperties(null);
    }

    public void saveToApplicationProperties(String profileName) {
        Properties properties = optionsToProperties();
        File directory = new File(System.getProperty("user.dir"));
        String fileName = "application.properties";
        if (profileName != null) {
            fileName = "application-" + profileName + ".properties";
        }

        File applicationsProperty = new File(directory, fileName);
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(applicationsProperty);
            properties.store(outputStream, "Automatically generated during Detect Interactive Mode.");
            interactiveWriter.println();
            interactiveWriter.println("Successfully saved to '" + applicationsProperty.getCanonicalPath() + "'!");
            outputStream.close();
        } catch (IOException e) {
            interactiveWriter.println(e);
            interactiveWriter.println("Failed to write to application.properties.");
            throw new RuntimeException(e);
        }
    }

    public MapPropertySource build() {
        Map<String, String> interactivePropertyMap = propertyToOptionMap.values().stream()
                                                         .collect(Collectors.toMap(option -> option.getDetectProperty().getKey(), InteractiveOption::getInteractiveValue));
        return new MapPropertySource(PROPERTY_SOURCE_NAME, interactivePropertyMap);
    }

}
