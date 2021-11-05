/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.poetry;

import java.io.File;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectable.result.FilesNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.PassedDetectableResult;
import com.synopsys.integration.detectable.detectable.result.PoetryLockfileNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectable.result.SectionNotFoundDetectableResult;
import com.synopsys.integration.detectable.detectables.poetry.parser.ToolPoetrySectionParser;
import com.synopsys.integration.detectable.detectables.poetry.parser.ToolPoetrySectionResult;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "Python", forge = "pypi", requirementsMarkdown = "Files: Poetry.lock, pyproject.toml")
public class PoetryDetectable extends Detectable {
    private static final String PYPROJECT_TOML_FILE_NAME = "pyproject.toml";
    private static final String POETRY_LOCK = "poetry.lock";

    private final FileFinder fileFinder;
    private final PoetryExtractor poetryExtractor;
    private final ToolPoetrySectionParser poetrySectionParser;

    private File pyprojectToml;
    private File poetryLock;
    private ToolPoetrySectionResult toolPoetrySectionResult;

    public PoetryDetectable(DetectableEnvironment environment, FileFinder fileFinder, PoetryExtractor poetryExtractor, ToolPoetrySectionParser tomlPoetrySectionParser) {
        super(environment);
        this.fileFinder = fileFinder;
        this.poetryExtractor = poetryExtractor;
        this.toolPoetrySectionResult = null;
        this.poetrySectionParser = tomlPoetrySectionParser;
    }

    @Override
    public DetectableResult applicable() {
        poetryLock = fileFinder.findFile(environment.getDirectory(), POETRY_LOCK);
        pyprojectToml = fileFinder.findFile(environment.getDirectory(), PYPROJECT_TOML_FILE_NAME);
        if (poetryLock == null && pyprojectToml == null) {
            return new FilesNotFoundDetectableResult(PYPROJECT_TOML_FILE_NAME, POETRY_LOCK);
        }

        toolPoetrySectionResult = poetrySectionParser.parseToolPoetrySection(pyprojectToml);

        if (poetryLock == null && !toolPoetrySectionResult.wasFound()) {
            return new SectionNotFoundDetectableResult(pyprojectToml.getName(), ToolPoetrySectionParser.TOOL_POETRY_KEY);
        }
        return new PassedDetectableResult();
    }

    @Override
    public DetectableResult extractable() {
        if (poetryLock == null && pyprojectToml != null) {
            return new PoetryLockfileNotFoundDetectableResult(environment.getDirectory().getAbsolutePath());
        }
        return new PassedDetectableResult();
    }

    @Override
    public Extraction extract(ExtractionEnvironment extractionEnvironment) {
        return poetryExtractor.extract(poetryLock, toolPoetrySectionResult.getToolPoetrySection().orElse(null));
    }
}
