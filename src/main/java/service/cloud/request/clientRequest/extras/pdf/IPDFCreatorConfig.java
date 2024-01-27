package service.cloud.request.clientRequest.extras.pdf;

public interface IPDFCreatorConfig {

    // Datos Subreporte Cuotas


    // fin Datos Subreporte Cuotas


    /**
     * Ruta del archivo *.properties
     */
    String PROPERTIES_PATH = "org/ventura/soluciones/pdfcreator/resources/pdf_creator";

    /**
     * Valores seleccionables del TAG ContactType
     */
    String CONTACT_NAME_VALUE = "contact_name";
    String CONTACT_MAIL_VALUE = "contact_mail";
    String CAMPOS_USUARIO_CAB = "CamposUsuarioCab";

    /**
     * Valor por defecto para los TAG's vacios
     */
    String EMPTY_VALUE = "-";

    int DECIMAL_ITEM_QUANTITY = 3;

    String UBL_ADDITIONAL_MONETARY_TOTAL = "AdditionalMonetaryTotal";
    String UBL_ADDITIONAL_PROPERTY = "AdditionalProperty";
    String UBL_ID = "ID";
    String UBL_PAYABLE_AMOUNT = "PayableAmount";
    String UBL_VALUE = "Value";

    /**
     * Codigo y nombre de los tipos de monedas.
     */
    String CURRENCY_CODE_PEN = "PEN";
    String CURRENCY_CODE_PEN_VALUE = "NUEVOS SOLES";
    String CURRENCY_CODE_USD = "USD";
    String CURRENCY_CODE_USD_VALUE = "D\u00D3LAR ESTADOUNIDENSE";
    String CURRENCY_CODE_EUR = "EUR";
    String CURRENCY_CODE_EUR_VALUE = "EUROS";

    String LOCALE_ES = "es";
    String LOCALE_PE = "PE";
    String PATTERN_DATE = "dd/MM/yyyy";
    String PATTER_UBL_DATE = "yyyy-MM-dd";
    String LANG_PATTERN = "llll";

    /**
     * Regex and format patterns
     */
    String PATTERN_FLOAT_DEC = "###,###.00";
    String PATTERN_FLOAT_DEC_3 = "###,###.000";

    String ALTERNATIVE_COND_UNIT_PRICE = "01";
    String ALTERNATIVE_COND_REF_VALUE = "02";

    /**
     * Formato de CODIGO DE BARRAS
     */
    String BARCODE_PATTERN = "{0}|{1}|{2}|{3}|{4}|{5}|{6}|{7}|{8}|{9}";

    /**
     * Extensiones
     */
    String EE_PDF = ".pdf";

    /**
     * Valor cuando no hay LEYENDAS
     */
    String LEGEND_DEFAULT_EMPTY = "NINGUNA";

    /**
     * SUNATTransaction values
     */
    String SUNAT_TRANS_VENTA_INTERNA_ID = "01";
    String SUNAT_TRANS_VENTA_INTERNA_DSC = "Venta Interna";
    String SUNAT_TRANS_EXPORTACION_ID = "02";
    String SUNAT_TRANS_EXPORTACION_DSC = "Exportaci\u00F3n";
    String SUNAT_TRANS_NO_DOMICILIADOS_ID = "03";
    String SUNAT_TRANS_NO_DOMICILIADOS_DSC = "No Domiciliados";
    String SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_ID = "04";
    String SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_DSC = "Venta Interna - Anticipos";
    String SUNAT_TRANS_VENTA_ITINERANTE_ID = "05";
    String SUNAT_TRANS_VENTA_ITINERANTE_DSC = "Venta Itinerante";
    String SUNAT_TRANS_FACTURA_GUIA_ID = "06";
    String SUNAT_TRANS_FACTURA_GUIA_DSC = "Factura Gu\u00EDa";
    String SUNAT_TRANS_VENTA_ARROZ_PILADO_ID = "07";
    String SUNAT_TRANS_VENTA_ARROZ_PILADO_DSC = "Venta Arroz Pilado";
    String SUNAT_TRANS_FACTURA_COMP_PERCEPCION_ID = "08";
    String SUNAT_TRANS_FACTURA_COMP_PERCEPCION_DSC = "Factura - Comprobante de Percepci\u00F3n";

