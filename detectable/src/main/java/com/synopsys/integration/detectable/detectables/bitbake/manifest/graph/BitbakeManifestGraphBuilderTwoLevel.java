package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;

public class BitbakeManifestGraphBuilderTwoLevel implements BitbakeManifestGraphBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator;
    private MutableDependencyGraph dependencyGraph;
    private final Map<String, Dependency> recipeDependenciesAdded = new HashMap<>();

    public BitbakeManifestGraphBuilderTwoLevel(BitbakeManifestExternalIdGenerator bitbakeManifestExternalIdGenerator) {
        this.bitbakeManifestExternalIdGenerator = bitbakeManifestExternalIdGenerator;
        dependencyGraph = new MutableMapDependencyGraph();
    }
    @Override
    public BitbakeManifestGraphBuilder addLayer(final String layerName) {
        return this;
    }

    @Override
    public BitbakeManifestGraphBuilder addRecipe(final String currentLayer, @Nullable final String parentRecipeName, final String recipeLayer, final String recipeName, final String recipeVersion) {
        return null;
    }

    @Override
    public MutableDependencyGraph build() {
        return null;
    }
}
