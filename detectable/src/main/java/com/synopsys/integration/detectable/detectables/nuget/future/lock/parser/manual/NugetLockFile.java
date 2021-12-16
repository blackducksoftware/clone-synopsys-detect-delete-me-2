package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.List;

import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.dependencygroup.DependencyGroup;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.library.Libraries;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.project.Project;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.model.target.Target;

public class NugetLockFile {
    public List<Target> targets;
    public Libraries libraries;
    public List<DependencyGroup> dependencyGroups;
    public Project project;
}
