/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect.bomtool.cpan;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.bomtool.BomToolGroupType;
import com.blackducksoftware.integration.hub.detect.bomtool.BomToolType;
import com.blackducksoftware.integration.hub.detect.bomtool.cpan.parse.CpanPackager;
import com.blackducksoftware.integration.hub.detect.manager.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;

@Component
public class CpanCliExtractor {
    @Autowired
    private CpanPackager cpanPackager;

    @Autowired
    private ExternalIdFactory externalIdFactory;

    @Autowired
    private ExecutableRunner executableRunner;

    public Extraction extract(final BomToolType bomToolType, final File directory, final File cpanExe, final File cpanmExe) {
        try {
            final ExecutableOutput cpanListOutput = executableRunner.runExe(cpanExe, "-l");
            final List<String> listText = cpanListOutput.getStandardOutputAsList();

            final ExecutableOutput showdepsOutput = executableRunner.runExe(cpanmExe, "--showdeps", ".");
            final List<String> showdeps = showdepsOutput.getStandardOutputAsList();

            final DependencyGraph dependencyGraph = cpanPackager.makeDependencyGraph(listText, showdeps);
            final ExternalId externalId = externalIdFactory.createPathExternalId(Forge.CPAN, directory.toString());
            final DetectCodeLocation detectCodeLocation = new DetectCodeLocation.Builder(BomToolGroupType.CPAN, bomToolType, directory.toString(), externalId, dependencyGraph).build();
            return new Extraction.Builder().success(detectCodeLocation).build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
