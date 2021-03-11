/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.lifecycle.run.operation.blackduck;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.bdio2upload.Bdio2UploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadBatchOutput;
import com.synopsys.integration.blackduck.codelocation.bdioupload.UploadTarget;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.workflow.OperationException;
import com.synopsys.integration.detect.workflow.OperationResult;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.DetectBdioUploadService;

public class BdioUploadOperation {
    private static final String OPERATION_NAME = "BLACK_DUCK_BDIO_UPLOAD";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OperationResult<CodeLocationCreationData<UploadBatchOutput>> execute(BlackDuckRunData blackDuckRunData, BdioResult bdioResult) throws OperationException {
        OperationResult<CodeLocationCreationData<UploadBatchOutput>> result = OperationResult.success(OPERATION_NAME);
        try {
            List<UploadTarget> uploadTargetList = bdioResult.getUploadTargets();
            if (!uploadTargetList.isEmpty()) {
                logger.info(String.format("Created %d BDIO files.", bdioResult.getUploadTargets().size()));
                if (blackDuckRunData.isOnline()) {
                    logger.debug("Uploading BDIO files.");
                    BlackDuckServicesFactory blackDuckServicesFactory = blackDuckRunData.getBlackDuckServicesFactory();
                    BdioUploadService bdioUploadService = blackDuckServicesFactory.createBdioUploadService();
                    Bdio2UploadService bdio2UploadService = blackDuckServicesFactory.createBdio2UploadService();
                    DetectBdioUploadService detectBdioUploadService = new DetectBdioUploadService();
                    logger.info(String.format("Created %d BDIO files.", uploadTargetList.size()));
                    logger.debug("Uploading BDIO files.");
                    CodeLocationCreationData<UploadBatchOutput> codeLocationCreationData = detectBdioUploadService.uploadBdioFiles(bdioResult, bdioUploadService,
                        bdio2UploadService);
                    result = OperationResult.success(OPERATION_NAME, codeLocationCreationData);
                }
            } else {
                logger.debug("Did not create any BDIO files.");
            }
        } catch (Exception ex) {
            result = OperationResult.fail(OPERATION_NAME);
            throw new OperationException("Error occurred uploading BDIO files.", ex, result);
        }
        return result;
    }
}
