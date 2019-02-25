/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.detectable.detectables.gradle.functional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectables.gradle.inspection.model.GradleReport;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportParser;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportTransformer;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;
import com.synopsys.integration.detectable.util.graph.MavenGraphAssert;

//import org.json.JSONException;
//import org.skyscreamer.jsonassert.JSONAssert;

public class GradleReportParserFunctionalTest {
    //private final TestUtil testUtil = new TestUtil();

    //TODO: Put back @Test
    public void extractCodeLocationTest() throws IOException {
        createNewCodeLocationTest("/gradle/dependencyGraph.txt", "/gradle/dependencyGraph-expected.json", "/gradle/rootProjectMetadata.txt", "hub-detect", "2.0.0-SNAPSHOT");
    }

    //TODO: Put back @Test
    public void complexTest() throws IOException {
        final CodeLocation codeLocation = build("/gradle/parse-tests/complex_dependencyGraph.txt");
        final DependencyGraph graph = codeLocation.getDependencyGraph();

        final MavenGraphAssert graphAssert = new MavenGraphAssert(graph);
        graphAssert.hasDependency("non-project:with-nested:1.0.0");
        graphAssert.hasDependency("solo:component:4.12");
        graphAssert.hasDependency("some.group:child:2.2.2");
        graphAssert.hasDependency("terminal:child:6.2.3");

        graphAssert.noDependency("child-project");
        graphAssert.noDependency("nested-parent");
        graphAssert.noDependency("spring-webflux");
        graphAssert.noDependency("spring-beans");
        graphAssert.noDependency("spring-core");
        graphAssert.noDependency("spring-web");
        graphAssert.noDependency("should-suppress");

        graphAssert.hasRootDependency("solo:component:4.12");
        graphAssert.hasRootDependency("non-project:with-nested:1.0.0");
        graphAssert.hasRootDependency("some.group:parent:5.0.0");
        graphAssert.hasRootDependency("terminal:child:6.2.3");

        final ExternalId parent = graphAssert.hasDependency("some.group:parent:5.0.0");
        final ExternalId child = graphAssert.hasDependency("some.group:child:2.2.2");
        graphAssert.hasParentChildRelationship(parent, child);
    }

    private CodeLocation build(final String resource) throws IOException {
        final File file = new File(resource);
        final GradleReportParser gradleReportParser = new GradleReportParser(new ExternalIdFactory());
        final Optional<CodeLocation> result = null;// = //;gradleReportParser.parseDependencies(file);
        if (result.isPresent()) { // TODO: What?
            return result.get();
        } else {
            return null;
        }
    }

    //TODO: Put back @Test
    public void testSpringFrameworkAop() throws IOException {
        final File file = new File("/gradle/spring-framework/spring_aop_dependencyGraph.txt");
        final GradleReportParser gradleReportParser = new GradleReportParser(new ExternalIdFactory());
        final Optional<CodeLocation> result = null;//= gradleReportParser.parseDependencies(file);
        assertTrue(result.isPresent());
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result.get()));
    }

    //TODO: Put back @Test
    public void testImplementationsGraph() throws IOException {
        final File file = new File("/gradle/gradle_implementations_dependencyGraph.txt");
        final GradleReportParser gradleReportParser = new GradleReportParser(new ExternalIdFactory());
        final Optional<CodeLocation> result = null;//= gradleReportParser.parseDependencies(file);
        assertTrue(result.isPresent());
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result.get()));
    }

    private void createNewCodeLocationTest(final String gradleInspectorOutputFilePath, final String expectedResourcePath, final String rootProjectFilePath, final String rootProjectName, final String rootProjectVersionName)
        throws IOException {

        final GradleReportParser gradleReportParser = new GradleReportParser(new ExternalIdFactory());
        final GradleReport result = gradleReportParser.parseReport(FunctionalTestFiles.asFile(gradleInspectorOutputFilePath));

        final GradleReportTransformer transformer = new GradleReportTransformer(new ExternalIdFactory());
        final CodeLocation codeLocation = transformer.trasnform(result);
        assertNotNull(codeLocation);
        //        assertTrue(rootProjectNameVersion.isPresent());
        //        assertEquals(rootProjectName, rootProjectNameVersion.get().getName());
        //       assertEquals(rootProjectVersionName, rootProjectNameVersion.get().getVersion());

        final String actual = new Gson().toJson(codeLocation);

        //        try {
        //            JSONAssert.assertEquals(FunctionalTestFiles.asString(expectedResourcePath), actual, false);
        //        } catch (final JSONException e) {
        //            throw new RuntimeException(e);
        //        }
        //testUtil.testJsonResource(expectedResourcePath, result.get()); TODO: WHAT THE FUCK
    }

    private void createNewCodeLocationTest2(final String gradleInspectorOutputFilePath, final String expectedResourcePath, final String rootProjectFilePath, final String rootProjectName, final String rootProjectVersionName)
        throws IOException {
        // TODO: FIX ME

        //        final GradleReportParser gradleReportParser = new GradleReportParser(new ExternalIdFactory());
        //        final Optional<CodeLocation> result = gradleReportParser.parseDependencies(FunctionalTestFiles.asFile(gradleInspectorOutputFilePath));
        //        final Optional<NameVersion> rootProjectNameVersion = gradleReportParser.parseRootProjectNameVersion(FunctionalTestFiles.asFile(rootProjectFilePath));
        //
        //        assertTrue(result.isPresent());
        //        assertTrue(rootProjectNameVersion.isPresent());
        //        assertEquals(rootProjectName, rootProjectNameVersion.get().getName());
        //        assertEquals(rootProjectVersionName, rootProjectNameVersion.get().getVersion());
        //
        //        final String actual = new Gson().toJson(result.get());

        //        try {
        //            JSONAssert.assertEquals(FunctionalTestFiles.asString(expectedResourcePath), actual, false);
        //        } catch (final JSONException e) {
        //            throw new RuntimeException(e);
        //        }
        //testUtil.testJsonResource(expectedResourcePath, result.get()); TODO: WHAT THE FUCK
    }

}
