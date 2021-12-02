/*
 * detectable
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detectable.detectables.nuget.future;

public class ParsedProject {
    private final String path;
    private final String guid; //Not currently used, but presently parsed.
    private final String name; //Not currently used, but presently parsed.
    //public List<String> projectDependencies; //Available, but not presently parsed.

    public ParsedProject(String path, String guid, String name) {
        this.path = path;
        this.guid = guid;
        this.name = name;
    }

    public String getPath() {
        return path;
    }
}
