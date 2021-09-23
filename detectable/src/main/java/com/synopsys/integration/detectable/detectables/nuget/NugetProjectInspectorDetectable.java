/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.nuget;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.Requirements;
import com.synopsys.integration.detectable.detectable.annotation.DetectableInfo;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.detectable.inspector.ProjectInspectorResolver;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorOptions;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detectable.detectables.projectinspector.ProjectInspectorExtractor;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.extraction.ExtractionEnvironment;

@DetectableInfo(language = "C#", forge = "NuGet.org", requirementsMarkdown = "File: a project file with one of the following extensions: .csproj, .sln")
public class NugetProjectInspectorDetectable extends Detectable {
    static final List<String> SUPPORTED_PROJECT_PATTERNS = Arrays.asList("*.csproj", "*.sln");

    private final FileFinder fileFinder;
    private final NugetInspectorOptions nugetInspectorOptions;
    private final ProjectInspectorResolver projectInspectorResolver;
    private final ProjectInspectorExtractor projectInspectorExtractor;

    private ExecutableTarget inspector;

    public NugetProjectInspectorDetectable(final DetectableEnvironment detectableEnvironment, final FileFinder fileFinder, final NugetInspectorOptions nugetInspectorOptions,
        ProjectInspectorResolver projectInspectorResolver, ProjectInspectorExtractor projectInspectorExtractor) {
        super(detectableEnvironment);
        this.fileFinder = fileFinder;
        this.nugetInspectorOptions = nugetInspectorOptions;
        this.projectInspectorResolver = projectInspectorResolver;
        this.projectInspectorExtractor = projectInspectorExtractor;
    }

    @Override
    public DetectableResult applicable() {
        Requirements requirements = new Requirements(fileFinder, environment);
        requirements.anyFileMatchesPatterns(SUPPORTED_PROJECT_PATTERNS);
        return requirements.result();
    }

    @Override
    public DetectableResult extractable() throws DetectableException {
        Requirements requirements = new Requirements(fileFinder, environment);
        inspector = requirements.executable(projectInspectorResolver::resolveProjectInspector, "Project Inspector");
        return requirements.result();
    }

    @Override
    public Extraction extract(final ExtractionEnvironment extractionEnvironment) throws ExecutableFailedException {
        List<String> arguments = Collections.emptyList();
        return projectInspectorExtractor.extract(arguments, environment.getDirectory(), extractionEnvironment.getOutputDirectory(), inspector);
    }

}