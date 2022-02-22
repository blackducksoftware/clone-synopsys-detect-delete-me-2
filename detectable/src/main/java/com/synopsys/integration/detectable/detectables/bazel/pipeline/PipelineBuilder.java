package com.synopsys.integration.detectable.detectables.bazel.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelCommandExecutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelVariableSubstitutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.FinalStep;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.FinalStepTransformColonSeparatedGavsToMaven;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.FinalStepTransformJsonProtoHaskellCabalLibrariesToHackage;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.HaskellCabalLibraryJsonProtoParser;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStep;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStepExecuteBazelOnEachLine;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStepParseFilterLines;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStepParseReplaceInEachLine;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStepParseSplitEach;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.IntermediateStepParseValuesFromXml;

public class PipelineBuilder {
    private final ExternalIdFactory externalIdFactory;
    private final BazelCommandExecutor bazelCommandExecutor;
    private final BazelVariableSubstitutor bazelVariableSubstitutor;
    private final HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser;
    private final List<IntermediateStep> intermediateSteps = new ArrayList<>();
    private FinalStep finalStep;

    public PipelineBuilder(ExternalIdFactory externalIdFactory, BazelCommandExecutor bazelCommandExecutor, BazelVariableSubstitutor bazelVariableSubstitutor, HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser) {
        this.externalIdFactory = externalIdFactory;
        this.bazelCommandExecutor = bazelCommandExecutor;
        this.bazelVariableSubstitutor = bazelVariableSubstitutor;
        this.haskellCabalLibraryJsonProtoParser = haskellCabalLibraryJsonProtoParser;
    }

    public PipelineBuilder addIntermediateStep(IntermediateStep intermediateStep) {
        intermediateSteps.add(intermediateStep);
        return this;
    }

    public PipelineBuilder setFinalStep(FinalStep finalStep) {
        this.finalStep = finalStep;
        return this;
    }

    public Pipeline build() {
        if (finalStep == null) {
            throw new UnsupportedOperationException("A final step is required");
        }
        return new Pipeline(intermediateSteps, finalStep);
    }

    // Step creation methods
    public PipelineBuilder parseReplaceInEachLine(String from, String to) {
        return addIntermediateStep(new IntermediateStepParseReplaceInEachLine(from, to));
    }

    public PipelineBuilder parseSplitEachLine(String splitOn) {
        return addIntermediateStep(new IntermediateStepParseSplitEach(splitOn));
    }

    public PipelineBuilder executeBazelOnEachLine(List<String> bazelArguments, boolean inputIsExpected) {
        return addIntermediateStep(new IntermediateStepExecuteBazelOnEachLine(bazelCommandExecutor, bazelVariableSubstitutor, bazelArguments, inputIsExpected));
    }

    public PipelineBuilder parseValuesFromXml(String xPathToElement, String targetAttributeName) {
        return addIntermediateStep(new IntermediateStepParseValuesFromXml(xPathToElement, targetAttributeName));
    }

    public PipelineBuilder parseFilterLines(String regex) {
        return addIntermediateStep(new IntermediateStepParseFilterLines(regex));
    }

    // A transform step must be the final step in the pipeline
    public PipelineBuilder transformToMavenDependencies() {
        return setFinalStep(new FinalStepTransformColonSeparatedGavsToMaven(externalIdFactory));
    }

    public PipelineBuilder transformToHackageDependencies() {
        return setFinalStep(new FinalStepTransformJsonProtoHaskellCabalLibrariesToHackage(haskellCabalLibraryJsonProtoParser, externalIdFactory));
    }
}
