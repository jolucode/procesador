package service.cloud.request.clientRequest.extras;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Esta interfaz contiene las constantes validas establecidas por Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */

//
public interface IUBLConfig {

    ArrayList<BigDecimal> lstImporteIGV = new ArrayList<>();

    /**
     * Codigo de tipos de documentos
     */
    String ID_DOC_WITHOUT_RUC = "0";
    String ID_DOC_DNI = "1";
    String ID_DOC_FOREIGN_CARD = "4";
    String ID_DOC_RUC = "6";
    String ID_DOC_PASSPORT = "7";
    String ID_DOC_DIPLOMATIC_ID = "A";
    String CARGO_DESCUENTO_TEXT = "Cargo/descuento";

    int DOC_RUC_LENGTH = 11;

    /**
     * Version del estandar UBL utilizado en los documentos
     */
    String UBL_VERSION_ID_2_0 = "2.0";
    String UBL_VERSION_ID_2_1 = "2.1";

    /**
     * Version del valor CustomizationIDType
     */
    String CUSTOMIZATION_ID_1_0 = "1.0";
    String CUSTOMIZATION_ID_2_0 = "2.0";


    /**
     * Codigos que representan un tipo de documento UBL.
     */
    String DOC_INVOICE_CODE = "01";
    String DOC_BOLETA_CODE = "03";
    String DOC_CREDIT_NOTE_CODE = "07";
    String DOC_RETENTION_CODE = "20";
    String DOC_PERCEPTION_CODE = "40";
    String DOC_DEBIT_NOTE_CODE = "08";
    String DOC_SENDER_REMISSION_GUIDE_CODE = "09";
    String DOC_MACHINE_TICKET_CODE = "12";
    String DOC_FINANCIAL_BANKS_CODE = "13";
    String DOC_BANK_INSURANCE_CODE = "18";
    String DOC_ISSUED_BY_AFP_CODE = "31";

    /**
     * Codigo de tipo de Nota de Credito - 01 : Anulacion de la operacion - 02 :
     * Anulacion por error en el RUC - 03 : Correccion por error en la
     * descripcion - 04 : Descuento global - 05 : Descuento por item - 06 :
     * Devolucion total - 07 : Devolucion por item - 08 : Bonificacion - 09 :
     * Disminucion en el valor - 10 : Otros conceptos
     */
    String CREDIT_NOTE_TYPE_01 = "01";
    String CREDIT_NOTE_TYPE_02 = "02";
    String CREDIT_NOTE_TYPE_03 = "03";
    String CREDIT_NOTE_TYPE_04 = "04";
    String CREDIT_NOTE_TYPE_05 = "05";
    String CREDIT_NOTE_TYPE_06 = "06";
    String CREDIT_NOTE_TYPE_07 = "07";
    String CREDIT_NOTE_TYPE_08 = "08";
    String CREDIT_NOTE_TYPE_09 = "09";
    String CREDIT_NOTE_TYPE_10 = "10";

    /**
     * Codigo de tipo de Nota de Debito - 01 : Intereses por mora - 02 : Aumento
     * en el valor - 03 : Penalidades / otros conceptos
     */
    String DEBIT_NOTE_TYPE_01 = "01";
    String DEBIT_NOTE_TYPE_02 = "02";
    String DEBIT_NOTE_TYPE_03 = "03";

    /**
     * Cantidad de caracteres del identificador del documento
     */
    int SERIE_CORRELATIVE_LENGTH = 13;

    String REMISSION_GUIDE_SERIE_PREFIX = "T";
    String INVOICE_SERIE_PREFIX = "F";
    String RETENTION_SERIE_PREFIX = "R";
    String BOLETA_SERIE_PREFIX = "B";
    String PERCEPCION_SERIE_PREFIX = "P";
    String VOIDED_SERIE_PREFIX = "RA";
    String SUMMARY_SERIE_PREFIX = "RC";
    String VOIDED_SERIE_PREFIX_CPE = "RR";

    /**
     * Parametros de los impuestos en UBL - ID : Identificador del impuesto -
     * NAME : Nombre del impuesto - CODE : Codigo asignado al impuesto
     */
    String TAX_TOTAL_IGV_ID = "1000";
    String TAX_TOTAL_ISC_ID = "2000";
    String TAX_TOTAL_EXP_ID = "9995";
    String TAX_TOTAL_GRA_ID = "9996";
    String TAX_TOTAL_EXO_ID = "9997";
    String TAX_TOTAL_INA_ID = "9998";
    String TAX_TOTAL_OTR_ID = "9999";
    String TAX_TOTAL_BPT_ID = "7152";

