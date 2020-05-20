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
package com.synopsys.integration.detectable.detectables.cargo.parse;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.moandjiezana.toml.Toml;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectables.cargo.model.CargoLock;
import com.synopsys.integration.detectable.detectables.cargo.model.Package;

public class CargoLockParser {

    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    private final Map<String, Dependency> packageMap = new HashMap<>();

    public DependencyGraph parseLockFile(final String lockFile) throws DetectableException {
        try {
            final CargoLock cargoLock = new Toml().read(lockFile).to(CargoLock.class);
            if (cargoLock.getPackages().isPresent()) {
                return parseDependencies(cargoLock.getPackages().get());
            }
        } catch (IllegalStateException e) {
            throw new DetectableException("Illegal syntax was detected in Cargo.lock file", e);
        }
        return new MutableMapDependencyGraph();
    }

    private DependencyGraph parseDependencies(final List<Package> lockPackages) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        Set<String> rootPackages = determineRootPackages(lockPackages);

        for (final String rootPackage : rootPackages) {
            graph.addChildToRoot(packageMap.get(rootPackage));
        }

        for (final Package lockPackage : lockPackages) {
            if (!lockPackage.getDependencies().isPresent()) {
                continue;
            }
            List<String> trimmedDependencies = extractDependencyNames(lockPackage.getDependencies().get());
            for (final String dependency : trimmedDependencies) {
                Dependency child = packageMap.get(dependency);
                Dependency parent = packageMap.get(lockPackage.getName().orElse(""));
                if (child != null && parent != null) {
                    graph.addChildWithParent(child, parent);
                }
            }
        }
        return graph;
    }

    private Set<String> determineRootPackages(List<Package> lockPackages) {
        Set<String> rootPackages = new HashSet<>();
        Set<String> dependencyPackages = new HashSet<>();

        for (final Package lockPackage : lockPackages) {
            final String projectName = lockPackage.getName().orElse("");
            final String projectVersion = lockPackage.getVersion().orElse("");

            packageMap.put(projectName, createCargoDependency(projectName, projectVersion));
            rootPackages.add(projectName);
            lockPackage.getDependencies()
                .map(this::extractDependencyNames)
                .ifPresent(dependencyPackages::addAll);
        }
        rootPackages.removeAll(dependencyPackages);

        return rootPackages;
    }

    private List<String> extractDependencyNames(List<String> rawDependencies) {
        return rawDependencies.stream()
                   .map(dependency -> dependency.split(" ")[0])
                   .collect(Collectors.toList());
    }

    private Dependency createCargoDependency(final String name, final String version) {
        final ExternalId dependencyExternalId = externalIdFactory.createNameVersionExternalId(Forge.CRATES, name, version);
        return new Dependency(name, version, dependencyExternalId);
    }
}
