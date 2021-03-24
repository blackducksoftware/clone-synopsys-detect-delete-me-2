/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.yarn;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.builder.MissingExternalIdException;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.npm.packagejson.model.PackageJson;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLock;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockParser;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockResult;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnTransformer;
import com.synopsys.integration.util.NameVersion;

public class YarnPackager {
    private final Gson gson;
    private final YarnLockParser yarnLockParser;
    private final YarnTransformer yarnTransformer;
    private final YarnLockOptions yarnLockOptions;

    public YarnPackager(Gson gson, YarnLockParser yarnLockParser, YarnTransformer yarnTransformer, YarnLockOptions yarnLockOptions) {
        this.gson = gson;
        this.yarnLockParser = yarnLockParser;
        this.yarnTransformer = yarnTransformer;
        this.yarnLockOptions = yarnLockOptions;
    }

    public YarnResult generateYarnResult(String packageJsonText, List<String> yarnLockLines, String yarnLockFilePath, List<NameVersion> externalDependencies) {
        PackageJson packageJson = gson.fromJson(packageJsonText, PackageJson.class);
        YarnLock yarnLock = yarnLockParser.parseYarnLock(yarnLockLines);
        YarnLockResult yarnLockResult = new YarnLockResult(packageJson, yarnLockFilePath, yarnLock);

        try {
            DependencyGraph dependencyGraph = yarnTransformer.transform(yarnLockResult, yarnLockOptions.useProductionOnly(), externalDependencies);
            CodeLocation codeLocation = new CodeLocation(dependencyGraph);

            return YarnResult.success(packageJson.name, packageJson.version, codeLocation);
        } catch (MissingExternalIdException exception) {
            return YarnResult.failure(exception);
        }
    }
}
