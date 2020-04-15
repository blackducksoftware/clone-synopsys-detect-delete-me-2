package com.synopsys.integration.detectable.detectables.docker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectable.executable.Executable;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectable.file.FileFinder;

public class BashDockerRunner {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileFinder fileFinder;
    private final ExecutableRunner executableRunner;

    public BashDockerRunner(final FileFinder fileFinder, final ExecutableRunner executableRunner) {
        this.fileFinder = fileFinder;
        this.executableRunner = executableRunner;
    }

    private void importTars(final List<File> importTars, final File directory, final Map<String, String> environmentVariables, final File bashExe) {
        try {
            for (final File imageToImport : importTars) {
                final String arguments = "load -i \"" + imageToImport.getCanonicalPath() + "\"";
                final Executable dockerImportImageExecutable;
                if (bashExe == null) {
                    dockerImportImageExecutable = new Executable(directory, environmentVariables, "docker", Collections.singletonList(arguments));
                } else {
                    // The -c is a bash option, the following String is the command we want to run
                    List<String> bashArguments = Arrays.asList("-c", "docker", arguments);
                    dockerImportImageExecutable = new Executable(directory, environmentVariables, bashExe.toString(), bashArguments);
                }
                executableRunner.execute(dockerImportImageExecutable);
            }
        } catch (final Exception e) {
            logger.debug("Exception encountered when resolving paths for docker air gap, running in online mode instead");
            logger.debug(e.getMessage());
        }
    }

    public void executeDocker(final File outputDirectory, final String imageArgument, final File javaExe, final File bashExe,
        final DockerInspectorInfo dockerInspectorInfo, DockerProperties dockerProperties)
        throws IOException, ExecutableRunnerException {

        final File dockerPropertiesFile = new File(outputDirectory, "application.properties");
        dockerProperties.populatePropertiesFile(dockerPropertiesFile, outputDirectory);
        final Map<String, String> environmentVariables = new HashMap<>(0);
        final List<String> dockerArguments = new ArrayList<>();
        dockerArguments.add("-jar");
        dockerArguments.add(dockerInspectorInfo.getDockerInspectorJar().getAbsolutePath());
        dockerArguments.add("--spring.config.location=file:" + dockerPropertiesFile.getCanonicalPath());
        dockerArguments.add(imageArgument);
        dockerArguments.add("--working.dir.path=" + outputDirectory.getAbsolutePath());
        if (dockerInspectorInfo.hasAirGapImageFiles()) {
            importTars(dockerInspectorInfo.getAirGapInspectorImageTarFiles(), outputDirectory, environmentVariables, bashExe);
        }
        final Executable dockerExecutable = new Executable(outputDirectory, environmentVariables, javaExe.getAbsolutePath(), dockerArguments);
        executableRunner.execute(dockerExecutable);
    }
}
