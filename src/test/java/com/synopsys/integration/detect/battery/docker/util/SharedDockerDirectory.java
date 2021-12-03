package com.synopsys.integration.detect.battery.docker.util;

import java.io.File;
import java.io.IOException;

public class SharedDockerDirectory {
    private static final String DOCKER_TEST_DIR_PATH_KEY = "DOCKER_TEST_DIR_PATH";
    private static File dockerRoot = null;
    private static File sharedTools = null;

    public static File getRoot() {
        if (dockerRoot == null) {
            String dockerRootPath = System.getenv(DOCKER_TEST_DIR_PATH_KEY);
            dockerRoot = new File(dockerRootPath);
            dockerRoot.mkdirs();
        }
        return dockerRoot;
    }

    public static File getSharedTools() throws IOException {
        if (sharedTools == null) {
            sharedTools = new File(getRoot(), "tools");
        }
        return sharedTools;
    }
}
