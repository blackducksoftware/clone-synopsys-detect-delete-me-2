package com.synopsys.integration.detect.tool.detector.inspectors.nuget;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.tool.detector.inspectors.nuget.runtime.DotNetRuntimeParser;

public class DotNetRuntimeParserTest {
    private static final List<String> VALID_RUNTIME_STRINGS = Arrays.asList(
        "Microsoft.AspNetCore.All 2.1.18 [/usr/local/share/dotnet/shared/Microsoft.AspNetCore.All]",
        "Microsoft.AspNetCore.All 2.1.18 [/usr/local/share/dotnet/2.1.18/shared/Microsoft.AspNetCore.All]",
        "Microsoft.AspNetCore.App 2.1.18 [/usr/local/share/dotnet/shared/Microsoft.AspNetCore.App]",
        "Microsoft.AspNetCore.App 2.1.18 [/usr/local/share/dotnet_1.0.0/shared/Microsoft.AspNetCore.App]",
        "Microsoft.NETCore.App 2.1.18 [/usr/local/share/dotnet/shared/Microsoft.NETCore.App]",
        "Microsoft.NETCore.App 3.1.4 [/usr/local/share/dotnet/shared/Microsoft.NETCore.App]"
    );

    @Test
    public void doesRuntimeContainVersionStartingWithValidTest() {
        DotNetRuntimeParser runtimeParser = new DotNetRuntimeParser();
        assertAvailability(Assertions::assertTrue, runtimeParser, "2.1");
        assertAvailability(Assertions::assertTrue, runtimeParser, "3.1");
        assertAvailability(Assertions::assertTrue, runtimeParser, "3.1");
        assertAvailability(Assertions::assertTrue, runtimeParser, "2.1.18");
    }

    @Test
    public void doesRuntimeContainVersionStartingWithInvalidTest() {
        DotNetRuntimeParser runtimeParser = new DotNetRuntimeParser();
        assertAvailability(Assertions::assertFalse, runtimeParser, "4.0");
        assertAvailability(Assertions::assertFalse, runtimeParser, "2.2");
        assertAvailability(Assertions::assertFalse, runtimeParser, "1.0");
    }

    private void assertAvailability(BiConsumer<Boolean, String> assertion, DotNetRuntimeParser runtimeParser, String versionSearchString) {
        boolean isVersionAvailable = runtimeParser.doesRuntimeContainVersionStartingWith(VALID_RUNTIME_STRINGS, versionSearchString);
        assertion.accept(isVersionAvailable, String.format("Different runtime availability expected for '%s' runtime", versionSearchString));
    }
}
