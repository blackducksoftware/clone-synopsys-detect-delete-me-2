package com.synopsys.integration.detectable.detectables.nuget.future.lock.dependencygroup;

import java.util.List;

public class DependencyGroup {
    public String name; //ex: "UAP,Version=v10.0"
    public List<String> dependencies; //ex: "Microsoft.Azure.Mobile.Client >= 4.0.1"
}