    String TAX_TOTAL_IGV_NAME = "IGV";
    String TAX_TOTAL_ISC_NAME = "ISC";
    String TAX_TOTAL_EXP_NAME = "EXP";
    String TAX_TOTAL_GRA_NAME = "GRA";
    String TAX_TOTAL_GRT_NAME = "GRA";
    String TAX_TOTAL_EXO_NAME = "EXO";
    String TAX_TOTAL_INA_NAME = "INA";
    String TAX_TOTAL_OTR_NAME = "OTR";
    String TAX_TOTAL_BPT_NAME = "ICBPER";

    String TAX_TOTAL_IGV_CODE = "VAT";
    String TAX_TOTAL_ISC_CODE = "EXC";
    String TAX_TOTAL_EXP_CODE = "FRE";
    String TAX_TOTAL_GRA_CODE = "FRE";
    String TAX_TOTAL_EXO_CODE = "VAT";
    String TAX_TOTAL_INA_CODE = "FRE";
    String TAX_TOTAL_OTR_CODE = "OTH";

    /**
     * Tag's del AlternativeConditionPrice.
     */
    String ALTERNATIVE_CONDICION_UNIT_PRICE = "01";
    String ALTERNATIVE_CONDICION_REFERENCE_VALUE = "02";
    String ALTERNATIVE_CONDICION_REGULATED_RATES = "03";

    /**
     * Decimales en los tax del UBL
     */
    int DECIMAL_TAX_TOTAL_AMOUNT = 2;
    int DECIMAL_TAX_TOTAL_IGV_PERCENT = 2;
    int DECIMAL_PREPAIDPAYMENT_PAIDAMOUNT = 2;
    int DECIMAL_MONETARYTOTAL_PAYABLEAMOUNT = 2;
    int DECIMAL_ALLOWANCECHARGE_MULTIPLIERFACTORNUMERIC = 5;
    int DECIMAL_ALLOWANCECHARGE_AMOUNT = 2;
    int DECIMAL_ALLOWANCECHARGE_BASEAMOUNT = 2;
    int DECIMAL_MONETARYTOTAL_LINEEXTENSIONAMOUNT = 2;
    int DECIMAL_MONETARYTOTAL_CHARGETOTALAMOUNT = 2;
    int DECIMAL_MONETARYTOTAL_PREPAIDAMOUNT = 2;
    int DECIMAL_ADDITIONAL_MONETARY_TOTAL_PAYABLE_AMOUNT = 2;
    int DECIMAL_PAYMENTTERMS_PAYMENTPERCENT = 2;
    int DECIMAL_PAYMENTTERMS_AMOUNT = 2;
    int DECIMAL_LINE_QUANTITY = 10;
    int DECIMAL_LINE_LINEEXTENSIONAMOUNT = 2;
    int DECIMAL_LINE_TAX_AMOUNT = 2;
    int DECIMAL_LINE_TAX_IGV_PERCENT = 2;
    int DECIMAL_LINE_UNIT_VALUE = 10;
    int DECIMAL_LINE_UNIT_PRICE = 10;
    int DECIMAL_LINE_REFERENCE_VALUE = 2;
    int DECIMAL_LINE_DELIVERY_DELIVERYTERMS_AMOUNT = 2;
    int DECIMAL_LINE_DELIVERY_SHIPMENT_CONSIGMENT_TRANSPORTHANDLINGUNIT_MEASUREMENTDIMENSION_MEASURE = 2;
    int DECIMAL_LINE_ITEM_ADDITIONALITEMPROPERTY_VALUEQUANTITY = 2;

    String CONTRACT_DOC_REF_PAYMENT_COND_CODE = "pay_cond";
    String CONTRACT_DOC_REF_SELL_ORDER_CODE = "cu01";
    String CONTRACT_DOC_REF_SELL_ORDER_INCO = "incoterms";

