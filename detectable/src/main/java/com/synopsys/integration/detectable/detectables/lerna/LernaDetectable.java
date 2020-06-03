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
package com.synopsys.integration.detectable.detectables.lerna;

import static com.synopsys.integration.detectable.detectables.lerna.LernaDetectable.PACKAGE_JSON;
import static com.synopsys.integration.detectable.detectables.lerna.LernaDetectable.PACKAGE_LOCK_JSON;
import static com.synopsys.integration.detectable.detectables.lerna.LernaDetectable.SHRINKWRAP_JSON;
import static com.synopsys.integration.detectable.detectables.lerna.LernaDetectable.YARN_LOCK;

import java.io.File;

import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.ExtractionEnvironment;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.resolver.LernaResolver;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.ExecutableNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.FileNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.FilesNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmPackageLockDetectable;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmShrinkwrapDetectable;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockDetectable;

public class LernaDetectable extends Detectable {
    public static final String LERNA_JSON = "lerna.json";
    public static final String PACKAGE_JSON = NpmPackageLockDetectable.PACKAGE_JSON;
    public static final String PACKAGE_LOCK_JSON = NpmPackageLockDetectable.PACKAGE_LOCK_JSON;
    public static final String SHRINKWRAP_JSON = NpmShrinkwrapDetectable.SHRINKWRAP_JSON;
    public static final String YARN_LOCK = YarnLockDetectable.YARN_LOCK_FILENAME;

    private final FileFinder fileFinder;
    private final LernaResolver lernaResolver;
    private final LernaExtractor lernaExtractor;

    private File lernaExecutable;

    public LernaDetectable(DetectableEnvironment environment, FileFinder fileFinder, LernaResolver lernaResolver, LernaExtractor lernaExtractor) {
        super(environment);
        this.fileFinder = fileFinder;
        this.lernaResolver = lernaResolver;
        this.lernaExtractor = lernaExtractor;
    }

    @Override
    public DetectableResult applicable() {
        File lernaJsonFile = fileFinder.findFile(environment.getDirectory(), LERNA_JSON);

        if (lernaJsonFile == null) {
            return new FileNotFoundDetectableResult(LERNA_JSON);
        }

        return new PassedDetectableResult();
    }

    @Override
    public DetectableResult extractable() throws DetectableException {
        // Lerna is used in conjunction with traditional NPM projects or Yarn projects.
        File packageLockFile = fileFinder.findFile(environment.getDirectory(), PACKAGE_LOCK_JSON);
        File shrinkwrapFile = fileFinder.findFile(environment.getDirectory(), SHRINKWRAP_JSON);
        File yarnLockFile = fileFinder.findFile(environment.getDirectory(), YARN_LOCK);
        if (packageLockFile == null && shrinkwrapFile == null && yarnLockFile == null) {
            return new FilesNotFoundDetectableResult(PACKAGE_LOCK_JSON, YARN_LOCK);
        }

        lernaExecutable = lernaResolver.resolveLerna();
        if (lernaExecutable == null) {
            return new ExecutableNotFoundDetectableResult("lerna");
        }

        return new PassedDetectableResult();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return lernaExtractor.extract(environment, lernaExecutable);
    }
}
