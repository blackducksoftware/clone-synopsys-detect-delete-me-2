package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeNode;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeRecipe;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.ShowRecipesResults;
import com.synopsys.integration.exception.IntegrationException;

public class BitbakeManifestGraphTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExternalIdFactory externalIdFactory;

    public BitbakeManifestGraphTransformer(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public DependencyGraph generateGraph(Map<String, String> imageRecipes, ShowRecipesResults showRecipesResult, BitbakeGraph bitbakeGraphFromTaskDepends) {
        MutableDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        Map<String, Dependency> namesToExternalIds = new HashMap<>();

        // TODO: Feels like this should be done before this method is called
        Map<String, BitbakeNode> recipeVersionLookup = toMap(bitbakeGraphFromTaskDepends);

        // TODO: It takes less than a second per layer loop, but still: It'd be nice to have
        // in advance a list of recipes per layer, assuming that's easy

        for (String currentLayerName : showRecipesResult.getLayerNames()) {
            logger.info("*** layer: {}", currentLayerName);
            ExternalId layerExternalId = generateLayerExternalId(currentLayerName);
            logger.info("*** layerExternalId for layer: {}: {}", currentLayerName, layerExternalId.toString());

            for (Map.Entry<String, String> candidateImageRecipeEntry : imageRecipes.entrySet()) {
                String candidateImageRecipeName = candidateImageRecipeEntry.getKey();
                String candidateImageRecipeVersion = candidateImageRecipeEntry.getValue();

                // TODO this may be unnecessary
                if (StringUtils.isBlank(candidateImageRecipeVersion)) {
                    logger.warn("*** NO VERSION for recipe {}", candidateImageRecipeName);
                    continue;
                }

                //logger.info("\tImage recipe: {}:{}", candidateImageRecipeName, candidateImageRecipeVersion);
                if (showRecipesResult.getRecipes().containsKey(candidateImageRecipeName)) {
                    Collection<String> candidateImageRecipeLayers = showRecipesResult.getRecipes().get(candidateImageRecipeName).getLayerNames();
                    //logger.info("\t\tLayers: {}", showRecipesResult.getRecipes().get(candidateImageRecipeName).getLayerNames());
                    if (candidateImageRecipeLayers.contains(currentLayerName)) {
                        logger.info("Recipe {} is associated with layer {}", candidateImageRecipeName, currentLayerName);
                    } else {
                        //logger.info("Recipe {} is not associated with layer {}", candidateImageRecipeName, currentLayerName);
                        continue;
                    }
                } else {
                    logger.warn("No layer list found for recipe {}", candidateImageRecipeName);
                    continue;
                }

                //ExternalId externalId = externalIdFactory.createYoctoExternalId(currentLayerName, candidateImageRecipeName, candidateImageRecipeVersion);
                ExternalId imageRecipeExternalId = generateRecipeExternalId(currentLayerName, candidateImageRecipeName, candidateImageRecipeVersion);
                logger.info("*** externalId for recipe: {}:{}:{}: {}", currentLayerName, candidateImageRecipeName, candidateImageRecipeVersion, imageRecipeExternalId.toString());

                // TODO pretty inefficient to search through all nodes every time. Why isn't this a map?

                for (BitbakeNode candidateNodeFromTaskDepends : bitbakeGraphFromTaskDepends.getNodes()) {
                    if (candidateImageRecipeName.equals(candidateNodeFromTaskDepends.getName())) {
                        for (String child : candidateNodeFromTaskDepends.getChildren()) {
                            BitbakeRecipe childRecipe = showRecipesResult.getRecipes().get(child);
                            if (childRecipe != null) {
                                if (childRecipe.getPrimaryLayer().isPresent()) {
                                    String childPrimaryLayer = childRecipe.getPrimaryLayer().get();
                                    BitbakeNode childRecipeNode = recipeVersionLookup.get(childRecipe.getName());
                                    if (childRecipeNode == null) {
                                        // TODO this should never happen
                                        logger.warn("Missing node for recipe {}", childRecipe.getName());
                                        continue;
                                    }
                                    String childRecipeVersion = childRecipeNode.getVersion().orElse(null);
                                    if (childRecipeVersion == null) {
                                        logger.warn("Missing version for recipe {}", childRecipe.getName());
                                        continue;
                                    }
                                    //ExternalId childExternalId = externalIdFactory.createYoctoExternalId(childPrimaryLayer, childRecipe.getName(), childRecipeVersion);
                                    ExternalId childExternalId = generateRecipeExternalId(childPrimaryLayer, childRecipe.getName(), childRecipeVersion);
                                    logger.info("*** childExternalId for child recipe: {}:{}:{}: {}", childPrimaryLayer, childRecipe.getName(), childRecipeVersion, childExternalId.toString());
                                } else {
                                    logger.warn("Don't have a primary layer for {}", childRecipe.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
        return dependencyGraph;
    }

    // TODO this feels like we could build this map while generating the BitbakeGraph
    private Map<String, BitbakeNode> toMap(BitbakeGraph bitbakeGraph) {
        Map<String, BitbakeNode> byNameMap = new HashMap<>();
        for (BitbakeNode node : bitbakeGraph.getNodes()) {
            byNameMap.put(node.getName(), node);
        }
        return byNameMap;
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
