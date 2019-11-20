package com.synopsys.integration.detect;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.configuration.DetectProperty;

public class DeprecatedPropertyReferenceTest {

    private final List<String> filesAllowedToReferenceDeprecatedComponents = Arrays.asList(
        "Application.java",
        "DetectBoot.java",
        "ProductDecider.java",
        "DetectConfigurationFactory.java",
        "DetectableOptionFactory.java",
        "PolarisTool.java",
        "ProductDeciderTest.java",
        String.format("%s.java", this.getClass().getSimpleName())
    );

    @Test
    public void testCodeReferencesToDeprecatedProperties() throws IOException {
        final Set<String> classesInViolation = new HashSet<>();
        final String thisJavaFilename = String.format("%s.java", this.getClass().getSimpleName());
        final List<DetectProperty> deprecatedProperties = getDeprecatedProperties();
        final String[] targetSuffixes = { "java", "groovy" };
        final File rootDir = new File("src");
        final Collection<File> javaFiles = FileUtils.listFiles(rootDir, targetSuffixes, true);
        for (final File javaFile : javaFiles) {
            final String javaFilename = javaFile.getName();
            if (isFileAllowedToReferenceDeprecatedProperties(javaFilename)) {
                continue;
            }
            final String javaFileContents = FileUtils.readFileToString(javaFile, StandardCharsets.UTF_8);
            for (final DetectProperty deprecatedProperty : deprecatedProperties) {
                if (javaFileContents.contains(deprecatedProperty.name())) {
                    System.out.printf("Deprecated property %s is referenced in %s\n", deprecatedProperty.name(), javaFile.getAbsolutePath());
                    classesInViolation.add(javaFilename);
                }
            }
        }

        Assertions.assertEquals(0, classesInViolation.size(), String.format("The following classes are in violation: %s", StringUtils.join(classesInViolation, ", ")));
    }

    private List<DetectProperty> getDeprecatedProperties() {
        final ArrayList<DetectProperty> deprecatedProperties = new ArrayList<>(64);
        for (final Field field : DetectProperty.class.getDeclaredFields()) {
            if (field.isEnumConstant() && field.isAnnotationPresent(Deprecated.class)) {
                final DetectProperty property = DetectProperty.valueOf(field.getName());
                deprecatedProperties.add(property);
            }
        }
        return deprecatedProperties;
    }

    private boolean isFileAllowedToReferenceDeprecatedProperties(final String filename) {
        return filesAllowedToReferenceDeprecatedComponents.contains(filename);
    }
}
