package service.cloud.request.clientRequest.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class Util {


    public static JSONObject convertToJSONObject(Object myObj) {
        JSONObject jsonObject = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(myObj);
            jsonObject = new JSONObject(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static boolean esValido(String cadena) {
        return cadena != null && !cadena.isEmpty();
    }


    public static JSONObject flattenJSON(JSONObject jsonObject) {
        JSONObject flattenedJson = new JSONObject();
        flattenJSONHelper(jsonObject, flattenedJson);
        return flattenedJson;
    }

    private static void flattenJSONHelper(JSONObject jsonObject, JSONObject flattenedJson) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                flattenJSONHelper((JSONObject) value, flattenedJson);
            } else if (value instanceof JSONArray) {
                flattenJSONArray((JSONArray) value, flattenedJson, key);
            } else {
                flattenedJson.put(key, value);
            }
        }
    }

    private static void flattenJSONArray(JSONArray jsonArray, JSONObject flattenedJson, String parentKey) {
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONObject) {
                flattenJSONHelper((JSONObject) value, flattenedJson);
            } else if (value instanceof JSONArray) {
                flattenJSONArray((JSONArray) value, flattenedJson, parentKey);
            } else {
                flattenedJson.put(parentKey, value);
            }
        }
    }


    public static Date returnDate(String dateString) {
        // Formatos de fecha existentes
        SimpleDateFormat originalDateFormatFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        SimpleDateFormat originalDateFormatShort = new SimpleDateFormat("yyyy-MM-dd");

        // Nuevo formato para fechas en formato dd/MM/yyyy
        SimpleDateFormat newDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date date;
        try {
            // Intentar con el formato completo
            date = originalDateFormatFull.parse(dateString);
        } catch (ParseException e) {
            // Intentar con el formato corto yyyy-MM-dd
            try {
                date = originalDateFormatShort.parse(dateString);
            } catch (ParseException ex) {
                // Intentar con el nuevo formato dd/MM/yyyy
                try {
                    date = newDateFormat.parse(dateString);
                } catch (ParseException exc) {
                    exc.printStackTrace();
                    return null;
                }
            }
        }

        // Convertir la fecha al formato deseado yyyy-MM-dd
        String formattedDateString = desiredDateFormat.format(date);
        try {
            return desiredDateFormat.parse(formattedDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String getTipoTransac(String type) {
        if (type.equals("E")) {
            return "Emisión";
        } else if (type.equals("C")) {
            return "Consulta";
        } else if (type.equals("B")) {
            return "Baja";
        }
        return type;
    }


}
