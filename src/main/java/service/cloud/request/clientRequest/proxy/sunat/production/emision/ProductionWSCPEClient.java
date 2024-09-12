package service.cloud.request.clientRequest.proxy.sunat.production.emision;


import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;
import javax.xml.bind.JAXBException;

public abstract class ProductionWSCPEClient implements ISunatClient {


    private static final Logger logger = Logger.getLogger(ProductionWSCPEClient.class);

    protected Consumer consumer;

    protected boolean printOption;

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

    protected BillService getSecurityPort() {
        BillService_Service service = new BillService_Service();

        HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        handlerResolver.setPrintSOAP(printOption);

        service.setHandlerResolver(handlerResolver);
        return service.getBillServicePort();
    } //getSecurityPort
}
