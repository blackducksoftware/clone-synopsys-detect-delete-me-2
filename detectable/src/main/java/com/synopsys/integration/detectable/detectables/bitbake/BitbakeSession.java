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
package com.synopsys.integration.detectable.detectables.bitbake;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.misc.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeFileType;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeRecipe;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeResult;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeRecipesParser;
import com.synopsys.integration.exception.IntegrationException;

public class BitbakeSession {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FileFinder fileFinder;
    private final ExecutableRunner executableRunner;
    private final BitbakeRecipesParser bitbakeRecipesParser;
    private final File outputDirectory;
    private final File buildEnvScript;
    private final String[] sourceArguments;
    private final File bashExecutable;

    public BitbakeSession(final FileFinder fileFinder, final ExecutableRunner executableRunner, final BitbakeRecipesParser bitbakeRecipesParser, final File outputDirectory, final File buildEnvScript, final String[] sourceArguments,
        final File bashExecutable) {
        this.fileFinder = fileFinder;
        this.executableRunner = executableRunner;
        this.bitbakeRecipesParser = bitbakeRecipesParser;
        this.outputDirectory = outputDirectory;
        this.buildEnvScript = buildEnvScript;
        this.sourceArguments = sourceArguments;
        this.bashExecutable = bashExecutable;
    }

    public Optional<BitbakeResult> executeBitbakeForDependencies(final File sourceDirectory, final String packageName)
        throws ExecutableRunnerException, IOException {
        final String bitbakeCommand = "bitbake -g " + packageName;
        final ExecutableOutput executableOutput = runBitbake(bitbakeCommand);
        final int returnCode = executableOutput.getReturnCode();

        if (returnCode != 0) {
            logger.error(String.format("Executing command '%s' returned a non-zero exit code %s", bitbakeCommand, returnCode));
            return Optional.empty();
        }

        return Arrays.stream(BitbakeFileType.values())
                   .map(bitbakeFileType -> getBitbakeResult(sourceDirectory, outputDirectory, bitbakeFileType))
                   .findFirst();
    }

    @Nullable
    private BitbakeResult getBitbakeResult(final File sourceDirectory, final File outputDirectory, final BitbakeFileType bitbakeFileType) {
        File file = fileFinder.findFile(outputDirectory, bitbakeFileType.getFileName(), 1);
        if (file == null) {
            file = fileFinder.findFile(sourceDirectory, bitbakeFileType.getFileName(), 1);
            if (file == null) {
                return null;
            }
        }

        return new BitbakeResult(bitbakeFileType, file);

    }

    public List<BitbakeRecipe> executeBitbakeForRecipeLayerCatalog() throws ExecutableRunnerException, IOException, IntegrationException {
        final String bitbakeCommand = "bitbake-layers show-recipes";
        final ExecutableOutput executableOutput = runBitbake(bitbakeCommand);
        if (executableOutput.getReturnCode() == 0) {
            return bitbakeRecipesParser.parseShowRecipes(executableOutput.getStandardOutputAsList());
        } else {
            throw new IntegrationException("Running command '%s' returned a non-zero exit code. Failed to extract bitbake recipe mapping.");
        }
    }

    private ExecutableOutput runBitbake(final String bitbakeCommand) throws ExecutableRunnerException, IOException {
        try {
            final StringBuilder sourceCommand = new StringBuilder("source " + buildEnvScript.getCanonicalPath());
            for (final String sourceArgument : sourceArguments) {
                sourceCommand.append(" ");
                sourceCommand.append(sourceArgument);
            }
            return executableRunner.execute(outputDirectory, bashExecutable, "-c", sourceCommand.toString() + "; " + bitbakeCommand);
        } catch (final ExecutableRunnerException e) {
            logger.error(String.format("Failed executing bitbake command. %s", bitbakeCommand));
            throw e;
        }
    }
}
