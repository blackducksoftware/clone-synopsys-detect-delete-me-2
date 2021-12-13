package com.synopsys.integration.detectable.detectables.lerna;

import java.io.File;
import java.util.List;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectables.lerna.model.LernaPackage;
import com.synopsys.integration.detectable.detectables.lerna.model.LernaResult;
import com.synopsys.integration.detectable.extraction.Extraction;

public class LernaExtractor {
    private final LernaPackageDiscoverer lernaPackageDiscoverer;
    private final LernaPackager lernaPackager;
    private final LernaOptions lernaOptions;

    public LernaExtractor(LernaPackageDiscoverer lernaPackageDiscoverer, LernaPackager lernaPackager, LernaOptions lernaOptions) {
        this.lernaPackageDiscoverer = lernaPackageDiscoverer;
        this.lernaPackager = lernaPackager;
        this.lernaOptions = lernaOptions;
    }

    public Extraction extract(File sourceDirectory, File packageJson, ExecutableTarget lernaExecutable) {
        try {
            List<LernaPackage> lernaPackages = lernaPackageDiscoverer.discoverLernaPackages(sourceDirectory, lernaExecutable, lernaOptions.getExcludedPackages(), lernaOptions.getIncludedPackages());
            LernaResult lernaResult = lernaPackager.generateLernaResult(sourceDirectory, packageJson, lernaPackages);

            if (lernaResult.getException().isPresent()) {
                throw lernaResult.getException().get();
            }

            return new Extraction.Builder()
                .projectName(lernaResult.getProjectName())
                .projectVersion(lernaResult.getProjectVersionName())
                .success(lernaResult.getCodeLocations())
                .build();
        } catch (Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }
}
