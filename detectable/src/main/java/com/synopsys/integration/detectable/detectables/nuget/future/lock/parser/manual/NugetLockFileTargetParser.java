package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target.PackageDependency;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target.ProjectDependency;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target.Target;

public class NugetLockFileTargetParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final NugetLockFileDependenciesParser dependenciesParser;

    public NugetLockFileTargetParser(NugetLockFileDependenciesParser dependenciesParser) {this.dependenciesParser = dependenciesParser;}

    public List<Target> parseTargets(JsonObject lockfile) {
        List<Target> targets = new ArrayList<>();
        JsonObject targetsJson = lockfile.getAsJsonObject("targets");
        for (final Map.Entry<String, JsonElement> targetJsonPair : targetsJson.entrySet()) {
            JsonObject targetPayload = targetJsonPair.getValue().getAsJsonObject();
            Target target = new Target();
            target.identifier = targetJsonPair.getKey();
            parseTargetPayload(target, targetPayload);
            targets.add(target);
        }
        return targets;
    }

    private void parseTargetPayload(Target target, JsonObject targetPayload) {
        for (final Map.Entry<String, JsonElement> targetDependencyPair : targetPayload.entrySet()) {
            String identifier = targetDependencyPair.getKey();
            JsonObject dependencyPayload = targetDependencyPair.getValue().getAsJsonObject();
            String type = dependencyPayload.get("type").getAsString();
            if ("package".equals(type)) {
                target.packageDependencies.add(new PackageDependency(identifier, dependenciesParser.parseDependencies(dependencyPayload)));
            } else if ("project".equals(type)) {
                target.projectDependencies.add(new ProjectDependency(identifier, dependenciesParser.parseDependencies(dependencyPayload)));
            } else {
                logger.warn("Unknown target dependency type: " + type);
            }
        }
    }

}
