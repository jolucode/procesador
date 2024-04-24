package service.cloud.request.clientRequest.proxy.sunat.factory;

import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;

import javax.activation.DataHandler;

/**
 * Esta interfaz contiene los metodos a implementar para acceder al
 * servicio web de la Sunat de tipo 'test', 'homologation'.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public interface ISunatClient {


    byte[] sendBill(String fileName, DataHandler contentFile) throws Exception;

    public abstract StatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception;

    void setConsumer(Consumer consumer);

    void printSOAP(boolean printOption);



} //ISunatClient
