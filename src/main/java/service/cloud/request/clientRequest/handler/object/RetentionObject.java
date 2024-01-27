package service.cloud.request.clientRequest.handler.object;

import service.cloud.request.clientRequest.handler.object.item.RetentionItemObject;
import service.cloud.request.clientRequest.handler.object.legend.LegendObject;

import java.io.InputStream;
import java.util.List;

public class RetentionObject {

    /* Informacion general */
    private String documentIdentifier;

    private String issueDate;

    private String currencyValue;

    private String sunatTransaction;

    private String total_doc_value;

    /* Informacion del emisor */
    private String senderSocialReason;

    private String senderRuc;

    private String senderFiscalAddress;

    private String senderDepProvDist;

    private String senderContact;

    private String senderMail;

    private String senderLogo;

    private String tel;

    private String tel1;

    private String web;

    private String comentarios;

    private String MontoenSoles;

    private String montoTotalDoc;

    private String validezPDF;

    private String regimenRET;

    /* Informacion del receptor */
    private String receiverSocialReason;

    private String receiverRuc;

    /* Lista de items */
    private List<RetentionItemObject> retentionItems;

    private List<WrapperItemObject> itemListDynamic;

    /* Montos */
    private String totalAmountValue;

    /* Codigo de barras */
    private InputStream barcodeValue;

    private String digestValue;

    private InputStream codeQR;

    /* Importe en letras */
    private String letterAmountValue;

    /* Lista de legendas */
    private List<LegendObject> legends;

    /* Codigo de resolucion de autorizacion por Sunat */
    private String resolutionCodeValue;

    public RetentionObject() {
    }

    public String getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(String documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getCurrencyValue() {
        return currencyValue;
    }

    public void setCurrencyValue(String currencyValue) {
        this.currencyValue = currencyValue;
    }

    public String getSunatTransaction() {
        return sunatTransaction;
    }

    public void setSunatTransaction(String sunatTransaction) {
        this.sunatTransaction = sunatTransaction;
    }

    public String getSenderSocialReason() {
        return senderSocialReason;
    }

    public void setSenderSocialReason(String senderSocialReason) {
        this.senderSocialReason = senderSocialReason;
    }

    public String getSenderRuc() {
        return senderRuc;
    }

    public void setSenderRuc(String senderRuc) {
        this.senderRuc = senderRuc;
    }

    public String getSenderFiscalAddress() {
        return senderFiscalAddress;
    }

    public void setSenderFiscalAddress(String senderFiscalAddress) {
        this.senderFiscalAddress = senderFiscalAddress;
    }

    public String getSenderDepProvDist() {
        return senderDepProvDist;
    }

    public void setSenderDepProvDist(String senderDepProvDist) {
        this.senderDepProvDist = senderDepProvDist;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getSenderMail() {
        return senderMail;
    }

    public void setSenderMail(String senderMail) {
        this.senderMail = senderMail;
    }

    public String getSenderLogo() {
        return senderLogo;
    }

    public void setSenderLogo(String senderLogo) {
        this.senderLogo = senderLogo;
    }

    public String getReceiverSocialReason() {
        return receiverSocialReason;
    }

    public void setReceiverSocialReason(String receiverSocialReason) {
        this.receiverSocialReason = receiverSocialReason;
    }

    public String getReceiverRuc() {
        return receiverRuc;
    }

    public void setReceiverRuc(String receiverRuc) {
        this.receiverRuc = receiverRuc;
    }

    public List<RetentionItemObject> getRetentionItems() {
        return retentionItems;
    }

    public void setRetentionItems(List<RetentionItemObject> retentionItems) {
        this.retentionItems = retentionItems;
    }

    public String getTotalAmountValue() {
        return totalAmountValue;
    }

    public void setTotalAmountValue(String totalAmountValue) {
        this.totalAmountValue = totalAmountValue;
    }

    public InputStream getBarcodeValue() {
        return barcodeValue;
    }

    public void setBarcodeValue(InputStream barcodeValue) {
        this.barcodeValue = barcodeValue;
    }

    public String getLetterAmountValue() {
        return letterAmountValue;
    }

    public void setLetterAmountValue(String letterAmountValue) {
        this.letterAmountValue = letterAmountValue;
    }

    public List<LegendObject> getLegends() {
        return legends;
    }

    public void setLegends(List<LegendObject> legends) {
        this.legends = legends;
    }

    public String getResolutionCodeValue() {
        return resolutionCodeValue;
    }

    public void setResolutionCodeValue(String resolutionCodeValue) {
        this.resolutionCodeValue = resolutionCodeValue;
    }

    public String getTel() {
        return tel;
    }

    public String getTel1() {
        return tel1;
    }

    public String getWeb() {
        return web;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setTel1(String tel1) {
        this.tel1 = tel1;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getMontoenSoles() {
        return MontoenSoles;
    }

    public void setMontoenSoles(String montoenSoles) {
        MontoenSoles = montoenSoles;
    }

    public String getValidezPDF() {
        return validezPDF;
    }

    public void setValidezPDF(String validezPDF) {
        this.validezPDF = validezPDF;
    }

    public String getRegimenRET() {
        return regimenRET;
    }

    public void setRegimenRET(String regimenRET) {
        this.regimenRET = regimenRET;
    }

    public String getMontoTotalDoc() {
        return montoTotalDoc;
    }

    public void setMontoTotalDoc(String montoTotalDoc) {
        this.montoTotalDoc = montoTotalDoc;
    }

    public String getComentarios() {
        return comentarios;
    }

    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }

    public String getTotal_doc_value() {
        return total_doc_value;
    }

    public void setTotal_doc_value(String total_doc_value) {
        this.total_doc_value = total_doc_value;
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


    public List<WrapperItemObject> getItemListDynamic() {
        return itemListDynamic;
    }

    public void setItemListDynamic(List<WrapperItemObject> itemListDynamic) {
        this.itemListDynamic = itemListDynamic;
    }

}
