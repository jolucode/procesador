package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

public class UBLDocumentWRP  implements Cloneable{

    /**
     * ************
     * <p>
     * Patron Singleton
     * <p>
     * **********************
     */
    private static UBLDocumentWRP instance = null;

    public  UBLDocumentWRP() {

    }

    public static UBLDocumentWRP getInstance() {
        if (instance == null) {
            instance = new UBLDocumentWRP();
        }
        return instance;
    }

    private Transaccion transaccion;

    private InvoiceType invoiceType;

    private InvoiceType boletaType;

    private CreditNoteType creditNoteType;

    private DebitNoteType debitNoteType;

    private PerceptionType perceptionType;

    private RetentionType retentionType;

    private DespatchAdviceType adviceType;


    public DespatchAdviceType getAdviceType() {
        return adviceType;
    }

    public void setAdviceType(DespatchAdviceType adviceType) {
        this.adviceType = adviceType;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Transaccion transaccion) {
        this.transaccion = transaccion;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

    public InvoiceType getBoletaType() {
        return boletaType;
    }

    public void setBoletaType(InvoiceType boletaType) {
        this.boletaType = boletaType;
    }

    public CreditNoteType getCreditNoteType() {
        return creditNoteType;
    }

    public void setCreditNoteType(CreditNoteType creditNoteType) {
        this.creditNoteType = creditNoteType;
    }

    public DebitNoteType getDebitNoteType() {
        return debitNoteType;
    }

    public void setDebitNoteType(DebitNoteType debitNoteType) {
        this.debitNoteType = debitNoteType;
    }

    public PerceptionType getPerceptionType() {
        return perceptionType;
    }

    public void setPerceptionType(PerceptionType perceptionType) {
        this.perceptionType = perceptionType;
    }

    public RetentionType getRetentionType() {
        return retentionType;
    }

    public void setRetentionType(RetentionType retentionType) {
        this.retentionType = retentionType;
    }

    @Override
    public UBLDocumentWRP clone() {
        try {
            UBLDocumentWRP cloned = (UBLDocumentWRP) super.clone();

            // Clonando campos mutables
            if (this.transaccion != null) {
                cloned.transaccion = this.transaccion.clone(); // Asegúrate de que Transaccion también implemente Cloneable
            }
            if (this.invoiceType != null) {
                cloned.invoiceType = this.invoiceType.clone(); // Asegúrate de que InvoiceType implemente Cloneable
            }
            if (this.boletaType != null) {
                cloned.boletaType = this.boletaType.clone(); // Asegúrate de que InvoiceType implemente Cloneable
            }
            if (this.creditNoteType != null) {
                cloned.creditNoteType = this.creditNoteType.clone(); // Asegúrate de que CreditNoteType implemente Cloneable
            }
            if (this.debitNoteType != null) {
                cloned.debitNoteType = this.debitNoteType.clone(); // Asegúrate de que DebitNoteType implemente Cloneable
            }
            //if (this.perceptionType != null) {
            //    cloned.perceptionType = this.perceptionType.clone(); // Asegúrate de que PerceptionType implemente Cloneable
            //}
            if (this.retentionType != null) {
                cloned.retentionType = this.retentionType.clone(); // Asegúrate de que RetentionType implemente Cloneable
            }
            if (this.adviceType != null) {
                cloned.adviceType = this.adviceType.clone(); // Asegúrate de que DespatchAdviceType implemente Cloneable
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Esto nunca debería ocurrir porque implementamos Cloneable
        }
    }

}
