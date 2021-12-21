package com.synopsys.integration.detectable.detectables.bitbake.manifest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeGraph;
import com.synopsys.integration.detectable.detectables.bitbake.common.model.BitbakeNode;

public class BitbakeNodesByName {
    private Map<String, BitbakeNode> bitbakeNodesByName;

    public BitbakeNodesByName(BitbakeGraph bitbakeGraph) {
        bitbakeNodesByName = new HashMap<>(bitbakeGraph.getNodes().size());
        for (BitbakeNode node : bitbakeGraph.getNodes()) {
            bitbakeNodesByName.put(node.getName(), node);
        }
    }

    public Optional<BitbakeNode> get(String recipeName) {
        return Optional.ofNullable(bitbakeNodesByName.get(recipeName));
    }
}
