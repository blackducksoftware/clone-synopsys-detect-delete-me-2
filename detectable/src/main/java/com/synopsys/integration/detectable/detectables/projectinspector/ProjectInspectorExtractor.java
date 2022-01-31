package com.synopsys.integration.detectable.detectables.projectinspector;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.extraction.Extraction;

public class ProjectInspectorExtractor {
    private final DetectableExecutableRunner executableRunner;
    private final ProjectInspectorParser projectInspectorParser;

    public ProjectInspectorExtractor(DetectableExecutableRunner executableRunner, ProjectInspectorParser projectInspectorParser) {
        this.executableRunner = executableRunner;
        this.projectInspectorParser = projectInspectorParser;
    }

    public Extraction extract(ProjectInspectorOptions projectInspectorOptions, List<String> extra, File targetDirectory, File outputDirectory, ExecutableTarget inspector) throws ExecutableFailedException {
        File outputFile = new File(outputDirectory, "inspection.json");

        List<String> arguments = new LinkedList<>();
        arguments.add("inspect");
        arguments.add("--dir");
        arguments.add(targetDirectory.toString());
        arguments.add("--output-file");
        arguments.add(outputFile.toString());
        arguments.addAll(extra);

        Optional.ofNullable(projectInspectorOptions.getAdditionalArguments())
            .map(arg -> arg.split(" "))
            .ifPresent(additionalArguments -> arguments.addAll(Arrays.asList(additionalArguments)));

        executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(targetDirectory, inspector, arguments));

        try {
            String outputContents = FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8);
            List<CodeLocation> codeLocations = projectInspectorParser.parse(outputContents);
            return new Extraction.Builder().success(codeLocations).build();
        } catch (IOException e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
