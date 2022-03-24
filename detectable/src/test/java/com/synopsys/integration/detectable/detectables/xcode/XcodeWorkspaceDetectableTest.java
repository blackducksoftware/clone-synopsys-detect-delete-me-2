/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detectable.detectables.xcode;

import java.io.IOException;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.functional.DetectableFunctionalTest;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

// TODO: Run this test. Thinking about 1 Extraction
public class XcodeWorkspaceDetectableTest extends DetectableFunctionalTest {
    public XcodeWorkspaceDetectableTest() throws IOException {
        super("Xcode Workspace - Package.resolved & ");
    }

    @Override
    public void setup() throws IOException {
        // Using the same file to simplify assertions
        String packageResolvedResourcePath = "/xcode/Package.resolved";

        // Within the workspace directory
        addFileFromResources(Paths.get("jake-test.xcworkspace/xcshareddata/swiftpm/Package.resolved"), packageResolvedResourcePath);

        // Defined in workspace data file
        addFileFromResources(Paths.get("project/MyLibrary/Package.resolved"), packageResolvedResourcePath);
        addFileFromResources(Paths.get("project/jakem-test.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved"), packageResolvedResourcePath);

        addFile(
            Paths.get("jake-test.xcworkspace/contents.xcworkspacedata"),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<Workspace",
            "   version = \"1.0\">",
            "   <FileRef",
            "      location = \"group:project/MyLibrary\">",
            "   </FileRef>",
            "   <FileRef",
            "      location = \"group:project/jakem-test.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved\">",
            "   </FileRef>",
            "</Workspace>"
        );
    }

    @NotNull
    @Override
    public Detectable create(@NotNull DetectableEnvironment environment) {
        return detectableFactory.createXcodeWorkspaceDetectable(environment);
    }

    @Override
    public void assertExtraction(@NotNull Extraction extraction) {
        Assertions.assertEquals(3, extraction.getCodeLocations().size(), "Expected 1 code location from local and 2 defined in the workspace data file.");

        CodeLocation localCodeLocation = extraction.getCodeLocations().get(0);
        assertPackageResolvedGraph(localCodeLocation.getDependencyGraph());

        CodeLocation directoryCodeLocation = extraction.getCodeLocations().get(1);
        assertPackageResolvedGraph(directoryCodeLocation.getDependencyGraph());

        CodeLocation projectCodeLocation = extraction.getCodeLocations().get(2);
        assertPackageResolvedGraph(projectCodeLocation.getDependencyGraph());
    }

    private void assertPackageResolvedGraph(DependencyGraph packageResolvedDependencyGraph) {
        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.GITHUB, packageResolvedDependencyGraph);
        graphAssert.hasRootDependency("apple/swift-argument-parser", "1.0.1");
        graphAssert.hasRootDependency("auth0/Auth0.swift", "1.35.0");
        graphAssert.hasRootDependency("mac-cain13/R.swift.Library", "5.4.0");

        graphAssert.hasNoDependency("invalid/url", "1.2.3");
        graphAssert.hasRootSize(3);
    }
}