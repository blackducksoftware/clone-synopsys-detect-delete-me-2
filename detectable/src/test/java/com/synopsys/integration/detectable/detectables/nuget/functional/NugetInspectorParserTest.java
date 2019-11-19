package com.synopsys.integration.detectable.detectables.nuget.functional;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.bdio.bdio1.BdioNodeFactory;
import com.synopsys.integration.bdio.bdio1.BdioPropertyHelper;
import com.synopsys.integration.bdio.bdio1.model.BdioComponent;
import com.synopsys.integration.bdio.bdio1.model.BdioExternalIdentifier;
import com.synopsys.integration.bdio.bdio1.model.BdioNode;
import com.synopsys.integration.bdio.bdio1.model.BdioProject;
import com.synopsys.integration.bdio.graph.DependencyGraphTransformer;
import com.synopsys.integration.bdio.model.BdioId;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.nuget.parse.NugetInspectorParser;
import com.synopsys.integration.detectable.detectables.nuget.parse.NugetParseResult;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;
import com.synopsys.integration.detectable.util.GraphCompare;

public class NugetInspectorParserTest {
    public Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Test
    public void createCodeLocationLDServiceDashboard() throws IOException {
        final String dependencyNodeFile = FunctionalTestFiles.asString("/nuget/LDService.Dashboard_inspection.json");
        final ArrayList<String> expectedOutputFiles = new ArrayList<>();
        expectedOutputFiles.add("/nuget/LDService.Dashboard_Output_0_graph.json");
        createCodeLocation(dependencyNodeFile, expectedOutputFiles);
    }

    @Test
    public void createCodeLocationLDService() throws IOException {
        final String dependencyNodeFile = FunctionalTestFiles.asString("/nuget/LDService_inspection.json");
        final ArrayList<String> expectedOutputFiles = new ArrayList<>();
        expectedOutputFiles.add("/nuget/LDService_Output_0_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_1_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_2_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_3_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_4_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_5_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_6_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_7_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_8_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_9_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_10_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_11_graph.json");
        expectedOutputFiles.add("/nuget/LDService_Output_12_graph.json");
        createCodeLocation(dependencyNodeFile, expectedOutputFiles);
    }

    @Test(timeout = 5000L)
    public void createCodeLocationDWService() throws IOException {
        final String dependencyNodeFile = FunctionalTestFiles.asString("/nuget/dwCheckApi_inspection_martin.json");
        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();

        final NugetInspectorParser packager = new NugetInspectorParser(gson, externalIdFactory);
        final NugetParseResult result = packager.createCodeLocation(dependencyNodeFile);

        for (final CodeLocation codeLocation : result.getCodeLocations()) {
            final BdioPropertyHelper bdioPropertyHelper = new BdioPropertyHelper();
            final BdioNodeFactory bdioNodeFactory = new BdioNodeFactory(bdioPropertyHelper);

            final DependencyGraphTransformer dependencyNodeTransformer = new DependencyGraphTransformer(bdioPropertyHelper, bdioNodeFactory);

            final BdioExternalIdentifier projectId = bdioPropertyHelper.createExternalIdentifier(codeLocation.getExternalId().get());
            final BdioProject project = bdioNodeFactory.createProject(result.getProjectName(), result.getProjectVersion(), BdioId.createFromPieces(Forge.NUGET.toString()), projectId);

            final Map<ExternalId, BdioNode> components = new HashMap<>();
            components.put(codeLocation.getExternalId().get(), project);

            final List<BdioComponent> bdioComponents = dependencyNodeTransformer.transformDependencyGraph(codeLocation.getDependencyGraph(), project, codeLocation.getDependencyGraph().getRootDependencies(), components);

            assertEquals(bdioComponents.size(), bdioComponents.size());
        }
    }

    private void createCodeLocation(final String dependencyNodeFile, final List<String> expectedOutputFiles) throws IOException {
        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        final NugetInspectorParser packager = new NugetInspectorParser(gson, externalIdFactory);

        final NugetParseResult result = packager.createCodeLocation(dependencyNodeFile);

        for (int i = 0; i < expectedOutputFiles.size(); i++) {
            final CodeLocation codeLocation = result.getCodeLocations().get(i);
            final String expectedOutputFile = expectedOutputFiles.get(i);

            GraphCompare.assertEqualsResource(expectedOutputFile, codeLocation.getDependencyGraph());
        }
    }
}
