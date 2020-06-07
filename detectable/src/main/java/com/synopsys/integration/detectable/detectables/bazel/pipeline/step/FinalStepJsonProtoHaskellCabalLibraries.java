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
package com.synopsys.integration.detectable.detectables.bazel.pipeline.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.exception.IntegrationException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FinalStepJsonProtoHaskellCabalLibraries implements FinalStep {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson;

    public FinalStepJsonProtoHaskellCabalLibraries(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public MutableDependencyGraph finish(final List<String> input) throws IntegrationException {
        final JsonElement resultsElement = JsonParser.parseString(input.get(0));
        final JsonObject resultsObject = resultsElement.getAsJsonObject();
        final JsonElement resultsMember = resultsObject.get("results");
        final JsonArray targets = resultsMember.getAsJsonArray();
        logger.info(String.format("Number of targets: %d", targets.size()));
        final JsonElement firstTargetElement = targets.get(0);
        final JsonObject firstTargetObject = firstTargetElement.getAsJsonObject();
        final JsonElement firstTargetElementSub = firstTargetObject.get("target");
        final JsonObject firstTargetObjectSub = firstTargetElementSub.getAsJsonObject();
        logger.info(String.format("firstTargetType: %s", firstTargetObjectSub.get("type").toString()));

        return null;
    }
}
