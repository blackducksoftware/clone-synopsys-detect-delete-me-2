package com.synopsys.integration.detectable.detectables.bazel.pipeline.step;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectables.bazel.pipeline.step.parse.GithubUrlParser;

public class FinalStepTransformGithubUrl implements FinalStep {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ExternalIdFactory externalIdFactory;
    private final GithubUrlParser githubUrlParser;

    public FinalStepTransformGithubUrl(ExternalIdFactory externalIdFactory, GithubUrlParser githubUrlParser) {
        this.externalIdFactory = externalIdFactory;
        this.githubUrlParser = githubUrlParser;
    }

    @Override
    public List<Dependency> finish(List<String> input) throws DetectableException {
        List<Dependency> dependencies = new LinkedList<>();
        for (String githubUrl : input) {
            logger.debug("bazel githubUrl: {}", githubUrl);
            try {
                String organization = githubUrlParser.parseOrganization(githubUrl);
                String repo = githubUrlParser.parseRepo(githubUrl);
                String version = githubUrlParser.parseVersion(githubUrl);
                Dependency dep = Dependency.FACTORY.createNameVersionDependency(Forge.GITHUB, organization + "/" + repo, version);
                dependencies.add(dep);
            } catch (MalformedURLException e) {
                logger.debug("URL '{}' does not appear to be a github released artifact location", githubUrl);
            }
        }
        return dependencies;
    }
}
