package service.cloud.request.clientrequest.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    // Definir múltiples formatos de fecha
    private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd", // Formato original
            "dd/MM/yyyy"  // Nuevo formato
    };

    public static Date parseDate(String dateString) {
        for (String format : DATE_FORMATS) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            try {
                return dateFormat.parse(dateString);
            } catch (ParseException e) {
                // Continuar intentando con los siguientes formatos
            }
        }
        // Si ninguno de los formatos funciona, manejar el error
        System.err.println("Formato de fecha no reconocido: " + dateString);
        return null;
    }

}
