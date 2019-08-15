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
package com.synopsys.integration.detect.help;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.detect.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperty;
import com.synopsys.integration.detect.configuration.PropertyAuthority;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.interactive.InteractiveOption;
import com.synopsys.integration.detect.util.ProxyUtil;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.SilentIntLogger;

public class DetectOptionManager {
    private final Logger logger = LoggerFactory.getLogger(DetectOptionManager.class);

    private final DetectConfiguration detectConfiguration;
    private final DetectInfo detectInfo;

    private List<DetectOption> detectOptions;
    private List<String> detectGroups;

    public DetectOptionManager(final DetectConfiguration detectConfiguration, final DetectInfo detectInfo) {
        this.detectConfiguration = detectConfiguration;
        this.detectInfo = detectInfo;

        init();
    }

    public List<DetectOption> getDetectOptions() {
        return detectOptions;
    }

    public List<String> getDetectGroups() {
        return detectGroups;
    }

    private void init() {
        final Map<DetectProperty, DetectOption> detectOptionsMap = new HashMap<>();

        final Map<DetectProperty, Object> propertyMap = detectConfiguration.getCurrentProperties();
        if (null != propertyMap && !propertyMap.isEmpty()) {
            for (final DetectProperty detectProperty : propertyMap.keySet()) {
                final DetectOption option = processField(detectProperty, detectConfiguration.getPropertyValueAsString(detectProperty, PropertyAuthority.None));
                if (option != null) {
                    if (!detectOptionsMap.containsKey(detectProperty)) {
                        detectOptionsMap.put(detectProperty, option);
                    }
                }
            }
        }

        detectOptions = detectOptionsMap.values().stream()
                            .sorted((o1, o2) -> o1.getDetectOptionHelp().primaryGroup.compareTo(o2.getDetectOptionHelp().primaryGroup))
                            .collect(Collectors.toList());

        detectGroups = detectOptions.stream()
                           .map(it -> it.getDetectOptionHelp().primaryGroup)
                           .distinct()
                           .sorted()
                           .collect(Collectors.toList());

        checkForRemovedProperties();
    }

    public BlackDuckServerConfig createBlackDuckServerConfig() throws DetectUserFriendlyException {
        return createBlackDuckServerConfig(new SilentIntLogger());
    }

    public BlackDuckServerConfig createBlackDuckServerConfig(final IntLogger logger) throws DetectUserFriendlyException {
        final BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder().setLogger(logger);

        final Set<String> allBlackDuckKeys = new HashSet<>(blackDuckServerConfigBuilder.getPropertyKeys());
        final Map<String, String> blackDuckProperties = detectConfiguration.getProperties(allBlackDuckKeys);
        final Map<String, String> blackDuckPropertiesNoProxy = blackDuckProperties.entrySet().stream()
                                                                   .filter(it -> !it.getKey().toLowerCase().contains("proxy"))
                                                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final List<Pattern> ignoredProxyHostPatterns = ProxyUtil.getIgnoredProxyHostPatterns(detectConfiguration.getProperty(DetectProperty.BLACKDUCK_PROXY_IGNORED_HOSTS, PropertyAuthority.None));
        boolean ignoreProxy = false;
        try {
            ignoreProxy = ProxyUtil.shouldIgnoreHost(new URL(detectConfiguration.getProperty(DetectProperty.BLACKDUCK_URL, PropertyAuthority.None)).getHost(), ignoredProxyHostPatterns);
        } catch (final MalformedURLException e) {
            logger.error("Unable to decide if proxy should be used for the given host, will use proxy.");
        }

        if (ignoreProxy) {
            blackDuckServerConfigBuilder.setProperties(blackDuckPropertiesNoProxy.entrySet());
        } else {
            blackDuckServerConfigBuilder.setProperties(blackDuckProperties.entrySet());
        }

        try {
            final BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            return blackDuckServerConfig;
        } catch (final IllegalArgumentException e) {
            throw new DetectUserFriendlyException("Failed to configure Black Duck server connection: " + e.getMessage(), e, ExitCodeType.FAILURE_CONFIGURATION);
        }
    }

