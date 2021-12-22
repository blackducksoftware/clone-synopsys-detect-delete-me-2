package com.synopsys.integration.detectable.detectables.nuget.future.range;

import org.apache.commons.lang3.StringUtils;

public class NuGetVersionCompare {

    public static int Compare(NuGetVersion x, NuGetVersion y) {
        if (x.equals(y)) {
            return 0;
        }

        if (y == null) {
            return 1;
        }

        if (x == null) {
            return -1;
        }

        SimpleVersion xv = x.getVersion();
        SimpleVersion yv = y.getVersion();

        // compare version
        int result = Integer.compare(xv.getMajor(), yv.getMajor());
        if (result != 0) {
            return result;
        }

        result = Integer.compare(xv.getMinor(), yv.getMinor());
        if (result != 0) {
            return result;
        }

        result = xv.getBuild().compareTo(yv.getBuild());
        if (result != 0) {
            return result;
        }

        if (xv.getRevision() != null && yv.getRevision() != null) {
            return xv.getRevision().compareTo(yv.getRevision());
        } else if (xv.getRevision() != null && xv.getRevision() > 0) {
            return 1;
        } else if (yv.getRevision() != null && yv.getRevision() > 0) {
            return -1;
        }

        // compare release labels
        String[] xLabels = x.getReleaseLabels();
        String[] yLabels = y.getReleaseLabels();

        if (xLabels != null
            && yLabels == null) {
            return -1;
        }

        if (xLabels == null
            && yLabels != null) {
            return 1;
        }

        if (xLabels != null
            && yLabels != null) {
            result = CompareReleaseLabels(xLabels, yLabels);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    private static int CompareReleaseLabels(String[] version1, String[] version2) {
        int result = 0;

        int count = Math.max(version1.length, version2.length);

        for (int i = 0; i < count; i++) {
            boolean aExists = i < version1.length;
            boolean bExists = i < version2.length;

            if (!aExists && bExists) {
                return -1;
            }

            if (aExists && !bExists) {
                return 1;
            }

            // compare the labels
            result = CompareRelease(version1[i], version2[i]);

            if (result != 0) {
                return result;
            }
        }

        return result;
    }

    private static int CompareRelease(String version1, String version2) {
        int result = 0;

        // check if the identifiers are numeric
        Integer version1Num = parseIntOrNull(version1);
        Integer version2Num = parseIntOrNull(version2);

        boolean v1IsNumeric = version1Num != null;
        boolean v2IsNumeric = version2Num != null;
        // if both are numeric compare them as numbers
        if (v1IsNumeric && v2IsNumeric) {
            result = version1Num.compareTo(version2Num);
        } else if (v1IsNumeric || v2IsNumeric) {
            // numeric labels come before alpha labels
            if (v1IsNumeric) {
                result = -1;
            } else {
                result = 1;
            }
        } else {
            // Ignoring 2.0.0 case sensitive compare. Everything will be compared case insensitively as 2.0.1 specifies.
            result = StringUtils.compareIgnoreCase(version1, version2);
        }

        return result;
    }

    public static Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
