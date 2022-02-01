package com.synopsys.integration.detectable.detectables.bazel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.Pipelines;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.WorkspaceRuleChooser;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelCommandExecutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelVariableSubstitutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.HaskellCabalLibraryJsonProtoParser;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.ToolVersionLogger;

public class BazelExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectableExecutableRunner executableRunner;
    private final ExternalIdFactory externalIdFactory;
    private final BazelWorkspaceFileParser bazelWorkspaceFileParser;
    private final WorkspaceRuleChooser workspaceRuleChooser;
    private final ToolVersionLogger toolVersionLogger;
    private final HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser;
    private final String bazelTarget;
    private final Set<WorkspaceRule> providedDependencyRuleTypes;
    private final BazelVariableSubstitutor bazelVariableSubstitutor;
    private final DependencyTransformer dependencyTransformer;
    private final BazelProjectNameGenerator bazelProjectNameGenerator;

    public BazelExtractor(DetectableExecutableRunner executableRunner,
        ExternalIdFactory externalIdFactory,
        BazelWorkspaceFileParser bazelWorkspaceFileParser,
        WorkspaceRuleChooser workspaceRuleChooser,
        ToolVersionLogger toolVersionLogger,
        HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser,
        String bazelTarget,
        Set<WorkspaceRule> providedDependencyRuleTypes,
        BazelVariableSubstitutor bazelVariableSubstitutor,
        DependencyTransformer dependencyTransformer,
        BazelProjectNameGenerator bazelProjectNameGenerator) {
        this.executableRunner = executableRunner;
        this.externalIdFactory = externalIdFactory;
        this.workspaceRuleChooser = workspaceRuleChooser;
        this.bazelWorkspaceFileParser = bazelWorkspaceFileParser;
        this.toolVersionLogger = toolVersionLogger;
        this.haskellCabalLibraryJsonProtoParser = haskellCabalLibraryJsonProtoParser;
        this.bazelTarget = bazelTarget;
        this.providedDependencyRuleTypes = providedDependencyRuleTypes;
        this.bazelVariableSubstitutor = bazelVariableSubstitutor;
        this.dependencyTransformer = dependencyTransformer;
        this.bazelProjectNameGenerator = bazelProjectNameGenerator;
    }

    public Extraction extract(ExecutableTarget bazelExe, File workspaceDir, File workspaceFile) throws DetectableException, ExecutableFailedException {
        toolVersionLogger.log(workspaceDir, bazelExe, "version");
        BazelCommandExecutor bazelCommandExecutor = new BazelCommandExecutor(executableRunner, workspaceDir, bazelExe);
        Pipelines pipelines = new Pipelines(bazelCommandExecutor, bazelVariableSubstitutor, externalIdFactory, haskellCabalLibraryJsonProtoParser);
        Set<WorkspaceRule> workspaceRulesFromFile = parseWorkspaceRulesFromFile(workspaceFile);
        Set<WorkspaceRule> workspaceRulesToQuery = workspaceRuleChooser.choose(workspaceRulesFromFile, providedDependencyRuleTypes);
        List<Dependency> aggregatedDependencies = collectDependencies(pipelines, workspaceRulesToQuery);
        CodeLocation codeLocation = dependencyTransformer.createCodeLocation(aggregatedDependencies);
        return buildResults(codeLocation, bazelProjectNameGenerator.generateFromBazelTarget(bazelTarget));
    }

    private Set<WorkspaceRule> parseWorkspaceRulesFromFile(final File workspaceFile) {
        List<String> workspaceFileLines;
        try {
            workspaceFileLines = FileUtils.readLines(workspaceFile, StandardCharsets.UTF_8);
            return bazelWorkspaceFileParser.parseWorkspaceRuleTypes(workspaceFileLines);
        } catch (IOException e) {
            return new HashSet<>(0);
        }
    }

    private Extraction buildResults(CodeLocation codeLocation, String projectName) {
        List<CodeLocation> codeLocations = Collections.singletonList(codeLocation);
        Extraction.Builder builder = new Extraction.Builder()
            .success(codeLocations)
            .projectName(projectName);
        return builder.build();
    }

    @NotNull
    private List<Dependency> collectDependencies(Pipelines pipelines, Set<WorkspaceRule> workspaceRules) throws DetectableException, ExecutableFailedException {
        List<Dependency> aggregatedDependencies = new ArrayList<>();
        // Make sure the order of processing deterministic
        List<WorkspaceRule> sortedWorkspaceRules = workspaceRules.stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        for (WorkspaceRule workspaceRule : sortedWorkspaceRules) {
            logger.info("Running processing pipeline for rule {}", workspaceRule);
            List<Dependency> ruleDependencies = pipelines.get(workspaceRule).run();
            logger.info("Number of dependencies discovered for rule {}: {}}", workspaceRule, ruleDependencies.size());
            logger.debug("Dependencies discovered for rule {}: {}}", workspaceRule, ruleDependencies);
            aggregatedDependencies.addAll(ruleDependencies);
        }
        return aggregatedDependencies;
    }
}
