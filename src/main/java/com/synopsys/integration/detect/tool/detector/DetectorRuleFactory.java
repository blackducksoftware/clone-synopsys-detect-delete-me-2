package com.synopsys.integration.detect.tool.detector;

import com.synopsys.integration.detect.tool.detector.factory.DetectDetectableFactory;
import com.synopsys.integration.detectable.detectables.bitbake.BitbakeDetectable;
import com.synopsys.integration.detectable.detectables.cargo.CargoDetectable;
import com.synopsys.integration.detectable.detectables.carthage.CarthageDetectable;
import com.synopsys.integration.detectable.detectables.clang.ClangDetectable;
import com.synopsys.integration.detectable.detectables.cocoapods.PodlockDetectable;
import com.synopsys.integration.detectable.detectables.conan.cli.ConanCliDetectable;
import com.synopsys.integration.detectable.detectables.conan.lockfile.ConanLockfileDetectable;
import com.synopsys.integration.detectable.detectables.conda.CondaCliDetectable;
import com.synopsys.integration.detectable.detectables.cpan.CpanCliDetectable;
import com.synopsys.integration.detectable.detectables.cran.PackratLockDetectable;
import com.synopsys.integration.detectable.detectables.dart.pubdep.DartPubDepDetectable;
import com.synopsys.integration.detectable.detectables.dart.pubspec.DartPubSpecLockDetectable;
import com.synopsys.integration.detectable.detectables.git.GitCliDetectable;
import com.synopsys.integration.detectable.detectables.git.GitParseDetectable;
import com.synopsys.integration.detectable.detectables.go.godep.GoDepLockDetectable;
import com.synopsys.integration.detectable.detectables.go.gogradle.GoGradleDetectable;
import com.synopsys.integration.detectable.detectables.go.gomod.GoModCliDetectable;
import com.synopsys.integration.detectable.detectables.go.vendor.GoVendorDetectable;
import com.synopsys.integration.detectable.detectables.go.vendr.GoVndrDetectable;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleDetectable;
import com.synopsys.integration.detectable.detectables.gradle.parsing.GradleProjectInspectorDetectable;
import com.synopsys.integration.detectable.detectables.ivy.IvyParseDetectable;
import com.synopsys.integration.detectable.detectables.lerna.LernaDetectable;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenPomDetectable;
import com.synopsys.integration.detectable.detectables.maven.cli.MavenPomWrapperDetectable;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenProjectInspectorDetectable;
import com.synopsys.integration.detectable.detectables.npm.cli.NpmCliDetectable;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmPackageLockDetectable;
import com.synopsys.integration.detectable.detectables.npm.lockfile.NpmShrinkwrapDetectable;
import com.synopsys.integration.detectable.detectables.npm.packagejson.NpmPackageJsonParseDetectable;
import com.synopsys.integration.detectable.detectables.nuget.NugetProjectDetectable;
import com.synopsys.integration.detectable.detectables.nuget.NugetProjectInspectorDetectable;
import com.synopsys.integration.detectable.detectables.nuget.NugetSolutionDetectable;
import com.synopsys.integration.detectable.detectables.packagist.ComposerLockDetectable;
import com.synopsys.integration.detectable.detectables.pear.PearCliDetectable;
import com.synopsys.integration.detectable.detectables.pip.inspector.PipInspectorDetectable;
import com.synopsys.integration.detectable.detectables.pipenv.build.PipenvDetectable;
import com.synopsys.integration.detectable.detectables.pipenv.parse.PipfileLockDetectable;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.PnpmLockDetectable;
import com.synopsys.integration.detectable.detectables.poetry.PoetryDetectable;
import com.synopsys.integration.detectable.detectables.rebar.RebarDetectable;
import com.synopsys.integration.detectable.detectables.rubygems.gemlock.GemlockDetectable;
import com.synopsys.integration.detectable.detectables.rubygems.gemspec.GemspecParseDetectable;
import com.synopsys.integration.detectable.detectables.sbt.SbtDetectable;
import com.synopsys.integration.detectable.detectables.swift.cli.SwiftCliDetectable;
import com.synopsys.integration.detectable.detectables.swift.lock.SwiftPackageResolvedDetectable;
import com.synopsys.integration.detectable.detectables.xcode.XcodeProjectDetectable;
import com.synopsys.integration.detectable.detectables.xcode.XcodeWorkspaceDetectable;
import com.synopsys.integration.detectable.detectables.yarn.YarnLockDetectable;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.detector.rule.DetectorRule;
import com.synopsys.integration.detector.rule.DetectorRuleSet;
import com.synopsys.integration.detector.rule.DetectorRuleSetBuilder;

