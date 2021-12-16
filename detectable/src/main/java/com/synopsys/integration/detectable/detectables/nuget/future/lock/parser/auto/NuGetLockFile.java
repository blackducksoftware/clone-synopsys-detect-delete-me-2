package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.auto;

import java.util.List;
import java.util.Map;

public class NuGetLockFile {
    public Integer version;
    public Map<String, Map<String, FrameworkDependency>> targets;
    public Map<String, Library> libraries;
    public Map<String, List<String>> projectFileDependencyGroups;
    public Project project;
}
