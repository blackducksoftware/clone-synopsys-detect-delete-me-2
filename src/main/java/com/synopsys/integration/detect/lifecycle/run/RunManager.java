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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectConfigurationFactory;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.DetectContext;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.lifecycle.run.runnables.BazelToolRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.BlackDuckRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.DetectRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.DetectorToolRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.DockerToolRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.PolarisRunnable;
import com.synopsys.integration.detect.lifecycle.run.runnables.RunnableState;
import com.synopsys.integration.detect.lifecycle.run.runnables.UniversalProjectToolsRunnable;
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
import com.synopsys.integration.detect.workflow.report.util.ReportConstants;
import com.synopsys.integration.detectable.detectable.inspector.nuget.NugetInspectorResolver;
import com.synopsys.integration.exception.IntegrationException;

public class RunManager {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DetectContext detectContext;

    public RunManager(DetectContext detectContext) {
        this.detectContext = detectContext;
    }

    public RunResult run(ProductRunData productRunData) throws DetectUserFriendlyException, IntegrationException {
        //TODO: Better way for run manager to get dependencies so he can be tested. (And better ways of creating his objects)
        DetectConfigurationFactory detectConfigurationFactory = detectContext.getBean(DetectConfigurationFactory.class);

        RunResult runResult = new RunResult();
        RunOptions runOptions = detectConfigurationFactory.createRunOptions();
        List<DetectRunnable> runnables = createRunnables(productRunData);

        RunnableState runState = RunnableState.success(runResult, runOptions, null);
        logger.info(ReportConstants.RUN_SEPARATOR);
        for (DetectRunnable runnable : runnables) {
            runState = runnable.run(runState);
        }

        logger.info("All tools have finished.");
        logger.info(ReportConstants.RUN_SEPARATOR);

        return runState.getCurrentRunResult();
    }

    private List<DetectRunnable> createRunnables(ProductRunData productRunData) {
        PropertyConfiguration detectConfiguration = detectContext.getBean(PropertyConfiguration.class);
        DetectConfigurationFactory detectConfigurationFactory = detectContext.getBean(DetectConfigurationFactory.class);
        DirectoryManager directoryManager = detectContext.getBean(DirectoryManager.class);
        EventSystem eventSystem = detectContext.getBean(EventSystem.class);
        CodeLocationNameGenerator codeLocationNameService = detectContext.getBean(CodeLocationNameGenerator.class);
        CodeLocationNameManager codeLocationNameManager = detectContext.getBean(CodeLocationNameManager.class, codeLocationNameService);
        BdioCodeLocationCreator bdioCodeLocationCreator = detectContext.getBean(BdioCodeLocationCreator.class);
        DetectInfo detectInfo = detectContext.getBean(DetectInfo.class);
        NugetInspectorResolver nugetInspectorResolver = detectContext.getBean(NugetInspectorResolver.class);
        DetectDetectableFactory detectDetectableFactory = detectContext.getBean(DetectDetectableFactory.class, nugetInspectorResolver);
        ExtractionEnvironmentProvider extractionEnvironmentProvider = new ExtractionEnvironmentProvider(directoryManager);
        CodeLocationConverter codeLocationConverter = new CodeLocationConverter(new ExternalIdFactory());
        ImpactAnalysisOptions impactAnalysisOptions = detectConfigurationFactory.createImpactAnalysisOptions();
        RunOptions runOptions = detectConfigurationFactory.createRunOptions();
        DetectToolFilter detectToolFilter = runOptions.getDetectToolFilter();

        PolarisRunnable polarisRunnable = new PolarisRunnable(productRunData, detectConfiguration, directoryManager, eventSystem, detectToolFilter);

        DockerToolRunnable dockerToolRunnable = new DockerToolRunnable(directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider, codeLocationConverter);
        BazelToolRunnable bazelToolRunnable = new BazelToolRunnable(directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider, codeLocationConverter);
        DetectorToolRunnable detectorToolRunnable = new DetectorToolRunnable(detectConfiguration, detectConfigurationFactory, directoryManager, eventSystem, detectDetectableFactory, detectToolFilter, extractionEnvironmentProvider,
            codeLocationConverter);
        UniversalProjectToolsRunnable universalProjectToolsRunnable = new UniversalProjectToolsRunnable(detectConfigurationFactory, directoryManager, eventSystem, dockerToolRunnable, bazelToolRunnable, detectorToolRunnable);

        BlackDuckRunnable blackDuckRunnable = new BlackDuckRunnable(detectContext, productRunData, detectConfigurationFactory, directoryManager, eventSystem, codeLocationNameManager, bdioCodeLocationCreator, detectInfo, detectToolFilter,
            impactAnalysisOptions);

        List<DetectRunnable> runnables = new ArrayList<>();
        // define the order of the runnables. Polaris, projectTools i.e. detectors, BlackDuck
        runnables.add(polarisRunnable);
        // this will set the projectNameVersion in the RunnableState object for BlackDuckRunnable to use.  It must execute before BlackDuck.
        runnables.add(universalProjectToolsRunnable);
        runnables.add(blackDuckRunnable);

        return runnables;
    }
}
