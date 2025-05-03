package service.cloud.request.clientRequest.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import service.cloud.request.clientRequest.config.StringCleaningTypeAdapterFactory;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {

    // Gson con adaptador personalizado que limpia strings al deserializar
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new StringCleaningTypeAdapterFactory())
            .create();

    // Serializa un objeto a JSON (sin limpieza)
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    // Deserializa y limpia strings en el proceso
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}

