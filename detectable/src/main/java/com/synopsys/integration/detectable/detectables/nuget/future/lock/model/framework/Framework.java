package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework;

import java.util.ArrayList;
import java.util.List;

public class Framework {
    public String identifier;
    public List<ProjectReference> projectReferences = new ArrayList<>();

    public Framework(String identifier) {
        this.identifier = identifier;
    }
}
