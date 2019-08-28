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
package com.synopsys.integration.detect.configuration;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.tool.detector.inspectors.nuget.NugetLocatorOptions;
import com.synopsys.integration.detect.workflow.ArtifactoryConstants;
import com.synopsys.integration.detectable.detectable.executable.impl.CachedExecutableResolverOptions;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorOptions;
import com.synopsys.integration.detectable.detectables.bazel.BazelDetectableOptions;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDetectableOptions;
import com.synopsys.integration.detectable.detectables.clang.ClangDetectableOptions;
import com.synopsys.integration.detectable.detectables.conda.CondaCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.docker.DockerDetectableOptions;
import com.synopsys.integration.detectable.detectables.go.godep.GoDepCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleInspectorOptions;
import com.synopsys.integration.detectable.detectables.gradle.inspection.inspector.GradleInspectorScriptOptions;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenCliExtractorOptions;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseOptions;
import com.synopsys.integration.detectable.detectables.npm.cli.NpmCliExtractorOptions;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmLockfileOptions;
import com.synopsys.integration.detectable.detectables.npm.packagejson.NpmPackageJsonParseDetectableOptions;
import com.synopsys.integration.detectable.detectables.packagist.ComposerLockDetectableOptions;
import com.synopsys.integration.detectable.detectables.pear.PearCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.pip.PipInspectorDetectableOptions;
import com.synopsys.integration.detectable.detectables.pip.PipenvDetectableOptions;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.GemspecParseDetectableOptions;
import com.synopsys.integration.detectable.detectables.sbt.SbtResolutionCacheDetectableOptions;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockOptions;

