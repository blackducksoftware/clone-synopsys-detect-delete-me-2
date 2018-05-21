package com.blackducksoftware.integration.hub.detect.extraction.bomtool.go;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraph;
import com.blackducksoftware.integration.hub.bdio.graph.MutableMapDependencyGraph;
import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.extraction.Extractor;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.go.parse.DepPackager;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocationFactory;

@Component
public class GoDepExtractor extends Extractor<GoDepContext> {

    @Autowired
    DepPackager goPackager;

    @Autowired
    ExternalIdFactory externalIdFactory;

    @Autowired
    protected DetectCodeLocationFactory codeLocationFactory;

    @Override
    public Extraction extract(final GoDepContext context) {

        DependencyGraph graph = goPackager.makeDependencyGraph(context.directory.toString(), context.goDepInspector);
        if(graph == null) {
            graph = new MutableMapDependencyGraph();
        }
        final ExternalId externalId = externalIdFactory.createPathExternalId(Forge.GOLANG, context.directory.toString());
        final DetectCodeLocation detectCodeLocation = codeLocationFactory.createBomCodeLocation(BomToolType.GO_DEP, context.directory, externalId, graph);

        return new Extraction.Builder().success(detectCodeLocation).build();

    }

}
