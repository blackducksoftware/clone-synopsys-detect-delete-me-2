/**
 * synopsys-detect
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
package com.synopsys.integration.detect;

import java.io.File;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.BdioTransformer;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detect.configuration.ConnectionManager;
import com.synopsys.integration.detect.configuration.DetectableOptionFactory;
import com.synopsys.integration.detect.tool.detector.DetectableFactory;
import com.synopsys.integration.detect.tool.detector.impl.DetectExecutableResolver;
import com.synopsys.integration.detect.tool.detector.inspectors.ArtifactoryDockerInspectorResolver;
import com.synopsys.integration.detect.tool.detector.inspectors.ArtifactoryGradleInspectorResolver;
import com.synopsys.integration.detect.tool.detector.inspectors.DockerInspectorInstaller;
import com.synopsys.integration.detect.tool.detector.inspectors.GradleInspectorInstaller;
import com.synopsys.integration.detect.tool.detector.inspectors.LocalPipInspectorResolver;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.AirgapNugetInspectorLocator;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.LocatorNugetInspectorResolver;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.NugetInspectorInstaller;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.NugetInspectorLocator;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.NugetLocatorOptions;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.OnlineNugetInspectorLocator;
import com.synopsys.integration.detect.workflow.ArtifactResolver;
import com.synopsys.integration.detect.workflow.airgap.AirGapInspectorPaths;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunner;
import com.synopsys.integration.detectable.detectable.file.FileFinder;
import com.synopsys.integration.detectable.detectable.inspector.GradleInspectorResolver;
import com.synopsys.integration.detectable.detectable.inspector.PipInspectorResolver;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorResolver;
import com.synopsys.integration.detectable.detectables.bazel.BazelDependencyParser;
import com.synopsys.integration.detectable.detectables.bazel.BazelDetectable;
import com.synopsys.integration.detectable.detectables.bazel.BazelExtractor;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.WorkspaceRuleChooser;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDetectable;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeExtractor;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeRecipesToLayerMapConverter;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeGraphTransformer;
import com.synopsys.integration.detectable.detectables.bitbake.parse.BitbakeRecipesParser;
import com.synopsys.integration.detectable.detectables.bitbake.parse.GraphParserTransformer;
import com.synopsys.integration.detectable.detectables.clang.ClangDetectable;
import com.synopsys.integration.detectable.detectables.clang.ClangExtractor;
import com.synopsys.integration.detectable.detectables.clang.compilecommand.CompileCommandDatabaseParser;
import com.synopsys.integration.detectable.detectables.clang.compilecommand.CompileCommandParser;
import com.synopsys.integration.detectable.detectables.clang.dependencyfile.ClangPackageDetailsTransformer;
import com.synopsys.integration.detectable.detectables.clang.dependencyfile.DependencyFileDetailGenerator;
import com.synopsys.integration.detectable.detectables.clang.dependencyfile.DependenyListFileParser;
import com.synopsys.integration.detectable.detectables.clang.dependencyfile.FilePathGenerator;
import com.synopsys.integration.detectable.detectables.clang.packagemanager.ClangPackageManagerFactory;
import com.synopsys.integration.detectable.detectables.clang.packagemanager.ClangPackageManagerInfoFactory;
import com.synopsys.integration.detectable.detectables.clang.packagemanager.ClangPackageManagerRunner;
import com.synopsys.integration.detectable.detectables.cocoapods.PodlockDetectable;
import com.synopsys.integration.detectable.detectables.cocoapods.PodlockExtractor;
import com.synopsys.integration.detectable.detectables.cocoapods.parser.PodlockParser;
import com.synopsys.integration.detectable.detectables.conda.CondaCliDetectable;
import com.synopsys.integration.detectable.detectables.conda.CondaCliExtractor;
import com.synopsys.integration.detectable.detectables.conda.parser.CondaListParser;
import com.synopsys.integration.detectable.detectables.cpan.CpanCliDetectable;
import com.synopsys.integration.detectable.detectables.cpan.CpanCliExtractor;
import com.synopsys.integration.detectable.detectables.cpan.parse.CpanListParser;
import com.synopsys.integration.detectable.detectables.cran.PackratLockDetectable;
import com.synopsys.integration.detectable.detectables.cran.PackratLockExtractor;
import com.synopsys.integration.detectable.detectables.cran.parse.PackratDescriptionFileParser;
import com.synopsys.integration.detectable.detectables.cran.parse.PackratLockFileParser;
import com.synopsys.integration.detectable.detectables.docker.DockerDetectable;
import com.synopsys.integration.detectable.detectables.docker.DockerExtractor;
import com.synopsys.integration.detectable.detectables.docker.DockerInspectorResolver;
import com.synopsys.integration.detectable.detectables.docker.DockerProperties;
import com.synopsys.integration.detectable.detectables.git.cli.GitCliDetectable;
import com.synopsys.integration.detectable.detectables.git.cli.GitCliExtractor;
import com.synopsys.integration.detectable.detectables.git.cli.GitUrlParser;
import com.synopsys.integration.detectable.detectables.git.parsing.GitParseDetectable;
import com.synopsys.integration.detectable.detectables.git.parsing.GitParseExtractor;
import com.synopsys.integration.detectable.detectables.git.parsing.parse.GitFileParser;
import com.synopsys.integration.detectable.detectables.git.parsing.parse.GitFileTransformer;
import com.synopsys.integration.detectable.detectables.go.godep.GoDepExtractor;
import com.synopsys.integration.detectable.detectables.go.godep.GoDepLockDetectable;
import com.synopsys.integration.detectable.detectables.go.godep.parse.GoLockParser;
import com.synopsys.integration.detectable.detectables.go.gogradle.GoGradleDetectable;
import com.synopsys.integration.detectable.detectables.go.gogradle.GoGradleExtractor;
import com.synopsys.integration.detectable.detectables.go.gogradle.GoGradleLockParser;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModCliDetectable;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModCliExtractor;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModGraphParser;
import com.synopsys.integration.detectable.detectables.go.vendor.GoVendorDetectable;
import com.synopsys.integration.detectable.detectables.go.vendor.GoVendorExtractor;
import com.synopsys.integration.detectable.detectables.go.vendr.GoVndrDetectable;
import com.synopsys.integration.detectable.detectables.go.vendr.GoVndrExtractor;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleDetectable;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleInspectorExtractor;
import com.synopsys.integration.detectable.detectables.gradle.inspection.inspector.GradleInspectorScriptCreator;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportParser;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportTransformer;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleRootMetadataParser;
import com.synopsys.integration.detectable.detectables.gradle.parsing.GradleParseDetectable;
import com.synopsys.integration.detectable.detectables.gradle.parsing.GradleParseExtractor;
import com.synopsys.integration.detectable.detectables.gradle.parsing.parse.BuildGradleParser;
import com.synopsys.integration.detectable.detectables.hex.RebarDetectable;
import com.synopsys.integration.detectable.detectables.hex.RebarExtractor;
import com.synopsys.integration.detectable.detectables.hex.parse.Rebar3TreeParser;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenCliExtractor;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenCodeLocationPackager;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenPomDetectable;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenPomWrapperDetectable;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseDetectable;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseExtractor;
import com.synopsys.integration.detectable.detectables.npm.NpmPackageJsonDiscoverer;
import com.synopsys.integration.detectable.detectables.npm.cli.NpmCliDetectable;
import com.synopsys.integration.detectable.detectables.npm.cli.NpmCliExtractor;
import com.synopsys.integration.detectable.detectables.npm.cli.parse.NpmCliParser;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmLockfileExtractor;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmPackageLockDetectable;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmShrinkwrapDetectable;
import com.synopsys.integration.detectable.detectables.npm.lockfile.parse.NpmLockfileParser;
import com.synopsys.integration.detectable.detectables.npm.packagejson.NpmPackageJsonParseDetectable;
import com.synopsys.integration.detectable.detectables.npm.packagejson.PackageJsonExtractor;
import com.synopsys.integration.detectable.detectables.nuget.NugetInspectorExtractor;
import com.synopsys.integration.detectable.detectables.nuget.NugetProjectDetectable;
import com.synopsys.integration.detectable.detectables.nuget.NugetSolutionDetectable;
import com.synopsys.integration.detectable.detectables.nuget.parse.NugetInspectorParser;
import com.synopsys.integration.detectable.detectables.packagist.ComposerLockDetectable;
import com.synopsys.integration.detectable.detectables.packagist.ComposerLockExtractor;
import com.synopsys.integration.detectable.detectables.packagist.parse.PackagistParser;
import com.synopsys.integration.detectable.detectables.pear.PearCliDetectable;
import com.synopsys.integration.detectable.detectables.pear.PearCliExtractor;
import com.synopsys.integration.detectable.detectables.pear.parse.PearListParser;
import com.synopsys.integration.detectable.detectables.pear.parse.PearPackageDependenciesParser;
import com.synopsys.integration.detectable.detectables.pear.parse.PearPackageXmlParser;
import com.synopsys.integration.detectable.detectables.pear.transform.PearDependencyGraphTransformer;
import com.synopsys.integration.detectable.detectables.pip.PipInspectorDetectable;
import com.synopsys.integration.detectable.detectables.pip.PipInspectorExtractor;
import com.synopsys.integration.detectable.detectables.pip.PipenvDetectable;
import com.synopsys.integration.detectable.detectables.pip.PipenvExtractor;
import com.synopsys.integration.detectable.detectables.pip.parser.PipInspectorTreeParser;
import com.synopsys.integration.detectable.detectables.pip.parser.PipenvGraphParser;
import com.synopsys.integration.detectable.detectables.rubygems.gemlock.GemlockDetectable;
import com.synopsys.integration.detectable.detectables.rubygems.gemlock.GemlockExtractor;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.GemspecParseDetectable;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.GemspecParseExtractor;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.parse.GemspecLineParser;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.parse.GemspecParser;
import com.synopsys.integration.detectable.detectables.sbt.SbtResolutionCacheDetectable;
import com.synopsys.integration.detectable.detectables.sbt.SbtResolutionCacheExtractor;
import com.synopsys.integration.detectable.detectables.swift.SwiftCliDetectable;
import com.synopsys.integration.detectable.detectables.swift.SwiftCliParser;
import com.synopsys.integration.detectable.detectables.swift.SwiftExtractor;
import com.synopsys.integration.detectable.detectables.swift.SwiftPackageTransformer;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockDetectable;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockExtractor;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLineLevelParser;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockParser;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnTransformer;

import freemarker.template.Configuration;

//@Configuration is used here to allow 'EnableAspectJAutoProxy' because it could not be enabled it otherwise.
//This configuration is NOT loaded when the application starts, but only manually when a DetectRun is needed.
//Spring scanning should not be invoked as this should not be loaded during boot.
@org.springframework.context.annotation.Configuration
public class DetectableBeanConfiguration {
    //The Important Ones
    private final FileFinder fileFinder;
    @Autowired
    public ExecutableRunner executableRunner;
    @Autowired
    public DetectableOptionFactory detectableOptionFactory;
    @Autowired
    public DetectExecutableResolver detectExecutableResolver;

    //The Regular Ones
    @Autowired
    public Gson gson;
    @Autowired
    public Configuration configuration;
    @Autowired
    public DocumentBuilder documentBuilder;
    @Autowired
    public ExternalIdFactory externalIdFactory;
    @Autowired
    public ConnectionManager connectionManager;
    @Autowired
    public AirGapInspectorPaths airGapInspectorPaths;
    @Autowired
    public DirectoryManager directoryManager;
    @Autowired
    public DetectInfo detectInfo;
    @Autowired
    public ArtifactResolver artifactResolver;

    public DetectableBeanConfiguration(final FileFinder fileFinder) {
        this.fileFinder = fileFinder;
    }

    //DetectableFactory
    //This is the ONLY class that should be taken from the Configuration manually.
    //Detectables should be accessed using the DetectableFactory which will create them through Spring.

    @Bean
    public DetectableFactory detectableFactory() {
        return new DetectableFactory();
    }

    //Detectable-Only Dependencies
    //All detector support classes. These are classes not actually used outside of the bom tools but are necessary for some bom tools.

    @Bean
    public BazelExtractor bazelExtractor() {
        final WorkspaceRuleChooser workspaceRuleChooser = new WorkspaceRuleChooser();
        final BazelDependencyParser bazelDependencyParser = new BazelDependencyParser(externalIdFactory);
        return new BazelExtractor(executableRunner, bazelDependencyParser, workspaceRuleChooser);
    }

    public FilePathGenerator filePathGenerator() {
        return new FilePathGenerator(executableRunner, compileCommandParser(), dependenyListFileParser());
    }

    public DependenyListFileParser dependenyListFileParser() {
        return new DependenyListFileParser();
    }

    public DependencyFileDetailGenerator dependencyFileDetailGenerator() {
        return new DependencyFileDetailGenerator(filePathGenerator());
    }

    public ClangPackageDetailsTransformer clangPackageDetailsTransformer() {
        return new ClangPackageDetailsTransformer(externalIdFactory);
    }

    public CompileCommandDatabaseParser compileCommandDatabaseParser() {
        return new CompileCommandDatabaseParser(gson);
    }

    public CompileCommandParser compileCommandParser() {
        return new CompileCommandParser();
    }

    @Bean
    public ClangExtractor clangExtractor() {
        return new ClangExtractor(executableRunner, dependencyFileDetailGenerator(), clangPackageDetailsTransformer(), compileCommandDatabaseParser());
    }

    @Bean
    public PodlockParser podlockParser() {
        return new PodlockParser(externalIdFactory);
    }

    @Bean
    public PodlockExtractor podlockExtractor() {
        return new PodlockExtractor(podlockParser());
    }

    @Bean
    public CondaListParser condaListParser() {
        return new CondaListParser(gson, externalIdFactory);
    }

    @Bean
    public CondaCliExtractor condaCliExtractor() {
        return new CondaCliExtractor(condaListParser(), executableRunner, detectableOptionFactory.createCondaOptions());
    }

    @Bean
    public CpanListParser cpanListParser() {
        return new CpanListParser(externalIdFactory);
    }

    @Bean
    public CpanCliExtractor cpanCliExtractor() {
        return new CpanCliExtractor(cpanListParser(), executableRunner);
    }

    public DockerInspectorResolver dockerInspectorResolver() {
        final DockerInspectorInstaller dockerInspectorInstaller = new DockerInspectorInstaller(artifactResolver);
        return new ArtifactoryDockerInspectorResolver(directoryManager, airGapInspectorPaths, fileFinder, dockerInspectorInstaller, detectableOptionFactory.createDockerDetectableOptions());
    }

    @Bean
    public PackratLockFileParser packratLockFileParser() {
        return new PackratLockFileParser(externalIdFactory);
    }

    @Bean
    public PackratDescriptionFileParser packratDescriptionFileParser() {
        return new PackratDescriptionFileParser();
    }

    @Bean
    public PackratLockExtractor packratLockExtractor() {
        return new PackratLockExtractor(packratDescriptionFileParser(), packratLockFileParser(), fileFinder);
    }

    @Bean
    public GitFileParser gitFileParser() {
        return new GitFileParser();
    }

    @Bean
    public GitFileTransformer gitFileTransformer() {
        return new GitFileTransformer();
    }

    @Bean
    public GitParseExtractor gitParseExtractor() {
        return new GitParseExtractor(gitFileParser(), gitFileTransformer());
    }

    @Bean
    public GitUrlParser gitUrlParser() {
        return new GitUrlParser();
    }

    @Bean
    public GitCliExtractor gitCliExtractor() {
        return new GitCliExtractor(executableRunner, gitUrlParser());
    }

    @Bean
    public GoLockParser goLockParser() {
        return new GoLockParser(externalIdFactory);
    }

    @Bean //final GoDepLockFileGenerator goDepLockFileGenerator, final GoLockParser goLockParser,
    public GoDepExtractor goDepExtractor() {
        return new GoDepExtractor(goLockParser());
    }

    @Bean
    public GoModGraphParser goModGraphParser() {
        return new GoModGraphParser(externalIdFactory);
    }

    @Bean
    public GoModCliExtractor goModCliExtractor() {
        return new GoModCliExtractor(executableRunner, goModGraphParser());
    }

    @Bean
    public GoVndrExtractor goVndrExtractor() {
        return new GoVndrExtractor(externalIdFactory);
    }

    @Bean
    public GoVendorExtractor goVendorExtractor() {
        return new GoVendorExtractor(gson, externalIdFactory);
    }

    @Bean
    public GradleReportParser gradleReportParser() {
        return new GradleReportParser();
    }

    @Bean
    public GradleReportTransformer gradleReportTransformer() {
        return new GradleReportTransformer(externalIdFactory);
    }

    @Bean
    public GradleRootMetadataParser gradleRootMetadataParser() {
        return new GradleRootMetadataParser();
    }

    @Bean
    public GradleInspectorResolver gradleInspectorResolver() {
        final GradleInspectorInstaller gradleInspectorInstaller = new GradleInspectorInstaller(artifactResolver);
        return new ArtifactoryGradleInspectorResolver(gradleInspectorInstaller, configuration, detectableOptionFactory.createGradleInspectorOptions().getGradleInspectorScriptOptions(), airGapInspectorPaths, directoryManager);
    }

    @Bean
    public Rebar3TreeParser rebar3TreeParser() {
        return new Rebar3TreeParser(externalIdFactory);
    }

    @Bean
    public RebarExtractor rebarExtractor() {
        return new RebarExtractor(executableRunner, rebar3TreeParser());
    }

    @Bean
    public MavenCodeLocationPackager mavenCodeLocationPackager() {
        return new MavenCodeLocationPackager(externalIdFactory);
    }

    @Bean
    public MavenCliExtractor mavenCliExtractor() {
        return new MavenCliExtractor(executableRunner, mavenCodeLocationPackager(), detectableOptionFactory.createMavenCliOptions());
    }

    @Bean
    public NpmCliParser npmCliDependencyFinder() {
        return new NpmCliParser(externalIdFactory);
    }

    @Bean
    public NpmLockfileParser npmLockfilePackager() {
        return new NpmLockfileParser(gson, externalIdFactory);
    }

    @Bean
    public NpmCliExtractor npmCliExtractor() {
        return new NpmCliExtractor(executableRunner, npmCliDependencyFinder(), detectableOptionFactory.createNpmCliExtractorOptions());
    }

    @Bean
    public NpmPackageJsonDiscoverer npmPackageJsonDiscoverer() {
        return new NpmPackageJsonDiscoverer(gson);
    }

    @Bean
    public NpmLockfileExtractor npmLockfileExtractor() {
        return new NpmLockfileExtractor(npmLockfilePackager());
    }

    @Bean
    public NugetInspectorResolver nugetInspectorResolver() {
        final NugetLocatorOptions installerOptions = detectableOptionFactory.createNugetInstallerOptions();
        final NugetInspectorLocator locator;
        final Optional<File> nugetAirGapPath = airGapInspectorPaths.getNugetInspectorAirGapFile();
        if (nugetAirGapPath.isPresent()) {
            locator = new AirgapNugetInspectorLocator(airGapInspectorPaths);
        } else {
            final NugetInspectorInstaller installer = new NugetInspectorInstaller(artifactResolver);
            locator = new OnlineNugetInspectorLocator(installer, directoryManager, installerOptions.getNugetInspectorVersion());
        }
        return new LocatorNugetInspectorResolver(detectExecutableResolver, executableRunner, detectInfo, fileFinder, installerOptions.getNugetInspectorName(), installerOptions.getPackagesRepoUrl(), locator);
    }

    @Bean
    public NugetInspectorParser nugetInspectorParser() {
        return new NugetInspectorParser(gson, externalIdFactory);
    }

    @Bean
    public NugetInspectorExtractor nugetInspectorExtractor() {
        return new NugetInspectorExtractor(nugetInspectorParser(), fileFinder);
    }

    @Bean
    public PackagistParser packagistParser() {
        return new PackagistParser(externalIdFactory, detectableOptionFactory.createComposerLockDetectableOptions());
    }

    @Bean
    public ComposerLockExtractor composerLockExtractor() {
        return new ComposerLockExtractor(packagistParser());
    }

    @Bean
    public PearListParser pearListParser() {
        return new PearListParser();
    }

    @Bean
    public PearPackageXmlParser pearPackageXmlParser() {
        return new PearPackageXmlParser();
    }

    @Bean
    public PearPackageDependenciesParser pearPackageDependenciesParser() {
        return new PearPackageDependenciesParser();
    }

    @Bean
    public PearDependencyGraphTransformer pearDependencyGraphTransformer() {
        return new PearDependencyGraphTransformer(externalIdFactory);
    }

    @Bean
    public PearCliExtractor pearCliExtractor() {
        return new PearCliExtractor(externalIdFactory, executableRunner, pearDependencyGraphTransformer(), pearPackageXmlParser(), pearPackageDependenciesParser(), pearListParser());
    }

    @Bean
    public PipenvGraphParser pipenvGraphParser() {
        return new PipenvGraphParser(externalIdFactory);
    }

    @Bean
    public PipenvExtractor pipenvExtractor() {
        return new PipenvExtractor(executableRunner, pipenvGraphParser());
    }

    @Bean
    public PipInspectorResolver pipInspectorResolver() {
        return new LocalPipInspectorResolver(directoryManager);
    }

    @Bean
    public PipInspectorTreeParser pipInspectorTreeParser() {
        return new PipInspectorTreeParser(externalIdFactory);
    }

    @Bean
    public PipInspectorExtractor pipInspectorExtractor() {
        return new PipInspectorExtractor(executableRunner, pipInspectorTreeParser());
    }

    @Bean
    public GemlockExtractor gemlockExtractor() {
        return new GemlockExtractor(externalIdFactory);
    }

    @Bean
    public SbtResolutionCacheExtractor sbtResolutionCacheExtractor() {
        return new SbtResolutionCacheExtractor(fileFinder, externalIdFactory, detectableOptionFactory.createSbtResolutionCacheDetectableOptions());
    }

    @Bean
    public YarnLockParser yarnLockParser() {
        return new YarnLockParser(yarnLineLevelParser());
    }

    @Bean
    public YarnTransformer yarnTransformer() {
        return new YarnTransformer(externalIdFactory);
    }

    @Bean
    public YarnLineLevelParser yarnLineLevelParser() {
        return new YarnLineLevelParser();
    }

    @Bean
    public YarnLockExtractor yarnLockExtractor() {
        return new YarnLockExtractor(yarnLockParser(), detectableOptionFactory.createYarnLockOptions(), yarnTransformer(), gson);
    }

    @Bean
    public BitbakeRecipesParser bitbakeRecipesParser() {
        return new BitbakeRecipesParser();
    }

    @Bean
    public BitbakeRecipesToLayerMapConverter bitbakeRecipesToLayerMap() {
        return new BitbakeRecipesToLayerMapConverter();
    }

    @Bean
    public BitbakeExtractor bitbakeExtractor() {
        return new BitbakeExtractor(executableRunner, fileFinder, graphParserTransformer(), bitbakeGraphTransformer(), bitbakeRecipesParser(), bitbakeRecipesToLayerMap());
    }

    @Bean
    public GraphParserTransformer graphParserTransformer() {
        return new GraphParserTransformer();
    }

    @Bean
    public BitbakeGraphTransformer bitbakeGraphTransformer() {
        return new BitbakeGraphTransformer(externalIdFactory);
    }

    @Bean
    public ClangPackageManagerInfoFactory clangPackageManagerInfoFactory() {
        return new ClangPackageManagerInfoFactory();
    }

    @Bean
    public ClangPackageManagerFactory clangPackageManagerFactory() {
        return new ClangPackageManagerFactory(clangPackageManagerInfoFactory());
    }

    @Bean
    public ClangPackageManagerRunner clangPackageManagerRunner() {
        return new ClangPackageManagerRunner();
    }

    @Bean
    public GradleInspectorExtractor gradleInspectorExtractor() {
        return new GradleInspectorExtractor(executableRunner, fileFinder, gradleReportParser(), gradleReportTransformer(), gradleRootMetadataParser());
    }

    @Bean
    public GradleInspectorScriptCreator gradleInspectorScriptCreator() {
        return new GradleInspectorScriptCreator(configuration);
    }

    @Bean
    public DockerExtractor dockerExtractor() {
        return new DockerExtractor(fileFinder, new DockerProperties(detectableOptionFactory.createDockerDetectableOptions()), executableRunner, new BdioTransformer(), new ExternalIdFactory(), gson);
    }

    @Bean
    public GemspecLineParser gemspecLineParser() {
        return new GemspecLineParser();
    }

    @Bean
    public GemspecParser gemspecParser() {
        return new GemspecParser(externalIdFactory, gemspecLineParser());
    }

    @Bean
    public GemspecParseExtractor gemspecExtractor() {
        return new GemspecParseExtractor(gemspecParser());
    }

    @Bean
    public GoGradleLockParser goGradleLockParser() {
        return new GoGradleLockParser(externalIdFactory);
    }

    @Bean
    public GoGradleExtractor goGradleExtractor() {
        return new GoGradleExtractor(goGradleLockParser());
    }

    @Bean
    public PackageJsonExtractor packageJsonExtractor() {
        return new PackageJsonExtractor(gson, externalIdFactory);
    }

    @Bean
    public SAXParser saxParser() throws ParserConfigurationException, SAXException {
        return SAXParserFactory.newInstance().newSAXParser();
    }

    @Bean
    public MavenParseExtractor mavenParseExtractor() throws ParserConfigurationException, SAXException {
        return new MavenParseExtractor(externalIdFactory, saxParser(), detectableOptionFactory.createMavenParseOptions());
    }

    @Bean
    public BuildGradleParser buildGradleParser() {
        return new BuildGradleParser(externalIdFactory);
    }

    @Bean
    public GradleParseExtractor gradleParseExtractor() {
        return new GradleParseExtractor(buildGradleParser());
    }

    @Bean
    public SwiftCliParser swiftCliParser() {
        return new SwiftCliParser(gson);
    }

    @Bean
    public SwiftPackageTransformer swiftPackageTransformer() {
        return new SwiftPackageTransformer(externalIdFactory);
    }

    @Bean
    public SwiftExtractor swiftExtractor() {
        return new SwiftExtractor(executableRunner, swiftCliParser(), swiftPackageTransformer());
    }

    //Detectables
    //Should be scoped to Prototype so a new Detectable is created every time one is needed.
    //Should only be accessed through the DetectableFactory.

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public DockerDetectable dockerDetectable(final DetectableEnvironment environment) {
        return new DockerDetectable(environment, dockerInspectorResolver(), detectExecutableResolver, detectExecutableResolver, detectExecutableResolver, dockerExtractor(), detectableOptionFactory.createDockerDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public BazelDetectable bazelDetectable(final DetectableEnvironment environment) {
        return new BazelDetectable(environment, fileFinder, bazelExtractor(), detectExecutableResolver, detectableOptionFactory.createBazelDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public BitbakeDetectable bitbakeBomTool(final DetectableEnvironment environment) {
        return new BitbakeDetectable(environment, fileFinder, detectableOptionFactory.createBitbakeDetectableOptions(), bitbakeExtractor(), detectExecutableResolver);
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public ClangDetectable clangBomTool(final DetectableEnvironment environment) {
        return new ClangDetectable(environment, executableRunner, fileFinder, clangPackageManagerFactory().createPackageManagers(), clangExtractor(), detectableOptionFactory.createClangDetectableOptions(), clangPackageManagerRunner());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public ComposerLockDetectable composerLockBomTool(final DetectableEnvironment environment) {
        return new ComposerLockDetectable(environment, fileFinder, composerLockExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public CondaCliDetectable condaBomTool(final DetectableEnvironment environment) {
        return new CondaCliDetectable(environment, fileFinder, detectExecutableResolver, condaCliExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public CpanCliDetectable cpanCliBomTool(final DetectableEnvironment environment) {
        return new CpanCliDetectable(environment, fileFinder, detectExecutableResolver, detectExecutableResolver, cpanCliExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GemlockDetectable gemlockBomTool(final DetectableEnvironment environment) {
        return new GemlockDetectable(environment, fileFinder, gemlockExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GitParseDetectable gitParseBomTool(final DetectableEnvironment environment) {
        return new GitParseDetectable(environment, fileFinder, gitParseExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GitCliDetectable gitCliBomTool(final DetectableEnvironment environment) {
        return new GitCliDetectable(environment, fileFinder, gitCliExtractor(), detectExecutableResolver);
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GoModCliDetectable goModCliBomTool(final DetectableEnvironment environment) {
        return new GoModCliDetectable(environment, fileFinder, detectExecutableResolver, goModCliExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GoDepLockDetectable goLockBomTool(final DetectableEnvironment environment) {
        return new GoDepLockDetectable(environment, fileFinder, goDepExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GoVndrDetectable goVndrBomTool(final DetectableEnvironment environment) {
        return new GoVndrDetectable(environment, fileFinder, goVndrExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GoVendorDetectable goVendorBomTool(final DetectableEnvironment environment) {
        return new GoVendorDetectable(environment, fileFinder, goVendorExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GoGradleDetectable goGradleDetectable(final DetectableEnvironment environment) {
        return new GoGradleDetectable(environment, fileFinder, goGradleExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GradleDetectable gradleDetectable(final DetectableEnvironment environment) {
        return new GradleDetectable(environment, fileFinder, detectExecutableResolver, gradleInspectorResolver(), gradleInspectorExtractor(), detectableOptionFactory.createGradleInspectorOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GradleParseDetectable gradleParseDetectable(final DetectableEnvironment environment) {
        return new GradleParseDetectable(environment, fileFinder, gradleParseExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public GemspecParseDetectable gemspecParseDetectable(final DetectableEnvironment environment) {
        return new GemspecParseDetectable(environment, fileFinder, gemspecExtractor(), detectableOptionFactory.createGemspecParseDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public MavenPomDetectable mavenPomBomTool(final DetectableEnvironment environment) {
        return new MavenPomDetectable(environment, fileFinder, detectExecutableResolver, mavenCliExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public MavenPomWrapperDetectable mavenPomWrapperBomTool(final DetectableEnvironment environment) {
        return new MavenPomWrapperDetectable(environment, fileFinder, detectExecutableResolver, mavenCliExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public MavenParseDetectable mavenParseDetectable(final DetectableEnvironment environment) throws ParserConfigurationException, SAXException {
        return new MavenParseDetectable(environment, fileFinder, mavenParseExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NpmCliDetectable npmCliBomTool(final DetectableEnvironment environment) {
        return new NpmCliDetectable(environment, fileFinder, detectExecutableResolver, npmCliExtractor(), npmPackageJsonDiscoverer());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NpmPackageLockDetectable npmPackageLockBomTool(final DetectableEnvironment environment) {
        return new NpmPackageLockDetectable(environment, fileFinder, npmLockfileExtractor(), detectableOptionFactory.createNpmLockfileOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NugetProjectDetectable nugetProjectBomTool(final DetectableEnvironment environment) {
        return new NugetProjectDetectable(environment, fileFinder, detectableOptionFactory.createNugetInspectorOptions(), nugetInspectorResolver(), nugetInspectorExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NpmShrinkwrapDetectable npmShrinkwrapBomTool(final DetectableEnvironment environment) {
        return new NpmShrinkwrapDetectable(environment, fileFinder, npmLockfileExtractor(), detectableOptionFactory.createNpmLockfileOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NpmPackageJsonParseDetectable npmPackageJsonParseDetectable(final DetectableEnvironment environment) {
        return new NpmPackageJsonParseDetectable(environment, fileFinder, packageJsonExtractor(), detectableOptionFactory.createNpmPackageJsonParseDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public NugetSolutionDetectable nugetSolutionBomTool(final DetectableEnvironment environment) {
        return new NugetSolutionDetectable(environment, fileFinder, nugetInspectorResolver(), nugetInspectorExtractor(), detectableOptionFactory.createNugetInspectorOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public PackratLockDetectable packratLockBomTool(final DetectableEnvironment environment) {
        return new PackratLockDetectable(environment, fileFinder, packratLockExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public PearCliDetectable pearCliBomTool(final DetectableEnvironment environment) {
        return new PearCliDetectable(environment, fileFinder, detectExecutableResolver, pearCliExtractor(), detectableOptionFactory.createPearCliDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public PipenvDetectable pipenvBomTool(final DetectableEnvironment environment) {
        return new PipenvDetectable(environment, detectableOptionFactory.createPipenvDetectableOptions(), fileFinder, detectExecutableResolver, detectExecutableResolver, pipenvExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public PipInspectorDetectable pipInspectorBomTool(final DetectableEnvironment environment) {
        return new PipInspectorDetectable(environment, fileFinder, detectExecutableResolver, detectExecutableResolver, pipInspectorResolver(), pipInspectorExtractor(), detectableOptionFactory.createPipInspectorDetectableOptions());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public PodlockDetectable podLockBomTool(final DetectableEnvironment environment) {
        return new PodlockDetectable(environment, fileFinder, podlockExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public RebarDetectable rebarBomTool(final DetectableEnvironment environment) {
        return new RebarDetectable(environment, fileFinder, detectExecutableResolver, rebarExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public SbtResolutionCacheDetectable sbtResolutionCacheBomTool(final DetectableEnvironment environment) {
        return new SbtResolutionCacheDetectable(environment, fileFinder, sbtResolutionCacheExtractor());
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public SwiftCliDetectable swiftCliDetectable(final DetectableEnvironment environment) {
        return new SwiftCliDetectable(environment, fileFinder, swiftExtractor(), detectExecutableResolver);
    }

    @Bean
    @Scope(scopeName = BeanDefinition.SCOPE_PROTOTYPE)
    public YarnLockDetectable yarnLockBomTool(final DetectableEnvironment environment) {
        return new YarnLockDetectable(environment, fileFinder, yarnLockExtractor());
    }
}