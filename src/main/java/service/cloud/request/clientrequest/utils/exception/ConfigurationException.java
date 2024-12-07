package service.cloud.request.clientrequest.utils.exception;

public class ConfigurationException extends Exception {

    private static final long serialVersionUID = 8868416388422803210L;

    /**
     * Constructor para la clase ConfigurationException.
     *
     * @param message El mensaje de error.
     */
    public ConfigurationException(String message) {
        super(message);
    } // ConfigurationException

    /**
     * Constructor para la clase ConfigurationException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public ConfigurationException(Throwable e) {
        super(e);
    } // ConfigurationException

    /**
     * Constructor para la clase ConfigurationException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public ConfigurationException(String message, Throwable e) {
        super(message, e);
    } // ConfigurationException

} // ConfigurationException
