package com.synopsys.integration.detectable.detectable.codelocation;

import java.util.ArrayList;
import java.util.List;

public class GAVCodeLocationId extends NameVersionCodeLocationId {
    private final String group;

    public GAVCodeLocationId(final String group, final String name, final String version) {
        super(name, version);
        this.group = group;
    }

    @Override
    public List<String> getIdPieces() {
        final List<String> idPieces = new ArrayList<>();
        idPieces.add(group);
        idPieces.addAll(super.getIdPieces());
        return idPieces;
    }
}
