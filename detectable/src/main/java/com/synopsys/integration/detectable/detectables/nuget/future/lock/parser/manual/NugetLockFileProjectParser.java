package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework.Framework;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework.ProjectReference;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.project.Project;

public class NugetLockFileProjectParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NugetLockFileDependenciesParser dependenciesParser;

    public NugetLockFileProjectParser(NugetLockFileDependenciesParser dependenciesParser) {this.dependenciesParser = dependenciesParser;}

    public Project parseProject(JsonObject lockfile) {
        Project project = new Project();
        GsonUtil.ifObjectPresent(lockfile, "project", projectJson -> {
            project.version = GsonUtil.getAsStringOrNull(lockfile, "version");
            GsonUtil.ifObjectPresent(projectJson, "restore", restoreJson -> {
                parseProjectRestore(project, restoreJson);
            });
            project.dependencies = dependenciesParser.parseDependencies(projectJson);
        });
        return project;
    }

    private void parseProjectRestore(Project project, JsonObject restoreJson) {
        project.name = GsonUtil.getAsStringOrNull(restoreJson, "projectName");
        GsonUtil.iterateMember(restoreJson, "frameworks", (frameworkId, frameworkValue) -> {
            Framework framework = new Framework(frameworkId);
            GsonUtil.iterateMember(frameworkValue.getAsJsonObject(), "projectReferences", (projectReferenceId, projectReferenceData) -> {
                ProjectReference projectReference = parseProjectReference(projectReferenceId, projectReferenceData);
                framework.projectReferences.add(projectReference);
            });
            project.frameworks.add(framework);
        });
    }

    private ProjectReference parseProjectReference(String projectReferenceId, JsonElement projectReferenceData) {
        String projectReferencePath = GsonUtil.getAsStringOrNull(projectReferenceData.getAsJsonObject(), "projectPath");
        return new ProjectReference(projectReferenceId, projectReferencePath);
    }
}
