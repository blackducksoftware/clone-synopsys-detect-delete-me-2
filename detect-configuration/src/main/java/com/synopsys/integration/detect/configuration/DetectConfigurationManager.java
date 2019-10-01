/**
 * detect-configuration
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.enumeration.PolicySeverityType;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.help.DetectOption;
import com.synopsys.integration.detect.property.PropertyType;
import com.synopsys.integration.detect.util.TildeInPathResolver;

public class DetectConfigurationManager {
    public static final String USER_HOME = System.getProperty("user.home");
    private final Logger logger = LoggerFactory.getLogger(DetectConfigurationManager.class);

    private final TildeInPathResolver tildeInPathResolver;
    private final DetectConfiguration detectConfiguration;

    private List<String> detectorSearchDirectoryExclusions;

    // properties to be updated
    private String policyCheckFailOnSeverities;
    private int parallelProcessors;
    private boolean hubOfflineMode;
    // end properties to be updated

    public DetectConfigurationManager(final TildeInPathResolver tildeInPathResolver, final DetectConfiguration detectConfiguration) {
        this.tildeInPathResolver = tildeInPathResolver;
        this.detectConfiguration = detectConfiguration;
    }

    public void process(final List<DetectOption> detectOptions) throws DetectUserFriendlyException {
        resolveTildeInPaths();
        resolvePolicyProperties();
        resolveParallelProcessingProperties();
        resolveSignatureScannerProperties();
        resolveDetectorSearchProperties();

        updateDetectProperties(detectOptions);
    }

    private void resolveTildeInPaths() {
        if (detectConfiguration.getBooleanProperty(DetectProperty.DETECT_RESOLVE_TILDE_IN_PATHS, PropertyAuthority.NONE)) {
            detectConfiguration.getCurrentProperties().keySet()
                .forEach(this::resolveTildeInDetectProperty);
        }
    }

    private void resolveTildeInDetectProperty(final DetectProperty detectProperty) {
        if (PropertyType.STRING == detectProperty.getPropertyType()) {
            final Optional<String> resolved = tildeInPathResolver.resolveTildeInValue(detectConfiguration.getProperty(detectProperty, PropertyAuthority.NONE));
            resolved.ifPresent(resolvedValue -> detectConfiguration.setDetectProperty(detectProperty, resolvedValue));
        }
    }

    private void resolvePolicyProperties() {
        final String policyCheckFailOnSeverities = detectConfiguration.getProperty(DetectProperty.DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES, PropertyAuthority.NONE);
        final boolean atLeastOnePolicySeverity = StringUtils.isNotBlank(policyCheckFailOnSeverities);
        if (atLeastOnePolicySeverity) {
            boolean allSeverities = false;
            final String[] splitSeverities = policyCheckFailOnSeverities.split(",");
            for (final String severity : splitSeverities) {
                if (severity.equalsIgnoreCase("ALL")) {
                    allSeverities = true;
                    break;
                }
            }
            if (allSeverities) {
                final List<String> allPolicyTypes = Arrays.stream(PolicySeverityType.values()).filter(type -> type != PolicySeverityType.UNSPECIFIED).map(Enum::toString).collect(Collectors.toList());
                this.policyCheckFailOnSeverities = StringUtils.join(allPolicyTypes, ",");
            } else {
                this.policyCheckFailOnSeverities = StringUtils.join(splitSeverities, ",");
            }
        }
    }

    private void resolveParallelProcessingProperties() {
        int providedParallelProcessors = detectConfiguration.getIntegerProperty(DetectProperty.DETECT_PARALLEL_PROCESSORS, PropertyAuthority.NONE);
        if (providedParallelProcessors <= 0) {
            providedParallelProcessors = Runtime.getRuntime().availableProcessors();
        }
        this.parallelProcessors = providedParallelProcessors;
    }

    private void resolveSignatureScannerProperties() throws DetectUserFriendlyException {
        if (StringUtils.isNotBlank(detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL, PropertyAuthority.NONE)) &&
                StringUtils.isNotBlank(detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH, PropertyAuthority.NONE))) {
            throw new DetectUserFriendlyException(
                "You have provided both a Black Duck signature scanner url AND a local Black Duck signature scanner path. Only one of these properties can be set at a time. If both are used together, the *correct* source of the signature scanner can not be determined.",
                ExitCodeType.FAILURE_GENERAL_ERROR);
        }
        final Boolean originalOfflineMode = detectConfiguration.getBooleanProperty(DetectProperty.BLACKDUCK_OFFLINE_MODE, PropertyAuthority.NONE);
        hubOfflineMode = originalOfflineMode;
        if (StringUtils.isNotBlank(detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_HOST_URL, PropertyAuthority.NONE))) {
            logger.info("A Black Duck signature scanner url was provided, which requires Black Duck offline mode. Setting Black Duck offline mode to true.");
            hubOfflineMode = true;
        }
        if (StringUtils.isNotBlank(detectConfiguration.getProperty(DetectProperty.DETECT_BLACKDUCK_SIGNATURE_SCANNER_OFFLINE_LOCAL_PATH, PropertyAuthority.NONE))) {
            logger.info("A local Black Duck signature scanner path was provided, which requires Black Duck offline mode. Setting Black Duck offline mode to true.");
            hubOfflineMode = true;
        }
    }

    private void resolveDetectorSearchProperties() {
        detectorSearchDirectoryExclusions = new ArrayList<>();
        detectorSearchDirectoryExclusions.addAll(Arrays.asList(detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION, PropertyAuthority.NONE)));

        if (detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION_DEFAULTS, PropertyAuthority.NONE)) {
            final List<String> defaultExcludedNames = Arrays.stream(DetectorSearchExcludedDirectories.values()).map(DetectorSearchExcludedDirectories::getDirectoryName).collect(Collectors.toList());
            detectorSearchDirectoryExclusions.addAll(defaultExcludedNames);
        }
    }

    private void updateDetectProperties(final List<DetectOption> detectOptions) {
        updateOptionValue(detectOptions, DetectProperty.DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES, policyCheckFailOnSeverities);
        detectConfiguration.setDetectProperty(DetectProperty.DETECT_POLICY_CHECK_FAIL_ON_SEVERITIES, policyCheckFailOnSeverities);

        updateOptionValue(detectOptions, DetectProperty.DETECT_PARALLEL_PROCESSORS, String.valueOf(parallelProcessors));
        detectConfiguration.setDetectProperty(DetectProperty.DETECT_PARALLEL_PROCESSORS, String.valueOf(parallelProcessors));

        updateOptionValue(detectOptions, DetectProperty.BLACKDUCK_OFFLINE_MODE, String.valueOf(hubOfflineMode));
        detectConfiguration.setDetectProperty(DetectProperty.BLACKDUCK_OFFLINE_MODE, String.valueOf(hubOfflineMode));

        updateOptionValue(detectOptions, DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION, StringUtils.join(detectorSearchDirectoryExclusions, ","));
        detectConfiguration.setDetectProperty(DetectProperty.DETECT_DETECTOR_SEARCH_EXCLUSION, StringUtils.join(detectorSearchDirectoryExclusions, ","));

    }

    private void updateOptionValue(final List<DetectOption> detectOptions, final DetectProperty detectProperty, final String value) {
        detectOptions.forEach(option -> {
            if (option.getDetectProperty() == detectProperty) {
                option.setPostInitValue(value);
            }
        });
    }

    @SuppressWarnings("unused")
    private void requestDeprecation(final List<DetectOption> detectOptions, final DetectProperty detectProperty) {
        detectOptions.forEach(option -> {
            if (option.getDetectProperty() == detectProperty) {
                option.requestDeprecation();
            }
        });
    }

}
