package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework.Framework;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework.ProjectReference;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.project.Project;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual.NugetLockFileProjectParser;
import com.synopsys.integration.detectable.util.GsonUtil;

public class ProjectParseTests {
    @Test
    void parsesProjectNameFromRestore() {
        JsonObject lockfile = GsonUtil.toJson("{ \"project\": {\n"
            + "    \"restore\": {\n"
            + "      \"projectName\": \"SmartHive.LevelMapApp.UWP\"\n"
            + "    }\n"
            + "}}");

        Project project = new NugetLockFileProjectParser(dependenciesParser).parseProject(lockfile);

        Assertions.assertEquals("SmartHive.LevelMapApp.UWP", project.name);
    }

    @Test
    void parsesVersionOnProject() {
        JsonObject lockfile = GsonUtil.toJson("{   \"project\": {\n"
            + "    \"version\": \"1.0.0\" } }");

        Project project = new NugetLockFileProjectParser(dependenciesParser).parseProject(lockfile);

        Assertions.assertEquals("1.0.0", project.version);
    }

    @Test
    void parsesProjectDependencies() {
        JsonObject lockfile = GsonUtil.toJson("{   \"project\": {\n"
            + "    \"dependencies\": {\n"
            + "      \"Xamarin.Forms\": \"[2.3.4.270, )\"\n"
            + "    }\n"
            + "  }\n"
            + "}");

        Project project = new NugetLockFileProjectParser(dependenciesParser).parseProject(lockfile);

        Assertions.assertEquals(1, project.dependencies.size());
        Assertions.assertEquals("Xamarin.Forms", project.dependencies.get(0).getName());
        Assertions.assertEquals("[2.3.4.270, )", project.dependencies.get(0).getVersion());
    }

    @Test
    void parsesFrameworkProjectReferencesFromRestore() {
        JsonObject lockfile = GsonUtil.toJson("{ \"project\": { \"restore\": { \"frameworks\": {\n"
            + "    \"uap10.0.10240\": {\n"
            + "        \"projectReferences\": {\n"
            + "            \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\": {\n"
            + "                \"projectPath\": \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\"\n"
            + "            }\n"
            + "        }\n"
            + "    }\n"
            + "}}}}");

        Project project = new NugetLockFileProjectParser(dependenciesParser).parseProject(lockfile);
        Assertions.assertEquals(1, project.frameworks.size());

        Framework framework = project.frameworks.get(0);
        Assertions.assertEquals("uap10.0.10240", framework.identifier);

        List<ProjectReference> projectReferences = framework.projectReferences;
        Assertions.assertEquals(1, projectReferences.size());
        Assertions.assertEquals("C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj", projectReferences.get(0).identifier);
        Assertions.assertEquals("C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj", projectReferences.get(0).path);
    }
}
