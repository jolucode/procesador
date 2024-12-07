package service.cloud.request.clientrequest.utils.exception;

import service.cloud.request.clientrequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa la excepcion de tipo UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class UBLDocumentException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 4659926513594893893L;

    private ErrorObj error;

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param message El mensaje de error.
     */
    public UBLDocumentException(String message) {
        super(message);
    } // UBLDocumentException

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public UBLDocumentException(Throwable e) {
        super(e);
    } // UBLDocumentException

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public UBLDocumentException(String message, Throwable e) {
        super(message, e);
    } // UBLDocumentException

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public UBLDocumentException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // UBLDocumentException

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public UBLDocumentException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } // UBLDocumentException

    /**
     * Este metodo obtiene el objeto ErrorObj.
     *
     * @return Retorna el objeto ErrorObj.
     */
    public ErrorObj getError() {
        return error;
    } // getError

    /**
     * Este metodo establece el objeto ErrorObj.
     *
     * @param error El objeto ErrorObj.
     */
    public void setError(ErrorObj error) {
        this.error = error;
    } // setError

} // UBLDocumentException
