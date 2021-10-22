package com.synopsys.integration.detectable.detectables.pnpm.lockfile.model;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

public class PnpmPackage {
    private static final String TRUE = "true";

    public String dev;
    public String optional;
    public Map<String, String> dependencies;

    public boolean isDev() {
        if (dev == null) {
            return false;
        }
        return dev.equals(TRUE);
    }

    public boolean isOptional() {
        if (optional == null) {
            return false;
        }
        return optional.equals(TRUE);
    }

    public boolean hasDependencies() {
        return dependencies != null && !CollectionUtils.isEmpty(dependencies.entrySet());
    }
}
