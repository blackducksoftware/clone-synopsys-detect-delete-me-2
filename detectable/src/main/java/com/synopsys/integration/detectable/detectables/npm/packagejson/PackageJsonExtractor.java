package com.synopsys.integration.detectable.detectables.npm.packagejson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.npm.packagejson.model.PackageJson;
import com.synopsys.integration.detectable.extraction.Extraction;

public class PackageJsonExtractor {
    private final Gson gson;
    private final ExternalIdFactory externalIdFactory;

    public PackageJsonExtractor(Gson gson, ExternalIdFactory externalIdFactory) {
        this.gson = gson;
        this.externalIdFactory = externalIdFactory;
    }

    public Extraction extract(InputStream packageJsonInputStream, boolean includeDevDependencies, boolean includePeerDependencies) {
        Reader packageJsonReader = new InputStreamReader(packageJsonInputStream);
        PackageJson packageJson = gson.fromJson(packageJsonReader, PackageJson.class);

        return extract(packageJson, includeDevDependencies, includePeerDependencies);
    }

    public Extraction extract(PackageJson packageJson, boolean includeDevDependencies, boolean includePeerDependencies) {
        List<Dependency> dependencies = transformDependencies(packageJson.dependencies);
        if (includeDevDependencies) {
            dependencies.addAll(transformDependencies(packageJson.devDependencies));
        }
        if (includePeerDependencies) {
            dependencies.addAll(transformDependencies(packageJson.peerDependencies));
        }

        MutableMapDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        dependencyGraph.addChildrenToRoot(dependencies);

        CodeLocation codeLocation = new CodeLocation(dependencyGraph);

        String projectName = StringUtils.stripToNull(packageJson.name);
        String projectVersion = StringUtils.stripToNull(packageJson.version);

        return new Extraction.Builder()
            .success(codeLocation)
            .projectName(projectName)
            .projectVersion(projectVersion)
            .build();
    }

    private List<Dependency> transformDependencies(Map<String, String> dependencies) {
        return dependencies.entrySet().stream()
            .map(this::entryToDependency)
            .collect(Collectors.toList());
    }

    private Dependency entryToDependency(Map.Entry<String, String> dependencyEntry) {
        ExternalId externalId = externalIdFactory.createNameVersionExternalId(Forge.RUBYGEMS, dependencyEntry.getKey(), dependencyEntry.getValue());
        return new Dependency(externalId);
    }

}
