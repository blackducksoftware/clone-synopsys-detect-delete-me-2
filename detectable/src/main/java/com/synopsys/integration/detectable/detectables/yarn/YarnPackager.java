/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.yarn;

import java.util.List;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.npm.packagejson.model.PackageJson;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLock;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockResult;
import com.synopsys.integration.util.NameVersion;

public class YarnPackager {
    private final YarnTransformer yarnTransformer;

    public YarnPackager(YarnTransformer yarnTransformer) {
        this.yarnTransformer = yarnTransformer;
    }

    public YarnResult generateYarnResult(PackageJson rootPackageJson, List<PackageJson> workspacePackageJsons, YarnLock yarnLock, String yarnLockFilePath, List<NameVersion> externalDependencies,
        boolean useProductionOnly) {
        YarnLockResult yarnLockResult = new YarnLockResult(rootPackageJson, workspacePackageJsons, yarnLockFilePath, yarnLock);

        try {
            DependencyGraph dependencyGraph = yarnTransformer.transform(yarnLockResult, useProductionOnly, externalDependencies);
            CodeLocation codeLocation = new CodeLocation(dependencyGraph);

            return YarnResult.success(rootPackageJson.name, rootPackageJson.version, codeLocation);
        } catch (MissingExternalIdException exception) {
            return YarnResult.failure(exception);
        }
    }
}
