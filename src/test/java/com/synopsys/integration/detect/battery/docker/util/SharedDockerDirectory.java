package com.synopsys.integration.detect.battery.docker.util;

import java.io.File;

public class SharedDockerDirectory {
    private static File dockerRoot = null;

    public static File getRoot() {
        if (dockerRoot == null) {
            String dockerRootPath = "dockerTestDir"; // arbitrary relative path
            dockerRoot = new File(dockerRootPath);
            dockerRoot.mkdirs();
        }
        return dockerRoot;
    }

}
