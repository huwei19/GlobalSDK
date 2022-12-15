package com.pwrd.hmsea.bridge;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonUtil {
    private static Gson sGson = new Gson();

    public JsonUtil() {
    }

    public static String object2Json(Object object) {
        return object == null ? "" : sGson.toJson(object);
    }

    public static <T> T json2Object(String json, Type type) {
        return sGson.fromJson(json, type);
    }
}
