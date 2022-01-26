package com.synopsys.integration.detectable.detectables.bazel.pipeline.step;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.ExecutableTarget;

public class IntermediateStepReplaceInEach implements IntermediateStep {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    final String targetPattern;
    final String replacementString;

    public IntermediateStepReplaceInEach(String targetPattern, String replacementString) {
        this.targetPattern = targetPattern;
        this.replacementString = replacementString;
    }

    @Override
    public List<String> process(File workspaceDir, ExecutableTarget bazelExe, List<String> input) {
        List<String> results = new ArrayList<>();
        logger.trace(String.format("Replace target pattern: %s; replacement string: %s", targetPattern, replacementString));
        for (String inputItem : input) {
            String modifiedInputItem = inputItem.replaceAll(targetPattern, replacementString);
            logger.trace(String.format("Edit changed %s to %s", inputItem, modifiedInputItem));
            results.add(modifiedInputItem);
        }
        return results;
    }
}