public class DetectableOptionFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectConfiguration detectConfiguration;

    public DetectableOptionFactory(final DetectConfiguration detectConfiguration) {
        this.detectConfiguration = detectConfiguration;
    }

    public BazelDetectableOptions createBazelDetectableOptions() {
        final String targetName = detectConfiguration.getProperty(DetectProperty.DETECT_BAZEL_TARGET, PropertyAuthority.None);
        final String fullRulesPath = detectConfiguration.getProperty(DetectProperty.DETECT_BAZEL_ADVANCED_RULES_PATH, PropertyAuthority.None);
        return new BazelDetectableOptions(targetName, fullRulesPath);
    }

    public BitbakeDetectableOptions createBitbakeDetectableOptions() {
        final String buildEnvName = detectConfiguration.getProperty(DetectProperty.DETECT_BITBAKE_BUILD_ENV_NAME, PropertyAuthority.None);
        final String[] sourceArguments = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BITBAKE_SOURCE_ARGUMENTS, PropertyAuthority.None);
        final String[] packageNames = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_BITBAKE_PACKAGE_NAMES, PropertyAuthority.None);
        return new BitbakeDetectableOptions(buildEnvName, sourceArguments, packageNames);
    }

    public ClangDetectableOptions createClangDetectableOptions() {
        final boolean cleanup = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_CLEANUP, PropertyAuthority.None);
        return new ClangDetectableOptions(cleanup);
    }

    public ComposerLockDetectableOptions createComposerLockDetectableOptions() {
        final boolean includedDevDependencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PACKAGIST_INCLUDE_DEV_DEPENDENCIES, PropertyAuthority.None);
        return new ComposerLockDetectableOptions(includedDevDependencies);
    }

    public CondaCliDetectableOptions createCondaOptions() {
        final String environmentName = detectConfiguration.getProperty(DetectProperty.DETECT_CONDA_ENVIRONMENT_NAME, PropertyAuthority.None);
        return new CondaCliDetectableOptions(environmentName);
    }

    public MavenParseOptions createMavenParseOptions() {
        final Boolean includePlugins = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_MAVEN_INCLUDE_PLUGINS, PropertyAuthority.None);
        return new MavenParseOptions(includePlugins);
    }

    public DockerDetectableOptions createDockerDetectableOptions() {
        final boolean dockerPathRequired = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_DOCKER_PATH_REQUIRED, PropertyAuthority.None);
        final String suppliedDockerImage = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_IMAGE, PropertyAuthority.None);
        final String suppliedDockerTar = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_TAR, PropertyAuthority.None);
        final String dockerInspectorLoggingLevel = detectConfiguration.getProperty(DetectProperty.LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION, PropertyAuthority.None);
        final String dockerInspectorVersion = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_INSPECTOR_VERSION, PropertyAuthority.None);
        final Map<String, String> additionalDockerProperties = detectConfiguration.getDockerProperties();
        final String dockerInspectorPath = detectConfiguration.getProperty(DetectProperty.DETECT_DOCKER_INSPECTOR_PATH, PropertyAuthority.None);
        return new DockerDetectableOptions(dockerPathRequired, suppliedDockerImage, suppliedDockerTar, dockerInspectorLoggingLevel, dockerInspectorVersion, additionalDockerProperties, dockerInspectorPath);
    }

    public GradleInspectorOptions createGradleInspectorOptions() {
        final String excludedProjectNames = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_EXCLUDED_PROJECTS, PropertyAuthority.None);
        final String includedProjectNames = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INCLUDED_PROJECTS, PropertyAuthority.None);
        final String excludedConfigurationNames = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_EXCLUDED_CONFIGURATIONS, PropertyAuthority.None);
        final String includedConfigurationNames = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INCLUDED_CONFIGURATIONS, PropertyAuthority.None);
        final String configuredGradleInspectorRepositoryUrl = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INSPECTOR_REPOSITORY_URL, PropertyAuthority.None);
        String customRepository = ArtifactoryConstants.GRADLE_INSPECTOR_MAVEN_REPO;
        if (StringUtils.isNotBlank(configuredGradleInspectorRepositoryUrl)) {
            logger.warn("Using a custom gradle repository will not be supported in the future.");
            customRepository = configuredGradleInspectorRepositoryUrl;
        }

        final String onlineInspectorVersion = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_INSPECTOR_VERSION, PropertyAuthority.None);
        final GradleInspectorScriptOptions scriptOptions = new GradleInspectorScriptOptions(excludedProjectNames, includedProjectNames, excludedConfigurationNames, includedConfigurationNames, customRepository, onlineInspectorVersion);
        final String gradleBuildCommand = detectConfiguration.getProperty(DetectProperty.DETECT_GRADLE_BUILD_COMMAND, PropertyAuthority.None);
        return new GradleInspectorOptions(gradleBuildCommand, scriptOptions);
    }

    public GoDepCliDetectableOptions createGoDepCliDetectableOptions() {
        final boolean allowsInitRun = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_GO_RUN_DEP_INIT, PropertyAuthority.None);
        return new GoDepCliDetectableOptions(allowsInitRun);
    }

    public MavenCliExtractorOptions createMavenCliOptions() {
        final String mavenBuildCommand = detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_BUILD_COMMAND, PropertyAuthority.None);
        final String mavenScope = detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_SCOPE, PropertyAuthority.None);
        final String mavenExcludedModules = detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_EXCLUDED_MODULES, PropertyAuthority.None);
        final String mavenIncludedModules = detectConfiguration.getProperty(DetectProperty.DETECT_MAVEN_INCLUDED_MODULES, PropertyAuthority.None);
        return new MavenCliExtractorOptions(mavenBuildCommand, mavenScope, mavenExcludedModules, mavenIncludedModules);
    }

    public NpmCliExtractorOptions createNpmCliExtractorOptions() {
        final boolean includeDevDependencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES, PropertyAuthority.None);
        final String npmArguments = detectConfiguration.getProperty(DetectProperty.DETECT_NPM_ARGUMENTS, PropertyAuthority.None);
        return new NpmCliExtractorOptions(includeDevDependencies, npmArguments);
    }

    public NpmLockfileOptions createNpmLockfileOptions() {
        final boolean includeDevDependencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES, PropertyAuthority.None);
        return new NpmLockfileOptions(includeDevDependencies);
    }

    public NpmPackageJsonParseDetectableOptions createNpmPackageJsonParseDetectableOptions() {
        final boolean includeDevDependencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES, PropertyAuthority.None);
        return new NpmPackageJsonParseDetectableOptions(includeDevDependencies);
    }

    public PearCliDetectableOptions createPearCliDetectableOptions() {
        final boolean onlyGatherRequired = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PEAR_ONLY_REQUIRED_DEPS, PropertyAuthority.None);
        return new PearCliDetectableOptions(onlyGatherRequired);
    }

    public PipenvDetectableOptions createPipenvDetectableOptions() {
        final String pipProjectName = detectConfiguration.getProperty(DetectProperty.DETECT_PIP_PROJECT_NAME, PropertyAuthority.None);
        final String pipProjectVersionName = detectConfiguration.getProperty(DetectProperty.DETECT_PIP_PROJECT_VERSION_NAME, PropertyAuthority.None);
        return new PipenvDetectableOptions(pipProjectName, pipProjectVersionName);
    }

    public PipInspectorDetectableOptions createPipInspectorDetectableOptions() {
        final String pipProjectName = detectConfiguration.getProperty(DetectProperty.DETECT_PIP_PROJECT_NAME, PropertyAuthority.None);
        final String[] requirementsFilePath = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_PIP_REQUIREMENTS_PATH, PropertyAuthority.None);
        return new PipInspectorDetectableOptions(pipProjectName, requirementsFilePath);
    }

    public GemspecParseDetectableOptions createGemspecParseDetectableOptions() {
        final boolean includeRuntimeDependencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_RUBY_INCLUDE_RUNTIME_DEPENDENCIES, PropertyAuthority.None);
        final boolean includeDevDeopendencies = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_RUBY_INCLUDE_DEV_DEPENDENCIES, PropertyAuthority.None);
        return new GemspecParseDetectableOptions(includeRuntimeDependencies, includeDevDeopendencies);
    }

    public SbtResolutionCacheDetectableOptions createSbtResolutionCacheDetectableOptions() {
        final String includedConfigurations = detectConfiguration.getProperty(DetectProperty.DETECT_SBT_INCLUDED_CONFIGURATIONS, PropertyAuthority.None);
        final String excludedConfigurations = detectConfiguration.getProperty(DetectProperty.DETECT_SBT_EXCLUDED_CONFIGURATIONS, PropertyAuthority.None);
        final int reportDepth = detectConfiguration.getIntegerProperty(DetectProperty.DETECT_SBT_REPORT_DEPTH, PropertyAuthority.None);
        return new SbtResolutionCacheDetectableOptions(includedConfigurations, excludedConfigurations, reportDepth);
    }

    public YarnLockOptions createYarnLockOptions() {
        final boolean useProductionOnly = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_YARN_PROD_ONLY, PropertyAuthority.None);
        return new YarnLockOptions(useProductionOnly);
    }

    public NugetInspectorOptions createNugetInspectorOptions() {
        final boolean ignoreFailures = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_NUGET_IGNORE_FAILURE, PropertyAuthority.None);
        final String excludedModules = detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_EXCLUDED_MODULES, PropertyAuthority.None);
        final String includedModules = detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INCLUDED_MODULES, PropertyAuthority.None);
        final String[] packagesRepoUrl = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_NUGET_PACKAGES_REPO_URL, PropertyAuthority.None);
        final String nugetConfigPath = detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_CONFIG_PATH, PropertyAuthority.None);
        return new NugetInspectorOptions(ignoreFailures, excludedModules, includedModules, packagesRepoUrl, nugetConfigPath);
    }

    public NugetLocatorOptions createNugetInstallerOptions() {
        final String[] packagesRepoUrl = detectConfiguration.getStringArrayProperty(DetectProperty.DETECT_NUGET_PACKAGES_REPO_URL, PropertyAuthority.None);
        final String nugetInspectorName = detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INSPECTOR_NAME, PropertyAuthority.None);
        final String nugetInspectorVersion = detectConfiguration.getProperty(DetectProperty.DETECT_NUGET_INSPECTOR_VERSION, PropertyAuthority.None);
        return new NugetLocatorOptions(packagesRepoUrl, nugetInspectorName, nugetInspectorVersion);
    }

    public CachedExecutableResolverOptions createCachedExecutableResolverOptions() {
        final boolean python3 = detectConfiguration.getBooleanProperty(DetectProperty.DETECT_PYTHON_PYTHON3, PropertyAuthority.None);
        return new CachedExecutableResolverOptions(python3);
    }
}
