package service.cloud.request.clientRequest.proxy.ose;


import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.object.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.ose.object.StatusResponse;

import javax.activation.DataHandler;

/**
 * Esta interfaz contiene los metodos ha implementar para acceder al Servicio Web OSE de TCI.
 *
 * @author Jose Manuel Lucas Barrera
 */
public interface IOSEClient {

    public static final String TEST_CLIENT = "test";
    public static final String PRODUCTION_CLIENT = "production";

    /**
     * Este metodo ha implementar enviara el documento ('Factura', 'Boleta', 'Nota de Credito' o
     * 'Nota de Debito') al Servicio Web OSE de TCI, retornando un CDR OSE de respuesta.
     *
     * @param fileName    El nombre del archivo adjunto
     * @param contentFile El archivo ZIP adjunto.
     * @return Retorna el CDR OSE de respuesta.
     * @throws Exception
     */
    public abstract byte[] sendBill(String fileName, DataHandler contentFile) throws Exception;

    /**
     * Este metodo ha implementar enviara el documento ('Resumen Diario' o 'Comunicacion de Baja') al
     * Servicio Web OSE de TCI, retornando un numero de ticket.
     *
     * @param fileName    El nombre del archivo adjunto
     * @param contentFile El archivo ZIP adjunto.
     * @return Retorna el numero de ticket.
     * @throws Exception
     */
    public abstract String sendSummary(String fileName, DataHandler contentFile) throws Exception;

    /**
     * Este metodo ha implementar enviara un bloque de documentos al Servicio Web OSE de TCI,
     * retornando un numero de ticket.
     *
     * @param fileName    El nombre del archivo adjunto
     * @param contentFile El archivo ZIP adjunto.
     * @return Retorna el numero de ticket.
     * @throws Exception
     */
    public abstract String sendPack(String fileName, DataHandler contentFile) throws Exception;

    /**
     * Este metodo ha implementar consultara el estado de un documento ('Factura', 'Nota de Credito'
     * o 'Nota de Debito'), retornando un objeto StatusResponse que contiene el CDR OSE de respuesta
     * y el codigo de respuesta.
     *
     * @param ticket El numero de ticket de consulta.
     * @return Retorna el objeto StatusResponse que contiene el CDR OSE de respuesta y el codigo de respuesta.
     * @throws Exception
     */
    public abstract StatusResponse getStatus(String ticket) throws Exception;

    /**
     * Este metodo ha implementar consultara el estado de un CDR OSE, retornando un objeto CdrStatusResponse
     * que contiene el CDR OSE de respuesta, el codigo de respuesta y el mensaje de respuesta.
     *
     * @param rucComprobante    Numero RUC del comprobante a consultar.
     * @param tipoComprobante   Tipo del comprobante.
     * @param serieComprobante  Serie del comprobante.
     * @param numeroComprobante Numero del comprobante.
     * @return Retorna el objeto CdrStatusResponse que contiene el CDR OSE de respuesta, el codigo de respuesta
     * y el mensaje de respuesta.
     * @throws Exception
     */
    public abstract CdrStatusResponse getStatusCDR(String rucComprobante,
                                                   String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception;


    //************************************************************************************
    //*********************************** OTROS METODOS **********************************
    //************************************************************************************

    /**
     * Este metodo ha implementar definira el objeto consumidor el cual contiene los datos del
     * emisor electronico (Usuario TCI y Password TCI).
     *
     * @param consumer Objeto consumidor.
     */
    public abstract void setConsumer(Consumer consumer);

    /**
     * Este metodo ha implementar definira la habilitacion o deshabilitacion de la opcion que muestra
     * la trama SOAP (request and response).
     *
     * @param printOption Valor booleano que definira la habilitacion o deshabilitacion de la opcion que muestra
     *                    la trama SOAP (request and response).
     */
    public abstract void printSOAP(boolean printOption);

} //IOSEClient
