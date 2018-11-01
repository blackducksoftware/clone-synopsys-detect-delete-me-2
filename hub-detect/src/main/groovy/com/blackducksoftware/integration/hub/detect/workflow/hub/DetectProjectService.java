package com.blackducksoftware.integration.hub.detect.workflow.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.detect.exception.DetectUserFriendlyException;
import com.blackducksoftware.integration.hub.detect.exitcode.ExitCodeType;
import com.blackducksoftware.integration.hub.detect.hub.HubServiceManager;
import com.synopsys.integration.blackduck.api.generated.component.ProjectRequest;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectRequestBuilder;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class DetectProjectService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HubServiceManager hubServiceManager;
    private final DetectProjectServiceOptions detectProjectServiceOptions;

    public DetectProjectService(final HubServiceManager hubServiceManager, final DetectProjectServiceOptions detectProjectServiceOptions) {
        this.hubServiceManager = hubServiceManager;
        this.detectProjectServiceOptions = detectProjectServiceOptions;
    }

    public Optional<ProjectVersionView> createOrUpdateHubProject(NameVersion projectNameVersion) throws IntegrationException, DetectUserFriendlyException, InterruptedException {
        final ProjectService projectService = hubServiceManager.createProjectService();
        final HubService hubService = hubServiceManager.createHubService();
        final ProjectRequest projectRequest = createProjectRequest(projectNameVersion, projectService, hubService);
        final boolean forceUpdate = detectProjectServiceOptions.isForceProjectVersionUpdate();
        ProjectVersionWrapper projectVersionViewWrapper = projectService.syncProjectAndVersion(projectRequest, forceUpdate);
        return Optional.ofNullable(projectVersionViewWrapper.getProjectVersionView());
    }

    public ProjectRequest createProjectRequest(final NameVersion projectNameVersion, final ProjectService projectService, final HubService hubService) throws DetectUserFriendlyException {
        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();

        projectRequestBuilder.setProjectName(projectNameVersion.getName());
        projectRequestBuilder.setVersionName(projectNameVersion.getVersion());

        projectRequestBuilder.setProjectLevelAdjustments(detectProjectServiceOptions.isProjectLevelAdjustments());
        projectRequestBuilder.setPhase(detectProjectServiceOptions.getProjectVersionPhase());
        projectRequestBuilder.setDistribution(detectProjectServiceOptions.getProjectVersionDistribution());
        projectRequestBuilder.setProjectTier(detectProjectServiceOptions.getProjectTier());
        projectRequestBuilder.setDescription(detectProjectServiceOptions.getProjectDescription());
        projectRequestBuilder.setReleaseComments(detectProjectServiceOptions.getProjectVersionNotes());
        projectRequestBuilder.setCloneCategories(convertClonePropertyToEnum(detectProjectServiceOptions.getCloneCategories()));

        final Optional<String> cloneUrl = findCloneUrl(projectNameVersion, projectService, hubService);
        if (cloneUrl.isPresent()) {
            logger.info("Cloning project version from release url: " + cloneUrl.get());
            projectRequestBuilder.setCloneFromReleaseUrl(cloneUrl.get());
        }

        return projectRequestBuilder.build();
    }

    private List<ProjectCloneCategoriesType> convertClonePropertyToEnum(final String[] cloneCategories) {
        final List<ProjectCloneCategoriesType> categories = new ArrayList<>();
        for (final String category : cloneCategories) {
            categories.add(ProjectCloneCategoriesType.valueOf(category));
        }
        logger.debug("Found clone categories:" + categories.stream().map(it -> it.toString()).collect(Collectors.joining(",")));
        return categories;
    }

    public Optional<String> findCloneUrl(NameVersion projectNameVersion, final ProjectService projectService, final HubService hubService) throws DetectUserFriendlyException {
        final String cloneProjectName = projectNameVersion.getName();
        final String cloneProjectVersionName = detectProjectServiceOptions.getCloneVersionName();
        if (StringUtils.isBlank(cloneProjectName) || StringUtils.isBlank(cloneProjectVersionName)) {
            logger.debug("No clone project or version name supplied. Will not clone.");
            return Optional.empty();
        }
        try {
            final ProjectVersionWrapper projectVersionWrapper = projectService.getProjectVersion(cloneProjectName, cloneProjectVersionName);
            final String url = hubService.getHref(projectVersionWrapper.getProjectVersionView());
            return Optional.of(url);
        } catch (final IntegrationException e) {
            throw new DetectUserFriendlyException("Unable to find clone release url for supplied clone version name.", e, ExitCodeType.FAILURE_CONFIGURATION);
        }

    }
}
