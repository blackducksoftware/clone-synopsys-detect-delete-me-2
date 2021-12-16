package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual.NugetLockFileDependenciesParser;
import com.synopsys.integration.detectable.util.GsonUtil;
import com.synopsys.integration.util.NameVersion;

public class DependencyParseTests {
    @Test
    void parsesNormalRange() {
        JsonObject dependenciesJson = GsonUtil.toJson("{ \"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[4.1.6, )\",\n"
            + "    }}");

        List<NameVersion> dependencies = new NugetLockFileDependenciesParser().parseDependencies(dependenciesJson);

        Assertions.assertEquals(1, dependencies.size());
        Assertions.assertEquals("HockeySDK.Core", dependencies.get(0).getName());
        Assertions.assertEquals("[4.1.6, )", dependencies.get(0).getVersion());
    }

    @Test
    void parsesPreview() {
        JsonObject dependenciesJson = GsonUtil.toJson("{\"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[0.0.5-preview, )\",\n"
            + "    }}");

        List<NameVersion> dependencies = new NugetLockFileDependenciesParser().parseDependencies(dependenciesJson);

        Assertions.assertEquals(1, dependencies.size());
        Assertions.assertEquals("HockeySDK.Core", dependencies.get(0).getName());
        Assertions.assertEquals("[0.0.5-preview, )", dependencies.get(0).getVersion());
    }

    @Test
    void parsesFourSections() {
        JsonObject dependenciesJson = GsonUtil.toJson("{\"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[2.3.4.270, )\",\n"
            + "    }}");

        List<NameVersion> dependencies = new NugetLockFileDependenciesParser().parseDependencies(dependenciesJson);

        Assertions.assertEquals(1, dependencies.size());
        Assertions.assertEquals("HockeySDK.Core", dependencies.get(0).getName());
        Assertions.assertEquals("[2.3.4.270, )", dependencies.get(0).getVersion());
    }

    @Test
    void parsesExact() {
        JsonObject dependenciesJson = GsonUtil.toJson("{\"dependencies\": {\n"
            + "      \"Microsoft.Azure.Mobile.Client\": \"4.0.1\",\n"
            + "    }}");

        List<NameVersion> dependencies = new NugetLockFileDependenciesParser().parseDependencies(dependenciesJson);

        Assertions.assertEquals(1, dependencies.size());
        Assertions.assertEquals("Microsoft.Azure.Mobile.Client", dependencies.get(0).getName());
        Assertions.assertEquals("4.0.1", dependencies.get(0).getVersion());
    }
}
