package com.synopsys.integration.detectable.detectables.bitbake.manifest.parse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeRecipe;

public class ShowRecipesResults {
    private final Set<String> layerNames = new HashSet<>();
    Map<String, BitbakeRecipe> recipes = new HashMap<>();

    public void addLayer(String layerName) {
        layerNames.add(layerName);
    }

    public void addRecipe(BitbakeRecipe recipe) {
        recipes.put(recipe.getName(), recipe);
    }

    public Set<String> getLayerNames() {
        return layerNames;
    }

    public Map<String, BitbakeRecipe> getRecipes() {
        return recipes;
    }
}
