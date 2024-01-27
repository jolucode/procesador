package service.cloud.request.clientRequest.utils.exception;

public class HibernateException extends Exception {

    private static final long serialVersionUID = 5054120023489323556L;

    /**
     * Constructor para la clase HibernateException.
     *
     * @param message El mensaje de error.
     */
    public HibernateException(String message) {
        super(message);
    } // HibernateException

    /**
     * Constructor para la clase HibernateException.
     *
     * @param e Contiene el stacktrace del error.
     */
    public HibernateException(Throwable e) {
        super(e);
    } // HibernateException

    /**
     * Constructor para la clase HibernateException.
     *
     * @param message El mensaje de error.
     * @param e       Contiene el stacktrace del error.
     */
    public HibernateException(String message, Throwable e) {
        super(message, e);
    } // HibernateException

} // HibernateException
