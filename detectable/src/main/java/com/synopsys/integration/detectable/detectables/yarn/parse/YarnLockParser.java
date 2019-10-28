/**
 * detectable
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
package com.synopsys.integration.detectable.detectables.yarn.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnLockParser {
    private static final String COMMENT_PREFIX = "#";
    private static final String VERSION_PREFIX = "version \"";
    private static final String VERSION_SUFFIX = "\"";
    private static final String DEPENDENCIES_START_TOKEN = "dependencies:";
    private static final String OPTIONAL_DEPENDENCIES_START_TOKEN = "optionalDependencies:";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final YarnLineLevelParser lineLevelParser;

    public YarnLockParser(final YarnLineLevelParser lineLevelParser) {
        this.lineLevelParser = lineLevelParser;
    }

    public YarnLock parseYarnLock(final List<String> yarnLockFileAsList) {
        final Map<String, YarnLock.Entry> yarnLockResolvedVersions = new HashMap<>();

        boolean firstEntry = true;
        String name = null;
        String resolvedVersion = null;
        final List<String> fuzzyIds = new ArrayList<>();
        final List<YarnLock.Entry.Dependency> fuzzyDependencies = new ArrayList<>();
        boolean inDependencies = false;
        boolean inOptionalDependencies = false;

        for (final String line : yarnLockFileAsList) {
            if (StringUtils.isBlank(line) || line.trim().startsWith(COMMENT_PREFIX)) {
                continue;
            }

            final String trimmedLine = line.trim();
            final int level = lineLevelParser.parseIndentLevel(line);
            if (level == 0) {
                if (firstEntry) {
                    firstEntry = false;
                } else {
                    if (name != null) {
                        final YarnLock.Entry yarnLockEntry = new YarnLock.Entry(name, resolvedVersion, fuzzyDependencies);
                        fuzzyIds.forEach(fuzzyId -> yarnLockResolvedVersions.put(fuzzyId, yarnLockEntry));
                    } else {
                        logger.warn(String.format("Failed to extract dependency name from fuzzy IDs: %s", StringUtils.join(fuzzyIds, ", ")));
                    }

                    resolvedVersion = null;
                    fuzzyIds.clear();
                    fuzzyDependencies.clear();
                    inDependencies = false;
                    inOptionalDependencies = false;
                }

                final List<String> fuzzyIdsFromLine = getFuzzyIdsFromLine(line);
                name = fuzzyIdsFromLine.stream()
                           .findFirst()
                           .map(this::parseName)
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .orElse(null);
                fuzzyIds.addAll(fuzzyIdsFromLine);
            } else if (level == 1 && trimmedLine.startsWith(VERSION_PREFIX)) {
                resolvedVersion = trimmedLine.substring(VERSION_PREFIX.length(), trimmedLine.lastIndexOf(VERSION_SUFFIX));
            } else if (level == 1 && trimmedLine.startsWith(DEPENDENCIES_START_TOKEN)) {
                inDependencies = true;
                inOptionalDependencies = false;
            } else if (level == 1 && trimmedLine.startsWith(OPTIONAL_DEPENDENCIES_START_TOKEN)) {
                inDependencies = false;
                inOptionalDependencies = true;
            } else if (inDependencies && level == 2) {
                final Optional<YarnLock.Entry.Dependency> yarnDependency = getYarnDependencyFromLine(trimmedLine);
                if (yarnDependency.isPresent()) {
                    fuzzyDependencies.add(yarnDependency.get());
                } else {
                    logger.warn(String.format("Failed to extract the correct number of dependency pieces from line '%s'", line));
                }
            } else if (inOptionalDependencies && level == 2) {
                // TODO: Handle optional dependencies
            }
        }

        if (resolvedVersion != null) {
            final YarnLock.Entry yarnLockEntry = new YarnLock.Entry(name, resolvedVersion, fuzzyDependencies);
            fuzzyIds.forEach(fuzzyId -> yarnLockResolvedVersions.put(fuzzyId, yarnLockEntry));
        }

        return new YarnLock(yarnLockResolvedVersions);
    }

    private Optional<String> parseName(final String fuzzyId) {
        String cleanedFuzzyNameVersionString = fuzzyId;
        if (fuzzyId.startsWith("@")) {
            cleanedFuzzyNameVersionString = fuzzyId.substring(1);
        }

        final String name;
        final String[] pieces = cleanedFuzzyNameVersionString.split("@");
        if (pieces.length == 1 || pieces.length == 2) {
            name = pieces[0];
        } else {
            logger.warn(String.format("Unknown yarn list dependency format: %s", fuzzyId));
            name = null;
        }

        return Optional.ofNullable(name);
    }

    // "@foo/bar@^1", "@foo/bar@^1.3.0": -> List<String>["@foo/bar@^1", "@foo/bar@^1.3.0"]
    private List<String> getFuzzyIdsFromLine(final String line) {
        final String[] ids = line.split(",");
        return Arrays.stream(ids)
                   .map(id -> id.trim().replaceAll("\"", "").replaceAll(":", ""))
                   .collect(Collectors.toList());
    }

    // "@foo/bar" "^1" -> @foo/bar@^1
    private Optional<YarnLock.Entry.Dependency> getYarnDependencyFromLine(final String line) {
        final String[] pieces = StringUtils.split(line, " ", 2);
        final YarnLock.Entry.Dependency yarnDependency;

        if (pieces.length == 2) {
            final String name = pieces[0].trim().replaceAll("\"", "");
            final String fuzzyVersion = pieces[1].trim().replaceAll("\"", "");
            yarnDependency = new YarnLock.Entry.Dependency(name, fuzzyVersion);
        } else {
            yarnDependency = null;
        }

        return Optional.ofNullable(yarnDependency);
    }
}
