package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class BitbakeManifestGraphBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExternalIdFactory externalIdFactory;
    private MutableDependencyGraph dependencyGraph;
    private final Map<String, Dependency> layerDependenciesAdded = new HashMap<>();
    private final Map<String, Dependency> recipeDependenciesAdded = new HashMap<>();

    public BitbakeManifestGraphBuilder(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
        dependencyGraph = new MutableMapDependencyGraph();
    }

    public BitbakeManifestGraphBuilder addLayer(String layerName) {
        if (layerDependenciesAdded.get(layerName) != null) {
            logger.warn("Attempt to add layer {} to graph more than once");
            return this;
        }
        logger.info("*** layer: {}", layerName);
        ExternalId layerExternalId = generateLayerExternalId(layerName);
        Dependency layerDependency = new Dependency(layerExternalId);
        dependencyGraph.addChildToRoot(layerDependency);
        layerDependenciesAdded.put(layerName, layerDependency);
        logger.info("*** layerExternalId for layer: {}: {}", layerName, layerExternalId.toString());
        return this;
    }

    public BitbakeManifestGraphBuilder addRecipe(String currentLayer, @Nullable String parentRecipeName, String recipeLayer, String recipeName, String recipeVersion) {
        if (recipeDependenciesAdded.containsKey(recipeName)) {
            // if we were building a true graph, we wouldn't do this
            return this;
        }
        ExternalId imageRecipeExternalId = generateRecipeExternalId(recipeLayer, recipeName, recipeVersion);
        Dependency recipeDependency = new Dependency(imageRecipeExternalId);
        // If we wanted a true graph: if (parentRecipeName != null) parentDependency = recipeDependenciesAdded.get(parentRecipeName);
        Dependency parentDependency = layerDependenciesAdded.get(currentLayer);
        dependencyGraph.addChildWithParent(recipeDependency, parentDependency);
        recipeDependenciesAdded.put(recipeName, recipeDependency);

        //logger.info("*** externalId for recipe: {}:{}:{}: {}", recipeLayer, recipeName, recipeVersion, imageRecipeExternalId.toString());
        return this;
    }

    public MutableDependencyGraph build() {
        return dependencyGraph;
    }

    private ExternalId generateRecipeExternalId(String layerName, String recipeName, @NotNull String recipeVersion) {
        if (recipeVersion.contains("AUTOINC")) {
            recipeVersion = recipeVersion.replaceFirst("AUTOINC\\+[\\w|\\d]*", "X");
        }
        ExternalId externalId = externalIdFactory.createYoctoExternalId(layerName, recipeName, recipeVersion);
        return externalId;
    }

    private ExternalId generateLayerExternalId(String layerName) {
        return externalIdFactory.createYoctoExternalId("layer", layerName, "0.0");
    }
}
