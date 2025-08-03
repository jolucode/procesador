package service.cloud.request.clientRequest.config;

import com.google.gson.*;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

import java.lang.reflect.Type;

public class TransacctionDTODeserializer implements JsonDeserializer<TransacctionDTO> {

    private final Gson cleanGson;

    public TransacctionDTODeserializer() {
        // Usa la limpieza general para todos los strings
        this.cleanGson = new GsonBuilder()
                .registerTypeAdapterFactory(new StringCleaningTypeAdapterFactory())
                .create();
    }

    @Override
    public TransacctionDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        // Deserializa todo el objeto con limpieza general
        TransacctionDTO dto = cleanGson.fromJson(json, TransacctionDTO.class);

        // FE_Comentario se mantiene original (sin limpiar)
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("FE_Comentario") && !jsonObject.get("FE_Comentario").isJsonNull()) {
            dto.setFE_Comentario(jsonObject.get("FE_Comentario").getAsString());
        }

        return dto;
    }
}

