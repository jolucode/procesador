package service.cloud.request.clientRequest.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.service.emision.ServiceEmision;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    static Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final XMLGregorianCalendar stringDateToDateGregory(Date date) {
        XMLGregorianCalendar xmlDate = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(date));

        } catch (Exception e) {
            logger.error("Este es error en XMLGregorian -> " + e.getMessage());
        }
        return xmlDate;
    }


    public static final String formatIssueDate(XMLGregorianCalendar xmlGregorianCal)
            throws Exception {

        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();

        Locale locale = new Locale(IPDFCreatorConfig.LOCALE_ES,
                IPDFCreatorConfig.LOCALE_PE);

        SimpleDateFormat sdf = new SimpleDateFormat(
                IPDFCreatorConfig.PATTERN_DATE, locale);
        String issueDate = sdf.format(inputDate);


        return issueDate;
    }


    public static String extractDocType(String input) {
        // Buscar el primer guión después del cual se encuentra el valor
        int firstDashIndex = input.indexOf('-');

        // Verificar si se encontró el guión y hay al menos dos caracteres después
        if (firstDashIndex != -1 && firstDashIndex + 2 < input.length()) {
            // Extraer los dos caracteres después del guión
            return input.substring(firstDashIndex + 1, firstDashIndex + 3);
        } else {
            return null; // Manejar el caso en el que el formato no es el esperado
        }
    }

    public static boolean isRegularDocument(String documentType) {

        return !documentType.equals(IUBLConfig.DOC_RETENTION_CODE)
                && !documentType.equals(IUBLConfig.DOC_PERCEPTION_CODE);
    }


    public static BigDecimal round(BigDecimal value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        value = value.setScale(places, RoundingMode.HALF_UP);
        return value;
    }

    public static boolean isNullOrTrimmedEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    public static String construirSerie(String prefijo, String fecha, String nuevoId) {
        return prefijo+"-" + fecha + "-" + nuevoId;
    }


}
