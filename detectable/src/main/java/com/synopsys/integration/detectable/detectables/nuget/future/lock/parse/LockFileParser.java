package com.synopsys.integration.detectable.detectables.nuget.future.lock.parse;

import com.google.gson.Gson;
import com.synopsys.integration.detectable.detectables.nuget.future.lock.parse.model.NuGetLockFile;

public class LockFileParser {
    private final Gson gson;

    public LockFileParser(Gson gson) {this.gson = gson;}

    public NuGetLockFile parse(String text) {
        return gson.fromJson(text, NuGetLockFile.class);
    }
}
