package com.synopsys.integration.detectable.detectables.pnpm.functional;

import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.DisabledOnOs;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.util.DependencyTypeFilter;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.PnpmLockOptions;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.model.PnpmDependencyType;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.functional.DetectableFunctionalTest;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

@DisabledOnOs(WINDOWS) //TODO: Can't cleanup the temp file on windows. May need to close resources?
public class PnpmLockDetectableTest extends DetectableFunctionalTest {
    public PnpmLockDetectableTest() throws IOException {
        super("Pnpm");
    }

    @Override
    protected void setup() throws IOException {
        addFile(Paths.get("package.json"),
            "{",
            "name: \"project\",",
            "version: \"version\"",
            "}"
        );

        addFile(Paths.get("pnpm-lock.yaml"),
            "lockfileVersion: 1.0.0",
            "",
            "dependencies:",
            "  material-design-icons: 3.0.1",
            "",
            "devDependencies:",
            "  autoprefixer: 9.8.6",
            "",
            "packages:",
            "",
            "  /material-design-icons/3.0.1:",
            "    resolution: {integrity: sha512-eGmwYQn3gxo4r7jdQnkrrN6bY478C3P+a/y72IJukF8LjB6ZHeB3c+Ehacj3sYeSmUXGlnA67/PmbM9CVwL7Dw==}",
            "    engines: {node: '>= 8'}",
            "    dependencies:",
            "      '@nodelib/fs.stat': 2.0.3",
            "    dev: false",
            "",
            "  /@nodelib/fs.stat/2.0.3:",
            "    resolution: {integrity: sha512-bQBFruR2TAwoevBEd/NWMoAAtNGzTRgdrqnYCc7dhzfoNvqPzLyqlEQnzZ3kVnNrSp25iyxE00/3h2fqGAGArA==}",
            "    engines: {node: '>= 8'}",
            "    dev: true",
            "",
            "  /autoprefixer/9.8.6:",
            "    resolution: {integrity: sha512-XrvP4VVHdRBCdX1S3WXVD8+RyG9qeb1D5Sn1DeLiG2xfSpzellk5k54xbUERJ3M5DggQxes39UGOTP8CFrEGbg==}",
            "    hasBin: true",
            "    dev: true"
        );
    }

    @Override
    public @NotNull Detectable create(@NotNull DetectableEnvironment detectableEnvironment) {
        PnpmLockOptions pnpmLockOptions = new PnpmLockOptions(new DependencyTypeFilter<>(Arrays.asList(PnpmDependencyType.APP, PnpmDependencyType.DEV)));
        return detectableFactory.createPnpmLockDetectable(detectableEnvironment, pnpmLockOptions);
    }

    @Override
    public void assertExtraction(@NotNull Extraction extraction) {
        Assertions.assertEquals(1, extraction.getCodeLocations().size());

        Assertions.assertEquals("project", extraction.getProjectName());
        Assertions.assertEquals("version", extraction.getProjectVersion());

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.NPMJS, extraction.getCodeLocations().get(0).getDependencyGraph());
        graphAssert.hasRootSize(2);
        graphAssert.hasRootDependency("material-design-icons", "3.0.1");
        graphAssert.hasRootDependency("autoprefixer", "9.8.6");
        graphAssert.hasParentChildRelationship("material-design-icons", "3.0.1", "@nodelib/fs.stat", "2.0.3");
    }
}
