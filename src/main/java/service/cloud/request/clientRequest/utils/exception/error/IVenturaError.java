package service.cloud.request.clientRequest.utils.exception.error;


import service.cloud.request.clientRequest.extras.IUBLConfig;

/*** Esta interfaz contiene todos los codigos de errores presentados en el Sistema* de HOMOLOGACION.** @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)*/
public interface IVenturaError {

    public ErrorObj ERROR_350 = new ErrorObj(
            350,
            "[UBL-DOCUMENT] Hubo un problema al generar el TaxtTotal de Impuestos del RESUMEN DIARIO.");
    public ErrorObj ERROR_349 = new ErrorObj(
            349,
            "[UBL-DOCUMENT] Hubo un problema al generar el BillingPayment en una de las LINEAS del RESUMEN DIARIO.");


    /*** Stados GENERALES* <p>* Disponible: 0 - 10*/
    ErrorObj ERROR_0 = new ErrorObj(0, "Success");
    ErrorObj ERROR_2 = new ErrorObj(2, "Error");
    /*** Errores Generales* <p>* Disponible: 11 - 100*/
    ErrorObj ERROR_15 = new ErrorObj(15, "[GENERAL] Hubo un problema al convertir el archivo en bytes.");
    /*** Seccion: Hibernate* <p>* Disponible: 101 - 150*/
    /*** Seccion: Sunat* <p>* Disponible: 151 - 200*/
    ErrorObj ERROR_151 = new ErrorObj(151, "[SUNAT-WS] Hubo un problema al intentar crear un cliente de servicio web de Sunat. El nombre del cliente no existe.");
    ErrorObj ERROR_154 = new ErrorObj(154, "[SUNAT-WS-CONSULT] Ocurrio un problema al intentar conectarse con SUNAT.");
    ErrorObj ERROR_155 = new ErrorObj(155, "[SUNAT-WS-CONSULT] Hubo un problema en el proceso del SERVICIO WEB de consulta de Sunat.");

