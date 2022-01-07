package com.synopsys.integration.detectable.detectables.pnpm.lockfile.model;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.detectable.detectable.enums.DependencyType;

public class PnpmPackageInfo {
    @Nullable
    public Boolean dev;
    @Nullable
    public Boolean optional;
    @Nullable
    public Map<String, String> dependencies;
    @Nullable
    public String name;
    @Nullable
    public String version;

    private boolean isDev() {
        return dev != null && dev;
    }

    private boolean isOptional() {
        return optional != null && optional;
    }

    public Map<String, String> getDependencies() {
        return MapUtils.emptyIfNull(dependencies);
    }

    public DependencyType getDependencyType() {
        if (isDev()) {
            return DependencyType.DEV;
        }
        if (isOptional()) {
            return DependencyType.OPTIONAL;
        }
        return DependencyType.APP;
    }

}