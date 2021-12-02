package com.synopsys.integration.detectable.detectables.nuget.future.lock.project;

import java.util.List;

import com.synopsys.integration.detectable.detectables.nuget.future.lock.framework.Framework;
import com.synopsys.integration.util.NameVersion;

public class Project {
    public String name;
    public String path;
    public String projectJsonPath;

    public List<NameVersion> dependencies;
    public List<Framework> frameworks;
}
