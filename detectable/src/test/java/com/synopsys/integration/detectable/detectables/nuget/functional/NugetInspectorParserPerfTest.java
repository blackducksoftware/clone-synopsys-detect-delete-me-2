package com.synopsys.integration.detectable.detectables.nuget.functional;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.bdio.bdio1.BdioNodeFactory;
import com.synopsys.integration.bdio.bdio1.BdioPropertyHelper;
import com.synopsys.integration.bdio.bdio1.model.BdioComponent;
import com.synopsys.integration.bdio.bdio1.model.BdioNode;
import com.synopsys.integration.bdio.bdio1.model.BdioProject;
import com.synopsys.integration.bdio.graph.DependencyGraphTransformer;
import com.synopsys.integration.bdio.model.BdioId;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.nuget.parse.NugetInspectorParser;
import com.synopsys.integration.detectable.detectables.nuget.parse.NugetParseResult;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;

public class NugetInspectorParserPerfTest {
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public ExternalIdFactory externalIdFactory = new ExternalIdFactory();

    @Test(timeout = 120000L)
    public void performanceTestNuget() throws IOException {
        final String dependencyGraphFile = FunctionalTestFiles.asString("/nuget/dwCheckApi_inspection.json");

        final NugetInspectorParser packager = new NugetInspectorParser(gson, externalIdFactory);

        final NugetParseResult result = packager.createCodeLocation(dependencyGraphFile);
        final CodeLocation codeLocation = result.getCodeLocations().get(0);

        final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
        final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);
        final DependencyGraphTransformer dependencyGraphTransformer = new DependencyGraphTransformer(bdioPropertyHelper, bdioNodeFactory);

        final BdioProject bdioNode = bdioNodeFactory.createProject("test", "1.0.0", BdioId.createFromPieces("bdioId"), externalIdFactory.createMavenExternalId("group", "name", "version"));

        final List<BdioComponent> components = dependencyGraphTransformer.transformDependencyGraph(codeLocation.getDependencyGraph(), bdioNode, codeLocation.getDependencyGraph().getRootDependencies(), new HashMap<ExternalId, BdioNode>());

        assertEquals(211, components.size());
    }
}
