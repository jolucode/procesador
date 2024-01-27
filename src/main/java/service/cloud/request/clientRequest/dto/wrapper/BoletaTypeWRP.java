package service.cloud.request.clientRequest.dto.wrapper;

import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;

public class BoletaTypeWRP {

    /******************************
     *
     * Patron Singleton
     *
     * *************************/

    public static BoletaTypeWRP instance = null;

    protected BoletaTypeWRP() {

    }

    public static BoletaTypeWRP getInstance() {
        if (instance == null) {
            instance = new BoletaTypeWRP();
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
