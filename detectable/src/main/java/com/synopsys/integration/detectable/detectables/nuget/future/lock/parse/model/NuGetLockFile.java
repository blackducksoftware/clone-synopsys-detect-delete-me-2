package com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model;

import java.util.List;
import java.util.Map;

public class NuGetLockFile {
    public Integer version;
    public Map<String, Map<String, Dependency>> targets;
    public Map<String, Library> libraries;
    public Map<String, List<String>> projectFileDependencyGroups;
    public Project project;
}
