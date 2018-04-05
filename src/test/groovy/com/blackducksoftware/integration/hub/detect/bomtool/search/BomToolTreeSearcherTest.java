package com.blackducksoftware.integration.hub.detect.bomtool.search;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.blackducksoftware.integration.hub.detect.Application;
import com.blackducksoftware.integration.hub.detect.bomtool.NestedBomTool;
import com.blackducksoftware.integration.test.TestLogger;
import com.blackducksoftware.integration.util.ResourceUtil;

@ContextConfiguration(classes = { Application.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class BomToolTreeSearcherTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File sourceDirectoryWithMultipleYarn;

    private File sourceDirectoryWithNestedNPM;

    private File sourceDirectoryWithNestedNPMInsideNodeModules;

    @Autowired
    private Set<NestedBomTool> nestedBomTools;

    @Before
    public void setupSearchStructure() throws Exception {
        sourceDirectoryWithMultipleYarn = folder.newFolder();
        File yarnBaseDir = new File(sourceDirectoryWithMultipleYarn, "yarnBaseDir");
        yarnBaseDir.mkdirs();
        String yarnLockContent = ResourceUtil.getResourceAsString(BomToolTreeSearcherTest.class, "/yarn/yarn.lock", StandardCharsets.UTF_8);
        File baseYarnLock = new File(yarnBaseDir, "yarn.lock");
        Files.write(baseYarnLock.toPath(), yarnLockContent.getBytes(StandardCharsets.UTF_8));
        File yarnDirectory = new File(yarnBaseDir, "yarnDir");
        yarnDirectory.mkdirs();
        File subYarnLock = new File(yarnDirectory, "yarn.lock");
        Files.write(subYarnLock.toPath(), yarnLockContent.getBytes(StandardCharsets.UTF_8));

        String npmPackageLockContent = ResourceUtil.getResourceAsString(BomToolTreeSearcherTest.class, "/npm/package-lock.json", StandardCharsets.UTF_8);
        sourceDirectoryWithNestedNPM = folder.newFolder();
        File npmBaseDir = new File(sourceDirectoryWithNestedNPM, "npmBaseDir");
        File npmDirectory = new File(npmBaseDir, "npmDir");
        File subNpmDirectory = new File(npmDirectory, "subNpmDirectory");
        subNpmDirectory.mkdirs();
        File npmPackageLock = new File(subNpmDirectory, "package-lock.json");
        Files.write(npmPackageLock.toPath(), npmPackageLockContent.getBytes(StandardCharsets.UTF_8));

        sourceDirectoryWithNestedNPMInsideNodeModules = folder.newFolder();
        File npmBaseWithNodeModulesDir = new File(sourceDirectoryWithNestedNPMInsideNodeModules, "npmBaseDir");
        npmBaseWithNodeModulesDir.mkdirs();
        File nodeModulesDirectory = new File(npmBaseWithNodeModulesDir, "node_modules");
        nodeModulesDirectory.mkdirs();
        File nodeModulesNpmPackageLock = new File(nodeModulesDirectory, "package-lock.json");
        Files.write(nodeModulesNpmPackageLock.toPath(), npmPackageLockContent.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testSearchBomToolSearchYarnNoDepth() throws Exception {
        final int maximumDepth = 0;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);

        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithMultipleYarn);

        assertEquals(0, bomToolTreeSearcher.getResults().size());
    }

    @Test
    public void testSearchBomToolSearchYarnDepth1() throws Exception {
        final int maximumDepth = 1;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);

        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithMultipleYarn);

        assertEquals(1, bomToolTreeSearcher.getResults().size());
    }

    @Test
    public void testSearchBomToolSearchYarnDepth2() throws Exception {
        final int maximumDepth = 2;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);

        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithMultipleYarn);
        // Should have only found one because the yarn projects are nested
        assertEquals(1, bomToolTreeSearcher.getResults().size());
    }

    @Test
    public void testSearchBomToolSearchYarnDepth2Forced() throws Exception {
        final int maximumDepth = 2;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, true, maximumDepth);

        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithMultipleYarn);

        assertEquals(2, bomToolTreeSearcher.getResults().size());
    }

    @Test
    public void testSearchBomToolSearchNpm() throws Exception {
        int maximumDepth = 2;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);

        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithNestedNPM);
        // Should not have found anything because the Npm project is deeper than 2 directories down
        assertEquals(0, bomToolTreeSearcher.getResults().size());

        maximumDepth = 3;
        bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);
        bomToolTreeSearcher.startSearching(nestedBomTools, sourceDirectoryWithNestedNPM);
        assertEquals(1, bomToolTreeSearcher.getResults().size());
    }

    @Test
    public void testSearchBomToolSearchNpmWithinNodeModules() throws Exception {
        final int maximumDepth = 2;

        BomToolTreeSearcher bomToolTreeSearcher = new BomToolTreeSearcher(null, false, maximumDepth);

        bomToolTreeSearcher.startSearching( nestedBomTools, sourceDirectoryWithNestedNPMInsideNodeModules);
        // Should not have found the Npm project because it is in a node_modules directory
        assertEquals(0, bomToolTreeSearcher.getResults().size());

        bomToolTreeSearcher = new BomToolTreeSearcher(null, true, maximumDepth);

        bomToolTreeSearcher.startSearching( nestedBomTools, sourceDirectoryWithNestedNPMInsideNodeModules);
        // Should  have found the Npm project because we are now forcing the search
        assertEquals(1, bomToolTreeSearcher.getResults().size());
    }

}
