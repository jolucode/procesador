package service.cloud.request.clientRequest.sunat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.ose.BillService;
import service.cloud.request.clientRequest.ose.BillService_Service;
import service.cloud.request.clientRequest.ose.HeaderHandlerResolver;
import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;
import service.cloud.request.clientRequest.ose.model.Consumer;

import javax.activation.DataHandler;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class OSEClient2 implements IOSEClient2 {

    private final Logger logger = Logger.getLogger(OSEClient2.class);


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
    public byte[] sendBill(String fileName, DataHandler contentFile) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() fileName[" + fileName + "], contentFile[" + contentFile + "]");
        }
        byte[] response = null;
        try {
            response = getSecurityPort().sendBill(fileName, contentFile);
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            Response.builder()
                    .errorCode(e.getMessage())
                    .errorMessage(exceptionProxy.getDescripcion())
                    .build();

            System.out.println("Value");
        }


        return response;
    }

    protected BillService2 getSecurityPort() throws JsonProcessingException {
        /*final String BASE_URL = "https://ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
        final String USERNAME = "20552572565";
        fial String PASSWORD = "DTkXyEZi6v";*/

        /*final String BASE_URL = "https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
        final String USERNAME = "20510910517";
        final String PASSWORD = "20510910517";*/

        final String BASE_URL = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService?wsdl";
        final String USERNAME = "20510910517";
        final String PASSWORD = "20510910517";

        try {
            // Configuración del servicio
            BillService_Service2 service = new BillService_Service2(new URL(BASE_URL));

            // Configuración del consumidor
            Consumer consumer = new Consumer();
            consumer.setUsername(USERNAME);
            consumer.setPassword(PASSWORD);

            // Configuración del handler
            HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            service.setHandlerResolver(handlerResolver);

            return service.getBillServicePort();
        } catch (MalformedURLException e) {
            // Loguear el error y retornar null o lanzar una excepción personalizada con un mensaje claro
            logger.error("URL malformada: " + BASE_URL, e);
            throw new RuntimeException("Error al configurar el servicio OSE: URL malformada.");
        } catch (Exception e) {
            // Loguear cualquier otro error y retornar null o lanzar una excepción personalizada con un mensaje claro
            logger.error("Error al configurar el servicio OSE", e);
            throw new RuntimeException("Error al configurar el servicio OSE.");
        }
    }

}