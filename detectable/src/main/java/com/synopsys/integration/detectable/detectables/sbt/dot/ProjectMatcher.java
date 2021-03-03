/**
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.detectable.detectables.sbt.dot;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphElement;
import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.bdio.model.externalid.ExternalId;

public class ProjectMatcher {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SbtDotGraphNodeParser sbtDotGraphNodeParser;

    public ProjectMatcher(final SbtDotGraphNodeParser sbtDotGraphNodeParser) {
        this.sbtDotGraphNodeParser = sbtDotGraphNodeParser;
    }

    public ProjectMatch determineProjectID(GraphParser graphParser, List<SbtProject> projects) {
        Set<String> nodeIdsUsedInDestination = graphParser.getEdges().values().stream()
                                                   .map(GraphEdge::getNode2)
                                                   .map(GraphElement::getId)
                                                   .collect(Collectors.toSet());
        Set<String> allNodeIds = new HashSet<>(graphParser.getNodes().keySet());
        Set<String> nodeIdsWithNoDestination = SetUtils.difference(allNodeIds, nodeIdsUsedInDestination);

        if (nodeIdsWithNoDestination.size() == 1) {
            String nodeId = nodeIdsWithNoDestination.stream().findFirst().get();
            ExternalId nodeExternalId = sbtDotGraphNodeParser.nodeToDependency(nodeId).getExternalId();
            Optional<SbtProject> matchingProject = projects.stream()
                                                       .filter(project -> project.getGroup().equals(nodeExternalId.getGroup()))
                                                       .filter(project -> project.getName().equals(nodeExternalId.getName()))
                                                       .filter(project -> project.getVersion().equals(nodeExternalId.getVersion()))
                                                       .findFirst();

            if (matchingProject.isPresent()) {
                return ProjectMatch.FoundMatch(nodeId, matchingProject.get());
            }
        }
        return ProjectMatch.NoMatch(nodeIdsUsedInDestination);
    }
}
