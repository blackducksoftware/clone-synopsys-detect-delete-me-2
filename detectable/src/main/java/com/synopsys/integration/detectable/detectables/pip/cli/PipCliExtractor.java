/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.pip.cli;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.pip.PythonProjectInfoResolver;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.util.NameVersion;

public class PipCliExtractor {
    private final DetectableExecutableRunner executableRunner;
    private final PipCliTransformer pipCliTransformer;
    private final PythonProjectInfoResolver pythonProjectInfoResolver;
    private final Gson gson;

    private final Type pipListOutputType = new TypeToken<List<NameVersion>>() {}.getType();

    public PipCliExtractor(DetectableExecutableRunner executableRunner, PipCliTransformer pipCliTransformer, PythonProjectInfoResolver pythonProjectInfoResolver, Gson gson) {
        this.executableRunner = executableRunner;
        this.pipCliTransformer = pipCliTransformer;
        this.pythonProjectInfoResolver = pythonProjectInfoResolver;
        this.gson = gson;
    }

    public Extraction extract(File directory, ExecutableTarget pythonExe, ExecutableTarget pipExe, File setupFile, @Nullable String providedProjectName, @Nullable String providedProjectVersionName) {
        Extraction extraction;

        try {
            String projectName = pythonProjectInfoResolver.resolveProjectName(directory, pythonExe, setupFile, providedProjectName);
            String projectVersionName = pythonProjectInfoResolver.resolveProjectVersionName(directory, pythonExe, setupFile, providedProjectVersionName);

            ExecutableOutput pipListOutput = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pipExe, Arrays.asList("--disable-pip-version-check", "list", "--format", "json")));
            List<NameVersion> dependencies = gson.fromJson(pipListOutput.getStandardOutput(), pipListOutputType);

            CodeLocation codeLocation = pipCliTransformer.createCodeLocation(dependencies, projectName, projectVersionName);

            return new Extraction.Builder()
                .success(codeLocation)
                .projectName(projectName)
                .projectVersion(projectVersionName)
                .build();
        } catch (Exception e) {
            extraction = new Extraction.Builder().exception(e).build();
        }

        return extraction;
    }

}
