package com.synopsys.integration.detect.battery.docker.util;

import java.io.File;

import com.synopsys.integration.exception.IntegrationException;

public class SharedDockerDirectory {
    private static final String DOCKER_TEST_DIR_PATH_KEY = "DOCKER_TEST_DIR_PATH";
    private static File dockerRoot = null;

    public static File getRoot() throws IntegrationException {
        if (dockerRoot == null) {
            String dockerRootPath = System.getenv(DOCKER_TEST_DIR_PATH_KEY);
            if (dockerRootPath == null) {
                throw new IntegrationException(String.format("%s must be set to run Detect Docker tests."));
            }
            dockerRoot = new File(dockerRootPath);
            dockerRoot.mkdirs();
        }
        return dockerRoot;
    }

}
