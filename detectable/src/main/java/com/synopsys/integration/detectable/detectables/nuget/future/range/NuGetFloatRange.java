package com.synopsys.integration.detectable.detectables.nuget.future.range;

import org.apache.commons.lang3.StringUtils;

public class NuGetFloatRange {
    private final NuGetVersionFloatBehavior behavior;

    public NuGetVersion getMinVersion() {
        return minVersion;
    }

    private final NuGetVersion minVersion;
    private final String releasePrefix;

    public NuGetFloatRange(NuGetVersionFloatBehavior behavior, NuGetVersion version, String releasePrefix) {
        this.behavior = behavior;
        this.minVersion = version;

        if (releasePrefix == null
            && version != null
            && version.IsPrerelease()) {
            // use the actual label if one was not given
            this.releasePrefix = version.getRelease();
        } else {
            this.releasePrefix = releasePrefix;
        }
    }

    public static NuGetFloatRange TryParse(String versionString) {
        if (versionString != null && !StringUtils.isBlank(versionString)) {
            int firstStarPosition = versionString.indexOf('*');
            int lastStarPosition = versionString.lastIndexOf('*');
            String releasePrefix = null;

            if (versionString.length() == 1
                && firstStarPosition == 0) {
                SimpleVersion simpleVersion = new SimpleVersion(0, 0, null, null);
                NuGetVersion nuGetVersion = new NuGetVersion(simpleVersion, null, null, null);
                return new NuGetFloatRange(NuGetVersionFloatBehavior.Major, nuGetVersion, "");
            } else if (versionString.equals("*-*")) {
                return new NuGetFloatRange(NuGetVersionFloatBehavior.AbsoluteLatest, NuGetVersion.TryParse("0.0.0-0"), "");
            } else if (firstStarPosition != lastStarPosition && lastStarPosition != -1 && versionString.indexOf('+') == -1) {
                NuGetVersionFloatBehavior behavior = NuGetVersionFloatBehavior.None;
                // 2 *s are only allowed in prerelease versions.
                int dashPosition = versionString.indexOf('-');
                String actualVersion = null;

                if (dashPosition != -1 &&
                    lastStarPosition == versionString.length() - 1 && // Last star is at the end of the full string
                    firstStarPosition == (dashPosition - 1) // First star is right before the first dash.
                ) {
                    // Get the stable part.
                    String stablePart = versionString.substring(0, dashPosition - 1); // Get the part without the *
                    stablePart += "0";
                    int versionParts = CalculateVersionParts(stablePart);
                    switch (versionParts) {
                        case 1:
                            behavior = NuGetVersionFloatBehavior.PrereleaseMajor;
                            break;
                        case 2:
                            behavior = NuGetVersionFloatBehavior.PrereleaseMinor;
                            break;
                        case 3:
                            behavior = NuGetVersionFloatBehavior.PrereleasePatch;
                            break;
                        case 4:
                            behavior = NuGetVersionFloatBehavior.PrereleaseRevision;
                            break;
                        default:
                            break;
                    }

                    String releaseVersion = versionString.substring(dashPosition + 1);
                    releasePrefix = releaseVersion.substring(0, releaseVersion.length() - 1);
                    String releasePart = releasePrefix;
                    if (releasePrefix.length() == 0 || releasePrefix.endsWith(".")) {
                        // 1.0.0-* scenario, an empty label is not a valid version.
                        releasePart += "0";
                    }

                    actualVersion = stablePart + "-" + releasePart;
                }

                NuGetVersion version = NuGetVersion.TryParse(actualVersion);
                if (version != null) {
                    return new NuGetFloatRange(behavior, version, releasePrefix);
                }
            }
            // A single * can only appear as the last char in the string.
            // * cannot appear in the metadata section after the +
            else if (lastStarPosition == versionString.length() - 1 && versionString.indexOf('+') == -1) {
                NuGetVersionFloatBehavior behavior = NuGetVersionFloatBehavior.None;

                String actualVersion = versionString.substring(0, versionString.length() - 1);

                if (versionString.indexOf('-') == -1) {
                    // replace the * with a 0
                    actualVersion += "0";

                    int versionParts = CalculateVersionParts(actualVersion);

                    if (versionParts == 2) {
                        behavior = NuGetVersionFloatBehavior.Minor;
                    } else if (versionParts == 3) {
                        behavior = NuGetVersionFloatBehavior.Patch;
                    } else if (versionParts == 4) {
                        behavior = NuGetVersionFloatBehavior.Revision;
                    }
                } else {
                    behavior = NuGetVersionFloatBehavior.Prerelease;

                    // check for a prefix
                    if (versionString.indexOf('-') == versionString.lastIndexOf('-')) {
                        releasePrefix = actualVersion.substring(versionString.lastIndexOf('-') + 1);

                        // For numeric labels 0 is the lowest. For alpha-numeric - is the lowest.
                        if (releasePrefix.length() == 0 || actualVersion.endsWith(".")) {
                            // 1.0.0-* scenario, an empty label is not a valid version.
                            actualVersion += "0";
                        } else if (actualVersion.endsWith("-")) {
                            // Append a dash to allow floating on the next character.
                            actualVersion += "-";
                        }
                    }
                }

                NuGetVersion version = NuGetVersion.TryParse(actualVersion);
                if (version != null) {
                    return new NuGetFloatRange(behavior, version, releasePrefix);
                }
            } else {
                // normal version parse
                NuGetVersion version = NuGetVersion.TryParse(versionString);
                if (version != null) {
                    // there is no float range for this version
                    return new NuGetFloatRange(NuGetVersionFloatBehavior.None, version, null);
                }
            }
        }

        return null;
    }

    static int CalculateVersionParts(String line) {
        int count = 1;
        if (line != null) {
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '.') {
                    count++;
                }
            }
        }
        return count;
    }

    public boolean hasMinVersion() {
        return minVersion != null;
    }
}
