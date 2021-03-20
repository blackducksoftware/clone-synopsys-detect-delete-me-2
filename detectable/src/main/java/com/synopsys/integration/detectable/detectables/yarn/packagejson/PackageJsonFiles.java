/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.yarn.packagejson;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectables.npm.packagejson.model.PackageJson;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockDetectable;
import com.synopsys.integration.detectable.detectables.yarn.workspace.Workspace;

public class PackageJsonFiles {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final PackageJsonReader packageJsonReader;

    public PackageJsonFiles(PackageJsonReader packageJsonReader) {
        this.packageJsonReader = packageJsonReader;
    }

    public PackageJson read(File packageJsonFile) throws IOException {
        logger.info("Reading package.json file: {}", packageJsonFile.getAbsolutePath());
        String packageJsonText = FileUtils.readFileToString(packageJsonFile, StandardCharsets.UTF_8);
        return packageJsonReader.read(packageJsonText);
    }

    @NotNull
    public Map<String, Workspace> readWorkspacePackageJsonFiles(File workspaceDir) throws IOException {
        File packageJsonFile = new File(workspaceDir, YarnLockDetectable.YARN_PACKAGE_JSON);
        List<String> workspaceDirPatterns = extractWorkspaceDirPatterns(packageJsonFile);

        Map<String, Workspace> workspacesByName = new HashMap<>();
        for (String workspaceSubdirPattern : workspaceDirPatterns) {
            logger.info("workspaceSubdirPattern: {}", workspaceSubdirPattern);
            String globString = String.format("glob:%s/%s/package.json", workspaceDir.getAbsolutePath(), workspaceSubdirPattern);
            logger.info("workspace subdir globString: {}", globString);
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globString);
            Files.walkFileTree(workspaceDir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) {
                        logger.info("\tFound a match: {}", file);
                        PackageJson packageJson = read(file.toFile());
                        Path rel = workspaceDir.toPath().relativize(file.getParent());
                        WorkspacePackageJson workspacePackageJson = new WorkspacePackageJson(file.toFile(), packageJson, rel.toString());
                        Workspace workspace = new Workspace(workspacePackageJson);
                        workspacesByName.put(packageJson.name, workspace);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        logger.info("Found {} matching workspace package.json files", workspacesByName.size());
        return workspacesByName;
    }

    @NotNull
    private List<String> extractWorkspaceDirPatterns(File packageJsonFile) throws IOException {
        String packageJsonText = FileUtils.readFileToString(packageJsonFile, StandardCharsets.UTF_8);
        return packageJsonReader.extractWorkspaceDirPatterns(packageJsonText);
    }
}
