package com.synopsys.integration.detectable.detectables.bitbake.manifest.model;

import java.util.HashSet;
import java.util.Set;

public class RecipeNode extends LayeredNode {
    private Set<String> layers = new HashSet<>();

    public RecipeNode(final String name) {
        super(name);
    }

    public void addLayer(String layer) {
        layers.add(layer);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }
}
