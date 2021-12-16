package com.synopsys.integration.detectable.detectables.pnpm.functional;

import java.io.File;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.enums.DependencyType;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.PnpmLockExtractor;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.process.PnpmLinkedPackageResolver;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.process.PnpmLockYamlParser;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.process.PnpmYamlTransformer;
import com.synopsys.integration.detectable.detectables.yarn.packagejson.PackageJsonFiles;
import com.synopsys.integration.detectable.detectables.yarn.packagejson.PackageJsonReader;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.DependencyTypeFilter;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;

public class PnpmLockExtractorTest {
    @Test
    public void testNoFailureOnNullPackageJson() {
        PackageJsonFiles packageJsonFiles = new PackageJsonFiles(new PackageJsonReader(new Gson()));
        DependencyTypeFilter dependencyTypeFilter = new DependencyTypeFilter(Arrays.asList(DependencyType.OPTIONAL, DependencyType.DEV));
        PnpmLockExtractor extractor = new PnpmLockExtractor(new PnpmLockYamlParser(new PnpmYamlTransformer(new ExternalIdFactory(), dependencyTypeFilter)), packageJsonFiles);

        File pnpmLockYaml = FunctionalTestFiles.asFile("/pnpm/pnpm-lock.yaml");
        Extraction extraction = extractor.extract(pnpmLockYaml, null, new PnpmLinkedPackageResolver(new File(""), packageJsonFiles));
        Assertions.assertTrue(extraction.isSuccess());
    }
}
