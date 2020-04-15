/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.detect.workflow.blackduck;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionPolicyStatusView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.service.ProjectBomService;
import com.synopsys.integration.blackduck.service.model.PolicyStatusDescription;
import com.synopsys.integration.detect.exitcode.ExitCodeType;
import com.synopsys.integration.detect.lifecycle.shutdown.ExitCodeRequest;
import com.synopsys.integration.detect.workflow.event.Event;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.exception.IntegrationException;

public class PolicyChecker {
    private final Logger logger = LoggerFactory.getLogger(PolicyChecker.class);

    private final EventSystem eventSystem;

    public PolicyChecker(final EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public void checkPolicy(final List<PolicyRuleSeverityType> policySeverities, final ProjectBomService projectBomService, final ProjectVersionView projectVersionView) throws IntegrationException {
        final Optional<PolicyStatusDescription> optionalPolicyStatusDescription = getPolicyStatus(projectBomService, projectVersionView);
        if (optionalPolicyStatusDescription.isPresent()) {
            final PolicyStatusDescription policyStatusDescription = optionalPolicyStatusDescription.get();
            logger.info(policyStatusDescription.getPolicyStatusMessage());
            if (arePolicySeveritiesViolated(policyStatusDescription, policySeverities)) {
                eventSystem.publishEvent(Event.ExitCode, new ExitCodeRequest(ExitCodeType.FAILURE_POLICY_VIOLATION, policyStatusDescription.getPolicyStatusMessage()));
            }
        } else {
            final String availableLinks = StringUtils.join(projectVersionView.getAvailableLinks(), ", ");
            logger.warn(String.format("It is not possible to check the policy status for this project/version. The policy-status link must be present. The available links are: %s", availableLinks));
        }
    }

    /**
     * For the given DetectProject, find the matching Black Duck project/version, then all of its code locations, then all of their scan summaries, wait until they are all complete, then get the policy status.
     * @throws IntegrationException upon failure to query Black Duck.
     */
    public Optional<PolicyStatusDescription> getPolicyStatus(final ProjectBomService projectBomService, final ProjectVersionView version) throws IntegrationException {
        final Optional<ProjectVersionPolicyStatusView> projectVersionPolicyStatusView = projectBomService.getPolicyStatusForVersion(version);
        if (!projectVersionPolicyStatusView.isPresent()) {
            return Optional.empty();
        }

        final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(projectVersionPolicyStatusView.get());

        PolicyStatusType statusEnum = PolicyStatusType.NOT_IN_VIOLATION;
        if (policyStatusDescription.getCountInViolation() != null && policyStatusDescription.getCountInViolation().value > 0) {
            statusEnum = PolicyStatusType.IN_VIOLATION;
        } else if (policyStatusDescription.getCountInViolationOverridden() != null && policyStatusDescription.getCountInViolationOverridden().value > 0) {
            statusEnum = PolicyStatusType.IN_VIOLATION_OVERRIDDEN;
        }
        logger.info(String.format("Policy Status: %s", statusEnum.name()));
        return Optional.of(policyStatusDescription);
    }

    private boolean arePolicySeveritiesViolated(final PolicyStatusDescription policyStatusDescription, final List<PolicyRuleSeverityType> policySeverities) {
        for (final PolicyRuleSeverityType policySeverity : policySeverities) {
            final int severityCount = policyStatusDescription.getCountOfSeverity(policySeverity);
            if (severityCount > 0) {
                return true;
            }
        }

        return false;
    }

}
