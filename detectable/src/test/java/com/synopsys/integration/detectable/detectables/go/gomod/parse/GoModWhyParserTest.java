package com.synopsys.integration.detectable.detectables.go.gomod.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class GoModWhyParserTest {

    @Test
    public void testParserUnused() throws IOException {
        Set<String> expectedExclusionSet = new LinkedHashSet<>();
        expectedExclusionSet.add("example.com/invalid-module-1");
        expectedExclusionSet.add("example.com/invalid-module-2");
        expectedExclusionSet.add("example.com/invalid-module-3");

        // Upgrades to the GoModWhyParser means this test case no longer breaks the parser
        expectedExclusionSet.add("example.com/invalid-missing-end-paren");

        GoModWhyParser goModWhyParser = new GoModWhyParser();
        File goModWhyOutputFile = new File("src/test/resources/detectables/unit/go/gomodwhy.xout");
        List<String> lines = Files.readAllLines(goModWhyOutputFile.toPath());
        Set<String> actualExclusionSet = goModWhyParser.createModuleExclusionList(lines);

        assertEquals(expectedExclusionSet, actualExclusionSet);
    }

}
