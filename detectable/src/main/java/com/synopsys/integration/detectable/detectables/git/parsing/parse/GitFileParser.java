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
package com.synopsys.integration.detectable.detectables.git.parsing.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectables.git.parsing.model.GitConfigElement;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

public class GitFileParser {
    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));

    public String parseGitHead(final String headFileContent) {
        return headFileContent.trim().replaceFirst("ref:\\w*", "").trim();
    }

    public List<GitConfigElement> parseGitConfig(final List<String> gitConfigLines) {
        final List<GitConfigElement> gitConfigElements = new ArrayList<>();
        final List<String> lineBuffer = new ArrayList<>();
        for (final String rawLine : gitConfigLines) {
            final String line = StringUtils.stripToEmpty(rawLine);

            if (StringUtils.isBlank(line)) {
                continue;
            }

            if (isGitConfigElementStart(line)) {
                final Optional<GitConfigElement> gitConfigElement = processGitConfigElementLines(lineBuffer);
                gitConfigElement.ifPresent(gitConfigElements::add);
                lineBuffer.clear();
            }

            lineBuffer.add(line);
        }

        final Optional<GitConfigElement> gitConfigElement = processGitConfigElementLines(lineBuffer);
        gitConfigElement.ifPresent(gitConfigElements::add);

        return gitConfigElements;
    }

    private boolean isGitConfigElementStart(final String line) {
        return line.startsWith("[") && line.endsWith("]");
    }

    private Optional<GitConfigElement> processGitConfigElementLines(final List<String> lines) {
        final Map<String, String> properties = new HashMap<>();
        String elementType = null;
        String elementName = null;

        for (final String line : lines) {
            if (isGitConfigElementStart(line)) {
                final String lineWithoutBrackets = line.replace("[", "").replace("]", "");
                final String[] pieces = lineWithoutBrackets.split(" ");

                if (pieces.length == 1) {
                    elementType = pieces[0].trim();
                } else if (pieces.length == 2) {
                    elementType = pieces[0].trim();
                    elementName = pieces[1].replace("\"", "").trim();
                } else {
                    logger.warn(String.format("Invalid git config element. Skipping. %s", line));
                    break;
                }
            } else {
                final String[] pieces = line.split("=");

                if (pieces.length == 2) {
                    final String propertyKey = pieces[0].trim();
                    final String propertyValue = pieces[1].trim();
                    properties.put(propertyKey, propertyValue);
                } else {
                    logger.warn(String.format("Invalid git config element property. Skipping. %s", line));
                }
            }
        }

        if (StringUtils.isNotBlank(elementType)) {
            return Optional.of(new GitConfigElement(elementType, elementName, properties));
        } else {
            return Optional.empty();
        }
    }
}
