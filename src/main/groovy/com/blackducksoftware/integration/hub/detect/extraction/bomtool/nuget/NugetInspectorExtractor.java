package com.blackducksoftware.integration.hub.detect.extraction.bomtool.nuget;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.bdio.graph.DependencyGraphCombiner;
import com.blackducksoftware.integration.hub.bdio.graph.MutableDependencyGraph;
import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.extraction.Extraction;
import com.blackducksoftware.integration.hub.detect.extraction.Extractor;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.nuget.parse.NugetInspectorPackager;
import com.blackducksoftware.integration.hub.detect.extraction.bomtool.nuget.parse.NugetParseResult;
import com.blackducksoftware.integration.hub.detect.model.BomDetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocation;
import com.blackducksoftware.integration.hub.detect.model.DetectCodeLocationFactory;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.detect.util.DetectFileManager;
import com.blackducksoftware.integration.hub.detect.util.executable.Executable;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableOutput;
import com.blackducksoftware.integration.hub.detect.util.executable.ExecutableRunner;

@Component
public class NugetInspectorExtractor extends Extractor<NugetInspectorContext>  {
    private final Logger logger = LoggerFactory.getLogger(NugetInspectorExtractor.class);

    static final String INSPECTOR_OUTPUT_PATTERN ="*_inspection.json";

    @Autowired
    private DetectConfiguration detectConfiguration;

    @Autowired
    private ExecutableRunner executableRunner;

    @Autowired
    public DetectFileManager detectFileManager;

    @Autowired
    private DetectFileFinder detectFileFinder;

    @Autowired
    NugetInspectorPackager nugetInspectorPackager;

    @Autowired
    public DetectCodeLocationFactory codeLocationFactory;

    @Override
    public Extraction extract(final NugetInspectorContext context) {

        try {
            final File outputDirectory = detectFileManager.getOutputDirectory(context);

            final List<String> options = new ArrayList<>(Arrays.asList(
                    "--target_path=" + context.directory.toString(),
                    "--output_directory=" + outputDirectory.getCanonicalPath(),
                    "--ignore_failure=" + detectConfiguration.getNugetInspectorIgnoreFailure()
                    ));

            if (detectConfiguration.getNugetInspectorExcludedModules() != null) {
                options.add("--excluded_modules=" + detectConfiguration.getNugetInspectorExcludedModules());
            }
            if (detectConfiguration.getNugetInspectorIncludedModules() != null) {
                options.add("--included_modules=" + detectConfiguration.getNugetInspectorIncludedModules());
            }
            if (detectConfiguration.getNugetPackagesRepoUrl() != null) {
                final String packagesRepos = Arrays.asList(detectConfiguration.getNugetPackagesRepoUrl()).stream().collect(Collectors.joining(","));
                options.add("--packages_repo_url=" + packagesRepos);
            }
            if (logger.isTraceEnabled()) {
                options.add("-v");
            }

            final Executable hubNugetInspectorExecutable = new Executable(context.directory, context.inspectorExe, options);
            final ExecutableOutput executableOutput = executableRunner.execute(hubNugetInspectorExecutable);

            if (executableOutput.getReturnCode() != 0) {
                return new Extraction.Builder().failure("Executable returned nothing.").build();
            }

            final List<File> dependencyNodeFiles = detectFileFinder.findFiles(outputDirectory, INSPECTOR_OUTPUT_PATTERN);
            final List<NugetParseResult> parseResults = dependencyNodeFiles.stream()
                    .map(it -> nugetInspectorPackager.createDetectCodeLocation(it))
                    .collect(Collectors.toList());

            final List<BomDetectCodeLocation> codeLocations = parseResults.stream()
                    .flatMap(it -> it.codeLocations.stream())
                    .collect(Collectors.toList());

            if (codeLocations.size() <= 0) {
                logger.warn("Unable to extract any dependencies from nuget");
            }

            final Map<String, BomDetectCodeLocation> codeLocationsBySource = new HashMap<>();
            final DependencyGraphCombiner combiner = new DependencyGraphCombiner();

            codeLocations.stream().forEach ( codeLocation -> {
                if (codeLocationsBySource.containsKey(codeLocation.getRelativePath())) {
                    logger.info("Multiple project code locations were generated for: " + context.directory.toString());
                    logger.info("This most likely means the same project exists in multiple solutions.");
                    logger.info("The code location's dependencies will be combined, in the future they will exist seperately for each solution.");
                    final DetectCodeLocation destination = codeLocationsBySource.get(codeLocation.getRelativePath());
                    combiner.addGraphAsChildrenToRoot((MutableDependencyGraph) destination.getDependencyGraph(), codeLocation.getDependencyGraph());
                } else {
                    codeLocationsBySource.put(codeLocation.getRelativePath(), codeLocation);
                }
            });

            final List<DetectCodeLocation> uniqueCodeLocations = codeLocationsBySource.values().stream().collect(Collectors.toList());

            final Extraction.Builder builder = new Extraction.Builder().success(uniqueCodeLocations);
            final Optional<NugetParseResult> project = parseResults.stream().filter(it -> StringUtils.isNotBlank(it.projectName)).findFirst();
            if (project.isPresent()) {
                builder.projectName(project.get().projectName);
                builder.projectVersion(project.get().projectVersion);
            }
            return builder.build();
        } catch (final Exception e) {
            return new Extraction.Builder().exception(e).build();
        }
    }

}
