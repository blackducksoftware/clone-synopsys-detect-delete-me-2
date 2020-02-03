/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.battery.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.battery.BatteryTest;
import com.synopsys.integration.detect.configuration.DetectProperty;

@Tag("battery")
public class MavenBattery {
    private static final String MAVEN_OUTPUT_RESOURCE = "maven-dependencytree.xout";

    @Test
    void property() {
        final BatteryTest test = sharedCliTest("maven-property");
        test.executableFromResourceFiles(DetectProperty.DETECT_MAVEN_PATH, MAVEN_OUTPUT_RESOURCE);
        test.run();
    }

    @Test
    void wrapper() {
        final BatteryTest test = sharedCliTest("maven-wrapper");
        test.executableSourceFileFromResourceFiles("mvnw.cmd", "mvnw", MAVEN_OUTPUT_RESOURCE);
        test.run();
    }

    BatteryTest sharedCliTest(final String name) {
        final BatteryTest test = new BatteryTest(name, "maven-cli");
        test.sourceDirectoryNamed("linux-maven");
        test.sourceFileNamed("pom.xml");
        test.git("https://github.com/BlackDuckCoPilot/example-maven-travis", "master");
        test.expectBdioResources();
        return test;
    }
}

