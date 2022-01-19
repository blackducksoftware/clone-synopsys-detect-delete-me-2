package com.synopsys.integration.detectable.detectables.go.gomod;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;

public class GoModCommandExecutor {
    private static final String FAILURE_MSG_QUERYING_FOR_THE_GO_MOD_GRAPH = "Querying for the go mod graph failed:";
    private static final String FAILURE_MSG_QUERYING_GO_FOR_THE_LIST_OF_MODULES = "Querying go for the list of modules failed: ";
    private static final String FAILURE_MSG_QUERYING_FOR_THE_VERSION = "Querying for the version failed: ";
    private static final String FAILURE_MSG_QUERYING_FOR_GO_MOD_WHY = "Querying for the go modules compiled into the binary failed:";
    private static final Pattern GENERATE_GO_LIST_JSON_OUTPUT_PATTERN = Pattern.compile("\\d+\\.[\\d.]+"); // TODO: Provide example. This looks like it's used for version matching contrary to the name.
    private static final String JSON_OUTPUT_FLAG = "-json";
    private static final String MODULE_OUTPUT_FLAG = "-m";

    private final DetectableExecutableRunner executableRunner;

    public GoModCommandExecutor(DetectableExecutableRunner executableRunner) {
        this.executableRunner = executableRunner;
    }

    // Excludes the "all" argument to return the root project modules
    List<String> generateGoListOutput(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "list", MODULE_OUTPUT_FLAG, JSON_OUTPUT_FLAG))
            .getStandardOutputAsList();
    }

    // TODO: Utilize the fields "Main": true, and "Indirect": true, fields from the JSON output to avoid running go list twice. Before switching to json output we needed to run twice. JM-01/2022
    List<String> generateGoListJsonOutput(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        // TODO: Move the Go version checking to it's own method. JM-01/2022
        List<String> goVersionOutput = executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "version"), FAILURE_MSG_QUERYING_FOR_THE_VERSION)
            .getStandardOutputAsList();
        Matcher matcher = GENERATE_GO_LIST_JSON_OUTPUT_PATTERN.matcher(goVersionOutput.get(0));
        if (matcher.find()) {
            String version = matcher.group();
            String[] parts = version.split("\\.");
            if (Integer.parseInt(parts[0]) > 1 || Integer.parseInt(parts[1]) >= 14) {
                return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "list", "-mod=readonly", MODULE_OUTPUT_FLAG, JSON_OUTPUT_FLAG, "all"), FAILURE_MSG_QUERYING_GO_FOR_THE_LIST_OF_MODULES)
                    .getStandardOutputAsList();
            } else {
                return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "list", MODULE_OUTPUT_FLAG, JSON_OUTPUT_FLAG, "all"), FAILURE_MSG_QUERYING_GO_FOR_THE_LIST_OF_MODULES)
                    .getStandardOutputAsList();
            }
        }
        return new ArrayList<>();
    }

    List<String> generateGoModGraphOutput(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "mod", "graph"), FAILURE_MSG_QUERYING_FOR_THE_GO_MOD_GRAPH)
            .getStandardOutputAsList();
    }

    List<String> generateGoModWhyOutput(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        // executing this command helps produce more accurate results. Parse the output to create a module exclusion list.
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, "mod", "why", "-m", "all"), FAILURE_MSG_QUERYING_FOR_GO_MOD_WHY)
            .getStandardOutputAsList();
    }

}
