package service.cloud.request.clientrequest.utils.exception;

import service.cloud.request.clientrequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa la excepcion de tipo UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class SignerDocumentException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 4659926513594893893L;

    private ErrorObj error;

    /**
     * Constructor para la clase SignerDocumentException.
     *
     * @param message El mensaje de error.
     */
    public SignerDocumentException(String message) {
        super(message);
    } // SignerDocumentException

    /**
     * Constructor para la clase SignerDocumentException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public SignerDocumentException(Throwable e) {
        super(e);
    } // SignerDocumentException

    /**
     * Constructor para la clase SignerDocumentException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public SignerDocumentException(String message, Throwable e) {
        super(message, e);
    } // SignerDocumentException

    /**
     * Constructor para la clase SignerDocumentException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public SignerDocumentException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // SignerDocumentException

    /**
     * Constructor para la clase SignerDocumentException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public SignerDocumentException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } // SignerDocumentException

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

} // SignerDocumentException
