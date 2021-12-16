package com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model;

import java.util.Map;

public abstract class Dependency {
    public String type;
    public Map<String, String> dependencies;
}
