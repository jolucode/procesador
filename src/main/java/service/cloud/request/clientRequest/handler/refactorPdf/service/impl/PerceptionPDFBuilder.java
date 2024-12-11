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
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.PerceptionObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.PerceptionPDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATPerceptionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PerceptionPDFBuilder implements PerceptionPDFGenerator {

    private static final Logger logger = Logger.getLogger(PerceptionPDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generatePerceptionPDF(UBLDocumentWRP perceptionType, ConfigData configData) {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateInvoicePDF() [" + this.docUUID + "]");
        }
        byte[] perceptionBytes = null;

        try {
            PerceptionObject perceptionObj = new PerceptionObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            perceptionObj.setDocumentIdentifier(perceptionType.getPerceptionType().getId().getValue());
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de la fecha." + perceptionType.getPerceptionType().getIssueDate().getValue());
            }
            perceptionObj.setIssueDate(formatIssueDate(perceptionType.getPerceptionType().getIssueDate().getValue()));
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            perceptionObj.setSenderSocialReason(perceptionType.getPerceptionType().getAgentParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setSenderRuc(perceptionType.getPerceptionType().getAgentParty().getPartyIdentification().get(0).getID().getValue());
            perceptionObj.setSenderFiscalAddress(perceptionType.getPerceptionType().getAgentParty().getPostalAddress().getStreetName().getValue());
            perceptionObj.setSenderDepProvDist(formatDepProvDist(perceptionType.getPerceptionType().getAgentParty().getPostalAddress()));
            perceptionObj.setSenderLogo("C:\\clientes\\files\\20510910517\\COMPANY_LOGO.jpg");
            perceptionObj.setTelValue(perceptionType.getTransaccion().getTelefono());
            perceptionObj.setWebValue(perceptionType.getTransaccion().getWeb());
            perceptionObj.setSenderMail(perceptionType.getTransaccion().getEMail());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            perceptionObj.setReceiverSocialReason(perceptionType.getPerceptionType().getReceiverParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setReceiverRuc(perceptionType.getPerceptionType().getReceiverParty().getPartyIdentification().get(0).getID().getValue());
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            perceptionObj.setPerceptionItems(getPerceptionItems(perceptionType.getPerceptionType().getSunatPerceptionDocumentReference(), new BigDecimal(perceptionType.getPerceptionType().getSunatPerceptionPercent().getValue())));
            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();
            for (int i = 0; i < perceptionType.getTransaccion().getTransactionComprobantesDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generatePerceptionPDF() [" + this.docUUID + "] Agregando datos al HashMap" + perceptionType.getTransaccion().getTransactionComprobantesDTOList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<String, String>();
                List<String> newlist = new ArrayList<String>();
                /*for (int j = 0; j < perceptionType.getTransaccion().getTransactionComprobantesDTOList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generatePerceptionPDF() [" + this.docUUID + "] Extrayendo Campos " + perceptionType.getTransaccion().getTransactionComprobantesDTOList().get(i));
                    }
                    itemObjectHash.put(perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getUsuariocampos().getNombre(), perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getValor());
                    newlist.add(perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }
                }*/
                for (TransactionComprobantesDTO comprobantesDTO : perceptionType.getTransaccion().getTransactionComprobantesDTOList()) {
                    // Obtener todas las variables del objeto TransactionComprobantesDTO
                    Field[] fields = comprobantesDTO.getClass().getDeclaredFields();

                    // Iterar sobre cada campo
                    for (Field field : fields) {
                        field.setAccessible(true); // Asegurarse de que el campo es accesible

                        try {
                            // Obtener el nombre del campo y su valor
                            String nombreCampo = field.getName();
                            Object valorCampo = field.get(comprobantesDTO);

                            // Añadir el nombre y el valor al HashMap, convirtiendo el valor a String
                            if (valorCampo != null) {
                                itemObjectHash.put(nombreCampo, valorCampo.toString());
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }

            perceptionObj.setItemListDynamic(listaItem);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }
            // Map<String, LegendObject> legendsMap = new HashMap<String,
            // LegendObject>();

            perceptionObj.setTotalAmountValue(perceptionType.getPerceptionType().getTotalInvoiceAmount().getValue().toString());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            perceptionType.getTransaccion();
            for (int i = 0; i < perceptionType.getTransaccion().getTransactionPropertiesDTOList().size(); i++) {
                if (perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getId().equalsIgnoreCase("1000")) {
                    perceptionObj.setLetterAmountValue(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getValor());
                }

            }
            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                perceptionObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                perceptionObj.setValidezPDF("");
            }

            // LegendObject legendLetters =
            // legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            // perceptionObj.setLetterAmountValue(legendLetters.getLegendValue());
            // legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }
            // String barcodeValue =
            // generateBarcodeInfoV2(perceptionType.getPerceptionType().getId().getValue(),
            // "40",
            // perceptionType.getPerceptionType().getIssueDate().toString(),
            // perceptionType.getPerceptionType().getTotalInvoiceAmount().getValue(),
            // perceptionType.getPerceptionType().getAgentParty(),
            // perceptionType.getPerceptionType().getReceiverParty(),
            // perceptionType.getPerceptionType().getUblExtensions());
            // if (logger.isInfoEnabled()) {logger.info("generateInvoicePDF() ["
            // + this.docUUID + "] BARCODE: \n" + barcodeValue);}
            // perceptionObj.setBarcodeValue(barcodeValue);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            // perceptionObj.setLegends(getLegendList(legendsMap));

            perceptionObj.setResolutionCodeValue("resolutionCde");
            perceptionObj.setImporteTexto(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(0).getValor());

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            perceptionBytes = createPerceptionPDF(perceptionObj, docUUID, configData);// PDFPerceptionCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createPerceptionPDF(perceptionObj, docUUID);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return perceptionBytes;
    } // generateInvoicePDF

    public byte[] createPerceptionPDF(PerceptionObject perceptionObj,
                                      String docUUID, ConfigData configData) throws PDFReportException {
        Map<String, Object> parameterMap;
        if (logger.isDebugEnabled()) {
            logger.debug("+createInvoicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == perceptionObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER,
                        perceptionObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE,
                        perceptionObj.getIssueDate());

                if (StringUtils.isNotBlank(perceptionObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL,
                            IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE,
                            perceptionObj.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON,
                        perceptionObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC,
                        perceptionObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS,
                        perceptionObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST,
                        perceptionObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT,
                        perceptionObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL,
                        perceptionObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL,
                        perceptionObj.getTelValue());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1,
                        perceptionObj.getTel2Value());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH,
                        perceptionObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB,
                        perceptionObj.getWebValue());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, perceptionObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON,
                        perceptionObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC,
                        perceptionObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE,
                        perceptionObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE,
                        perceptionObj.getLetterAmountValue());

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE,
                        IPDFCreatorConfig.LEGEND_PERCEPTION_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE,
                        perceptionObj.getResolutionCodeValue());

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "perceptionDocument_Ing.jrxml" : "perceptionDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(perceptionObj.getSenderRuc(), documentName);

                JasperPrint iJasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        parameterMap,
                        new JRBeanCollectionDataSource(perceptionObj
                                .getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") - ERROR: "
                        + e.getMessage());
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") -->"
                        + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createInvoicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    }

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
    }

    protected List<PerceptionItemObject> getPerceptionItems(List<SUNATPerceptionDocumentReferenceType> perceptionLines, BigDecimal porcentaje) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getInvoiceItems() [" + this.docUUID + "] invoiceLines: " + perceptionLines);
        }
        List<PerceptionItemObject> itemList = null;

        if (null != perceptionLines && 0 < perceptionLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<PerceptionItemObject>(
                    perceptionLines.size());

            try {
                for (SUNATPerceptionDocumentReferenceType iLine : perceptionLines) {
                    PerceptionItemObject invoiceItemObj = new PerceptionItemObject();

                    invoiceItemObj.setFechaEmision(formatIssueDate(iLine
                            .getIssueDate().getValue()));
                    invoiceItemObj
                            .setTipoDocumento(iLine.getId().getSchemeID());
                    invoiceItemObj.setNumSerieDoc(iLine.getId().getValue());
                    invoiceItemObj.setMonedaMontoTotal(iLine.getTotalInvoiceAmount().getCurrencyID() + " ");
                    invoiceItemObj.setPrecioVenta(iLine.getTotalInvoiceAmount()
                            .getValue());
                    invoiceItemObj.setMonedaPercepcion("PEN ");
                    invoiceItemObj.setImportePercepcion(iLine
                            .getSunatPerceptionInformation()
                            .getPerceptionAmount().getValue());
                    invoiceItemObj.setMontoTotalCobrado(iLine
                            .getSunatPerceptionInformation()
                            .getSunatNetTotalCashed().getValue());
                    invoiceItemObj.setPorcentajePercepcion(porcentaje);
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
    }

}
