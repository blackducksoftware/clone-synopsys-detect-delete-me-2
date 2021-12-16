package com.synopsys.integration.detectable.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonUtil {
    public static JsonObject toJson(String arg) {
        return JsonParser.parseString(arg).getAsJsonObject();
    }
}
