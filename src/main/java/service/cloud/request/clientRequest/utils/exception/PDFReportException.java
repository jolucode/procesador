package service.cloud.request.clientRequest.utils.exception;

import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;

/**
 * Esta clase representa el tipo de excepcion del emisor electronico para el
 * envio de documentos UBL a Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class PDFReportException extends Exception implements
        java.io.Serializable {

    private static final long serialVersionUID = -2062202786220686656L;

    private ErrorObj error;

    /**
     * Constructor para la clase PDFReportException.
     *
     * @param message El mensaje de error.
     */
    public PDFReportException(String message) {
        super(message);
    } // PDFReportException

    /**
     * Constructor para la clase PDFReportException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public PDFReportException(Throwable e) {
        super(e);
    } // PDFReportException

    /**
     * Constructor para la clase PDFReportException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public PDFReportException(String message, Throwable e) {
        super(message, e);
    } // PDFReportException

    /**
     * Constructor para la clase PDFReportException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     */
    public PDFReportException(ErrorObj error) {
        super(error.getMessage());
        this.error = error;
    } // PDFReportException

    /**
     * Constructor para la clase PDFReportException.
     *
     * @param error Objeto error, contiene el codigo de error y el mensaje.
     * @param e     Contiene el stacktrace del error.
     */
    public PDFReportException(ErrorObj error, Throwable e) {
        super(error.getMessage(), e);
        this.error = error;
    } // PDFReportException

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

} // PDFReportException
