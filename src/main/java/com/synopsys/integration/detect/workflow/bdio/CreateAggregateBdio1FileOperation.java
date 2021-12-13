package com.synopsys.integration.detect.workflow.bdio;

import com.synopsys.integration.bdio.SimpleBdioFactory;
import com.synopsys.integration.bdio.model.SimpleBdioDocument;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;

//TODO: this may not need to exist anymore and can just use the standard write bdio operations.
public class CreateAggregateBdio1FileOperation {
    private final SimpleBdioFactory simpleBdioFactory;
    private final DetectBdioWriter detectBdioWriter;

    public CreateAggregateBdio1FileOperation(SimpleBdioFactory simpleBdioFactory, DetectBdioWriter detectBdioWriter) {
        this.simpleBdioFactory = simpleBdioFactory;
        this.detectBdioWriter = detectBdioWriter;
    }

    public void writeAggregateBdio1File(AggregateCodeLocation aggregateCodeLocation) throws DetectUserFriendlyException {
        SimpleBdioDocument aggregateBdioDocument = simpleBdioFactory.createSimpleBdioDocument(aggregateCodeLocation.getCodeLocationName(), aggregateCodeLocation.getProjectNameVersion().getName(),
            aggregateCodeLocation.getProjectNameVersion().getVersion(), aggregateCodeLocation.getProjectExternalId(), aggregateCodeLocation.getAggregateDependencyGraph());
        detectBdioWriter.writeBdioFile(aggregateCodeLocation.getAggregateFile(), aggregateBdioDocument);
    }
}
