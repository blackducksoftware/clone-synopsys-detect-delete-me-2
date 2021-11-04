package com.synopsys.integration.detectable.detectables.pip.cli;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.util.NameVersion;

public class PipCliTransformer {
    private final ExternalIdFactory externalIdFactory;

    public PipCliTransformer(ExternalIdFactory externalIdFactory) {
        this.externalIdFactory = externalIdFactory;
    }

    public CodeLocation createCodeLocation(List<NameVersion> dependencies, @Nullable String projectName, @Nullable String projectVersionName) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();
        dependencies.stream()
            .map(nameVersion -> externalIdFactory.createNameVersionExternalId(Forge.PYPI, nameVersion.getName(), nameVersion.getVersion()))
            .map(Dependency::new)
            .forEach(graph::addChildToRoot);

        ExternalId projectExternalId = null;
        if (StringUtils.isNotBlank(projectName)) {
            projectExternalId = externalIdFactory.createNameVersionExternalId(Forge.PYPI, projectName, projectVersionName);
        }

        return new CodeLocation(graph, projectExternalId);
    }
    
}
