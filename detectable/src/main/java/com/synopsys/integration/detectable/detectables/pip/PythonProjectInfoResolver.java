package com.synopsys.integration.detectable.detectables.pip;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class PythonProjectInfoResolver {
    private final DetectableExecutableRunner executableRunner;

    public PythonProjectInfoResolver(DetectableExecutableRunner executableRunner) {
        this.executableRunner = executableRunner;
    }

    @Nullable
    public String resolveProjectName(File directory, ExecutableTarget pythonExe, File setupFile, @Nullable String providedProjectName) throws ExecutableRunnerException {
        String projectName = providedProjectName;

        if (StringUtils.isBlank(projectName) && setupFile != null && setupFile.exists()) {
            List<String> arguments = Arrays.asList(setupFile.getAbsolutePath(), "--name");
            List<String> output = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pythonExe, arguments)).getStandardOutputAsList();
            projectName = output.get(output.size() - 1).replace('_', '-').trim();
        }

        return projectName;
    }

    @Nullable
    public String resolveProjectVersionName(File directory, ExecutableTarget pythonExe, File setupFile, String providedProjectVersionName) throws ExecutableRunnerException {
        String projectVersionName = providedProjectVersionName;

        if (StringUtils.isBlank(projectVersionName) && setupFile != null && setupFile.exists()) {
            List<String> arguments = Arrays.asList(setupFile.getAbsolutePath(), "--version");
            List<String> output = executableRunner.execute(ExecutableUtils.createFromTarget(directory, pythonExe, arguments)).getStandardOutputAsList();
            projectVersionName = output.get(output.size() - 1).trim();
        }

        return projectVersionName;
    }
}
