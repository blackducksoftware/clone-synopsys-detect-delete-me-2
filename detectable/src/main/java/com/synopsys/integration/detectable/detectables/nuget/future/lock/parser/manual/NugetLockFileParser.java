package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class NugetLockFileParser {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Gson gson;
    private final NugetLockFileTargetParser targetParser;
    private final NugetLockFileLibraryParser libraryParser;
    private final NugetLockFileDependencyGroupParser dependencyGroupParser;
    private final NugetLockFileProjectParser projectParser;

    public NugetLockFileParser(Gson gson, NugetLockFileTargetParser targetParser, NugetLockFileLibraryParser libraryParser,
        NugetLockFileDependencyGroupParser dependencyGroupParser, NugetLockFileProjectParser projectParser) {
        this.gson = gson;
        this.targetParser = targetParser;
        this.libraryParser = libraryParser;
        this.dependencyGroupParser = dependencyGroupParser;
        this.projectParser = projectParser;
    }

    public void parseFile(File file) throws FileNotFoundException {
        JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
        String version = json.getAsJsonPrimitive("version").getAsString();
        if (version.equals("2") || version.equals("3")) {
            NugetLockFile lockfile = new NugetLockFile();
            lockfile.targets = targetParser.parseTargets(json);
            lockfile.libraries = libraryParser.parseLibraries(json);
            lockfile.dependencyGroups = dependencyGroupParser.parseDependencyGroups(json);
            lockfile.project = projectParser.parseProject(json);
        } else {
            logger.warn("Unknown lock file format: " + version);
        }
    }

}
