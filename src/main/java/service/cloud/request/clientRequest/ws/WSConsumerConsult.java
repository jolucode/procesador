package service.cloud.request.clientRequest.ws;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.proxy.object.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.ose.IOSEClient;
import service.cloud.request.clientRequest.proxy.ose.OSEClient;
import service.cloud.request.clientRequest.utils.exception.ConectionSunatException;
import service.cloud.request.clientRequest.utils.exception.SoapFaultException;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.prueba.Client;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;
import service.cloud.request.clientRequest.proxy.sunat.factory.SunatClientFactory;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;

import javax.xml.soap.Detail;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.logging.Level;

/**
 * Esta clase contiene los metodos para consultar si se genero la constancia de
 * recepcion de un tipo de documento especifico conectandose al servicio web de
 * CONSULTAS de Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class WSConsumerConsult {

    private final Logger logger = Logger.getLogger(WSConsumerConsult.class);

    /*
     * Cliente del servicio web de consultas
     */
    private ISunatClient sunatClient;

    private IOSEClient oseClient;

    /* Archivo de configuracion */

    private final String docUUID;

    @Autowired
    ApplicationProperties applicationProperties;


    /**
     * Constructor privado para evitar instancias.
     *
     * @param docUUID UUID identificador del documento.
     */
    private WSConsumerConsult(String docUUID) {
        this.docUUID = docUUID;
    } //WSConsumerConsult

    /**
     * Este metodo crea una nueva instancia de la clase WSConsumerConsult.
     *
     * @param docUUID UUID identificador del documento.
     * @return Retorna una nueva instancia de la clase WSConsumerConsult.
     */
    public static synchronized WSConsumerConsult newInstance(String docUUID) {
        return new WSConsumerConsult(docUUID);
    } //newInstance

    /**
     * Este metodo define la configuracion del objeto WS Consumidor de consultas
     * segun las configuraciones del config.xml
     *
     * @param senderRuc        Numero RUC del emisor electronico.
     * @param usuarioSec       Usuario SOL secundario del emisor electronico.
     * @param passwordSec      Clave SOL secundaria del emisor electronico.
     * @param configuracion Archivo de configuracion convertido en objeto.
     * @throws ConfigurationException
     */
    public void setConfiguration(String senderRuc, String usuarioSec, String passwordSec, ConfigData configuracion)
            throws ConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+setConfiguration() [" + this.docUUID + "]");
        }

        if (null == configuracion) {
            logger.error("setConfiguration() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_464.getMessage());
            throw new ConfigurationException(IVenturaError.ERROR_464.getMessage());
        }

        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            Consumer consumer = new Consumer(usuarioSec, passwordSec, senderRuc);

            sunatClient = SunatClientFactory.getInstance().getSunatClient(configuracion.getAmbiente());
            sunatClient.setConsumer(consumer);
            sunatClient.printSOAP(true);

        }
        if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {

            String claveSol = passwordSec;
            Consumer consumer = new Consumer(usuarioSec, claveSol);

            oseClient = new OSEClient(configuracion.getAmbiente());
            oseClient.setConsumer(consumer);
            oseClient.printSOAP(true);

        }

    }

    /**
     * Este metodo define la configuracion del objeto WS Consumidor de consultas segun las
     * configuraciones del archivo config.xml
     * <p>
     * senderRuc        Numero RUC del emisor electronico.
     * usuarioSec       Usuario SOL secundario del emisor electronico.
     * passwordSec      Clave SOL secundaria del emisor electronico.
     *
     * @throws ConfigurationException
     */
    public void setConfiguration(ConfigData configuracion, String rucEmisor)
            throws ConfigurationException {
        //this.configuracion = configuracionObj;
        if (logger.isDebugEnabled()) {
            logger.debug("+setConfiguration() [" + this.docUUID + "]");
        }

        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            Consumer consumer = new Consumer(configuracion.getUsuarioSol(), configuracion.getClaveSol(), rucEmisor);

            sunatClient = SunatClientFactory.getInstance().getSunatClient(configuracion.getAmbiente());
            sunatClient.setConsumer(consumer);
            sunatClient.printSOAP(Boolean.parseBoolean("true"));

        }
        if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {

            Consumer consumer = new Consumer(configuracion.getUsuarioSol(), configuracion.getClaveSol());
           oseClient = new OSEClient(configuracion.getAmbiente());
            oseClient.setConsumer(consumer);
            oseClient.printSOAP(Boolean.parseBoolean("true"));
        }
    }

    public StatusResponse getStatusCDR(String documentRUC, String documentType, String documentSerie, Integer documentNumber, ConfigData configuracion) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStatusCDR() [" + this.docUUID + "]");
        }
        StatusResponse statusResponse = null;
        try {
            if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
                StatusResponse statusResponseSUNAT = sunatClient.getStatusCDR(documentRUC, documentType, documentSerie, documentNumber);
                statusResponse = getStatusResponse(statusResponseSUNAT, configuracion.getIntegracionWs());
            }
            if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {
                CdrStatusResponse statusResponseOSE = oseClient.getStatusCDR(documentRUC, documentType, documentSerie, documentNumber);
                statusResponse = getStatusResponse(statusResponseOSE, configuracion.getIntegracionWs());
            }

            if (logger.isInfoEnabled()) {
                logger.info("getStatusCDR() [" + this.docUUID + "] Se obtuvo respuesta: " + statusResponse);
            }
        } catch (SOAPFaultException e) {
            String sErrorCodeSUNAT = null;
            if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
                sErrorCodeSUNAT = e.getFault().getFaultCode() + " - " + e.getFault().getFaultString();
            } else if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {
                sErrorCodeSUNAT = e.getFault().getFaultString();
            }

            Detail detail = e.getFault().getDetail();
            if (null != detail) {
                logger.error("sendSummary() [" + this.docUUID + "] SOAPFaultException - FaultDetail: " + detail.getTextContent());
                LoggerTrans.getCDThreadLogger().log(Level.SEVERE, "[{0}]: Excepci\u00F3n de SUNAT FaultDetail: \"{1}\"", new Object[]{this.docUUID, detail.getTextContent()});
            }

            throw new SoapFaultException(sErrorCodeSUNAT);
        } catch (WebServiceException e) {
            logger.error("getStatusCDR() [" + this.docUUID + "] WebServiceException - ERROR: " + e.getMessage());
            LoggerTrans.getCDThreadLogger().log(Level.SEVERE, "[{0}]: Excepci\\u00F3n de tipo WebServiceException : {1}", new Object[]{this.docUUID, e.getMessage()});

            throw new ConectionSunatException(IVenturaError.ERROR_154);
        } catch (Exception e) {
            logger.error("getStatusCDR() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("getStatusCDR() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            LoggerTrans.getCDThreadLogger().log(Level.SEVERE, "[{0}]: Excepci\\u00F3n de tipo Gen\\u00E9rica: {1}", new Object[]{this.docUUID, e.getMessage()});
            throw new ConectionSunatException(IVenturaError.ERROR_155);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getStatus() [" + this.docUUID + "]");
        }
        return statusResponse;
    }

    private StatusResponse getStatusResponse(Object object, String integracionWS) {
        if (object instanceof StatusResponse && integracionWS.contains("SUNAT")) {
            return new StatusResponse(((StatusResponse) object).getStatusCode(),
                    ((StatusResponse) object).getStatusMessage(), ((StatusResponse) object).getContent());
        }
        if (object instanceof CdrStatusResponse) {
            return new StatusResponse(((CdrStatusResponse) object).getStatusCode(),
                    ((CdrStatusResponse) object).getStatusMessage(), ((CdrStatusResponse) object).getContent());
        }

        return null;
    }


}
