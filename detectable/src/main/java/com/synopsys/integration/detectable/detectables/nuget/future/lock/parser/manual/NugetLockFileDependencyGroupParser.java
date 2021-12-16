package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.dependencygroup.DependencyGroup;

public class NugetLockFileDependencyGroupParser {
    public List<DependencyGroup> parseDependencyGroups(JsonObject dependencyJson) {
        List<DependencyGroup> dependencyGroups = new ArrayList<>();

        if (dependencyJson.has("projectFileDependencyGroups")) {
            JsonObject dependenciesJson = dependencyJson.getAsJsonObject("projectFileDependencyGroups");
            for (final Map.Entry<String, JsonElement> dependencyGroupByFramework : dependenciesJson.entrySet()) {
                String frameworkId = dependencyGroupByFramework.getKey();
                List<String> dependencies = new ArrayList<>();
                dependencyGroupByFramework.getValue().getAsJsonArray().forEach(element -> dependencies.add(element.getAsString()));
                dependencyGroups.add(new DependencyGroup(frameworkId, dependencies));
            }
        }

        return dependencyGroups;
    }
}
