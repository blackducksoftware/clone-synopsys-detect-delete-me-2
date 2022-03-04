package com.synopsys.integration.detectable.detectables.bazel;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BazelDetectableOptions {
    private final String targetName;
    private final Set<WorkspaceRule> workspaceRulesFromDeprecatedProperty;
    private final Set<WorkspaceRule> workspaceRulesFromProperty;
    private final List<String> bazelCqueryAdditionalOptions;

    public BazelDetectableOptions(
        String targetName,
        Set<WorkspaceRule> workspaceRulesFromDeprecatedProperty,
        Set<WorkspaceRule> workspaceRulesFromProperty,
        List<String> bazelCqueryAdditionalOptions
    ) {
        this.targetName = targetName;
        this.workspaceRulesFromDeprecatedProperty = workspaceRulesFromDeprecatedProperty;
        this.workspaceRulesFromProperty = workspaceRulesFromProperty;
        this.bazelCqueryAdditionalOptions = bazelCqueryAdditionalOptions;
    }

    public Optional<String> getTargetName() {
        return Optional.ofNullable(targetName);
    }

    public List<String> getBazelCqueryAdditionalOptions() {
        return bazelCqueryAdditionalOptions;
    }

    public Set<WorkspaceRule> getWorkspaceRulesFromDeprecatedProperty() {
        return workspaceRulesFromDeprecatedProperty;
    }

    public Set<WorkspaceRule> getWorkspaceRulesFromProperty() {
        return workspaceRulesFromProperty;
    }
}
