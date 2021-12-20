package com.synopsys.integration.detectable.detectables.bitbake.common.model;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BitbakeRecipe {
    private final String name;
    private final Collection<String> layerNames;
    private String primaryLayer = null;

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
        if (primaryLayer == null) {
            primaryLayer = layer;
        }
    }

    public Optional<String> getPrimaryLayer() {
        return Optional.ofNullable(primaryLayer);
    }
}
