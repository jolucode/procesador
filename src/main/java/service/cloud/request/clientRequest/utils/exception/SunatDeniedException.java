package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Este clase representa el tipo de excepcion que ocurre dentro del proceso de
 * HOMOLOGACION cuando un documento es rechazado por Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class SunatDeniedException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = 2901011962219963496L;

    private ErrorObj error;

    /**
     * Constructor para la clase SunatDeniedException.
     *
     * @param message El mensaje de error.
     */
    public SunatDeniedException(String message) {
        super(message);
    } // SunatDeniedException

    /**
     * Constructor para la clase SunatDeniedException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public SunatDeniedException(Throwable e) {
        super(e);
    } // SunatDeniedException

    /**
     * Constructor para la clase SunatDeniedException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public SunatDeniedException(String message, Throwable e) {
        super(message, e);
    } // SunatDeniedException

    /**
     * Constructor para la clase SunatDeniedException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public SunatDeniedException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // SunatDeniedException

    /**
     * Constructor para la clase SunatDeniedException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public SunatDeniedException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } // SunatDeniedException

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

} // SunatDeniedException
