package service.cloud.request.clientrequest.utils.exception;

import java.text.SimpleDateFormat;
import java.util.Date;
public class DateUtils {

  // Formatear Date a String
  public static String formatDateToString(Date date) {
    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return outputFormat.format(date);
  }

}