package com.synopsys.integration.detectable.detectables.pnpm.functional;

import java.io.IOException;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectables.pnpm.lockfile.PnpmLockDetectableOptions;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.functional.DetectableFunctionalTest;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

public class PnpmLockDetectableTest extends DetectableFunctionalTest {
    public PnpmLockDetectableTest() throws IOException {
        super("Pnpm");
    }

    @Override
    protected void setup() throws IOException {
        addFile("package.json");

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
        return detectableFactory.createPnpmLockDetectable(detectableEnvironment, new PnpmLockDetectableOptions(true, true));
    }

    @Override
    public void assertExtraction(@NotNull Extraction extraction) {
        Assertions.assertEquals(1, extraction.getCodeLocations().size());

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.NPMJS, extraction.getCodeLocations().get(0).getDependencyGraph());
        graphAssert.hasRootSize(2);
        graphAssert.hasRootDependency("material-design-icons", "3.0.1");
        graphAssert.hasRootDependency("autoprefixer", "9.8.6");
        graphAssert.hasParentChildRelationship("material-design-icons", "3.0.1", "@nodelib/fs.stat", "2.0.3");
    }
}
