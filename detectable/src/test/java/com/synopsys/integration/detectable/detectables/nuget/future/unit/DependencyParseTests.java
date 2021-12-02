package com.synopsys.integration.detectable.detectables.nuget.future.unit;

public class DependencyParseTests {
    void parsesNormalRange() {
        String dependencies = "\"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[4.1.6, )\",\n"
            + "    },";
    }

    void parsesPreview() {
        String dependencies = "\"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[0.0.5-preview, )\",\n"
            + "    },";
    }

    void parsesFourSections() {
        String dependencies = "\"dependencies\": {\n"
            + "      \"HockeySDK.Core\": \"[2.3.4.270, )\",\n"
            + "    },";
    }

    void parsesExact() {
        String dependencies = "\"dependencies\": {\n"
            + "      \"Microsoft.Azure.Mobile.Client\": \"4.0.1\",\n"
            + "    },";
    }
}
