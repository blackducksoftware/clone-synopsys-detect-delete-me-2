package com.synopsys.integration.detectable.detectables.docker;

import java.util.Arrays;
import java.util.List;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocationId;

public class DockerCodeLocationIdContainer implements CodeLocationId {
    private final ExternalId pathExternalId;

    public DockerCodeLocationIdContainer(final ExternalId pathExternalId) {
        this.pathExternalId = pathExternalId;
    }

    @Override
    public List<String> getIdPieces() {
        return Arrays.asList(pathExternalId.getExternalIdPieces());
    }
}
