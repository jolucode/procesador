package service.cloud.request.clientRequest.proxy.ose;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;

import java.net.URL;

public abstract class OSEBasicClient implements IOSEClient {

    Logger logger = Logger.getLogger(OSEBasicClient.class);

    private Consumer consumer;

    private boolean printOption;

    private String wsdlLocation;


    protected OSEBasicClient(String clientType) {
        if (clientType.equalsIgnoreCase(TEST_CLIENT)) {
            if (logger.isDebugEnabled()) {
                logger.debug("OSEBasicClient() Conectando OSE, modo TEST.");
            }
            this.wsdlLocation = "https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl"; //VariablesGlobales.rutaOseTestWebservice;
            logger.info("La ruta OSE test: " + this.wsdlLocation);
        } else if (clientType.equals(PRODUCTION_CLIENT)) {
            if (logger.isDebugEnabled()) {
                logger.debug("OSEBasicClient() Conectando OSE, modo PRODUCTION.");
            }
            this.wsdlLocation = "https://ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";//VariablesGlobales.rutaOseWebservice;
            logger.info("La ruta OSE production: " + this.wsdlLocation);
        }

    }


    @Override
    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void printSOAP(boolean printOption) {
        this.printOption = printOption;
    }

    protected BillService getSecurityPort() throws Exception {
        BillService_Service service = StringUtils.isBlank(this.wsdlLocation) ?
                new BillService_Service() :
                new BillService_Service(new URL(new URL("https://ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl"), this.wsdlLocation));/*VariablesGlobales.rutaOseWebservice*/
        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        handlerResolver.setPrintSOAP(printOption);
        service.setHandlerResolver(handlerResolver);
        return service.getBillServicePort();
    }
}
