package com.synopsys.integration.detectable.detectables.ivy;

import java.io.IOException;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.functional.DetectableFunctionalTest;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

public class IvyParseDetectableTest extends DetectableFunctionalTest {
    public IvyParseDetectableTest() throws IOException {
        super("ivyparse");
    }

    @Override
    protected void setup() throws IOException {
        addFile(Paths.get("ivy.xml"),
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<ivy-module xmlns:e=\"http://ant.apache.org/ivy/extra\">",
            "    <info organisation=\"pmdungeon\" module=\"pmdungeon\" />",
            "    <dependencies>",
            "        <dependency org=\"com.google.code.gson\" name=\"gson\" rev=\"1.0\" />",
            "        <dependency org=\"com.badlogicgames.gdx\" name=\"gdx-ui\" rev=\"2.0\" />",
            "        <dependency org=\"com.badlogicgames.gdx\" name=\"gdx-backend\" rev=\"3.0\" />",
            "    </dependencies>",
            "</ivy-module>"
        );
    }

    @NotNull
    @Override
    public Detectable create(@NotNull DetectableEnvironment detectableEnvironment) {
        return detectableFactory.createIvyParseDetectable(detectableEnvironment);
    }

    @Override
    public void assertExtraction(@NotNull Extraction extraction) {
        Assertions.assertEquals(1, extraction.getCodeLocations().size(), "A code location should have been generated.");

        NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.MAVEN, extraction.getCodeLocations().get(0).getDependencyGraph());
        graphAssert.hasRootSize(3);

        ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        ExternalId gson = externalIdFactory.createMavenExternalId("com.google.code.gson", "gson", "1.0");
        ExternalId gdxUi = externalIdFactory.createMavenExternalId("com.badlogicgames.gdx", "gdx-ui", "2.0");
        ExternalId gdxBackend = externalIdFactory.createMavenExternalId("com.badlogicgames.gdx", "gdx-backend", "3.0");

        graphAssert.hasRootDependency(gson);
        graphAssert.hasRootDependency(gdxUi);
        graphAssert.hasRootDependency(gdxBackend);
    }
}
