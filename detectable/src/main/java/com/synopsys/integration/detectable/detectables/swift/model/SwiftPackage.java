package com.synopsys.integration.detectable.detectables.swift.model;

import java.util.ArrayList;
import java.util.List;

public class SwiftPackage {
    private String name;
    private String version;
    private List<SwiftPackage> dependencies = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public List<SwiftPackage> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final List<SwiftPackage> dependencies) {
        this.dependencies = dependencies;
    }
}
