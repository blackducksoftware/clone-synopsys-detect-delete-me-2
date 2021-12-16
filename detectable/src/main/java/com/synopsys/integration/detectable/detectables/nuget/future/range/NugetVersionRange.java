package com.synopsys.integration.detectable.detectables.nuget.future.range;

import de.skuzzle.semantic.Version;

public class NuGetVersionRange {
    /*
    Real world examples:
        "[4.1.6, )"
        "[0.0.5-preview, )"
        "[2.3.4.270, )"
        "4.0.1"
     */
    private final boolean hasMinVersion;
    private boolean includeMinVersion;
    private final Version minVersion;

    private final boolean hasMaxVersion;
    private boolean includeMaxVersion;
    private final Version maxVersion;

    public NuGetVersionRange(boolean hasMinVersion, boolean includeMinVersion, Version minVersion, boolean hasMaxVersion, boolean includeMaxVersion, Version maxVersion) {
        this.hasMinVersion = hasMinVersion;
        this.includeMinVersion = includeMinVersion;
        this.minVersion = minVersion;
        this.hasMaxVersion = hasMaxVersion;
        this.includeMaxVersion = includeMaxVersion;
        this.maxVersion = maxVersion;
    }

    public Version bestVersion(Version... versions) { //TODO: implement
        //basically filter out too high
        //then filter out too low
        //then pick the greatest
        return versions[0];
    }

    public static NuGetVersionRange forExact(Version version) {
        return new NuGetVersionRange(true, true, version, true, true, version);
    }

    public static NuGetVersionRange forMaximumInclusive(Version version) {
        return new NuGetVersionRange(false, false, null, true, true, version);
    }
    public static NuGetVersionRange forMaximumExclusive(Version version) {
        return new NuGetVersionRange(false, false, null, true, false, version);
    }
    public static NuGetVersionRange forMinimumInclusive(Version version) {
        return new NuGetVersionRange(true, true, version, true, true, version);
    }
    public static NuGetVersionRange forMinimumExclusive(Version version) {
        return new NuGetVersionRange(true, false, version, false, false, null);
    }
}
