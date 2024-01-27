package service.cloud.request.clientRequest.proxy.sunat.config;

/**
 * Esta interfaz contiene todos los tipos de errores considerados por Sunat,
 * segun su 'Anexo N02 - Listado de errores - MANUAL DEL PROGRAMADOR'.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public interface ISunatError {

    /**
     * Tipos de ERRORES.
     * <p>
     * - SUNAT_ERROR_USER		: Error producido por causa del USUARIO.
     * - SUNAT_ERROR_INTERNAL	: Error interno producido por Sunat.
     * - SUNAT_ERROR_EXTERNAL	: Error exteno a Sunat.
     * <p>
     * NOTA: Estos tipos de errores no son criterio de SUNAT.
     */
    String SUNAT_ERROR_USER = "SUNAT_ERROR_USER";
    String SUNAT_ERROR_INTERNAL = "SUNAT_ERROR_INTERNAL";
    String SUNAT_ERROR_EXTERNAL = "SUNAT_ERROR_EXTERNAL";


    /**
     * List of error messages by Sunat (Code and message)
     */
    String ERR_0100_CODE = "0100";
    String ERR_0100_MESSAGE = "El sistema no puede responder su solicitud. Intente nuevamente o comun\u00edquese con su Administrador.";

    String ERR_0101_CODE = "0101";
    String ERR_0101_MESSAGE = "El encabezado de seguridad es incorrecto.";

    String ERR_0102_CODE = "0102";
    String ERR_0102_MESSAGE = "Usuario o contrase\u00f1a incorrectos.";

    String ERR_0103_CODE = "0103";
    String ERR_0103_MESSAGE = "El Usuario ingresado no existe.";

    String ERR_0104_CODE = "0104";
    String ERR_0104_MESSAGE = "La Clave ingresada es incorrecta.";

    String ERR_0105_CODE = "0105";
    String ERR_0105_MESSAGE = "El Usuario no est\u00e1 activo.";

    String ERR_0106_CODE = "0106";
    String ERR_0106_MESSAGE = "El Usuario no es v\u00e1lido.";

    String ERR_0109_CODE = "0109";
    String ERR_0109_MESSAGE = "El sistema no puede responder su solicitud. (El servicio de autenticaci\u00f3n no est\u00e1 disponible)";

    String ERR_0110_CODE = "0110";
    String ERR_0110_MESSAGE = "No se pudo obtener la informaci\u00f3n del tipo de usuario.";

    String ERR_0111_CODE = "0111";
    String ERR_0111_MESSAGE = "No tiene el perfil para enviar comprobantes electr\u00f3nicos.";

    String ERR_0112_CODE = "0112";
    String ERR_0112_MESSAGE = "El usuario debe ser secundario.";

    String ERR_0113_CODE = "0113";
    String ERR_0113_MESSAGE = "El usuario no esta afiliado a Factura Electr\u00f3nica.";

    String ERR_0125_CODE = "0125";
    String ERR_0125_MESSAGE = "No se pudo obtener la constancia.";

    String ERR_0126_CODE = "0126";
    String ERR_0126_MESSAGE = "El ticket no le pertenece al usuario.";

    String ERR_0127_CODE = "0127";
    String ERR_0127_MESSAGE = "El ticket no existe.";

    String ERR_0130_CODE = "0130";
    String ERR_0130_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo obtener el ticket de proceso)";

    String ERR_0131_CODE = "0131";
    String ERR_0131_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo grabar el archivo en el directorio)";

    String ERR_0132_CODE = "0132";
    String ERR_0132_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo grabar escribir en el archivo zip)";

    String ERR_0133_CODE = "0133";
    String ERR_0133_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo grabar la entrada del log)";

    String ERR_0134_CODE = "0134";
    String ERR_0134_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo grabar en el storage)";

    String ERR_0135_CODE = "0135";
    String ERR_0135_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo encolar el pedido)";

    String ERR_0136_CODE = "0136";
    String ERR_0136_MESSAGE = "El sistema no puede responder su solicitud. (No se pudo recibir una respuesta del batch)";

    String ERR_0137_CODE = "0137";
    String ERR_0137_MESSAGE = "El sistema no puede responder su solicitud. (Se obtuvo una respuesta nula)";

    String ERR_0138_CODE = "0138";
    String ERR_0138_MESSAGE = "El sistema no puede responder su solicitud. (Error en Base de Datos)";

    String ERR_0151_CODE = "0151";
    String ERR_0151_MESSAGE = "El nombre del archivo ZIP es incorrecto.";

    String ERR_0152_CODE = "0152";
    String ERR_0152_MESSAGE = "No se puede enviar por este m\u00e9todo un archivo de resumen.";

    String ERR_0153_CODE = "0153";
    String ERR_0153_MESSAGE = "No se puede enviar por este m\u00e9todo un archivo por lotes";

    String ERR_0154_CODE = "0154";
    String ERR_0154_MESSAGE = "El RUC del archivo no corresponde al RUC del usuario.";

    String ERR_0155_CODE = "0155";
    String ERR_0155_MESSAGE = "El archivo ZIP est\u00e1 vac\u00edo.";

    String ERR_0156_CODE = "0156";
    String ERR_0156_MESSAGE = "El archivo ZIP est\u00e1 corrupto.";

    String ERR_0157_CODE = "0157";
    String ERR_0157_MESSAGE = "El archivo ZIP no contiene comprobantes.";

    String ERR_0158_CODE = "0158";
    String ERR_0158_MESSAGE = "El archivo ZIP contiene demasiados comprobantes para este tipo de env\u00edo.";

    String ERR_0159_CODE = "0159";
    String ERR_0159_MESSAGE = "El nombre del archivo XML es incorrecto.";

    String ERR_0160_CODE = "0160";
    String ERR_0160_MESSAGE = "El archivo XML esta vac\u00edo.";

    String ERR_0161_CODE = "0161";
    String ERR_0161_MESSAGE = "El nombre del archivo XML no coincide con el nombre del archivo ZIP.";

    String ERR_0200_CODE = "0200";
    String ERR_0200_MESSAGE = "No se pudo procesar su solicitud. (Ocurrio un error en el batch)";

    String ERR_0201_CODE = "0201";
    String ERR_0201_MESSAGE = "No se pudo procesar su solicitud. (Llego un requerimiento nulo al batch)";

    String ERR_0202_CODE = "0202";
    String ERR_0202_MESSAGE = "No se pudo procesar su solicitud. (No llego informaci\u00f3n del archivo ZIP)";

    String ERR_0203_CODE = "0203";
    String ERR_0203_MESSAGE = "No se pudo procesar su solicitud. (No se encontr\u00f3 archivos en la informaci\u00f3n del archivo ZIP)";

    String ERR_0204_CODE = "0204";
    String ERR_0204_MESSAGE = "No se pudo procesar su solicitud. (Este tipo de requerimiento solo acepta 1 archivo)";

    String ERR_0250_CODE = "0250";
    String ERR_0250_MESSAGE = "No se pudo procesar su solicitud. (Ocurrio un error desconocido al hacer unzip)";

    String ERR_0251_CODE = "0251";
    String ERR_0251_MESSAGE = "No se pudo procesar su solicitud. (No se pudo crear un directorio para el unzip)";

    String ERR_0252_CODE = "0252";
    String ERR_0252_MESSAGE = "No se pudo procesar su solicitud. (No se encontr\u00f3 archivos dentro del zip)";

    String ERR_0253_CODE = "0253";
    String ERR_0253_MESSAGE = "No se pudo procesar su solicitud. (No se pudo comprimir la constancia)";

    String ERR_0300_CODE = "0300";
    String ERR_0300_MESSAGE = "No se encontr\u00f3 la ra\u00edz documento xml.";

    String ERR_0301_CODE = "0301";
    String ERR_0301_MESSAGE = "Elemento ra\u00edz del xml no esta definido.";

    String ERR_0302_CODE = "0302";
    String ERR_0302_MESSAGE = "C\u00f3digo del tipo de comprobante no registrado.";

    String ERR_0303_CODE = "0303";
    String ERR_0303_MESSAGE = "No existe el directorio de schemas.";

    String ERR_0304_CODE = "0304";
    String ERR_0304_MESSAGE = "No existe el archivo de schema.";

    String ERR_0305_CODE = "0305";
    String ERR_0305_MESSAGE = "El sistema no puede procesar el archivo xml.";

    String ERR_0306_CODE = "0306";
    String ERR_0306_MESSAGE = "No se puede leer (parsear) el archivo XML.";

    String ERR_0307_CODE = "0307";
    String ERR_0307_MESSAGE = "No se pudo recuperar la constancia.";

    String ERR_0400_CODE = "0400";
    String ERR_0400_MESSAGE = "No tiene permiso para enviar casos de pruebas.";

    String ERR_0401_CODE = "0401";
    String ERR_0401_MESSAGE = "El caso de prueba no existe.";

    String ERR_0402_CODE = "0402";
    String ERR_0402_MESSAGE = "La numeraci\u00f3n o nombre del documento ya ha sido enviado anteriormente.";

    String ERR_0403_CODE = "0403";
    String ERR_0403_MESSAGE = "El documento afectado por la nota no existe.";

    String ERR_0404_CODE = "0404";
    String ERR_0404_MESSAGE = "El documento afectado por la nota se encuentra rechazado.";

    String ERR_1001_CODE = "1001";
    String ERR_1001_MESSAGE = "ID - El dato SERIE-CORRELATIVO no cumple con el formato de acuerdo al tipo de comprobante.";

    String ERR_1002_CODE = "1002";
    String ERR_1002_MESSAGE = "El XML no contiene informacion en el tag ID";

    String ERR_1003_CODE = "1003";
    String ERR_1003_MESSAGE = "InvoiceTypeCode - El valor del tipo de documento es inv\u00e1lido o no coincide con el nombre del archivo.";

    String ERR_1004_CODE = "1004";
    String ERR_1004_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de InvoiceTypeCode.";

    String ERR_1005_CODE = "1005";
    String ERR_1005_MESSAGE = "CustomerAssignedAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_1006_CODE = "1006";
    String ERR_1006_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomerAssignedAccountID del emisor del documento.";

    String ERR_1007_CODE = "1007";
    String ERR_1007_MESSAGE = "AdditionalAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_1008_CODE = "1008";
    String ERR_1008_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de AdditionalAccountID del emisor del documento.";

    String ERR_1009_CODE = "1009";
    String ERR_1009_MESSAGE = "IssueDate - El dato ingresado no cumple con el patr\u00f3n YYYY-MM-DD";

    String ERR_1010_CODE = "1010";
    String ERR_1010_MESSAGE = "El XML no contiene el tag IssueDate.";

    String ERR_1011_CODE = "1011";
    String ERR_1011_MESSAGE = "IssueDate - El dato ingresao no es v\u00e1lido.";

    String ERR_1012_CODE = "1012";
    String ERR_1012_MESSAGE = "ID - El dato ingresado no cumple con el patr\u00f3n SERIE-CORRELATIVO.";

    String ERR_1013_CODE = "1013";
    String ERR_1013_MESSAGE = "El XML no contiene informaci\u00f3n en el tag ID.";

    String ERR_1014_CODE = "1014";
    String ERR_1014_MESSAGE = "CustomerAssignedAccountID - El dato ingresado no cumple con el est\u00e1ndar";

    String ERR_1015_CODE = "1015";
    String ERR_1015_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomerAssignedAccountID del emisor del documento.";

    String ERR_1016_CODE = "1016";
    String ERR_1016_MESSAGE = "AdditionalAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_1017_CODE = "1017";
    String ERR_1017_MESSAGE = "El XML no contiene el tag AdditionalAccountID del emisor del documento.";

    String ERR_1018_CODE = "1018";
    String ERR_1018_MESSAGE = "IssueDate - El dato ingresado no cumple con el patr\u00f3n YYYY-MM-DD";

    String ERR_1019_CODE = "1019";
    String ERR_1019_MESSAGE = "El XML no contiene el tag IssueDate.";

    String ERR_1020_CODE = "1020";
    String ERR_1020_MESSAGE = "IssueDate - El dato ingresado no es v\u00e1lido.";

    String ERR_1021_CODE = "1021";
    String ERR_1021_MESSAGE = "Error en la validaci\u00f3n de la nota de cr\u00e9dito.";

    String ERR_1022_CODE = "1022";
    String ERR_1022_MESSAGE = "La serie o n\u00famero del documento modificado por la Nota Electr\u00f3nica no cumple con el formato establecido.";

    String ERR_1023_CODE = "1023";
    String ERR_1023_MESSAGE = "No se ha especificado el tipo de documento modificado por la Nota Electr\u00f3nica.";

    String ERR_1024_CODE = "1024";
    String ERR_1024_MESSAGE = "CustomerAssignedAccountID - El dato ingresado no cumple con el est\u00e1ndar";

    String ERR_1025_CODE = "1025";
    String ERR_1025_MESSAGE = "El XML no contiene el tag o no existe la informaci\u00f3n de CustomerAssignedAccountID del emisor del documento.";

    String ERR_1026_CODE = "1026";
    String ERR_1026_MESSAGE = "AdditionalAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_1027_CODE = "1027";
    String ERR_1027_MESSAGE = "El XML no contiene el tag AdditionalAccountID del emisor del documento.";

    String ERR_1028_CODE = "1028";
    String ERR_1028_MESSAGE = "IssueDate - El dato ingresado no cumple con el patr\u00f3n YYYY-MM-DD";

    String ERR_1029_CODE = "1029";
    String ERR_1029_MESSAGE = "El XML no contiene el tag IssueDate.";

    String ERR_1030_CODE = "1030";
    String ERR_1030_MESSAGE = "IssueDate - El dato ingresado no es v\u00e1lido.";

    String ERR_1031_CODE = "1031";
    String ERR_1031_MESSAGE = "Error en la validaci\u00f3n de la nota de cr\u00e9dito.";

    String ERR_1032_CODE = "1032";
    String ERR_1032_MESSAGE = "El comprobante fue informado previamente en una comunicaci\u00f3n de baja.";

    String ERR_1033_CODE = "1033";
    String ERR_1033_MESSAGE = "El comprobante fue registrado previamente con otros datos.";

    String ERR_1034_CODE = "1034";
    String ERR_1034_MESSAGE = "N\u00famero de RUC del nombre del archivo no coincide con el consignado en el contenido del archivo XML.";

    String ERR_1035_CODE = "1035";
    String ERR_1035_MESSAGE = "N\u00famero de Serie del nombre del archivo no coincide con el consignado en el contenido del archivo XML.";

    String ERR_1036_CODE = "1036";
    String ERR_1036_MESSAGE = "N\u00famero de documento en el nombre del archivo no coincide con el consignado en el contenido del XML.";

    String ERR_1037_CODE = "1037";
    String ERR_1037_MESSAGE = "El XML no contiene el tag o no existe informacion de RegistrationName del emisor del documento.";

    String ERR_1038_CODE = "1038";
    String ERR_1038_MESSAGE = "RegistrationName - El nombre o raz\u00f3n social del emisor no cumple con el est\u00e1ndar.";

    String ERR_1039_CODE = "1039";
    String ERR_1039_MESSAGE = "Solo se pueden recibir notas electr\u00f3nicas que modifiquen facturas.";

    String ERR_1040_CODE = "1040";
    String ERR_1040_MESSAGE = "El tipo de documento modificado por la nota electr\u00f3nica no es v\u00e1lido.";

    String ERR_2010_CODE = "2010";
    String ERR_2010_MESSAGE = "El contribuyente no est\u00e1 activo.";

    String ERR_2011_CODE = "2011";
    String ERR_2011_MESSAGE = "El contribuyente no est\u00e1 habido.";

    String ERR_2012_CODE = "2012";
    String ERR_2012_MESSAGE = "El contribuyente no est\u00e1 autorizado a emitir comprobantes electr\u00f3nicos.";

    String ERR_2013_CODE = "2013";
    String ERR_2013_MESSAGE = "El contribuyente no cumple con tipo de empresa o tributos requeridos.";

    String ERR_2014_CODE = "2014";
    String ERR_2014_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomerAssignedAccountID del receptor del documento.";

    String ERR_2015_CODE = "2015";
    String ERR_2015_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de AdditionalAccountID del receptor del documento.";

    String ERR_2016_CODE = "2016";
    String ERR_2016_MESSAGE = "AdditionalAcountID - El dato ingresado en el tipo de documento de identidad del receptor no cumple con el est\u00e1ndar.";

    String ERR_2017_CODE = "2017";
    String ERR_2017_MESSAGE = "CustomerAssignedAccountID - El n\u00famero de documento de identidad del receptor debe ser RUC.";

    String ERR_2018_CODE = "2018";
    String ERR_2018_MESSAGE = "CustomerAssignedAccountID -  El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2019_CODE = "2019";
    String ERR_2019_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de RegistrationName del emisor del documento.";

    String ERR_2020_CODE = "2020";
    String ERR_2020_MESSAGE = "RegistrationName - El nombre o raz\u00f3n social del emisor no cumple con el est\u00e1ndar.";

    String ERR_2021_CODE = "2021";
    String ERR_2021_MESSAGE = "EL XML no contiene el tag o no existe informaci\u00f3n de RegistrationName del receptor del documento.";

    String ERR_2022_CODE = "2022";
    String ERR_2022_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2023_CODE = "2023";
    String ERR_2023_MESSAGE = "El N\u00famero de orden del item no cumple con el formato establecido.";

    String ERR_2024_CODE = "2024";
    String ERR_2024_MESSAGE = "El XML no contiene el tag InvoiceQuantity en el detalle de los items.";

    String ERR_2025_CODE = "2025";
    String ERR_2025_MESSAGE = "InvoiceQuantity El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2026_CODE = "2026";
    String ERR_2026_MESSAGE = "El XML no contiene el tag cac:Item/cbc:Description en el detalle de los Items.";

    String ERR_2027_CODE = "2027";
    String ERR_2027_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de cac:Item/cbc:Description del item";

    String ERR_2028_CODE = "2028";
    String ERR_2028_MESSAGE = "Debe existir el tag cac:AlternativeConditionPrice con un elemento cbc:PriceTypeCode con valor 01.";

    String ERR_2029_CODE = "2029";
    String ERR_2029_MESSAGE = "PriceTypeCode El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2030_CODE = "2030";
    String ERR_2030_MESSAGE = "El XML no contiene el tag cbc:PriceTypeCode";

    String ERR_2031_CODE = "2031";
    String ERR_2031_MESSAGE = "LineExtensionAmount El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2032_CODE = "2032";
    String ERR_2032_MESSAGE = "El XML no contiene el tag LineExtensionAmount en el detalle de los Items.";

    String ERR_2033_CODE = "2033";
    String ERR_2033_MESSAGE = "El dato ingresado en TaxAmount de la linea no cumple con el formato establecido.";

    String ERR_2034_CODE = "2034";
    String ERR_2034_MESSAGE = "TaxAmount es obligatorio.";

    String ERR_2035_CODE = "2035";
    String ERR_2035_MESSAGE = "cac:TaxCategory/cac:TaxScheme/cbc:ID El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2036_CODE = "2036";
    String ERR_2036_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2037_CODE = "2037";
    String ERR_2037_MESSAGE = "El XML no contiene el tag cac:TaxCategory/cac:TaxScheme/cbc:ID del Item.";

    String ERR_2038_CODE = "2038";
    String ERR_2038_MESSAGE = "cac:TaxScheme/cbc:Name del item - No existe el tag o el dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2039_CODE = "2039";
    String ERR_2039_MESSAGE = "El XML no contiene el tag cac:TaxCategory/cac:TaxScheme/cbc:Name del Item.";

    String ERR_2040_CODE = "2040";
    String ERR_2040_MESSAGE = "El tipo de afectaci\u00f3n del IGV es incorrecto.";

    String ERR_2041_CODE = "2041";
    String ERR_2041_MESSAGE = "El sistema de c\u00e1lculo de ISC es incorrecto.";

    String ERR_2042_CODE = "2042";
    String ERR_2042_MESSAGE = "Debe indicar el IGV. Es un campo obligatorio.";

    String ERR_2043_CODE = "2043";
    String ERR_2043_MESSAGE = "El dato ingresado en PayableAmount no cumple con el formato establecido.";

    String ERR_2044_CODE = "2044";
    String ERR_2044_MESSAGE = "PayableAmount es obligatorio.";

    String ERR_2045_CODE = "2045";
    String ERR_2045_MESSAGE = "El valor ingresado en AdditionalMonetaryTotal/cbc:ID es incorrecto.";

    String ERR_2046_CODE = "2046";
    String ERR_2046_MESSAGE = "AdditionalMonetaryTotal/cbc:ID debe tener valor.";

    String ERR_2047_CODE = "2047";
    String ERR_2047_MESSAGE = "Es obligatorio al menos un AdditionalMonetaryTotal con c\u00f3digo 1001, 1002, 1003.";

    String ERR_2048_CODE = "2048";
    String ERR_2048_MESSAGE = "El dato ingresado en TaxAmount no cumple con el formato establecido.";

    String ERR_2049_CODE = "2049";
    String ERR_2049_MESSAGE = "TaxAmount es obligatorio.";

    String ERR_2050_CODE = "2050";
    String ERR_2050_MESSAGE = "TaxScheme ID - No existe el tag o el dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2051_CODE = "2051";
    String ERR_2051_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2052_CODE = "2052";
    String ERR_2052_MESSAGE = "El XML no contiene el tag TaxScheme ID de impuestos globales.";

    String ERR_2053_CODE = "2053";
    String ERR_2053_MESSAGE = "TaxScheme Name - No existe el tag o el dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2054_CODE = "2054";
    String ERR_2054_MESSAGE = "El XML no contiene el tag TaxScheme Name de impuestos globales.";

    String ERR_2055_CODE = "2055";
    String ERR_2055_MESSAGE = "TaxScheme TaxTypeCode - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2056_CODE = "2056";
    String ERR_2056_MESSAGE = "El XML no contiene el tag TaxScheme TaxTypeCode de impuestos globales.";

    String ERR_2057_CODE = "2057";
    String ERR_2057_MESSAGE = "El Name o TaxTypeCode debe corresponder con el Id para el IGV.";

    String ERR_2058_CODE = "2058";
    String ERR_2058_MESSAGE = "El Name o TaxTypeCode debe corresponder con el Id para el ISC.";

    String ERR_2059_CODE = "2059";
    String ERR_2059_MESSAGE = "El dato ingresado en TaxSubtotal/cbc:TaxAmount no cumple con el formato establecido.";

    String ERR_2060_CODE = "2060";
    String ERR_2060_MESSAGE = "TaxSubtotal/cbc:TaxAmount es obligatorio.";

    String ERR_2061_CODE = "2061";
    String ERR_2061_MESSAGE = "El tag global cac:TaxTotal/cbc:TaxAmount debe tener el mismo valor que cac:TaxTotal/cac:Subtotal/cbc:TaxAmount";

    String ERR_2062_CODE = "2062";
    String ERR_2062_MESSAGE = "El dato ingresado en PayableAmount no cumple con el formato establecido.";

    String ERR_2063_CODE = "2063";
    String ERR_2063_MESSAGE = "El XML no contiene el tag PayableAmount.";

    String ERR_2064_CODE = "2064";
    String ERR_2064_MESSAGE = "El dato ingresado en ChargeTotalAmount no cumple con el formato establecido.";

    String ERR_2065_CODE = "2065";
    String ERR_2065_MESSAGE = "El dato ingresado en AllowanceTotalAmount no cumple con el formato establecido.";

    String ERR_2066_CODE = "2066";
    String ERR_2066_MESSAGE = "Debe indicar una descripci\u00f3n para el tag sac:AdditionalProperty/cbc:Value";

    String ERR_2067_CODE = "2067";
    String ERR_2067_MESSAGE = "cac:Price/cbc:PriceAmount - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2068_CODE = "2068";
    String ERR_2068_MESSAGE = "El XML no contiene el tag cac:Price/cbc:PriceAmount en el detalle de los Items.";

    String ERR_2069_CODE = "2069";
    String ERR_2069_MESSAGE = "DocumentCurrencyCode - El dato ingresado no cumple con la estructura.";

    String ERR_2070_CODE = "2070";
    String ERR_2070_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de DocumentCurrencyCode.";

    String ERR_2071_CODE = "2071";
    String ERR_2071_MESSAGE = "La moneda debe ser la misma en todo el documento.";

    String ERR_2072_CODE = "2072";
    String ERR_2072_MESSAGE = "CustomizationID - La versi\u00f3n del documento no es la correcta.";

    String ERR_2073_CODE = "2073";
    String ERR_2073_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomizationID.";

    String ERR_2074_CODE = "2074";
    String ERR_2074_MESSAGE = "UBLVersionID - La versi\u00f3n del UBL no es correcta.";

    String ERR_2075_CODE = "2075";
    String ERR_2075_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de UBLVersionID";

    String ERR_2076_CODE = "2076";
    String ERR_2076_MESSAGE = "cac:Signature/cbc:ID - Falta el identificador de la firma.";

    String ERR_2077_CODE = "2077";
    String ERR_2077_MESSAGE = "El tag cac:Signature/cbc:ID debe contener informaci\u00f3n.";

    String ERR_2078_CODE = "2078";
    String ERR_2078_MESSAGE = "cac:Signature/cac:SignatoryParty/cac:PartyIdentification/cbc:ID - Debe ser igual al RUC del emisor.";

    String ERR_2079_CODE = "2079";
    String ERR_2079_MESSAGE = "El XML no contiene el tag cac:Signature/cac:SignatoryParty/cac:PartyIdentification/cbc:ID";

    String ERR_2080_CODE = "2080";
    String ERR_2080_MESSAGE = "cac:Signature/cac:SignatoryParty/cac:PartyName/cbc:Name - No cumple con el est\u00e1ndar.";

    String ERR_2081_CODE = "2081";
    String ERR_2081_MESSAGE = "El XML no contiene el tag cac:Signature/cac:SignatoryParty/cac:PartyName/cbc:Name";

    String ERR_2082_CODE = "2082";
    String ERR_2082_MESSAGE = "cac:Signature/cac:DigitalSignatureAttachment/cac:ExternalReference/cbc:URI - No cumple con el est\u00e1ndar.";

    String ERR_2083_CODE = "2083";
    String ERR_2083_MESSAGE = "El XML no contiene el tag cac:Signature/cac:DigitalSignatureAttachment/cac:ExternalReference/cbc:URI.";

    String ERR_2084_CODE = "2084";
    String ERR_2084_MESSAGE = "ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/ds:Signature/@Id - No cumple con el est\u00e1ndar.";

    String ERR_2085_CODE = "2085";
    String ERR_2085_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/ds:Signature/@Id.";

    String ERR_2086_CODE = "2086";
    String ERR_2086_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:CanonicalizationMethod/@Algorithm - No cumple con el est\u00e1ndar.";

    String ERR_2087_CODE = "2087";
    String ERR_2087_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:CanonicalizationMethod/@Algorithm.";

    String ERR_2088_CODE = "2088";
    String ERR_2088_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:SignatureMethod/@Algorithm - No cumple con el est\u00e1ndar.";

    String ERR_2089_CODE = "2089";
    String ERR_2089_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:SignatureMethod/@Algorithm.";

    String ERR_2090_CODE = "2090";
    String ERR_2090_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/@URI - Debe estar vac\u00edo para id.";

    String ERR_2091_CODE = "2091";
    String ERR_2091_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/@URI";

    String ERR_2092_CODE = "2092";
    String ERR_2092_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/.../ds:Transform@Algorithm - No cumple con el est\u00e1ndar.";

    String ERR_2093_CODE = "2093";
    String ERR_2093_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/ds:Transform@Algorithm.";

    String ERR_2094_CODE = "2094";
    String ERR_2094_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestMethod/@Algorithm - No cumple con el est\u00e1ndar.";

    String ERR_2095_CODE = "2095";
    String ERR_2095_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestMethod/@Algorithm.";

    String ERR_2096_CODE = "2096";
    String ERR_2096_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestValue - No cumple con el est\u00e1ndar.";

    String ERR_2097_CODE = "2097";
    String ERR_2097_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestValue.";

    String ERR_2098_CODE = "2098";
    String ERR_2098_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:SignatureValue - No cumple con el est\u00e1ndar.";

    String ERR_2099_CODE = "2099";
    String ERR_2099_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:SignatureValue.";

    String ERR_2100_CODE = "2100";
    String ERR_2100_MESSAGE = "ext:UBLExtensions/.../ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate - No cumple con el est\u00e1ndar.";

    String ERR_2101_CODE = "2101";
    String ERR_2101_MESSAGE = "El XML no contiene el tag ext:UBLExtensions/.../ds:Signature/ds:KeyInfo/ds:X509Data/ds:X509Certificate.";

    String ERR_2102_CODE = "2102";
    String ERR_2102_MESSAGE = "Error al procesar la factura.";

    String ERR_2103_CODE = "2103";
    String ERR_2103_MESSAGE = "La serie ingresada no es v\u00e1lida.";

    String ERR_2104_CODE = "2104";
    String ERR_2104_MESSAGE = "N\u00famero de RUC del emisor no existe.";

    String ERR_2105_CODE = "2105";
    String ERR_2105_MESSAGE = "Factura a dar de baja no se encuentra registrada en SUNAT.";

    String ERR_2106_CODE = "2106";
    String ERR_2106_MESSAGE = "Factura a dar de baja ya se encuentra en estado de baja.";

    String ERR_2107_CODE = "2107";
    String ERR_2107_MESSAGE = "N\u00famero de RUC SOL no coincide con RUC emisor.";

    String ERR_2108_CODE = "2108";
    String ERR_2108_MESSAGE = "Presentaci\u00f3n fuera de fecha.";

    String ERR_2109_CODE = "2109";
    String ERR_2109_MESSAGE = "El comprobante fue registrado previamente con otros datos.";

    String ERR_2110_CODE = "2110";
    String ERR_2110_MESSAGE = "UBLVersionID - La versi\u00f3n del UBL no es correcta.";

    String ERR_2111_CODE = "2111";
    String ERR_2111_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de UBLVersionID.";

    String ERR_2112_CODE = "2112";
    String ERR_2112_MESSAGE = "CustomizationID - La versi\u00f3n del documento no es correcta.";

    String ERR_2113_CODE = "2113";
    String ERR_2113_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomizationID.";

    String ERR_2114_CODE = "2114";
    String ERR_2114_MESSAGE = "DocumentCurrencyCode -  El dato ingresado no cumple con la estructura.";

    String ERR_2115_CODE = "2115";
    String ERR_2115_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de DocumentCurrencyCode.";

    String ERR_2116_CODE = "2116";
    String ERR_2116_MESSAGE = "El tipo de documento modificado por la Nota de cr\u00e9dito debe ser factura electr\u00f3nica o ticket.";

    String ERR_2117_CODE = "2117";
    String ERR_2117_MESSAGE = "La serie o n\u00famero del documento modificado por la Nota de Cr\u00e9dito no cumple con el formato establecido.";

    String ERR_2118_CODE = "2118";
    String ERR_2118_MESSAGE = "Debe indicar las facturas relacionadas a la Nota de Cr\u00e9dito.";

    String ERR_2119_CODE = "2119";
    String ERR_2119_MESSAGE = "La factura relacionada en la nota de cr\u00e9dito no esta registrada.";

    String ERR_2120_CODE = "2120";
    String ERR_2120_MESSAGE = "La factura relacionada en la nota de cr\u00e9dito  se encuentra de baja.";

    String ERR_2121_CODE = "2121";
    String ERR_2121_MESSAGE = "La factura relacionada en la nota de cr\u00e9dito est\u00e1 registrada como rechazada.";

    String ERR_2122_CODE = "2122";
    String ERR_2122_MESSAGE = "El tag cac:LegalMonetaryTotal/cbc:PayableAmount debe tener informaci\u00f3n v\u00e1lida.";

    String ERR_2123_CODE = "2123";
    String ERR_2123_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2124_CODE = "2124";
    String ERR_2124_MESSAGE = "El XML no contiene el tag RegistrationName del emisor del documento.";

    String ERR_2125_CODE = "2125";
    String ERR_2125_MESSAGE = "ReferenceID - El dato ingresado debe indicar SERIE-CORRELATIVO del documento al que se relaciona la Nota.";

    String ERR_2126_CODE = "2126";
    String ERR_2126_MESSAGE = "El XML no contiene informacion en el tag ReferenceID del documento al que se relaciona la nota.";

    String ERR_2127_CODE = "2127";
    String ERR_2127_MESSAGE = "ResponseCode - El dato ingresado no cumple con la estructura.";

    String ERR_2128_CODE = "2128";
    String ERR_2128_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de ResponseCode.";

    String ERR_2129_CODE = "2129";
    String ERR_2129_MESSAGE = "AdditionalAccountID - El dato ingresado en el tipo de documento de identidad del receptor no cumple con el est\u00e1ndar.";

    String ERR_2130_CODE = "2130";
    String ERR_2130_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de AdditionalAccountID del receptor del documento.";

    String ERR_2131_CODE = "2131";
    String ERR_2131_MESSAGE = "CustomerAssignedAccountID - El n\u00famero de documento de identidad del receptor debe ser RUC.";

    String ERR_2132_CODE = "2132";
    String ERR_2132_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomerAssignedAccountID del receptor del documento.";

    String ERR_2133_CODE = "2133";
    String ERR_2133_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2134_CODE = "2134";
    String ERR_2134_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de RegistrationName del receptor del documento.";

    String ERR_2135_CODE = "2135";
    String ERR_2135_MESSAGE = "cac:DiscrepancyResponse/cbc:Description - El dato ingresado no cumple con la estructura.";

    String ERR_2136_CODE = "2136";
    String ERR_2136_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de cac:DiscrepancyResponse/cbc:Description.";

    String ERR_2137_CODE = "2137";
    String ERR_2137_MESSAGE = "El n\u00famero de orden del item no cumple con el formato establecido.";

    String ERR_2138_CODE = "2138";
    String ERR_2138_MESSAGE = "CreditedQuantity/@unitCode - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2139_CODE = "2139";
    String ERR_2139_MESSAGE = "CreditedQuantity - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2140_CODE = "2140";
    String ERR_2140_MESSAGE = "El PriceTypeCode debe tener el valor 01.";

    String ERR_2141_CODE = "2141";
    String ERR_2141_MESSAGE = "cac:TaxCategory/cac:TaxScheme/cbc:ID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2142_CODE = "2142";
    String ERR_2142_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2143_CODE = "2143";
    String ERR_2143_MESSAGE = "cac:TaxScheme/cbc:Name del item - No existe el tag o el dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2144_CODE = "2144";
    String ERR_2144_MESSAGE = "cac:TaxCategory/cac:TaxScheme/cbc:TaxTypeCode El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2145_CODE = "2145";
    String ERR_2145_MESSAGE = "El tipo de afectaci\u00f3n del IGV es incorrecto.";

    String ERR_2146_CODE = "2146";
    String ERR_2146_MESSAGE = "El Nombre Internacional debe ser VAT.";

    String ERR_2147_CODE = "2147";
    String ERR_2147_MESSAGE = "El sistema de calculo del ISC es incorrecto.";

    String ERR_2148_CODE = "2148";
    String ERR_2148_MESSAGE = "El Nombre Internacional debe ser EXC.";

    String ERR_2149_CODE = "2149";
    String ERR_2149_MESSAGE = "El dato ingresado en PayableAmount no cumple con el formato establecido.";

    String ERR_2150_CODE = "2150";
    String ERR_2150_MESSAGE = "El valor ingresado en AdditionalMonetaryTotal/cbc:ID es incorrecto.";

    String ERR_2151_CODE = "2151";
    String ERR_2151_MESSAGE = "AdditionalMonetaryTotal/cbc:ID debe tener valor.";

    String ERR_2152_CODE = "2152";
    String ERR_2152_MESSAGE = "Es obligatorio al menos un AdditionalInformation.";

    String ERR_2153_CODE = "2153";
    String ERR_2153_MESSAGE = "Error al procesar la Nota de Cr\u00e9dito.";

    String ERR_2154_CODE = "2154";
    String ERR_2154_MESSAGE = "TaxAmount - El dato ingresado en impuestos globales no cumple con el est\u00e1ndar.";

    String ERR_2155_CODE = "2155";
    String ERR_2155_MESSAGE = "TaxAmount - El XML no contiene el tag TaxAmount de impuestos globales.";

    String ERR_2156_CODE = "2156";
    String ERR_2156_MESSAGE = "TaxScheme ID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2157_CODE = "2157";
    String ERR_2157_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2158_CODE = "2158";
    String ERR_2158_MESSAGE = "El XML no contiene el tag o no existe informacion de TaxScheme ID de impuestos globales.";

    String ERR_2159_CODE = "2159";
    String ERR_2159_MESSAGE = "TaxScheme Name - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2160_CODE = "2160";
    String ERR_2160_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de TaxScheme Name de impuestos globales.";

    String ERR_2161_CODE = "2161";
    String ERR_2161_MESSAGE = "CustomizationID - La versi\u00f3n del documento no es correcta.";

    String ERR_2162_CODE = "2162";
    String ERR_2162_MESSAGE = "El XML no contiene el tag o no existe informacion de CustomizationID.";

    String ERR_2163_CODE = "2163";
    String ERR_2163_MESSAGE = "UBLVersionID - La versi\u00f3n del UBL no es correcta.";

    String ERR_2164_CODE = "2164";
    String ERR_2164_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de UBLVersionID.";

    String ERR_2165_CODE = "2165";
    String ERR_2165_MESSAGE = "Error al procesar la Nota de D\u00e9bito.";

    String ERR_2166_CODE = "2166";
    String ERR_2166_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2167_CODE = "2167";
    String ERR_2167_MESSAGE = "El XML no contiene el tag RegistrationName del emisor del documento.";

    String ERR_2168_CODE = "2168";
    String ERR_2168_MESSAGE = "DocumentCurrencyCode -  El dato ingresado no cumple con el formato establecido.";

    String ERR_2169_CODE = "2169";
    String ERR_2169_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de DocumentCurrencyCode.";

    String ERR_2170_CODE = "2170";
    String ERR_2170_MESSAGE = "ReferenceID - El dato ingresado debe indicar SERIE-CORRELATIVO del documento al que se relaciona la Nota.";

    String ERR_2171_CODE = "2171";
    String ERR_2171_MESSAGE = "El XML no contiene informaci\u00f3n en el tag ReferenceID del documento al que se relaciona la nota.";

    String ERR_2172_CODE = "2172";
    String ERR_2172_MESSAGE = "ResponseCode - El dato ingresado no cumple con la estructura.";

    String ERR_2173_CODE = "2173";
    String ERR_2173_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de ResponseCode.";

    String ERR_2174_CODE = "2174";
    String ERR_2174_MESSAGE = "cac:DiscrepancyResponse/cbc:Description - El dato ingresado no cumple con la estructura.";

    String ERR_2175_CODE = "2175";
    String ERR_2175_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de cac:DiscrepancyResponse/cbc:Description.";

    String ERR_2176_CODE = "2176";
    String ERR_2176_MESSAGE = "AdditionalAccountID - El dato ingresado en el tipo de documento de identidad del receptor no cumple con el est\u00e1ndar.";

    String ERR_2177_CODE = "2177";
    String ERR_2177_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de AdditionalAccountID del receptor del documento.";

    String ERR_2178_CODE = "2178";
    String ERR_2178_MESSAGE = "CustomerAssignedAccountID - El n\u00famero de documento de identidad del receptor debe ser RUC.";

    String ERR_2179_CODE = "2179";
    String ERR_2179_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de CustomerAssignedAccountID del receptor del documento.";

    String ERR_2180_CODE = "2180";
    String ERR_2180_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2181_CODE = "2181";
    String ERR_2181_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de RegistrationName del receptor del documento.";

    String ERR_2182_CODE = "2182";
    String ERR_2182_MESSAGE = "TaxScheme ID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2183_CODE = "2183";
    String ERR_2183_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2184_CODE = "2184";
    String ERR_2184_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de TaxScheme ID de impuestos globales.";

    String ERR_2185_CODE = "2185";
    String ERR_2185_MESSAGE = "TaxScheme Name - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2186_CODE = "2186";
    String ERR_2186_MESSAGE = "El XML no contiene el tag o no existe informaci\u00f3n de TaxScheme Name de impuestos globales.";

    String ERR_2187_CODE = "2187";
    String ERR_2187_MESSAGE = "El n\u00famero de orden del item no cumple con el formato establecido.";

    String ERR_2188_CODE = "2188";
    String ERR_2188_MESSAGE = "DebitedQuantity/@unitCode El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2189_CODE = "2189";
    String ERR_2189_MESSAGE = "DebitedQuantity El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2190_CODE = "2190";
    String ERR_2190_MESSAGE = "El XML no contiene el tag Price/cbc:PriceAmount en el detalle de los Items.";

    String ERR_2191_CODE = "2191";
    String ERR_2191_MESSAGE = "El XML no contiene el tag Price/cbc:LineExtensionAmount en el detalle de los Items.";

    String ERR_2192_CODE = "2192";
    String ERR_2192_MESSAGE = "EL PriceTypeCode debe tener el valor 01.";

    String ERR_2193_CODE = "2193";
    String ERR_2193_MESSAGE = "cac:TaxCategory/cac:TaxScheme/cbc:ID El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2194_CODE = "2194";
    String ERR_2194_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2195_CODE = "2195";
    String ERR_2195_MESSAGE = "cac:TaxScheme/cbc:Name del item - No existe el tag o el dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2196_CODE = "2196";
    String ERR_2196_MESSAGE = "cac:TaxCategory/cac:TaxScheme/cbc:TaxTypeCode El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2197_CODE = "2197";
    String ERR_2197_MESSAGE = "El tipo de afectacion del IGV es incorrecto.";

    String ERR_2198_CODE = "2198";
    String ERR_2198_MESSAGE = "El Nombre Internacional debe ser VAT.";

    String ERR_2199_CODE = "2199";
    String ERR_2199_MESSAGE = "El sistema de calculo del ISC es incorrecto.";

    String ERR_2200_CODE = "2200";
    String ERR_2200_MESSAGE = "El Nombre Internacional debe ser EXC.";

    String ERR_2201_CODE = "2201";
    String ERR_2201_MESSAGE = "El tag cac:RequestedMonetaryTotal/cbc:PayableAmount debe tener informaci\u00f3n v\u00e1lida.";

    String ERR_2202_CODE = "2202";
    String ERR_2202_MESSAGE = "TaxAmount - El dato ingresado en impuestos globales no cumple con el est\u00e1ndar.";

    String ERR_2203_CODE = "2203";
    String ERR_2203_MESSAGE = "El XML no contiene el tag TaxAmount de impuestos globales.";

    String ERR_2204_CODE = "2204";
    String ERR_2204_MESSAGE = "El tipo de documento modificado por la Nota de D\u00e9bito debe ser factura electr\u00f3nica o ticket.";

    String ERR_2205_CODE = "2205";
    String ERR_2205_MESSAGE = "La serie o n\u00famero del documento modificado por la Nota de D\u00e9bito no cumple con el formato establecido.";

    String ERR_2206_CODE = "2206";
    String ERR_2206_MESSAGE = "Debe indicar los documentos afectados por la Nota de D\u00e9bito.";

    String ERR_2207_CODE = "2207";
    String ERR_2207_MESSAGE = "La factura relacionada en la nota de d\u00e9bito se encuentra de baja.";

    String ERR_2208_CODE = "2208";
    String ERR_2208_MESSAGE = "La factura relacionada en la nota de d\u00e9bito esta registrada como rechazada.";

    String ERR_2209_CODE = "2209";
    String ERR_2209_MESSAGE = "La factura relacionada en la Nota de d\u00e9bito no esta registrada.";

    String ERR_2210_CODE = "2210";
    String ERR_2210_MESSAGE = "El dato ingresado no cumple con el formato RC-fecha-correlativo.";

    String ERR_2211_CODE = "2211";
    String ERR_2211_MESSAGE = "El XML no contiene el tag ID.";

    String ERR_2212_CODE = "2212";
    String ERR_2212_MESSAGE = "UBLVersionID - La versi\u00f3n del UBL del resumen de boletas no es correcta.";

    String ERR_2213_CODE = "2213";
    String ERR_2213_MESSAGE = "El XML no contiene el tag UBLVersionID.";

    String ERR_2214_CODE = "2214";
    String ERR_2214_MESSAGE = "CustomizationID - La versi\u00f3n del resumen de boletas no es correcta.";

    String ERR_2215_CODE = "2215";
    String ERR_2215_MESSAGE = "El XML no contiene el tag CustomizationID.";

    String ERR_2216_CODE = "2216";
    String ERR_2216_MESSAGE = "CustomerAssignedAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2217_CODE = "2217";
    String ERR_2217_MESSAGE = "El XML no contiene el tag CustomerAssignedAccountID del emisor del documento.";

    String ERR_2218_CODE = "2218";
    String ERR_2218_MESSAGE = "AdditionalAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2219_CODE = "2219";
    String ERR_2219_MESSAGE = "El XML no contiene el tag AdditionalAccountID del emisor del documento.";

    String ERR_2220_CODE = "2220";
    String ERR_2220_MESSAGE = "El ID debe coincidir con el nombre del archivo";

    String ERR_2221_CODE = "2221";
    String ERR_2221_MESSAGE = "El RUC debe coincidir con el RUC del nombre del archivo.";

    String ERR_2222_CODE = "2222";
    String ERR_2222_MESSAGE = "El contribuyente no est\u00e9 autorizado a emitir comprobantes electr\u00f3nicos.";

    String ERR_2223_CODE = "2223";
    String ERR_2223_MESSAGE = "El archivo ya fue presentado anteriormente.";

    String ERR_2224_CODE = "2224";
    String ERR_2224_MESSAGE = "N\u00famero de RUC SOL no coincide con RUC emisor.";

    String ERR_2225_CODE = "2225";
    String ERR_2225_MESSAGE = "N\u00famero de RUC del emisor no existe.";

    String ERR_2226_CODE = "2226";
    String ERR_2226_MESSAGE = "El contribuyente no esta activo.";

    String ERR_2227_CODE = "2227";
    String ERR_2227_MESSAGE = "El contribuyente no cumple con tipo de empresa o tributos requeridos.";

    String ERR_2228_CODE = "2228";
    String ERR_2228_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2229_CODE = "2229";
    String ERR_2229_MESSAGE = "El XML no contiene el tag RegistrationName del emisor del documento.";

    String ERR_2230_CODE = "2230";
    String ERR_2230_MESSAGE = "IssueDate - El dato ingresado no cumple con el patron YYYY-MM-DD.";

    String ERR_2231_CODE = "2231";
    String ERR_2231_MESSAGE = "El XML no contiene el tag IssueDate.";

    String ERR_2232_CODE = "2232";
    String ERR_2232_MESSAGE = "IssueDate- El dato ingresado no es v\u00e1lido.";

    String ERR_2233_CODE = "2233";
    String ERR_2233_MESSAGE = "ReferenceDate - El dato ingresado no cumple con el patron YYYY-MM-DD.";

    String ERR_2234_CODE = "2234";
    String ERR_2234_MESSAGE = "El XML no contiene el tag ReferenceDate.";

    String ERR_2235_CODE = "2235";
    String ERR_2235_MESSAGE = "ReferenceDate- El dato ingresado no es v\u00e1lido.";

    String ERR_2236_CODE = "2236";
    String ERR_2236_MESSAGE = "La fecha de IssueDate no debe ser mayor al d\u00eda de hoy.";

    String ERR_2237_CODE = "2237";
    String ERR_2237_MESSAGE = "La fecha de ReferenceDate no debe ser mayor al d\u00eda de hoy.";

    String ERR_2238_CODE = "2238";
    String ERR_2238_MESSAGE = "LineID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2239_CODE = "2239";
    String ERR_2239_MESSAGE = "LineID - El dato ingresado debe ser correlativo mayor a cero.";

    String ERR_2240_CODE = "2240";
    String ERR_2240_MESSAGE = "El XML no contiene el tag LineID de SummaryDocumentsLine.";

    String ERR_2241_CODE = "2241";
    String ERR_2241_MESSAGE = "DocumentTypeCode - El valor del tipo de documento es inv\u00e1lido.";

    String ERR_2242_CODE = "2242";
    String ERR_2242_MESSAGE = "El XML no contiene el tag DocumentTypeCode.";

    String ERR_2243_CODE = "2243";
    String ERR_2243_MESSAGE = "El dato ingresado no cumple con el patron SERIE.";

    String ERR_2244_CODE = "2244";
    String ERR_2244_MESSAGE = "El XML no contiene el tag DocumentSerialID.";

    String ERR_2245_CODE = "2245";
    String ERR_2245_MESSAGE = "El dato ingresado en StartDocumentNumberID debe ser num\u00e9rico.";

    String ERR_2246_CODE = "2246";
    String ERR_2246_MESSAGE = "El XML no contiene el tag StartDocumentNumberID.";

    String ERR_2247_CODE = "2247";
    String ERR_2247_MESSAGE = "El dato ingresado en sac:EndDocumentNumberID debe ser num\u00e9rico.";

    String ERR_2248_CODE = "2248";
    String ERR_2248_MESSAGE = "El XML no contiene el tag sac:EndDocumentNumberID.";

    String ERR_2249_CODE = "2249";
    String ERR_2249_MESSAGE = "Los rangos deben ser mayores a cero.";

    String ERR_2250_CODE = "2250";
    String ERR_2250_MESSAGE = "En el rango de comprobantes, el EndDocumentNumberID debe ser mayor o igual al StartInvoiceNumberID.";

    String ERR_2251_CODE = "2251";
    String ERR_2251_MESSAGE = "El dato ingresado en TotalAmount debe ser num\u00e9rico mayor o igual a cero.";

    String ERR_2252_CODE = "2252";
    String ERR_2252_MESSAGE = "El XML no contiene el tag TotalAmount.";

    String ERR_2253_CODE = "2253";
    String ERR_2253_MESSAGE = "El dato ingresado en TotalAmount debe ser num\u00e9rico mayor a cero.";

    String ERR_2254_CODE = "2254";
    String ERR_2254_MESSAGE = "PaidAmount - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2255_CODE = "2255";
    String ERR_2255_MESSAGE = "El XML no contiene el tag PaidAmount.";

    String ERR_2256_CODE = "2256";
    String ERR_2256_MESSAGE = "InstructionID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2257_CODE = "2257";
    String ERR_2257_MESSAGE = "El XML no contiene el tag InstructionID.";

    String ERR_2258_CODE = "2258";
    String ERR_2258_MESSAGE = "Debe indicar Referencia de Importes asociados a las boletas de venta.";

    String ERR_2259_CODE = "2259";
    String ERR_2259_MESSAGE = "Debe indicar 3 Referencias de Importes asociados a las boletas de venta.";

    String ERR_2260_CODE = "2260";
    String ERR_2260_MESSAGE = "PaidAmount - El dato ingresado debe ser mayor o igual a 0.00";

    String ERR_2261_CODE = "2261";
    String ERR_2261_MESSAGE = "cbc:Amount - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2262_CODE = "2262";
    String ERR_2262_MESSAGE = "El XML no contiene el tag cbc:Amount.";

    String ERR_2263_CODE = "2263";
    String ERR_2263_MESSAGE = "ChargeIndicator - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2264_CODE = "2264";
    String ERR_2264_MESSAGE = "El XML no contiene el tag ChargeIndicator.";

    String ERR_2265_CODE = "2265";
    String ERR_2265_MESSAGE = "Debe indicar informaci\u00f3n acerca del Importe Total de Otros Cargos.";

    String ERR_2266_CODE = "2266";
    String ERR_2266_MESSAGE = "Debe indicar cargos mayores o iguales a cero.";

    String ERR_2267_CODE = "2267";
    String ERR_2267_MESSAGE = "TaxScheme ID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2268_CODE = "2268";
    String ERR_2268_MESSAGE = "El c\u00f3digo del tributo es inv\u00e1lido.";

    String ERR_2269_CODE = "2269";
    String ERR_2269_MESSAGE = "El XML no contiene el tag TaxScheme ID de informaci\u00f3n acerca del importe total de un tipo particular de impuesto.";

    String ERR_2270_CODE = "2270";
    String ERR_2270_MESSAGE = "TaxScheme Name - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2271_CODE = "2271";
    String ERR_2271_MESSAGE = "El XML no contiene el tag TaxScheme Name de impuesto.";

    String ERR_2272_CODE = "2272";
    String ERR_2272_MESSAGE = "TaxScheme TaxTypeCode - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2273_CODE = "2273";
    String ERR_2273_MESSAGE = "TaxAmount - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2274_CODE = "2274";
    String ERR_2274_MESSAGE = "El XML no contiene el tag TaxAmount.";

    String ERR_2275_CODE = "2275";
    String ERR_2275_MESSAGE = "Si el c\u00f3digo de tributo es 2000, el nombre del tributo debe ser ISC.";

    String ERR_2276_CODE = "2276";
    String ERR_2276_MESSAGE = "Si el c\u00f3digo de tributo es 1000, el nombre del tributo debe ser IGV.";

    String ERR_2277_CODE = "2277";
    String ERR_2277_MESSAGE = "No se ha consignado ninguna informaci\u00f3n del importe total de tributos.";

    String ERR_2278_CODE = "2278";
    String ERR_2278_MESSAGE = "Debe indicar informaci\u00f3n acerca del importe total de ISC e IGV.";

    String ERR_2279_CODE = "2279";
    String ERR_2279_MESSAGE = "Debe indicar Items de consolidado de documentos.";

    String ERR_2280_CODE = "2280";
    String ERR_2280_MESSAGE = "Existen problemas con la informaci\u00f3n del resumen de comprobantes.";

    String ERR_2281_CODE = "2281";
    String ERR_2281_MESSAGE = "Error en la validaci\u00f3n de los rangos de los comprobantes.";

    String ERR_2282_CODE = "2282";
    String ERR_2282_MESSAGE = "Existe documento ya informado anteriormente.";

    String ERR_2283_CODE = "2283";
    String ERR_2283_MESSAGE = "El dato ingresado no cumple con el formato RA-fecha-correlativo.";

    String ERR_2284_CODE = "2284";
    String ERR_2284_MESSAGE = "El XML no contiene el tag ID.";

    String ERR_2285_CODE = "2285";
    String ERR_2285_MESSAGE = "El ID debe coincidir con el nombre del archivo.";

    String ERR_2286_CODE = "2286";
    String ERR_2286_MESSAGE = "El RUC debe coincidir con el RUC del nombre del archivo.";

    String ERR_2287_CODE = "2287";
    String ERR_2287_MESSAGE = "AdditionalAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2288_CODE = "2288";
    String ERR_2288_MESSAGE = "El XML no contiene el tag AdditionalAccountID del emisor del documento.";

    String ERR_2289_CODE = "2289";
    String ERR_2289_MESSAGE = "CustomerAssignedAccountID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2290_CODE = "2290";
    String ERR_2290_MESSAGE = "El XML no contiene el tag CustomerAssignedAccountID del emisor del documento.";

    String ERR_2291_CODE = "2291";
    String ERR_2291_MESSAGE = "El contribuyente no esta autorizado a emitir comprobantes electr\u00f3nicos.";

    String ERR_2292_CODE = "2292";
    String ERR_2292_MESSAGE = "N\u00famero de RUC SOL no coincide con RUC emisor.";

    String ERR_2293_CODE = "2293";
    String ERR_2293_MESSAGE = "N\u00famero de RUC del emisor no existe.";

    String ERR_2294_CODE = "2294";
    String ERR_2294_MESSAGE = "El contribuyente no esta activo.";

    String ERR_2295_CODE = "2295";
    String ERR_2295_MESSAGE = "El contribuyente no cumple con tipo de empresa o tributos requeridos.";

    String ERR_2296_CODE = "2296";
    String ERR_2296_MESSAGE = "RegistrationName - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2297_CODE = "2297";
    String ERR_2297_MESSAGE = "El XML no contiene el tag RegistrationName del emisor del documento.";

    String ERR_2298_CODE = "2298";
    String ERR_2298_MESSAGE = "IssueDate - El dato ingresado no cumple con el patron YYYY-MM-DD.";

    String ERR_2299_CODE = "2299";
    String ERR_2299_MESSAGE = "El XML no contiene el tag IssueDate.";

    String ERR_2300_CODE = "2300";
    String ERR_2300_MESSAGE = "IssueDate - El dato ingresado no es v\u00e1lido.";

    String ERR_2301_CODE = "2301";
    String ERR_2301_MESSAGE = "La fecha del IssueDate no debe ser mayor al d\u00eda de hoy.";

    String ERR_2302_CODE = "2302";
    String ERR_2302_MESSAGE = "ReferenceDate - El dato ingresado no cumple con el patron YYYY-MM-DD";

    String ERR_2303_CODE = "2303";
    String ERR_2303_MESSAGE = "El XML no contiene el tag ReferenceDate.";

    String ERR_2304_CODE = "2304";
    String ERR_2304_MESSAGE = "ReferenceDate - El dato ingresado no es v\u00e1lido.";

    String ERR_2305_CODE = "2305";
    String ERR_2305_MESSAGE = "LineID - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2306_CODE = "2306";
    String ERR_2306_MESSAGE = "LineID - El dato ingresado debe ser correlativo mayor a cero.";

    String ERR_2307_CODE = "2307";
    String ERR_2307_MESSAGE = "El XML no contiene el tag LineID de VoidedDocumentsLine.";

    String ERR_2308_CODE = "2308";
    String ERR_2308_MESSAGE = "DocumentTypeCode - El valor del tipo de documento es inv\u00e1lido.";

    String ERR_2309_CODE = "2309";
    String ERR_2309_MESSAGE = "El XML no contiene el tag DocumentTypeCode.";

    String ERR_2310_CODE = "2310";
    String ERR_2310_MESSAGE = "El dato ingresado no cumple con el patron SERIE.";

    String ERR_2311_CODE = "2311";
    String ERR_2311_MESSAGE = "El XML no contiene el tag DocumentSerialID.";

    String ERR_2312_CODE = "2312";
    String ERR_2312_MESSAGE = "El dato ingresado en DocumentNumberID debe ser num\u00e9rico y como m\u00e1ximo de 8 d\u00edgitos.";

    String ERR_2313_CODE = "2313";
    String ERR_2313_MESSAGE = "El XML no contiene el tag DocumentNumberID.";

    String ERR_2314_CODE = "2314";
    String ERR_2314_MESSAGE = "El dato ingresado en VoidReasonDescription debe contener informaci\u00f3n v\u00e1lida.";

    String ERR_2315_CODE = "2315";
    String ERR_2315_MESSAGE = "El XML no contiene el tag VoidReasonDescription.";

    String ERR_2316_CODE = "2316";
    String ERR_2316_MESSAGE = "Debe indicar Items en VoidedDocumentsLine.";

    String ERR_2317_CODE = "2317";
    String ERR_2317_MESSAGE = "Error al procesar el resumen de anulados.";

    String ERR_2318_CODE = "2318";
    String ERR_2318_MESSAGE = "CustomizationID - La versi\u00f3n del documento no es correcta.";

    String ERR_2319_CODE = "2319";
    String ERR_2319_MESSAGE = "El XML no contiene el tag CustomizationID.";

    String ERR_2320_CODE = "2320";
    String ERR_2320_MESSAGE = "UBLVersionID - La versi\u00f3n del UBL  no es la correcta.";

    String ERR_2321_CODE = "2321";
    String ERR_2321_MESSAGE = "El XML no contiene el tag UBLVersionID.";

    String ERR_2322_CODE = "2322";
    String ERR_2322_MESSAGE = "Error en la validaci\u00f3n de los rangos.";

    String ERR_2323_CODE = "2323";
    String ERR_2323_MESSAGE = "Existe documento ya informado anteriormente en una comunicaci\u00f3n de baja.";

    String ERR_2324_CODE = "2324";
    String ERR_2324_MESSAGE = "El archivo de comunicaci\u00f3n de baja ya fue presentado anteriormente.";

    String ERR_2325_CODE = "2325";
    String ERR_2325_MESSAGE = "El certificado usado no es el comunicado a SUNAT.";

    String ERR_2326_CODE = "2326";
    String ERR_2326_MESSAGE = "El certificado usado se encuentra de baja.";

    String ERR_2327_CODE = "2327";
    String ERR_2327_MESSAGE = "El certificado usado no se encuentra vigente.";

    String ERR_2328_CODE = "2328";
    String ERR_2328_MESSAGE = "El certificado usado se encuentra revocado.";

    String ERR_2329_CODE = "2329";
    String ERR_2329_MESSAGE = "La fecha de emisi\u00f3n se encuentra fuera del l\u00edmite permitido.";

    String ERR_2330_CODE = "2330";
    String ERR_2330_MESSAGE = "La fecha de generaci\u00f3n de la comunicaci\u00f3n debe ser igual a la fecha consignada en el nombre del archivo.";

    String ERR_2331_CODE = "2331";
    String ERR_2331_MESSAGE = "N\u00famero de RUC del nombre del archivo no coincide con el consignado en el contenido del archivo XML.";

    String ERR_2332_CODE = "2332";
    String ERR_2332_MESSAGE = "N\u00famero de Serie del nombre del archivo no coincide con el consignado en el contenido del archivo XML.";

    String ERR_2333_CODE = "2333";
    String ERR_2333_MESSAGE = "N\u00famero de documento en el nombre del archivo no coincide con el consignado en el contenido del XML.";

    String ERR_2334_CODE = "2334";
    String ERR_2334_MESSAGE = "El documento electr\u00f3nico ingresado ha sido alterado.";

    String ERR_2335_CODE = "2335";
    String ERR_2335_MESSAGE = "El documento electr\u00f3nico ingresado ha sido alterado.";

    String ERR_2336_CODE = "2336";
    String ERR_2336_MESSAGE = "Ocurri\u00f3 un error en el proceso de validaci\u00f3n de la firma digital.";

    String ERR_2337_CODE = "2337";
    String ERR_2337_MESSAGE = "La moneda debe ser la misma en todo el documento.";

    String ERR_2338_CODE = "2338";
    String ERR_2338_MESSAGE = "La moneda debe ser la misma en todo el documento.";

    String ERR_2339_CODE = "2339";
    String ERR_2339_MESSAGE = "El dato ingresado en PayableAmount no cumple con el formato establecido.";

    String ERR_2340_CODE = "2340";
    String ERR_2340_MESSAGE = "El valor ingresado en AdditionalMonetaryTotal/cbc:ID es incorrecto.";

    String ERR_2341_CODE = "2341";
    String ERR_2341_MESSAGE = "AdditionalMonetaryTotal/cbc:ID debe tener valor.";

    String ERR_2342_CODE = "2342";
    String ERR_2342_MESSAGE = "Fecha de emisi\u00f3n de la factura no coincide con la informada en la comunicaci\u00f3n.";

    String ERR_2343_CODE = "2343";
    String ERR_2343_MESSAGE = "cac:TaxTotal/cac:TaxSubtotal/cbc:TaxAmount - El dato ingresado no cumple con el est\u00e1ndar.";

    String ERR_2344_CODE = "2344";
    String ERR_2344_MESSAGE = "El XML no contiene el tag cac:TaxTotal/cac:TaxSubtotal/cbc:TaxAmount";

    String ERR_2345_CODE = "2345";
    String ERR_2345_MESSAGE = "La serie no corresponde al tipo de comprobante.";

    String ERR_2346_CODE = "2346";
    String ERR_2346_MESSAGE = "La fecha de generaci\u00f3n del resumen debe ser igual a la fecha consignada en el nombre del archivo.";

    String ERR_2347_CODE = "2347";
    String ERR_2347_MESSAGE = "Los rangos informados en el archivo XML se encuentran duplicados o superpuestos.";

    String ERR_2348_CODE = "2348";
    String ERR_2348_MESSAGE = "Los documentos informados en el archivo XML se encuentran duplicados.";

    String ERR_2349_CODE = "2349";
    String ERR_2349_MESSAGE = "Debe consignar solo un elemento sac:AdditionalMonetaryTotal con cbc:ID igual a 1001";

    String ERR_2350_CODE = "2350";
    String ERR_2350_MESSAGE = "Debe consignar solo un elemento sac:AdditionalMonetaryTotal con cbc:ID igual a 1002";

    String ERR_2351_CODE = "2351";
    String ERR_2351_MESSAGE = "Debe consignar solo un elemento sac:AdditionalMonetaryTotal con cbc:ID igual a 1003";

    String ERR_2352_CODE = "2352";
    String ERR_2352_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel global para IGV (cbc:ID igual a 1000)";

    String ERR_2353_CODE = "2353";
    String ERR_2353_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel global para ISC (cbc:ID igual a 2000)";

    String ERR_2354_CODE = "2354";
    String ERR_2354_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel global para Otros (cbc:ID igual a 9999)";

    String ERR_2355_CODE = "2355";
    String ERR_2355_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel de item para IGV (cbc:ID igual a 1000)";

    String ERR_2356_CODE = "2356";
    String ERR_2356_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel de item para ISC (cbc:ID igual a 2000)";

    String ERR_2357_CODE = "2357";
    String ERR_2357_MESSAGE = "Debe consignar solo un elemento sac:BillingPayment a nivel de item con cbc:InstructionID igual a 01";

    String ERR_2358_CODE = "2358";
    String ERR_2358_MESSAGE = "Debe consignar solo un elemento sac:BillingPayment a nivel de item con cbc:InstructionID igual a 02";

    String ERR_2359_CODE = "2359";
    String ERR_2359_MESSAGE = "Debe consignar solo un elemento sac:BillingPayment a nivel de item con cbc:InstructionID igual a 03";

    String ERR_2360_CODE = "2360";
    String ERR_2360_MESSAGE = "Debe consignar solo un elemento sac:BillingPayment a nivel de item con cbc:InstructionID igual a 04";

    String ERR_2361_CODE = "2361";
    String ERR_2361_MESSAGE = "Debe consignar solo un elemento cac:TaxTotal a nivel de item para Otros (cbc:ID igual a 9999)";

    String ERR_2362_CODE = "2362";
    String ERR_2362_MESSAGE = "Debe consignar solo un tag cac:AccountingSupplierParty/cbc:AdditionalAccountID";

    String ERR_2363_CODE = "2363";
    String ERR_2363_MESSAGE = "Debe consignar solo un tag cac:AccountingCustomerParty/cbc:AdditionalAccountID";

    String ERR_2364_CODE = "2364";
    String ERR_2364_MESSAGE = "El comprobante contiene un tipo y n\u00famero  de Gu\u00eda de Remisi\u00f3n repetido.";

    String ERR_2365_CODE = "2365";
    String ERR_2365_MESSAGE = "El comprobante contiene un tipo y n\u00famero de Documento Relacionado repetido.";

    String ERR_2366_CODE = "2366";
    String ERR_2366_MESSAGE = "El c\u00f3digo en el tag sac:AdditionalProperty/cbc:ID debe tener 4 posiciones.";

    String ERR_2367_CODE = "2367";
    String ERR_2367_MESSAGE = "El dato ingresado en PriceAmount del Precio de venta unitario por item no cumple con el formato establecido.";

    String ERR_2368_CODE = "2368";
    String ERR_2368_MESSAGE = "El dato ingresado en TaxSubtotal/cbc:TaxAmount del item no cumple con el formato establecido.";

    String ERR_2369_CODE = "2369";
    String ERR_2369_MESSAGE = "El dato ingresado en PriceAmount del Valor de venta unitario por item no cumple con el formato establecido.";

    String ERR_2370_CODE = "2370";
    String ERR_2370_MESSAGE = "El dato ingresado en LineExtensionAmount del item no cumple con el formato establecido.";

    String ERR_2371_CODE = "2371";
    String ERR_2371_MESSAGE = "El XML no contiene el tag cbc:TaxExemptionReasonCode de Afectaci\u00f3n al IGV.";

    String ERR_2372_CODE = "2372";
    String ERR_2372_MESSAGE = "El tag en el item cac:TaxTotal/cbc:TaxAmount debe tener el mismo valor que cac:TaxTotal/cac:TaxSubtotal/cbc:TaxAmount.";

    String ERR_2373_CODE = "2373";
    String ERR_2373_MESSAGE = "Si existe monto de ISC en el ITEM debe especificar el sistema de c\u00e9lculo.";

    String ERR_2374_CODE = "2374";
    String ERR_2374_MESSAGE = "La factura a dar de baja tiene una fecha de recepci\u00f3n fuera del plazo permitido.";

    String ERR_2375_CODE = "2375";
    String ERR_2375_MESSAGE = "Fecha de emisi\u00f3n de la boleta no coincide con la fecha de emisi\u00f3n consignada en la comunicaci\u00f3n.";

    String ERR_2376_CODE = "2376";
    String ERR_2376_MESSAGE = "La boleta de venta a dar de baja fue informada en un resumen con fecha de recepcion fuera del plazo permitido.";

    String ERR_2377_CODE = "2377";
    String ERR_2377_MESSAGE = "El Name o TaxTypeCode debe corresponder con el Id para el IGV.";

    String ERR_2378_CODE = "2378";
    String ERR_2378_MESSAGE = "El Name o TaxTypeCode debe corresponder con el Id para el ISC.";

    String ERR_2379_CODE = "2379";
    String ERR_2379_MESSAGE = "La numeraci\u00f3n de boleta de venta a dar de baja fue generada en una fecha fuera del plazo permitido.";

    String ERR_2380_CODE = "2380";
    String ERR_2380_MESSAGE = "El documento tiene observaciones.";

    String ERR_2381_CODE = "2381";
    String ERR_2381_MESSAGE = "Comprobante no cumple con el Grupo 1: No todos los items corresponden a operaciones gravadas a IGV.";

    String ERR_2382_CODE = "2382";
    String ERR_2382_MESSAGE = "Comprobante no cumple con el Grupo 2: No todos los items corresponden a operaciones inafectas o exoneradas al IGV.";

    String ERR_2383_CODE = "2383";
    String ERR_2383_MESSAGE = "Comprobante no cumple con el Grupo 3: Falta leyenda con codigo 1002.";

    String ERR_2384_CODE = "2384";
    String ERR_2384_MESSAGE = "Comprobante no cumple con el Grupo 3: Existe item con operaci\u00f3n onerosa.";

    String ERR_2385_CODE = "2385";
    String ERR_2385_MESSAGE = "Comprobante no cumple con el Grupo 4: Debe exitir Total descuentos mayor a cero.";

    String ERR_2386_CODE = "2386";
    String ERR_2386_MESSAGE = "Comprobante no cumple con el Grupo 5: Todos los items deben tener operaciones afectas a ISC.";

    String ERR_2387_CODE = "2387";
    String ERR_2387_MESSAGE = "Comprobante no cumple con el Grupo 6: El monto de percepci\u00f3n no existe o es cero.";

    String ERR_2388_CODE = "2388";
    String ERR_2388_MESSAGE = "Comprobante no cumple con el Grupo 6: Todos los items deben tener c\u00f3digo de Afectaci\u00f3n al IGV igual a 10";

    String ERR_2389_CODE = "2389";
    String ERR_2389_MESSAGE = "Comprobante no cumple con el Grupo 7: El c\u00f3digo de moneda no es diferente a PEN.";

    String ERR_2390_CODE = "2390";
    String ERR_2390_MESSAGE = "Comprobante no cumple con el Grupo 8: No todos los items corresponden a operaciones gravadas a IGV.";

    String ERR_2391_CODE = "2391";
    String ERR_2391_MESSAGE = "Comprobante no cumple con el Grupo 9: No todos los items corresponden a operaciones inafectas o exoneradas al IGV.";

    String ERR_2392_CODE = "2392";
    String ERR_2392_MESSAGE = "Comprobante no cumple con el Grupo 10: Falta leyenda con codigo 1002";

    String ERR_2393_CODE = "2393";
    String ERR_2393_MESSAGE = "Comprobante no cumple con el Grupo 10: Existe item con operaci\u00f3n onerosa.";

    String ERR_2394_CODE = "2394";
    String ERR_2394_MESSAGE = "Comprobante no cumple con el Grupo 11: Debe existir Total descuentos mayor a cero.";

    String ERR_2395_CODE = "2395";
    String ERR_2395_MESSAGE = "Comprobante no cumple con el Grupo 12: El c\u00f3digo de moneda no es diferente a PEN.";

    String ERR_2396_CODE = "2396";
    String ERR_2396_MESSAGE = "Si el monto total es mayor a S/. 700.00 debe consignar tipo y n\u00famero de documento del adquiriente.";

    String ERR_2397_CODE = "2397";
    String ERR_2397_MESSAGE = "El tipo de documento del adquiriente no puede ser N\u00famero de RUC.";

    String ERR_2398_CODE = "2398";
    String ERR_2398_MESSAGE = "El documento a dar de baja se encuentra rechazado.";

    String ERR_2399_CODE = "2399";
    String ERR_2399_MESSAGE = "El tipo de documento modificado por la Nota de cr\u00e9dito debe ser boleta electr\u00f3nica.";

    String ERR_2400_CODE = "2400";
    String ERR_2400_MESSAGE = "El tipo de documento modificado por la Nota de d\u00e9bito debe ser boleta electr\u00f3nica.";

    String ERR_2401_CODE = "2401";
    String ERR_2401_MESSAGE = "No se puede leer (parsear) el archivo XML.";

    String ERR_2402_CODE = "2402";
    String ERR_2402_MESSAGE = "El caso de prueba no existe.";

    String ERR_2403_CODE = "2403";
    String ERR_2403_MESSAGE = "La numeraci\u00f3n o nombre del documento ya ha sido enviado anteriormente.";

    String ERR_2404_CODE = "2404";
    String ERR_2404_MESSAGE = "Documento afectado por la nota electr\u00f3nica no se encuentra autorizado.";

    String ERR_2405_CODE = "2405";
    String ERR_2405_MESSAGE = "Contribuyente no se encuentra autorizado como emisor de boletas electr\u00f3nicas.";

    String ERR_2406_CODE = "2406";
    String ERR_2406_MESSAGE = "Existe mas de un tag sac:AdditionalMonetaryTotal con el mismo ID.";

    String ERR_2407_CODE = "2407";
    String ERR_2407_MESSAGE = "Existe mas de un tag sac:AdditionalProperty con el mismo ID.";

    String ERR_2408_CODE = "2408";
    String ERR_2408_MESSAGE = "El dato ingresado en PriceAmount del Valor referencial unitario por item no cumple con el formato establecido.";

    String ERR_2409_CODE = "2409";
    String ERR_2409_MESSAGE = "Existe mas de un tag cac:AlternativeConditionPrice con el mismo cbc:PriceTypeCode.";

    String ERR_2410_CODE = "2410";
    String ERR_2410_MESSAGE = "Se ha consignado un valor inv\u00e1lido en el campo cbc:PriceTypeCode.";

    String ERR_2411_CODE = "2411";
    String ERR_2411_MESSAGE = "Ha consignado mas de un elemento cac:AllowanceCharge con el mismo campo cbc:ChargeIndicator.";

    String ERR_2412_CODE = "2412";
    String ERR_2412_MESSAGE = "Se ha consignado mas de un documento afectado por la nota (tag cac:BillingReference).";

    String ERR_2413_CODE = "2413";
    String ERR_2413_MESSAGE = "Se ha consignado mas de un motivo o sustento de la nota (tag cac:DiscrepancyResponse/cbc:Description).";

    String ERR_2414_CODE = "2414";
    String ERR_2414_MESSAGE = "No se ha consignado en la nota el tag cac:DiscrepancyResponse";

    String ERR_2415_CODE = "2415";
    String ERR_2415_MESSAGE = "Se ha consignado en la nota mas de un tag cac:DiscrepancyResponse";

    String ERR_2416_CODE = "2416";
    String ERR_2416_MESSAGE = "Si existe leyenda Transferida Gratuita debe consignar Total Valor de Venta de Operaciones Gratuitas.";

    String ERR_2417_CODE = "2417";
    String ERR_2417_MESSAGE = "Debe consignar Valor Referencial unitario por item en operaciones no onerosas.";

    String ERR_2418_CODE = "2418";
    String ERR_2418_MESSAGE = "Si consigna Valor Referencial unitario por item en operaciones no onerosas, la operaci\u00f3n debe ser no onerosa.";

    String ERR_2419_CODE = "2419";
    String ERR_2419_MESSAGE = "El dato ingresado en AllowanceTotalAmount no cumple con el formato establecido.";

    String ERR_2420_CODE = "2420";
    String ERR_2420_MESSAGE = "Ya transcurrieron mas de 25 dias calendarios para concluir con su proceso de homologaci\u00f3n.";

    String ERR_4000_CODE = "4000";
    String ERR_4000_MESSAGE = "El documento ya fue presentado anteriormente.";

    String ERR_4001_CODE = "4001";
    String ERR_4001_MESSAGE = "El n\u00famero de RUC del receptor no existe.";

    String ERR_4002_CODE = "4002";
    String ERR_4002_MESSAGE = "Para el TaxTypeCode, esta usando un valor que no existe en el catalogo.";

    String ERR_4003_CODE = "4003";
    String ERR_4003_MESSAGE = "El comprobante fue registrado previamente como rechazado.";

    String ERR_4004_CODE = "4004";
    String ERR_4004_MESSAGE = "El DocumentTypeCode de las guias debe existir y tener 2 posiciones.";

    String ERR_4005_CODE = "4005";
    String ERR_4005_MESSAGE = "El DocumentTypeCode de las gu\u00edas debe ser 09 o 31.";

    String ERR_4006_CODE = "4006";
    String ERR_4006_MESSAGE = "El ID de las gu\u00edas debe tener informaci\u00f3n de la SERIE-NUMERO de gu\u00eda.";

    String ERR_4007_CODE = "4007";
    String ERR_4007_MESSAGE = "El XML no contiene el ID de las gu\u00edas.";

    String ERR_4008_CODE = "4008";
    String ERR_4008_MESSAGE = "El DocumentTypeCode de Otros documentos relacionados no cumple con el est\u00e1ndar.";

    String ERR_4009_CODE = "4009";
    String ERR_4009_MESSAGE = "El DocumentTypeCode de Otros documentos relacionados tiene valores incorrectos.";

    String ERR_4010_CODE = "4010";
    String ERR_4010_MESSAGE = "El ID de los documentos relacionados no cumplen con el est\u00e1ndar.";

    String ERR_4011_CODE = "4011";
    String ERR_4011_MESSAGE = "El XML no contiene el tag ID de documentos relacionados.";

    String ERR_4012_CODE = "4012";
    String ERR_4012_MESSAGE = "El ubigeo indicado en el comprobante no es el mismo que esta registrado para el contribuyente.";

    String ERR_4013_CODE = "4013";
    String ERR_4013_MESSAGE = "El RUC del receptor no esta activo.";

    String ERR_4014_CODE = "4014";
    String ERR_4014_MESSAGE = "El RUC del receptor no esta habido.";

    String ERR_4015_CODE = "4015";
    String ERR_4015_MESSAGE = "Si el tipo de documento del receptor no es RUC, debe tener operaciones de exportaci\u00f3n.";

    String ERR_4016_CODE = "4016";
    String ERR_4016_MESSAGE = "El total valor venta neta de oper. gravadas IGV debe ser mayor a 0.00 o debe existir oper. gravadas onerosas.";

    String ERR_4017_CODE = "4017";
    String ERR_4017_MESSAGE = "El total valor venta neta de oper. inafectas IGV debe ser mayor a 0.00 o debe existir oper. inafectas onerosas o de export.";

    String ERR_4018_CODE = "4018";
    String ERR_4018_MESSAGE = "El total valor venta neta de oper. exoneradas IGV debe ser mayor a 0.00 o debe existir oper. exoneradas.";

    String ERR_4019_CODE = "4019";
    String ERR_4019_MESSAGE = "El c\u00e9lculo del IGV no es correcto.";

    String ERR_4020_CODE = "4020";
    String ERR_4020_MESSAGE = "El ISC no esta informado correctamente.";

    String ERR_4021_CODE = "4021";
    String ERR_4021_MESSAGE = "Si se utiliza la leyenda con c\u00f3digo 2000, el importe de percepci\u00f3n debe ser mayor a 0.00";

    String ERR_4022_CODE = "4022";
    String ERR_4022_MESSAGE = "Si se utiliza la leyenda con c\u00f3digo 2001, el total de operaciones exoneradas debe ser mayor a 0.00";

    String ERR_4023_CODE = "4023";
    String ERR_4023_MESSAGE = "Si se utiliza la leyenda con c\u00f3digo 2002, el total de operaciones exoneradas debe ser mayor a 0.00";

    String ERR_4024_CODE = "4024";
    String ERR_4024_MESSAGE = "Si se utiliza la leyenda con c\u00f3digo 2003, el total de operaciones exoneradas debe ser mayor a 0.00";

    String ERR_4025_CODE = "4025";
    String ERR_4025_MESSAGE = "Si usa la leyenda de Transferencia o Servivicio gratuito, todos los items deben ser no onerosos";

    String ERR_4026_CODE = "4026";
    String ERR_4026_MESSAGE = "No se puede indicar Gu\u00eda de remision de remitente y Gu\u00eda de remisi\u00f3n de transportista en el mismo documento.";

    String ERR_4027_CODE = "4027";
    String ERR_4027_MESSAGE = "El importe total no coincide con la sumatoria de los valores de venta mas los tributos mas los cargos.";

    String ERR_4028_CODE = "4028";
    String ERR_4028_MESSAGE = "El monto total de la nota de cr\u00e9dito debe ser menor o igual al monto de la factura.";

    String ERR_4029_CODE = "4029";
    String ERR_4029_MESSAGE = "El ubigeo indicado en el comprobante no es el mismo que esta registrado para el contribuyente.";

    String ERR_4030_CODE = "4030";
    String ERR_4030_MESSAGE = "El ubigeo indicado en el comprobante no es el mismo que esta registrado para el contribuyente.";

    String ERR_4031_CODE = "4031";
    String ERR_4031_MESSAGE = "Debe indicar el nombre comercial.";

    String ERR_4032_CODE = "4032";
    String ERR_4032_MESSAGE = "Si el c\u00f3digo del motivo de emisi\u00f3n de la Nota de Cr\u00e9dito es 03, debe existir la descripci\u00f3n del item.";

    String ERR_4033_CODE = "4033";
    String ERR_4033_MESSAGE = "La fecha de generaci\u00f3n de la numeraci\u00f3n debe ser menor o igual a la fecha de generaci\u00f3n de la comunicaci\u00f3n.";

    String ERR_4034_CODE = "4034";
    String ERR_4034_MESSAGE = "El comprobante fue registrado previamente como baja.";

    String ERR_4035_CODE = "4035";
    String ERR_4035_MESSAGE = "El comprobante fue registrado previamente como rechazado.";

    String ERR_4036_CODE = "4036";
    String ERR_4036_MESSAGE = "La fecha de emisi\u00f3n de los rangos debe ser menor o igual a la fecha de generaci\u00f3n del resumen.";

    String ERR_4037_CODE = "4037";
    String ERR_4037_MESSAGE = "El c\u00e9lculo del Total de IGV del Item no es correcto.";

    String ERR_4038_CODE = "4038";
    String ERR_4038_MESSAGE = "El resumen contiene menos series por tipo de documento que el envio anterior para la misma fecha de emisi\u00f3n.";

    String ERR_4039_CODE = "4039";
    String ERR_4039_MESSAGE = "No ha consignado informaci\u00f3n del ubigeo del domicilio fiscal.";

    String ERR_4040_CODE = "4040";
    String ERR_4040_MESSAGE = "Si el importe de percepci\u00f3n es mayor a 0.00, debe utilizar una leyenda con c\u00f3digo 2000.";

    String ERR_4041_CODE = "4041";
    String ERR_4041_MESSAGE = "El c\u00f3digo de pa\u00eds debe ser PE.";

} //ISunatError
