package com.synopsys.integration.detect.lifecycle.run;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.blackduck.bdio2.Bdio2Factory;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectMappingService;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.configuration.connection.ConnectionFactory;
import com.synopsys.integration.detect.configuration.enumeration.DetectTool;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.tool.DetectableTool;
import com.synopsys.integration.detect.tool.binaryscanner.BinaryScanOptions;
import com.synopsys.integration.detect.tool.binaryscanner.BlackDuckBinaryScannerTool;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.DetectorIssuePublisher;
import com.synopsys.integration.detect.tool.detector.DetectorRuleFactory;
import com.synopsys.integration.detect.tool.detector.DetectorTool;
import com.synopsys.integration.detect.tool.detector.executable.DetectExecutableRunner;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.tool.impactanalysis.BlackDuckImpactAnalysisTool;
import com.synopsys.integration.detect.tool.polaris.PolarisTool;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerOptions;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerTool;
import com.synopsys.integration.detect.workflow.bdio.BdioManager;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;
import com.synopsys.integration.detect.workflow.blackduck.BlackDuckPostActions;
import com.synopsys.integration.detect.workflow.blackduck.DetectCustomFieldService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectService;
import com.synopsys.integration.detect.workflow.blackduck.DetectProjectServiceOptions;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameGenerator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionDecider;
import com.synopsys.integration.detect.workflow.project.ProjectNameVersionOptions;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.file.WildcardFileFinder;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorResolver;
import com.synopsys.integration.detector.base.DetectorType;
import com.synopsys.integration.detector.evaluation.DetectorEvaluationOptions;
import com.synopsys.integration.detector.finder.DetectorFinder;
import com.synopsys.integration.detector.finder.DetectorFinderOptions;
import com.synopsys.integration.detector.rule.DetectorRuleSet;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class RunFactory {
    private ConnectionFactory connectionFactory;
    private final DetectContext detectContext;
    private PropertyConfiguration detectConfiguration;
    private DetectConfigurationFactory detectConfigurationFactory;
    private DirectoryManager directoryManager;
    private EventSystem eventSystem;
    private CodeLocationNameGenerator codeLocationNameService;
    private CodeLocationNameManager codeLocationNameManager;
    private BdioCodeLocationCreator bdioCodeLocationCreator;
    private DetectInfo detectInfo;
    private NugetInspectorResolver nugetInspectorResolver;
    private DetectDetectableFactory detectDetectableFactory;
    private ExtractionEnvironmentProvider extractionEnvironmentProvider;
    private CodeLocationConverter codeLocationConverter;

    public RunFactory(DetectContext detectContext) {
        // Beans are retrieved immediately so failures (missing beans) are detected early.
        this.detectContext = detectContext; //Unfortunate. Need to remove the dependency SigScanner has.
        detectConfiguration = detectContext.getBean(PropertyConfiguration.class);
        detectConfigurationFactory = detectContext.getBean(DetectConfigurationFactory.class);
        directoryManager = detectContext.getBean(DirectoryManager.class);
        eventSystem = detectContext.getBean(EventSystem.class);
        codeLocationNameService = detectContext.getBean(CodeLocationNameGenerator.class);
        codeLocationNameManager = detectContext.getBean(CodeLocationNameManager.class, codeLocationNameService);
        bdioCodeLocationCreator = detectContext.getBean(BdioCodeLocationCreator.class);
        detectInfo = detectContext.getBean(DetectInfo.class);
        nugetInspectorResolver = detectContext.getBean(NugetInspectorResolver.class);
        detectDetectableFactory = detectContext.getBean(DetectDetectableFactory.class, nugetInspectorResolver);
        connectionFactory = detectContext.getBean(ConnectionFactory.class);

        extractionEnvironmentProvider = new ExtractionEnvironmentProvider(directoryManager);
        codeLocationConverter = new CodeLocationConverter(new ExternalIdFactory());
    }

    public RunOptions createRunOptions() {
        return detectConfigurationFactory.createRunOptions();
    }

    public DetectableTool createDockerTool() {
        return new DetectableTool(detectDetectableFactory::createDockerDetectable,
            extractionEnvironmentProvider, codeLocationConverter, "DOCKER", DetectTool.DOCKER,
            eventSystem);
    }

    public DetectableTool createBazelTool() {

        return new DetectableTool(detectDetectableFactory::createBazelDetectable,
            extractionEnvironmentProvider, codeLocationConverter, "BAZEL", DetectTool.BAZEL,
            eventSystem);
    }

    public File getSourceDirectory() {
        return directoryManager.getSourceDirectory();
    }

    public DetectorTool createDetectorTool() {
        String projectBomTool = detectConfiguration.getValueOrEmpty(DetectProperties.DETECT_PROJECT_DETECTOR.getProperty()).orElse(null);
        List<DetectorType> requiredDetectors = detectConfiguration.getValueOrDefault(DetectProperties.DETECT_REQUIRED_DETECTOR_TYPES.getProperty());
        boolean buildless = detectConfiguration.getValueOrDefault(DetectProperties.DETECT_BUILDLESS.getProperty());

        DetectorRuleFactory detectorRuleFactory = new DetectorRuleFactory();
        DetectorRuleSet detectRuleSet = detectorRuleFactory.createRules(detectDetectableFactory, buildless);

        Path sourcePath = directoryManager.getSourceDirectory().toPath();
        DetectorFinderOptions finderOptions = detectConfigurationFactory.createSearchOptions(sourcePath);
        DetectorEvaluationOptions detectorEvaluationOptions = detectConfigurationFactory.createDetectorEvaluationOptions();

        DetectorIssuePublisher detectorIssuePublisher = new DetectorIssuePublisher();
        return new DetectorTool(new DetectorFinder(), extractionEnvironmentProvider, eventSystem, codeLocationConverter, detectorIssuePublisher, detectRuleSet, finderOptions, detectorEvaluationOptions,
            projectBomTool,
            requiredDetectors);
    }

    public DetectProjectService createDetectProjectService(BlackDuckServicesFactory blackDuckServicesFactory) throws DetectUserFriendlyException {
        DetectProjectServiceOptions options = detectConfigurationFactory.createDetectProjectServiceOptions();
        ProjectMappingService detectProjectMappingService = blackDuckServicesFactory.createProjectMappingService();
        DetectCustomFieldService detectCustomFieldService = new DetectCustomFieldService();
        return new DetectProjectService(blackDuckServicesFactory, options, detectProjectMappingService, detectCustomFieldService);
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public ProjectNameVersionDecider createNameVersionDecider() {
        ProjectNameVersionOptions projectNameVersionOptions = detectConfigurationFactory.createProjectNameVersionOptions(directoryManager.getSourceDirectory().getName());
        return new ProjectNameVersionDecider(projectNameVersionOptions);
    }

    public BdioManager createBdioManager() {
        BdioOptions bdioOptions = detectConfigurationFactory.createBdioOptions();
        return new BdioManager(detectInfo, bdioOptions, new SimpleBdioFactory(), new ExternalIdFactory(), new Bdio2Factory(), new IntegrationEscapeUtil(), codeLocationNameManager, bdioCodeLocationCreator, directoryManager);
    }

    public BlackDuckSignatureScannerTool createBlackDuckSignatureScanTool() throws DetectUserFriendlyException {
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = detectConfigurationFactory.createBlackDuckSignatureScannerOptions();
        return new BlackDuckSignatureScannerTool(blackDuckSignatureScannerOptions, detectConfigurationFactory, connectionFactory, directoryManager, codeLocationNameManager, detectContext);
    }

    public BlackDuckBinaryScannerTool createBinaryScannerTool(BlackDuckServicesFactory blackDuckServicesFactory) {
        BinaryScanOptions binaryScanOptions = detectConfigurationFactory.createBinaryScanOptions();
        return new BlackDuckBinaryScannerTool(eventSystem, codeLocationNameManager, directoryManager, new WildcardFileFinder(), binaryScanOptions, blackDuckServicesFactory);
    }

    public BlackDuckImpactAnalysisTool createBlackDuckImpactAnalysisTool(BlackDuckServicesFactory blackDuckServicesFactory) { //Questionable if this belongs here because it makes the online/offline distinction -jp
        if (null != blackDuckServicesFactory) {
            return BlackDuckImpactAnalysisTool.ONLINE(directoryManager, codeLocationNameManager, detectConfigurationFactory.createImpactAnalysisOptions(), blackDuckServicesFactory, eventSystem);
        } else {
            return BlackDuckImpactAnalysisTool.OFFLINE(directoryManager, codeLocationNameManager, detectConfigurationFactory.createImpactAnalysisOptions(), eventSystem);
        }
    }

    public BlackDuckPostActions createBlackDuckPostActions(BlackDuckServicesFactory blackDuckServicesFactory) {
        return new BlackDuckPostActions(blackDuckServicesFactory, eventSystem, detectConfigurationFactory.createBlackDuckPostOptions(), detectConfigurationFactory.findTimeoutInSeconds());
    }

    public PolarisTool createPolarisTool(PolarisServerConfig polarisServerConfig) {
        DetectableExecutableRunner polarisExecutableRunner = DetectExecutableRunner.newInfo(eventSystem);
        return new PolarisTool(eventSystem, directoryManager, polarisExecutableRunner, detectConfiguration, polarisServerConfig);
    }
}