    /**
     * Abreviatura del tipo de documento
     */
    String ID_DOC_WITHOUT_RUC_DSC = "N.A.";
    String ID_DOC_DNI_DSC = "DNI";
    String ID_DOC_FOREIGN_CARD_DSC = "CE";
    String ID_DOC_RUC_DSC = "RUC";
    String ID_DOC_PASSPORT_DSC = "PAS";
    String ID_DOC_DIPLOMATIC_ID_DSC = "CDI";

    /**
     * Descripcion de los codigos de tipo de Nota de Credito
     */
    String CREDIT_NOTE_TYPE_01_DSC = "Anulaci\u00F3n de la Operaci\u00F3n";
    String CREDIT_NOTE_TYPE_02_DSC = "Anulaci\u00F3n por error en el RUC";
    String CREDIT_NOTE_TYPE_03_DSC = "Correcci\u00F3n por error en la Descripci\u00F3n";
    String CREDIT_NOTE_TYPE_04_DSC = "Descuento Global";
    String CREDIT_NOTE_TYPE_05_DSC = "Descuento por Item";
    String CREDIT_NOTE_TYPE_06_DSC = "Devoluci\u00F3n Total";
    String CREDIT_NOTE_TYPE_07_DSC = "Devoluci\u00F3n por Item";
    String CREDIT_NOTE_TYPE_08_DSC = "Bonificaci\u00F3n";
    String CREDIT_NOTE_TYPE_09_DSC = "Disminuci\u00F3n en el Valor";
    String CREDIT_NOTE_TYPE_10_DSC = "Otros Conceptos";

    /**
     * Descripcion de los codigos de tipo de Nota de Debito
     */
    String DEBIT_NOTE_TYPE_01_DSC = "Intereses por Mora";
    String DEBIT_NOTE_TYPE_02_DSC = "Aumento en el Valor";
    String DEBIT_NOTE_TYPE_03_DSC = "Penalidades / Otros Conceptos";

    /**
     * Documentos afectados por las Notas electronicas
     */
    String DOC_INVOICE_DESC = "Factura";
    String DOC_BOLETA_DESC = "Boleta de Venta";
    String DOC_MACHINE_TICKET_DESC = "Ticket de Maquina Registradora";
    String DOC_FINANCIAL_BANKS_DESC = "Documentos emitidos por Bancos, Instituciones Financieras, etc.";
    String DOC_BANK_INSURANCE_DESC = "De Banca y Seguros";
    String DOC_ISSUED_BY_AFP_DESC = "Documento emitidos por las AFP";

    /**
     * Parametros de los templates
     */
    String CODIGO_EMBARQUE = "CODIGO_EMBARQUE";
    String CODIGO_MOTIVO = "CODIGO_MOTIVO";
    String DESCRIPCION_MOTIVO = "DESCRIPCION_MOTIVO";
    String DIRECCION_DESTINO = "DIRECCION_DESTINO";
    String DIRECCION_PARTIDA = "DIRECCION_PARTIDA";
    String DOCUMENTO_CONDUCTOR = "DOCUMENTO_CONDUCTOR";
    String TIPO_DOCUMENTO_CONDUCTOR = "TIPO_DOCUMENTO_CONDUCTOR";
    String FECHA_EMISION = "FECHA_EMISION";
    String FECHA_TRASLADO = "FECHA_TRASLADO";
    String MODALIDAD_TRASLADO = "MODALIDAD_TRASLADO";
    String NOMBRE_CONSUMIDOR = "NOMBRE_CONSUMIDOR";
    String NOMBRE_EMISOR = "NOMBRE_EMISOR";
    String NOMBRE_TRANSPORTISTA = "NOMBRE_TRANSPORTISTA";
    String TIPO_DOCUMENTO_TRANSPORTISTA = "TIPO_DOCUMENTO_TRANSPORTISTA";
    String RUC_TRANSPORTISTA = "RUC_TRANSPORTISTA";
    String NUMERO_BULTOS = "NUMERO_BULTOS";
    String PLACA_VEHICULO = "PLACA_VEHICULO";
    String UNIDAD_MEDIDA_PESONETO = "UNIDAD_MEDIDA_PESONETO";


