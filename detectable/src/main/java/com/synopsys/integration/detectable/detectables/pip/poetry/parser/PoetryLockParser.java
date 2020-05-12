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
package com.synopsys.integration.detectable.detectables.pip.poetry.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class PoetryLockParser {

    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String DEPENDENCIES_KEY = "dependencies";
    private static final String PACKAGE_KEY = "package";

    private final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    private final Map<String, Dependency> packageMap = new HashMap<>();

    public DependencyGraph parseLockFile(Path path) throws IOException {
        TomlParseResult result = Toml.parse(path);
        if (result.get(PACKAGE_KEY) != null) {
            TomlArray lockPackages = result.getArray(PACKAGE_KEY);
            return parseDependencies(lockPackages);
        }

        return new MutableMapDependencyGraph();
    }

    private DependencyGraph parseDependencies(final TomlArray lockPackages) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        Set<String> rootPackages = determineRootPackages(lockPackages);

        for (final String rootPackage : rootPackages) {
            graph.addChildToRoot(packageMap.get(rootPackage));
        }

        for (int i = 0; i < lockPackages.size(); i++) {
            TomlTable lockPackage = lockPackages.getTable(i);
            List<String> dependencies = extractFromDependencyList(lockPackage.getTable(DEPENDENCIES_KEY));
            if (dependencies == null || dependencies.isEmpty()) {
                continue;
            }
            List<String> trimmedDependencies = trimDependencies(dependencies);
            for (final String dependency : trimmedDependencies) {
                Dependency child = packageMap.get(dependency);
                Dependency parent = packageMap.get(lockPackage.getString(NAME_KEY));
                if (child != null && parent != null) {
                    graph.addChildWithParent(child, parent);
                }
            }
        }

        return graph;
    }

    private Set<String> determineRootPackages(TomlArray lockPackages) {
        Set<String> rootPackages = new HashSet<>();
        Set<String> dependencyPackages = new HashSet<>();

        for (int i = 0; i < lockPackages.size(); i++) {
            TomlTable lockPackage = lockPackages.getTable(i);

            if (lockPackage != null) {
                final String projectName = lockPackage.getString(NAME_KEY);
                final String projectVersion = lockPackage.getString(VERSION_KEY);

                packageMap.put(projectName, createPoetryDependency(projectName, projectVersion));
                rootPackages.add(projectName);

                if (lockPackage.getTable(DEPENDENCIES_KEY) != null) {
                    List<String> dependencies = extractFromDependencyList(lockPackage.getTable(DEPENDENCIES_KEY));
                    dependencyPackages.addAll(trimDependencies(dependencies));
                }

            }
        }
        rootPackages.removeAll(dependencyPackages);

        return rootPackages;
    }

    private List<String> trimDependencies(List<String> rawDependencies) {
        List<String> trimmedDependencies = new ArrayList<>();

        for (String rawDependency : rawDependencies) {
            String trimmedDependency = rawDependency.split(" ")[0];
            trimmedDependencies.add(trimmedDependency);
        }
        return trimmedDependencies;
    }

    private List<String> extractFromDependencyList(TomlTable dependencyList) {
        List<String> dependencies = new ArrayList<>();
        if (dependencyList == null) {
            return dependencies;
        }
        for (List<String> key : dependencyList.keyPathSet()) {
            dependencies.add(key.get(0));
        }
        return dependencies;
    }

    private Dependency createPoetryDependency(final String name, final String version) {
        final ExternalId dependencyExternalId = externalIdFactory.createNameVersionExternalId(Forge.PYPI, name, version);
        return new Dependency(name, version, dependencyExternalId);
    }
}
