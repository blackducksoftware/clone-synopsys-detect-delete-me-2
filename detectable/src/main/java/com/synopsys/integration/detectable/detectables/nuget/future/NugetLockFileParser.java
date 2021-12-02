package com.synopsys.integration.detectable.detectables.nuget.future;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NugetLockFileParser {
    private final Gson gson;

    public NugetLockFileParser(final Gson gson) {this.gson = gson;}

    public void parseFile(File file) throws FileNotFoundException {
        JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
        String version = json.getAsJsonPrimitive("version").getAsString();
        if (version.equals("2")) {
            JsonObject targets = json.getAsJsonObject("targets");
            for (final Map.Entry<String, JsonElement> target : targets.entrySet()) {

            }
        } else if (version.equals("3")) {

        }
    }
}
