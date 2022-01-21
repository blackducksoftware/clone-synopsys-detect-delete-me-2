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

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.Pipeline;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.Pipelines;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.WorkspaceRuleChooser;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelCommandExecutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.BazelVariableSubstitutor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.HaskellCabalLibraryJsonProtoParser;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.ToolVersionLogger;
import com.synopsys.integration.exception.IntegrationException;

public class BazelExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectableExecutableRunner executableRunner;
    private final ExternalIdFactory externalIdFactory;
    private final BazelWorkspaceFileParser bazelWorkspaceFileParser;
    private final WorkspaceRuleChooser workspaceRuleChooser;
    private final ToolVersionLogger toolVersionLogger;
    private final HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser;

    public BazelExtractor(DetectableExecutableRunner executableRunner,
        ExternalIdFactory externalIdFactory,
        BazelWorkspaceFileParser bazelWorkspaceFileParser,
        WorkspaceRuleChooser workspaceRuleChooser,
        ToolVersionLogger toolVersionLogger,
        HaskellCabalLibraryJsonProtoParser haskellCabalLibraryJsonProtoParser) {
        this.executableRunner = executableRunner;
        this.externalIdFactory = externalIdFactory;
        this.workspaceRuleChooser = workspaceRuleChooser;
        this.bazelWorkspaceFileParser = bazelWorkspaceFileParser;
        this.toolVersionLogger = toolVersionLogger;
        this.haskellCabalLibraryJsonProtoParser = haskellCabalLibraryJsonProtoParser;
    }

    public Extraction extract(ExecutableTarget bazelExe, File workspaceDir, File workspaceFile, String bazelTarget,
        BazelProjectNameGenerator bazelProjectNameGenerator, Set<WorkspaceRule> providedDependencyRuleTypes,
        List<String> providedCqueryAdditionalOptions) {
        logger.debug("Bazel extraction:");
        try {
            toolVersionLogger.log(workspaceDir, bazelExe, "version");
            BazelCommandExecutor bazelCommandExecutor = new BazelCommandExecutor(executableRunner, workspaceDir, bazelExe);
            BazelVariableSubstitutor bazelVariableSubstitutor = new BazelVariableSubstitutor(bazelTarget, providedCqueryAdditionalOptions);
            Pipelines pipelines = new Pipelines(bazelCommandExecutor, bazelVariableSubstitutor, externalIdFactory, haskellCabalLibraryJsonProtoParser);
            Set<WorkspaceRule> workspaceRulesFromFile = parseWorkspaceRulesFromFile(workspaceFile);
            Set<WorkspaceRule> workspaceRulesToQuery = workspaceRuleChooser.choose(workspaceRulesFromFile, providedDependencyRuleTypes);
            List<Dependency> aggregatedDependencies = collectDependencies(pipelines, workspaceRulesToQuery);
            return buildResults(aggregatedDependencies, bazelProjectNameGenerator.generateFromBazelTarget(bazelTarget));
        } catch (Exception e) {
            String msg = String.format("Bazel processing exception: %s", e.getMessage());
            logger.debug(msg, e);
            return new Extraction.Builder().failure(msg).build();
        }
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

    private Extraction buildResults(List<Dependency> aggregatedDependencies, String projectName) {
        MutableDependencyGraph dependencyGraph = createDependencyGraph(aggregatedDependencies);
        CodeLocation codeLocation = new CodeLocation(dependencyGraph);
        List<CodeLocation> codeLocations = Collections.singletonList(codeLocation);
        Extraction.Builder builder = new Extraction.Builder()
            .success(codeLocations)
            .projectName(projectName);
        return builder.build();
    }

    @NotNull
    private List<Dependency> collectDependencies(Pipelines pipelines, Set<WorkspaceRule> workspaceRules) throws IntegrationException {
        List<Dependency> aggregatedDependencies = new ArrayList<>();
        // Make sure the order of processing deterministic
        List<WorkspaceRule> sortedWorkspaceRules = workspaceRules.stream()
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        for (WorkspaceRule workspaceRule : sortedWorkspaceRules) {
            logger.info("Running processing pipeline for rule {}", workspaceRule);
            Pipeline pipeline = pipelines.get(workspaceRule);
            List<Dependency> ruleDependencies = pipeline.run();
            logger.info(String.format("Number of dependencies discovered for rule %s: %d", workspaceRule, ruleDependencies.size()));
            logger.debug(String.format("Dependencies discovered for rule %s: %s", workspaceRule, ruleDependencies));
            aggregatedDependencies.addAll(ruleDependencies);
        }
        return aggregatedDependencies;
    }

    @NotNull
    private MutableDependencyGraph createDependencyGraph(List<Dependency> aggregatedDependencies) {
        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        for (Dependency dependency : aggregatedDependencies) {
            dependencyGraph.addChildToRoot(dependency);
        }
        return dependencyGraph;
    }
}
