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
package com.synopsys.integration.detectable.detectables.cargo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.ExtractionEnvironment;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.FilesNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.GoDepRunInitEnsureDetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;

@DetectableInfo(language = "Rust", forge = "crates", requirementsMarkdown = "File: Cargo.lock")
public class CargoDetectable extends Detectable {
    public static final String CARGO_LOCK_FILENAME = "Cargo.lock";
    public static final String CARGO_TOML_FILENAME = "Cargo.toml";

    private final FileFinder fileFinder;
    private final CargoExtractor cargoExtractor;

    private File cargoLock;
    private File cargoToml;

    public CargoDetectable(final DetectableEnvironment environment, final FileFinder fileFinder, final CargoExtractor cargoExtractor) {
        super(environment);
        this.fileFinder = fileFinder;
        this.cargoExtractor = cargoExtractor;
    }

    @Override
    public DetectableResult applicable() {
        cargoLock = fileFinder.findFile(environment.getDirectory(), CARGO_LOCK_FILENAME);
        if (cargoLock == null) {
            cargoToml = fileFinder.findFile(environment.getDirectory(), CARGO_TOML_FILENAME);
            if (cargoToml == null) {
                return new FilesNotFoundDetectableResult(CARGO_LOCK_FILENAME, CARGO_TOML_FILENAME);
            }
        }
        return new PassedDetectableResult();
    }

    @Override
    public DetectableResult extractable() {
        if (cargoLock == null && cargoToml != null) {
            return new GoDepRunInitEnsureDetectableResult(environment.getDirectory().getAbsolutePath());
        }
        return new PassedDetectableResult();
    }

    @Override
    public Extraction extract(final ExtractionEnvironment extractionEnvironment) {
        try (final InputStream inputStream = new FileInputStream(cargoLock)) {
            return cargoExtractor.extract(inputStream);
        } catch (final IOException e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
