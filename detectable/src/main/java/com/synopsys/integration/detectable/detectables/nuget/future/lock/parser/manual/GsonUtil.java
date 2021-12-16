package com.synopsys.integration.detectable.detectables.nuget.future.lock.parser.manual;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonUtil {
    public static String getAsStringOrNull(JsonObject target, String memberName) {
        if (target.has(memberName)) {
            return target.get(memberName).getAsString();
        }
        return null;
    }

    public static void ifObjectPresent(JsonObject target, String memberName, Consumer<JsonObject> action) {
        if (target.has(memberName)) {
            action.accept(target.get(memberName).getAsJsonObject());
        }
    }

    public static void iterateMember(JsonObject target, String memberName, BiConsumer<String, JsonElement> action) {
        if (target.has(memberName)) {
            JsonObject memberJson = target.get(memberName).getAsJsonObject();
            iterate(memberJson, action);
        }
    }

    public static void iterate(JsonObject target, BiConsumer<String, JsonElement> action) {
        for (final Map.Entry<String, JsonElement> pair : target.entrySet()) {
            action.accept(pair.getKey(), pair.getValue());
        }
    }
}
