package com.blackducksoftware.integration.hub.detect.project.result;

import org.slf4j.Logger;

import com.blackducksoftware.integration.hub.detect.model.BomToolType;

public class PreferredNotFoundUnchosenProjectInfoResult extends ProjectInfoResult {
    private final BomToolType bomToolType;

    public PreferredNotFoundUnchosenProjectInfoResult(final BomToolType bomToolType) {
        this.bomToolType = bomToolType;
    }

    @Override
    public void printDescription(final Logger logger) {
        logger.info("A bom tool of type " + bomToolType.toString() + " was not found. Project info could not be found in a bom tool.");
    }

}
