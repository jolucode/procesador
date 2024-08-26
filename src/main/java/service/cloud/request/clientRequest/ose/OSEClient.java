package service.cloud.request.clientRequest.ose;

import io.micrometer.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.ose.model.Consumer;
import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

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
        } catch (MalformedURLException e) {
            logger.error("Error en la URL del servicio OSE", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error al obtener el estado del CDR", e);
            throw e;
        }
    }

    protected BillService getSecurityPort() throws MalformedURLException {
        // URL y credenciales configuradas de manera más segura
        final String BASE_URL = "https://ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
        final String USERNAME = "20552572565";//System.getenv("20552572565");
        final String PASSWORD = "DTkXyEZi6v"; //System.getenv("DTkXyEZi6v");

        // Configuración del servicio
        BillService_Service service = new BillService_Service(new URL(BASE_URL));

        // Configuración del consumidor
        Consumer consumer = new Consumer();
        consumer.setUsername(USERNAME);
        consumer.setPassword(PASSWORD);

        // Configuración del handler
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        service.setHandlerResolver(handlerResolver);

        return service.getBillServicePort();
    }
}