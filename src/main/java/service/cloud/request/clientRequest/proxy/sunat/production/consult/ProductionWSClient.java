package service.cloud.request.clientRequest.proxy.sunat.production.consult;


import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;
import service.cloud.request.clientRequest.proxy.sunat.security.HeaderHandlerResolver;

import javax.xml.bind.JAXBException;

/**
 * Esta clase implementa algunos metodos de la interfaz ISunatClient. Ademas,
 * realiza la seguridad (WS Security) establecida por Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public abstract class ProductionWSClient implements ISunatClient {

    private static final Logger logger = Logger.getLogger(ProductionWSClient.class);

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
        this.printOption = printOption;
    } //printSOAP

    /**
     * Este metodo implementa la seguridad establecida por Sunat, para
     * el envio de documentos electronicos.
     *
     * @return Retorna el objeto BilService de la opcion de 'Production Client'.
     * @throws JAXBException
     */
    protected BillService getSecurityPort() throws JAXBException {
        BillService_Service service = new BillService_Service();

        service.cloud.request.clientRequest.proxy.sunat.security.HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver(consumer);
        handlerResolver.setPrintSOAP(printOption);

        service.setHandlerResolver(handlerResolver);
        return service.getBillServicePort();
    } //getSecurityPort

} //ProductionWSClient
