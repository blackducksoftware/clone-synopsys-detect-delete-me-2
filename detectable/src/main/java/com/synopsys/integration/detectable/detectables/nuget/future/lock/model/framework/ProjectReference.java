package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.framework;

public class ProjectReference {
    public String identifier;
    public String path;

    public ProjectReference(String identifier, String path) {
        this.identifier = identifier;
        this.path = path;
    }
}
