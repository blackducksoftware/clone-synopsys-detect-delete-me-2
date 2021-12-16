package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.util.NameVersion;

public class NugetLockFileDependenciesParser {
    public List<NameVersion> parseDependencies(JsonObject dependencyJson) {
        List<NameVersion> dependencies = new ArrayList<>();
        if (dependencyJson.has("dependencies")) {
            JsonObject dependenciesJson = dependencyJson.getAsJsonObject("dependencies");
            for (final Map.Entry<String, JsonElement> dependencyPair : dependenciesJson.entrySet()) {
                dependencies.add(new NameVersion(dependencyPair.getKey(), dependencyPair.getValue().getAsString()));
            }
        }

        return dependencies;
    }
}
