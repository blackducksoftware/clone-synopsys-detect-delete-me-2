package com.synopsys.integration.detectable.detectables.sbt.dot;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphParser;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.graph.MutableDependencyGraph;
import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.dependency.Dependency;

public class SbtGraphParserTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SbtDotGraphNodeParser sbtDotGraphNodeParser;

    public SbtGraphParserTransformer(SbtDotGraphNodeParser sbtDotGraphNodeParser) {
        this.sbtDotGraphNodeParser = sbtDotGraphNodeParser;
    }

    public DependencyGraph transformDotToGraph(GraphParser graphParser, String projectNodeId) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        for (GraphEdge graphEdge : graphParser.getEdges().values()) {
            Dependency parent = sbtDotGraphNodeParser.nodeToDependency(graphEdge.getNode1().getId());
            Dependency child = sbtDotGraphNodeParser.nodeToDependency(graphEdge.getNode2().getId());
            if (projectNodeId.equals(graphEdge.getNode1().getId())) {
                graph.addChildToRoot(child);
            } else {
                graph.addChildWithParent(child, parent);
            }
        }

        return graph;
    }

    public DependencyGraph transformDotToGraph(GraphParser graphParser, Set<String> projectNodeIds) {
        MutableDependencyGraph graph = new MutableMapDependencyGraph();

        for (GraphEdge graphEdge : graphParser.getEdges().values()) {
            Dependency parent = sbtDotGraphNodeParser.nodeToDependency(graphEdge.getNode1().getId());
            Dependency child = sbtDotGraphNodeParser.nodeToDependency(graphEdge.getNode2().getId());
            if (projectNodeIds.contains(graphEdge.getNode1().getId())) {
                graph.addChildToRoot(parent);
            }
            graph.addChildWithParent(child, parent);
        }

        return graph;
    }
}
