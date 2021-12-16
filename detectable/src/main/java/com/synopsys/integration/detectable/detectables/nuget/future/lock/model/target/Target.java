package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target;

import java.util.ArrayList;
import java.util.List;

public class Target {
    public String identifier; //ex: "UAP,Version=v10.0.10240"
    public List<PackageDependency> packageDependencies = new ArrayList<>();
    public List<ProjectDependency> projectDependencies = new ArrayList<>();
}
