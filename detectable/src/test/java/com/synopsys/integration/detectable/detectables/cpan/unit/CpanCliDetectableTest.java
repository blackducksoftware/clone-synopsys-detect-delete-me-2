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
package com.synopsys.integration.detectable.detectables.cpan.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.Extraction;
import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.resolver.CpanResolver;
import com.synopsys.integration.detectable.detectable.executable.resolver.CpanmResolver;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectables.cpan.CpanCliDetectable;
import com.synopsys.integration.detectable.detectables.cpan.CpanCliExtractor;
import com.synopsys.integration.detectable.functional.DetectableFunctionalTest;
import com.synopsys.integration.detectable.util.MockDetectableEnvironment;
import com.synopsys.integration.detectable.util.MockFileFinder;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

public class CpanCliDetectableTest extends DetectableFunctionalTest {
    public CpanCliDetectableTest() throws IOException {
        super("cpan");
    }

    @Test
    public void testApplicable() {

        final DetectableEnvironment environment = MockDetectableEnvironment.empty();
        final FileFinder fileFinder = MockFileFinder.withFileNamed("Makefile.PL");

        final CpanResolver cpanResolver = null;
        final CpanmResolver cpanmResolver = null;
        final CpanCliExtractor cpanCliExtractor = null;

        final CpanCliDetectable detectable = new CpanCliDetectable(environment, fileFinder, cpanResolver, cpanmResolver, cpanCliExtractor);

        assertTrue(detectable.applicable().getPassed());
    }

    @Override
    protected void setup() throws IOException {
        addFile("Makefile.PL");

        ExecutableOutput cpanListOutput = createStandardOutput(
            "ExtUtils::MakeMaker\t7.24",
            "perl\t5.1",
            "Test::More\t1.3"
        );
        addExecutableOutputToOutputDirectory(cpanListOutput, "cpan", "-l");

        ExecutableOutput cpanmShowDepsOutput = createStandardOutput(
            "--> Working on .",
            "Configuring App-cpanminus-1.7043 ... OK",
            "ExtUtils::MakeMaker~6.58",
            "Test::More",
            "perl~5.008001",
            "ExtUtils::MakeMaker"
        );
        addExecutableOutputToOutputDirectory(cpanmShowDepsOutput, "cpanm", "--showdeps", ".");
    }

    @NotNull
    @Override
    public Detectable create(@NotNull final DetectableEnvironment detectableEnvironment) {
        class CpanResolverTest implements CpanResolver {
            @Override
            public File resolveCpan() {
                return new File("cpan");
            }
        }

        class CpanmResolverTest implements CpanmResolver {
            @Override
            public File resolveCpanm() {
                return new File("cpanm");
            }
        }
        return detectableFactory.createCpanCliDetectable(detectableEnvironment, new CpanResolverTest(), new CpanmResolverTest());
    }

    @Override
    public void assertExtraction(@NotNull final Extraction extraction) {
        Assertions.assertNotEquals(0, extraction.getCodeLocations().size());

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.CPAN, extraction.getCodeLocations().get(0).getDependencyGraph());
        graphAssert.hasRootSize(3);
        graphAssert.hasRootDependency("Test-More", "1.3");
        graphAssert.hasRootDependency("ExtUtils-MakeMaker", "7.24");
        graphAssert.hasRootDependency("perl", "5.1");

    }
}
