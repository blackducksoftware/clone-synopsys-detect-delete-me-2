package com.synopsys.integration.detectable.detectables.bitbake.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.executable.ExecutableRunnerException;

public class TaskDependsDotFile {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public File generate(BitbakeSession bitbakeSession, File sourceDirectory, String targetImageName, boolean followSymLinks, int searchDepth) throws IOException, ExecutableRunnerException, IntegrationException {
        File taskDependsFile = bitbakeSession.executeBitbakeForDependencies(sourceDirectory, targetImageName, followSymLinks, searchDepth)
            .orElseThrow(() -> new IntegrationException("Failed to find file \"task-depends.dot\"."));

        logger.trace(FileUtils.readFileToString(taskDependsFile, Charset.defaultCharset()));
        return taskDependsFile;
    }
}
