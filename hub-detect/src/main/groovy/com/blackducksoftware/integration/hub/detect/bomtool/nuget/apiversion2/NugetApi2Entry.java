/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect.bomtool.nuget.apiversion2;

import java.util.List;

public class NugetApi2Entry {
    private final String id;
    private final List<String> authors;
    private final String name;
    private final String version;

    public NugetApi2Entry(final String id, final List<String> authors, final String name, final String version) {
        this.id = id;
        this.authors = authors;
        this.name = name;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
