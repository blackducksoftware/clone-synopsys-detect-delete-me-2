package com.synopsys.integration.detect.battery.docker.provider;

import java.io.File;

import org.junit.jupiter.api.Assertions;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.detect.battery.util.DetectorBatteryTest;
import com.synopsys.integration.detect.commontest.FileUtil;

public class BuildDockerImageProvider implements DockerImageProvider {
    private final String dockerfileResourceName;

    public BuildDockerImageProvider(final String dockerfileResourceName) {
        this.dockerfileResourceName = dockerfileResourceName;
    }

    public static BuildDockerImageProvider forDockerfilResourceNamed(String dockerfileResourceName) {
        return new BuildDockerImageProvider(dockerfileResourceName);
    }

    @Override
    public void installImage(String imageName, final DockerClient dockerClient) {
        File imageDockerFile = FileUtil.asFile(DetectorBatteryTest.class, dockerfileResourceName, "/docker/");
        Assertions.assertNotNull(imageDockerFile, "Could not find the dockerfile in the resources, ensure the dockerfile exists as named. It is needed to build the image if the image is not present.");

        dockerClient.buildImageCmd(imageDockerFile)
            .withTags(Bds.of(imageName).toSet())
            .exec(new BuildImageResultCallback())
            .awaitImageId();
    }
}