    /**
     * Formato de fechas dentro de un Documento UBL.
     */
    String DATE_FORMAT = "yyyy-MM-dd";
    String ISSUEDATE_FORMAT = "yyyy-MM-dd";
    String ISSUETIME_FORMAT = "HH:mm:ss";
    String DUEDATE_FORMAT = "yyyy-MM-dd";
    String REFERENCEDATE_FORMAT = "yyyy-MM-dd";
    String STARTDATE_FORMAT = "yyyy-MM-dd";
    String PAIDDATE_FORMAT = "yyyy-MM-dd";
    String SUNATPERCEPTIONDATE_FORMAT = "yyyy-MM-dd";
    String SUNATRETENTIONDATE_FORMAT = "yyyy-MM-dd";

    String HIDDEN_UVALUE = "hidden_uvalue";

    String ADDITIONAL_MONETARY_1001 = "1001";
    String ADDITIONAL_MONETARY_1002 = "1002";
    String ADDITIONAL_MONETARY_1003 = "1003";
    String ADDITIONAL_MONETARY_1004 = "1004";
    String ADDITIONAL_MONETARY_1005 = "1005";
    String ADDITIONAL_MONETARY_2001 = "2001";
    String ADDITIONAL_MONETARY_2002 = "2002";
    String ADDITIONAL_MONETARY_2003 = "2003";
    String ADDITIONAL_MONETARY_2004 = "2004";
    String ADDITIONAL_MONETARY_2005 = "2005";
    String ADDITIONAL_MONETARY_3001 = "3001";

    String ADDITIONAL_PROPERTY_1000 = "1000";


    String UBL_DIGESTVALUE_TAG = "ds:DigestValue";
    String UBL_SIGNATUREVALUE_TAG = "ds:SignatureValue";
    String UBL_SUNAT_TRANSACTION_TAG = "sac:SUNATTransaction";


    /**
     * @schemeAgencyName
     */
    String PROPERTIES_NAME_ITEM = "Propiedad del Item";
    String SCHEME_AGENCY_NAME_PE_SUNAT = "PE:SUNAT";
    String SCHEME_AGENCY_NAME_PE_INEI = "PE:INEI";
    String SCHEME_AGENCY_NAME_UNECE = "United Nations Economic Commission for Europe";
    String GUIAS_LIST_NAME = "Tipo de Documento";
    String GUIAS_SCHEMA_NAME = "Documento de Identidad";
    String DOCUMENT_REL = "Documento relacionado al transporte";

    /**
     * @listAgencyName
     */
    String LIST_AGENCY_NAME_PE_MTC = "PE:MTC";
    String LIST_AGENCY_NAME_PE_SUNAT = "PE:SUNAT";
    String LIST_AGENCY_NAME_UNECE = "United Nations Economic Commission for Europe";
    String ESTABLECIMIENTOS_GUIAS = "Establecimientos anexos";

    String INDICATOR_SUBPART = "Subpartida nacional";
    String INDICATOR_REGULADO_SUNAT = "Indicador de bien regulado por SUNAT";
    String INDICATOR_NUMERACION_DAM_DS = "Numeracion de la DAM o DS";
    String INDICATOR_SERIE_DAM_DS = "Numero de serie en la DAM o DS";


    /**
     * @GuiasSunatRest
     */
    String TRANSPORT_MOT = "Motivo de traslado";
    String TRANSPORT_MOD = "Modalidad de traslado";

    /**
     * @listName
     */
    String LIST_NAME_CREDIT_NOTE_TYPE = "Tipo de nota de credito";
    String LIST_NAME_DEBIT_NOTE_TYPE = "Tipo de nota de debito";


    /**
     * @listURI and @schemeURI
     */

    String URI_CATALOG_01 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01";
    String URI_CATALOG_05 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo05";
    String URI_CATALOG_06 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo06";
    String URI_CATALOG_07 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo07";
    String URI_CATALOG_09 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo09";
    String URI_CATALOG_10 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo10";
    String URI_CATALOG_12 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo12";
    String URI_CATALOG_20 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo20";
    String URI_CATALOG_16 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo16";
    String URI_CATALOG_18 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo18";
    String URI_CATALOG_51 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo51";
    String URI_CATALOG_53 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo53";
    String URI_CATALOG_54 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo54";
    String URI_CATALOG_55 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo55";
    String URI_CATALOG_59 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo59";
    String URI_CATALOG_63 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo63";
    String URI_CATALOG_61 = "urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo61";


    String ADDRESS_TYPE_CODE_DEFAULT = "0000";

    String TAX_TOTAL_GRT_ID = "9996";
} //IUBLConfig
