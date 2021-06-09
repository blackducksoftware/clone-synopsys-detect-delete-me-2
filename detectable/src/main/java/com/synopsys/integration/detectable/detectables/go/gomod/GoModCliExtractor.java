/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.go.gomod;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.extraction.Extraction;

public class GoModCliExtractor {
    private final GoModCommandExecutor goModCommandExecutor;
    private final GoModGraphTransformer goModGraphTransformer;
    private final GoModGraphParser goModGraphParser;
    private final GoModWhyParser goModWhyParser;

    public GoModCliExtractor(GoModCommandExecutor executor, GoModGraphParser goModGraphParser, GoModGraphTransformer goModGraphTransformer, GoModWhyParser goModWhyParser) {
        this.goModGraphParser = goModGraphParser;
        this.goModWhyParser = goModWhyParser;
        this.goModCommandExecutor = executor;
        this.goModGraphTransformer = goModGraphTransformer;
    }

    public Extraction extract(File directory, ExecutableTarget goExe, boolean dependencyVerificationEnabled) {
        try {
            List<String> listOutput = goModCommandExecutor.generateGoListOutput(directory, goExe);
            List<String> listUJsonOutput = goModCommandExecutor.generateGoListUJsonOutput(directory, goExe);
            List<String> modGraphOutput = goModCommandExecutor.generateGoModGraphOutput(directory, goExe);
            Set<String> moduleExclusionList = Collections.emptySet();
            if (dependencyVerificationEnabled) {
                List<String> modWhyOutput = goModCommandExecutor.generateGoModWhyOutput(directory, goExe);
                moduleExclusionList = goModWhyParser.createModuleExclusionList(modWhyOutput);
            }
            List<String> finalModGraphOutput = goModGraphTransformer.transformGoModGraphOutput(modGraphOutput, listUJsonOutput);
            List<CodeLocation> codeLocations = goModGraphParser.parseListAndGoModGraph(listOutput, finalModGraphOutput, moduleExclusionList);
            return new Extraction.Builder().success(codeLocations).build();//no project info - hoping git can help with that.
        } catch (Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
