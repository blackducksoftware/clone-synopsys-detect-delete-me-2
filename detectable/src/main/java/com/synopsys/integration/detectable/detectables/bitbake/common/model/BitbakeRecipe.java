package com.synopsys.integration.detectable.detectables.bitbake.common.model;

import java.util.Collection;
import java.util.List;

public class BitbakeRecipe {
    private final String name;
    private final Collection<String> layerNames;

    public BitbakeRecipe(String name, Collection<String> layerNames) {
        this.name = name;
        this.layerNames = layerNames;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getLayerNames() {
        return layerNames;
    }

    public void addLayerName(String layer) {
        layerNames.add(layer);
    }
}
