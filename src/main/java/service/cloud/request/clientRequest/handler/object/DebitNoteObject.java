package service.cloud.request.clientRequest.handler.object;

import service.cloud.request.clientRequest.handler.object.legend.LegendObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Esta clase contiene la informacion de la nota de debito que se colocara en el
 * template del PDF de tipo NOTA DE DEBITO.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class DebitNoteObject {
    /* Informacion general */

    private String documentIdentifier;

    private String issueDate;

    private String dueDate;

    private String currencyValue;

    private String sunatTransaction;

    private String paymentCondition;

    private String sellOrder;

    private String sellerName;

    private String remissionGuides;

    private String telefono;

    private String Telefono_1;

    private String Web;

    private String porcentajeIGV;

    private String Comentarios;

    private String perceptionAmount;

    private String perceptionPercentage;

    private String ISCPercetange;

    private InputStream codeQR;

    private String digestValue;

    /* Informacion de la nota de debito */
    private String typeOfDebitNote;

    private String descOfDebitNote;

    private String documentReferenceToCn;

    private String dateDocumentReference;

    /* Informacion del emisor */
    private String senderSocialReason;

    private String senderRuc;

    private String senderFiscalAddress;

    private String senderDepProvDist;

    private String senderContact;

    private String senderMail;

    private String senderLogo;

    /* Informacion del receptor */
    private String receiverRegistrationName;

    private String receiverIdentifier;

    private String receiverIdentifierType;

    private String receiverFiscalAddress;

    /* Lista de items */
    //private List<DebitNoteItemObject> debitNoteItems;

    private List<WrapperItemObject> itemsListDynamic;

    /* Montos */
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

    private Map<String, String> invoicePersonalizacion;

    private String validezPDF;

    /**
     * Constructor basico de la clase DebitNoteObject.
     */
    public DebitNoteObject() {
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

    public String getTypeOfDebitNote() {
        return typeOfDebitNote;
    } //getTypeOfDebitNote

    public void setTypeOfDebitNote(String typeOfDebitNote) {
        this.typeOfDebitNote = typeOfDebitNote;
    } //setTypeOfDebitNote

    public String getDescOfDebitNote() {
        return descOfDebitNote;
    } //getDescOfDebitNote

    public void setDescOfDebitNote(String descOfDebitNote) {
        this.descOfDebitNote = descOfDebitNote;
    } //setDescOfDebitNote

    public String getDocumentReferenceToCn() {
        return documentReferenceToCn;
    } //getDocumentReferenceToCn

    public void setDocumentReferenceToCn(String documentReferenceToCn) {
        this.documentReferenceToCn = documentReferenceToCn;
    } //setDocumentReferenceToCn

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

    public String getReceiverRegistrationName() {
        return receiverRegistrationName;
    } //getReceiverRegistrationName

    public void setReceiverRegistrationName(String receiverRegistrationName) {
        this.receiverRegistrationName = receiverRegistrationName;
    } //setReceiverRegistrationName

    public String getReceiverIdentifier() {
        return receiverIdentifier;
    } //getReceiverIdentifier

    public void setReceiverIdentifier(String receiverIdentifier) {
        this.receiverIdentifier = receiverIdentifier;
    } //setReceiverIdentifier

    public String getReceiverIdentifierType() {
        return receiverIdentifierType;
    } //getReceiverIdentifierType

    public void setReceiverIdentifierType(String receiverIdentifierType) {
        this.receiverIdentifierType = receiverIdentifierType;
    } //setReceiverIdentifierType

    public String getReceiverFiscalAddress() {
        return receiverFiscalAddress;
    } //getReceiverFiscalAddress

    public void setReceiverFiscalAddress(String receiverFiscalAddress) {
        this.receiverFiscalAddress = receiverFiscalAddress;
    } //setReceiverFiscalAddress

    //public List<DebitNoteItemObject> getDebitNoteItems() {
        //return debitNoteItems;
    //} //getDebitNoteItems

    //public void setDebitNoteItems(List<DebitNoteItemObject> debitNoteItems) {
       // this.debitNoteItems = debitNoteItems;
   // } //setDebitNoteItems

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

    public String getPaymentCondition() {
        return paymentCondition;
    }

    public void setPaymentCondition(String paymentCondition) {
        this.paymentCondition = paymentCondition;
    }

    public String getSellOrder() {
        return sellOrder;
    }

    public void setSellOrder(String sellOrder) {
        this.sellOrder = sellOrder;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getRemissionGuides() {
        return remissionGuides;
    }

    public void setRemissionGuides(String remissionGuides) {
        this.remissionGuides = remissionGuides;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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

    public String getPorcentajeIGV() {
        return porcentajeIGV;
    }

    public void setPorcentajeIGV(String porcentajeIGV) {
        this.porcentajeIGV = porcentajeIGV;
    }

    public Map<String, String> getInvoicePersonalizacion() {
        return invoicePersonalizacion;
    }

    public void setInvoicePersonalizacion(Map<String, String> invoicePersonalizacion) {
        this.invoicePersonalizacion = invoicePersonalizacion;
    }

    public List<WrapperItemObject> getItemsListDynamic() {
        return itemsListDynamic;
    }

    public void setItemsListDynamic(List<WrapperItemObject> itemsListDynamic) {
        this.itemsListDynamic = itemsListDynamic;
    }

    public String getDateDocumentReference() {
        return dateDocumentReference;
    }

    public void setDateDocumentReference(String dateDocumentReference) {
        this.dateDocumentReference = dateDocumentReference;
    }

    public String getComentarios() {
        return Comentarios;
    }

    public void setComentarios(String comentarios) {
        Comentarios = comentarios;
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

    public String getISCPercetange() {
        return ISCPercetange;
    }

    public void setISCPercetange(String ISCPercetange) {
        this.ISCPercetange = ISCPercetange;
    }

} //DebitNoteObject
