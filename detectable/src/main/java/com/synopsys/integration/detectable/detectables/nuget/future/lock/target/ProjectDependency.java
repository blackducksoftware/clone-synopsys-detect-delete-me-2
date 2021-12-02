package com.synopsys.integration.detectable.detectables.nuget.future.lock.target;

import java.util.List;

import com.synopsys.integration.util.NameVersion;

public class ProjectDependency {
    public String identifier; //ex: "Lib1/1.0.0"
    public List<NameVersion> dependencies; // ex: "HockeySDK.Core": "4.1.6"
}