    String DOCUMENT_IDENTIFIER = "DOCUMENT_IDENTIFIER";
    String ISSUE_DATE = "ISSUE_DATE";
    String DUE_DATE = "DUE_DATE";
    String CURRENCY_VALUE = "CURRENCY_VALUE";
    String OPERATION_TYPE_LABEL = "OPERATION_TYPE_LABEL";
    String OPERATION_TYPE_DSC = "Tip. Operacion:";
    String OPERATION_TYPE_VALUE = "OPERATION_TYPE_VALUE";
    String PAYMENT_CONDITION = "CONDICION_PAGO";
    String SELL_ORDER = "ORDEN_VENTA";
    String SELLER_NAME = "VENDEDOR";
    String REMISSION_GUIDE = "GUIAS";
    // String REFERENCIA = "REFERENCIA";
    String PORCIGV = "IGV";
    String PORCISC = "ISC";

    String CREDIT_NOTE_TYPE_VALUE = "CREDIT_NOTE_TYPE_VALUE";
    String CREDIT_NOTE_DESC_VALUE = "CREDIT_NOTE_DESC_VALUE";
    String DEBIT_NOTE_TYPE_VALUE = "DEBIT_NOTE_TYPE_VALUE";
    String DEBIT_NOTE_DESC_VALUE = "DEBIT_NOTE_DESC_VALUE";
    String REFERENCE_DOC_VALUE = "REFERENCE_DOC_VALUE";
    String DATE_REFERENCE_DOC_VALUE = "DATE_REFERENCE_DOC_VALUE";

    String SENDER_SOCIAL_REASON = "SENDER_SOCIAL_REASON";
    String SENDER_RUC = "SENDER_RUC";
    String SENDER_FISCAL_ADDRESS = "SENDER_FISCAL_ADDRESS";
    String SENDER_DEP_PROV_DIST = "SENDER_DEP_PROV_DIST";
    String SENDER_CONTACT = "SENDER_CONTACT";
    String SENDER_MAIL = "SENDER_MAIL";
    String SENDER_LOGO_PATH = "SENDER_LOGO_PATH";
    String LICENCIA_CONDUCIR = "LICENCIA_CONDUCIR";
    String SENDER_TEL = "TEL_VALUE";
    String SENDER_TEL_1 = "TEL_VALUE_1";
    String SENDER_WEB = "WEB_VALUE";
    String COMMENTS = "COMMENTS";
    String ANTICIPO_APLICADO = "ANTICIPO_APLICADO";
    String TOTAL_DOCUMENTO = "TOTAL_DOC_VALUE";
    String DIGESTVALUE = "DIGESTVALUE";
    String CODEQR = "CODEQR";


    String RUC_CONSUMIDOR = "RUC_CONSUMIDOR";
    String RUC_EMISOR = "RUC_EMISOR";

    String RECEIVER_SOCIAL_REASON = "RECEIVER_SOCIAL_REASON";
    String RECEIVER_FULLNAME = "RECEIVER_FULLNAME";
    String RECEIVER_REGISTRATION_NAME = "RECEIVER_REGISTRATION_NAME";
    String RECEIVER_RUC = "RECEIVER_RUC";
    String RECEIVER_IDENTIFIER = "RECEIVER_IDENTIFIER";
    String RECEIVER_IDENTIFIER_TYPE = "RECEIVER_IDENTIFIER_TYPE";
    String RECEIVER_FISCAL_ADDRESS = "RECEIVER_FISCAL_ADDRESS";

