package com.synopsys.integration.detectable.detectables.bitbake.manifest.model;

import java.util.HashSet;
import java.util.Set;

public class LayeredGraph {
    private final Set<LayeredNode> rootNodes = new HashSet<>();

    public void addRootNode(LayeredNode node) {
        rootNodes.add(node);
    }

}
