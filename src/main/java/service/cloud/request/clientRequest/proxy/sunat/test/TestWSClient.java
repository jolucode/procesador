package service.cloud.request.clientRequest.proxy.sunat.test;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;
import service.cloud.request.clientRequest.proxy.sunat.security.HeaderHandlerResolver;
import service.cloud.request.clientRequest.proxy.sunat.test.service.BillService;
import service.cloud.request.clientRequest.proxy.sunat.test.service.BillService_Service;
import service.cloud.request.clientRequest.utils.Utils;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class TestWSClient implements ISunatClient {

    private static final Logger logger = Logger.getLogger(TestWSClient.class);

    protected Consumer consumer;

    protected boolean printOption;


    private URL WSDL_LOCATION;
    private QName SERVICE;

    /**
     * Este metodo define el objeto Consumidor que contiene los datos del emisor
     * electronico (RUC, Usuario secundario SOL y Clave secundaria SOL).
     */
    @Override
    public void setConsumer(Consumer consumer) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-setConsumer()");
        }
        this.consumer = consumer;
    } //setConsumer

    /**
     * Este metodo habilita o desabilita la opcion de mostrar la trama
     * SOAP (Consulta y respuesta).
     */
    @Override
    public void printSOAP(boolean printOption) {
        this.printOption = true;
        if (logger.isDebugEnabled()) {
            logger.debug("PrintSoap" + this.printOption);
        }
    } //printSOAP

    /**
     * Este metodo implementa la seguridad establecida por Sunat, para
     * el envio de documentos electronicos.
     *
     * @return Retorna el objeto BillService de la opcion de 'Test Client'.
     * @throws JAXBException
     */


    protected BillService getSecurityPort(String documentType) throws JAXBException, MalformedURLException {
        if (Utils.isRegularDocument(documentType)) {
            URL baseURL = BillService_Service.class.getResource(".");
            WSDL_LOCATION = new URL(baseURL, "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService?wsdl");
            SERVICE = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "billService");
        } else {
            URL baseURL = BillService_Service.class.getResource(".");
            WSDL_LOCATION = new URL(baseURL, "https://www.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService?wsdl");
            SERVICE = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "billService");
        }
        BillService_Service service = new BillService_Service(WSDL_LOCATION, SERVICE);
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        handlerResolver.setPrintSOAP(printOption);
        service.setHandlerResolver(handlerResolver);
        return service.getBillServicePort();
    } //getSecurityPort

    protected BillService getSecurityPort() throws JAXBException {
        BillService_Service service = new BillService_Service();
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        handlerResolver.setPrintSOAP(printOption);
        service.setHandlerResolver(handlerResolver);
        return service.getBillServicePort();
    }

}
