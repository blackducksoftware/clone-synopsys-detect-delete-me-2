package com.synopsys.integration.detect.tool.impactanalysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.integration.BlackDuckIntegrationTest;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameGenerator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.util.NoThreadExecutorService;

public class ImpactAnalysisTest extends BlackDuckIntegrationTest {
    private CodeLocationNameGenerator codeLocationNameGenerator = new CodeLocationNameGenerator(null);
    private CodeLocationNameManager codeLocationNameManager = new CodeLocationNameManager(codeLocationNameGenerator);

    @Test
    public void testImpactAnalysisForDetect() throws IOException, IntegrationException {
        File toScan = new File("./");
        Path outputDirectory = new File("/Users/ekerwin/working/impactanalysis").toPath();
        NameVersion projectNameVersion = new NameVersion("roger", "wilco");
        ProjectVersionWrapper projectAndVersion = projectService.syncProjectAndVersion(ProjectSyncModel.createWithDefaults(projectNameVersion));

        ImpactAnalysisOptions impactAnalysisOptions = new ImpactAnalysisOptions("prefix", "suffix", outputDirectory);
        ImpactAnalysisNamingOperation impactAnalysisNamingOperation = new ImpactAnalysisNamingOperation(codeLocationNameManager);
        String impactAnalysisCodeLocationName = impactAnalysisNamingOperation.createCodeLocationName(toScan, projectNameVersion, impactAnalysisOptions);

        GenerateImpactAnalysisOperation generateImpactAnalysisOperation = new GenerateImpactAnalysisOperation();
        Path impactAnalysisFile = generateImpactAnalysisOperation.generateImpactAnalysis(toScan, impactAnalysisCodeLocationName, outputDirectory);

        ImpactAnalysisBatchRunner impactAnalysisBatchRunner = new ImpactAnalysisBatchRunner(logger, blackDuckApiClient, apiDiscovery, new NoThreadExecutorService(), gson);
        ImpactAnalysisUploadService impactAnalysisUploadService = new ImpactAnalysisUploadService(impactAnalysisBatchRunner, codeLocationCreationService);
        ImpactAnalysisUploadOperation impactAnalysisUploadOperation = new ImpactAnalysisUploadOperation(impactAnalysisUploadService);
        CodeLocationCreationData<ImpactAnalysisBatchOutput> creationData = impactAnalysisUploadOperation.uploadImpactAnalysis(impactAnalysisFile, projectNameVersion, impactAnalysisCodeLocationName);

        ImpactAnalysisMapCodeLocationsOperation mapCodeLocationsOperation = new ImpactAnalysisMapCodeLocationsOperation(blackDuckApiClient);
        mapCodeLocationsOperation.mapCodeLocations(impactAnalysisFile, creationData, projectAndVersion);

        blackDuckApiClient.delete(projectAndVersion.getProjectView());
    }

}
