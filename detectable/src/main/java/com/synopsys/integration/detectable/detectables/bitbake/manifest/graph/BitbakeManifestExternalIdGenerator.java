package com.synopsys.integration.detectable.detectables.bitbake.manifest.graph;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;

public class BitbakeManifestExternalIdGenerator {
    private final ExternalIdFactory externalIdFactory;

    public BitbakeManifestExternalIdGenerator(final ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public ExternalId generateRecipeExternalId(String layerName, String recipeName, @NotNull String recipeVersion) {
        if (recipeVersion.contains("AUTOINC")) {
            recipeVersion = recipeVersion.replaceFirst("AUTOINC\\+[\\w|\\d]*", "X");
        }
        ExternalId externalId = externalIdFactory.createYoctoExternalId(layerName, recipeName, recipeVersion);
        return externalId;
    }

    public ExternalId generateLayerExternalId(String layerName) {
        return externalIdFactory.createYoctoExternalId("layer", layerName, "0.0");
    }
}
