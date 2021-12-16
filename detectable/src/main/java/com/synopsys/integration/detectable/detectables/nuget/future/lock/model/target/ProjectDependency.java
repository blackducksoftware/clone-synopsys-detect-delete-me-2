package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.util.NameVersion;

public class ProjectDependency {
    public String identifier; //ex: "Lib1/1.0.0"
    public List<NameVersion> dependencies = new ArrayList<>(); // ex: "HockeySDK.Core": "4.1.6"

    public ProjectDependency(String identifier, List<NameVersion> dependencies) {
        this.identifier = identifier;
        this.dependencies = dependencies;
    }
}