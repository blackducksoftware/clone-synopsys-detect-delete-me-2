package com.synopsys.integration.detectable.detectables.nuget.future.unit;

public class DependencyGroupTests {
    public void parsesDependencyGroupsInEmpty() {
        String groups = "\"projectFileDependencyGroups\": {\n"
            + "    \"\": [\n"
            + "      \"Microsoft.Azure.Mobile.Client >= 4.0.1\""
            + "    ],\n"
            + "  },";
    }

    public void parsesDependencyGroupsInNamed() {
        String groups = "\"projectFileDependencyGroups\": {\n"
            + "    \"\": [],\n"
            + "    \"UAP,Version=v10.0\": [\n"
            + "      \"Xamarin.Forms >= 2.3.4.247\""
            + "    ],\n"
            + "  },";
    }
}
