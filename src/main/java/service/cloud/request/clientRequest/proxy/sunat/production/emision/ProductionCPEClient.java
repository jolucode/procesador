package service.cloud.request.clientRequest.proxy.sunat.production.emision;


import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;
import service.cloud.request.clientRequest.proxy.sunat.production.consult.BillConsultService;
import service.cloud.request.clientRequest.proxy.sunat.production.consult.SunatSoapConsultInterceptor;

import javax.activation.DataHandler;
import java.net.MalformedURLException;
import java.net.URL;

public class ProductionCPEClient extends ProductionWSCPEClient {

    private static final Logger logger = Logger.getLogger(ProductionCPEClient.class);

    @Override
    public byte[] sendBill(String fileName, DataHandler contentFile) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() [PRODUCTION] fileName: " + fileName + " contentFile: " + contentFile);
        }
        SunatSoapInterceptor soapInterceptor = new SunatSoapInterceptor(consumer);
        BillServiceImpl billService = new BillServiceImpl(getLocationForService());
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
        handlerResolver.addHandlers(soapInterceptor);
        billService.setHandlerResolver(handlerResolver);
        BillService billServicePort = billService.getBillServicePort();
        if (logger.isDebugEnabled()) {
            logger.debug("-sendBill() [PRODUCTION]");
        }
        return billServicePort.sendBill(fileName, contentFile);
    }

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
        service.cloud.request.clientRequest.proxy.sunat.production.consult.BillService consultServicePort = billService.getBillConsultServicePort();
        StatusResponse statusResponse = consultServicePort.getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);
        return new StatusResponse(statusResponse.getContent(), statusResponse.getStatusCode(), statusResponse.getStatusMessage());
    }

    private URL getLocationForConsultService() throws MalformedURLException {
        String urlWebService = "https://e-factura.sunat.gob.pe/ol-it-wsconscpegem/billConsultService?wsdl";
        return new URL(urlWebService);
    }
    private URL getLocationForService() throws MalformedURLException {
        return new URL("https://e-factura.sunat.gob.pe/ol-ti-itcpfegem/billService?wsdl");
    }
}

