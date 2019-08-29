package com.synopsys.integration.detectable.detectable.codelocation;

import java.util.ArrayList;
import java.util.List;

public class NameVersionCodeLocationId implements CodeLocationId {
    private final String name;
    private final String version;

    public NameVersionCodeLocationId(final String name, final String version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public List<String> getIdPieces() {
        final List<String> idPieces = new ArrayList<>();
        idPieces.add(name);
        idPieces.add(version);
        return idPieces;
    }
}
