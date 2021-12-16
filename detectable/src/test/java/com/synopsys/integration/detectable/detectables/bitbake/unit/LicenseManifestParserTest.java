package com.synopsys.integration.detectable.detectables.bitbake.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.LicenseManifestParser;
import com.synopsys.integration.exception.IntegrationException;

public class LicenseManifestParserTest {

    @Test
    void test() throws IntegrationException {
        LicenseManifestParser parser = new LicenseManifestParser();

        List<String> licenseManifestFileLines = new ArrayList<>();
        licenseManifestFileLines.add("PACKAGE NAME: adwaita-icon-theme");
        licenseManifestFileLines.add("PACKAGE VERSION: 3.34.3");
        licenseManifestFileLines.add("RECIPE NAME: adwaita-icon-theme");
        licenseManifestFileLines.add("LICENSE: LGPL-3.0 | CC-BY-SA-3.0");
        licenseManifestFileLines.add("");
        licenseManifestFileLines.add("PACKAGE NAME: adwaita-icon-theme-symbolic");
        licenseManifestFileLines.add("PACKAGE VERSION: 3.34.3");
        licenseManifestFileLines.add("RECIPE NAME: adwaita-icon-theme");
        licenseManifestFileLines.add("LICENSE: LGPL-3.0 | CC-BY-SA-3.0");
        licenseManifestFileLines.add("");
        licenseManifestFileLines.add("PACKAGE NAME: alsa-conf");
        licenseManifestFileLines.add("PACKAGE VERSION: 1.2.5.1");
        licenseManifestFileLines.add("RECIPE NAME: alsa-lib");
        licenseManifestFileLines.add("LICENSE: LGPLv2.1 & GPLv2+");
        licenseManifestFileLines.add("");
        Map<String, String> imageRecipes = parser.collectImageRecipes(licenseManifestFileLines);
        assertEquals(2, imageRecipes.keySet().size());
        assertEquals("3.34.3", imageRecipes.get("adwaita-icon-theme"));
        assertEquals("1.2.5.1", imageRecipes.get("alsa-lib"));
    }
}
