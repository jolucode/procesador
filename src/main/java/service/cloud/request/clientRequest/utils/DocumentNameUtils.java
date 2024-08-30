package service.cloud.request.clientRequest.utils;

import java.text.MessageFormat;

public class DocumentNameUtils {
    private static final String DOCUMENT_PATTERN = "{0}-{1}-{2}";

    /**
     * Este método genera el nombre de un documento dado el tipo de documento.
     *
     * @param senderRUC     Número de RUC del emisor.
     * @param docIdentifier Identificador del documento (Serie y correlativo).
     * @param docType       Código del tipo de documento.
     * @return Retorna el nombre del documento según el tipo de documento.
     */
    public static String getDocumentName(String senderRUC, String docIdentifier, String docType) {
        // Utiliza el patrón de formato para generar el nombre del documento
        return MessageFormat.format(DOCUMENT_PATTERN, senderRUC, docType, docIdentifier);
    }

    /**
     * Este método obtiene el nombre de un documento de tipo "Comunicación de Baja".
     *
     * @param senderRUC     Número de RUC del emisor.
     * @param docIdentifier Identificador del documento.
     * @return Retorna el nombre del documento de tipo "Comunicación de Baja".
     */
    public static String getVoidedDocumentName(String senderRUC, String docIdentifier) {
        return senderRUC + "-" + docIdentifier;
    }

}
