package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

public class SunatGenericException extends Exception implements java.io.Serializable {

    private static final long serialVersionUID = 4604730228897985798L;

    private ErrorObj error;


    /**
     * Constructor para la clase SunatGenericException.
     *
     * @param message El mensaje de error.
     */
    public SunatGenericException(String message) {
        super(message);
    } //SunatGenericException

    /**
     * Constructor para la clase SunatGenericException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public SunatGenericException(Throwable e) {
        super(e);
    } //SunatGenericException

    /**
     * Constructor para la clase SunatGenericException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public SunatGenericException(String message, Throwable e) {
        super(message, e);
    } //SunatGenericException

    /**
     * Constructor para la clase SunatGenericException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public SunatGenericException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } //SunatGenericException

    /**
     * Constructor para la clase SunatGenericException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public SunatGenericException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } //SunatGenericException


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

} //SunatGenericException
