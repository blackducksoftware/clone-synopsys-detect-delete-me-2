package com.synopsys.integration.detectable.detectables.pnpm.lockfile.process;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.enums.DependencyType;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmLockYaml;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmPackageInfo;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmProjectPackage;
import com.synopsys.integration.detectable.util.DependencyTypeFilter;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class PnpmYamlTransformer {
    private static final String LINKED_PACKAGE_PREFIX = "link:";

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExternalIdFactory externalIdFactory;
    private final DependencyTypeFilter dependencyTypeFilter;

    public PnpmYamlTransformer(ExternalIdFactory externalIdFactory, DependencyTypeFilter dependencyTypeFilter) {
        this.externalIdFactory = externalIdFactory;
        this.dependencyTypeFilter = dependencyTypeFilter;
    }

    public CodeLocation generateCodeLocation(File sourcePath, PnpmLockYaml pnpmLockYaml, @Nullable NameVersion projectNameVersion, PnpmLinkedPackageResolver linkedPackageResolver) throws IntegrationException {
        return generateCodeLocation(sourcePath, convertPnpmLockYamlToPnpmProjectPackage(pnpmLockYaml), null, projectNameVersion, pnpmLockYaml.packages, linkedPackageResolver);
    }

    public CodeLocation generateCodeLocation(
        File sourcePath,
        PnpmProjectPackage projectPackage,
        @Nullable String reportingProjectPackagePath,
        @Nullable NameVersion projectNameVersion,
        @Nullable Map<String, PnpmPackageInfo> packageMap,
        PnpmLinkedPackageResolver linkedPackageResolver
    ) throws IntegrationException {
        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        List<String> rootPackageIds = extractRootPackageIds(projectPackage, reportingProjectPackagePath, linkedPackageResolver);
        buildGraph(dependencyGraph, rootPackageIds, packageMap, linkedPackageResolver, reportingProjectPackagePath);

        if (projectNameVersion != null) {
            return new CodeLocation(dependencyGraph, externalIdFactory.createNameVersionExternalId(Forge.NPMJS, projectNameVersion.getName(), projectNameVersion.getVersion()), sourcePath);
        }
        return new CodeLocation(dependencyGraph, sourcePath);
    }

    private void buildGraph(
        MutableDependencyGraph graphBuilder,
        List<String> rootPackageIds,
        @Nullable Map<String, PnpmPackageInfo> packageMap,
        PnpmLinkedPackageResolver linkedPackageResolver,
        @Nullable String reportingProjectPackagePath
    ) throws IntegrationException {
        if (packageMap == null) {
            throw new DetectableException("Could not parse 'packages' section of the pnpm-lock.yaml file.");
        }
        for (Map.Entry<String, PnpmPackageInfo> packageEntry : packageMap.entrySet()) {
            String packageId = packageEntry.getKey();
            Optional<Dependency> pnpmPackage = buildDependencyFromPackageEntry(packageEntry);
            if (!pnpmPackage.isPresent()) {
                logger.debug(String.format("Could not add package %s to the graph.", packageId));
                continue;
            }

            if (isRootPackage(packageId, rootPackageIds)) {
                graphBuilder.addChildToRoot(pnpmPackage.get());
            }

            PnpmPackageInfo packageInfo = packageEntry.getValue();
            if (dependencyTypeFilter.shouldReportDependencyType(packageInfo.getDependencyType())) {
                for (Map.Entry<String, String> packageDependency : packageInfo.getDependencies().entrySet()) {
                    String dependencyPackageId = convertRawEntryToPackageId(packageDependency, linkedPackageResolver, reportingProjectPackagePath);
                    Optional<Dependency> child = buildDependencyFromPackageId(dependencyPackageId);
                    child.ifPresent(c -> graphBuilder.addChildWithParent(child.get(), pnpmPackage.get()));
                }
            }
        }
    }

    private PnpmProjectPackage convertPnpmLockYamlToPnpmProjectPackage(PnpmLockYaml pnpmLockYaml) {
        PnpmProjectPackage pnpmProjectPackage = new PnpmProjectPackage();

        pnpmProjectPackage.dependencies = pnpmLockYaml.dependencies;
        pnpmProjectPackage.devDependencies = pnpmLockYaml.devDependencies;
        pnpmProjectPackage.optionalDependencies = pnpmLockYaml.optionalDependencies;

        return pnpmProjectPackage;
    }

    private List<String> extractRootPackageIds(PnpmProjectPackage pnpmProjectPackage, @Nullable String reportingProjectPackagePath, PnpmLinkedPackageResolver linkedPackageResolver) {
        Map<String, String> rawPackageInfo = new HashMap<>();
        dependencyTypeFilter.ifReportingType(DependencyType.APP, pnpmProjectPackage.dependencies, rawPackageInfo::putAll);
        dependencyTypeFilter.ifReportingType(DependencyType.DEV, pnpmProjectPackage.devDependencies, rawPackageInfo::putAll);
        dependencyTypeFilter.ifReportingType(DependencyType.OPTIONAL, pnpmProjectPackage.optionalDependencies, rawPackageInfo::putAll);

        return rawPackageInfo.entrySet().stream()
            .map(entry -> convertRawEntryToPackageId(entry, linkedPackageResolver, reportingProjectPackagePath))
            .collect(Collectors.toList());
    }

    private String convertRawEntryToPackageId(Map.Entry<String, String> entry, PnpmLinkedPackageResolver linkedPackageResolver, @Nullable String reportingProjectPackagePath) {
        String name = StringUtils.strip(entry.getKey(), "'");
        String version = entry.getValue();
        if (version.startsWith(LINKED_PACKAGE_PREFIX)) {
            // a linked project package's version will be referenced in the format: <linkPrefix><pathToLinkedPackageRelativeToReportingProjectPackage>
            version = linkedPackageResolver.resolveVersionOfLinkedPackage(reportingProjectPackagePath, version.replace(LINKED_PACKAGE_PREFIX, ""));
        }
        return String.format("/%s/%s", name, version);
    }

    private Optional<NameVersion> parseNameVersionFromId(String id) {
        // ids follow format: /name/version, where name often contains slashes
        try {
            int indexOfLastSlash = id.lastIndexOf("/");
            String name = id.substring(1, indexOfLastSlash);
            String version = id.substring(indexOfLastSlash + 1);
            return Optional.of(new NameVersion(name, version));
        } catch (Exception e) {
            logger.debug(String.format("There was an issue parsing package id: %s.  This is likely an unsupported format.", id));
            return Optional.empty();
        }
    }

    private Optional<Dependency> buildDependencyFromPackageEntry(Map.Entry<String, PnpmPackageInfo> packageEntry) {
        PnpmPackageInfo packageInfo = packageEntry.getValue();
        if (packageInfo.name != null) {
            return Optional.of(new Dependency(externalIdFactory.createNameVersionExternalId(Forge.NPMJS, packageInfo.name, packageInfo.version)));
        }
        return buildDependencyFromPackageId(packageEntry.getKey());
    }

    private Optional<Dependency> buildDependencyFromPackageId(String packageId) {
        return parseNameVersionFromId(packageId)
            .map(nameVersion -> externalIdFactory.createNameVersionExternalId(Forge.NPMJS, nameVersion.getName(), nameVersion.getVersion()))
            .map(Dependency::new);
    }

    private boolean isRootPackage(String id, List<String> rootIds) {
        return rootIds.contains(id) ||
            rootIds.stream()
                .map(this::parseNameVersionFromId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(NameVersion::getVersion)
                .anyMatch(id::equals); // for file dependencies, they are declared as <name> : <fileIdAsReportedInPackagesSection>
    }

}
