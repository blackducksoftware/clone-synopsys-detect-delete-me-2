package com.synopsys.integration.detect.battery.detector;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.battery.util.DetectorBatteryTestRunner;
import com.synopsys.integration.detect.configuration.DetectProperties;

@Tag("battery")
public class BitbakeBattery {

    @Test
    void test() {
        DetectorBatteryTestRunner test = new DetectorBatteryTestRunner("bitbake-orig-poky", "bitbake/orig/poky");
        test.sourceFileFromResource("oe-init-build-env");
        test.sourceFileFromResource("task-depends.dot");
        test.executableFromResourceFiles(DetectProperties.DETECT_BASH_PATH.getProperty(), "bitbake-g.xout");
        // TODO need at least one more command executor, right?
        test.property("detect.bitbake.package.names", "core-image-sato");
        test.expectBdioResources();
        test.run();
    }
}
