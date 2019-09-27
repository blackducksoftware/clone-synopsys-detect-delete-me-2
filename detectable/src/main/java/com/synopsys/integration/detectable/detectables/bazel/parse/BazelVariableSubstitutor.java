/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.bazel.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BazelVariableSubstitutor {
    private final Map<String, String> substitutions;

    public BazelVariableSubstitutor(final String bazelTarget) {
        substitutions = new HashMap<>(1);
        substitutions.put("\\$\\{detect.bazel.target}", bazelTarget);
    }

//    public BazelVariableSubstitutor(final String bazelTarget, final String bazelTargetDependencyId) {
//        substitutions = new HashMap<>(2);
//        substitutions.put("\\$\\{detect.bazel.target}", bazelTarget);
        // TODO remove: substitutions.put("\\$\\{detect.bazel.target.dependency}", bazelTargetDependencyId);
//    }

    public List<String> substitute(final List<String> origStrings, final String input) {
        final List<String> modifiedStrings = new ArrayList<>(origStrings.size());
        for (String origString : origStrings) {
            modifiedStrings.add(substitute(origString, input));
        }
        return modifiedStrings;
    }

    private String substitute(final String origString, final String input) {
        String modifiedString = origString;
        if (input != null) {
            substitutions.put("\\$\\{0}", input);
        }
        for (String variablePattern : substitutions.keySet()) {
            modifiedString = modifiedString.replaceAll(variablePattern, substitutions.get(variablePattern));
        }
        return modifiedString;
    }
}
