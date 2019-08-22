package com.synopsys.integration.detect.workflow.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.synopsys.integration.detect.kotlin.nameversion.DetectorNameVersionHandler;
import com.synopsys.integration.detect.kotlin.nameversion.DetectorProjectInfoMetadata;
import com.synopsys.integration.detect.kotlin.nameversion.PreferredDetectorNameVersionHandler;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.detect.exception.DetectUserFriendlyException;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.detect.kotlin.nameversion.DetectorProjectInfo;

public class ProjectNameDeciderTests {

    @Test
    public void choosesLowestDepth() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 2;
        add(DetectorType.NPM, lowestDepth, "npm0", "npm0v", possibilities);
        add(DetectorType.MAVEN, lowestDepth + 1, "maven", "npm0v", possibilities);
        add(DetectorType.GRADLE, lowestDepth + 2, "npm1", "npm0v", possibilities);
        add(DetectorType.NPM, lowestDepth + 1, "npm2", "npm0v", possibilities);

        assertProject("npm0", null, possibilities);
    }

    @Test
    public void choosesUniqueOverMultiple() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 2;
        add(DetectorType.NPM, lowestDepth, "npm1", "0", possibilities);
        add(DetectorType.NPM, lowestDepth, "npm2", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth, "gradle", "0", possibilities);

        assertProject("gradle", null, possibilities);
    }

    @Test
    public void choosesAlphabeticalArbitrary() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 1;
        add(DetectorType.GRADLE, lowestDepth, "f", "0", possibilities);
        add(DetectorType.NPM, lowestDepth, "a", "0", possibilities);
        add(DetectorType.MAVEN, lowestDepth, "b", "0", possibilities);

        assertProject("a", null, possibilities);
    }

    @Test
    public void choosesNoneAmongMultiple() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 1;
        add(DetectorType.NPM, lowestDepth, "f-npm", "0", possibilities);
        add(DetectorType.NPM, lowestDepth, "a-npm", "0", possibilities);
        add(DetectorType.NPM, lowestDepth, "b-npm", "0", possibilities);

        assertNoProject(null, possibilities);
    }

    @Test
    public void choosesPreferredEvenDeeper() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 0;
        add(DetectorType.NPM, lowestDepth, "npm", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth + 1, "gradle", "0", possibilities);

        assertProject("gradle", DetectorType.GRADLE, possibilities);
    }

    @Test
    public void choosesShallowestPrefferred() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 0;
        add(DetectorType.NPM, lowestDepth, "npm1", "0", possibilities);
        add(DetectorType.NPM, lowestDepth, "npm2", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth, "c-gradle1", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth + 1, "a-gradle2", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth + 1, "b-gradle3", "0", possibilities);

        assertProject("c-gradle1", DetectorType.GRADLE, possibilities);
    }

    @Test
    public void choosesNonWithNoPreferredBomToolFound() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 0;
        add(DetectorType.NPM, lowestDepth, "a", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth, "b", "0", possibilities);

        assertNoProject(DetectorType.MAVEN, possibilities);
    }

    @Test
    public void choosesNoPreferredBomToolWhenMultipleFound() throws DetectUserFriendlyException {
        final List<DetectorProjectInfo> possibilities = new ArrayList<>();

        final int lowestDepth = 0;
        add(DetectorType.NPM, lowestDepth, "a", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth, "b", "0", possibilities);
        add(DetectorType.GRADLE, lowestDepth, "b", "0", possibilities);

        assertNoProject(DetectorType.GRADLE, possibilities);
    }

    private Optional<NameVersion> decide(final DetectorType preferred, final List<DetectorProjectInfo> possibilities) {
        final DetectorNameVersionHandler decider;
        if (preferred != null) {
            decider = new PreferredDetectorNameVersionHandler(preferred);
        } else {
            decider = new DetectorNameVersionHandler(Collections.singletonList(DetectorType.GIT));
        }
        for (final DetectorProjectInfo possibility : possibilities) {
            if (decider.willAccept(new DetectorProjectInfoMetadata(possibility.getDetectorType(), possibility.getDepth()))) {
                decider.accept(possibility);
            }
        }
        return decider.finalDecision().getChosenNameVersion();
    }

    private void assertProject(final String projectName, final DetectorType preferred, final List<DetectorProjectInfo> possibilities) throws DetectUserFriendlyException {
        Optional<NameVersion> chosen = decide(preferred, possibilities);
        Assert.assertTrue(chosen.isPresent());
        Assert.assertEquals(chosen.get().getName(), projectName);
    }

    private void assertNoProject(final DetectorType preferred, final List<DetectorProjectInfo> possibilities) throws DetectUserFriendlyException {
        Optional<NameVersion> chosen = decide(preferred, possibilities);
        Assert.assertFalse(chosen.isPresent());
    }

    private void add(final DetectorType type, final int depth, final String projectName, final String projectVersion, final List<DetectorProjectInfo> list) {
        final NameVersion nameVersion = new NameVersion(projectName, projectVersion);
        final DetectorProjectInfo possibility = new DetectorProjectInfo(type, depth, nameVersion);
        list.add(possibility);
    }

}
