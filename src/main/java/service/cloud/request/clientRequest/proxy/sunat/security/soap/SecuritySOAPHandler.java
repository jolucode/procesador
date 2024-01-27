package service.cloud.request.clientRequest.proxy.sunat.security.soap;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.sunat.config.ISunatConfig;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.util.Set;


/**
 * Esta clase implementa los metodos de la interfaz SOAPHandler, agregando en su
 * metodo 'handleMessage' la seguridad (WS Security) de cabecera establecida por
 * la Sunat en el mensaje SOAP de salida.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class SecuritySOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger logger = Logger.getLogger(SecuritySOAPHandler.class);

    private final Consumer consumer;

    private final boolean printOption;


    /**
     * Constructor para la clase SecuritySOAPHandler.
     *
     * @param consumer    El objeto consumidor que contiene los datos del emisor electronico.
     * @param printOption Valor para habilitar (true) o desabilitar (false) la opcion de
     *                    mostrar la trama SOAP.
     */
    public SecuritySOAPHandler(Consumer consumer, boolean printOption) {
        this.consumer = consumer;
        this.printOption = true;
    } //SecuritySOAPHandler


    /**
     * Este metodo agrega la cabecera con la seguridad establecida por la Sunat
     * al mensaje SOAP de salida. Y muestra el mensaje SOAP de salida y respuesta
     * segun el valor del parametro 'printOption'.
     */
    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        logger.info("+handleMessage()");
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty) {
            SOAPMessage message = context.getMessage();
            logger.info("handleMessage() message: " + message);

            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                if (envelope.getHeader() != null) {
                    envelope.getHeader().detachNode();
                }
                SOAPHeader header = envelope.addHeader();


                SOAPElement security = header.addChildElement(ISunatConfig.WS_SECURITY_HEADER_PARENT_NAME, ISunatConfig.WS_SECURITY_HEADER_PARENT_BASE_PFX, ISunatConfig.WS_SECURITY_HEADER_PARENT_VALUE);

                SOAPElement usernameToken = security.addChildElement(ISunatConfig.WS_SECURITY_SUB_UNTOKEN_NAME, ISunatConfig.WS_SECURITY_HEADER_PARENT_BASE_PFX);
                usernameToken.addAttribute(new QName(ISunatConfig.WS_SECURITY_SUB_UNTOKEN_PFX), ISunatConfig.WS_SECURITY_SUB_UNTOKEN_VALUE);

                SOAPElement username = usernameToken.addChildElement(ISunatConfig.WS_SECURITY_PARAM_USERNAME, ISunatConfig.WS_SECURITY_HEADER_PARENT_BASE_PFX);
                username.addTextNode(this.consumer.getRuc() + this.consumer.getUsername());

                SOAPElement password = usernameToken.addChildElement(ISunatConfig.WS_SECURITY_PARAM_PASSWORD, ISunatConfig.WS_SECURITY_HEADER_PARENT_BASE_PFX);
                password.setAttribute(ISunatConfig.WS_SECURITY_COMMON_ATTRIBUTE_TYPE, ISunatConfig.WS_SECURITY_COMMON_ATRIBUTE_PWD_VALUE);
                password.addTextNode(this.consumer.getPassword());
                //message.writeTo(System.out);
                System.out.println();
                if (printOption) {
                    /* Imprime el mensaje SOAP de salida en System.out */
                    //message.writeTo(System.out);
                    System.out.println();

                    /* Imprime el mensaje SOAP de salida en LOG4J */
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    message.writeTo(baos);
                    if (logger.isInfoEnabled()) {
                        logger.info("handleMessage() [REQUEST] Mensaje SOAP: \n" + baos);
                    }
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
                logger.error("handleMessage() Exception -->" + ExceptionUtils.getStackTrace(e));
            }
        } else {
            try {
                if (printOption) {
                    /* Imprime el mensaje SOAP de respuesta en System.out */
                    SOAPMessage message = context.getMessage();
                    //message.writeTo(System.out);
                    //System.out.println();

                    /* Imprime el mensaje SOAP de respuesta en LOG4J */
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    message.writeTo(baos);

                    if (logger.isInfoEnabled()) {
                        logger.info("handleMessage() [RESPONSE] Mensaje SOAP: \n" + baos);
                    }
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
                logger.error("handleMessage() Exception -->" + ExceptionUtils.getStackTrace(e));
            }
        }

        return outboundProperty;
    } //handleMessage

    /**
     * Este metodo no se implementa para esta clase.
     */
    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    } //handleFault

    /**
     * Este metodo no se implementa para esta clase.
     */
    @Override
    public void close(MessageContext context) {

    } //close

    /**
     * Este metodo no se implementa para esta clase.
     */
    @Override
    public Set<QName> getHeaders() {
        return null;
    } //getHeaders

} //SecuritySOAPHandler
