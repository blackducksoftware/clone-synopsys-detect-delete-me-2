package com.synopsys.integration.detect.workflow.bdio;

import java.io.File;

import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.DependencyGraphUtil;
import com.synopsys.integration.bdio.graph.ProjectDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.ProjectDependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detect.workflow.codelocation.CodeLocationNameManager;
import com.synopsys.integration.util.IntegrationEscapeUtil;
import com.synopsys.integration.util.NameVersion;

public class CreateAggregateCodeLocationOperation {
    private final ExternalIdFactory externalIdFactory;
    private final CodeLocationNameManager codeLocationNameManager;

    public CreateAggregateCodeLocationOperation(ExternalIdFactory externalIdFactory, CodeLocationNameManager codeLocationNameManager) {
        this.externalIdFactory = externalIdFactory;
        this.codeLocationNameManager = codeLocationNameManager;
    }

    public AggregateCodeLocation createAggregateCodeLocation(
        File bdioOutputDirectory,
        DependencyGraph aggregateDependencyGraph,
        NameVersion projectNameVersion,
        String aggregateName,
        String extension
    ) {
        ExternalId projectExternalId = externalIdFactory.createNameVersionExternalId(new Forge("/", "DETECT"), projectNameVersion.getName(), projectNameVersion.getVersion());
        String codeLocationName = codeLocationNameManager.createAggregateCodeLocationName(projectNameVersion);

        String fileName = new IntegrationEscapeUtil().replaceWithUnderscore(aggregateName) + extension;
        File aggregateBdioFile = new File(bdioOutputDirectory, fileName);

        // TODO: Stop-gap measure to avoid changes propagating. Shouldn't be a problem in 8.0.0 JM-04/2022
        ProjectDependencyGraph projectDependencyGraph = new ProjectDependencyGraph(new ProjectDependency(projectExternalId));
        DependencyGraphUtil.copyRootDependencies(projectDependencyGraph, aggregateDependencyGraph);

        return new AggregateCodeLocation(aggregateBdioFile, codeLocationName, projectNameVersion, projectExternalId, projectDependencyGraph);
    }
}
