package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

public class SoapFaultException extends Exception implements java.io.Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 4770411452264097320L;

    /**
     * The fault that caused this exception.
     */
    private ErrorObj error;

    /**
     * Constructor.
     */
    public SoapFaultException(String message) {
        super(message);
    }


    public SoapFaultException(Throwable e) {
        super(e);
    }


    public SoapFaultException(String message, Throwable e) {
        super(message, e);
    }

    public SoapFaultException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    }

    public SoapFaultException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } //SunatGenericException

    public ErrorObj getError() {
        return error;
    } //getError

    public void setError(ErrorObj error) {
        this.error = error;
    } //setError

}