public class DetectorRuleFactory {
    public DetectorRuleSet createRules(DetectDetectableFactory detectableFactory, boolean buildless) {
        if (buildless) {
            return createBuildlessRules(detectableFactory);
        } else {
            return createRules(detectableFactory);
        }
    }

    //TODO: It would just be nice not to have to call 'build' after each of the addDetectors.
    private DetectorRuleSet createRules(DetectDetectableFactory detectableFactory) {
        DetectorRuleSetBuilder ruleSet = new DetectorRuleSetBuilder();

        //TODO: Verify we still need to pass detector name here. We may now be able to get it from the detectable class - before we could not as it was not instantiated.
        ruleSet.addDetector(DetectorType.CARGO, "Cargo", CargoDetectable.class, detectableFactory::createCargoDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.CARTHAGE, "Carthage", CarthageDetectable.class, detectableFactory::createCarthageDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.BITBAKE, "Bitbake", BitbakeDetectable.class, detectableFactory::createBitbakeDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.COCOAPODS, "Pod Lock", PodlockDetectable.class, detectableFactory::createPodLockDetectable).defaults().build();

        DetectorRule<?> xcodeProject = ruleSet.addDetector(
            DetectorType.XCODE,
            "Xcode Project",
            XcodeProjectDetectable.class,
            detectableFactory::createXcodeProjectDetectable
        ).defaults().notSelfTypeNestable().build();

        DetectorRule<?> xcodeWorkspace = ruleSet.addDetector(
            DetectorType.XCODE,
            "Xcode Workspace",
            XcodeWorkspaceDetectable.class,
            detectableFactory::createXcodeWorkspaceDetectable
        ).defaults().notSelfTypeNestable().build();

        DetectorRule<?> swiftCli = ruleSet.addDetector(
            DetectorType.SWIFT,
            "Swift CLI",
            SwiftCliDetectable.class,
            detectableFactory::createSwiftCliDetectable
        ).defaults().nestableExceptTo(DetectorType.XCODE).build();

        DetectorRule<?> swiftPackageResolved = ruleSet.addDetector(
            DetectorType.SWIFT,
            "Swift Package Resolved",
            SwiftPackageResolvedDetectable.class,
            detectableFactory::createSwiftPackageResolvedDetectable
        ).defaults().nestableExceptTo(DetectorType.XCODE).build();

        ruleSet.yield(swiftPackageResolved).to(swiftCli);
        ruleSet.yield(xcodeProject).to(xcodeWorkspace);

        DetectorRule<?> conanCliRule = ruleSet.addDetector(DetectorType.CONAN, "Conan CLI", ConanCliDetectable.class, detectableFactory::createConanCliDetectable).defaults()
            .build();
        DetectorRule<?> conanLockfileRule = ruleSet.addDetector(
            DetectorType.CONAN,
            "Conan Lockfile",
            ConanLockfileDetectable.class,
            detectableFactory::createConanLockfileDetectable
        ).defaults().build();
        ruleSet.yield(conanCliRule).to(conanLockfileRule);

        ruleSet.addDetector(DetectorType.CONDA, "Conda Cli", CondaCliDetectable.class, detectableFactory::createCondaCliDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.CPAN, "Cpan Cli", CpanCliDetectable.class, detectableFactory::createCpanCliDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.CRAN, "Packrat Lock", PackratLockDetectable.class, detectableFactory::createPackratLockDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.DART, "Dart Pub Deps", DartPubDepDetectable.class, detectableFactory::createDartPubDepDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.GO_MOD, "Go Mod Cli", GoModCliDetectable.class, detectableFactory::createGoModCliDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_GRADLE, "Go Gradle", GoGradleDetectable.class, detectableFactory::createGoGradleDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_DEP, "Go Lock", GoDepLockDetectable.class, detectableFactory::createGoLockDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_VNDR, "Go Vndr", GoVndrDetectable.class, detectableFactory::createGoVndrDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_VENDOR, "Go Vendor", GoVendorDetectable.class, detectableFactory::createGoVendorDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.GRADLE, "Gradle Inspector", GradleDetectable.class, detectableFactory::createGradleDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.IVY, "Ivy Parse", IvyParseDetectable.class, detectableFactory::createIvyParseDetectable).defaultLock().build();

        ruleSet.addDetector(DetectorType.HEX, "Rebar", RebarDetectable.class, detectableFactory::createRebarDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.MAVEN, "Maven Pom", MavenPomDetectable.class, detectableFactory::createMavenPomDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.MAVEN, "Maven Wrapper", MavenPomWrapperDetectable.class, detectableFactory::createMavenPomWrapperDetectable).defaults().build();

        DetectorRule<?> lernaDetectable = ruleSet.addDetector(DetectorType.LERNA, "Lerna", LernaDetectable.class, detectableFactory::createLernaDetectable).defaults().build();
        DetectorRule<?> yarnLock = ruleSet.addDetector(DetectorType.YARN, "Yarn Lock", YarnLockDetectable.class, detectableFactory::createYarnLockDetectable).defaultLock().build();
        DetectorRule<?> npmPackageLock = ruleSet.addDetector(DetectorType.NPM, "Package Lock", NpmPackageLockDetectable.class, detectableFactory::createNpmPackageLockDetectable)
            .defaultLock().build();
        DetectorRule<?> npmShrinkwrap = ruleSet.addDetector(DetectorType.NPM, "Shrinkwrap", NpmShrinkwrapDetectable.class, detectableFactory::createNpmShrinkwrapDetectable)
            .defaultLock().build();
        DetectorRule<?> npmCli = ruleSet.addDetector(DetectorType.NPM, "Npm Cli", NpmCliDetectable.class, detectableFactory::createNpmCliDetectable).defaults().build();
        DetectorRule<?> pnpmLock = ruleSet.addDetector(DetectorType.PNPM, "Pnpm Lock", PnpmLockDetectable.class, detectableFactory::createPnpmLockDetectable).defaults().build();

        ruleSet.yield(npmPackageLock).to(lernaDetectable);
        ruleSet.yield(npmShrinkwrap).to(lernaDetectable);
        ruleSet.yield(npmCli).to(lernaDetectable);
        ruleSet.yield(yarnLock).to(lernaDetectable);
        ruleSet.yield(pnpmLock).to(lernaDetectable);

        ruleSet.yield(npmPackageLock).to(npmShrinkwrap);
        ruleSet.yield(npmCli).to(npmPackageLock);
        ruleSet.yield(npmCli).to(npmShrinkwrap);

        ruleSet.yield(npmCli).to(yarnLock);
        ruleSet.yield(npmPackageLock).to(yarnLock);
        ruleSet.yield(npmShrinkwrap).to(yarnLock);

        ruleSet.yield(npmCli).to(pnpmLock);

        DetectorRule<?> nugetSolution = ruleSet.addDetector(DetectorType.NUGET, "Solution", NugetSolutionDetectable.class, detectableFactory::createNugetSolutionDetectable)
            .defaults().build();
        //The Project detectable is "notNestable" because it will falsely apply under a solution (the solution includes all of the projects).
        DetectorRule<?> nugetProject = ruleSet.addDetector(DetectorType.NUGET, "Project", NugetProjectDetectable.class, detectableFactory::createNugetProjectDetectable)
            .notNestable().noMaxDepth().build();

        ruleSet.yield(nugetProject).to(nugetSolution);

        ruleSet.addDetector(DetectorType.PACKAGIST, "Composer", ComposerLockDetectable.class, detectableFactory::createComposerDetectable).defaults().build();

        DetectorRule<?> pipEnv = ruleSet.addDetector(DetectorType.PIP, "Pip Env", PipenvDetectable.class, detectableFactory::createPipenvDetectable).defaults().build();
        DetectorRule<?> pipfileLock = ruleSet.addDetector(DetectorType.PIP, "Pipfile Lock", PipfileLockDetectable.class, detectableFactory::createPipfileLockDetectable).defaults()
            .build();
        DetectorRule<?> pipInspector = ruleSet.addDetector(DetectorType.PIP, "Pip Inspector", PipInspectorDetectable.class, detectableFactory::createPipInspectorDetectable)
            .defaults().build();
        DetectorRule<?> poetry = ruleSet.addDetector(DetectorType.POETRY, "Poetry", PoetryDetectable.class, detectableFactory::createPoetryDetectable).defaults().build();

        ruleSet.yield(pipInspector).to(pipEnv);
        ruleSet.yield(poetry).to(pipEnv);
        ruleSet.yield(pipfileLock).to(pipEnv);
        ruleSet.yield(pipfileLock).to(pipInspector);

        ruleSet.addDetector(DetectorType.RUBYGEMS, "Gemlock", GemlockDetectable.class, detectableFactory::createGemlockDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.SBT, "Sbt", SbtDetectable.class, detectableFactory::createSbtDetectable).defaults().build(); //TODO: Yield
        ruleSet.addDetector(DetectorType.PEAR, "Pear", PearCliDetectable.class, detectableFactory::createPearCliDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.CLANG, "Clang", ClangDetectable.class, detectableFactory::createClangDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.GIT, "Git", GitCliDetectable.class, detectableFactory::createGitDetectable).defaults().build();

        return ruleSet.build();
    }

