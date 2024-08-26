package service.cloud.request.clientRequest.ose.model;

/**
 * Este clase representa el objeto consumidor que contiene la
 * informacion de acceso del emisor electronio a la Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class Consumer {

    /* Declaracion de variables */
    private String username;

    private String password;

    private String ruc;


    /**
     * Constructor basico para la clase Consumer.
     */
    public Consumer() {
    }

    /**
     * Constructor para la clase Consumer.
     *
     * @param username El usuario secundario SOL.
     * @param password La clave secundaria SOL.
     * @param ruc      El RUC del emisor electronico.
     */
    public Consumer(String username, String password, String ruc) {
        this.username = username;
        this.password = password;
        this.ruc = ruc;
    }

    public Consumer(String username, String password) {
        this.username = username;
        this.password = password;
    }


    /**
     * Este metodo obtiene el nombre usuario secundario SOL.
     *
     * @return Retorna el nombre de usuario secundario SOL.
     */
    public String getUsername() {
        return username;
    } //getUsername

    /**
     * Este metodo define el nombre de usuario secundario SOL.
     *
     * @param username El usuario secundario SOL.
     */
    public void setUsername(String username) {
        this.username = username;
    } //setUsername

    /**
     * Este metodo obtiene la clave secundaria SOL.
     *
     * @return Retorna la clave secundaria SOL.
     */
    public String getPassword() {
        return password;
    } //getPassword

    /**
     * Este metodo define la clave secundaria SOL.
     *
     * @param password La clave secundaria SOL.
     */
    public void setPassword(String password) {
        this.password = password;
    } //setPassword

    /**
     * Este metodo obtiene el RUC (Registro unico del contribuyente) del
     * emisor electronico.
     *
     * @return Retorna el RUC del emisor electronico.
     */
    public String getRuc() {
        return ruc;
    } //getRuc

    /**
     * Este metodo define el RUC (Registro unico del contribuyente) del
     * emisor electronico.
     *
     * @param ruc El numero de RUC del emisor electronico.
     */
    public void setRuc(String ruc) {
        this.ruc = ruc;
    } //setRuc

} //Consumer
