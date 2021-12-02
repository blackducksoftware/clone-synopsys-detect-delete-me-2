package com.synopsys.integration.detectable.detectables.nuget.future.unit;

public class FrameworkParseTests {

    void parsesProjectReference() {
        String block = "\"frameworks\": {\n"
            + "        \"uap10.0.10240\": {\n"
            + "          \"projectReferences\": {\n"
            + "            \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\": {\n"
            + "              \"projectPath\": \"C:\\\\PROJECTS\\\\SmartHive\\\\SmartHive.Controllers\\\\SmartHive.Controllers.csproj\"\n"
            + "            }\n"
            + "          }\n"
            + "        }";

    }

    void parsesEmpty() {

    }
}
