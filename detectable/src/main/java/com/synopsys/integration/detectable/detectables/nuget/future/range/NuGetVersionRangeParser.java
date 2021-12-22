package com.synopsys.integration.detectable.detectables.nuget.future.range;

import org.apache.commons.lang3.StringUtils;

public class NuGetVersionRangeParser {

    public NuGetVersionRange parse(String value, boolean allowFloating) {
        String trimmedValue = value.trim();
        if (StringUtils.isEmpty(trimmedValue)) {
            return null;
        }

        char[] charArray = trimmedValue.toCharArray();

        // * is the only 1 char range
        if (allowFloating
            && charArray.length == 1
            && charArray[0] == '*') {
            return new NuGetVersionRange(new NuGetVersion(new SimpleVersion(0, 0, 0, null), null, null, value), true, null, true, NuGetFloatRange.TryParse(trimmedValue), value);
        }

        String minVersionString = null;
        String maxVersionString = null;
        boolean isMinInclusive;
        boolean isMaxInclusive;
        NuGetVersion minVersion = null;
        NuGetVersion maxVersion = null;
        NuGetFloatRange floatRange = null;

        if (charArray[0] == '('
            || charArray[0] == '[') {
            // The first character must be [ to (
            switch (charArray[0]) {
                case '[':
                    isMinInclusive = true;
                    break;
                case '(':
                    isMinInclusive = false;
                    break;
                default:
                    return null;
            }

            // The last character must be ] ot )
            switch (charArray[charArray.length - 1]) {
                case ']':
                    isMaxInclusive = true;
                    break;
                case ')':
                    isMaxInclusive = false;
                    break;
                default:
                    return null;
            }

            // Get rid of the two brackets
            trimmedValue = trimmedValue.substring(1, trimmedValue.length() - 2);

            // Split by comma, and make sure we don't get more than two pieces
            String[] parts = StringUtils.splitPreserveAllTokens(trimmedValue, ',');

            if (parts.length > 2) {
                return null;
            } else {
                boolean allEmpty = true;

                for (int i = 0; i < parts.length; i++) {
                    if (!StringUtils.isEmpty(parts[i])) {
                        allEmpty = false;
                        break;
                    }
                }

                // If all parts are empty, then neither of upper or lower bounds were specified. Version spec is of the format (,]
                if (allEmpty) {
                    return null;
                }
            }

            // (1.0.0] and [1.0.0),(1.0.0) are invalid.
            if (parts.length == 1
                && !(isMinInclusive && isMaxInclusive)) {
                return null;
            }

            // If there is only one piece, we use it for both min and max
            minVersionString = parts[0];
            maxVersionString = (parts.length == 2) ? parts[1] : parts[0];
        } else {
            // default to min inclusive when there are no braces
            isMinInclusive = true;
            isMaxInclusive = false;

            // use the entire value as the version
            minVersionString = trimmedValue;
        }

        if (!StringUtils.isEmpty(minVersionString)) {
            // parse the min version string
            if (allowFloating && minVersionString.contains("*")) {
                // single floating version
                floatRange = NuGetFloatRange.TryParse(minVersionString);
                if (floatRange != null && floatRange.hasMinVersion()) {
                    minVersion = floatRange.getMinVersion();
                } else {
                    // invalid float
                    return null;
                }
            } else {
                // single non-floating version
                minVersion = NuGetVersion.TryParse(minVersionString);
                if (minVersion == null) {
                    // invalid version
                    return null;
                }
            }
        }

        // parse the max version string, the max cannot float
        if (!StringUtils.isEmpty(maxVersionString)) {
            maxVersion = NuGetVersion.TryParse(maxVersionString);
            if (maxVersion == null) {
                // invalid version
                return null;
            }
        }

        if (minVersion != null && maxVersion != null) {
            int result = NuGetVersionCompare.Compare(minVersion, maxVersion);

            // minVersion > maxVersion
            if (result > 0) {
                return null;
            }

            // minVersion is equal to maxVersion (1.0.0, 1.0.0], [1.0.0, 1.0.0)
            if (result == 0
                && (isMinInclusive ^ isMaxInclusive)) {
                return null;
            }
        }

        // Successful parse!
        return new NuGetVersionRange(
            minVersion,
            isMinInclusive,
            maxVersion,
            isMaxInclusive,
            floatRange,
            value);
    }
}
