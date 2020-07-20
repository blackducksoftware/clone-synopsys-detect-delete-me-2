/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.battery;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.skyscreamer.jsonassert.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.detect.Application;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detectable.detectable.executable.Executable;
import com.synopsys.integration.detectable.detectable.executable.ExecutableOutput;
import com.synopsys.integration.detectable.detectable.executable.ExecutableRunnerException;
import com.synopsys.integration.detectable.detectable.executable.impl.SimpleExecutableRunner;

import freemarker.template.TemplateException;

public final class BatteryTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final List<BatteryExecutable> executables = new ArrayList<>();

    private final List<String> additionalProperties = new ArrayList<>();
    private final List<String> emptyFileNames = new ArrayList<>();
    private final List<String> resourceFileNames = new ArrayList<>();
    private final List<String> resourceZipNames = new ArrayList<>();
    private boolean shouldExpectBdioResources = false;
    private String sourceDirectoryName = "source";

    private String detectVersion = "";
    private String toolsValue = "DETECTOR";
    private boolean useDetectScript = false;

    private File batteryDirectory;
    private File mockDirectory;
    private File outputDirectory;
    private File bdioDirectory;
    private File sourceDirectory;

    private final String testName;
    private final String resourcePrefix;

    private final AtomicInteger commandCount = new AtomicInteger();
    private final AtomicInteger executableCount = new AtomicInteger();

    public BatteryTest(String name) {
        this.testName = name;
        this.resourcePrefix = name;
    }

    public BatteryTest(String testName, String resourcePrefix) {
        this.testName = testName;
        this.resourcePrefix = resourcePrefix;
    }

    private List<String> prefixResources(String... resourceFiles) {
        return Arrays.stream(resourceFiles)
                   .map(it -> "/" + this.resourcePrefix + "/" + it)
                   .collect(Collectors.toList());
    }

    public void executableFromResourceFiles(Property detectProperty, String... resourceFiles) {
        ResourceTypingExecutableCreator creator = new ResourceTypingExecutableCreator(prefixResources(resourceFiles));
        executables.add(BatteryExecutable.propertyOverrideExecutable(detectProperty, creator));
    }

    public void executableSourceFileFromResourceFiles(String windowsName, String linuxName, String... resourceFiles) {
        ResourceTypingExecutableCreator creator = new ResourceTypingExecutableCreator(prefixResources(resourceFiles));
        executables.add(BatteryExecutable.sourceFileExecutable(windowsName, linuxName, creator));
    }

    public ResourceCopyingExecutableCreator executableThatCopiesFiles(Property detectProperty, String... resourceFiles) {
        ResourceCopyingExecutableCreator resourceCopyingExecutable = new ResourceCopyingExecutableCreator(prefixResources(resourceFiles));
        executables.add(BatteryExecutable.propertyOverrideExecutable(detectProperty, resourceCopyingExecutable));
        return resourceCopyingExecutable;
    }

    public ResourceCopyingExecutableCreator executableSourceFileThatCopiesFiles(String windowsName, String linuxName, String... resourceFiles) {
        ResourceCopyingExecutableCreator resourceCopyingExecutable = new ResourceCopyingExecutableCreator(prefixResources(resourceFiles));
        executables.add(BatteryExecutable.sourceFileExecutable(windowsName, linuxName, resourceCopyingExecutable));
        return resourceCopyingExecutable;
    }

    public void executable(Property detectProperty, String... responses) {
        executables.add(BatteryExecutable.propertyOverrideExecutable(detectProperty, new StringTypingExecutableCreator(Arrays.asList(responses))));
    }

    public void git(String origin, String branch) {
        sourceFileNamed(".git");
        executable(DetectProperties.DETECT_GIT_PATH(), origin, branch);
    }

    public void sourceFileNamed(String filename) {
        emptyFileNames.add(filename);
    }

    public void sourceFolderFromExpandedResource(String filename) {
        resourceZipNames.add(filename);
    }

    public void sourceFileFromResource(String filename) {
        resourceFileNames.add(filename);
    }

    public void expectBdioResources() {
        shouldExpectBdioResources = true;
    }

    public void property(Property property, String value) {
        property(property.getKey(), value);
    }

    public void property(String property, String value) {
        additionalProperties.add("--" + property + "=" + value);
    }

    public void withDetectLatest() {
        useDetectScript = true;
        detectVersion = "";
    }

    public void withDetectVersion(String version) {
        useDetectScript = true;
        detectVersion = version;
    }

    public void withToolsValue(String toolsValue) {
        this.toolsValue = toolsValue;
    }

    public void run() {
        try {
            checkEnvironment();
            initializeDirectories();
            createFiles();
            List<String> executableArguments = createExecutables();
            runDetect(executableArguments);

            assertBdio();
        } catch (ExecutableRunnerException | IOException | JSONException | TemplateException | BdioCompare.BdioCompareException e) {
            Assertions.assertNull(e, "An exception should not have been thrown!");
        }
    }

    private void runDetect(List<String> additionalArguments) throws IOException, ExecutableRunnerException {
        List<String> detectArguments = new ArrayList<>();
        Map<Property, String> properties = new HashMap<>();

        properties.put(DetectProperties.DETECT_TOOLS(), toolsValue);
        properties.put(DetectProperties.BLACKDUCK_OFFLINE_MODE(), "true");
        properties.put(DetectProperties.DETECT_OUTPUT_PATH(), outputDirectory.getCanonicalPath());
        properties.put(DetectProperties.DETECT_BDIO_OUTPUT_PATH(), bdioDirectory.getCanonicalPath());
        properties.put(DetectProperties.DETECT_CLEANUP(), "false");
        properties.put(DetectProperties.LOGGING_LEVEL_COM_SYNOPSYS_INTEGRATION(), "INFO"); // Leave at INFO for Travis. Long logs cause build to fail.
        properties.put(DetectProperties.DETECT_SOURCE_PATH(), sourceDirectory.getCanonicalPath());
        for (Map.Entry<Property, String> entry : properties.entrySet()) {
            detectArguments.add("--" + entry.getKey().getKey() + "=" + entry.getValue());
        }

        detectArguments.addAll(additionalArguments);
        detectArguments.addAll(additionalProperties);

        if (executeDetectScript(detectArguments)) {
            logger.info("Executed as script.");
        } else if (executeDetectJar(detectArguments)) {
            logger.info("Executed as jar.");
        } else {
            logger.info("Executed as static.");
            executeDetectStatic(detectArguments);
        }
    }

    private void executeDetectStatic(List<String> detectArguments) {
        boolean previous = Application.shouldExit();
        Application.setShouldExit(false);
        Application.main(detectArguments.toArray(new String[0]));
        Application.setShouldExit(previous);
    }

    private ExecutableOutput downloadDetectBash(File target) throws ExecutableRunnerException {
        List<String> shellArguments = new ArrayList<>();
        shellArguments.add("-s");
        shellArguments.add("-L");
        shellArguments.add("https://detect.synopsys.com/detect.sh");
        shellArguments.add("-o");
        shellArguments.add(target.toString());

        Executable executable = new Executable(outputDirectory, new HashMap<>(), "curl", shellArguments);
        SimpleExecutableRunner executableRunner = new SimpleExecutableRunner();
        return executableRunner.execute(executable);
    }

    private boolean executeDetectScript(List<String> detectArguments) throws ExecutableRunnerException {
        if (!useDetectScript) {
            return false;
        }

        List<String> shellArguments = new ArrayList<>();
        String target = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            target = "powershell";
            shellArguments.add("\"[Net.ServicePointManager]::SecurityProtocol = 'tls12'; irm https://detect.synopsys.com/detect.ps1?$(Get-Random) | iex; detect\"");
        } else {
            File scriptTarget = new File(batteryDirectory, "detect.sh");
            if (scriptTarget.exists()) {
                Assertions.assertTrue(scriptTarget.delete(), "Failed to cleanup an existing detect shell script. This file is cleaned up to ensure latest script is always used.");
            }
            ExecutableOutput downloadOutput = downloadDetectBash(scriptTarget);
            Assertions.assertTrue(downloadOutput.getReturnCode() == 0 && scriptTarget.exists(), "Something went wrong downloading the detect script.");
            Assertions.assertTrue(scriptTarget.setExecutable(true), "Failed to change script permissions to execute. The downloaded detect script must be executable.");
            target = scriptTarget.toString();
        }
        shellArguments.addAll(detectArguments);

        Map<String, String> environmentVariables = new HashMap<>();

        if (StringUtils.isNotBlank(detectVersion)) {
            environmentVariables.put("DETECT_LATEST_RELEASE_VERSION", detectVersion);
        }

        Executable executable = new Executable(outputDirectory, environmentVariables, target, shellArguments);
        SimpleExecutableRunner executableRunner = new SimpleExecutableRunner();
        ExecutableOutput result = executableRunner.execute(executable);

        Assertions.assertEquals(0, result.getReturnCode(), "Detect returned a non-zero exit code:" + result.getReturnCode());

        List<String> lines = result.getStandardOutputAsList();

        Assertions.assertTrue(lines.size() > 0, "Detect wrote nothing to standard out.");

        return true;
    }

    private boolean executeDetectJar(List<String> detectArguments) throws ExecutableRunnerException {
        String java = System.getenv("BATTERY_TESTS_JAVA_PATH");
        String detectJar = System.getenv("BATTERY_TESTS_DETECT_JAR_PATH");
        boolean bothExist = StringUtils.isNotBlank(java) && StringUtils.isNotBlank(detectJar) && new File(java).exists() && new File(detectJar).exists();
        if (!bothExist) {
            return false;
        }

        List<String> javaArguments = new ArrayList<>();
        javaArguments.add("-jar");
        javaArguments.add(detectJar);
        javaArguments.addAll(detectArguments);

        SimpleExecutableRunner executableRunner = new SimpleExecutableRunner();
        ExecutableOutput result = executableRunner.execute(outputDirectory, java, javaArguments);

        Assertions.assertEquals(0, result.getReturnCode(), "Detect returned a non-zero exit code:" + result.getReturnCode());

        List<String> lines = result.getStandardOutputAsList();

        Assertions.assertTrue(lines.size() > 0, "Detect wrote nothing to standard out.");

        return true;
    }

    private void initializeDirectories() throws IOException {
        File testDirectory = new File(batteryDirectory, testName);

        if (testDirectory.exists()) {
            FileUtils.deleteDirectory(testDirectory);
        }

        mockDirectory = new File(testDirectory, "mock");
        outputDirectory = new File(testDirectory, "output");
        bdioDirectory = new File(testDirectory, "bdio");
        sourceDirectory = new File(testDirectory, sourceDirectoryName);

        Assertions.assertTrue(outputDirectory.mkdirs());
        Assertions.assertTrue(sourceDirectory.mkdirs());
        Assertions.assertTrue(bdioDirectory.mkdirs());
        Assertions.assertTrue(mockDirectory.mkdirs());
    }

    private void checkEnvironment() {
        Assumptions.assumeTrue(StringUtils.isNotBlank(System.getenv().get("BATTERY_TESTS_PATH")), "The environment variable BATTERY_TESTS_PATH must be set.");

        batteryDirectory = new File(System.getenv("BATTERY_TESTS_PATH"));
        if (!batteryDirectory.exists()) {
            Assertions.assertTrue(batteryDirectory.mkdirs(), String.format("Failed to create battery directory at: %s", batteryDirectory.getAbsolutePath()));
        }
        Assertions.assertTrue(batteryDirectory.exists(), "The detect battery path must exist.");
    }

    private List<String> createExecutables() throws IOException, TemplateException {
        List<String> properties = new ArrayList<>();

        for (BatteryExecutable executable : executables) {
            int id = executableCount.getAndIncrement();
            Assertions.assertNotNull(executable.creator, "Every battery executable must have a 'creator' or a way to actually generate the executable..");
            BatteryExecutableInfo info = new BatteryExecutableInfo(mockDirectory, sourceDirectory);
            File commandFile = executable.creator.createExecutable(id, info, commandCount);
            if (executable.detectProperty != null) {
                properties.add("--" + executable.detectProperty.getKey() + "=" + commandFile.getCanonicalPath());
            } else if (executable.linuxSourceFileName != null && executable.windowsSourceFileName != null) {
                File target;
                if (SystemUtils.IS_OS_WINDOWS) {
                    target = new File(sourceDirectory, executable.windowsSourceFileName);
                } else {
                    target = new File(sourceDirectory, executable.linuxSourceFileName);
                }
                FileUtils.moveFile(commandFile, target);
            } else {
                throw new RuntimeException("Every battery executable must either specify an override property or a location (for both linux and windows) in the source directory for the executable to go.");
            }
        }
        return properties;
    }

    private void createFiles() throws IOException {
        for (String filename : emptyFileNames) {
            File file = new File(sourceDirectory, filename);
            FileUtils.writeStringToFile(file, "THIS FILE INTENTIONALLY LEFT BLANK", Charset.defaultCharset());
        }

        for (String resourceFileName : resourceFileNames) {
            InputStream inputStream = BatteryFiles.asInputStream("/" + resourcePrefix + "/" + resourceFileName);
            File file = new File(sourceDirectory, resourceFileName);
            Assertions.assertNotNull(inputStream, "Could not find resource file: " + file);
            FileUtils.copyInputStreamToFile(inputStream, file);
        }

        for (String resourceZipFileName : resourceZipNames) {
            File zipFile = BatteryFiles.asFile("/" + resourcePrefix + "/" + resourceZipFileName + ".zip");
            File target = new File(sourceDirectory, resourceZipFileName);
            ZipUtil.unpack(zipFile, target);
        }
    }

    private void assertBdio() throws IOException, JSONException, BdioCompare.BdioCompareException {
        File[] bdio = bdioDirectory.listFiles();
        Assertions.assertTrue(bdio != null && bdio.length > 0, "Bdio output files could not be found.");

        if (shouldExpectBdioResources) {
            File expectedBdioFolder = BatteryFiles.asFile("/" + resourcePrefix + "/bdio");
            File[] expectedBdioFiles = expectedBdioFolder.listFiles();
            Assertions.assertTrue(expectedBdioFiles != null && expectedBdioFiles.length > 0, "Expected bdio resource files could not be found: " + expectedBdioFolder.getCanonicalPath());
            Assertions.assertEquals(expectedBdioFiles.length, bdio.length, "Detect did not create the expected number of bdio files.");

            List<File> actualByName = Arrays.stream(bdio).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
            List<File> expectedByName = Arrays.stream(expectedBdioFiles).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());

            int issueCount = 0;
            for (int i = 0; i < expectedByName.size(); i++) {
                logger.info("***BDIO BATTERY TEST|" + testName + "|" + resourcePrefix + "|" + expectedByName.get(i).getName() + "***");

                File expected = expectedByName.get(i);
                File actual = actualByName.get(i);
                Assertions.assertEquals(expected.getName(), actual.getName(), "Bdio file names did not match when sorted.");

                String expectedJson = FileUtils.readFileToString(expected, Charset.defaultCharset());
                String actualJson = FileUtils.readFileToString(actual, Charset.defaultCharset());

                JSONArray expectedJsonArray = (JSONArray) JSONParser.parseJSON(expectedJson);
                JSONArray actualJsonArray = (JSONArray) JSONParser.parseJSON(actualJson);

                BdioCompare compare = new BdioCompare();
                List<BdioCompare.BdioIssue> issues = compare.compare(expectedJsonArray, actualJsonArray);

                if (issues.size() > 0) {
                    logger.error("=================");
                    logger.error("BDIO Issues");
                    logger.error("Expected: " + expected.getCanonicalPath());
                    logger.error("Actual: " + actual.getCanonicalPath());
                    logger.error("=================");
                    issues.forEach(issue -> logger.error(issue.getIssue()));
                    logger.error("=================");
                }
                issueCount += issues.size();
            }
            Assertions.assertEquals(0, issueCount, "The BDIO comparison failed, one or more issues were found, please check the logs.");

        } else {
            Assertions.assertEquals(0, bdio.length, "No bdio resources were asserted but detect created bdio files. Add resource bdio assertion or add a new type of assertion.");
        }
    }

    public void sourceDirectoryNamed(String sourceDirectoryName) {
        this.sourceDirectoryName = sourceDirectoryName;
    }
}
