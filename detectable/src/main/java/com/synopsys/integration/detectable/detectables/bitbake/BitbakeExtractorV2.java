package com.synopsys.integration.detectable.detectables.bitbake;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;
import com.synopsys.integration.detectable.detectable.util.EnumListFilter;
import com.synopsys.integration.detectable.detectables.bitbake.collect.BitbakeCommandRunner;
import com.synopsys.integration.detectable.detectables.bitbake.collect.BuildFileFinder;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeEnvironment;
import com.synopsys.integration.detectable.detectables.bitbake.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.model.ShowRecipesResults;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeEnvironmentParser;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeRecipesParser;
import com.synopsys.integration.detectable.detectables.bitbake.parse.LicenseManifestParser;
import com.synopsys.integration.detectable.detectables.bitbake.parse.PwdOutputParser;
import com.synopsys.integration.detectable.detectables.bitbake.transform.BitbakeGraphTransformer;
import com.synopsys.integration.detectable.detectables.bitbake.transform.GraphParserTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.ToolVersionLogger;
import com.synopsys.integration.exception.IntegrationException;

public class BitbakeExtractorV2 {
    public static final String DEFAULT_BUILD_DIR_NAME = "build";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ToolVersionLogger toolVersionLogger;
    private final BitbakeCommandRunner bitbakeCommandRunner;
    private final BuildFileFinder buildFileFinder;
    private final PwdOutputParser pwdOutputParser;
    private final BitbakeEnvironmentParser bitbakeEnvironmentParser;
    private final BitbakeRecipesParser bitbakeRecipesParser;
    private final LicenseManifestParser licenseManifestParser;
    private final GraphParserTransformer graphParserTransformer;
    private final BitbakeGraphTransformer bitbakeGraphTransformer;
    private final List<String> packageNames;
    private final EnumListFilter<BitbakeDependencyType> dependencyTypeFilter;

    public BitbakeExtractorV2(
        ToolVersionLogger toolVersionLogger,
        BitbakeCommandRunner bitbakeCommandRunner,
        BuildFileFinder buildFileFinder,
        PwdOutputParser pwdOutputParser,
        BitbakeEnvironmentParser bitbakeEnvironmentParser,
        BitbakeRecipesParser bitbakeRecipesParser,
        LicenseManifestParser licenseManifestParser,
        GraphParserTransformer graphParserTransformer,
        BitbakeGraphTransformer bitbakeGraphTransformer,
        List<String> packageNames,
        EnumListFilter<BitbakeDependencyType> dependencyTypeFilter
    ) {
        this.toolVersionLogger = toolVersionLogger;
        this.bitbakeCommandRunner = bitbakeCommandRunner;
        this.buildFileFinder = buildFileFinder;
        this.pwdOutputParser = pwdOutputParser;
        this.bitbakeEnvironmentParser = bitbakeEnvironmentParser;
        this.bitbakeRecipesParser = bitbakeRecipesParser;
        this.licenseManifestParser = licenseManifestParser;
        this.graphParserTransformer = graphParserTransformer;
        this.bitbakeGraphTransformer = bitbakeGraphTransformer;
        this.packageNames = packageNames;
        this.dependencyTypeFilter = dependencyTypeFilter;
    }

