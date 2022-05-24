package com.synopsys.integration.detect.tool.binaryscanner;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.common.util.finder.SimpleFileFinder;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.OperationException;
import com.synopsys.integration.detect.lifecycle.run.data.DockerTargetData;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationFactory;
import com.synopsys.integration.detect.lifecycle.run.step.BinaryScanStepRunner;
import com.synopsys.integration.detect.util.finder.DetectExcludedDirectoryFilter;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.exception.IntegrationException;

public class BinaryUploadOperationTest {
    @Test
    public void testShouldFailOnDirectory() throws OperationException {
        DetectExcludedDirectoryFilter fileFilter = new DetectExcludedDirectoryFilter(Collections.emptyList());
        BinaryScanOptions binaryScanOptions = new BinaryScanOptions(Paths.get("."), Collections.singletonList(""), fileFilter, 0, false);
        OperationFactory operationFactory = Mockito.mock(OperationFactory.class);

        Mockito.when(operationFactory.calculateBinaryScanOptions()).thenReturn(binaryScanOptions);

        BinaryScanStepRunner binaryScanStepRunner = new BinaryScanStepRunner(operationFactory);
        Optional<File> result = binaryScanStepRunner.determineBinaryScanFileTarget(DockerTargetData.NO_DOCKER_TARGET);

        Mockito.verify(operationFactory).publishBinaryFailure(Mockito.anyString());
        Assertions.assertFalse(result.isPresent());
    }

    @Test
    void testExcludedDirectories() {
        // TODO implement me
        // should, ideally, add tests for sig scanner and detector search exclusions too: basically test the DetectConfigurationFactory refactoring that I did first
    }
    @Test
    public void testMultipleTargetPaths() throws DetectUserFriendlyException, IOException, IntegrationException {
        SimpleFileFinder fileFinder = new SimpleFileFinder();
        DirectoryManager directoryManager = Mockito.mock(DirectoryManager.class);

        File rootDirectory = Files.createTempDirectory("BinaryScannerTest").toFile();
        File subDirectory = new File(rootDirectory, "BinaryScannerSubDirectory");
        File binaryFile_1 = new File(subDirectory, "binaryTestFile_1.txt");
        File binaryFile_2 = new File(subDirectory, "binaryTestFile_2.text");
        FileUtils.write(binaryFile_1, "binary test file 1", StandardCharsets.UTF_8);
        FileUtils.write(binaryFile_2, "binary test file 2", StandardCharsets.UTF_8);
        subDirectory.mkdirs();
        ArrayList<String> targetPaths = new ArrayList<>();
        targetPaths.add("binaryTestFile_1.txt");
        targetPaths.add("*.text");

        Mockito.when(directoryManager.getSourceDirectory()).thenReturn(rootDirectory);
        Mockito.when(directoryManager.getBinaryOutputDirectory()).thenReturn(rootDirectory);

        BinaryScanFindMultipleTargetsOperation multipleTargets = new BinaryScanFindMultipleTargetsOperation(fileFinder, directoryManager);
        Optional<File> zip = multipleTargets.searchForMultipleTargets(targetPaths, false, 3);
        Assertions.assertTrue(zip.isPresent());
        Assertions.assertTrue(zip.get().isFile());
        Assertions.assertTrue(zip.get().canRead());

        FileUtils.deleteDirectory(rootDirectory);
    }
}
