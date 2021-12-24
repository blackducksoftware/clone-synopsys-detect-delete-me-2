package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;

public interface BitbakeManifestGraphBuilderInterface {
    BitbakeManifestGraphBuilderInterface addLayer(String layerName);

    BitbakeManifestGraphBuilderInterface addRecipe(String currentLayer, @Nullable String parentRecipeName, String recipeLayer, String recipeName, String recipeVersion);

    MutableDependencyGraph build();
}
