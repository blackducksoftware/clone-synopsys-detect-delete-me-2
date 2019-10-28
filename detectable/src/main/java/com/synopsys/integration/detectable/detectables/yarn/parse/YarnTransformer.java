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
package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class YarnTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExternalIdFactory externalIdFactory;

    public YarnTransformer(final ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph transform(final PackageJson packageJson, final YarnLock yarnLock) {
        final MutableDependencyGraph graph = new MutableMapDependencyGraph();

        for (final Map.Entry<String, String> packageJsonEntry : packageJson.dependencies.entrySet()) {
            final String dependencyFuzzyId = packageJsonEntry.getKey() + "@" + packageJsonEntry.getValue();
            final Optional<YarnLock.Entry> entry = yarnLock.entryForFuzzyId(dependencyFuzzyId);

            if (entry.isPresent()) {
                final Dependency dependency = buildGraph(yarnLock, graph, entry.get());
                graph.addChildToRoot(dependency);
            } else {
                logger.warn(String.format("Missing entry in yarn.lock for '%s'", dependencyFuzzyId));
            }
        }

        return graph;
    }

    private Dependency buildGraph(final YarnLock yarnLock, final MutableDependencyGraph mutableDependencyGraph, final YarnLock.Entry yarnLockEntry) {
        final String name = yarnLockEntry.getName();
        final String version = yarnLockEntry.getResolvedVersion();
        final ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.NPMJS, name, version);
        final Dependency dependency = new Dependency(externalId);

        for (final YarnLock.Entry.Dependency yarnDependency : yarnLockEntry.getYarnDependencies()) {
            final Optional<YarnLock.Entry> dependencyEntry = yarnLock.entryForFuzzyId(yarnDependency.getFuzzyId());
            if (dependencyEntry.isPresent()) {
                final Dependency transitiveDependency = buildGraph(yarnLock, mutableDependencyGraph, dependencyEntry.get());
                mutableDependencyGraph.addParentWithChild(dependency, transitiveDependency);
            } else {
                logger.warn(String.format("Missing entry in yarn.lock for '%s'", yarnDependency.getFuzzyId()));
            }
        }

        return dependency;
    }
}
