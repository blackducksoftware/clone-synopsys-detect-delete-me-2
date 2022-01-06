package com.synopsys.integration.detectable.detectables.bitbake;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeRecipe;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeRecipesParser;
import com.synopsys.integration.detectable.util.ToolVersionLogger;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.ExecutableOutput;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class BitbakeSession {
    private static final String TASK_DEPENDS_FILE_NAME = "task-depends.dot";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileFinder fileFinder;
    private final DetectableExecutableRunner executableRunner;
    private final BitbakeRecipesParser bitbakeRecipesParser;
    private final File workingDirectory;
    private final File buildEnvScript;
    private final List<String> sourceArguments;
    private final ExecutableTarget bashExecutable;
    private final ToolVersionLogger toolVersionLogger;

    public BitbakeSession(FileFinder fileFinder, DetectableExecutableRunner executableRunner, BitbakeRecipesParser bitbakeRecipesParser,
        File workingDirectory, File buildEnvScript, List<String> sourceArguments,
        ExecutableTarget bashExecutable, ToolVersionLogger toolVersionLogger) {
        this.fileFinder = fileFinder;
        this.executableRunner = executableRunner;
        this.bitbakeRecipesParser = bitbakeRecipesParser;
        this.workingDirectory = workingDirectory;
        this.buildEnvScript = buildEnvScript;
        this.sourceArguments = sourceArguments;
        this.bashExecutable = bashExecutable;
        this.toolVersionLogger = toolVersionLogger;
    }

    public Optional<File> executeBitbakeForDependencies(File sourceDirectory, String packageName, boolean followSymLinks, Integer searchDepth)
        throws ExecutableRunnerException, IOException {

        String bitbakeCommand = "bitbake -g " + packageName;
        ExecutableOutput executableOutput = runBitbake(bitbakeCommand);
        int returnCode = executableOutput.getReturnCode();

        if (returnCode != 0) {
            logger.error(String.format("Executing command '%s' returned a non-zero exit code %s", bitbakeCommand, returnCode));
            return Optional.empty();
        }

        return findTaskDependsFile(sourceDirectory, workingDirectory, followSymLinks, searchDepth);

    }

    public void logBitbakeVersion() {
        toolVersionLogger.log(() -> runBitbake("bitbake --version"));
    }

    public File determineBuildDir(File sourceDir) {
        // TODO refactor; detect failure once
        File derivedBuildDir;
        try {
            ExecutableOutput output = runBitbake("pwd");
            String pwdOutput = output.getStandardOutput();
            derivedBuildDir = new File(pwdOutput.trim());
        } catch (Exception e) {
            derivedBuildDir = new File(sourceDir, "build");
            logger.warn(String.format("Unable to determine build directory location (%s); assuming %s.", e.getMessage(), derivedBuildDir.getAbsolutePath()));
        }
        if (derivedBuildDir.isDirectory()) {
            logger.debug("Derived build dir: {}", derivedBuildDir.getAbsolutePath());
        } else {
            File fallbackBuildDir = new File(sourceDir, "build");
            logger.warn("Derived build dir {} is not a directory; using {} for build dir", derivedBuildDir.getAbsolutePath(), fallbackBuildDir);
            derivedBuildDir = fallbackBuildDir;
        }
        return derivedBuildDir;
    }

    private Optional<File> findTaskDependsFile(File sourceDirectory, File outputDirectory, boolean followSymLinks, Integer searchDepth) {
        File file = fileFinder.findFile(outputDirectory, TASK_DEPENDS_FILE_NAME, followSymLinks, searchDepth);
        if (file == null) {
            file = fileFinder.findFile(sourceDirectory, TASK_DEPENDS_FILE_NAME, followSymLinks, searchDepth);
        }

        return Optional.ofNullable(file);

    }

    public List<BitbakeRecipe> executeBitbakeForRecipeLayerCatalog() throws ExecutableRunnerException, IOException, IntegrationException {
        final String bitbakeCommand = "bitbake-layers show-recipes";
        ExecutableOutput executableOutput = runBitbake(bitbakeCommand);
        if (executableOutput.getReturnCode() == 0) {
            return bitbakeRecipesParser.parseShowRecipes(executableOutput.getStandardOutputAsList());
        } else {
            throw new IntegrationException("Running command '%s' returned a non-zero exit code. Failed to extract bitbake recipe mapping.");
        }
    }

    private ExecutableOutput runBitbake(String bitbakeCommand) throws ExecutableRunnerException, IOException {
        StringBuilder sourceCommand = new StringBuilder("source " + buildEnvScript.getCanonicalPath());
        for (String sourceArgument : sourceArguments) {
            sourceCommand.append(" ");
            sourceCommand.append(sourceArgument);
        }
        return executableRunner.execute(ExecutableUtils.createFromTarget(workingDirectory, bashExecutable, "-c", sourceCommand.toString() + "; " + bitbakeCommand));
    }
}
