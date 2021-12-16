package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.library.Libraries;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.library.PackageLibrary;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.library.ProjectLibrary;

public class NugetLockFileLibraryParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Libraries parseLibraries(JsonObject lockfile) {
        Libraries libraries = new Libraries();
        JsonObject librariesJson = lockfile.getAsJsonObject("libraries");
        for (final Map.Entry<String, JsonElement> libraryPair : librariesJson.entrySet()) {
            String identifier = libraryPair.getKey();
            JsonObject dependencyPayload = libraryPair.getValue().getAsJsonObject();
            String type = dependencyPayload.get("type").getAsString();
            if ("package".equals(type)) {
                libraries.packageLibraries.add(new PackageLibrary(identifier));
            } else if ("project".equals(type)) {
                libraries.projectLibraries.add(parseProjectLibrary(identifier, dependencyPayload));
            } else {
                logger.warn("Unknown target dependency type: " + type);
            }
        }
        return libraries;
    }

    private ProjectLibrary parseProjectLibrary(String identifier, JsonObject dependencyPayload) {
        return new ProjectLibrary(identifier, dependencyPayload.get("path").getAsString(), dependencyPayload.get("msbuildProject").getAsString());
    }
}
