package service.cloud.request.clientRequest.proxy.sunat.test;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;
import service.cloud.request.clientRequest.utils.Utils;

import javax.activation.DataHandler;

/**
 * Esta clase implementa los metodos faltantes de la interfaz ISunatClient, relacionados
 * a los metodos extraidos del servicio web de Test.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class TestClient extends TestWSClient {

    private static final Logger logger = Logger.getLogger(TestClient.class);

    @Override
    public StatusResponse getStatus(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception {
        return null;
    }

    @Override
    public StatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception {
        return null;
    }

    /**
     * Este metodo envia una 'Factura', 'Boleta', 'Nota de Credito' o 'Nota de Debito' al
     * servicio web de Sunat, retornando un CDR de respuesta.
     */
    @Override
    public byte[] sendBill(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() [TEST] fileName: " + fileName + " contentFile: " + contentFile);
        }

        String documentType = Utils.extractDocType(fileName);
        byte[] cdrResponse = getSecurityPort(documentType).sendBill(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendBill() [TEST]");
        }
        return cdrResponse;
    } //sendBill

    /**
     * Este metodo envia una 'Comunicacion de Baja' o 'Resumen Diario' al servicio web de la
     * Sunat, retornando un numero de ticket.
     */
    @Override
    public String sendSummary(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendSummary() [TEST] fileName: " + fileName + " contentFile: " + contentFile);
        }

        String documentType = Utils.extractDocType(fileName);
        String response = getSecurityPort(documentType).sendSummary(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendSummary() [TEST]");
        }
        return response;
    } //sendSummary

    /**
     * Este metodo envia un documento al servicio web de la Sunat, retornando un numero de
     * ticket.
     */
    @Override
    public String sendPack(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendPack() [TEST] fileName: " + fileName + " contentFile: " + contentFile);
        }

        String documentType = Utils.extractDocType(fileName);
        String response = getSecurityPort(documentType).sendPack(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendPack() [TEST]");
        }
        return response;
    } //sendPack

    /**
     * Este metodo envia el ticket al servicio web de la Sunat, retornando un objeto que contiene
     * el codigo de respuesta y el CDR de respuesta.
     */
    @Override
    public StatusResponse getStatus(String ticket) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStatus() [TEST] ticket: " + ticket);
        }

        //StatusResponse response = getSecurityPort().getStatus(ticket);

        if (logger.isDebugEnabled()) {
            logger.debug("-getStatus() [TEST]");
        }
        return null;
        //return response;
    } //getStatus

} //TestClient