    public void postConfigurationProcessedInit() throws IllegalArgumentException, SecurityException {
        for (final DetectOption option : detectOptions) {
            String fieldValue = option.getPostInitValue();
            if (StringUtils.isBlank(fieldValue)) {
                fieldValue = detectConfiguration.getPropertyValueAsString(option.getDetectProperty(), PropertyAuthority.None);
            }
            final boolean valuesMatch = option.getResolvedValue().equals(fieldValue);
            final boolean propertyWasSet = detectConfiguration.wasPropertyActuallySet(option.getDetectProperty());
            if (!valuesMatch && propertyWasSet) {
                if (option.getInteractiveValue() != null) {
                    option.setFinalValue(fieldValue, DetectOption.FinalValueType.INTERACTIVE);
                } else if (option.getResolvedValue().equals("latest")) {
                    option.setFinalValue(fieldValue, DetectOption.FinalValueType.LATEST);
                } else if (option.getResolvedValue().trim().length() == 0) {
                    option.setFinalValue(fieldValue, DetectOption.FinalValueType.CALCULATED);
                } else {
                    option.setFinalValue(fieldValue, DetectOption.FinalValueType.OVERRIDE);
                }
            } else {
                if (isEqualToDefault(option.getDetectProperty(), fieldValue)) {
                    option.setFinalValue(fieldValue, DetectOption.FinalValueType.DEFAULT);
                } else {
                    if (propertyWasSet) {
                        option.setFinalValue(fieldValue, DetectOption.FinalValueType.SUPPLIED);
                        if (option.getDetectOptionHelp().isDeprecated) {
                            option.requestDeprecation();
                        }
                    } else {
                        option.setFinalValue(fieldValue, DetectOption.FinalValueType.COPIED);
                    }
                }
            }

            if (option.isRequestedDeprecation()) {
                final String removeVersion = option.getDetectOptionHelp().deprecationRemoveInVersion.getDisplayValue();
                final String failVersion = option.getDetectOptionHelp().deprecationFailInVersion.getDisplayValue();
                option.addWarning(option.getDetectOptionHelp().deprecation + " It will cause failure in " + failVersion + " and be removed in " + removeVersion + ".");
            }
        }
    }

    public Boolean isEqualToDefault(final DetectProperty property, final String value) {
        String defaultValue = "";
        if (null != property.getDefaultValue()) {
            defaultValue = property.getDefaultValue();
        }
        return value.equals(defaultValue);
    }

    public void applyInteractiveOptions(final List<InteractiveOption> interactiveOptions) {
        for (final InteractiveOption interactiveOption : interactiveOptions) {
            for (final DetectOption detectOption : detectOptions) {
                if (detectOption.getDetectProperty().equals(interactiveOption.getDetectProperty())) {
                    detectOption.setInteractiveValue(interactiveOption.getInteractiveValue());
                    detectConfiguration.setDetectProperty(detectOption.getDetectProperty(), interactiveOption.getInteractiveValue());
                    break;
                }
            }
        }
    }

    public List<DetectOption.OptionValidationResult> getAllInvalidOptionResults() throws DetectUserFriendlyException {
        return detectOptions.stream()
                   .filter(DetectOption::hasStrictValidation)
                   .map(DetectOption::validate)
                   .filter(validationResult -> !validationResult.isValid())
                   .collect(Collectors.toList());
    }

