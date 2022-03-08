package com.synopsys.integration.detectable.detectables.bitbake.functional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.synopsys.integration.common.util.finder.SimpleFileFinder;
import com.synopsys.integration.detectable.annotations.FunctionalTest;
import com.synopsys.integration.detectable.detectables.bitbake.collect.BuildFileFinder;
import com.synopsys.integration.detectable.detectables.bitbake.data.BitbakeEnvironment;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;

@FunctionalTest
class BuildFileFinderTest {
    private final BuildFileFinder finder = new BuildFileFinder(new SimpleFileFinder(), true, 10);

    @ParameterizedTest
    @ValueSource(strings = { "/bitbake/builddir_default", "/bitbake/builddir_custom" })
    void testFindingInDefaultAndCustom(String directoryPath) {
        File buildDir = FunctionalTestFiles.asFile(directoryPath);
        BitbakeEnvironment bitbakeEnvironment = new BitbakeEnvironment(null, null);

        Optional<File> licensesManifestFile = finder.findLicenseManifestFile(buildDir, "targetimage", bitbakeEnvironment);

        assertTrue(licensesManifestFile.isPresent());
        assertTrue(licensesManifestFile.get().isFile());
    }

    @Test
    void testFindingBasedOnArchitecture() {
        File buildDir = FunctionalTestFiles.asFile("/bitbake/builddir_arch");
        BitbakeEnvironment bitbakeEnvironment = new BitbakeEnvironment("testarch", null); // This test adds architecture.

        Optional<File> licensesManifestFile = finder.findLicenseManifestFile(buildDir, "targetimage", bitbakeEnvironment);

        assertTrue(licensesManifestFile.isPresent());
        assertTrue(licensesManifestFile.get().isFile());
    }

    @Test
    void testFindingBasedOnLicenseDir() {
        File buildDir = FunctionalTestFiles.asFile("/bitbake/builddir_env");
        File licenseDir = FunctionalTestFiles.asFile("/bitbake/builddir_env/envprovidedpath/licenses");
        BitbakeEnvironment bitbakeEnvironment = new BitbakeEnvironment(null, licenseDir.getAbsolutePath()); // This test adds license directory

        Optional<File> licensesManifestFile = finder.findLicenseManifestFile(buildDir, "targetimage", bitbakeEnvironment);

        assertTrue(licensesManifestFile.isPresent());
        assertTrue(licensesManifestFile.get().isFile());
        assertEquals(licenseDir.getAbsolutePath() + "/targetimage-last-modified-architecture/license.manifest", licensesManifestFile.get().getAbsolutePath());
    }
}
