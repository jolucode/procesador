package service.cloud.request.clientRequest.handler.object;

import service.cloud.request.clientRequest.handler.object.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.object.legend.LegendObject;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Esta clase contiene la informacion de la factura que se colocara en el
 * template del PDF de tipo FACTURA.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class InvoiceObject {
    /* Informacion general */

    private String documentIdentifier;

    private String issueDate;

    private String dueDate;

    private String formSap;

    private String currencyValue;

    private String sunatTransaction;

    private String paymentCondition;

    private String sellOrder;

    private String sellerName;

    private String remissionGuides;

    private String datos_extra_fac;

    private String porcentajeIGV;

    private String nuevoCalculo;

    private InputStream codeQR;


    /* Informacion del emisor */
    private String senderSocialReason;

    private String senderRuc;

    private String senderFiscalAddress;

    private String senderDepProvDist;

    private String senderContact;

    private String senderMail;

    private String senderLogo;

    private String Telefono;

    private String Telefono_1;

    private String Web;

    private String Comentarios;

    private String Anticipos;

    private String digestValue;

    /* Informacion del receptor */
    private String receiverSocialReason;

    private String receiverRuc;

    private String receiverFiscalAddress;

    /* Lista de items */
    private List<InvoiceItemObject> invoiceItems;

    private List<WrapperItemObject> itemListDynamic;

    private List<WrapperItemObject> itemListDynamicC; // Numa

    /* Monto de Percepcion */
    private String perceptionAmount;

    private String perceptionPercentage;

    /* Monto de Retencion */
    private String retentionPercentage;

    /* Lista de Personalizacion */
    Map<String, String> invoicePersonalizacion;


    /* Montos */
    private String prepaidAmountValue;

    private String subtotalValue;

    private String igvValue;

    private String iscValue;

    private String amountValue;

    private String discountValue;

    private String totalAmountValue;

    private String gravadaAmountValue;

    private String exoneradaAmountValue;

    private String inafectaAmountValue;

    private String gratuitaAmountValue;

    /* Codigo de barras */
    private InputStream barcodeValue;

    /* Importe en letras */
    private String letterAmountValue;

    /* Lista de legendas */
    private List<LegendObject> legends;

    /* Codigo de resolucion de autorizacion por Sunat */
    private String resolutionCodeValue;

    private String validezPDF;

    private String impuestoBolsa = "0";

    private String impuestoBolsaMoneda = "PEN";

    private String C1;
    private String C2;
    private String C3;

    private String F1;
    private String F2;
    private String F3;

    private BigDecimal M1;
    private BigDecimal M2;
    private BigDecimal M3;

    private int totalCuotas;
    private BigDecimal montoPendiente;
    private String metodoPago;

    private BigDecimal baseImponibleRetencion;
    private BigDecimal porcentajeRetencion;
    private BigDecimal montoRetencion;


    /**
     * Constructor basico de la clase InvoiceObject.
     */
    public InvoiceObject() {
    }

    public String getFormSap() {
        return formSap;
    }

    public void setFormSap(String formSap) {
        this.formSap = formSap;
    }

    public String getDocumentIdentifier() {
        return documentIdentifier;
    } //getDocumentIdentifier

    public void setDocumentIdentifier(String documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    } //setDocumentIdentifier

    public String getIssueDate() {
        return issueDate;
    } //getIssueDate

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    } //setIssueDate

    public String getDueDate() {
        return dueDate;
    } //getDueDate

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    } //setDueDate

    public String getCurrencyValue() {
        return currencyValue;
    } //getCurrencyValue

    public void setCurrencyValue(String currencyValue) {
        this.currencyValue = currencyValue;
    } //setCurrencyValue

    public String getSunatTransaction() {
        return sunatTransaction;
    } //getSunatTransaction

    public void setSunatTransaction(String sunatTransaction) {
        this.sunatTransaction = sunatTransaction;
    } //setSunatTransaction

    public String getPaymentCondition() {
        return paymentCondition;
    } //getPaymentCondition

    public void setPaymentCondition(String paymentCondition) {
        this.paymentCondition = paymentCondition;
    } //setPaymentCondition

    public String getSellOrder() {
        return sellOrder;
    } //getSellOrder

    public void setSellOrder(String sellOrder) {
        this.sellOrder = sellOrder;
    } //setSellOrder

    public String getSellerName() {
        return sellerName;
    } //getSellerName

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    } //setSellerName

    public String getRemissionGuides() {
        return remissionGuides;
    } //getRemissionGuides

    public void setRemissionGuides(String remissionGuides) {
        this.remissionGuides = remissionGuides;
    } //setRemissionGuides

    public InputStream getBarcodeValue() {
        return barcodeValue;
    } //getBarcodeValue

    public void setBarcodeValue(InputStream barcodeValue) {
        this.barcodeValue = barcodeValue;
    } //setBarcodeValue

    public String getSenderSocialReason() {
        return senderSocialReason;
    } //getSenderSocialReason

    public void setSenderSocialReason(String senderSocialReason) {
        this.senderSocialReason = senderSocialReason;
    } //setSenderSocialReason

    public String getSenderRuc() {
        return senderRuc;
    } //getSenderRuc

    public void setSenderRuc(String senderRuc) {
        this.senderRuc = senderRuc;
    } //setSenderRuc

    public String getSenderFiscalAddress() {
        return senderFiscalAddress;
    } //getSenderFiscalAddress

    public void setSenderFiscalAddress(String senderFiscalAddress) {
        this.senderFiscalAddress = senderFiscalAddress;
    } //setSenderFiscalAddress

    public String getSenderDepProvDist() {
        return senderDepProvDist;
    } //getSenderDepProvDist

    public void setSenderDepProvDist(String senderDepProvDist) {
        this.senderDepProvDist = senderDepProvDist;
    } //setSenderDepProvDist

    public String getSenderContact() {
        return senderContact;
    } //getSenderContact

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    } //setSenderContact

    public String getSenderMail() {
        return senderMail;
    } //getSenderMail

    public void setSenderMail(String senderMail) {
        this.senderMail = senderMail;
    } //setSenderMail

    public String getSenderLogo() {
        return senderLogo;
    } //getSenderLogo

    public void setSenderLogo(String senderLogo) {
        this.senderLogo = senderLogo;
    } //setSenderLogo

    public String getReceiverSocialReason() {
        return receiverSocialReason;
    } //getReceiverSocialReason

    public void setReceiverSocialReason(String receiverSocialReason) {
        this.receiverSocialReason = receiverSocialReason;
    } //setReceiverSocialReason

    public String getReceiverRuc() {
        return receiverRuc;
    } //getReceiverRuc

    public void setReceiverRuc(String receiverRuc) {
        this.receiverRuc = receiverRuc;
    } //setReceiverRuc

    public String getReceiverFiscalAddress() {
        return receiverFiscalAddress;
    } //getReceiverFiscalAddress

    public void setReceiverFiscalAddress(String receiverFiscalAddress) {
        this.receiverFiscalAddress = receiverFiscalAddress;
    } //setReceiverFiscalAddress

    public List<InvoiceItemObject> getInvoiceItems() {
        return invoiceItems;
    } //getInvoiceItems

    public void setInvoiceItems(List<InvoiceItemObject> invoiceItems) {
        this.invoiceItems = invoiceItems;
    } //setInvoiceItems

    public String getSubtotalValue() {
        return subtotalValue;
    } //getSubtotalValue

    public void setSubtotalValue(String subtotalValue) {
        this.subtotalValue = subtotalValue;
    } //setSubtotalValue

    public String getIgvValue() {
        return igvValue;
    } //getIgvValue

    public void setIgvValue(String igvValue) {
        this.igvValue = igvValue;
    } //setIgvValue

    public String getIscValue() {
        return iscValue;
    } //getIscValue

    public void setIscValue(String iscValue) {
        this.iscValue = iscValue;
    } //setIscValue

    public String getAmountValue() {
        return amountValue;
    } //getAmountValue

    public void setAmountValue(String amountValue) {
        this.amountValue = amountValue;
    } //setAmountValue

    public String getDiscountValue() {
        return discountValue;
    } //getDiscountValue

    public void setDiscountValue(String discountValue) {
        this.discountValue = discountValue;
    } //setDiscountValue

    public String getTotalAmountValue() {
        return totalAmountValue;
    } //getTotalAmountValue

    public void setTotalAmountValue(String totalAmountValue) {
        this.totalAmountValue = totalAmountValue;
    } //setTotalAmountValue

    public String getGravadaAmountValue() {
        return gravadaAmountValue;
    } //getGravadaAmountValue

    public void setGravadaAmountValue(String gravadaAmountValue) {
        this.gravadaAmountValue = gravadaAmountValue;
    } //setGravadaAmountValue

    public String getExoneradaAmountValue() {
        return exoneradaAmountValue;
    } //getExoneradaAmountValue

    public void setExoneradaAmountValue(String exoneradaAmountValue) {
        this.exoneradaAmountValue = exoneradaAmountValue;
    } //setExoneradaAmountValue

    public String getInafectaAmountValue() {
        return inafectaAmountValue;
    } //getInafectaAmountValue

    public void setInafectaAmountValue(String inafectaAmountValue) {
        this.inafectaAmountValue = inafectaAmountValue;
    } //setInafectaAmountValue

    public String getGratuitaAmountValue() {
        return gratuitaAmountValue;
    } //getGratuitaAmountValue

    public void setGratuitaAmountValue(String gratuitaAmountValue) {
        this.gratuitaAmountValue = gratuitaAmountValue;
    } //setGratuitaAmountValue

    public String getLetterAmountValue() {
        return letterAmountValue;
    } //getLetterAmountValue

    public void setLetterAmountValue(String letterAmountValue) {
        this.letterAmountValue = letterAmountValue;
    } //setLetterAmountValue

    public List<LegendObject> getLegends() {
        return legends;
    } //getLegends

    public void setLegends(List<LegendObject> legends) {
        this.legends = legends;
    } //setLegends

    public String getResolutionCodeValue() {
        return resolutionCodeValue;
    } //getResolutionCodeValue

    public void setResolutionCodeValue(String resolutionCodeValue) {
        this.resolutionCodeValue = resolutionCodeValue;
    } //setResolutionCodeValue

    public String getPrepaidAmountValue() {
        return prepaidAmountValue;
    }

    public void setPrepaidAmountValue(String prepaidAmountValue) {
        this.prepaidAmountValue = prepaidAmountValue;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        Telefono = telefono;
    }

    public String getTelefono_1() {
        return Telefono_1;
    }

    public void setTelefono_1(String telefono_1) {
        Telefono_1 = telefono_1;
    }

    public String getWeb() {
        return Web;
    }

    public void setWeb(String web) {
        Web = web;
    }

    public String getDatos_extra_fac() {
        return datos_extra_fac;
    }

    public void setDatos_extra_fac(String datos_extra_fac) {
        this.datos_extra_fac = datos_extra_fac;
    }

    public String getPorcentajeIGV() {
        return porcentajeIGV;
    }

    public void setPorcentajeIGV(String porcentajeIGV) {
        this.porcentajeIGV = porcentajeIGV;
    }

    public String getNuevoCalculo() {
        return nuevoCalculo;
    }

    public void setNuevoCalculo(String nuevoCalculo) {
        this.nuevoCalculo = nuevoCalculo;
    }

    public Map<String, String> getInvoicePersonalizacion() {
        return invoicePersonalizacion;
    }

    public void setInvoicePersonalizacion(Map<String, String> invoicePersonalizacion) {
        this.invoicePersonalizacion = invoicePersonalizacion;
    }

    public String getPerceptionAmount() {
        return perceptionAmount;
    }

    public void setPerceptionAmount(String perceptionAmount) {
        this.perceptionAmount = perceptionAmount;
    }

    public String getPerceptionPercentage() {
        return perceptionPercentage;
    }

    public void setPerceptionPercentage(String perceptionPercentage) {
        this.perceptionPercentage = perceptionPercentage;
    }

    public List<WrapperItemObject> getItemListDynamic() {
        return itemListDynamic;
    }

    public void setItemListDynamic(List<WrapperItemObject> itemListDynamic) {
        this.itemListDynamic = itemListDynamic;
    }

    public List<WrapperItemObject> getItemListDynamicC() {
        return itemListDynamicC;
    }

    public void setItemListDynamicC(List<WrapperItemObject> itemListDynamicC) {
        this.itemListDynamicC = itemListDynamicC;
    }

    public String getComentarios() {
        return Comentarios;
    }

    public void setComentarios(String comentarios) {
        Comentarios = comentarios;
    }

    public String getAnticipos() {
        return Anticipos;
    }

    public void setAnticipos(String anticipos) {
        Anticipos = anticipos;
    }

    public String getRetentionPercentage() {
        return retentionPercentage;
    }

    public void setRetentionPercentage(String retentionPercentage) {
        this.retentionPercentage = retentionPercentage;
    }

    public String getValidezPDF() {
        return validezPDF;
    }

    public void setValidezPDF(String validezPDF) {
        this.validezPDF = validezPDF;
    }

    public String getDigestValue() {
        return digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

    public InputStream getCodeQR() {
        return codeQR;
    }

    public void setCodeQR(InputStream codeQR) {
        this.codeQR = codeQR;
    }

    public String getImpuestoBolsa() {
        return impuestoBolsa;
    }

    public void setImpuestoBolsa(String impuestoBolsa) {
        this.impuestoBolsa = impuestoBolsa;
    }

    public String getImpuestoBolsaMoneda() {
        return impuestoBolsaMoneda;
    }

    public void setImpuestoBolsaMoneda(String impuestoBolsaMoneda) {
        this.impuestoBolsaMoneda = impuestoBolsaMoneda;
    }

    public String getC1() {
        return C1;
    }

    public void setC1(String c1) {
        C1 = c1;
    }

    public String getC2() {
        return C2;
    }

    public void setC2(String c2) {
        C2 = c2;
    }

    public String getC3() {
        return C3;
    }

    public void setC3(String c3) {
        C3 = c3;
    }

    public String getF1() {
        return F1;
    }

    public void setF1(String f1) {
        F1 = f1;
    }

    public String getF2() {
        return F2;
    }

    public void setF2(String f2) {
        F2 = f2;
    }

    public String getF3() {
        return F3;
    }

    public void setF3(String f3) {
        F3 = f3;
    }

    public BigDecimal getM1() {
        return M1;
    }

    public void setM1(BigDecimal m1) {
        M1 = m1;
    }

    public BigDecimal getM2() {
        return M2;
    }

    public void setM2(BigDecimal m2) {
        M2 = m2;
    }

    public BigDecimal getM3() {
        return M3;
    }

    public void setM3(BigDecimal m3) {
        M3 = m3;
    }

    public int getTotalCuotas() {
        return totalCuotas;
    }

    public void setTotalCuotas(int totalCuotas) {
        this.totalCuotas = totalCuotas;
    }

    public BigDecimal getMontoPendiente() {
        return montoPendiente;
    }

    public void setMontoPendiente(BigDecimal montoPendiente) {
        this.montoPendiente = montoPendiente;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getBaseImponibleRetencion() {
        return baseImponibleRetencion;
    }

    public void setBaseImponibleRetencion(BigDecimal baseImponibleRetencion) {
        this.baseImponibleRetencion = baseImponibleRetencion;
    }

    public BigDecimal getPorcentajeRetencion() {
        return porcentajeRetencion;
    }

    public void setPorcentajeRetencion(BigDecimal porcentajeRetencion) {
        this.porcentajeRetencion = porcentajeRetencion;
    }

    public BigDecimal getMontoRetencion() {
        return montoRetencion;
    }

    public void setMontoRetencion(BigDecimal montoRetencion) {
        this.montoRetencion = montoRetencion;
    }
} //InvoiceObject
