package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.util.NameVersion;

public class PackageDependency {
    public String identifier; // ex: "HockeySDK.Core/4.1.6"
    public List<NameVersion> dependencies = new ArrayList<>(); // ex: "HockeySDK.Core": "4.1.6"

    public PackageDependency(String identifier, List<NameVersion> dependencies) {
        this.identifier = identifier;
        this.dependencies = dependencies;
    }
}
