package service.cloud.request.clientRequest.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.TransactionImpuestosDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.object.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.object.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.object.legend.LegendObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATRetentionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PDFBasicGenerateHandler {

    private final Logger logger = Logger
            .getLogger(PDFBasicGenerateHandler.class);

    /* Identificador del documento */
    protected String docUUID;


    /**
     * Constructor de la clase PDFBasicGenerateHandler.
     *
     * @param docUUID UUID identificador de la transaccion.
     */
    public PDFBasicGenerateHandler(String docUUID) {
        this.docUUID = docUUID;
    } // PDFBasicGenerateHandler

    /**
     * Este metodo transforma la fecha de emision a String.
     *
     * @param xmlGregorianCal La fecha de emision de entrada.
     * @return Retorna la fecha de emision formateada.
     * @throws Exception
     */
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
    } // formatIssueDate

    /**
     * Este metodo transforma la fecha de vencimiento al formato del PDF.
     *
     * @param inputDueDate Fecha de vencimiento en un formato no valido.
     * @return Retorna la fecha de vencimiento con formato para el PDF.
     * @throws Exception
     */
    protected String formatDueDate(Date inputDueDate) throws Exception {
        String dueDate = null;
        try {

            SimpleDateFormat sdf = null;

            /*new SimpleDateFormat(
                    IPDFCreatorConfig.PATTER_UBL_DATE);
            Date date = sdf.parse(inputDueDate);
             */
            sdf = new SimpleDateFormat(IPDFCreatorConfig.PATTERN_DATE);
            dueDate = sdf.format(inputDueDate);
        } catch (Exception e) {
            logger.error("formatDueDate() [" + this.docUUID + "] ERROR: "
                    + e.getMessage());
        }
        return dueDate;
    } // formatDueDate

    /**
     * Este metodo obtiene el nombre del tipo de moneda segun el codigo de
     * entrada.
     *
     * @param currencyCode El codigo del tipo de moneda.
     * @return Retorna el nombre del tipo de moneda.
     */
    protected String getCurrencyName(String currencyCode)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getCurrencyName() [" + this.docUUID
                    + "] currencyCode: " + currencyCode);
        }
        String value = null;

        if (currencyCode.equalsIgnoreCase(IPDFCreatorConfig.CURRENCY_CODE_PEN)) {
            value = IPDFCreatorConfig.CURRENCY_CODE_PEN_VALUE;
        } else if (currencyCode
                .equalsIgnoreCase(IPDFCreatorConfig.CURRENCY_CODE_USD)) {
            value = IPDFCreatorConfig.CURRENCY_CODE_USD_VALUE;
        } else if (currencyCode
                .equalsIgnoreCase(IPDFCreatorConfig.CURRENCY_CODE_EUR)) {
            value = IPDFCreatorConfig.CURRENCY_CODE_EUR_VALUE;
        } else {
            logger.error("getCurrencyName() ERROR: "
                    + IVenturaError.ERROR_422.getMessage());
//            throw new PDFReportException(IVenturaError.ERROR_422);
            value = "";
        }
        return value;
    } // getCurrencyName

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

    protected String getCurrencyV2(BigDecimal value, String currencyCode,
                                   String pattern) throws Exception {
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

            String tempDesc = format.format(money);

            java.text.DecimalFormat decF = (java.text.DecimalFormat) format;
            decF.applyPattern(pattern);

            if (0 > money) {
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = tempDesc.substring(0, 3) + " " + decF.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrencyV2

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

    protected String getContactProperty(ContactType contact,
                                        String contactProperty) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getContactProperty() [" + this.docUUID
                    + "] contactProperty: " + contactProperty);
        }
        String value = null;

        if (null != contact) {
            if (contactProperty
                    .equalsIgnoreCase(IPDFCreatorConfig.CONTACT_NAME_VALUE)) {
                value = (null != contact.getName()) ? contact.getName()
                        .getValue() : IPDFCreatorConfig.EMPTY_VALUE;
            } else if (contactProperty
                    .equalsIgnoreCase(IPDFCreatorConfig.CONTACT_MAIL_VALUE)) {
                value = (null != contact.getElectronicMail()) ? contact
                        .getElectronicMail().getValue()
                        : IPDFCreatorConfig.EMPTY_VALUE;
            }
        } else {
            value = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return value;
    } // getContactProperty

    protected String getReceiverFiscalAddress(AddressType postalAddress) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getReceiverFiscalAddress() [" + this.docUUID
                    + "] postalAddress: " + postalAddress);
        }
        String fiscalAddress = null;

        if (null != postalAddress) {
            if (null != postalAddress.getStreetName()
                    && StringUtils.isNotBlank(postalAddress.getStreetName()
                    .getValue())) {
                String address = postalAddress.getStreetName().getValue()
                        .toUpperCase();
                String department = postalAddress.getCountrySubentity()
                        .getValue().toUpperCase();
                String province = postalAddress.getCityName().getValue()
                        .toUpperCase();
                String district = postalAddress.getDistrict().getValue()
                        .toUpperCase();

                fiscalAddress = address + " - " + district + " - " + province
                        + " - " + department;
            } else {
                fiscalAddress = IPDFCreatorConfig.EMPTY_VALUE;
            }
        } else {
            fiscalAddress = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return fiscalAddress;
    } // getFiscalAddress

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

    protected BigDecimal getAdditionalMonetaryTotal(
            List<UBLExtensionType> ublExtension, String addiMonetaryValue)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getAdditionalMonetaryTotal() [" + this.docUUID
                    + "] addiMonetaryValue: " + addiMonetaryValue);
        }
        String idValue = null;
        String payableAmountValue = null;
        BigDecimal responseValue = BigDecimal.ZERO;

        try {
            NodeList parentNodeList = ublExtension.get(0).getExtensionContent()
                    .getAny().getChildNodes();

            for (int i = 0; i < parentNodeList.getLength(); i++) {
                if (StringUtils.isNotBlank(parentNodeList.item(i)
                        .getLocalName())) {
                    if (parentNodeList
                            .item(i)
                            .getLocalName()
                            .equalsIgnoreCase(
                                    IPDFCreatorConfig.UBL_ADDITIONAL_MONETARY_TOTAL)) {
                        NodeList nodeList = parentNodeList.item(i)
                                .getChildNodes();

                        for (int j = 0; j < nodeList.getLength(); j++) {
                            if (StringUtils.isNotBlank(nodeList.item(j)
                                    .getLocalName())) {
                                String nodeValue = nodeList.item(j)
                                        .getTextContent();

                                if (nodeList
                                        .item(j)
                                        .getLocalName()
                                        .equalsIgnoreCase(
                                                IPDFCreatorConfig.UBL_ID)) {
                                    idValue = nodeValue;
                                } else if (nodeList
                                        .item(j)
                                        .getLocalName()
                                        .equalsIgnoreCase(
                                                IPDFCreatorConfig.UBL_PAYABLE_AMOUNT)) {
                                    payableAmountValue = nodeValue;
                                }
                            }
                        }

                        if (idValue.equalsIgnoreCase(addiMonetaryValue)) {
                            responseValue = BigDecimal.valueOf(Double
                                    .valueOf(payableAmountValue));
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("getAdditionalMonetaryTotal() [" + this.docUUID
                    + "] ERROR: " + e.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_419);
        }
        return responseValue;
    } // getAdditionalMonetaryTotal

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

    protected BigDecimal getTaxTotalValue(List<TaxTotalType> taxTotalList, String taxTotalCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTaxTotalValue() [" + this.docUUID
                    + "] taxTotalCode: " + taxTotalCode);
        }
        BigDecimal taxValue = BigDecimal.ZERO;

        for (int i = 0; i < taxTotalList.size(); i++) {
            if (taxTotalList.get(i).getTaxSubtotal().get(0).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase(taxTotalCode)) {
                taxValue = taxTotalList.get(i).getTaxAmount().getValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("+-getTaxTotalValue() [" + this.docUUID
                            + "] taxValue: " + taxValue);
                }
            }
        }
        return taxValue;
    } // getTaxTotalValue

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

    protected Map<String, LegendObject> getAdditionalProperties(
            List<UBLExtensionType> ublExtension) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAdditionalProperties() [" + this.docUUID + "]");
        }
        Map<String, LegendObject> legendsMap = null;
        String id = null;
        String value = null;

        try {
            legendsMap = new HashMap<String, LegendObject>();

            NodeList parentNodeList = ublExtension.get(0).getExtensionContent()
                    .getAny().getChildNodes();

            for (int i = 0; i < parentNodeList.getLength(); i++) {
                if (StringUtils.isNotBlank(parentNodeList.item(i)
                        .getLocalName())
                        && parentNodeList
                        .item(i)
                        .getLocalName()
                        .equalsIgnoreCase(
                                IPDFCreatorConfig.UBL_ADDITIONAL_PROPERTY)) {
                    NodeList nodeList = parentNodeList.item(i).getChildNodes();

                    for (int j = 0; j < nodeList.getLength(); j++) {
                        if (StringUtils.isNotBlank(nodeList.item(j)
                                .getLocalName())) {
                            String nodeValue = nodeList.item(j).getTextContent();

                            if (nodeList.item(j).getLocalName().equalsIgnoreCase(IPDFCreatorConfig.UBL_ID)) {
                                id = nodeValue;
                            } else if (nodeList
                                    .item(j)
                                    .getLocalName()
                                    .equalsIgnoreCase(
                                            IPDFCreatorConfig.UBL_VALUE)) {
                                value = nodeValue;
                            }
                        }
                    }

                    LegendObject legendObj = new LegendObject();
                    legendObj.setLegendValue(value);

                    legendsMap.put(id, legendObj);
                }
            }
        } catch (Exception e) {
            logger.error("getAdditionalProperties() [" + this.docUUID
                    + "] ERROR: " + IVenturaError.ERROR_420.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_420);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAdditionalProperties() [" + this.docUUID + "]");
        }
        return legendsMap;
    } // getAdditionalProperties

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

    protected String getSunatTransactionInfo(
            List<UBLExtensionType> ublExtensionList) throws PDFReportException {
        String sunatTransactionInfo = null;
        try {
            String sunatTransCode = null;
            NodeList nodeList = ublExtensionList.get(0).getExtensionContent()
                    .getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG)) {
                    sunatTransCode = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isNotBlank(sunatTransCode)) {
                sunatTransCode = sunatTransCode.replace("\n", "").trim();

                switch (sunatTransCode) {
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_DSC;
                        break;
                    default:
                        throw new PDFReportException(IVenturaError.ERROR_421);
                }
            }
        } catch (PDFReportException e) {
            logger.error("getSunatTransactionInfo() [" + this.docUUID
                    + "] ERROR: " + e.getError().getId() + "-"
                    + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getSunatTransactionInfo() [" + this.docUUID
                    + "] Exception -->" + e.getMessage());
        }
        return sunatTransactionInfo;
    } // getSunatTransactionInfo

    protected String getReceiverIdentifierType(String identifierType)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getReceiverIdentifierType() [" + this.docUUID + "]");
        }
        String result = null;

        if (StringUtils.isNotBlank(identifierType)) {
            switch (identifierType) {
                case IUBLConfig.ID_DOC_WITHOUT_RUC:
                    result = IPDFCreatorConfig.ID_DOC_WITHOUT_RUC_DSC;
                    break;
                case IUBLConfig.ID_DOC_DNI:
                    result = IPDFCreatorConfig.ID_DOC_DNI_DSC;
                    break;
                case IUBLConfig.ID_DOC_FOREIGN_CARD:
                    result = IPDFCreatorConfig.ID_DOC_FOREIGN_CARD_DSC;
                    break;
                case IUBLConfig.ID_DOC_RUC:
                    result = IPDFCreatorConfig.ID_DOC_RUC_DSC;
                    break;
                case IUBLConfig.ID_DOC_PASSPORT:
                    result = IPDFCreatorConfig.ID_DOC_PASSPORT_DSC;
                    break;
                case IUBLConfig.ID_DOC_DIPLOMATIC_ID:
                    result = IPDFCreatorConfig.ID_DOC_DIPLOMATIC_ID_DSC;
                    break;
                default:
                    throw new PDFReportException(IVenturaError.ERROR_425);
            }
        } else {
            result = IPDFCreatorConfig.ID_DOC_WITHOUT_RUC_DSC;
        }
        return result;
    } // getReceiverIdentifierType

    protected String getReceiverFiscalAddress2(LocationType physicalLocation)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getReceiverFiscalAddress2() [" + this.docUUID + "]");
        }
        String fiscalAddress = null;

        if (null != physicalLocation) {
            fiscalAddress = physicalLocation.getDescription().getValue();
        } else {
            fiscalAddress = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return fiscalAddress;
    } // getReceiverFiscalAddress2

    protected String getTypeOfCreditNoteValue(String cnTypeCode)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTypeOfCreditNoteValue() cnTypeCode: "
                    + cnTypeCode);
        }
        String response = null;

        if (StringUtils.isNotBlank(cnTypeCode)) {
            switch (cnTypeCode) {
                case IUBLConfig.CREDIT_NOTE_TYPE_01:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_01_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_02:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_02_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_03:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_03_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_04:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_04_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_05:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_05_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_06:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_06_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_07:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_07_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_08:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_08_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_09:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_09_DSC;
                    break;
                case IUBLConfig.CREDIT_NOTE_TYPE_10:
                    response = IPDFCreatorConfig.CREDIT_NOTE_TYPE_10_DSC;
                    break;
                default:
                    throw new PDFReportException(IVenturaError.ERROR_426);
            }
        } else {
            logger.error("getTypeOfCreditNoteValue() ERROR: "
                    + IVenturaError.ERROR_427.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_427);
        }
        return response;
    } // getTypeOfCreditNoteValue

    protected String getTypeOfDebitNoteValue(String dnTypeCode)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTypeOfDebitNoteValue() dnTypeCode: "
                    + dnTypeCode);
        }
        String response = null;

        if (StringUtils.isNotBlank(dnTypeCode)) {
            switch (dnTypeCode) {
                case IUBLConfig.DEBIT_NOTE_TYPE_01:
                    response = IPDFCreatorConfig.DEBIT_NOTE_TYPE_01_DSC;
                    break;
                case IUBLConfig.DEBIT_NOTE_TYPE_02:
                    response = IPDFCreatorConfig.DEBIT_NOTE_TYPE_02_DSC;
                    break;
                case IUBLConfig.DEBIT_NOTE_TYPE_03:
                    response = IPDFCreatorConfig.DEBIT_NOTE_TYPE_03_DSC;
                    break;
                default:
                    throw new PDFReportException(IVenturaError.ERROR_432);
            }
        } else {
            logger.error("getTypeOfDebitNoteValue() ERROR: "
                    + IVenturaError.ERROR_433.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_433);
        }
        return response;
    } // getTypeOfDebitNoteValue

    protected String getDocumentReferenceValue(
            BillingReferenceType billingReference) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getDocumentReferenceValue() [" + this.docUUID + "]");
        }
        String response = null;

        if (null != billingReference) {
            String type = billingReference.getInvoiceDocumentReference()
                    .getDocumentTypeCode().getValue();
            String identifier = billingReference.getInvoiceDocumentReference()
                    .getID().getValue();

            switch (type) {
                case IUBLConfig.DOC_INVOICE_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_INVOICE_DESC + ")";
                    break;
                case IUBLConfig.DOC_BOLETA_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_BOLETA_DESC + ")";
                    break;
                case IUBLConfig.DOC_MACHINE_TICKET_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_MACHINE_TICKET_DESC + ")";
                    break;
                case IUBLConfig.DOC_FINANCIAL_BANKS_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_FINANCIAL_BANKS_DESC + ")";
                    break;
                case IUBLConfig.DOC_BANK_INSURANCE_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_BANK_INSURANCE_DESC + ")";
                    break;
                case IUBLConfig.DOC_ISSUED_BY_AFP_CODE:
                    response = identifier + " ("
                            + IPDFCreatorConfig.DOC_ISSUED_BY_AFP_DESC + ")";
                    break;
                default:
                    throw new PDFReportException(IVenturaError.ERROR_430);
            }
        } else {
            logger.error("getDocumentReferenceValue() [" + this.docUUID
                    + "] ERROR: " + IVenturaError.ERROR_428.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_428);
        }
        return response;
    } // getDocumentReferenceValue

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
    } // getRemissionGuides

    protected String getContractDocumentReference(
            List<DocumentReferenceType> contractDocumentReferences, String code) {
        if (logger.isInfoEnabled()) {
            logger.info("generateCreditNotePDF() [Lista contractDocumentReferences ]"
                    + contractDocumentReferences.size());
        }
        String contractDocRefResponse = null;

        if (null != contractDocumentReferences
                && 0 < contractDocumentReferences.size()) {
            for (DocumentReferenceType contDocumentRef : contractDocumentReferences) {
                if (contDocumentRef.getDocumentTypeCode().getValue()
                        .equalsIgnoreCase(code)) {
                    if (logger.isInfoEnabled()) {
                        logger.info("generateCreditNotePDF() [Lista contractDocumentReferences ]"
                                + contDocumentRef.getID().getValue());
                    }
                    contractDocRefResponse = contDocumentRef.getID().getValue();
                }
            }
        }

        if (StringUtils.isBlank(contractDocRefResponse)) {
            contractDocRefResponse = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return contractDocRefResponse;
    } // getContractDocumentReference

    public String generateGuiaBarcodeInfoV2(String identifier, String documentType, String issueDate, BigDecimal payableAmountVal, BigDecimal taxTotalList, SupplierPartyType accSupplierParty, CustomerPartyType accCustomerParty, UBLExtensionsType ublExtensions) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateBarcodeInfo()");
        }
        String barcodeValue = null;
        try {
            /* a.) Numero de RUC del emisor electronico */
            String senderRuc = accSupplierParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* b.) Tipo de comprobante de pago electronico */
            /* Parametro de entrada del metodo */
            /* c.) Numeracion conformada por serie y numero correlativo */
            String serie = identifier.substring(0, 4);
            String correlative = Integer.valueOf(identifier.substring(5)).toString();
            /* d.) Sumatoria IGV, de ser el caso */
            String igvTax = null;
            BigDecimal igvTaxBigDecimal = BigDecimal.ZERO;
            igvTax = igvTaxBigDecimal.setScale(2, RoundingMode.HALF_UP).toString();
            /* e.) Importe total de la venta, cesion en uso o servicio prestado */
            String payableAmount = payableAmountVal.toString();
            /* f.) Fecha de emision */
            /* Parametro de entrada del metodo */
            /* g,) Tipo de documento del adquiriente o usuario */
            String receiverDocType = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getSchemeID();
            /* h.) Numero de documento del adquiriente */
            String receiverDocNumber = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* i.) Valor resumen <ds:DigestValue> */
            String digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */
            String signatureValue = getSignatureValue(ublExtensions);
            /*
             * Armando el codigo de barras
             */
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, senderRuc, documentType, serie, correlative, igvTax, payableAmount, issueDate, receiverDocType, receiverDocNumber, digestValue, signatureValue);
        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        return barcodeValue;
    }
} // PDFBasicGenerateHandler
