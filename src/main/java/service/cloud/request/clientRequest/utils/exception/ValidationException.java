package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa la excepcion de tipo UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class ValidationException extends Exception implements java.io.Serializable {

    private static final long serialVersionUID = -3227524743909619028L;

    private ErrorObj error;

    /**
     * Constructor para la clase UBLDocumentException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public ValidationException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } //UBLDocumentException

    /**
     * Este metodo obtiene el objeto ErrorObj.
     *
     * @return Retorna el objeto ErrorObj.
     */
    public ErrorObj getError() {
        return error;
    } //getError

    /**
     * Este metodo establece el objeto ErrorObj.
     *
     * @param error El objeto ErrorObj.
     */
    public void setError(ErrorObj error) {
        this.error = error;
    } //setError

} //UBLDocumentException
