/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.conan.lockfile;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.extraction.Extraction;

public class ConanLockfileExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // TODO can we use this?
    //private ExternalIdFactory externalIdFactory;
    private final Gson gson;

    public ConanLockfileExtractor(Gson gson /*, final ExternalIdFactory externalIdFactory */) {
        this.gson = gson;
        // this.externalIdFactory = externalIdFactory;
    }

    public Extraction extract(File lockfile) {
        try {
            ConanLockfileParser conanLockfileParser = new ConanLockfileParser();
            String conanLockfileContents = FileUtils.readFileToString(lockfile, StandardCharsets.UTF_8);
            logger.debug(conanLockfileContents);

            DependencyGraph dependencyGraph = conanLockfileParser.parse(gson, conanLockfileContents);
            CodeLocation codeLocation = new CodeLocation(dependencyGraph);
            return new Extraction.Builder().success(codeLocation).build();
        } catch (Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}