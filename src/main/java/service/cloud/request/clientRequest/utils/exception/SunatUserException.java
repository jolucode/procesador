package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa el tipo de excepcion del emisor electronico para el
 * envio de documentos UBL a Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class SunatUserException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 3221368616789871492L;

    private ErrorObj error;

    /**
     * Constructor para la clase SunatUserException.
     *
     * @param message El mensaje de error.
     */
    public SunatUserException(String message) {
        super(message);
    } // SunatUserException

    /**
     * Constructor para la clase SunatUserException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public SunatUserException(Throwable e) {
        super(e);
    } // SunatUserException

    /**
     * Constructor para la clase SunatUserException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public SunatUserException(String message, Throwable e) {
        super(message, e);
    } // SunatUserException

    /**
     * Constructor para la clase SunatUserException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public SunatUserException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // SunatUserException

    /**
     * Constructor para la clase SunatUserException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public SunatUserException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } // SunatUserException

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

} // SunatUserException
