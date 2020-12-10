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
package com.synopsys.integration.detectable.detectables.conan.cli.parser;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectables.conan.cli.parser.element.NodeElementParser;
import com.synopsys.integration.detectable.detectables.conan.graph.ConanNode;
import com.synopsys.integration.detectable.detectables.conan.graph.ConanNodeBuilder;

public class ConanInfoNodeParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConanInfoLineAnalyzer conanInfoLineAnalyzer;
    private final NodeElementParser nodeElementParser;

    public ConanInfoNodeParser(ConanInfoLineAnalyzer conanInfoLineAnalyzer, NodeElementParser nodeElementParser) {
        this.conanInfoLineAnalyzer = conanInfoLineAnalyzer;
        this.nodeElementParser = nodeElementParser;
    }

    /*
     * A node looks like this:
     * ref:
     *     node body element (either key: value, key:\nlist-of-values)
     */
    public ConanInfoNodeParseResult parseNode(List<String> conanInfoOutputLines, int nodeStartIndex) {
        String nodeHeaderLine = conanInfoOutputLines.get(nodeStartIndex);
        ConanNodeBuilder nodeBuilder = new ConanNodeBuilder();
        nodeBuilder.setRefFromConanInfo(nodeHeaderLine.trim());
        int bodyLineCount = 0;
        for (int lineIndex = nodeStartIndex + 1; lineIndex < conanInfoOutputLines.size(); lineIndex++) {
            String nodeBodyLine = conanInfoOutputLines.get(lineIndex);
            logger.trace("Parsing line: {}: {}", lineIndex + 1, nodeBodyLine);
            // Check to see if we've overshot the end of the node
            Optional<ConanInfoNodeParseResult> result = getResultIfDone(nodeBodyLine, lineIndex, nodeStartIndex, bodyLineCount, nodeBuilder);
            if (result.isPresent()) {
                return result.get();
            }
            bodyLineCount++;
            // parseElement tells this code what line to parse next (= where it left off)
            lineIndex = nodeElementParser.parseElement(nodeBuilder, conanInfoOutputLines, lineIndex);
        }
        logger.trace("Reached end of conan info output");
        Optional<ConanNode> node = nodeBuilder.build();
        ConanInfoNodeParseResult result =
            new ConanInfoNodeParseResult(conanInfoOutputLines.size() - 1, node.orElse(null));
        return result;
    }

    private Optional<ConanInfoNodeParseResult> getResultIfDone(String nodeBodyLine, int lineIndex, int nodeStartIndex, int bodyLineCount, ConanNodeBuilder nodeBuilder) {
        int indentDepth = conanInfoLineAnalyzer.measureIndentDepth(nodeBodyLine);
        if (indentDepth > 0) {
            // We're not done parsing this node
            return Optional.empty();
        }
        if (bodyLineCount == 0) {
            logger.trace("This wasn't a node (it was just a conan info command log message)");
            return Optional.of(new ConanInfoNodeParseResult(nodeStartIndex));
        } else {
            logger.trace("Reached end of node");
            Optional<ConanNode> node = nodeBuilder.build();
            return Optional.of(new ConanInfoNodeParseResult(lineIndex - 1, node.orElse(null)));
        }
    }

}
