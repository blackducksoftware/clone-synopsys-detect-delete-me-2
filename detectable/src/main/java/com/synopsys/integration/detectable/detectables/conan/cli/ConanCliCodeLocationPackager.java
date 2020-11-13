package com.synopsys.integration.detectable.detectables.conan.cli;

import java.util.Arrays;
import java.util.List;

import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class ConanCliCodeLocationPackager {
    private final ExternalIdFactory externalIdFactory;
    private final ConanInfoParser conanInfoParser;

    public ConanCliCodeLocationPackager(ExternalIdFactory externalIdFactory, ConanInfoParser conanInfoParser) {
        this.externalIdFactory = externalIdFactory;
        this.conanInfoParser = conanInfoParser;
    }

    public List<ConanParseResult> extractCodeLocations(String sourcePath, List<String> conanOutput) {
        return Arrays.asList(conanInfoParser.generateCodeLocation("tbd conan info output string"));
    }
}
