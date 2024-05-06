package service.cloud.request.clientRequest.proxy.sunat.factory;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.sunat.production.consult.ProductionClient;
import service.cloud.request.clientRequest.proxy.sunat.production.emision.ProductionCPEClient;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.proxy.sunat.config.ISunatConfig;
import service.cloud.request.clientRequest.proxy.sunat.test.TestClient;


/**
 * Esta clase implementa la clase SunatClientFactory y realiza el "FACTORY" para la
 * obtencion de un cliente de servicio web, llamandolo por su nombre.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public final class SunatClientFactoryImpl extends SunatClientFactory {

    private static final Logger logger = Logger.getLogger(SunatClientFactoryImpl.class);


    @Override
    public ISunatClient getSunatClient(String clientName) throws ConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getSunatClient() clientName: " + clientName);
        }
        ISunatClient sunatClient = null;

        if (clientName.equalsIgnoreCase(ISunatConfig.PRODUCTION_CLIENT)) {
            if (logger.isInfoEnabled()) {
                logger.info("getSunatClient() Create a PRODUCTION_CLIENT.");
            }
            sunatClient = new ProductionClient();
        } else if (clientName.equalsIgnoreCase(ISunatConfig.TEST_CLIENT)) {
            if (logger.isInfoEnabled()) {
                logger.info("getSunatClient() Create a TEST_CLIENT.");
            }
            sunatClient = new TestClient();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getSunatClient()");
        }
        return sunatClient;
    } //getSunatClient

    /**
     * Este metodo obtiene un objeto de interfaz ISunatClient en base al nombre del cliente.
     */
    @Override
    public ISunatClient getSunatClient(String clientName, String documentType) throws ConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getSunatClient() clientName: " + clientName);
        }
        ISunatClient sunatClient = null;

        if (clientName.equalsIgnoreCase(ISunatConfig.TEST_CLIENT)) {
            sunatClient = new TestClient();
            if (logger.isInfoEnabled()) {
                logger.info("getSunatClient() Create a TEST_CLIENT para PERCEPCIONES Y RETENCIONES.");
            }

        } else if (clientName.equalsIgnoreCase(ISunatConfig.PRODUCTION_CLIENT)) {
            sunatClient = new ProductionCPEClient();
            if (logger.isInfoEnabled()) {
                logger.info("getSunatClient() Create a PRODUCTION_CLIENT para PERCEPCIONES Y RETENCIONES.");
            }
        } else {
            logger.error("getSunatClient() " + IVenturaError.ERROR_151.getMessage());
            throw new ConfigurationException(IVenturaError.ERROR_151.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getSunatClient()");
        }
        return sunatClient;
    } //getSunatClient

} //SunatClientFactoryImpl
