/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.bazel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.configuration.property.types.enumfilterable.FilterableEnumUtils;
import com.synopsys.integration.configuration.property.types.enumfilterable.FilterableEnumValue;

public class BazelDetectableOptions {
    private final String targetName;
    private final List<FilterableEnumValue<WorkspaceRule>> bazelDependencyRulesPropertyValues;
    private final List<String> bazelCqueryAdditionalOptions;

    public BazelDetectableOptions(String targetName, List<FilterableEnumValue<WorkspaceRule>> bazelDependencyRulesPropertyValues,
        List<String> bazelCqueryAdditionalOptions) {
        this.targetName = targetName;
        this.bazelDependencyRulesPropertyValues = bazelDependencyRulesPropertyValues;
        this.bazelCqueryAdditionalOptions = bazelCqueryAdditionalOptions;
    }

    public Optional<String> getTargetName() {
        return Optional.ofNullable(targetName);
    }

    public List<String> getBazelCqueryAdditionalOptions() {
        return bazelCqueryAdditionalOptions;
    }

    public Set<WorkspaceRule> getBazelDependencyRules() {
        Set<WorkspaceRule> bazelDependencyRules = new HashSet<>();
        if (noneSpecified(bazelDependencyRulesPropertyValues)) {
            // Leave bazelDependencyRules empty
        } else if (allSpecified(bazelDependencyRulesPropertyValues)) {
            bazelDependencyRules.addAll(Arrays.asList(WorkspaceRule.values()));
        } else {
            bazelDependencyRules.addAll(FilterableEnumUtils.toPresentValues(bazelDependencyRulesPropertyValues));
        }
        return bazelDependencyRules;
    }

    private boolean noneSpecified(List<FilterableEnumValue<WorkspaceRule>> rulesPropertyValues) {
        boolean noneWasSpecified = false;
        if (rulesPropertyValues == null ||
                FilterableEnumUtils.containsNone(rulesPropertyValues) ||
                (FilterableEnumUtils.toPresentValues(rulesPropertyValues).isEmpty() && !FilterableEnumUtils.containsAll(rulesPropertyValues))) {
            noneWasSpecified = true;
        }
        return noneWasSpecified;
    }

    private boolean allSpecified(List<FilterableEnumValue<WorkspaceRule>> userProvidedRules) {
        boolean allWasSpecified = false;
        if (userProvidedRules != null && FilterableEnumUtils.containsAll(userProvidedRules)) {
            allWasSpecified = true;
        }
        return allWasSpecified;
    }
}
