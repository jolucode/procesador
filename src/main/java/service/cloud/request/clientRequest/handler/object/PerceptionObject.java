package service.cloud.request.clientRequest.handler.object;

import service.cloud.request.clientRequest.handler.object.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.object.legend.LegendObject;

import java.io.InputStream;
import java.util.List;

public class PerceptionObject {

    /* Informacion general */
    private String documentIdentifier;

    private String issueDate;

    private String currencyValue;

    private String sunatTransaction;

    /* Informacion del emisor */
    private String senderSocialReason;

    private String senderRuc;

    private String senderFiscalAddress;

    private String senderDepProvDist;

    private String senderContact;

    private String senderMail;

    private String senderLogo;

    private String telValue;

    private String tel2Value;

    private String webValue;

    private String comments;

    private String importeTexto;

    private String digestValue;

    private InputStream codeQR;

    /* Informacion del receptor */
    private String receiverSocialReason;

    private String receiverRuc;

    /* Lista de items */
    private List<PerceptionItemObject> perceptionItems;

    private List<WrapperItemObject> itemListDynamic;

    /* Montos */
    private String totalAmountValue;

    /* Codigo de barras */
    private InputStream barcodeValue;

    /* Importe en letras */
    private String letterAmountValue;

    /* Lista de legendas */
    private List<LegendObject> legends;

    /* Codigo de resolucion de autorizacion por Sunat */
    private String resolutionCodeValue;

    private String validezPDF;

    public PerceptionObject() {
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

    public List<PerceptionItemObject> getPerceptionItems() {
        return perceptionItems;
    }

    public void setPerceptionItems(List<PerceptionItemObject> perceptionItems) {
        this.perceptionItems = perceptionItems;
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

    public String getTelValue() {
        return telValue;
    }

    public String getTel2Value() {
        return tel2Value;
    }

    public String getWebValue() {
        return webValue;
    }

    public String getComments() {
        return comments;
    }

    public void setTelValue(String telValue) {
        this.telValue = telValue;
    }

    public void setTel2Value(String tel2Value) {
        this.tel2Value = tel2Value;
    }

    public void setWebValue(String webValue) {
        this.webValue = webValue;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getImporteTexto() {
        return importeTexto;
    }

    public void setImporteTexto(String importeTexto) {
        this.importeTexto = importeTexto;
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

    public List<WrapperItemObject> getItemListDynamic() {
        return itemListDynamic;
    }

    public void setItemListDynamic(List<WrapperItemObject> itemListDynamic) {
        this.itemListDynamic = itemListDynamic;
    }

}
