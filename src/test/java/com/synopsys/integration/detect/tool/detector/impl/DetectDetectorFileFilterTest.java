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
package com.synopsys.integration.detect.tool.detector.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.tool.detector.file.DetectDetectorFileFilter;

class DetectDetectorFileFilterTest {
    @Test
    void testIsExcludedDirectories() {
        Path sourcePath = new File("my/file/path").toPath();
        List<String> excludedDirectories = Arrays.asList("root", "root2");
        List<String> excludedDirectoryPaths = new ArrayList<>();
        List<String> excludedDirectoryNamePatterns = new ArrayList<>();
        List<String> detectIgnored = new ArrayList<>();
        DetectDetectorFileFilter detectDetectorFileFilter = new DetectDetectorFileFilter(sourcePath, detectIgnored, excludedDirectories, excludedDirectoryPaths, excludedDirectoryNamePatterns);

        File root = new File(sourcePath.toFile(), "root");
        File root2 = new File(sourcePath.toFile(), "root2");
        File doNotExcludeDir = new File(root, "doNotExclude");

        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(root));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(root2));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(doNotExcludeDir));
    }

    @Test
    void testIsExcludedDirectoryPaths() {
        Path sourcePath = new File("my/subDir1/subDir2/file/path").toPath();
        List<String> excludedDirectories = new ArrayList<>();
        List<String> excludedDirectoryPaths = Collections.singletonList("subDir1/subDir2");
        List<String> excludedDirectoryNamePatterns = new ArrayList<>();
        List<String> detectIgnored = new ArrayList<>();
        DetectDetectorFileFilter detectDetectorFileFilter = new DetectDetectorFileFilter(sourcePath, detectIgnored, excludedDirectories, excludedDirectoryPaths, excludedDirectoryNamePatterns);

        File root = new File("path/to/root");
        File subDir1 = new File(root, "subDir1");
        File subDir2 = new File(root, "subDir2");
        File deepSubDir2 = new File(subDir1, "subDir2");

        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(root));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(subDir1));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(subDir2));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(deepSubDir2));
    }

    @Test
    void testIsExcludedDirectoryNamePatterns() {
        Path sourcePath = new File("my/subDir1/subDir2/file/path").toPath();
        List<String> excludedDirectories = new ArrayList<>();
        List<String> excludedDirectoryPaths = new ArrayList<>();
        List<String> excludedDirectoryNamePatterns = Arrays.asList("*1", "namePatternsDir*");
        List<String> detectIgnored = new ArrayList<>();
        DetectDetectorFileFilter detectDetectorFileFilter = new DetectDetectorFileFilter(sourcePath, detectIgnored, excludedDirectories, excludedDirectoryPaths, excludedDirectoryNamePatterns);

        File root = new File(sourcePath.toFile(), "root");
        File subDir1 = new File(root, "subDir1");
        File subDir2 = new File(root, "subDir2");
        File deepSubDir2 = new File(subDir1, "subDir2");
        File namePatternsDir = new File(root, "namePatternsDir51134");

        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(root));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(subDir1));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(subDir2));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(deepSubDir2));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(namePatternsDir));
    }

    @Test
    void testIsExcludedDetectIgnore() {
        Path sourcePath = new File("my/file/path").toPath();
        List<String> excludedDirectories = new ArrayList<>();
        List<String> excludedDirectoryPaths = new ArrayList<>();
        List<String> excludedDirectoryNamePatterns = new ArrayList<>();
        List<String> detectIgnored = Arrays.asList("*1", "*subDir2*");
        DetectDetectorFileFilter detectDetectorFileFilter = new DetectDetectorFileFilter(sourcePath, detectIgnored, excludedDirectories, excludedDirectoryPaths, excludedDirectoryNamePatterns);

        File root = new File(sourcePath.toFile(), "root");
        File subDir1 = new File(root, "subDir1");
        File subDir2 = new File(root, "subDir2");
        File subDir2File = new File(subDir2, "file.txt");
        File deepSubDir2 = new File(subDir1, "subDir2");
        File namePatternsDir = new File(root, "namePatternsDir51134");

        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(root));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(subDir1));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(subDir2));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(subDir2File));
        Assertions.assertTrue(detectDetectorFileFilter.isExcluded(deepSubDir2));
        Assertions.assertFalse(detectDetectorFileFilter.isExcluded(namePatternsDir));
    }
}