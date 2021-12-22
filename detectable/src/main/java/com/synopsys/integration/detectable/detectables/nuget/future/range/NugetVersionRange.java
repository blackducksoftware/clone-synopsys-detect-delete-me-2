package com.synopsys.integration.detectable.detectables.nuget.future.range;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import de.skuzzle.semantic.Version;

public class NuGetVersionRange {
    private final boolean includeMinVersion;
    @Nullable
    private final NuGetVersion minVersion;

    private final boolean includeMaxVersion;
    @Nullable
    private final NuGetVersion maxVersion;

    public NuGetVersionRange(@Nullable NuGetVersion minVersion, boolean includeMinVersion, @Nullable NuGetVersion maxVersion, boolean includeMaxVersion, NuGetFloatRange floatRange, String originalString) {
        this.includeMinVersion = includeMinVersion;
        this.minVersion = minVersion;
        this.includeMaxVersion = includeMaxVersion;
        this.maxVersion = maxVersion;
    }

    public static NuGetVersionRange forExact(NuGetVersion version) {
        return new NuGetVersionRange(version, true, version, true, null, null);
    }

    public static NuGetVersionRange forMaximumInclusive(NuGetVersion version) {
        return new NuGetVersionRange(null, false, version, true, null, null);
    }

    public static NuGetVersionRange forMaximumExclusive(NuGetVersion version) {
        return new NuGetVersionRange(null, false, version, false, null, null);
    }

    public static NuGetVersionRange forMinimumInclusive(NuGetVersion version) {
        return new NuGetVersionRange(version, true, version, true, null, null);
    }

    public static NuGetVersionRange forMinimumExclusive(NuGetVersion version) {
        return new NuGetVersionRange(version, false, null, false, null, null);
    }

    public boolean hasMinVersion() {
        return minVersion != null;
    }

    public boolean includeMinVersion() {
        return includeMinVersion;
    }

    @Nullable
    public NuGetVersion minVersion() {
        return minVersion;
    }

    public boolean hasMaxVersion() {
        return maxVersion != null;
    }

    public boolean includeMaxVersion() {
        return includeMaxVersion;
    }

    @Nullable
    public NuGetVersion maxVersion() {
        return maxVersion;
    }

    public boolean Satisfies(@NotNull NuGetVersion version) {
        // Determine if version is in the given range using the comparer.
        boolean condition = true;
        if (minVersion != null) {
            if (includeMinVersion) {
                condition = NuGetVersionCompare.Compare(minVersion, version) <= 0;
            } else {
                condition = NuGetVersionCompare.Compare(minVersion, version) < 0;
            }
        }

        if (maxVersion != null) {
            if (includeMaxVersion) {
                condition &= NuGetVersionCompare.Compare(maxVersion, version) >= 0;
            } else {
                condition &= NuGetVersionCompare.Compare(maxVersion, version) > 0;
            }
        }

        return condition;
    }
}
