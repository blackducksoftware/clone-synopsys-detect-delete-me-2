package com.synopsys.integration.detectable.detectables.nuget.future.lock.target;

import java.util.List;

import com.synopsys.integration.util.NameVersion;

public class PackageDependency {
    public String identifier; // ex: "HockeySDK.Core/4.1.6"
    public List<NameVersion> dependencies; // ex: "HockeySDK.Core": "4.1.6"
}
