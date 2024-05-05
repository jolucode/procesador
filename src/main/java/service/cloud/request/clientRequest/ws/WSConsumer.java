package service.cloud.request.clientRequest.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.ose.IOSEClient;
import service.cloud.request.clientRequest.proxy.ose.OSEClient;
import service.cloud.request.clientRequest.proxy.ose.object.StatusResponse;
import service.cloud.request.clientRequest.proxy.sunat.factory.ISunatClient;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.logging.FileHandler;

/**
 * Esta clase contiene los metodos para enviar los documentos UBL firmados al
 * servicio web de Sunat.
 */
public class WSConsumer {

    private final Logger logger = Logger.getLogger(WSConsumer.class);

    private ISunatClient sunatClient;

    private IOSEClient oseClient;


    private FileHandler fileHandler;

    private String docUUID;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param docUUID UUID identificador del documento.
     */
    private WSConsumer(String docUUID) {
        this.docUUID = docUUID;
    } //WSConsumer

    /**
     * Este metodo crea una nueva instancia de la clase WSConsumer.
     *
     * @param docUUID UUID identificador del documento.
     * @return Retorna una nueva instancia de la clase WSConsumer.
     */
    public static synchronized WSConsumer newInstance(String docUUID) {
        return new WSConsumer(docUUID);
    } //newInstance

    /**
     * Este metodo define la configuracion del objeto WS Consumidor segun las
     * configuraciones del archivo config.xml
     *
     * @param senderRuc     Numero RUC del emisos electronico.
     * @param usuarioSec    Usuario SOL secundario del emisor electronico.
     * @param passwordSec   Clave SOL secundaria del emisor electronico.
     * @param configuracion Archivo de configuracion convertido en objeto.
     */
    public void setConfiguration(String senderRuc, String usuarioSec, String passwordSec, ConfigData configuracion) {

        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            Consumer consumer = new Consumer(usuarioSec, passwordSec, senderRuc);

            //sunatClient = SunatClientFactory.getInstance().getSunatClient(configuracion.getAmbiente());
            sunatClient.setConsumer(consumer);
            //sunatClient.printSOAP(Boolean.parseBoolean(true);

        }
        if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {

            String claveSol = passwordSec;
            Consumer consumer = new Consumer(usuarioSec, claveSol);

            //oseClient = new OSEClient(configuracionObj.getSunat().getClienteSunat());

            oseClient = new OSEClient(configuracion.getAmbiente());
            oseClient.setConsumer(consumer);
            oseClient.printSOAP(true);

        }

    }


    public StatusResponse getStatus(String ticket, ConfigData configuracion) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStatus() [" + this.docUUID + "]");
        }
        StatusResponse statusResponse = new StatusResponse();
        if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
            //pe.gob.sunat.service.StatusResponse statusResponseSUNAT = sunatClient.getStatus(ticket);
            //statusResponse = getStatusResponse(statusResponseSUNAT);
        }
        if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {

            StatusResponse statusResponseCONOSE = oseClient.getStatus(ticket);
            statusResponse = getStatusResponse(statusResponseCONOSE);
        }

        return statusResponse;
    } //getStatus


    private StatusResponse getStatusResponse(Object object) {
        /*if (object instanceof pe.gob.sunat.service.StatusResponse) {
            return new StatusResponse(((pe.gob.sunat.service.StatusResponse) object).getStatusCode(), ((pe.gob.sunat.service.StatusResponse) object).getContent());
        }*/
        if (object instanceof StatusResponse) {
            return new StatusResponse(((StatusResponse) object).getStatusCode(), ((StatusResponse) object).getContent());
        }
        return null;
    }

} //WSConsumer
