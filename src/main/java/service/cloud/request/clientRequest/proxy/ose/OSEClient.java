package service.cloud.request.clientRequest.proxy.ose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.proxy.model.Consumer;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.security.HeaderHandlerResolver;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class OSEClient implements IOSEClient {

    private final Logger logger = Logger.getLogger(OSEClient.class);

    @Value("${application.soap-client.ose.base-url}")
    private String oseBaseUrl;

    @Autowired
    ClientProperties clientProperties;

    @Override
    public CdrStatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception {
        try {
            CdrStatusResponse response = getSecurityPort(rucComprobante).getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);
            return response;
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            CdrStatusResponse response = new CdrStatusResponse();
            response.setStatusMessage(exceptionProxy.getDescripcion());
            return response;

        }
    }

    @Override
    public CdrStatusResponse sendBill(String ruc, String fileName, DataHandler contentFile) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() fileName[" + fileName + "], contentFile[" + contentFile + "]");
        }
        CdrStatusResponse cdrStatusResponse = new CdrStatusResponse();
        try {
            cdrStatusResponse.setContent(getSecurityPort(ruc).sendBill(fileName, contentFile));
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            Response.builder().errorCode(e.getMessage()).errorMessage(exceptionProxy.getDescripcion()).build();
            cdrStatusResponse.setStatusMessage(exceptionProxy.getDescripcion());
            System.out.println("Value");
        }
        return cdrStatusResponse;
    }

    protected BillService getSecurityPort(String ruc) throws JsonProcessingException {

        final QName SERVICE = new QName("http://service.sunat.gob.pe", "billService");
        final QName BillServicePort = new QName("http://service.sunat.gob.pe", "BillServicePort");


        Client client = clientProperties.listaClientesOf(ruc);
        try {
            BillService_Service service = new BillService_Service(new URL(oseBaseUrl), SERVICE);

            Consumer consumer = new Consumer();
            consumer.setUsername(client.getUsuarioSol());
            consumer.setPassword(client.getClaveSol());

            HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            service.setHandlerResolver(handlerResolver);

            return service.getBillServicePort(BillServicePort);
        } catch (MalformedURLException e) {
            logger.error("URL malformada: " + oseBaseUrl, e);
            throw new RuntimeException("Error al configurar el servicio OSE: URL malformada.");
        } catch (Exception e) {
            logger.error("Error al configurar el servicio OSE", e);
            throw new RuntimeException("Error al configurar el servicio OSE.");
        }
    }

}