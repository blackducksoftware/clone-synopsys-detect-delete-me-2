package com.synopsys.integration.detectable.detectables.nuget.future.range;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.semantic.Version;

public class NuGetVersionConstraintParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //Of the form '[Version, Version]' or '(Version, Version)' or 'Version, Version'
    public NuGetVersionRange parse(String versionRangeText) {
        if (StringUtils.containsAny(versionRangeText, "()[],")) {
            String[] pieces = versionRangeText.split(",");
            Version left = null;
            boolean leftInclusive = true;
            if (pieces.length >= 1) {
                String leftBoundaryText = pieces[0].trim();
                String leftVersionText = StringUtils.replaceChars(leftBoundaryText, "[(", "");
                if (!StringUtils.isBlank(leftVersionText)) {
                    left = Version.parseVersion(leftVersionText);
                    if (leftBoundaryText.startsWith("(")) {
                        leftInclusive = false;
                    } else if (!leftBoundaryText.startsWith("[")) {
                        logger.warn("Unknown version constraint boundary, treating as inclusive: " + leftBoundaryText);
                    }
                }
            }
            Version right = null;
            boolean rightInclusive = true;
            if (pieces.length >= 2) {
                String rightBoundaryText = pieces[1].trim();
                String rightVersionText = StringUtils.replaceChars(rightBoundaryText, ")]", "");
                if (!StringUtils.isBlank(rightVersionText)) {
                    right = Version.parseVersion(rightVersionText);
                    if (rightBoundaryText.endsWith(")")) {
                        rightInclusive = false;
                    } else if (!rightBoundaryText.endsWith("]")) {
                        logger.warn("Unknown version constraint boundary, treating as inclusive: " + rightBoundaryText);
                    }
                }
            }

            return new NuGetVersionRange(left != null, leftInclusive, left, right != null, rightInclusive, right);
        } else {
            Version version = Version.parseVersion(versionRangeText);
            return NuGetVersionRange.forExact(version);
        }
    }
}
