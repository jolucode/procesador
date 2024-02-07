package service.cloud.request.clientRequest.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.ose.IOSEClient;
import service.cloud.request.clientRequest.proxy.ose.OSEClient;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;
import service.cloud.request.clientRequest.proxy.sunat.factory.SunatClientFactory;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.exception.ConectionSunatException;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import javax.activation.DataHandler;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Esta clase contiene los metodos para enviar los documentos UBL firmados al
 * servicio web de Sunat.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com) implements
 * ISunatClient
 */
public class WSConsumerNew {

    private final Logger logger = Logger.getLogger(WSConsumerNew.class);

    public static int IErrorCode = 0;

    /*
     * Cliente del servicio web
     */
    private ISunatClient sunatClient;

    private IOSEClient oseClient;

    /*
     * Objeto HANDLER para manipular los
     * documentos UBL.
     */
    private FileHandler fileHandler;

    /* Archivo de configuracion */

    private final String docUUID;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param docUUID UUID identificador del documento.
     */
    private WSConsumerNew(String docUUID) {
        this.docUUID = docUUID;
    } //WSConsumer

    /**
     * Este metodo crea una nueva instancia de la clase WSConsumer.
     *
     * @param docUUID UUID identificador del documento.
     * @return Retorna una nueva instancia de la clase WSConsumer.
     */
    public static synchronized WSConsumerNew newInstance(String docUUID) {
        return new WSConsumerNew(docUUID);
    } //newInstance

    /**
     * Este metodo define la configuracion del objeto WS Consumidor segun las configuraciones del
     * archivo config.xml
     *
     * @param senderRuc    Numero RUC del emisos electronico.
     * @param usuarioSec   Usuario SOL secundario del emisor electronico.
     * @param passwordSec  Clave SOL secundaria del emisor electronico.
     * @param fileHandler  Objeto FileHandler que se encarga de manipular el documento UBL.
     * @param documentType Tipo de documento a enviar.
     * @throws ConfigurationException
     */
    public void setConfiguration(String senderRuc, String usuarioSec, String passwordSec, ConfigData configuracion, FileHandler fileHandler, String documentType) throws ConfigurationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+setConfiguration() [" + this.docUUID + "]");
        }

        if (null == configuracion) {
            logger.error("setConfiguration() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_456.getMessage());
            throw new ConfigurationException(IVenturaError.ERROR_456.getMessage());
        }


        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            Consumer consumer = new Consumer(usuarioSec, passwordSec, senderRuc);

            sunatClient = SunatClientFactory.getInstance().getSunatClient(configuracion.getAmbiente(), documentType);
            sunatClient.setConsumer(consumer);
            sunatClient.printSOAP(Boolean.parseBoolean(configuracion.getMostrarSoap()));

            if (logger.isInfoEnabled()) {
                logger.info("setConfiguration() [" + this.docUUID + "]\n" + "######################### CONFIGURACION SUNAT #########################\n" + "# Usuario SOL: " + consumer.getUsername() + "\tClave SOL: " + (null != consumer.getPassword()) + "\n" + "# Cliente WS: " + configuracion.getAmbiente() + "\n" + "# Mostrar SOAP: " + configuracion.getMostrarSoap() + "\n" + "#######################################################################");
            }
        } else if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {
            String claveSol = passwordSec;
            Consumer consumer = new Consumer(usuarioSec, claveSol);

            oseClient = new OSEClient(configuracion.getAmbiente());
            oseClient.setConsumer(consumer);
            oseClient.printSOAP(Boolean.parseBoolean(configuracion.getMostrarSoap()));

            if (logger.isInfoEnabled()) {
                logger.info("setConfiguration() [" + this.docUUID + "]\n" + "######################### CONFIGURACION OSE #########################\n" + "# Usuario SOL: " + consumer.getUsername() + "\tClave SOL: " + (null != consumer.getPassword()) + "\n" + "# Cliente WS: " + configuracion.getAmbiente() + "\n" + "# Mostrar SOAP: " + configuracion.getMostrarSoap() + "\n" + "#######################################################################");
            }
        }

        /* Archivo de configuracion */
        //this.configuracion = configuracionObj;

        /* Agregando el objeto FileHandler */
        //this.fileHandler = fileHandler;
        if (logger.isDebugEnabled()) {
            logger.debug("-setConfiguration() [" + this.docUUID + "]");
        }
    } //setConfiguration

    public Response sendBill(DataHandler zipDocument, String documentName, ConfigData allClass) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() [" + this.docUUID + "]");
        }

        byte[] cdrResponse = null;

        try {
            if (allClass.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
                cdrResponse = sunatClient.sendBill(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);

            }
            if (allClass.getIntegracionWs().equalsIgnoreCase("OSE")) {
                cdrResponse = oseClient.sendBill(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("sendBill() [" + this.docUUID + "] Se obtuvo respuesta del WS.");
            }
        } catch (SOAPFaultException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            return Response.builder()
                    .errorCode(e.getMessage())
                    .errorMessage(exceptionProxy.getDescripcion())
                    .build();
        }
        return Response.builder()
                .response(cdrResponse)
                .build();
    }


    public String sendSummary(DataHandler zipDocument, String documentName, ConfigData configuracion) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+sendSummary() [" + this.docUUID + "]");
        }

        validarConnectionInternet();

        String ticket = null;
        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            ticket = sunatClient.sendSummary(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
        }
        if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {
            ticket = oseClient.sendSummary(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-sendSummary() [" + this.docUUID + "]");
        }
        return ticket;
    } //sendSummary

    private void validarConnectionInternet() throws ConectionSunatException {
        try {
            LoggerTrans.getCDThreadLogger().log(Level.INFO, "Verificando conexion a internet...");

            URL url = new URL("http://www.google.com");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();

            LoggerTrans.getCDThreadLogger().log(Level.INFO, "Conexion exitosa a internet");
        } catch (MalformedURLException ex) {
            throw new ConectionSunatException(IVenturaError.ERROR_5000);
        } catch (IOException ex) {
            throw new ConectionSunatException(IVenturaError.ERROR_5000);
        }
    }


} //WSConsumer
