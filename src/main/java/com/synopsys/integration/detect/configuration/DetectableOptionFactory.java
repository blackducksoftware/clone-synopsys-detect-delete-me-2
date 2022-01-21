package com.synopsys.integration.detect.configuration;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.configuration.property.base.NullableProperty;
import com.synopsys.integration.configuration.property.base.ValuedProperty;
import com.synopsys.integration.configuration.property.types.path.PathResolver;
import com.synopsys.integration.detect.PropertyConfigUtils;
import com.synopsys.integration.detect.tool.detector.inspectors.nuget.NugetLocatorOptions;
import com.synopsys.integration.detect.workflow.ArtifactoryConstants;
import com.synopsys.integration.detect.workflow.diagnostic.DiagnosticSystem;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorOptions;
import com.synopsys.integration.detectable.detectable.util.DependencyTypeFilter;
import com.synopsys.integration.detectable.detectable.util.EnumListFilter;
import com.synopsys.integration.detectable.detectable.util.ExcludedDependencyTypeFilter;
import com.synopsys.integration.detectable.detectables.bazel.BazelDetectableOptions;
import com.synopsys.integration.detectable.detectables.bazel.WorkspaceRule;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDependencyType;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDetectableOptions;
import com.synopsys.integration.detectable.detectables.clang.ClangDetectableOptions;
import com.synopsys.integration.detectable.detectables.conan.cli.config.ConanCliOptions;
import com.synopsys.integration.detectable.detectables.conan.cli.config.ConanDependencyType;
import com.synopsys.integration.detectable.detectables.conan.lockfile.ConanLockfileExtractorOptions;
import com.synopsys.integration.detectable.detectables.conda.CondaCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.dart.pubdep.DartPubDependencyType;
import com.synopsys.integration.detectable.detectables.dart.pubdep.DartPubDepsDetectableOptions;
import com.synopsys.integration.detectable.detectables.docker.DockerDetectableOptions;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModDependencyType;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleConfigurationType;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleInspectorOptions;
import com.synopsys.integration.detectable.detectables.gradle.inspection.inspector.GradleInspectorScriptOptions;
import com.synopsys.integration.detectable.detectables.lerna.LernaOptions;
import com.synopsys.integration.detectable.detectables.lerna.LernaPackageType;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenCliExtractorOptions;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseOptions;
import com.synopsys.integration.detectable.detectables.npm.NpmDependencyType;
import com.synopsys.integration.detectable.detectables.npm.cli.NpmCliExtractorOptions;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmLockfileOptions;
import com.synopsys.integration.detectable.detectables.npm.packagejson.NpmPackageJsonParseDetectableOptions;
import com.synopsys.integration.detectable.detectables.packagist.ComposerLockDetectableOptions;
import com.synopsys.integration.detectable.detectables.packagist.PackagistDependencyType;
import com.synopsys.integration.detectable.detectables.pear.PearCliDetectableOptions;
import com.synopsys.integration.detectable.detectables.pear.PearDependencyType;
import com.synopsys.integration.detectable.detectables.pip.inspector.PipInspectorDetectableOptions;
import com.synopsys.integration.detectable.detectables.pipenv.PipenvDetectableOptions;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.PnpmLockOptions;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmDependencyType;
import com.synopsys.integration.detectable.detectables.projectinspector.ProjectInspectorOptions;
import com.synopsys.integration.detectable.detectables.rubygems.GemspecDependencyType;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.GemspecParseDetectableOptions;
import com.synopsys.integration.detectable.detectables.sbt.parse.SbtResolutionCacheOptions;
import com.synopsys.integration.detectable.detectables.yarn.YarnDependencyType;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockOptions;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class DetectableOptionFactory {

    private final PropertyConfiguration detectConfiguration;
    @Nullable
    private final DiagnosticSystem diagnosticSystem;
    private final PathResolver pathResolver;
    private final ProxyInfo proxyInfo;

    public DetectableOptionFactory(PropertyConfiguration detectConfiguration, @Nullable DiagnosticSystem diagnosticSystem, PathResolver pathResolver, ProxyInfo proxyInfo) {
        this.detectConfiguration = detectConfiguration;
        this.diagnosticSystem = diagnosticSystem;
        this.pathResolver = pathResolver;
        this.proxyInfo = proxyInfo;
    }

    public BazelDetectableOptions createBazelDetectableOptions() {
        String targetName = getNullableValue(DetectProperties.DETECT_BAZEL_TARGET);
        List<String> bazelCqueryAdditionalOptions = getValue(DetectProperties.DETECT_BAZEL_CQUERY_OPTIONS);
        Set<WorkspaceRule> bazelDependencyRules = PropertyConfigUtils.getAllNoneList(detectConfiguration, DetectProperties.DETECT_BAZEL_DEPENDENCY_RULE.getProperty()).representedValueSet();
        return new BazelDetectableOptions(targetName, bazelDependencyRules, bazelCqueryAdditionalOptions);
    }

    public BitbakeDetectableOptions createBitbakeDetectableOptions() {
        String buildEnvName = getValue(DetectProperties.DETECT_BITBAKE_BUILD_ENV_NAME);
        List<String> sourceArguments = getValue(DetectProperties.DETECT_BITBAKE_SOURCE_ARGUMENTS);
        List<String> packageNames = getValue(DetectProperties.DETECT_BITBAKE_PACKAGE_NAMES);
        Integer searchDepth = getValue(DetectProperties.DETECT_BITBAKE_SEARCH_DEPTH);
        List<BitbakeDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_BITBAKE_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
        ExcludedDependencyTypeFilter<BitbakeDependencyType> dependencyTypeFilter = new ExcludedDependencyTypeFilter<>(excludedDependencyTypes);
        return new BitbakeDetectableOptions(buildEnvName, sourceArguments, packageNames, searchDepth, getFollowSymLinks(), dependencyTypeFilter);
    }

    public ClangDetectableOptions createClangDetectableOptions() {
        Boolean cleanup = getValue(DetectProperties.DETECT_CLEANUP);
        return new ClangDetectableOptions(cleanup);
    }

    public ComposerLockDetectableOptions createComposerLockDetectableOptions() {

        EnumListFilter<PackagistDependencyType> packagistDependencyTypeFilter;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_PACKAGIST_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            List<PackagistDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_PACKAGIST_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
            packagistDependencyTypeFilter = EnumListFilter.fromExcluded(excludedDependencyTypes);
        } else {
            boolean includedDevDependencies = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_PACKAGIST_INCLUDE_DEV_DEPENDENCIES));
            if (includedDevDependencies) {
                packagistDependencyTypeFilter = EnumListFilter.excludeNone();
            } else {
                packagistDependencyTypeFilter = EnumListFilter.fromExcluded(PackagistDependencyType.DEV);
            }
        }

        return new ComposerLockDetectableOptions(packagistDependencyTypeFilter);
    }

    public CondaCliDetectableOptions createCondaOptions() {
        String environmentName = getNullableValue(DetectProperties.DETECT_CONDA_ENVIRONMENT_NAME);
        return new CondaCliDetectableOptions(environmentName);
    }

    public DartPubDepsDetectableOptions createDartPubDepsDetectableOptions() {
        EnumListFilter<DartPubDependencyType> dependencyTypeFilter = EnumListFilter.excludeNone();
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_PUB_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            Set<DartPubDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_PUB_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValueSet();
            dependencyTypeFilter = EnumListFilter.fromExcluded(excludedDependencyTypes);
        } else {
            boolean excludeDevDependencies = getValue(DetectProperties.DETECT_PUD_DEPS_EXCLUDE_DEV);
            if (excludeDevDependencies) {
                dependencyTypeFilter = EnumListFilter.fromExcluded(DartPubDependencyType.DEV);
            }
        }
        return new DartPubDepsDetectableOptions(dependencyTypeFilter);
    }

    public MavenParseOptions createMavenParseOptions() {
        Boolean includePlugins = getValue(DetectProperties.DETECT_MAVEN_INCLUDE_PLUGINS);
        Boolean legacyMode = getValue(DetectProperties.DETECT_MAVEN_BUILDLESS_LEGACY_MODE);
        return new MavenParseOptions(includePlugins, legacyMode);
    }

    public DockerDetectableOptions createDockerDetectableOptions() {
        Boolean dockerPathRequired = getValue(DetectProperties.DETECT_DOCKER_PATH_REQUIRED);
        String suppliedDockerImage = getNullableValue(DetectProperties.DETECT_DOCKER_IMAGE);
        String dockerImageId = getNullableValue(DetectProperties.DETECT_DOCKER_IMAGE_ID);
        String suppliedDockerTar = getNullableValue(DetectProperties.DETECT_DOCKER_TAR);
        LogLevel dockerInspectorLoggingLevel;
        if (detectConfiguration.wasKeyProvided(DetectProperties.LOGGING_LEVEL_DETECT.getProperty().getKey())) {
            dockerInspectorLoggingLevel = getValue(DetectProperties.LOGGING_LEVEL_DETECT);
        } else {
            dockerInspectorLoggingLevel = getValue(DetectProperties.LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION);
        }
        String dockerInspectorVersion = getNullableValue(DetectProperties.DETECT_DOCKER_INSPECTOR_VERSION);
        Map<String, String> additionalDockerProperties = detectConfiguration.getRaw(DetectProperties.DOCKER_PASSTHROUGH.getProperty());
        if (diagnosticSystem != null) {
            additionalDockerProperties.putAll(diagnosticSystem.getAdditionalDockerProperties());
        }

        Path dockerInspectorPath = detectConfiguration.getValue(DetectProperties.DETECT_DOCKER_INSPECTOR_PATH.getProperty()).map(path -> path.resolvePath(pathResolver)).orElse(null);
        String dockerPlatformTopLayerId = getNullableValue(DetectProperties.DETECT_DOCKER_PLATFORM_TOP_LAYER_ID);
        return new DockerDetectableOptions(dockerPathRequired, suppliedDockerImage, dockerImageId, suppliedDockerTar, dockerInspectorLoggingLevel, dockerInspectorVersion, additionalDockerProperties, dockerInspectorPath,
            dockerPlatformTopLayerId);
    }

    public GoModCliDetectableOptions createGoModCliDetectableOptions() {
        EnumListFilter<GoModDependencyType> dependencyTypeFilter = EnumListFilter.excludeNone();
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_GO_MOD_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            List<GoModDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_GO_MOD_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
            dependencyTypeFilter = EnumListFilter.fromExcluded(excludedDependencyTypes);
        } else {
            boolean dependencyVerificationEnabled = getValue(DetectProperties.DETECT_GO_ENABLE_VERIFICATION);
            if (dependencyVerificationEnabled) {
                dependencyTypeFilter = EnumListFilter.fromExcluded(GoModDependencyType.UNUSED);
            }
        }

        return new GoModCliDetectableOptions(dependencyTypeFilter);
    }

    public GradleInspectorOptions createGradleInspectorOptions() {
        List<String> excludedProjectNames = getValue(DetectProperties.DETECT_GRADLE_EXCLUDED_PROJECTS);
        List<String> includedProjectNames = getValue(DetectProperties.DETECT_GRADLE_INCLUDED_PROJECTS);
        List<String> excludedConfigurationNames = getValue(DetectProperties.DETECT_GRADLE_EXCLUDED_CONFIGURATIONS);
        List<String> includedConfigurationNames = getValue(DetectProperties.DETECT_GRADLE_INCLUDED_CONFIGURATIONS);
        String customRepository = ArtifactoryConstants.GRADLE_INSPECTOR_MAVEN_REPO;

        EnumListFilter<GradleConfigurationType> dependencyTypeFilter;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_GRADLE_CONFIGURATION_TYPES_EXCLUDED.getProperty())) {
            List<GradleConfigurationType> excludedConfigurationTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_GRADLE_CONFIGURATION_TYPES_EXCLUDED.getProperty()).representedValues();
            dependencyTypeFilter = EnumListFilter.fromExcluded(excludedConfigurationTypes);
        } else {
            boolean includeUnresolvedConfigurations = getValue(DetectProperties.DETECT_GRADLE_INCLUDE_UNRESOLVED_CONFIGURATIONS);
            if (includeUnresolvedConfigurations) {
                dependencyTypeFilter = EnumListFilter.excludeNone();
            } else {
                dependencyTypeFilter = EnumListFilter.fromExcluded(GradleConfigurationType.UNRESOLVED);
            }
        }

        String onlineInspectorVersion = getNullableValue(DetectProperties.DETECT_GRADLE_INSPECTOR_VERSION);
        GradleInspectorScriptOptions scriptOptions = new GradleInspectorScriptOptions(excludedProjectNames, includedProjectNames, excludedConfigurationNames, includedConfigurationNames, customRepository, onlineInspectorVersion);
        String gradleBuildCommand = getNullableValue(DetectProperties.DETECT_GRADLE_BUILD_COMMAND);
        return new GradleInspectorOptions(gradleBuildCommand, scriptOptions, proxyInfo, dependencyTypeFilter);
    }

    public LernaOptions createLernaOptions() {
        EnumListFilter<LernaPackageType> lernaPackageTypeFilter;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_LERNA_PACKAGE_TYPES_EXCLUDED.getProperty())) {
            List<LernaPackageType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_LERNA_PACKAGE_TYPES_EXCLUDED.getProperty()).representedValues();
            lernaPackageTypeFilter = EnumListFilter.fromExcluded(excludedDependencyTypes);
        } else {
            boolean includePrivate = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_LERNA_INCLUDE_PRIVATE));
            if (includePrivate) {
                lernaPackageTypeFilter = EnumListFilter.excludeNone();
            } else {
                lernaPackageTypeFilter = EnumListFilter.fromExcluded(LernaPackageType.PRIVATE);
            }
        }

        List<String> excludedPackages = getValue(DetectProperties.DETECT_LERNA_EXCLUDED_PACKAGES);
        List<String> includedPackages = getValue(DetectProperties.DETECT_LERNA_INCLUDED_PACKAGES);
        return new LernaOptions(lernaPackageTypeFilter, excludedPackages, includedPackages);
    }

    public MavenCliExtractorOptions createMavenCliOptions() {
        String mavenBuildCommand = getNullableValue(DetectProperties.DETECT_MAVEN_BUILD_COMMAND);
        List<String> mavenExcludedScopes = getValue(DetectProperties.DETECT_MAVEN_EXCLUDED_SCOPES);
        List<String> mavenIncludedScopes = getValue(DetectProperties.DETECT_MAVEN_INCLUDED_SCOPES);
        List<String> mavenExcludedModules = getValue(DetectProperties.DETECT_MAVEN_EXCLUDED_MODULES);
        List<String> mavenIncludedModules = getValue(DetectProperties.DETECT_MAVEN_INCLUDED_MODULES);
        return new MavenCliExtractorOptions(mavenBuildCommand, mavenExcludedScopes, mavenIncludedScopes, mavenExcludedModules, mavenIncludedModules);
    }

    public ConanCliOptions createConanCliOptions() {
        Path lockfilePath = detectConfiguration.getValue(DetectProperties.DETECT_CONAN_LOCKFILE_PATH.getProperty()).map(path -> path.resolvePath(pathResolver)).orElse(null);
        String additionalArguments = getNullableValue(DetectProperties.DETECT_CONAN_ARGUMENTS);
        Boolean preferLongFormExternalIds = getValue(DetectProperties.DETECT_CONAN_REQUIRE_PREV_MATCH);
        EnumListFilter<ConanDependencyType> dependencyTypeFilter = createConanDependencyTypeFilter();
        return new ConanCliOptions(lockfilePath, additionalArguments, dependencyTypeFilter, preferLongFormExternalIds);
    }

    // TODO: Remove in 8.0.0. This will be one line, no method necessary - JM 01/2022
    private EnumListFilter<ConanDependencyType> createConanDependencyTypeFilter() {
        Boolean includeBuildDependencies = getValue(DetectProperties.DETECT_CONAN_INCLUDE_BUILD_DEPENDENCIES);
        Set<ConanDependencyType> excludedDependencyTypes;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_CONAN_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_CONAN_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValueSet();
        } else {
            excludedDependencyTypes = new LinkedHashSet<>();
            if (Boolean.FALSE.equals(includeBuildDependencies)) {
                excludedDependencyTypes.add(ConanDependencyType.BUILD);
            }
        }
        return EnumListFilter.fromExcluded(excludedDependencyTypes);
    }

    public ConanLockfileExtractorOptions createConanLockfileOptions() {
        Path lockfilePath = detectConfiguration.getValue(DetectProperties.DETECT_CONAN_LOCKFILE_PATH.getProperty()).map(path -> path.resolvePath(pathResolver)).orElse(null);
        Boolean preferLongFormExternalIds = getValue(DetectProperties.DETECT_CONAN_REQUIRE_PREV_MATCH);
        EnumListFilter<ConanDependencyType> dependencyTypeFilter = createConanDependencyTypeFilter();
        return new ConanLockfileExtractorOptions(lockfilePath, dependencyTypeFilter, preferLongFormExternalIds);
    }

    public NpmCliExtractorOptions createNpmCliExtractorOptions() {
        NpmDependencyTypeOptions npmDependencyTypeOptions = createNpmDependencyTypeOptions();
        String npmArguments = getNullableValue(DetectProperties.DETECT_NPM_ARGUMENTS);
        return new NpmCliExtractorOptions(npmDependencyTypeOptions.includeDevDependencies, npmDependencyTypeOptions.includePeerDependencies, npmArguments);
    }

    public NpmLockfileOptions createNpmLockfileOptions() {
        EnumListFilter<NpmDependencyType> npmDependencyTypeFilter = createNpmDependencyTypeFilter();
        return new NpmLockfileOptions(npmDependencyTypeFilter);
    }

    public NpmPackageJsonParseDetectableOptions createNpmPackageJsonParseDetectableOptions() {
        EnumListFilter<NpmDependencyType> npmDependencyTypeFilter = createNpmDependencyTypeFilter();
        return new NpmPackageJsonParseDetectableOptions(npmDependencyTypeFilter);
    }

    private EnumListFilter<NpmDependencyType> createNpmDependencyTypeFilter() {
        Set<NpmDependencyType> excludedDependencyTypes;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValueSet();
        } else {
            boolean excludeDevDependencies = Boolean.FALSE.equals(getValue(DetectProperties.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES));
            boolean excludePeerDependencies = Boolean.FALSE.equals(getValue(DetectProperties.DETECT_NPM_INCLUDE_PEER_DEPENDENCIES));
            excludedDependencyTypes = new LinkedHashSet<>();
            if (excludeDevDependencies) {
                excludedDependencyTypes.add(NpmDependencyType.DEV);
            }
            if (excludePeerDependencies) {
                excludedDependencyTypes.add(NpmDependencyType.DEV);
            }
        }
        return EnumListFilter.fromExcluded(excludedDependencyTypes);
    }

    private NpmDependencyTypeOptions createNpmDependencyTypeOptions() {
        boolean includeDevDependencies = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_NPM_INCLUDE_DEV_DEPENDENCIES));
        boolean includePeerDependencies = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_NPM_INCLUDE_PEER_DEPENDENCIES));
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            List<NpmDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_NPM_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
            ExcludedDependencyTypeFilter<NpmDependencyType> dependencyTypeFilter = new ExcludedDependencyTypeFilter<>(excludedDependencyTypes);
            includeDevDependencies = dependencyTypeFilter.shouldReportDependencyType(NpmDependencyType.DEV);
            includePeerDependencies = dependencyTypeFilter.shouldReportDependencyType(NpmDependencyType.PEER);
        }
        return new NpmDependencyTypeOptions(includeDevDependencies, includePeerDependencies);
    }

    private static class NpmDependencyTypeOptions {
        public final boolean includeDevDependencies;
        public final boolean includePeerDependencies;

        private NpmDependencyTypeOptions(boolean includeDevDependencies, boolean includePeerDependencies) {
            this.includeDevDependencies = includeDevDependencies;
            this.includePeerDependencies = includePeerDependencies;
        }
    }

    public PearCliDetectableOptions createPearCliDetectableOptions() {
        boolean onlyGatherRequired = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_PEAR_ONLY_REQUIRED_DEPS));
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_PEAR_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            List<PearDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_PEAR_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
            ExcludedDependencyTypeFilter<PearDependencyType> dependencyTypeFilter = new ExcludedDependencyTypeFilter<>(excludedDependencyTypes);
            onlyGatherRequired = dependencyTypeFilter.shouldExcludeDependencyType(PearDependencyType.OPTIONAL);
        }
        return new PearCliDetectableOptions(onlyGatherRequired);
    }

    public PipenvDetectableOptions createPipenvDetectableOptions() {
        String pipProjectName = getNullableValue(DetectProperties.DETECT_PIP_PROJECT_NAME);
        String pipProjectVersionName = getNullableValue(DetectProperties.DETECT_PIP_PROJECT_VERSION_NAME);
        Boolean pipProjectTreeOnly = getValue(DetectProperties.DETECT_PIP_ONLY_PROJECT_TREE);
        return new PipenvDetectableOptions(pipProjectName, pipProjectVersionName, pipProjectTreeOnly);
    }

    public PipInspectorDetectableOptions createPipInspectorDetectableOptions() {
        String pipProjectName = getNullableValue(DetectProperties.DETECT_PIP_PROJECT_NAME);
        List<Path> requirementsFilePath = getValue(DetectProperties.DETECT_PIP_REQUIREMENTS_PATH).stream()
            .map(it -> it.resolvePath(pathResolver))
            .collect(Collectors.toList());
        return new PipInspectorDetectableOptions(pipProjectName, requirementsFilePath);
    }

    public PnpmLockOptions createPnpmLockOptions() {
        List<PnpmDependencyType> pnpmDependencyTypes = PropertyConfigUtils.getAllNoneList(detectConfiguration, DetectProperties.DETECT_PNPM_DEPENDENCY_TYPES.getProperty()).representedValues();
        DependencyTypeFilter<PnpmDependencyType> dependencyTypeFilter = new DependencyTypeFilter<>(pnpmDependencyTypes);
        return new PnpmLockOptions(dependencyTypeFilter);
    }

    public ProjectInspectorOptions createProjectInspectorOptions() {
        String additionalArguments = detectConfiguration.getValue(DetectProperties.PROJECT_INSPECTOR_ARGUMENTS.getProperty()).orElse(null);
        return new ProjectInspectorOptions(additionalArguments);
    }

    public GemspecParseDetectableOptions createGemspecParseDetectableOptions() {
        boolean includeRuntimeDependencies = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_RUBY_INCLUDE_RUNTIME_DEPENDENCIES));
        boolean includeDevDependencies = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_RUBY_INCLUDE_DEV_DEPENDENCIES));
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_RUBY_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            List<GemspecDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_RUBY_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValues();
            ExcludedDependencyTypeFilter<GemspecDependencyType> dependencyTypeFilter = new ExcludedDependencyTypeFilter<>(excludedDependencyTypes);
            includeRuntimeDependencies = dependencyTypeFilter.shouldReportDependencyType(GemspecDependencyType.RUNTIME);
            includeDevDependencies = dependencyTypeFilter.shouldReportDependencyType(GemspecDependencyType.DEV);
        }
        return new GemspecParseDetectableOptions(includeRuntimeDependencies, includeDevDependencies);
    }

    public SbtResolutionCacheOptions createSbtResolutionCacheDetectableOptions() {
        String sbtCommandAdditionalArguments = getNullableValue(DetectProperties.DETECT_SBT_ARGUMENTS);
        List<String> includedConfigurations = getValue(DetectProperties.DETECT_SBT_INCLUDED_CONFIGURATIONS);
        List<String> excludedConfigurations = getValue(DetectProperties.DETECT_SBT_EXCLUDED_CONFIGURATIONS);
        Integer reportDepth = getValue(DetectProperties.DETECT_SBT_REPORT_DEPTH);
        return new SbtResolutionCacheOptions(sbtCommandAdditionalArguments, includedConfigurations, excludedConfigurations, reportDepth, getFollowSymLinks());
    }

    public YarnLockOptions createYarnLockOptions() {
        EnumListFilter<YarnDependencyType> yarnDependencyTypeFilter;
        if (detectConfiguration.wasPropertyProvided(DetectProperties.DETECT_YARN_DEPENDENCY_TYPES_EXCLUDED.getProperty())) {
            Set<YarnDependencyType> excludedDependencyTypes = PropertyConfigUtils.getNoneList(detectConfiguration, DetectProperties.DETECT_YARN_DEPENDENCY_TYPES_EXCLUDED.getProperty()).representedValueSet();
            yarnDependencyTypeFilter = EnumListFilter.fromExcluded(excludedDependencyTypes);
        } else {
            boolean useProductionOnly = Boolean.TRUE.equals(getValue(DetectProperties.DETECT_YARN_PROD_ONLY));
            if (useProductionOnly) {
                yarnDependencyTypeFilter = EnumListFilter.fromExcluded(YarnDependencyType.NON_PRODUCTION);
            } else {
                yarnDependencyTypeFilter = EnumListFilter.excludeNone();
            }
        }
        List<String> excludedWorkspaces = getValue(DetectProperties.DETECT_YARN_EXCLUDED_WORKSPACES);
        List<String> includedWorkspaces = getValue(DetectProperties.DETECT_YARN_INCLUDED_WORKSPACES);
        return new YarnLockOptions(yarnDependencyTypeFilter, excludedWorkspaces, includedWorkspaces);
    }

    public NugetInspectorOptions createNugetInspectorOptions() {
        Boolean ignoreFailures = getValue(DetectProperties.DETECT_NUGET_IGNORE_FAILURE);
        List<String> excludedModules = getValue(DetectProperties.DETECT_NUGET_EXCLUDED_MODULES);
        List<String> includedModules = getValue(DetectProperties.DETECT_NUGET_INCLUDED_MODULES);
        List<String> packagesRepoUrl = getValue(DetectProperties.DETECT_NUGET_PACKAGES_REPO_URL);
        Path nugetConfigPath = detectConfiguration.getValue(DetectProperties.DETECT_NUGET_CONFIG_PATH.getProperty()).map(path -> path.resolvePath(pathResolver)).orElse(null);
        return new NugetInspectorOptions(ignoreFailures, excludedModules, includedModules, packagesRepoUrl, nugetConfigPath);
    }

    public NugetLocatorOptions createNugetInstallerOptions() {
        List<String> packagesRepoUrl = getValue(DetectProperties.DETECT_NUGET_PACKAGES_REPO_URL);
        String nugetInspectorVersion = getNullableValue(DetectProperties.DETECT_NUGET_INSPECTOR_VERSION);
        return new NugetLocatorOptions(packagesRepoUrl, nugetInspectorVersion);
    }

    private <P, T extends NullableProperty<P>> P getNullableValue(DetectProperty<T> detectProperty) {
        return detectConfiguration.getValue(detectProperty.getProperty()).orElse(null);
    }

    private <P, T extends ValuedProperty<P>> P getValue(DetectProperty<T> detectProperty) {
        return detectConfiguration.getValue(detectProperty.getProperty());
    }

    private boolean getFollowSymLinks() {
        return getValue(DetectProperties.DETECT_FOLLOW_SYMLINKS);
    }
}
