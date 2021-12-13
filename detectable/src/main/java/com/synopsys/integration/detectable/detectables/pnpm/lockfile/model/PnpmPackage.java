package com.synopsys.integration.detectable.detectables.pnpm.lockfile.model;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.detectable.detectable.enums.DependencyType;

public class PnpmPackage {
    @Nullable
    public Boolean dev;
    @Nullable
    public Boolean optional;
    @Nullable
    public Map<String, String> dependencies;

    private boolean isDev() {
        return dev != null ? dev : false;
    }

    private boolean isOptional() {
        return optional != null ? optional : false;
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
