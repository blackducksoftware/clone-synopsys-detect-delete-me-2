/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.pipenv;

import java.io.File;
import java.util.Arrays;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.pip.PythonProjectInfoResolver;
import com.synopsys.integration.detectable.detectables.pipenv.model.PipFreeze;
import com.synopsys.integration.detectable.detectables.pipenv.model.PipenvGraph;
import com.synopsys.integration.detectable.detectables.pipenv.parser.PipEnvJsonGraphParser;
import com.synopsys.integration.detectable.detectables.pipenv.parser.PipenvFreezeParser;
import com.synopsys.integration.detectable.detectables.pipenv.parser.PipenvTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.executable.ExecutableOutput;

public class PipenvExtractor {
    private final DetectableExecutableRunner executableRunner;
    private final PipenvTransformer pipenvTransformer;
    private final PipenvFreezeParser pipenvFreezeParser;
    private final PipEnvJsonGraphParser pipEnvJsonGraphParser;
    private final PythonProjectInfoResolver pythonProjectInfoResolver;

    public PipenvExtractor(DetectableExecutableRunner executableRunner, PipenvTransformer pipenvTransformer, PipenvFreezeParser pipenvFreezeParser, PipEnvJsonGraphParser pipEnvJsonGraphParser,
        PythonProjectInfoResolver pythonProjectInfoResolver) {
        this.executableRunner = executableRunner;
        this.pipenvTransformer = pipenvTransformer;
        this.pipenvFreezeParser = pipenvFreezeParser;
        this.pipEnvJsonGraphParser = pipEnvJsonGraphParser;
        this.pythonProjectInfoResolver = pythonProjectInfoResolver;
    }

    public Extraction extract(File directory, ExecutableTarget pythonExe, ExecutableTarget pipenvExe, File setupFile, String providedProjectName, String providedProjectVersionName, boolean includeOnlyProjectTree) {
        Extraction extraction;

        try {
            String projectName = pythonProjectInfoResolver.resolveProjectName(directory, pythonExe, setupFile, providedProjectName);
            String projectVersionName = pythonProjectInfoResolver.resolveProjectVersionName(directory, pythonExe, setupFile, providedProjectVersionName);

            ExecutableOutput pipFreezeOutput = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pipenvExe, Arrays.asList("run", "pip", "freeze")));
            ExecutableOutput graphOutput = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pipenvExe, Arrays.asList("graph", "--bare", "--json-tree")));

            PipFreeze pipFreeze = pipenvFreezeParser.parse(pipFreezeOutput.getStandardOutputAsList());
            PipenvGraph pipenvGraph = pipEnvJsonGraphParser.parse(graphOutput.getStandardOutput());
            CodeLocation codeLocation = pipenvTransformer.transform(projectName, projectVersionName, pipFreeze, pipenvGraph, includeOnlyProjectTree);

            return new Extraction.Builder()
                .projectName(projectName)
                .projectVersion(projectVersionName)
                .success(codeLocation)
                .build();
        } catch (Exception e) {
            extraction = new Extraction.Builder().exception(e).build();
        }

        return extraction;
    }

}
