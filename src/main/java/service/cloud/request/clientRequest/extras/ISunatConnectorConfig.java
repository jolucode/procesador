package service.cloud.request.clientRequest.extras;


public interface ISunatConnectorConfig {

    /**
     * Tabla:	Transaccion
     * Columna:	FE_TipoTrans
     */
    String FE_TIPO_TRANS_EMISION = "E";
    String FE_TIPO_TRANS_BAJA = "B";


    /**
     * Directorio de los documentos que se almacenan
     * en base al tipo de documento.
     */
    String INVOICE_PATH = "factura";
    String PERCEPTION_PATH = "percepcion";
    String RETENTION_PATH = "retencion";
    String REMISSION_GUIDE_PATH = "guiaremision";
    String BOLETA_PATH = "boleta";
    String CREDIT_NOTE_PATH = "notacredito";
    String DEBIT_NOTE_PATH = "notadebito";
    String SUMMARY_DOCUMENT_PATH = "resumen";
    String VOIDED_DOCUMENT_PATH = "baja";
    String REVERSION_DOCUMENT_PATH = "reversion";


    /**
     * Extensiones de archivos
     */
    String EE_XML = ".xml";
    String EE_ZIP = ".zip";
    String EE_PDF = ".pdf";


    /**
     * El formato de ENCODE.
     */
    String ENCODING_UTF = "UTF-8";

    /**
     * Las llaves del objeto MAP que contiene la respuesta
     * del CDR
     */
    String CDR_OBJECT_KEY = "CDR_OBJECT";
    String CDR_BYTES_KEY = "CDR_BYTES";

} //ISunatConnectorConfig
