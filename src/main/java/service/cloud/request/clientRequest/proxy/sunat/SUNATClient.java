package service.cloud.request.clientRequest.proxy.sunat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.proxy.ose.HeaderHandlerResolver;
import service.cloud.request.clientRequest.proxy.ose.model.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.ose.model.Consumer;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class SUNATClient implements ISUNATClient {

    private final Logger logger = Logger.getLogger(SUNATClient.class);


    @Override
    public CdrStatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante,
                                          Integer numeroComprobante) throws Exception {
        long initTime = System.currentTimeMillis();
        try {
            //CdrStatusResponse response = getSecurityPort().getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);
            //return response;
            return null;
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            CdrStatusResponse response = new CdrStatusResponse();
            response.setStatusMessage(exceptionProxy.getDescripcion());
            return response;

        }
    }


    @Override
    public CdrStatusResponse sendBill(String fileName, DataHandler contentFile) throws Exception {

        CdrStatusResponse cdrStatusResponse = new CdrStatusResponse();

        try {
            cdrStatusResponse.setContent(getSecurityPort().sendBill(fileName, contentFile));
        } catch (SOAPFaultException e) {
            String sErrorCodeSUNAT = e.getFault().getFaultCode() + " - " + e.getFault().getFaultString();
            cdrStatusResponse.setStatusMessage(sErrorCodeSUNAT);
        }
        return cdrStatusResponse;
    }

    protected BillService2 getSecurityPort() throws JsonProcessingException {

        final String BASE_URL = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService?wsdl";
        final String USERNAME = "20510910517";
        final String PASSWORD = "20510910517";


        final QName SERVICE = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "billService");
        final QName BillServicePort = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "BillServicePort");


        try {
            BillService_Service2 service = new BillService_Service2(new URL(BASE_URL), SERVICE);

            Consumer consumer = new Consumer();
            consumer.setUsername(USERNAME);
            consumer.setPassword(PASSWORD);

            HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            service.setHandlerResolver(handlerResolver);

            return service.getBillServicePort(BillServicePort);
        } catch (MalformedURLException e) {
            logger.error("URL malformada: " + BASE_URL, e);
            throw new RuntimeException("Error al configurar el servicio OSE: URL malformada.");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}