package com.synopsys.integration.detectable.detectables.nuget.future.lock.model.library;

public class ProjectLibrary {
    public String identifier;
    public String path;
    public String msbuildProject;

    public ProjectLibrary(String identifier, String path, String msbuildProject) {
        this.identifier = identifier;
        this.path = path;
        this.msbuildProject = msbuildProject;
    }
}
