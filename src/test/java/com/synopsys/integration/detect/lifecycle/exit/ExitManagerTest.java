package com.synopsys.integration.detect.lifecycle.exit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeManager;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeUtility;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.status.DetectStatusManager;

public class ExitManagerTest {
    @Test
    public void testNormalExit() {
        long startTime = System.currentTimeMillis();
        EventSystem eventSystem = new EventSystem();
        DetectStatusManager statusManager = new DetectStatusManager(eventSystem);
        ExitCodeUtility exitCodeUtility = new ExitCodeUtility();
        ExitCodeManager exitCodeManager = new ExitCodeManager(eventSystem, exitCodeUtility);
        ExitManager exitManager = new ExitManager(eventSystem, exitCodeManager, statusManager);

        ExitOptions exitOptions = new ExitOptions(startTime, true, false, true);
        ExitResult exitResult = exitManager.exit(exitOptions);

        assertEquals(startTime, exitOptions.getStartTime());
        assertTrue(exitOptions.shouldLogResults());
        assertFalse(exitResult.shouldForceSuccess());
        assertTrue(exitResult.shouldPerformExit());
        assertEquals(ExitCodeType.SUCCESS, exitResult.getExitCodeType());
    }

    @Test
    public void testForceExit() {
        long startTime = System.currentTimeMillis();
        EventSystem eventSystem = new EventSystem();
        DetectStatusManager statusManager = new DetectStatusManager(eventSystem);
        ExitCodeUtility exitCodeUtility = new ExitCodeUtility();
        ExitCodeManager exitCodeManager = new ExitCodeManager(eventSystem, exitCodeUtility);
        exitCodeManager.addExitCodeRequest(new ExitCodeRequest(ExitCodeType.FAILURE_CONFIGURATION, "JUnit failure code."));
        ExitManager exitManager = new ExitManager(eventSystem, exitCodeManager, statusManager);

        ExitOptions exitOptions = new ExitOptions(startTime, true, true, true);
        ExitResult exitResult = exitManager.exit(exitOptions);

        assertEquals(startTime, exitOptions.getStartTime());
        assertTrue(exitOptions.shouldLogResults());
        assertTrue(exitResult.shouldForceSuccess());
        assertTrue(exitResult.shouldPerformExit());
        assertEquals(ExitCodeType.FAILURE_CONFIGURATION, exitResult.getExitCodeType());
    }

    @Test
    public void testSkipExit() {
        long startTime = System.currentTimeMillis();
        EventSystem eventSystem = new EventSystem();
        DetectStatusManager statusManager = new DetectStatusManager(eventSystem);
        ExitCodeUtility exitCodeUtility = new ExitCodeUtility();
        ExitCodeManager exitCodeManager = new ExitCodeManager(eventSystem, exitCodeUtility);
        exitCodeManager.addExitCodeRequest(new ExitCodeRequest(ExitCodeType.FAILURE_CONFIGURATION, "JUnit failure code."));
        ExitManager exitManager = new ExitManager(eventSystem, exitCodeManager, statusManager);

        ExitOptions exitOptions = new ExitOptions(startTime, true, false, false);
        ExitResult exitResult = exitManager.exit(exitOptions);

        assertEquals(startTime, exitOptions.getStartTime());
        assertTrue(exitOptions.shouldLogResults());
        assertFalse(exitResult.shouldForceSuccess());
        assertFalse(exitResult.shouldPerformExit());
        assertEquals(ExitCodeType.FAILURE_CONFIGURATION, exitResult.getExitCodeType());

    }

}