    /*** Seccion: Signer-documents* <p>* Disponible: 251 - 300*/
    ErrorObj ERROR_251 = new ErrorObj(251, "[SIGNER-DOCUMENTS] Ocurrio un problema al convertir el certificado a bytes.");
    ErrorObj ERROR_252 = new ErrorObj(252, "[SIGNER-DOCUMENTS] Ocurrio un error al extraer el ALIAS del certificado.");
    ErrorObj ERROR_253 = new ErrorObj(253, "[SIGNER-DOCUMENTS] Ocurrio un error al cargar el certificado digital.");
    ErrorObj ERROR_254 = new ErrorObj(254, "[SIGNER-DOCUMENTS] La ruta del documento UBL ha firmar, no contiene un archivo.");
    ErrorObj ERROR_255 = new ErrorObj(255, "[SIGNER-DOCUMENTS] Ocurrio un problema al intentar firmar el documento UBL.");
    ErrorObj ERROR_256 = new ErrorObj(256, "[SIGNER-DOCUMENTS] Ocurrio un problema al buscar la posicion del TAG a firmar del documento UBL.");
    ErrorObj ERROR_257 = new ErrorObj(257, "[SIGNER-DOCUMENTS] La verificacion del certificado digital a retornado errores. No es posible firmar.");
    ErrorObj ERROR_258 = new ErrorObj(258, "[SIGNER-DOCUMENTS] La validacion del certificado digital retorno error. Verifique la contrasenia del certificado.");
    ErrorObj ERROR_259 = new ErrorObj(259, "[SIGNER-DOCUMENTS] El certificado digital es nulo. Verifique la ruta en DISCO.");
    /*** Seccion: Documentos UBL* <p>* Disponible: 301 - 400*/
    ErrorObj ERROR_301 = new ErrorObj(301, "[UBL-DOCUMENT] Ocurrio un problema al generar el objeto SignatureType.");
    ErrorObj ERROR_302 = new ErrorObj(302, "[UBL-DOCUMENT] Ocurrio un problema al generar el objeto SupplierPartyType.");
    ErrorObj ERROR_303 = new ErrorObj(303, "[UBL-DOCUMENT] Ocurrio un problema al generar el objeto CustomerPartyType.");
    ErrorObj ERROR_312 = new ErrorObj(312, "[UBL-DOCUMENT] La fecha de emision es nula.");
    ErrorObj ERROR_313 = new ErrorObj(313, "[UBL-DOCUMENT] Hubo un problema con la fecha de emision.");
    ErrorObj ERROR_314 = new ErrorObj(314, "[UBL-DOCUMENT] La fecha de vencimiento es nula.");
    ErrorObj ERROR_315 = new ErrorObj(315, "[UBL-DOCUMENT] Hubo un problema con la fecha de vencimiento.");
    ErrorObj ERROR_316 = new ErrorObj(316, "[UBL-DOCUMENT] No se encontraron impuestos en la transaccion.");
    ErrorObj ERROR_317 = new ErrorObj(317, "[UBL-DOCUMENT] Hubo un problema al extraer los impuestos de la transaccion.");
    ErrorObj ERROR_318 = new ErrorObj(318, "[UBL-DOCUMENT] El tipo de impuesto ingresado no es valido.");
    ErrorObj ERROR_319 = new ErrorObj(319, "[UBL-DOCUMENT] No se encontraron items en la transaccion.");
    ErrorObj ERROR_320 = new ErrorObj(320, "[UBL-DOCUMENT] Hubo un problema al extraer los items de la transaccion.");
    ErrorObj ERROR_321 = new ErrorObj(321, "[UBL-DOCUMENT] No se encontro un IMPORTE TOTAL en la transaccion.");
    ErrorObj ERROR_322 = new ErrorObj(322, "[UBL-DOCUMENT] No se encontraron impuestos en uno de los items de la transaccion.");
    ErrorObj ERROR_323 = new ErrorObj(323, "[UBL-DOCUMENT] Hubo un problema al extraer los impuestos en uno de los items de la transaccion.");
    ErrorObj ERROR_324 = new ErrorObj(324, "[UBL-DOCUMENT] El tipo de impuesto de item ingresado no es valido.");
    ErrorObj ERROR_325 = new ErrorObj(325, "[UBL-DOCUMENT] Hubo un problema al extraer la descripcion de uno de los items de la transaccion.");
    ErrorObj ERROR_326 = new ErrorObj(326, "[UBL-DOCUMENT] Hubo un problema al extraer la DESCRIPCION o el CODIGO DE ARTICULO de uno de los items de la transaccion.");
    ErrorObj ERROR_327 = new ErrorObj(327, "[UBL-DOCUMENT] Hubo un problema al extraer el DESCUENTO DE LINEA de un item de la transaccion.");
    ErrorObj ERROR_328 = new ErrorObj(328, "[UBL-DOCUMENT] Hubo un problema al generar el UBLExtension de TOTALES y PROPIEDADES.");
    ErrorObj ERROR_329 = new ErrorObj(329, "[UBL-DOCUMENT] Hubo un problema al generar el NODO con la informacion de TOTALES y PROPIEDADES.");
    ErrorObj ERROR_330 = new ErrorObj(330, "[UBL-DOCUMENT] Ocurrio un problema al extraer la informacion de TOTALES. Lista nula o vacia.");
    ErrorObj ERROR_331 = new ErrorObj(331, "[UBL-DOCUMENT] Ocurrio un problema al extraer la informacion de PROPIEDADES. Lista nula o vacia.");
    ErrorObj ERROR_332 = new ErrorObj(332, "[UBL-DOCUMENT] Ocurrio un problema al extraer el VALOR UNITARIO de un item en la transaccion.");
    ErrorObj ERROR_333 = new ErrorObj(333, "[UBL-DOCUMENT] No se encontro una lista de BillReference de un item para extraer el VALOR UNITARIO.");
    ErrorObj ERROR_334 = new ErrorObj(334, "[UBL-DOCUMENT] No se encontro un VALOR UNITARIO para un item de la transaccion.");
    ErrorObj ERROR_335 = new ErrorObj(335, "[UBL-DOCUMENT] No se encontro un IMPORTE en la transaccion.");
    ErrorObj ERROR_336 = new ErrorObj(336, "[UBL-DOCUMENT] Ocurrio un problema al extraer la informacion del ANTICIPO.");
    ErrorObj ERROR_337 = new ErrorObj(337, "[UBL-DOCUMENT] Ocurrio un problema al generar el objeto DiscrepancyResponse.");
    ErrorObj ERROR_338 = new ErrorObj(338, "[UBL-DOCUMENT] Ocurrio un problema al generar el objeto BillingReference.");
    ErrorObj ERROR_340 = new ErrorObj(340, "[UBL-DOCUMENT] La fecha de referencia es nula.");
    ErrorObj ERROR_341 = new ErrorObj(341, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto InvoiceType de la FACTURA.");
    ErrorObj ERROR_342 = new ErrorObj(342, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto InvoiceType de la BOLETA DE VENTA.");
    ErrorObj ERROR_343 = new ErrorObj(343, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto CreditNoteType.");
    ErrorObj ERROR_344 = new ErrorObj(344, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto DebitNoteType.");
    ErrorObj ERROR_345 = new ErrorObj(345, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto VoidedDocumentsType.");
    ErrorObj ERROR_347 = new ErrorObj(347, "[UBL-DOCUMENT] Hubo un problema con la fecha de referencia.");/* ErrorObj ERROR_348 = new ErrorObj(348,"[UBL-DOCUMENT] No se encontro el valor del SUBTOTAL en la transaccion.");*/
    ErrorObj ERROR_351 = new ErrorObj(351, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto PerceptionType.");
    ErrorObj ERROR_352 = new ErrorObj(352, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto RetentionType.");
    ErrorObj ERROR_353 = new ErrorObj(353, "[UBL-DOCUMENT] Hubo un problema con la hora de emision.");
    ErrorObj ERROR_356 = new ErrorObj(356, "[UBL-DOCUMENT] Hubo un problema al generar el TAG Delivery de un item en la transaccion.");
    ErrorObj ERROR_357 = new ErrorObj(357, "[UBL-DOCUMENT] Hubo un problema al generar el TAG Delivery/DeliveryTerms de un item en la transaccion.");
    ErrorObj ERROR_358 = new ErrorObj(358, "[UBL-DOCUMENT] Hubo un problema al generar el TAG <cac:Delivery><cac:Shipment> de un item en la transaccion.");
    ErrorObj ERROR_359 = new ErrorObj(359, "[UBL-DOCUMENT] Hubo un problema al generar el TAG <cac:Delivery><cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:MeasurementDimension> de un item en la transaccion.");
    ErrorObj ERROR_360 = new ErrorObj(360, "[UBL-DOCUMENT] La fecha de cobro es nula.");
    ErrorObj ERROR_361 = new ErrorObj(361, "[UBL-DOCUMENT] Hubo un problema con la fecha de cobro.");
    ErrorObj ERROR_362 = new ErrorObj(362, "[UBL-DOCUMENT] La fecha de percepcion es nula.");
    ErrorObj ERROR_363 = new ErrorObj(363, "[UBL-DOCUMENT] Hubo un problema con la fecha de percepcion.");
    ErrorObj ERROR_364 = new ErrorObj(364, "[UBL-DOCUMENT] La fecha de retencion es nula.");
    ErrorObj ERROR_365 = new ErrorObj(365, "[UBL-DOCUMENT] Hubo un problema con la fecha de retencion.");
    ErrorObj ERROR_366 = new ErrorObj(366, "[UBL-DOCUMENT] Hubo un problema al generar el TAG <cac:Shipment> de la Guia de Remision.");
    ErrorObj ERROR_367 = new ErrorObj(367, "[UBL-DOCUMENT] La fecha de inicio es nula.");
    ErrorObj ERROR_368 = new ErrorObj(368, "[UBL-DOCUMENT] Hubo un problema con la fecha de inicio.");
    ErrorObj ERROR_369 = new ErrorObj(369, "[UBL-DOCUMENT] Hubo un problema generico al construir el objeto DespatchAdviceType de la GUIA DE REMISION.");
    ErrorObj ERROR_370 = new ErrorObj(370, "[UBL-DOCUMENT] No se encontro un TOTAL PRECIO DE VENTA en la transaccion.");
    /*** Seccion: PDFCreatorDocument* <p>* Disponible: 401 - 450*/
    ErrorObj ERROR_401 = new ErrorObj(401, "[PDF] No se encontro el TEMPLATE del PDF de la Factura.");
    ErrorObj ERROR_402 = new ErrorObj(402, "[PDF] No se encontro el TEMPLATE del PDF de la Boleta de Venta.");
    ErrorObj ERROR_403 = new ErrorObj(403, "[PDF] No se encontro el TEMPLATE del PDF de la Nota de Credito.");
    ErrorObj ERROR_404 = new ErrorObj(404, "[PDF] No se encontro el TEMPLATE del PDF de la Nota de Debito.");
    ErrorObj ERROR_405 = new ErrorObj(405, "[PDF] Hubo un problema al iniciar la carga del TEMPLATE.");
    ErrorObj ERROR_406 = new ErrorObj(406, "[PDF] El objeto InvoiceObj es nulo.");
    ErrorObj ERROR_407 = new ErrorObj(407, "[PDF] El objeto BoletaObj es nulo.");
    ErrorObj ERROR_408 = new ErrorObj(408, "[PDF] El objeto CreditNoteObj es nulo.");
    ErrorObj ERROR_409 = new ErrorObj(409, "[PDF] El objeto DebitNoteObj es nulo.");
    ErrorObj ERROR_410 = new ErrorObj(410, "[PDF] No se pudo dar formato al departamento, provincia y distrito.");
    ErrorObj ERROR_411 = new ErrorObj(411, "[PDF] No se encontraron items en el objeto InvoiceType de la FACTURA.");
    ErrorObj ERROR_415 = new ErrorObj(415, "[PDF] Hubo un problema al extraer la informacion de los items.");
    ErrorObj ERROR_416 = new ErrorObj(416, "[PDF] La lista de AlternativeConditionPrice de un item es nula.");
    ErrorObj ERROR_418 = new ErrorObj(418, "[PDF] Ocurrio un problema al generar el CODIGO DE BARRAS.");
    ErrorObj ERROR_419 = new ErrorObj(419, "[PDF] Ocurrio un problema al extraer uno de los valores TOTALES.");
    ErrorObj ERROR_420 = new ErrorObj(420, "[PDF] Ocurrio un problema al extraer uno de los valores de PROPIEDADES.");
    ErrorObj ERROR_421 = new ErrorObj(421, "[PDF] El codigo del SUNATTransaction no es valido.");
    ErrorObj ERROR_422 = new ErrorObj(422, "[PDF] El codigo de moneda no tiene una definicion en texto.");
    ErrorObj ERROR_423 = new ErrorObj(423, "[PDF] El valor del DigestValue es nulo o vacio.");
    ErrorObj ERROR_424 = new ErrorObj(424, "[PDF] El valor del SignatureValue es nulo o vacio.");
    ErrorObj ERROR_425 = new ErrorObj(425, "[PDF] El tipo de identificador del receptor no es valido.");
    ErrorObj ERROR_426 = new ErrorObj(426, "[PDF] El codigo del tipo de Nota de Credito no es valido.");
    ErrorObj ERROR_427 = new ErrorObj(427, "[PDF] El codigo del tipo de Nota de Credito esta vacio.");
    ErrorObj ERROR_428 = new ErrorObj(428, "[PDF] No existe documento relacionado a la Nota de Credito. BillingReference=null");
    ErrorObj ERROR_430 = new ErrorObj(430, "[PDF] El codigo del tipo de documento relacionado no es valido.");
    ErrorObj ERROR_431 = new ErrorObj(431, "[PDF] No es posible generar los datos del RECEPTOR porque la serie del documento no es valida.");
    ErrorObj ERROR_432 = new ErrorObj(432, "[PDF] El codigo del tipo de Nota de Debito no es valido.");
    ErrorObj ERROR_433 = new ErrorObj(433, "[PDF] El codigo del tipo de Nota de Debito esta vacio.");
    ErrorObj ERROR_441 = new ErrorObj(441, "[PDF] Ocurrio un error al crear el PDF de la FACTURA.");
    ErrorObj ERROR_442 = new ErrorObj(442, "[PDF] Ocurrio un error al crear el PDF de la BOLETA DE VENTA.");
    ErrorObj ERROR_443 = new ErrorObj(443, "[PDF] Ocurrio un error al crear el PDF de la NOTA DE CREDITO.");
    ErrorObj ERROR_444 = new ErrorObj(444, "[PDF] Ocurrio un error al crear el PDF de la NOTA DE DEBITO.");
    /*** Seccion: SunatConnectorClient* <p>* Disponible: 451 - 500*/
    ErrorObj ERROR_454 = new ErrorObj(454, "[SUNAT-CONNECTOR] Hubo un problema al guardar el documento UBL en DISCO.");
    ErrorObj ERROR_455 = new ErrorObj(455, "[SUNAT-CONNECTOR] Ocurrio un error al convertir el documento UBL al formato ZIP.");
    ErrorObj ERROR_456 = new ErrorObj(456, "[SUNAT-CONNECTOR] No se pudo configurar el WS Consumidor.");
    ErrorObj ERROR_457 = new ErrorObj(457, "[SUNAT-CONNECTOR] Hubo un problema al comprimir el documento.");
    ErrorObj ERROR_458 = new ErrorObj(458, "[SUNAT-CONNECTOR] La constancia CDR retornada por Sunat es nula.");
    ErrorObj ERROR_460 = new ErrorObj(460, "[SUNAT-CONNECTOR] No es posible generar la representacion impresa porque el tipo de documento no es valido.");
    ErrorObj ERROR_461 = new ErrorObj(461, "[SUNAT-CONNECTOR] No es posible guardar el documento PDF en DISCO porque el PDF en bytes es nulo.");
    ErrorObj ERROR_462 = new ErrorObj(462, "[SUNAT-CONNECTOR] Hubo un problema al extraer un tipo de impuesto.");
    ErrorObj ERROR_464 = new ErrorObj(464, "[SUNAT-CONNECTOR] No se pudo configurar el WS Consumidor de Consultas.");
    /*** Seccion: CHECK* <p>* Disponible: 501 - 550*/
    ErrorObj ERROR_501 = new ErrorObj(501, "[CHECK] Ocurrio un problema al extraer uno de los valores TOTALES.");
    ErrorObj ERROR_502 = new ErrorObj(502, "[CHECK] No se encontro el TAG de totales de la OP.GRAVADA, pero existen items con dicho tipo de operacion.");
    ErrorObj ERROR_503 = new ErrorObj(503, "[CHECK] No se encontro items con OP.GRAVADA, pero existe el TAG de totales de este tipo.");
    ErrorObj ERROR_504 = new ErrorObj(504, "[CHECK] No se encontro el TAG de totales de la OP.INAFECTA, pero existen items con dicho tipo de operacion.");
    ErrorObj ERROR_505 = new ErrorObj(505, "[CHECK] No se encontro items con OP.INAFECTA, pero existe el TAG de totales de este tipo.");
    ErrorObj ERROR_506 = new ErrorObj(506, "[CHECK] No se encontro el TAG de totales de la OP.EXONERADA, pero existen items con dicho tipo de operacion.");
    ErrorObj ERROR_507 = new ErrorObj(507, "[CHECK] No se encontro items con OP.EXONERADA, pero existe el TAG de totales de este tipo.");
    ErrorObj ERROR_508 = new ErrorObj(508, "[CHECK] No se encontro el TAG de totales de la OP.GRATUITA, pero existen items con dicho tipo de operacion.");
    ErrorObj ERROR_509 = new ErrorObj(509, "[CHECK] No se encontro items con OP.GRATUITA, pero existe el TAG de totales de este tipo.");
    ErrorObj ERROR_510 = new ErrorObj(510, "[CHECK] No se encontro el TAG de impuesto de IGV TOTAL, pero existen items con dicho tipo de impuesto.");
    ErrorObj ERROR_511 = new ErrorObj(511, "[CHECK] No se encontro items con impuesto de tipo IGV, pero existe el TAG de impuesto de IGV TOTAL.");
    ErrorObj ERROR_512 = new ErrorObj(512, "[CHECK] No se encontro el TAG de impuesto de ISC TOTAL, pero existen items con dicho tipo de impuesto.");
    ErrorObj ERROR_513 = new ErrorObj(513, "[CHECK] No se encontro items con impuesto de tipo ISC, pero existe el TAG de impuesto de ISC TOTAL.");
    ErrorObj ERROR_514 = new ErrorObj(514, "[CHECK] La SERIE-CORRELATIVO del documento es nulo.");
    ErrorObj ERROR_515 = new ErrorObj(515, "[CHECK] La SERIE-CORRELATIVO tiene mas caracteres de lo permitido. (Caracteres permitidos=" + IUBLConfig.SERIE_CORRELATIVE_LENGTH + ")");
    ErrorObj ERROR_516 = new ErrorObj(516, "[CHECK] La SERIE-CORRELATIVO no tiene el formato correcto.");
    ErrorObj ERROR_517 = new ErrorObj(517, "[CHECK] La SERIE-CORRELATIVO no tiene el formato correcto. Debe iniciar con F o P o B.");
    ErrorObj ERROR_518 = new ErrorObj(518, "[CHECK] El CORRELATIVO del identificador del documento no tiene el formato correcto.");
    ErrorObj ERROR_519 = new ErrorObj(519, "[CHECK] El Numero RUC del emisor debe contener 11 caracteres.");
    ErrorObj ERROR_520 = new ErrorObj(520, "[CHECK] El Numero RUC no contiene el formato correcto.");
    ErrorObj ERROR_521 = new ErrorObj(521, "[CHECK] La fecha de emision es nula.");
    ErrorObj ERROR_522 = new ErrorObj(522, "[CHECK] No se encontro el TAG de DESCUENTO TOTAL, pero existen items con DESCUENTO DE LINEA.");
    ErrorObj ERROR_523 = new ErrorObj(523, "[CHECK] No se encontro el TAG de DESCUENTO TOTAL, pero existe descuento a nivel GLOBAL.");
    ErrorObj ERROR_524 = new ErrorObj(524, "[CHECK] El codigo [{0}] de TAG de TOTALES no es valido.");
    ErrorObj ERROR_525 = new ErrorObj(525, "[CHECK] El codigo de tipo de nota de credito no es valido.");
    ErrorObj ERROR_526 = new ErrorObj(526, "[CHECK] El codigo de tipo de nota de debito no es valido.");
    ErrorObj ERROR_527 = new ErrorObj(527, "[CHECK] La lista de documentos referenciados a la nota electronica esta vacia.");
    ErrorObj ERROR_528 = new ErrorObj(528, "[CHECK] El codigo de documento referenciado a la nota electronica no es valido.");
    ErrorObj ERROR_529 = new ErrorObj(529, "[CHECK] El IDENTIFICADOR del documento es nulo.");
    ErrorObj ERROR_530 = new ErrorObj(530, "[CHECK] El IDENTIFICADOR no tiene el formato correcto. Debe iniciar con RA o RC.");
    ErrorObj ERROR_549 = new ErrorObj(549, "[CHECK] El Cliente debe tener un correo electronico.");
    ErrorObj ERROR_550 = new ErrorObj(550, "[CHECK] La sociedad debe tener un correo electronico.");
    /*** PERCEPCION CHECK*/
    ErrorObj ERROR_531 = new ErrorObj(531, "[CHECK] El TAG SunatTotalCashed debe de ser mayor a CERO.");
    ErrorObj ERROR_532 = new ErrorObj(532, "[CHECK] El TAG TotalInvoiceAmount debe de ser mayor a CERO.");
    ErrorObj ERROR_533 = new ErrorObj(533, "[CHECK] Se encontro items TAG PaidAmount que son menores a CERO.");
    ErrorObj ERROR_534 = new ErrorObj(534, "[CHECK] Se encontro items TAG TotalInvoiceAmount que son menores a CERO.");
    ErrorObj ERROR_535 = new ErrorObj(535, "[CHECK] Se encontro items TAG PerceptionAmount que son menores a CERO.");
    ErrorObj ERROR_536 = new ErrorObj(536, "[CHECK] Se encontro items TAG SunatNetTotalCashed que son menores a CERO.");
    ErrorObj ERROR_1000 = new ErrorObj(1000, "[CHECK] El ubigeo del cliente no cumple con el formato establecido .");
    ErrorObj ERROR_1001 = new ErrorObj(1001, "[CHECK] El ubigeo del emisor no cumple con el formato establecido .");
    /***** RETENCION CHECK** ****/
    ErrorObj ERROR_540 = new ErrorObj(540, "[CHECK] El TAG SunatRetentionSystemCode debe contener solo DOS caracteres.");
    ErrorObj ERROR_541 = new ErrorObj(541, "[CHECK] El TAG SunatRetentionPercent debe contener solo UN caracter.");
    ErrorObj ERROR_542 = new ErrorObj(542, "[CHECK] El TAG TotalInvoiceAmount debe ser mayor a CERO.");
    ErrorObj ERROR_543 = new ErrorObj(543, "[CHECK] El TAG El TAG TotalPaid debe contener solo UN caracter debe ser mayor a CERO.");

    ErrorObj ERROR_1100 = new ErrorObj(1100, "[CHECK] El ubigeo del cliente no cumple con el formato establecido .");
    ErrorObj ERROR_1101 = new ErrorObj(1101, "[CHECK] El ubigeo del emisor no cumple con el formato establecido .");
    /**
     * ERROR DE CONEXION A INTERNET
     */
    ErrorObj ERROR_5000 = new ErrorObj(5000, "[CONNECTION] No se puede establecer conexion a internet.");
}