    String VALIDEZPDF = "VALIDEZ_PDF";
    String REGIMENRET = "REGIMEN_RET";
    String IMPORTETOTALDOC = "IMPORTE_TOTAL_DOC";
    String PERCENTAGE_PERCEPTION = "PERCENTAGE_PERCEPTION";

    String AMOUNT_PERCEPTION = "AMOUNT_PERCEPTION";

    String PREPAID_AMOUNT_VALUE = "PREPAID_VALUE";
    String SUBTOTAL_VALUE = "SUBTOTAL_VALUE";
    String IGV_VALUE = "IGV_VALUE";
    String ISC_VALUE = "ISC_VALUE";
    String AMOUNT_VALUE = "AMOUNT_VALUE";
    String DISCOUNT_VALUE = "DISCOUNT_VALUE";
    String TOTAL_AMOUNT_VALUE = "TOTAL_AMOUNT_VALUE";
    String TOTAL_AMOUNT_VALUE_SOLES = "TOTAL_AMOUNT_VALUE_SOLES";
    String GRAVADA_AMOUNT_VALUE = "GRAVADA_AMOUNT_VALUE";
    String EXONERADA_AMOUNT_VALUE = "EXONERADA_AMOUNT_VALUE";
    String INAFECTA_AMOUNT_VALUE = "INAFECTA_AMOUNT_VALUE";
    String GRATUITA_AMOUNT_LABEL = "GRATUITA_AMOUNT_LABEL";
    String GRATUITA_AMOUNT_LABEL_DSC = "Total Op. Gratuita:";
    String GRATUITA_AMOUNT_VALUE = "GRATUITA_AMOUNT_VALUE";
    String NEW_TOTAL_VALUE = "NEW_TOTAL";
    String IMPUESTO_BOLSA = "IMPUESTO_BOLSA";
    String IMPUESTO_BOLSA_MONEDA = "IMPUESTO_BOLSA_MONEDA";
    String IMPORTE_TEXTO = "IMPORTE_TEXTO";

    String BARCODE_VALUE = "BARCODE_VALUE";
    String LETTER_AMOUNT_VALUE = "LETTER_AMOUNT_VALUE";

    String SUBREPORT_LEGENDS_DIR = "SUBREPORT_LEGENDS_DIR";
    String SUBREPORT_LEGENDS_DATASOURCE = "SUBREPORT_LEGENDS_DATASOURCE";
    String SUBREPORT_LEGENDS_MAP = "SUBREPORT_LEGENDS_MAP";

    String SUBREPORT_CUOTAS_MAP = "SUBREPORT_CUOTAS_MAP"; // VALORES PARA SUBREPORTE DE CUOTAS

    String SUBREPORT_PAYMENTS_DIR = "SUBREPORT_PAYMENTS_DIR";
    String SUBREPORT_PAYMENTS_DATASOURCE = "SUBREPORT_PAYMENTS_DATASOURCE";

    String LEGEND_DOCUMENT_TYPE = "LEGEND_DOCUMENT_TYPE";
    String RESOLUTION_CODE_VALUE = "RESOLUTION_CODE_VALUE";

    /* Valores agregados a la leyenda del TEMPLATE */ String LEGEND_INVOICE_DOCUMENT = "factura electr\u00F3nica";
    String LEGEND_BOLETA_DOCUMENT = "boleta de venta electr\u00F3nica";
    String LEGEND_CREDIT_NOTE_DOCUMENT = "nota de cr\u00E9dito electr\u00F3nica";
    String LEGEND_DEBIT_NOTE_DOCUMENT = "nota de d\u00E9bito electr\u00F3nica";
    String LEGEND_PERCEPTION_DOCUMENT = "comprobante de percepcion";
} // IPDFCreatorConfig
