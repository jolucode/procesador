package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.TransactionImpuestosDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.DespatchAdviceObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.DespatchAdvicePDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATRetentionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DespatchAdvicePDFBuilder implements DespatchAdvicePDFGenerator {

    private static final Logger logger = Logger.getLogger(DespatchAdvicePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generateDespatchAdvicePDF(UBLDocumentWRP despatchAdvice, ConfigData configData) {

        if (logger.isDebugEnabled()) {
            logger.debug("+generateDespatchAdvicePDF() [" + this.docUUID + "]");
        }
        byte[] despatchInBytes = null;

        try {
            DespatchAdviceObject despatchAdviceObject = new DespatchAdviceObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            despatchAdviceObject.setCodigoEmbarque(despatchAdvice.getTransaccion().getTransactionGuias().getCodigoPuerto());
            despatchAdviceObject.setCodigoMotivoTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getCodigoMotivo());
            despatchAdviceObject.setDescripcionMotivoTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getDescripcionMotivo());
            despatchAdviceObject.setDireccionDestino(despatchAdvice.getTransaccion().getSN_DIR_Direccion());
            despatchAdviceObject.setDireccionPartida(despatchAdvice.getTransaccion().getTransactionGuias().getDireccionPartida());
            despatchAdviceObject.setDocumentoConductor(despatchAdvice.getTransaccion().getTransactionGuias().getDocumentoConductor());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            String fechaEmision = format.format(despatchAdvice.getTransaccion().getDOC_FechaEmision());
            despatchAdviceObject.setFechaEmision(fechaEmision);
            String fechaInicioTraslado = format.format(despatchAdvice.getTransaccion().getTransactionGuias().getFechaInicioTraslado());
            despatchAdviceObject.setFechaTraslado(fechaInicioTraslado);
            despatchAdviceObject.setModalidadTraslado(despatchAdvice.getTransaccion().getTransactionGuias().getModalidadTraslado());
            despatchAdviceObject.setNombreConsumidor(despatchAdvice.getTransaccion().getSN_RazonSocial());
            despatchAdviceObject.setNombreEmisor(despatchAdvice.getTransaccion().getRazonSocial());
            despatchAdviceObject.setNumeroBultos(despatchAdvice.getTransaccion().getTransactionGuias().getNumeroBultos());
            despatchAdviceObject.setNumeroContenedor(despatchAdvice.getTransaccion().getTransactionGuias().getNumeroContenedor());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getTransaccion().getDOC_Serie() + "-" + despatchAdvice.getTransaccion().getDOC_Numero());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservacione());
            despatchAdviceObject.setPesoBruto(despatchAdvice.getTransaccion().getTransactionGuias().getPeso());
            despatchAdviceObject.setTipoDocumentoConductor(despatchAdvice.getTransaccion().getTransactionGuias().getTipoDocConductor());
            despatchAdviceObject.setTipoDocumentoTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getTipoDOCTransportista());
            despatchAdviceObject.setUMPesoBruto(despatchAdvice.getTransaccion().getTransactionGuias().getUnidadMedida());
            despatchAdviceObject.setNumeroDocConsumidor(despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro());
            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());

            if (despatchAdvice.getTransaccion().getTransactionGuias().getModalidadTraslado().equalsIgnoreCase("01")) {
                despatchAdviceObject.setPlacaVehiculo(despatchAdvice.getTransaccion().getTransactionGuias().getPlacaVehiculo());
                despatchAdviceObject.setLicenciaConducir(despatchAdvice.getTransaccion().getTransactionGuias().getLicenciaConductor());
                despatchAdviceObject.setRUCTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getRUCTransporista());
                despatchAdviceObject.setNombreTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getNombreRazonTransportista());
            } else {
                despatchAdviceObject.setRUCTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getRUCTransporista());
                despatchAdviceObject.setNombreTransportista(despatchAdvice.getTransaccion().getTransactionGuias().getNombreRazonTransportista());
            }

            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < despatchAdvice.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Agregando datos al HashMap");
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de transaccionLineasCamposUsuario
                Map<String, String> camposUsuarioMap = despatchAdvice.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();

                // Iterar sobre las entradas del mapa
                for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                    String nombreCampo = entry.getKey();
                    String valorCampo = entry.getValue();

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Extrayendo Campos " + nombreCampo);
                    }

                    itemObjectHash.put(nombreCampo, valorCampo);
                    newlist.add(valorCampo);

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Nuevo Tamaño " + newlist.size());
                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }

            if (despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS() != null
                    && !despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS().isEmpty()) {

                Map<String, String> hashedMap = new HashMap<>();

                // Iterar sobre cada mapa en la lista
                for (Map<String, String> map : despatchAdvice.getTransaccion().getTransactionContractDocRefListDTOS()) {
                    // Asumimos que cada mapa contiene un único par clave-valor
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        hashedMap.put(entry.getKey(), entry.getValue());
                    }
                }

                despatchAdviceObject.setDespatchAdvicePersonalizacion(hashedMap);
            }

            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getAdviceType().getID().getValue());
            despatchAdviceObject.setTelefono(despatchAdvice.getTransaccion().getTelefono());
            despatchAdviceObject.setTelefono1(despatchAdvice.getTransaccion().getTelefono_1());
            despatchAdviceObject.setEmail(despatchAdvice.getTransaccion().getEMail());
            despatchAdviceObject.setPaginaWeb(despatchAdvice.getTransaccion().getWeb());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservacione());
            despatchAdviceObject.setItemListDynamic(listaItem);

            for (int i = 0; i < despatchAdviceObject.getItemListDynamic().size(); i++) {

                for (int j = 0; j < despatchAdviceObject.getItemListDynamic().get(i).getLstDinamicaItem().size(); j++) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + despatchAdviceObject.getItemListDynamic().get(i).getLstDinamicaItem().get(j));
                    }

                }
            }

            String barcodeValue = generateBarCodeInfoString(despatchAdvice.getTransaccion().getDocIdentidad_Nro(), despatchAdvice.getTransaccion().getDOC_Codigo(), despatchAdvice.getTransaccion().getDOC_Serie(), despatchAdvice.getTransaccion().getDOC_Numero(), null, despatchAdvice.getTransaccion().getDOC_FechaEmision().toString(), "00", despatchAdvice.getTransaccion().getSN_DocIdentidad_Tipo(), despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro(), despatchAdvice.getAdviceType().getUBLExtensions());

            //String barcodeValue = generateBarCodeInfoString(despatchAdvice.getInvoiceType().getID().getValue(), despatchAdvice.getInvoiceType().getInvoiceTypeCode().getValue(), despatchAdvice.getAdviceType().getIssueDate(), despatchAdvice.getTransaccion().getDOCNumero(), null, invoiceType.getInvoiceType().getAccountingSupplierParty(), despatchAdvice.getInvoiceType().getAccountingCustomerParty(), despatchAdvice.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                //logger.info("generateInvoicePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //despatchAdviceObject.setBarcodeValue(barcodeValue);

            InputStream inputStream;
            InputStream inputStreamPDF;
            //String rutaPath = ADJUNTOS + File.separator + "CodigoQR" + File.separator + "09" + File.separator + despatchAdvice.getTransaccion().getDOCNumero() + ".png";
            //File f = new File(ADJUNTOS + File.separator + "CodigoQR" + File.separator + "09");
            // if (!f.exists()) {
            //     f.mkdirs();
            //}

            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "09");
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "09" + File.separator + despatchAdvice.getTransaccion().getDOC_Numero() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            despatchAdviceObject.setCodeQR(inputStream);

            despatchAdviceObject.setSenderLogo("C:\\clientes\\files\\20510910517\\COMPANY_LOGO.jpg");

            despatchInBytes = createDespatchAdvicePDF(despatchAdviceObject, docUUID, configData);

        } catch (PDFReportException e) {
            logger.error("generateDespatchAdvicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateDespatchAdvicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateDespatchAdvicePDF() [" + this.docUUID + "]");
        }
        return despatchInBytes;
    }

    public byte[] createDespatchAdvicePDF(DespatchAdviceObject despatchAdviceObject, String docUUID, ConfigData configuracion) throws PDFReportException {

        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;

        if (logger.isDebugEnabled()) {
            logger.debug("+createDespatchAdvicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == despatchAdviceObject) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {

                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.CODIGO_EMBARQUE, despatchAdviceObject.getCodigoEmbarque());
                parameterMap.put(IPDFCreatorConfig.CODIGO_MOTIVO, despatchAdviceObject.getCodigoMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DESCRIPCION_MOTIVO, despatchAdviceObject.getDescripcionMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_DESTINO, despatchAdviceObject.getDireccionDestino());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_PARTIDA, despatchAdviceObject.getDireccionPartida());
                parameterMap.put(IPDFCreatorConfig.DOCUMENTO_CONDUCTOR, despatchAdviceObject.getDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.FECHA_EMISION, despatchAdviceObject.getFechaEmision());
                parameterMap.put(IPDFCreatorConfig.FECHA_TRASLADO, despatchAdviceObject.getFechaTraslado());
                parameterMap.put(IPDFCreatorConfig.MODALIDAD_TRASLADO, despatchAdviceObject.getModalidadTraslado());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_CONSUMIDOR, despatchAdviceObject.getNombreConsumidor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_EMISOR, despatchAdviceObject.getNombreEmisor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_TRANSPORTISTA, despatchAdviceObject.getNombreTransportista());
                parameterMap.put(IPDFCreatorConfig.NUMERO_BULTOS, despatchAdviceObject.getNumeroBultos());
                parameterMap.put(IPDFCreatorConfig.PLACA_VEHICULO, despatchAdviceObject.getPlacaVehiculo());
                parameterMap.put(IPDFCreatorConfig.RUC_TRANSPORTISTA, despatchAdviceObject.getRUCTransportista());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_CONDUCTOR, despatchAdviceObject.getTipoDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_TRANSPORTISTA, despatchAdviceObject.getTipoDocumentoTransportista());
                parameterMap.put(IPDFCreatorConfig.UNIDAD_MEDIDA_PESONETO, despatchAdviceObject.getUMPesoBruto());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, despatchAdviceObject.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, despatchAdviceObject.getNumeroGuia());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, despatchAdviceObject.getEmail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, despatchAdviceObject.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, despatchAdviceObject.getTelefono1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, despatchAdviceObject.getPaginaWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, despatchAdviceObject.getObervaciones());
                parameterMap.put(IPDFCreatorConfig.RUC_CONSUMIDOR, despatchAdviceObject.getNumeroDocConsumidor());
                parameterMap.put(IPDFCreatorConfig.RUC_EMISOR, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, despatchAdviceObject.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.LICENCIA_CONDUCIR, despatchAdviceObject.getLicenciaConducir());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, despatchAdviceObject.getDespatchAdvicePersonalizacion());

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, despatchAdviceObject.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }
                String documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "remissionguideDocument_Ing.jrxml" : "remissionguideDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(despatchAdviceObject.getRUCTransportista(),documentName );

                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(despatchAdviceObject.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument =  outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createDespatchAdvicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createDespatchAdvicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createDespatchAdvicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createInvoicePDF

    protected String getRemissionGuides(
            List<DocumentReferenceType> despatchDocumentReferences) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getRemissionGuides()");
        }
        String remissionGuides = null;

        if (null != despatchDocumentReferences
                && 0 < despatchDocumentReferences.size()) {
            for (DocumentReferenceType despatchDocRef : despatchDocumentReferences) {
                if (null == remissionGuides) {
                    remissionGuides = "";
                } else {
                    remissionGuides += ", ";
                }

                remissionGuides += despatchDocRef.getID().getValue();
            }
        } else {
            remissionGuides = IPDFCreatorConfig.EMPTY_VALUE;
        }

        return remissionGuides;
    }

    protected String formatIssueDate(XMLGregorianCalendar xmlGregorianCal)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+formatIssueDate() [" + this.docUUID + "]");
        }
        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();

        Locale locale = new Locale(IPDFCreatorConfig.LOCALE_ES,
                IPDFCreatorConfig.LOCALE_PE);

        SimpleDateFormat sdf = new SimpleDateFormat(
                IPDFCreatorConfig.PATTERN_DATE, locale);
        String issueDate = sdf.format(inputDate);

        if (logger.isDebugEnabled()) {
            logger.debug("-formatIssueDate() [" + this.docUUID + "]");
        }
        return issueDate;
    }

    protected String getCurrencyV3(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;

        /*
         * Obtener el objeto Locale de PERU
         */
        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);

        /*
         * Obtener el formato de moneda local
         */
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);

            /*
             * Establecer el currency en el formato
             */
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        "###,###.00", new DecimalFormatSymbols(Locale.US));
                //System.out.println(decF.format(money));
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected BigDecimal getTransaccionTotales(List<TransactionTotalesDTO> lstTotales, String addiMonetaryValue) {

        if (lstTotales != null) {
            if (lstTotales.size() > 0) {
                for (int i = 0; i < lstTotales.size(); i++) {
                    if (lstTotales.get(i).getId().equalsIgnoreCase(addiMonetaryValue)) {
                        return lstTotales.get(i).getMonto();
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    protected List<InvoiceItemObject> getInvoiceItems(
            List<InvoiceLineType> invoiceLines) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getInvoiceItems() [" + this.docUUID
                    + "] invoiceLines: " + invoiceLines);
        }
        List<InvoiceItemObject> itemList = null;

        if (null != invoiceLines && 0 < invoiceLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<InvoiceItemObject>(invoiceLines.size());

            try {
                for (InvoiceLineType iLine : invoiceLines) {
                    InvoiceItemObject invoiceItemObj = new InvoiceItemObject();

                    invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity()
                            .getValue());
                    // invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity().getValue().setScale(IPDFCreatorConfig.DECIMAL_ITEM_QUANTITY,
                    // RoundingMode.HALF_UP).toString());
                    invoiceItemObj
                            .setUnitMeasureItem((null != iLine.getNote()) ? iLine
                                    .getNote().get(0).getValue() : "");
                    invoiceItemObj.setDescriptionItem(iLine.getItem()
                            .getDescription().get(0).getValue().toUpperCase());
                    invoiceItemObj.setUnitValueItem(iLine.getPrice()
                            .getPriceAmount().getValue());
                    // invoiceItemObj.setUnitValueItem(getCurrencyV2(iLine.getPrice().getPriceAmount().getValue(),
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    BigDecimal unitPrice = getPricingReferenceValue(iLine
                                    .getPricingReference()
                                    .getAlternativeConditionPrice(),
                            IPDFCreatorConfig.ALTERNATIVE_COND_UNIT_PRICE);
                    invoiceItemObj.setUnitPriceItem(unitPrice);
                    // invoiceItemObj.setUnitPriceItem(getCurrencyV2(unitPrice,
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    invoiceItemObj.setDiscountItem(getCurrency(
                            getDiscountItem(iLine.getAllowanceCharge()), null));
                    invoiceItemObj.setAmountItem(getCurrency(iLine
                            .getLineExtensionAmount().getValue(), null));

                    itemList.add(invoiceItemObj);
                }
            } catch (PDFReportException e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + IVenturaError.ERROR_415.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_415);
            }
        } else {
            logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_411.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_411);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getInvoiceItems()");
        }
        return itemList;
    } // getInvoiceItems

    protected String getCurrency(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;

        /*
         * Obtener el objeto Locale de PERU
         */
        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);

        /*
         * Obtener el formato de moneda local
         */
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);

            /*
             * Establecer el currency en el formato
             */
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        IPDFCreatorConfig.PATTERN_FLOAT_DEC);
                //System.out.println(decF.format(money));
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected BigDecimal getTaxTotalValue2(List<TransactionImpuestosDTO> taxTotalList,
                                           String taxTotalCode) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTaxTotalValue() [" + this.docUUID
                    + "] taxTotalCode: " + taxTotalCode);
        }
        BigDecimal taxValue = BigDecimal.ZERO;

        for (int i = 0; i < taxTotalList.size(); i++) {
            if (taxTotalList.get(i).getTipoTributo().equalsIgnoreCase(taxTotalCode)) {
                taxValue = taxValue.add(taxTotalList.get(i).getMonto());
                if (logger.isDebugEnabled()) {
                    logger.debug("+-getTaxTotalValue() [" + this.docUUID
                            + "] taxValue: " + taxValue);
                }
            }
        }

        if (null == taxTotalList && 0 > taxTotalList.size()) {
            logger.error("getTaxTotalValue() ERROR: "
                    + IVenturaError.ERROR_462.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_462);
        }
        return taxValue;
    }

    public String generateBarCodeInfoString(String RUC_emisor_electronico, String documentType, String serie, String correlativo, List<TaxTotalType> taxTotalList, String issueDate, String Importe_total_venta, String Tipo_documento_adquiriente, String Numero_documento_adquiriente, UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = "";
        try {

            /***/
            String digestValue = getDigestValue(ublExtensions);
            logger.debug("Digest Value" + digestValue);

            /**El elemento opcional KeyInfo contiene información sobre la llave que se necesita para validar la firma, como lo muestra*/
            String signatureValue = getSignatureValue(ublExtensions);
            logger.debug("signatureValue" + signatureValue);

            String Sumatoria_IGV = "";
            if (taxTotalList != null) {
                Sumatoria_IGV = getTaxTotalValueV21(taxTotalList).toString();
                logger.debug("Sumatoria_IGV" + Sumatoria_IGV);
            }
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, RUC_emisor_electronico, documentType, serie, correlativo, Sumatoria_IGV, Importe_total_venta, issueDate, Tipo_documento_adquiriente, Numero_documento_adquiriente, digestValue);

        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }

        return barcodeValue;
    }

    public String generateDigestValue(UBLExtensionsType ublExtensions)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateDigestValueInfo()");
        }

        String digestValue = null;
        try {


            /* i.) Valor resumen <ds:DigestValue> */
            digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */

        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateBarcodeInfo()");
        }
        return digestValue;
    }

    private String getDigestValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String digestValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_DIGESTVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_DIGESTVALUE_TAG)) {
                    digestValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(digestValue)) {
                throw new PDFReportException(IVenturaError.ERROR_423);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("getDigestValue() Exception -->" + e.getMessage());
            throw e;
        }
        return digestValue;
    } // getDigestValue

    protected Map<String, LegendObject> getaddLeyends(List<NoteType> lstNote) throws PDFReportException {
        Map<String, LegendObject> legendsMap = null;
        String id = null;
        String value = null;

        try {
            legendsMap = new HashMap<String, LegendObject>();
            if (lstNote != null) {
                if (lstNote.size() > 0) {
                    for (int i = 0; i < lstNote.size(); i++) {
                        LegendObject legendObj = new LegendObject();
                        legendObj.setLegendValue(lstNote.get(i).getValue());
                        legendsMap.put(lstNote.get(i).getLanguageLocaleID(), legendObj);
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("getAdditionalProperties() [" + this.docUUID
                    + "] ERROR: " + IVenturaError.ERROR_420.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_420);
        }
        return legendsMap;
    }


    protected List<LegendObject> getLegendList(
            Map<String, LegendObject> legendsMap) {
        List<LegendObject> legendList = null;
        if (null != legendsMap && 0 < legendsMap.size()) {
            legendList = new ArrayList<LegendObject>(legendsMap.values());
        } else {
            /*
             * No existe LEYENDAS.
             */
            LegendObject legendObj = new LegendObject();
            legendObj.setLegendValue(IPDFCreatorConfig.LEGEND_DEFAULT_EMPTY);

            legendList = new ArrayList<LegendObject>();
            legendList.add(legendObj);
        }

        return legendList;
    } // getLegendList

    private String getSignatureValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String signatureValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SIGNATUREVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SIGNATUREVALUE_TAG)) {
                    signatureValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(signatureValue)) {
                throw new PDFReportException(IVenturaError.ERROR_424);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("getSignatureValue() Exception -->" + e.getMessage());
            throw e;
        }
        return signatureValue;
    } // getSignatureValue

    protected BigDecimal getTaxTotalValueV21(List<TaxTotalType> taxTotalList) {

        for (int i = 0; i < taxTotalList.size(); i++) {
            for (int j = 0; j < taxTotalList.get(i).getTaxSubtotal().size(); j++) {
                if (taxTotalList.get(i).getTaxSubtotal().get(j).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase("1000")) {
                    return taxTotalList.get(i).getTaxAmount().getValue();
                }
            }

        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getDiscountItem(
            List<AllowanceChargeType> allowanceCharges) {
        BigDecimal value = BigDecimal.ZERO;

        if (null != allowanceCharges && 0 < allowanceCharges.size()) {
            /*
             * Se entiende que en el caso exista una lista, solo debe tener un
             * solo valor
             */
            value = allowanceCharges.get(0).getAmount().getValue();
        }
        return value;
    } // getDiscountItem

    private BigDecimal getPricingReferenceValue(
            List<PriceType> alternativeConditionPrice, String priceTypeCode)
            throws PDFReportException {
        BigDecimal value = null;

        if (null != alternativeConditionPrice
                && 0 < alternativeConditionPrice.size()) {
            for (PriceType altCondPrice : alternativeConditionPrice) {
                if (altCondPrice.getPriceTypeCode().getValue()
                        .equalsIgnoreCase(priceTypeCode)) {
                    value = altCondPrice.getPriceAmount().getValue();
                }
            }

            //if (null == value) {
            //  throw new PDFReportException(IVenturaError.ERROR_417);
            //}
        } else {
            throw new PDFReportException(IVenturaError.ERROR_416);
        }
        return value;
    } // getPricingReferenceValue

    public static InputStream generateQRCode(String qrCodeData, String filePath) {

        try {

            String charset = "utf-8"; // or "ISO-8859-1"
            Map hintMap = new HashMap();

            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException | IOException ex) {
            //java.util.logging.Logger.getLogger(PDFGenerateHandler.class.getName()).log(Level.SEVERE, null, ex);
            logger.error(ex.getMessage());
        }
        return null;
    }

    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
    }

    public static InputStream generatePDF417Code(String qrCodeData, String filePath, int width, int height, int margin) {

        try {
            BitMatrix bitMatrixFromEncoder = null;
            PDF417 objPdf417 = new PDF417();
            objPdf417.generateBarcodeLogic(qrCodeData, 5);
            objPdf417.setEncoding(StandardCharsets.UTF_8);
            int aspectRatio = 4;
            byte[][] originalScale = objPdf417.getBarcodeMatrix().getScaledMatrix(1, aspectRatio);

            boolean rotated = false;
            if ((height > width) != (originalScale[0].length < originalScale.length)) {
                originalScale = rotateArray(originalScale);
                rotated = true;
            }

            int scaleX = width / originalScale[0].length;
            int scaleY = height / originalScale.length;

            int scale;
            if (scaleX < scaleY) {
                scale = scaleX;
            } else {
                scale = scaleY;
            }

            if (scale > 1) {
                byte[][] scaledMatrix = objPdf417.getBarcodeMatrix().getScaledMatrix(scale, scale * aspectRatio);
                if (rotated) {
                    scaledMatrix = rotateArray(scaledMatrix);
                }
                bitMatrixFromEncoder = bitMatrixFromBitArray(scaledMatrix, margin);
            }
            bitMatrixFromEncoder = bitMatrixFromBitArray(originalScale, margin);

            MatrixToImageWriter.writeToFile(bitMatrixFromEncoder, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException ex) {

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;

    }

    private static BitMatrix bitMatrixFromBitArray(byte[][] input, int margin) {
        // Creates the bit matrix with extra space for whitespace
        BitMatrix output = new BitMatrix(input[0].length + 2 * margin, input.length + 2 * margin);
        output.clear();
        for (int y = 0, yOutput = output.getHeight() - margin - 1; y < input.length; y++, yOutput--) {
            byte[] inputY = input[y];
            for (int x = 0; x < input[0].length; x++) {
                // Zero is white in the byte matrix
                if (inputY[x] == 1) {
                    output.set(x + margin, yOutput);
                }
            }
        }
        return output;
    }

    private static byte[][] rotateArray(byte[][] bitarray) {
        byte[][] temp = new byte[bitarray[0].length][bitarray.length];
        for (int ii = 0; ii < bitarray.length; ii++) {
            // This makes the direction consistent on screen when rotating the
            // screen;
            int inverseii = bitarray.length - ii - 1;
            for (int jj = 0; jj < bitarray[0].length; jj++) {
                temp[jj][inverseii] = bitarray[ii][jj];
            }
        }
        return temp;
    }

    protected String formatDepProvDist(AddressType postalAddress)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-formatDepProvDist() [" + this.docUUID + "]");
        }
        String depProvDist = null;
        if (null != postalAddress) {
            String department = postalAddress.getCountrySubentity().getValue();
            String province = postalAddress.getCityName().getValue();
            String district = postalAddress.getDistrict().getValue();

            depProvDist = district + " - " + province + " - " + department;

        } else {
            throw new PDFReportException(IVenturaError.ERROR_410);
        }
        return depProvDist;
    } // formatDepProvDist

    protected List<RetentionItemObject> getRetentionItems(
            List<SUNATRetentionDocumentReferenceType> retentionLines,
            String porcentaje) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getInvoiceItems() [" + this.docUUID
                    + "] invoiceLines: " + retentionLines);
        }
        List<RetentionItemObject> itemList = null;

        if (null != retentionLines && 0 < retentionLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<RetentionItemObject>(retentionLines.size());

            try {
                for (SUNATRetentionDocumentReferenceType iLine : retentionLines) {
                    RetentionItemObject retencionItemObj = new RetentionItemObject();

                    retencionItemObj.setFechaEmision(formatIssueDate(iLine.getIssueDate().getValue()));
                    retencionItemObj.setFechaPago(formatIssueDate(iLine.getPayment().getPaidDate().getValue()));
                    retencionItemObj.setTipoDocumento(iLine.getId().getSchemeID());
                    retencionItemObj.setNroDoc(iLine.getId().getValue());
                    retencionItemObj.setMontoTotal(iLine.getTotalInvoiceAmount().getValue().toString());
                    retencionItemObj.setMonedaMontoTotal(iLine.getTotalInvoiceAmount().getCurrencyID() + " ");
                    retencionItemObj.setMonedaRetencion("PEN ");
                    retencionItemObj.setValorRetencion(iLine.getSunatRetentionInformation().getSunatRetentionAmount().getValue().toString());
                    //invoiceItemObj.setMontoSoles(iLine.getPayment().getPaidAmount().getValue().multiply(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue()).toString());
                    if (iLine.getId().getSchemeID().equalsIgnoreCase("07")) {
                        retencionItemObj.setMontoSoles(iLine.getPayment().getPaidAmount().getValue().multiply(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue()).toString());
                    } else {
                        retencionItemObj.setMontoSoles(iLine.getSunatRetentionInformation().getSunatRetentionAmount().getValue().add(iLine.getSunatRetentionInformation().getSunatNetTotalPaid().getValue()).toString());
                    }
                    retencionItemObj.setNumeroPago(iLine.getPayment().getID().getValue());

                    if (iLine.getId().getSchemeID().equalsIgnoreCase("07")) {
                        retencionItemObj.setImporteNeto(BigDecimal.ZERO);
                    } else {
                        retencionItemObj.setImporteNeto(iLine.getSunatRetentionInformation().getSunatNetTotalPaid().getValue());
                    }
                    retencionItemObj.setTipoCambio(iLine.getSunatRetentionInformation().getExchangeRate().getCalculationRate().getValue().toString());
                    BigDecimal bigDecimal = new BigDecimal(porcentaje);
                    bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
                    retencionItemObj.setPorcentaje(bigDecimal);
                    itemList.add(retencionItemObj);
                }
            } catch (PDFReportException e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + IVenturaError.ERROR_415.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_415);
            }
        } else {
            logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_411.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_411);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getInvoiceItems()");
        }
        return itemList;
    } // getInvoiceItems

    public String generateBarcodeInfoV2(String identifier,
                                        String documentType, String issueDate, BigDecimal payableAmountVal,
                                        BigDecimal taxTotalList,
                                        PartyType accSupplierParty,
                                        PartyType accCustomerParty, UBLExtensionsType ublExtensions)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateBarcodeInfo()");
        }
        String barcodeValue = null;

        try {
            /* a.) Numero de RUC del emisor electronico */
            String senderRuc = accSupplierParty.getPartyIdentification().get(0).getID().getValue();

            /* b.) Tipo de comprobante de pago electronico */
            /* Parametro de entrada del metodo */

            /* c.) Numeracion conformada por serie y numero correlativo */
            String serie = identifier.substring(0, 4);
            String correlative = Integer.valueOf(identifier.substring(5))
                    .toString();

            /* d.) Sumatoria IGV, de ser el caso */
            String igvTax = null;
            BigDecimal igvTaxBigDecimal = BigDecimal.ZERO;

            igvTax = igvTaxBigDecimal.setScale(2, RoundingMode.HALF_UP)
                    .toString();

            /* e.) Importe total de la venta, cesion en uso o servicio prestado */
            String payableAmount = payableAmountVal.toString();

            /* f.) Fecha de emision */
            /* Parametro de entrada del metodo */

            /* g,) Tipo de documento del adquiriente o usuario */
            String receiverDocType = accCustomerParty.getPartyIdentification()
                    .get(0).getID().getSchemeID();

            /* h.) Numero de documento del adquiriente */
            String receiverDocNumber = accCustomerParty.getPartyIdentification()
                    .get(0).getID().getValue();

            /* i.) Valor resumen <ds:DigestValue> */
            String digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */
            String signatureValue = getSignatureValue(ublExtensions);

            /*
             * Armando el codigo de barras
             */
            barcodeValue = MessageFormat.format(
                    IPDFCreatorConfig.BARCODE_PATTERN, senderRuc, documentType,
                    serie, correlative, igvTax, payableAmount, issueDate,
                    receiverDocType, receiverDocNumber, digestValue,
                    signatureValue);
        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateBarcodeInfo()");
        }
        return barcodeValue;
    }// generateBarcodeInfo}
}
