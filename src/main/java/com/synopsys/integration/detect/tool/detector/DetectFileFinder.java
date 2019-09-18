/**
 * synopsys-detect
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.detect.tool.detector;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.synopsys.integration.detectable.detectable.file.impl.SimpleFileFinder;

public class DetectFileFinder extends SimpleFileFinder {
    private final List<String> excludedFileNames;

    public DetectFileFinder(final String[] excludedFileNames) {
        this(Arrays.asList(excludedFileNames));
    }

    public DetectFileFinder(final List<String> excludedFileNames) {
        this.excludedFileNames = excludedFileNames;
    }

    @Override
    public List<File> findFiles(final File directoryToSearch, final List<String> filenamePatterns, final int depth, boolean findInsideMatchingDirectories) {
        return super.findFiles(directoryToSearch, filenamePatterns, depth, findInsideMatchingDirectories).stream()
                   .filter(file -> !excludedFileNames.contains(file.getName()))
                   .collect(Collectors.toList());
    }
}
