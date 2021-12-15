package com.synopsys.integration.detectable.detectables.bitbake.manifest.model;

import java.util.HashSet;
import java.util.Set;

public abstract class LayeredNode {
    private final String name;
    private String version = null;
    private final Set<LayeredNode> children = new HashSet<>();

    public LayeredNode(final String name) {
        this.name = name;
    }

    public void addChild(LayeredNode child) {
        children.add(child);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof LayeredNode)) {
            return false;
        }
        LayeredNode other = (LayeredNode) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equalsIgnoreCase(other.name)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equalsIgnoreCase(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.toLowerCase().hashCode());
        result = prime * result + ((version == null) ? 0 : version.toLowerCase().hashCode());
        return result;
    }
}
