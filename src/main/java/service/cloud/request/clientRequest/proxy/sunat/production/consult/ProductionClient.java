package service.cloud.request.clientRequest.proxy.sunat.production.consult;


import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Esta clase implementa los metodos faltantes de la interfaz ISunatClient, relacionados
 * a los metodos extraidos del servicio web de Produccion.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class ProductionClient extends ProductionWSClient {

    private static final Logger logger = Logger.getLogger(ProductionClient.class);


    /**
     * Este metodo envia una 'Factura', 'Boleta', 'Nota de Credito' o 'Nota de Debito' al
     * servicio web de Sunat, retornando un CDR de respuesta.
     */
    @Override
    public byte[] sendBill(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() [PRODUCTION] fileName: " + fileName + " contentFile: " + contentFile);
        }

        byte[] cdrResponse = null;//getSecurityPort().sendBill(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendBill() [PRODUCTION]");
        }
        return cdrResponse;
    } //sendBill


    //public StatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception {
    //    return null;
    //}
    @Override
    public StatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStatusCdr() [PRODUCTION] rucComprobante: " + rucComprobante + " tipoComprobante: " + tipoComprobante + " serieComprobante: " + serieComprobante + " numeroComprobante: " + numeroComprobante);
        }
        SunatSoapConsultInterceptor soapConsultInterceptor = new SunatSoapConsultInterceptor(consumer);
        BillConsultService billService = new BillConsultService(getLocationForConsultService());
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
        handlerResolver.addHandlers(soapConsultInterceptor);
        billService.setHandlerResolver(handlerResolver);
        BillServiceCon consultServicePort = billService.getBillConsultServicePort();
        StatusResponse statusResponse = consultServicePort.getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);
        return new StatusResponse(statusResponse.getContent(), statusResponse.getStatusCode(), statusResponse.getStatusMessage());
    }

    /**
     * Este metodo envia una 'Comunicacion de Baja' o 'Resumen Diario' al servicio web de la
     * Sunat, retornando un numero de ticket.
     */@Override
   public String sendSummary(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendSummary() [PRODUCTION] fileName: " + fileName + " contentFile: " + contentFile);
        }

        String response = getSecurityPort().sendSummary(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendSummary() [PRODUCTION]");
        }
        return response;
    }//sendSummary

    /**
     * Este metodo envia un documento al servicio web de la Sunat, retornando un numero de
     * ticket.
     */
    /*@Override
    public String sendPack(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendPack() [PRODUCTION] fileName: " + fileName + " contentFile: " + contentFile);
        }

        String response = getSecurityPort().sendPack(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendPack() [PRODUCTION]");
        }
        return response;
    }*/ //sendPack

    /**
     * Este metodo envia el ticket al servicio web de la Sunat, retornando un objeto que contiene
     * el codigo de respuesta y el CDR de respuesta.
     */
    @Override
    public StatusResponse getStatus(String ticket) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStatus() [PRODUCTION] ticket: " + ticket);
        }

        StatusResponse response = getSecurityPort().getStatus(ticket);

        if (logger.isDebugEnabled()) {
            logger.debug("-getStatus() [PRODUCTION]");
        }
        return response;
    } //getStatus
    private URL getLocationForConsultService() throws MalformedURLException {
        String urlWebService = "https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService?wsdl";
        return new URL(urlWebService);
    }

} //ProductionClient
