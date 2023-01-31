package com.synopsys.integration.detect.lifecycle.run.step;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.generated.view.DeveloperScansScanView;
import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.binaryscanner.BinaryScanBatchOutput;
import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScanCommandOutput;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.detect.configuration.enumeration.BlackduckScanMode;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.OperationException;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.DockerTargetData;
import com.synopsys.integration.detect.lifecycle.run.operation.OperationRunner;
import com.synopsys.integration.detect.lifecycle.run.step.utility.StepHelper;
import com.synopsys.integration.detect.tool.signaturescanner.operation.SignatureScanOuputResult;
import com.synopsys.integration.detect.tool.signaturescanner.operation.SignatureScanRapidResult;
import com.synopsys.integration.detect.workflow.bdba.BdbaStatusScanView;
import com.synopsys.integration.detect.workflow.bdio.BdioResult;
import com.synopsys.integration.detect.workflow.blackduck.developer.RapidModeWaitOperation;
import com.synopsys.integration.detect.workflow.blackduck.developer.aggregate.RapidScanResultSummary;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class RapidModeStepRunner {
    private final OperationRunner operationRunner;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final StepHelper stepHelper;
    private final Gson gson;
    private final DirectoryManager directoryManager;
    private RapidModeWaitOperation rapidModeWaitOperation;

    public RapidModeStepRunner(OperationRunner operationRunner, StepHelper stepHelper, Gson gson, DirectoryManager directoryManager) {
        this.operationRunner = operationRunner;
        this.stepHelper = stepHelper;
        this.gson = gson;
        this.directoryManager = directoryManager;
    }

    public void runOnline(BlackDuckRunData blackDuckRunData, NameVersion projectVersion, BdioResult bdioResult,
            DockerTargetData dockerTargetData, Boolean scaEnvironment) throws OperationException {
        operationRunner.phoneHome(blackDuckRunData);
        Optional<File> rapidScanConfig = operationRunner.findRapidScanConfig();
        String scanMode = blackDuckRunData.getScanMode().displayName();
        rapidScanConfig.ifPresent(config -> logger.info("Found " + scanMode.toLowerCase() + " scan config file: {}", config));

        String blackDuckUrl = blackDuckRunData.getBlackDuckServerConfig().getBlackDuckUrl().toString();
        List<HttpUrl> parsedUrls = new ArrayList<>();
        
        List<HttpUrl> uploadResultsUrls = operationRunner.performRapidUpload(blackDuckRunData, bdioResult, rapidScanConfig.orElse(null));
        
        if (uploadResultsUrls != null && uploadResultsUrls.size() > 0) {
            parsedUrls.addAll(uploadResultsUrls);
        }

        stepHelper.runToolIfIncluded(DetectTool.SIGNATURE_SCAN, "Signature Scanner", () -> {
            logger.debug("Rapid scan signature scan detected.");

            SignatureScanStepRunner signatureScanStepRunner = new SignatureScanStepRunner(operationRunner);
            SignatureScanOuputResult signatureScanOutputResult = signatureScanStepRunner
                    .runRapidSignatureScannerOnline(blackDuckRunData, projectVersion, dockerTargetData);

            parsedUrls.addAll(parseScanUrls(scanMode, signatureScanOutputResult, blackDuckUrl));
        });
        
        stepHelper.runToolIfIncluded(DetectTool.BINARY_SCAN, "Binary Scanner", () -> {
            logger.debug("Rapid binary scan detected.");
            
            // TODO check SCA
            
            // Generate the UUID we use to communicate with BDBA
            UUID bdbaScanId = UUID.randomUUID();
            
            RapidBinaryScanStepRunner rapidBinaryScanStepRunner = new RapidBinaryScanStepRunner(gson, bdbaScanId);
            Response response = rapidBinaryScanStepRunner.submitScan();
            BdbaStatusScanView results = rapidBinaryScanStepRunner.pollForResults();
            
            // Download the BDIO file from BDBA and extract it
            rapidBinaryScanStepRunner.downloadAndExtractBdio(directoryManager, projectVersion);
            
            // TODO Get scanId from BlackDuck, need to send a Start along with the BDIO header we get
            // from BDBA
            UUID bdScanId = operationRunner.initiateRapidBinaryScan(blackDuckRunData, blackDuckUrl);
            
            // TODO Send BDIO chunks to BlackDuck
            rapidBinaryScanStepRunner.submitBdioChunk(blackDuckRunData);
            
            // TODO send finish call to BlackDuck to let it know the BDIO is fully submitted
            
            // TODO add this scan to the URLs to wait for
            //d he?parsedUrls.add(new HttpUrl(blackDuckUrl + "/api/developer-scans/" + bdScanId.toString()));
        });

        // Get info about any scans that were done
        BlackduckScanMode mode = blackDuckRunData.getScanMode();
        List<DeveloperScansScanView> rapidResults = operationRunner.waitForRapidResults(blackDuckRunData, parsedUrls, mode);

        // Generate a report, even an empty one if no scans were done as that is what previous detect versions did.
        File jsonFile = operationRunner.generateRapidJsonFile(projectVersion, rapidResults);
        RapidScanResultSummary summary = operationRunner.logRapidReport(rapidResults, mode);
        operationRunner.publishRapidResults(jsonFile, summary, mode);
    }

    /**
     * The signature scanner only returns a high level success or failure to us. Details are in the
     * output directory's scanOutput.json. We need to crack that open to get the scanId so we can poll
     * for the true results from BlackDuck later.
     * 
     * @return a list of URLs that BlackDuck should poll for rapid signature scan results.
     */
    private List<HttpUrl> parseScanUrls(String scanMode, SignatureScanOuputResult signatureScanOutputResult, String blackDuckUrl) throws IOException, IntegrationException {
        List<ScanCommandOutput> outputs = signatureScanOutputResult.getScanBatchOutput().getOutputs();
        List<HttpUrl> parsedUrls = new ArrayList<>(outputs.size());
        
        for (ScanCommandOutput output : outputs) {
            try {
                File specificRunOutputDirectory = output.getSpecificRunOutputDirectory();
                String scanOutputLocation = specificRunOutputDirectory.toString() + "/output/scanOutput.json";
                Reader reader = Files.newBufferedReader(Paths.get(scanOutputLocation));

                SignatureScanRapidResult result = gson.fromJson(reader, SignatureScanRapidResult.class);

                HttpUrl url = new HttpUrl(blackDuckUrl + "/api/developer-scans/" + result.scanId);

                logger.info(scanMode + " mode signature scan URL: {}", url);
                parsedUrls.add(url);
            } catch (Exception e) {
                throw new IntegrationException("Unable to parse rapid signature scan results.");
            }
        }
        
        return parsedUrls;
    }
}
