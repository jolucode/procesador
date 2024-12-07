package service.cloud.request.clientrequest.handler.refactorPdf.dto;

import lombok.Data;
import service.cloud.request.clientrequest.handler.refactorPdf.dto.legend.LegendObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Data
public class DebitNoteObject {
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
    private String typeOfDebitNote;
    private String descOfDebitNote;
    private String documentReferenceToCn;
    private String dateDocumentReference;
    private String senderSocialReason;
    private String senderRuc;
    private String senderFiscalAddress;
    private String senderDepProvDist;
    private String senderContact;
    private String senderMail;
    private String senderLogo;
    private String receiverRegistrationName;
    private String receiverIdentifier;
    private String receiverIdentifierType;
    private String receiverFiscalAddress;
    private List<WrapperItemObject> itemsListDynamic;
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
    private InputStream barcodeValue;
    private String letterAmountValue;
    private List<LegendObject> legends;
    private String resolutionCodeValue;
    private Map<String, String> invoicePersonalizacion;
    private String validezPDF;

    public DebitNoteObject() {
    }
} //DebitNoteObject
