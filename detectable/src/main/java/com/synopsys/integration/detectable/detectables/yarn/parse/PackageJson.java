package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class PackageJson {
    @SerializedName("name")
    public String name;

    @SerializedName("version")
    public String version;

    @SerializedName("dependencies")
    public Map<String, String> dependencies = new HashMap<>();

    @SerializedName("devDependencies")
    public Map<String, String> devDependencies = new HashMap<>();
}
