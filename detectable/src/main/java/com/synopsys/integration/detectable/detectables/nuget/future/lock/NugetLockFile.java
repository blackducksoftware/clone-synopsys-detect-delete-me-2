package com.synopsys.integration.detectable.detectables.nuget.future.lock;

import java.util.List;

import com.synopsys.integration.detectable.detectables.nuget.future.lock.dependencygroup.DependencyGroup;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.library.PackageLibrary;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.library.ProjectLibrary;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.project.Project;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.target.Target;

public class NugetLockFile {
    public List<Target> targets;
    public List<DependencyGroup> dependencyGroups;
    public Project project;

    public List<PackageLibrary> packageLibraries;
    public List<ProjectLibrary> projectLibraries;
}
