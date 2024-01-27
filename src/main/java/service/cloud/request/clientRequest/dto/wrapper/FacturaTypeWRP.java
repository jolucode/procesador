package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;

public class FacturaTypeWRP {

    /**
     * ****************************
     * <p>
     * Patron Singleton
     * <p>
     * ************************
     */
    private static FacturaTypeWRP instance = null;

    protected FacturaTypeWRP() {

    }

    public static FacturaTypeWRP getInstance() {
        if (instance == null) {
            instance = new FacturaTypeWRP();

        }
        return instance;
    }

    private InvoiceType invoiceType;

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

}
