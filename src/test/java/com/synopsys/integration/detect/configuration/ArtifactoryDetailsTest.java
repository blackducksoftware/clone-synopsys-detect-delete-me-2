package com.synopsys.integration.detect.configuration;

import java.io.IOException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import com.google.gson.Gson;
import com.synopsys.integration.detect.workflow.ArtifactoryDetails;

public class ArtifactoryDetailsTest {

    @Test
    public void verifyLoadsFromResources() throws IOException {
        ArtifactoryDetails artifactoryDetails = ArtifactoryDetails.fromResources(new Gson());
        Assertions.assertNotNull(artifactoryDetails.artifactoryUrl);

        Assertions.assertNotNull(artifactoryDetails.gradleInspectorRepo);
        Assertions.assertNotNull(artifactoryDetails.gradleInspectorProperty);
        Assertions.assertNotNull(artifactoryDetails.gradleInspectorMavenRepo);

        Assertions.assertNotNull(artifactoryDetails.nugetDotnet3InspectorRepo);
        Assertions.assertNotNull(artifactoryDetails.nugetDotnet3InspectorProperty);
        Assertions.assertNotNull(artifactoryDetails.nugetDotnet3InspectorVersionOverride);

        Assertions.assertNotNull(artifactoryDetails.nugetInspectorRepo);
        Assertions.assertNotNull(artifactoryDetails.nugetInspectorProperty);
        Assertions.assertNotNull(artifactoryDetails.nugetInspectorVersionOverride);

        Assertions.assertNotNull(artifactoryDetails.classicNugetInspectorRepo);
        Assertions.assertNotNull(artifactoryDetails.classicNugetInspectorProperty);
        Assertions.assertNotNull(artifactoryDetails.classicNugetInspectorVersionOverride);

        Assertions.assertNotNull(artifactoryDetails.dockerInspectorRepo);
        Assertions.assertNotNull(artifactoryDetails.dockerInspectorProperty);
        Assertions.assertNotNull(artifactoryDetails.dockerInspectorAirGapProperty);
        Assertions.assertNotNull(artifactoryDetails.dockerInspectorVersionOverride);
    }

}
