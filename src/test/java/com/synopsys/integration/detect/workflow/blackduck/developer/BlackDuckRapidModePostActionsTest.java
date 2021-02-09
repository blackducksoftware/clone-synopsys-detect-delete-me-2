package com.synopsys.integration.detect.workflow.blackduck.developer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.detect.workflow.DetectRun;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.file.DirectoryOptions;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class BlackDuckRapidModePostActionsTest {

    @Test
    public void testJsonFileContent() throws Exception {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        EventSystem eventSystem = Mockito.mock(EventSystem.class);
        Path scanOutputPath = Files.createTempDirectory("rapid_scan_output_path");
        DirectoryOptions directoryOptions = new DirectoryOptions(null, null, null, scanOutputPath, null);
        DirectoryManager directoryManager = new DirectoryManager(directoryOptions, new DetectRun(""));

        File expectedOutputFile = new File("src/test/resources/workflow/blackduck/rapid_scan_result_file.json");
        String expectedOutput = FileUtils.readFileToString(expectedOutputFile, StandardCharsets.UTF_8).trim();

        List<DeveloperScanComponentResultView> results = createResults(gson, expectedOutput);

        BlackDuckRapidModePostActions postActions = new BlackDuckRapidModePostActions(gson, eventSystem, directoryManager);
        NameVersion nameVersion = new NameVersion("rapid_scan_post_action", "test");
        postActions.perform(nameVersion, results);

        File actualOutputFile = createActualOutputFile(directoryManager, nameVersion);
        String actualOutput = FileUtils.readFileToString(actualOutputFile, StandardCharsets.UTF_8).trim();

        assertEquals(expectedOutput, actualOutput);
    }

    private List<DeveloperScanComponentResultView> createResults(Gson gson, String jsonContent) {
        List<DeveloperScanComponentResultView> results = new ArrayList<>();
        JsonArray array = gson.fromJson(jsonContent, JsonArray.class);
        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            JsonElement arrayItem = iterator.next();
            results.add(gson.fromJson(arrayItem, DeveloperScanComponentResultView.class));
        }

        return results;
    }

    private File createActualOutputFile(DirectoryManager directoryManager, NameVersion projectNameVersion) {
        IntegrationEscapeUtil escapeUtil = new IntegrationEscapeUtil();
        String escapedProjectName = escapeUtil.replaceWithUnderscore(projectNameVersion.getName());
        String escapedProjectVersionName = escapeUtil.replaceWithUnderscore(projectNameVersion.getVersion());
        return new File(directoryManager.getScanOutputDirectory(), escapedProjectName + "_" + escapedProjectVersionName + "_BlackDuck_DeveloperMode_Result.json");
    }
}
