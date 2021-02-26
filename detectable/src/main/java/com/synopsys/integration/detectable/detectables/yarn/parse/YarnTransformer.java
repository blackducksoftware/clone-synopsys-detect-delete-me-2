/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.LazyExternalIdDependencyGraphBuilder;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependencyid.StringDependencyId;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectables.npm.packagejson.model.PackageJson;
import com.synopsys.integration.detectable.detectables.yarn.parse.entry.YarnLockEntry;
import com.synopsys.integration.detectable.detectables.yarn.parse.entry.YarnLockEntryId;
import com.synopsys.integration.util.NameVersion;

public class YarnTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExternalIdFactory externalIdFactory;

    public YarnTransformer(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph transform(YarnLockResult yarnLockResult, boolean productionOnly, List<NameVersion> externalDependencies) throws MissingExternalIdException {
        LazyExternalIdDependencyGraphBuilder graphBuilder = new LazyExternalIdDependencyGraphBuilder();

        addRootNodesToGraph(graphBuilder, yarnLockResult.getRootPackageJson(), yarnLockResult.getWorkspacePackageJsons(), productionOnly);

        for (YarnLockEntry entry : yarnLockResult.getYarnLock().getEntries()) {
            for (YarnLockEntryId entryId : entry.getIds()) {
                StringDependencyId entryExternalId = new StringDependencyId(entryId.getName() + "@" + entryId.getVersion());
                graphBuilder.setDependencyInfo(entryExternalId, entryId.getName(), entry.getVersion(), externalIdFactory.createNameVersionExternalId(Forge.NPMJS, entryId.getName(), entry.getVersion()));
                for (YarnLockDependency entryDependency : entry.getDependencies()) {
                    StringDependencyId entryDependencyExternalId = new StringDependencyId(entryDependency.getName() + "@" + entryDependency.getVersion());
                    if (!productionOnly || !entryDependency.isOptional()) {
                        logger.info("Adding {} as child of {}", entryDependencyExternalId.getValue(), entryExternalId.getValue());
                        graphBuilder.addChildWithParent(entryDependencyExternalId, entryExternalId);
                    } else {
                        logger.debug("Excluding optional dependency: {}", entryDependencyExternalId.getValue());
                    }
                }
            }
        }
        return graphBuilder.build((dependencyId, lazyDependencyInfo) -> {
            Optional<NameVersion> externalDependency = externalDependencies.stream().filter(it -> it.getName().equals(lazyDependencyInfo.getName())).findFirst();
            Optional<ExternalId> externalId = externalDependency.map(it -> externalIdFactory.createNameVersionExternalId(Forge.NPMJS, it.getName(), it.getVersion()));

            if (externalId.isPresent()) {
                return externalId.get();
            } else {
                StringDependencyId stringDependencyId = (StringDependencyId) dependencyId;
                if (isWorkspace(yarnLockResult, dependencyId)) {
                    logger.info("Including workspace {} in the graph", stringDependencyId.getValue());
                } else {
                    logger.warn(String.format("Missing yarn dependency. Dependency '%s' is missing from %s.", stringDependencyId.getValue(), yarnLockResult.getYarnLockFilePath()));
                }
                return externalIdFactory.createNameVersionExternalId(Forge.NPMJS, stringDependencyId.getValue());
            }
        });
    }

    private boolean isWorkspace(YarnLockResult yarnLockResult, com.synopsys.integration.bdio.model.dependencyid.DependencyId dependencyId) {
        for (String workspaceName : yarnLockResult.getWorkspacePackageJsons().keySet()) {
            String dependencyIdString = ((StringDependencyId) dependencyId).getValue();
            if (dependencyIdString.startsWith(workspaceName + "@")) {
                return true;
            }
        }
        return false;
    }

    private void addRootNodesToGraph(LazyExternalIdDependencyGraphBuilder graphBuilder,
        PackageJson rootPackageJson, Map<String, PackageJson> workspacePackageJsons, boolean productionOnly) {
        System.out.printf("* Processing Root PackageJson: %s:%s\n", rootPackageJson.name, rootPackageJson.version);
        for (Map.Entry<String, String> packageDependency : rootPackageJson.dependencies.entrySet()) {
            StringDependencyId stringDependencyId = new StringDependencyId(packageDependency.getKey() + "@" + packageDependency.getValue());
            System.out.printf("ROOT stringDependencyId: %s\n", stringDependencyId);
            graphBuilder.addChildToRoot(stringDependencyId);
        }
        if (!productionOnly) {
            for (Map.Entry<String, String> packageDependency : rootPackageJson.devDependencies.entrySet()) {
                StringDependencyId stringDependencyId = new StringDependencyId(packageDependency.getKey() + "@" + packageDependency.getValue());
                System.out.printf("ROOT stringDependencyId [dev]: %s\n", stringDependencyId);
                graphBuilder.addChildToRoot(stringDependencyId);
            }
        }
        // TODO
        // v4 yarn.lock files have the root and it's dependencies (including workspaces)
        // v1 yarn.lock files do not, so the workspaces must be added to the root
        // Worse: Because v1 yarn.lock files don't define workspaces and their dependencies,
        // we'd need to mine that info from ALL of the workspace package.json files
        // What version of yarn did it switch to v4 yarn.lock? Can we support workspaces starting with that version?
        // Yarn 1 used yarn.lock v1. yarn 2 (from the beginning) has used yarn.lock v4 and included root project.
        // MY CURRENT THINKING IS: YARN V2 (YARN.LOCK V4) ONLY. Do not add workspaces as root dependencies,
        // but let the "dependencies" list do that if appropriate.
        // To be supported: yarn.lock should be v4:
        // __metadata:
        //  version: 4
        //
        //        for (PackageJson curWorkspacePackageJson : workspacePackageJsons.values()) {
        //            System.out.printf("* Processing workspace PackageJson: %s:%s\n", curWorkspacePackageJson.name, curWorkspacePackageJson.version);
        //            StringDependencyId workspaceStringDependencyId = new StringDependencyId(curWorkspacePackageJson.name + "@" + curWorkspacePackageJson.version);
        //            System.out.printf("WORKSPACE stringDependencyId: %s\n", workspaceStringDependencyId);
        //            graphBuilder.addChildToRoot(workspaceStringDependencyId);
        //        }
    }
}
