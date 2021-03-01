/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.nameversion.decision;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.workflow.nameversion.DetectorProjectInfo;
import com.synopsys.integration.util.NameVersion;

public class ArbitraryNameVersionDecision extends NameVersionDecision {
    private final DetectorProjectInfo chosenDetector;
    private final List<DetectorProjectInfo> otherDetectors;

    public ArbitraryNameVersionDecision(@Nullable final NameVersion nameVersion, final DetectorProjectInfo chosenDetector, final List<DetectorProjectInfo> otherDetectors) {
        super(nameVersion);
        this.chosenDetector = chosenDetector;
        this.otherDetectors = otherDetectors;
    }

    public DetectorProjectInfo getChosenDetector() {
        return chosenDetector;
    }

    public List<DetectorProjectInfo> getOtherDetectors() {
        return otherDetectors;
    }

    @Override
    public void printDescription(final Logger logger) {
        logger.info("The following project names were found: ");
        logger.info(String.format("\t%s: %s, %s",
            chosenDetector.getDetectorType().name(),
            chosenDetector.getNameVersion().getName(),
            chosenDetector.getNameVersion().getVersion()));
        for (final DetectorProjectInfo projectNamePossibility : otherDetectors) {
            logger.info(String.format("\t%s: %s, %s",
                projectNamePossibility.getDetectorType().name(),
                projectNamePossibility.getNameVersion().getName(),
                projectNamePossibility.getNameVersion().getVersion()
            ));
        }
        logger.info(String.format("Chose to use %s at depth %d for project name and version. Override with %s.",
            chosenDetector.getDetectorType().name(),
            chosenDetector.getDepth(),
             DetectProperties.DETECT_PROJECT_DETECTOR.getProperty().getKey()
        ));

    }
}