    private DetectorRuleSet createBuildlessRules(DetectDetectableFactory detectableFactory) {
        DetectorRuleSetBuilder ruleSet = new DetectorRuleSetBuilder();

        ruleSet.addDetector(DetectorType.CARGO, "Cargo", CargoDetectable.class, detectableFactory::createCargoDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.CARTHAGE, "Carthage", CarthageDetectable.class, detectableFactory::createCarthageDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.COCOAPODS, "Pod Lock", PodlockDetectable.class, detectableFactory::createPodLockDetectable).defaults().build();

        ruleSet.addDetector(
            DetectorType.SWIFT,
            "Swift Package Resolved",
            SwiftPackageResolvedDetectable.class,
            detectableFactory::createSwiftPackageResolvedDetectable
        ).defaults().notNestableBeneath(DetectorType.XCODE).build();

        ruleSet.addDetector(DetectorType.PACKAGIST, "Packrat Lock", PackratLockDetectable.class, detectableFactory::createPackratLockDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.DART, "Dart Pub Spec", DartPubSpecLockDetectable.class, detectableFactory::createDartPubSpecLockDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.GO_DEP, "Go Lock", GoDepLockDetectable.class, detectableFactory::createGoLockDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_VNDR, "Go Vndr", GoVndrDetectable.class, detectableFactory::createGoVndrDetectable).defaults().build();
        ruleSet.addDetector(DetectorType.GO_VENDOR, "Go Vendor", GoVendorDetectable.class, detectableFactory::createGoVendorDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.GRADLE, "Gradle Project Inspector", GradleProjectInspectorDetectable.class, detectableFactory::createGradleProjectInspectorDetectable)
            .defaults().build();
        ruleSet.addDetector(DetectorType.GO_GRADLE, "Go Gradle", GoGradleDetectable.class, detectableFactory::createGoGradleDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.IVY, "Ivy Parse", IvyParseDetectable.class, detectableFactory::createIvyParseDetectable).defaultLock().build();

        ruleSet.addDetector(DetectorType.MAVEN, "Maven Project Inspector", MavenProjectInspectorDetectable.class, detectableFactory::createMavenProjectInspectorDetectable)
            .defaults().build();

        DetectorRule<?> pipfileLock = ruleSet.addDetector(DetectorType.PIP, "Pipfile Lock", PipfileLockDetectable.class, detectableFactory::createPipfileLockDetectable).defaults()
            .build();
        DetectorRule<?> poetry = ruleSet.addDetector(DetectorType.POETRY, "Poetry", PoetryDetectable.class, detectableFactory::createPoetryDetectable).defaults().build();

        ruleSet.yield(pipfileLock).to(poetry);

        DetectorRule<?> yarnLock = ruleSet.addDetector(DetectorType.YARN, "Yarn Lock", YarnLockDetectable.class, detectableFactory::createYarnLockDetectable).defaults().build();
        DetectorRule<?> npmPackageLock = ruleSet.addDetector(DetectorType.NPM, "Package Lock", NpmPackageLockDetectable.class, detectableFactory::createNpmPackageLockDetectable)
            .defaults().build();
        DetectorRule<?> npmShrinkwrap = ruleSet.addDetector(DetectorType.NPM, "Shrinkwrap", NpmShrinkwrapDetectable.class, detectableFactory::createNpmShrinkwrapDetectable)
            .defaults().build();
        DetectorRule<?> npmPackageJsonParse = ruleSet.addDetector(
            DetectorType.NPM,
            "Package Json Parse",
            NpmPackageJsonParseDetectable.class,
            detectableFactory::createNpmPackageJsonParseDetectable
        ).defaults().build();
        DetectorRule<?> pnpmLock = ruleSet.addDetector(DetectorType.PNPM, "Pnpm Lock", PnpmLockDetectable.class, detectableFactory::createPnpmLockDetectable).defaults().build();

        ruleSet.yield(npmPackageLock).to(npmShrinkwrap);
        ruleSet.yield(npmPackageJsonParse).to(npmPackageLock);
        ruleSet.yield(npmPackageJsonParse).to(npmShrinkwrap);

        ruleSet.yield(npmPackageJsonParse).to(yarnLock);
        ruleSet.yield(npmPackageLock).to(yarnLock);
        ruleSet.yield(npmShrinkwrap).to(yarnLock);

        ruleSet.yield(npmPackageJsonParse).to(pnpmLock);

        ruleSet.addDetector(DetectorType.NUGET, "NuGet Project Inspector", NugetProjectInspectorDetectable.class, detectableFactory::createNugetParseDetectable).defaults().build();

        ruleSet.addDetector(DetectorType.PACKAGIST, "Composer", ComposerLockDetectable.class, detectableFactory::createComposerDetectable).defaults().build();

        DetectorRule<?> gemlock = ruleSet.addDetector(DetectorType.RUBYGEMS, "Gemlock", GemlockDetectable.class, detectableFactory::createGemlockDetectable).defaults().build();
        DetectorRule<?> gemspec = ruleSet.addDetector(DetectorType.RUBYGEMS, "Gemspec", GemspecParseDetectable.class, detectableFactory::createGemspecParseDetectable).defaults()
            .build();

        ruleSet.yield(gemspec).to(gemlock);

        ruleSet.addDetector(DetectorType.GIT, "Git Parse", GitParseDetectable.class, detectableFactory::createGitParseDetectable).defaults().build();

        return ruleSet.build();
    }
}