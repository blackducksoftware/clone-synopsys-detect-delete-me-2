package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.auto;

import java.util.Map;

public class Project {
    public String version;
    public Restore restore;
    public Map<String, String> dependencies;
}
