package com.synopsys.integration.detectable.detectables.nuget.future.lock.target;

import java.util.List;

public class Target {
    public String identifier; //ex: "UAP,Version=v10.0.10240"
    public List<PackageDependency> packageDependencies;
    public List<ProjectDependency> projectDependencies;
}
