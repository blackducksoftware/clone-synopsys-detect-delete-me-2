/**
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.lifecycle.run;

import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.run.steps.BazelToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.BlackDuckRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DetectRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DetectorToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.DockerToolRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.PolarisRunStep;
import com.synopsys.integration.detect.lifecycle.run.steps.ProjectInfoRunStep;
import com.synopsys.integration.detect.tool.detector.CodeLocationConverter;
import com.synopsys.integration.detect.tool.detector.DetectDetectableFactory;
import com.synopsys.integration.detect.tool.detector.extraction.ExtractionEnvironmentProvider;
import com.synopsys.integration.detect.tool.impactanalysis.ImpactAnalysisOptions;
import com.synopsys.integration.detect.util.filter.DetectToolFilter;
import com.synopsys.integration.detect.workflow.codelocation.BdioCodeLocationCreator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameGenerator;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorResolver;

public class RunContext {
    private DetectContext detectContext;
    private ProductRunData productRunData;
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

    public RunContext(DetectContext detectContext, ProductRunData productRunData) {
        this.detectContext = detectContext;
        this.productRunData = productRunData;
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
        extractionEnvironmentProvider = new ExtractionEnvironmentProvider(directoryManager);
        codeLocationConverter = new CodeLocationConverter(new ExternalIdFactory());
    }

    private PolarisRunStep createPolarisRunnable(DetectToolFilter detectToolFilter) {
        return new PolarisRunStep(productRunData, detectConfiguration, directoryManager, eventSystem, detectToolFilter);
    }

    private DockerToolRunStep createDockerToolRunnable(DetectToolFilter detectToolFilter) {
        return new DockerToolRunStep(directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider, codeLocationConverter);
    }

    private BazelToolRunStep createBazelToolRunnable(DetectToolFilter detectToolFilter) {
        return new BazelToolRunStep(directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider, codeLocationConverter);
    }

    private DetectorToolRunStep createDetectorToolRunnable(DetectToolFilter detectToolFilter) {
        return new DetectorToolRunStep(detectConfiguration, detectConfigurationFactory, directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider,
            codeLocationConverter);
    }

    private ProjectInfoRunStep createProjectInfoRunnable() {
        return new ProjectInfoRunStep(detectConfigurationFactory, directoryManager, eventSystem);
    }

    private BlackDuckRunStep createBlackDuckRunnable(DetectToolFilter detectToolFilter) {
        ImpactAnalysisOptions impactAnalysisOptions = detectConfigurationFactory.createImpactAnalysisOptions();
        return new BlackDuckRunStep(detectContext, productRunData, detectConfigurationFactory, directoryManager, eventSystem, codeLocationNameManager, bdioCodeLocationCreator, detectInfo, detectToolFilter,
            impactAnalysisOptions);
    }

    public List<DetectRunStep> createRunSequence() {
        RunOptions runOptions = detectConfigurationFactory.createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

        PolarisRunStep polarisRunnable = createPolarisRunnable(detectToolFilter);
        DockerToolRunStep dockerToolRunnable = createDockerToolRunnable(detectToolFilter);
        BazelToolRunStep bazelToolRunnable = createBazelToolRunnable(detectToolFilter);
        DetectorToolRunStep detectorToolRunnable = createDetectorToolRunnable(detectToolFilter);
        BlackDuckRunStep blackDuckRunnable = createBlackDuckRunnable(detectToolFilter);

        List<DetectRunStep> runnables = new LinkedList<>();
        // define the order of the runnables. Polaris, projectTools i.e. detectors, BlackDuck
        runnables.add(polarisRunnable);
        runnables.add(dockerToolRunnable);
        runnables.add(bazelToolRunnable);
        runnables.add(detectorToolRunnable);
        // this will set the projectNameVersion in the RunnableState object for BlackDuckRunnable to use.  It must execute before BlackDuck.
        runnables.add(createProjectInfoRunnable());
        runnables.add(blackDuckRunnable);

        return runnables;
    }
}
