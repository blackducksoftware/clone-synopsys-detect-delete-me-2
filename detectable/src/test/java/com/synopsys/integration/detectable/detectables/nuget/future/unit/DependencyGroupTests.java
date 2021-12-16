package com.synopsys.integration.detectable.detectables.nuget.future.unit;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.dependencygroup.DependencyGroup;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual.NugetLockFileDependencyGroupParser;
import com.synopsys.integration.detectable.util.GsonUtil;

public class DependencyGroupTests {
    @Test
    public void parsesDependencyGroupsInEmpty() {
        JsonObject groups = GsonUtil.toJson("{ \"projectFileDependencyGroups\": {\n"
            + "    \"\": [\n"
            + "      \"Microsoft.Azure.Mobile.Client >= 4.0.1\""
            + "    ]\n"
            + "  } }");

        List<DependencyGroup> dependencyGroups = new NugetLockFileDependencyGroupParser().parseDependencyGroups(groups);
        Assertions.assertEquals(1, dependencyGroups.size());
        DependencyGroup group = dependencyGroups.get(0);
        Assertions.assertEquals("", group.name);
        Assertions.assertEquals(1, group.dependencies.size());
        Assertions.assertEquals("Microsoft.Azure.Mobile.Client >= 4.0.1", group.dependencies.get(0));
    }

    @Test
    public void parsesDependencyGroupsInNamed() {
        JsonObject groups = GsonUtil.toJson("{ \"projectFileDependencyGroups\": {\n"
            + "    \"\": [],\n"
            + "    \"UAP,Version=v10.0\": [\n"
            + "      \"Xamarin.Forms >= 2.3.4.247\""
            + "    ]\n"
            + "  } }");

        List<DependencyGroup> dependencyGroups = new NugetLockFileDependencyGroupParser().parseDependencyGroups(groups);
        Assertions.assertEquals(2, dependencyGroups.size());

        DependencyGroup group1 = dependencyGroups.get(0);
        Assertions.assertEquals("", group1.name);
        Assertions.assertEquals(0, group1.dependencies.size());

        DependencyGroup group2 = dependencyGroups.get(1);
        Assertions.assertEquals("UAP,Version=v10.0", group2.name);
        Assertions.assertEquals(1, group2.dependencies.size());
        Assertions.assertEquals("Xamarin.Forms >= 2.3.4.247", group2.dependencies.get(0));

    }

}
