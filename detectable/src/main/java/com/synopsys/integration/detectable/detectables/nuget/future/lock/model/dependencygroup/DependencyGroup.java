package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.dependencygroup;

import java.util.ArrayList;
import java.util.List;

public class DependencyGroup {
    public String name; //ex: "UAP,Version=v10.0"
    public List<String> dependencies = new ArrayList<>(); //ex: "Microsoft.Azure.Mobile.Client >= 4.0.1"

    public DependencyGroup(String name, List<String> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }
}