    public Extraction extract(File sourceDirectory, ExecutableTarget bashExecutable, File buildEnvironmentFile) throws ExecutableFailedException, IOException {
        toolVersionLogger.log(() -> bitbakeCommandRunner.runBitbakeVersion(sourceDirectory, bashExecutable, buildEnvironmentFile));

        File buildDirectory = determineBuildDirectory(sourceDirectory, bashExecutable, buildEnvironmentFile);
        BitbakeEnvironment bitbakeEnvironment = executeBitbakeForEnvironment(sourceDirectory, bashExecutable, buildEnvironmentFile);
        ShowRecipesResults showRecipesResults = executeBitbakeForRecipeLayerCatalog(sourceDirectory, bashExecutable, buildEnvironmentFile);

        List<CodeLocation> codeLocations = packageNames.stream()
            .map(targetPackage -> {
                try {
                    return generateCodeLocationForTargetPackage(
                        sourceDirectory,
                        bashExecutable,
                        buildEnvironmentFile,
                        buildDirectory,
                        targetPackage,
                        showRecipesResults,
                        bitbakeEnvironment
                    );
                } catch (ExecutableFailedException | IntegrationException | IOException e) {
                    logger.error("Failed to extract a Code Location while running Bitbake against package '{}': {}", targetPackage, e.getMessage());
                    logger.debug(e.getMessage(), e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (codeLocations.isEmpty()) {
            return Extraction.failure("No Code Locations were generated during extraction");
        } else {
            return Extraction.success(codeLocations);
        }
    }

    private File determineBuildDirectory(File sourceDirectory, ExecutableTarget bashExecutable, File buildEnvironmentFile) {
        File fallbackBuildDir = new File(sourceDirectory, DEFAULT_BUILD_DIR_NAME);
        File derivedBuildDirectory;
        try {
            List<String> pwdOutput = bitbakeCommandRunner.runPwdCommand(sourceDirectory, bashExecutable, buildEnvironmentFile);
            derivedBuildDirectory = pwdOutputParser.deriveBuildDirectory(pwdOutput);
        } catch (ExecutableFailedException | IOException e) {

            logger.warn("Unable to determine build directory location due to error: {}; ; using {} for build dir", e.getMessage(), fallbackBuildDir.getAbsolutePath());
            return fallbackBuildDir;
        }

        if (derivedBuildDirectory.isDirectory()) {
            logger.debug("Derived build dir: {}", derivedBuildDirectory.getAbsolutePath());
            return derivedBuildDirectory;
        } else {
            logger.warn("Derived build dir {} is not a directory; using {} for build dir", derivedBuildDirectory.getAbsolutePath(), fallbackBuildDir.getAbsolutePath());
            return fallbackBuildDir;
        }
    }

    private BitbakeEnvironment executeBitbakeForEnvironment(File sourceDirectory, ExecutableTarget bashExecutable, File buildEnvironmentFile) {
        try {
            List<String> bitbakeEnvironmentOutput = bitbakeCommandRunner.runBitbakeEnvironment(sourceDirectory, bashExecutable, buildEnvironmentFile);
            return bitbakeEnvironmentParser.parseArchitecture(bitbakeEnvironmentOutput);
        } catch (ExecutableFailedException | IOException e) {
            logger.warn("Unable to get bitbake environment due to error executing {}: {}", BitbakeCommandRunner.BITBAKE_ENVIRONMENT_COMMAND, e.getMessage());
            return new BitbakeEnvironment(null, null);
        }
    }

    private ShowRecipesResults executeBitbakeForRecipeLayerCatalog(File sourceDirectory, ExecutableTarget bashExecutable, File buildEnvironmentFile)
        throws ExecutableFailedException, IOException {
        List<String> showRecipesOutput = bitbakeCommandRunner.runBitbakeLayersShowRecipes(sourceDirectory, bashExecutable, buildEnvironmentFile);
        return bitbakeRecipesParser.parseShowRecipes(showRecipesOutput);
    }

    private CodeLocation generateCodeLocationForTargetPackage(
        File sourceDirectory,
        ExecutableTarget bashExecutable,
        File buildEnvironmentFile,
        File buildDirectory,
        String targetPackage,
        ShowRecipesResults showRecipesResults,
        BitbakeEnvironment bitbakeEnvironment
    ) throws ExecutableFailedException, IntegrationException, IOException {
        BitbakeGraph bitbakeGraph = generateBitbakeGraph(sourceDirectory, bashExecutable, buildEnvironmentFile, buildDirectory, targetPackage, showRecipesResults.getLayerNames());
        Map<String, String> imageRecipes = null;
        if (dependencyTypeFilter.shouldExclude(BitbakeDependencyType.BUILD)) {
            imageRecipes = readImageRecipes(buildDirectory, targetPackage, bitbakeEnvironment);
        }
        DependencyGraph dependencyGraph = bitbakeGraphTransformer.transform(bitbakeGraph, showRecipesResults.getRecipesWithLayers(), imageRecipes);
        return new CodeLocation(dependencyGraph);
    }

    private Map<String, String> readImageRecipes(File buildDirectory, String targetImageName, BitbakeEnvironment bitbakeEnvironment) throws IntegrationException, IOException {
        Optional<File> licenseManifestFile = buildFileFinder.findLicenseManifestFile(buildDirectory, targetImageName, bitbakeEnvironment);
        if (licenseManifestFile.isPresent()) {
            List<String> licenseManifestLines = FileUtils.readLines(licenseManifestFile.get(), StandardCharsets.UTF_8);
            return licenseManifestParser.collectImageRecipes(licenseManifestLines);
        } else {
            logger.info("No license.manifest file found for target image {}; every dependency will be considered a BUILD dependency.", targetImageName);
            return new HashMap<>(0);
        }
    }

    private BitbakeGraph generateBitbakeGraph(
        File sourceDirectory,
        ExecutableTarget bashExecutable,
        File buildEnvironmentFile,
        File buildDirectory,
        String targetPackage,
        Set<String> knownLayers
    ) throws IOException, IntegrationException, ExecutableFailedException {
        bitbakeCommandRunner.runBitbakeGraph(sourceDirectory, bashExecutable, buildEnvironmentFile, targetPackage);
        File taskDependsFile = buildFileFinder.findTaskDependsFile(sourceDirectory, buildDirectory);
        if (logger.isTraceEnabled()) {
            logger.trace(FileUtils.readFileToString(taskDependsFile, Charset.defaultCharset()));
        }
        InputStream dependsFileInputStream = FileUtils.openInputStream(taskDependsFile);
        GraphParser graphParser = new GraphParser(dependsFileInputStream);
        return graphParserTransformer.transform(graphParser, knownLayers);
    }
}
