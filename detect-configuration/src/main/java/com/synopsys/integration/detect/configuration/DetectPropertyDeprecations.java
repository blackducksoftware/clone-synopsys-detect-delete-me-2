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
package com.synopsys.integration.detect.configuration;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class DetectPropertyDeprecations {
    static final Map<DetectProperty, DetectProperty> PROPERTY_OVERRIDES = new HashMap<>();

    //You do NOT need to add your deprecated property to this override map.
    //This is simply a helper when ONE property is replaced by ONE new property such that you can treat the new property the same as the old. (Only need to GET/SET the new property).
    //In the case or CHAINING or COMPLEX behavior between the deprecated and current properties it is left up to the developer.
    //For example, with PARALLEL_PROCESSORS, configuration manager calculates him based on two other properties.
    //For example, with DETECT_BLACKDUCK_SIGNATURE_SCANNER_DISABLED and DETECT_HUB_SIGNATURE_SCANNER_DISABLED, even though it appears here, additional code in the config factory handles what should actually happen.

    static {
        PROPERTY_OVERRIDES.put(DetectProperty.LOGGING_LEVEL_COM_BLACKDUCKSOFTWARE_INTEGRATION, DetectProperty.LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_DISABLE_WITHOUT_BLACKDUCK, DetectProperty.DETECT_IGNORE_CONNECTION_FAILURES);

        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_API_TOKEN, DetectProperty.BLACKDUCK_API_TOKEN);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_URL, DetectProperty.BLACKDUCK_URL);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_TIMEOUT, DetectProperty.BLACKDUCK_TIMEOUT);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_USERNAME, DetectProperty.BLACKDUCK_USERNAME);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PASSWORD, DetectProperty.BLACKDUCK_PASSWORD);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_HOST, DetectProperty.BLACKDUCK_PROXY_HOST);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_PORT, DetectProperty.BLACKDUCK_PROXY_PORT);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_USERNAME, DetectProperty.BLACKDUCK_PROXY_USERNAME);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_PASSWORD, DetectProperty.BLACKDUCK_PROXY_PASSWORD);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_NTLM_DOMAIN, DetectProperty.BLACKDUCK_PROXY_NTLM_DOMAIN);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_IGNORED_HOSTS, DetectProperty.BLACKDUCK_PROXY_IGNORED_HOSTS);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_PROXY_NTLM_WORKSTATION, DetectProperty.BLACKDUCK_PROXY_NTLM_WORKSTATION);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_TRUST_CERT, DetectProperty.BLACKDUCK_TRUST_CERT);
        PROPERTY_OVERRIDES.put(DetectProperty.BLACKDUCK_HUB_OFFLINE_MODE, DetectProperty.BLACKDUCK_OFFLINE_MODE);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_DISABLE_WITHOUT_HUB, DetectProperty.DETECT_DISABLE_WITHOUT_BLACKDUCK);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_DRY_RUN, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_SNIPPET_MODE, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_SNIPPET_MODE);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_PATTERNS, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_PATTERNS);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_PATHS, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_PATHS);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_EXCLUSION_NAME_PATTERNS);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_MEMORY, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_MEMORY);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_DISABLED, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DISABLED);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_LOCAL_PATH, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_LOCAL_PATH);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_HOST_URL, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_HUB_SIGNATURE_SCANNER_ARGUMENTS, DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_ARGUMENTS);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_MAVEN_SCOPE, DetectProperty.DETECT_MAVEN_INCLUDED_SCOPES);

        //Detector
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_PROJECT_BOM_TOOL, DetectProperty.DETECT_PROJECT_DETECTOR);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_BOM_TOOL_SEARCH_DEPTH, DetectProperty.DETECT_DETECTOR_SEARCH_DEPTH);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_REQUIRED_BOM_TOOL_TYPES, DetectProperty.DETECT_REQUIRED_DETECTOR_TYPES);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_BOM_TOOL_SEARCH_CONTINUE, DetectProperty.DETECT_DETECTOR_SEARCH_CONTINUE);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_BOM_TOOL_SEARCH_EXCLUSION, DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_BOM_TOOL_SEARCH_EXCLUSION_DEFAULTS, DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION_DEFAULTS);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_EXCLUDED_BOM_TOOL_TYPES, DetectProperty.DETECT_EXCLUDED_DETECTOR_TYPES);
        PROPERTY_OVERRIDES.put(DetectProperty.DETECT_INCLUDED_BOM_TOOL_TYPES, DetectProperty.DETECT_INCLUDED_DETECTOR_TYPES);
    }
}
