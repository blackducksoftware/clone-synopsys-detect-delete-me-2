package com.synopsys.integration.detectable.detectables.bitbake.manifest;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.common.util.finder.FileFinder;
import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectables.bitbake.common.parse.BitbakeRecipesParser;
import com.synopsys.integration.detectable.detectables.bitbake.common.parse.GraphParserTransformer;
import com.synopsys.integration.detectable.detectables.bitbake.dependency.BitbakeRecipesToLayerMapConverter;
import com.synopsys.integration.detectable.detectables.bitbake.dependency.parse.BitbakeGraphTransformer;
import com.synopsys.integration.detectable.extraction.Extraction;
import com.synopsys.integration.detectable.util.ToolVersionLogger;

public class BitbakeManifestExtractor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectableExecutableRunner executableRunner;
    private final FileFinder fileFinder;
    private final GraphParserTransformer graphParserTransformer;
    private final BitbakeGraphTransformer bitbakeGraphTransformer;
    private final BitbakeRecipesParser bitbakeRecipesParser;
    private final BitbakeRecipesToLayerMapConverter bitbakeRecipesToLayerMap;
    private final ToolVersionLogger toolVersionLogger;

    public BitbakeManifestExtractor(DetectableExecutableRunner executableRunner, FileFinder fileFinder, GraphParserTransformer graphParserTransformer, BitbakeGraphTransformer bitbakeGraphTransformer,
        BitbakeRecipesParser bitbakeRecipesParser, BitbakeRecipesToLayerMapConverter bitbakeRecipesToLayerMap, ToolVersionLogger toolVersionLogger) {
        this.executableRunner = executableRunner;
        this.fileFinder = fileFinder;
        this.graphParserTransformer = graphParserTransformer;
        this.bitbakeGraphTransformer = bitbakeGraphTransformer;
        this.bitbakeRecipesParser = bitbakeRecipesParser;
        this.bitbakeRecipesToLayerMap = bitbakeRecipesToLayerMap;
        this.toolVersionLogger = toolVersionLogger;
    }

    public Extraction extract(File sourceDirectory, File buildEnvScript, List<String> sourceArguments, List<String> packageNames, boolean followSymLinks, Integer searchDepth, ExecutableTarget bash) {
        Extraction extraction = new Extraction.Builder()
                .failure("Bitbake Manifest Extractor not implemented yet.")
                .build();
        return extraction;
    }
}
