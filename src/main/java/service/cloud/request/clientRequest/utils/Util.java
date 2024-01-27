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
        SimpleDateFormat originalDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        SimpleDateFormat desiredDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = originalDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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
