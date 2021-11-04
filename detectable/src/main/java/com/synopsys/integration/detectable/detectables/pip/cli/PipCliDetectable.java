/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.pip.cli;

import java.io.File;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.PassedResultBuilder;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.resolver.PipResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.PythonResolver;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.FileNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectables.pipenv.PipenvDetectableOptions;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "Python", forge = "PyPi", requirementsMarkdown = "Files: Pipfile or Pipfile.lock.<br/><br/>Executables: python or python3, and pipenv.")
public class PipCliDetectable extends Detectable {
    public static final String SETUPTOOLS_DEFAULT_FILE_NAME = "setup.py";

    private final PipenvDetectableOptions pipenvDetectableOptions;
    private final FileFinder fileFinder;
    private final PythonResolver pythonResolver;
    private final PipResolver pipResolver;
    private final PipCliExtractor pipCliExtractor;

    private ExecutableTarget pythonExe;
    private ExecutableTarget pipExe;
    private File setupFile;

    public PipCliDetectable(DetectableEnvironment environment, PipenvDetectableOptions pipenvDetectableOptions, FileFinder fileFinder, PythonResolver pythonResolver, PipResolver pipResolver, PipCliExtractor pipCliExtractor) {
        super(environment);
        this.pipenvDetectableOptions = pipenvDetectableOptions;
        this.fileFinder = fileFinder;
        this.pipResolver = pipResolver;
        this.pipCliExtractor = pipCliExtractor;
        this.pythonResolver = pythonResolver;
    }

    @Override
    public DetectableResult applicable() {
        File setupToolsFile = fileFinder.findFile(environment.getDirectory(), SETUPTOOLS_DEFAULT_FILE_NAME);

        if (setupToolsFile != null) {
            PassedResultBuilder passedResultBuilder = new PassedResultBuilder();
            passedResultBuilder.foundNullableFile(setupToolsFile);
            return passedResultBuilder.build();
        } else {
            return new FileNotFoundDetectableResult(SETUPTOOLS_DEFAULT_FILE_NAME);
        }

    }

    @Override
    public DetectableResult extractable() throws DetectableException {
        Requirements requirements = new Requirements(fileFinder, environment);
        pythonExe = requirements.executable(pythonResolver::resolvePython, "python");
        pipExe = requirements.executable(pipResolver::resolvePip, "pip");

        setupFile = fileFinder.findFile(environment.getDirectory(), SETUPTOOLS_DEFAULT_FILE_NAME);
        requirements.explainNullableFile(setupFile);

        return requirements.result();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return pipCliExtractor.extract(
            environment.getDirectory(),
            pythonExe,
            pipExe,
            setupFile,
            pipenvDetectableOptions.getPipProjectName().orElse(null),
            pipenvDetectableOptions.getPipProjectVersionName().orElse(null)
        );
    }

}

