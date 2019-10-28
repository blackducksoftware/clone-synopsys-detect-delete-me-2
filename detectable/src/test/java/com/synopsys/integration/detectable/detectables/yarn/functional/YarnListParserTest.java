package com.synopsys.integration.detectable.detectables.yarn.functional;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.annotations.FunctionalTest;
import com.synopsys.integration.detectable.detectables.yarn.parse.PackageJson;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLineLevelParser;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLock;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnLockParser;
import com.synopsys.integration.detectable.detectables.yarn.parse.YarnTransformer;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;
import com.synopsys.integration.detectable.util.GraphCompare;
import com.synopsys.integration.detectable.util.graph.NameVersionGraphAssert;

@FunctionalTest
public class YarnListParserTest {
    @Test
    void parseCrazyYarnListTest() {
        final List<String> yarnLock = FunctionalTestFiles.asListOfStrings("/yarn/yarn-lock-missing.txt");
        final String packageJson = FunctionalTestFiles.asString("/yarn/yarn-package-json-missing.txt");

        final DependencyGraph dependencyGraph = createDependencyGraph(yarnLock, packageJson);
        final NameVersionGraphAssert graphAssert = new NameVersionGraphAssert(Forge.NPMJS, dependencyGraph);
        graphAssert.hasDependency("missing", "1.0.0");
    }

    @Test
    void parseYarnListTest() {
        final List<String> yarnLock = FunctionalTestFiles.asListOfStrings("/yarn/yarn-lock-simple.txt");
        final String packageJson = FunctionalTestFiles.asString("/yarn/yarn-list-simple.txt");

        final DependencyGraph dependencyGraph = createDependencyGraph(yarnLock, packageJson);
        GraphCompare.assertEqualsResource("/yarn/yarn-simple-expected-graph.json", dependencyGraph);
    }

    @Test
    void parseYarnListWithResolvableVersions() {
        final List<String> yarnLock = FunctionalTestFiles.asListOfStrings("/yarn/yarn-lock-resolved.txt");
        final String packageJson = FunctionalTestFiles.asString("/yarn/yarn-list-resolved.txt");

        final DependencyGraph dependencyGraph = createDependencyGraph(yarnLock, packageJson);
        GraphCompare.assertEqualsResource("/yarn/yarn-resolved-expected-graph.json", dependencyGraph);
    }

    private DependencyGraph createDependencyGraph(final List<String> yarnLockText, final String packageJsonText) {
        final YarnLineLevelParser lineLevelParser = new YarnLineLevelParser();
        final YarnLockParser yarnLockParser = new YarnLockParser(lineLevelParser);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final PackageJson packageJson = gson.fromJson(packageJsonText, PackageJson.class);

        final ExternalIdFactory externalIdFactory = new ExternalIdFactory();
        final YarnTransformer yarnTransformer = new YarnTransformer(externalIdFactory);

        final YarnLock yarnLock = yarnLockParser.parseYarnLock(yarnLockText);
        final DependencyGraph dependencyGraph = yarnTransformer.transform(packageJson, yarnLock);
        return dependencyGraph;
    }
}
