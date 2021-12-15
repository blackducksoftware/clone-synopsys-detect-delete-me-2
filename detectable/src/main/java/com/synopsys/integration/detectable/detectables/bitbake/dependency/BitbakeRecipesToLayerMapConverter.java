package com.synopsys.integration.detectable.detectables.bitbake.dependency;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.synopsys.integration.detectable.detectables.bitbake.dependency.model.BitbakeRecipe;

public class BitbakeRecipesToLayerMapConverter {
    public Map<String, String> convert(List<BitbakeRecipe> bitbakeRecipes) {
        Map<String, String> recipeNameToLayersMap = new HashMap<>();

        for (BitbakeRecipe bitbakeRecipe : bitbakeRecipes) {
            String key = bitbakeRecipe.getName();
            bitbakeRecipe.getLayerNames().stream().findFirst().ifPresent(layer -> recipeNameToLayersMap.put(key, layer));
        }

        return recipeNameToLayersMap;
    }
}
