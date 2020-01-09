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
package com.synopsys.integration.detect.help;

import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.detect.configuration.DetectProperty;

public class DetectSingleOption extends DetectOption {
    public DetectSingleOption(final DetectProperty detectProperty, final boolean strictValidation, final boolean caseSensitiveValidation, final List<String> validValues, final DetectOptionHelp detectOptionHelp, final String resolvedValue) {
        super(detectProperty, strictValidation, caseSensitiveValidation, validValues, detectOptionHelp, resolvedValue);
    }

    @Override
    public OptionValidationResult validateValue(final String value) {
        OptionValidationResult result;

        if (validValuesContains(value)) {
            result = OptionValidationResult.valid("");
        } else {
            final String validationMesssage = String.format("%s: Unknown value '%s', acceptable values are %s", getDetectProperty().getPropertyKey(), value, getValidValues().stream().collect(Collectors.joining(",")));
            result = OptionValidationResult.invalid(validationMesssage);
        }

        return result;
    }

    @Override
    public boolean isCommaSeperatedList() {
        return false;
    }

}
