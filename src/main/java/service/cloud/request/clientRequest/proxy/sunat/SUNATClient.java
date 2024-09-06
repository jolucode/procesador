package service.cloud.request.clientRequest.proxy.sunat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.proxy.security.HeaderHandlerResolver;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.model.Consumer;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class SUNATClient implements ISUNATClient {

    private final Logger logger = Logger.getLogger(SUNATClient.class);

    @Value("${application.soap-client.sunat.base-url-emision}")
    private String sunatBaseUrlEmision;

    @Value("${application.soap-client.sunat.base-url-retper}")
    private String sunatBaseUrlRetPer;


    @Autowired
    ClientProperties clientProperties;


    @Override
    public CdrStatusResponse sendBill(String ruc, String fileName, DataHandler contentFile) throws Exception {

        CdrStatusResponse cdrStatusResponse = new CdrStatusResponse();

        String[] partes = fileName.split("-");
        try {
            cdrStatusResponse.setContent(getSecurityPort(ruc, partes[1]).sendBill(fileName, contentFile));
        } catch (SOAPFaultException e) {
            String sErrorCodeSUNAT = e.getFault().getFaultCode() + " - " + e.getFault().getFaultString();
            cdrStatusResponse.setStatusMessage(sErrorCodeSUNAT);
        }
        return cdrStatusResponse;
    }


    @Override
    public String sendSummary(String ruc, String fileName, DataHandler contentFile) throws Exception {
        String[] partes = fileName.split("-");
        String ticket = getSecurityPort(ruc, partes[1]).sendSummary(fileName, contentFile);
        return ticket;
    }

    protected BillServiceSunat getSecurityPort(String ruc, String docType) throws JsonProcessingException {

        final QName SERVICE = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "billService");
        final QName BillServicePort = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "BillServicePort");
        Client client = clientProperties.listaClientesOf(ruc);

        String ruta = (docType.equals("20") || docType.equals("40")) ? sunatBaseUrlRetPer : sunatBaseUrlEmision;

        try {
            BillService_Service_Sunat service = new BillService_Service_Sunat(new URL(ruta), SERVICE);

            Consumer consumer = new Consumer();
            consumer.setUsername(client.getUsuarioSol());
            consumer.setPassword(client.getClaveSol());

            HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            service.setHandlerResolver(handlerResolver);

            return service.getBillServicePort(BillServicePort);
        } catch (MalformedURLException e) {
            logger.error("URL malformada: " + ruta, e);
            throw new RuntimeException("Error al configurar el servicio OSE: URL malformada.");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}