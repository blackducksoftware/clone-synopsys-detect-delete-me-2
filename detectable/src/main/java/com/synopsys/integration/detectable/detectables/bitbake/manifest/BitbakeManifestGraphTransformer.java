package com.synopsys.integration.detectable.detectables.bitbake.manifest;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeNode;
import com.synopsys.integration.detectable.detectables.bitbake.manifest.parse.ShowRecipesResults;

public class BitbakeManifestGraphTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public DependencyGraph generateGraph(Map<String, String> imageRecipes, ShowRecipesResults showRecipesResult, BitbakeGraph bitbakeGraph) {

        for (Map.Entry<String, String> imageRecipeEntry : imageRecipes.entrySet()) {
            logger.info("\tImage recipe: {}:{}", imageRecipeEntry.getKey(), imageRecipeEntry.getValue());
            if (showRecipesResult.getRecipes().containsKey(imageRecipeEntry.getKey())) {
                logger.info("\t\tLayers: {}", showRecipesResult.getRecipes().get(imageRecipeEntry.getKey()).getLayerNames());
            } else {
                logger.info("\t\tLayers: unknown");
            }
            for (BitbakeNode candidateNode : bitbakeGraph.getNodes()) {
                if (imageRecipeEntry.getKey().equals(candidateNode.getName())) {
                    logger.info("\t\tFound recipe in task-depends.dot data with children: {}", candidateNode.getChildren());
                }
            }
        }
        return null;
    }

    private void oldBad(Map<String, String> imageRecipes, ShowRecipesResults showRecipesResult, BitbakeGraph bitbakeGraph) {
        for (String layerName : showRecipesResult.getLayerNames()) {
            logger.info("*** layer: {}", layerName);
            // TODO pretty inefficient to check all image recipes every time
            // TODO and what about direct dependencies on layer 2 that depend on a recipe from layer 1 ???
            for (Map.Entry<String, String> imageRecipeEntry : imageRecipes.entrySet()) {
                logger.info("\tcandidate recipe: {}:{}", imageRecipeEntry.getKey(), imageRecipeEntry.getValue());
                if (showRecipesResult.getRecipes().containsKey(imageRecipeEntry.getKey())) {
                    logger.info("\t\tRecipe {} is an image direct dependency according to license.manifest", imageRecipeEntry.getKey());
                    if (showRecipesResult.getRecipes().get(imageRecipeEntry.getKey()).getLayerNames().contains(layerName)) {
                        logger.info("\t\t\t*** Recipe {} was found on layer {}", imageRecipeEntry.getKey(), layerName);
                    }
                }
            }
        }
    }
}
