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
package com.synopsys.integration.detect.workflow.blackduck.analytics;

import java.io.IOException;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.UrlSupport;

public class AnalyticsConfigurationService {
    private static final BlackDuckPath INTEGRATION_SETTINGS_PATH = new BlackDuckPath("/api/internal/integration-settings");
    private static final String MIME_TYPE = "application/vnd.blackducksoftware.integration-setting-1+json";

    private final Gson gson;

    public AnalyticsConfigurationService(Gson gson) {
        this.gson = gson;
    }

    public AnalyticsSetting fetchAnalyticsSetting(BlackDuckService blackDuckService) throws IntegrationException, IOException {
        HttpUrl url = new UrlSupport().appendRelativeUrl(blackDuckService.getUrl(INTEGRATION_SETTINGS_PATH), "/analytics");

        Request request = new Request.Builder()
                              .url(url)
                              .method(HttpMethod.GET)
                              .acceptMimeType(MIME_TYPE)
                              .build();
        try (Response response = blackDuckService.execute(request)) {
            response.throwExceptionForError();
            return gson.fromJson(response.getContentString(), AnalyticsSetting.class);
        }
    }
}
