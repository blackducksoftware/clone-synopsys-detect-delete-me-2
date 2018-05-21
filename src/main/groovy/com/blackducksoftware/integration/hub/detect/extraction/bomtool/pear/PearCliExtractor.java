package com.blackducksoftware.integration.hub.detect.extraction.bomtool.pear;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.model.Forge;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.model.externalid.ExternalIdFactory;
import com.blackducksoftware.integration.hub.detect.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.extraction.Extractor;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.pear.parse.PearDependencyFinder;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.pear.parse.PearParseResult;
import com.blackducksoftware.integration.hub.detect.model.BomToolType;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocationFactory;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;

@Component
public class PearCliExtractor extends Extractor<PearCliContext> {

    static final String PACKAGE_XML_FILENAME = "package.xml";

    @Autowired
    protected DetectFileFinder detectFileFinder;

    @Autowired
    protected ExternalIdFactory externalIdFactory;

    @Autowired
    PearDependencyFinder pearDependencyFinder;

    @Autowired
    protected ExecutableRunner executableRunner;

    @Autowired
    public DetectCodeLocationFactory codeLocationFactory;

    @Override
    public Extraction extract(final PearCliContext context) {
        try {
            final ExecutableOutput pearListing = executableRunner.runExe(context.pearExe, "list");
            final ExecutableOutput pearDependencies = executableRunner.runExe(context.pearExe, "package-dependencies", PACKAGE_XML_FILENAME);

            final File packageFile = detectFileFinder.findFile(context.directory, PACKAGE_XML_FILENAME);

            final PearParseResult result = pearDependencyFinder.parse(packageFile, pearListing, pearDependencies);
            final ExternalId id = externalIdFactory.createNameVersionExternalId(Forge.PEAR, result.name, result.version);
            final DetectCodeLocation detectCodeLocation = codeLocationFactory.createBomCodeLocation(BomToolType.PEAR, context.directory, id, result.dependencyGraph);


            return new Extraction.Builder().success(detectCodeLocation).projectName(result.name).projectVersion(result.version).build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

}
