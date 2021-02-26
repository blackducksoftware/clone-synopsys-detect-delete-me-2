/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.exit;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.status.DetectStatusManager;
import com.synopsys.integration.log.Slf4jIntLogger;

public class ExitManager {
    private Logger logger = LoggerFactory.getLogger(ExitManager.class);
    private EventSystem eventSystem;
    private ExitCodeManager exitCodeManager;
    private DetectStatusManager statusManager;

    public ExitManager(EventSystem eventSystem, ExitCodeManager exitCodeManager, DetectStatusManager statusManager) {
        this.eventSystem = eventSystem;
        this.exitCodeManager = exitCodeManager;
        this.statusManager = statusManager;
    }

    public ExitResult exit(ExitOptions exitOptions) {
        long startTime = exitOptions.getStartTime();
        boolean logResults = exitOptions.shouldLogResults();
        boolean forceSuccessExit = exitOptions.shouldForceSuccessExit();
        boolean shouldExit = exitOptions.shouldExit();

        //Generally, when requesting a failure status, an exit code is also requested, but if it is not, we default to an unknown error.
        if (statusManager.hasAnyFailure()) {
            eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_UNKNOWN_ERROR, "A failure status was requested by one or more of Detect's tools."));
        }

        //Find the final (as requested) exit code
        ExitCodeType finalExitCode = exitCodeManager.getWinningExitCode();

        //Print detect's status
        if (logResults) {
            statusManager.logDetectResults(new Slf4jIntLogger(logger), finalExitCode);
        }

        //Print duration of run
        long endTime = System.currentTimeMillis();
        String duration = DurationFormatUtils.formatPeriod(startTime, endTime, "HH'h' mm'm' ss's' SSS'ms'");
        logger.info("Detect duration: {}", duration);

        //Exit with formal exit code
        if (finalExitCode != ExitCodeType.SUCCESS && forceSuccessExit) {
            logger.warn("Forcing success: Exiting with exit code 0. Ignored exit code was {}.", finalExitCode.getExitCode());
        } else if (finalExitCode != ExitCodeType.SUCCESS) {
            logger.error("Exiting with code {} - {}", finalExitCode.getExitCode(), finalExitCode);
        }

        if (!shouldExit) {
            logger.info("Would normally exit({}) but it is overridden.", finalExitCode.getExitCode());
        }

        return new ExitResult(finalExitCode, forceSuccessExit, shouldExit);
    }
}
