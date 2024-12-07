package service.cloud.request.clientrequest.proxy.sunat.consulta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.cloud.request.clientrequest.config.ClientProperties;
import service.cloud.request.clientrequest.exception.ExceptionProxy;
import service.cloud.request.clientrequest.model.Client;
import service.cloud.request.clientrequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientrequest.proxy.model.Consumer;
import service.cloud.request.clientrequest.proxy.security.HeaderHandlerResolver;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class SUNATClientConsult implements ISUNATClientConsult {

    private final Logger logger = Logger.getLogger(SUNATClientConsult.class);

    @Value("${application.soap-client.sunat.base-url-consulta}")
    private String sunatBaseUrlConsulta;

    @Autowired
    ClientProperties clientProperties;

    @Override
    public CdrStatusResponse getStatus(String ruc, String ticket) throws Exception {
        return getSecurityPort(ruc).getStatus(ticket);
    }

    @Override
    public CdrStatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante,
                                          Integer numeroComprobante) throws Exception {
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

    protected BillServiceSunatConsult getSecurityPort(String ruc) throws JsonProcessingException {

        final QName SERVICE = new QName("http://service.ws.consulta.comppago.electronico.registro.servicio2.sunat.gob.pe/", "billConsultService");
        final QName BillServicePort = new QName("http://service.ws.consulta.comppago.electronico.registro.servicio2.sunat.gob.pe/", "BillConsultServicePort");

        Client client = clientProperties.listaClientesOf(ruc);

        try {

            BillService_Service_Sunat_Consult service = new BillService_Service_Sunat_Consult(new URL(sunatBaseUrlConsulta), SERVICE);

            Consumer consumer = new Consumer();
            consumer.setUsername(client.getUsuarioSol());
            consumer.setPassword(client.getClaveSol());

            HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
            service.setHandlerResolver(handlerResolver);

            return service.getBillServicePort(BillServicePort);
        } catch (MalformedURLException e) {
            logger.error("URL malformada: " + sunatBaseUrlConsulta, e);
            throw new RuntimeException("Error al configurar el servicio OSE: URL malformada.");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}