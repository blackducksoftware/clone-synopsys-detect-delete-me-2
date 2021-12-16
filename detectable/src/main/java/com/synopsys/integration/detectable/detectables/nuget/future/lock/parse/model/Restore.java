package com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model;

import java.util.Map;

public class Restore {
    public String projectUniqueName;
    public String projectName;
    public String projectPath;
    public String packagesPath;
    public String outputPath;
    public String projectStyle;

    public Map<String, RestoreFramework> frameworks;

}
