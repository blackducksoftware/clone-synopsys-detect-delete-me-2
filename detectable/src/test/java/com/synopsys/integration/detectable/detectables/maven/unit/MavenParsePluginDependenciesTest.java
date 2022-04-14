package com.synopsys.integration.detectable.detectables.maven.unit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseExtractor;
import com.synopsys.integration.detectable.detectables.maven.parsing.MavenParseOptions;
import com.synopsys.integration.detectable.extraction.Extraction;

public class MavenParsePluginDependenciesTest {

    private File getInput() throws IOException {
        Path tempDirectory = Files.createTempDirectory("pluginDependenciesTest");

        Path input = Paths.get("input");
        List<String> inputLines = Arrays.asList(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
            "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">",
            "    <modelVersion>4.0.0</modelVersion>",
            "    <parent>",
            "        <groupId>com.blackducksoftware.integration</groupId>",
            "        <artifactId>common-maven-parent</artifactId>",
            "        <version>5.0.0</version>",
            "    </parent>",
            "",
            "    <artifactId>hub-teamcity</artifactId>",
            "    <version>4.0.1-SNAPSHOT</version>",
            "    <packaging>pom</packaging>",
            "",
            "    <build>",
            "        <pluginManagement>",
            "            <plugins>",
            "                <plugin>",
            "                    <groupId>org.codehaus.groovy</groupId>",
            "                    <artifactId>groovy-eclipse-compiler</artifactId>",
            "                    <version>2.9.2-01</version>",
            "                    <extensions>true</extensions>",
            "                </plugin>",
            "                <plugin>",
            "                    <groupId>org.apache.maven.plugins</groupId>",
            "                    <artifactId>maven-deploy-plugin</artifactId>",
            "                    <version>2.8.2</version>",
            "                </plugin>",
            "                <plugin>",
            "                    <groupId>org.apache.maven.plugins</groupId>",
            "                    <artifactId>maven-assembly-plugin</artifactId>",
            "                    <version>2.6</version>",
            "                </plugin>",
            "            </plugins>",
            "        </pluginManagement>",
            "    </build>",
            "</project>"
        );

        Path relativePath = tempDirectory.resolve(input);
        Files.createDirectories(relativePath.getParent());
        return Files.write(relativePath, inputLines).toFile();
    }

    private Set<String> getPluginDependencies() {
        Set<String> pluginDependencies = new HashSet<>();
        pluginDependencies.add("org.apache.maven.plugins:maven-assembly-plugin:2.6");
        pluginDependencies.add("org.apache.maven.plugins:maven-deploy-plugin:2.8.2");
        pluginDependencies.add("org.codehaus.groovy:groovy-eclipse-compiler:2.9.2-01");
        return pluginDependencies;
    }

    @Test
    public void testIncludingPluginDependencies() throws Exception {
        MavenParseExtractor pomXmlParser = new MavenParseExtractor(SAXParserFactory.newInstance().newSAXParser());
        Extraction extraction = pomXmlParser.extract(getInput(), new MavenParseOptions(true, true));
        DependencyGraph dependencyGraph = extraction.getCodeLocations().get(0).getDependencyGraph();

        Set<String> externalIds = dependencyGraph.getRootDependencies().stream().map(dependency -> dependency.getExternalId().createExternalId()).collect(Collectors.toSet());
        assertTrue(externalIds.containsAll(getPluginDependencies()));
    }

}