    private DetectOption processField(final DetectProperty detectProperty, final String currentValue) {
        try {
            final Field field = DetectProperty.class.getField(detectProperty.name());

            String defaultValue = "";
            if (null != detectProperty.getDefaultValue()) {
                defaultValue = detectProperty.getDefaultValue();
            }

            List<String> validValues = new ArrayList<>();
            boolean isCommaSeparatedList = false;
            boolean strictValidation = false;
            boolean caseSensitiveValidation = false;
            final AcceptableValues acceptableValueAnnotation = field.getAnnotation(AcceptableValues.class);
            if (acceptableValueAnnotation != null) {
                validValues = Arrays.asList(acceptableValueAnnotation.value());
                strictValidation = acceptableValueAnnotation.strict();
                caseSensitiveValidation = acceptableValueAnnotation.caseSensitive();
                isCommaSeparatedList = acceptableValueAnnotation.isCommaSeparatedList();
            }

            String resolvedValue = defaultValue;
            field.setAccessible(true);

            final boolean hasValue = null != currentValue;
            if (defaultValue != null && !defaultValue.trim().isEmpty() && !hasValue) {
                resolvedValue = defaultValue;
                detectConfiguration.setDetectProperty(detectProperty, resolvedValue);
            } else if (hasValue) {
                resolvedValue = currentValue;
            }

            final DetectOptionHelp help = processFieldHelp(field);

            final DetectOption detectOption;
            if (isCommaSeparatedList) {
                detectOption = new DetectListOption(detectProperty, strictValidation, caseSensitiveValidation, validValues, help, resolvedValue);
            } else {
                detectOption = new DetectSingleOption(detectProperty, strictValidation, caseSensitiveValidation, validValues, help, resolvedValue);
            }

            return detectOption;
        } catch (final IllegalArgumentException | NoSuchFieldException e) {
            logger.error(String.format("Could not resolve field %s: %s", detectProperty.name(), e.getMessage()));
        }
        return null;
    }

    private DetectOptionHelp processFieldHelp(final Field field) {
        final DetectOptionHelp help = new DetectOptionHelp();

        final HelpDescription descriptionAnnotation = field.getAnnotation(HelpDescription.class);
        help.description = descriptionAnnotation.value();

        final HelpGroup groupAnnotation = field.getAnnotation(HelpGroup.class);
        help.primaryGroup = groupAnnotation.primary();
        final String[] additionalGroups = groupAnnotation.additional();
        if (additionalGroups.length > 0) {
            help.additionalGroups.addAll(Arrays.stream(additionalGroups).collect(Collectors.toList()));
        } else {
            if (StringUtils.isNotBlank(help.primaryGroup)) {
                help.additionalGroups.add(help.primaryGroup);
            }
        }

        final HelpDetailed issuesAnnotation = field.getAnnotation(HelpDetailed.class);
        if (issuesAnnotation != null) {
            help.detailedHelp = issuesAnnotation.value();
        }

        final DetectDeprecation deprecationAnnotation = field.getAnnotation(DetectDeprecation.class);
        if (deprecationAnnotation != null) {
            help.isDeprecated = true;
            help.deprecation = deprecationAnnotation.description();
            help.deprecationFailInVersion = deprecationAnnotation.failInVersion();
            help.deprecationRemoveInVersion = deprecationAnnotation.removeInVersion();
        }

        return help;
    }

    private void checkForRemovedProperties() {
        final int detectMajorVersion = detectInfo.getDetectMajorVersion();
        for (final DetectOption detectOption : detectOptions) {
            if (detectOption.getDetectOptionHelp().isDeprecated) {
                if (detectMajorVersion >= detectOption.getDetectOptionHelp().deprecationRemoveInVersion.getIntValue()) {
                    throw new RuntimeException("A property should have been removed in this Detect Major Version: " + detectOption.getDetectProperty().getPropertyKey());
                }
            }
        }
    }

    public List<DetectOption> findDeprecatedFailureProperties() {
        final int detectMajorVersion = detectInfo.getDetectMajorVersion();

        return detectOptions.stream()
                   .filter(DetectOption::hasWarnings)
                   .filter(DetectOption::isRequestedDeprecation)
                   .filter(detectOption -> detectMajorVersion >= detectOption.getDetectOptionHelp().deprecationFailInVersion.getIntValue())
                   .collect(Collectors.toList());
    }

}
