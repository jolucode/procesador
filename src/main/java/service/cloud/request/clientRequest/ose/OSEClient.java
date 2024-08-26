package service.cloud.request.clientRequest.ose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.ose.model.Consumer;
import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class OSEClient implements IOSEClient{

    private final Logger logger = Logger.getLogger(OSEClient.class);


    @Override
    public CdrStatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante,
                                          Integer numeroComprobante) throws Exception {
        long initTime = System.currentTimeMillis();
        try {
            CdrStatusResponse response = getSecurityPort().getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);
            return response;
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            CdrStatusResponse response = new CdrStatusResponse();
            response.setStatusMessage(exceptionProxy.getDescripcion());
            return response;

        }
    }

    protected BillService getSecurityPort() throws JsonProcessingException {
        final String BASE_URL = "https://ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
        final String USERNAME = "20552572565";
        final String PASSWORD = "DTkXyEZi6v";

        try {
            // Configuración del servicio
            BillService_Service service = new BillService_Service(new URL(BASE_URL));

            // Configuración del consumidor
            Consumer consumer = new Consumer();
            consumer.setUsername(USERNAME);
            consumer.setPassword(PASSWORD);

            // Configuración del handler
            //HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            //service.setHandlerResolver(handlerResolver);

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