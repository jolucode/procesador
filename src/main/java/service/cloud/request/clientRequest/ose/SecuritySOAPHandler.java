package service.cloud.request.clientRequest.ose;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.ose.model.Consumer;

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

public class SecuritySOAPHandler implements SOAPHandler<SOAPMessageContext>  {

    private final Logger logger = Logger.getLogger(SecuritySOAPHandler.class);

    private Consumer consumer;

    private boolean printOption;

    public SecuritySOAPHandler(Consumer consumer, boolean printOption) {
        this.consumer = consumer;
        this.printOption = printOption;
    }


    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if (logger.isInfoEnabled()) {
            logger.info("+handleMessage()");
        }
        Boolean outboundProperty = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty.booleanValue()) {
            SOAPMessage message = context.getMessage();
//            if (logger.isInfoEnabled()) {
//                logger.info("handleMessage() message: " + message);
//            }
            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                if (null != envelope.getHeader()) {
                    envelope.getHeader().detachNode();
                }

                SOAPHeader header = envelope.addHeader();

                SOAPElement security = header.addChildElement(IWSSecurityConfig.HEADER_PARENT_NAME,
                        IWSSecurityConfig.HEADER_PARENT_BASE_PFX, IWSSecurityConfig.HEADER_PARENT_VALUE);

                SOAPElement usernameToken = security.addChildElement(IWSSecurityConfig.SUB_UNTOKEN_NAME, IWSSecurityConfig.HEADER_PARENT_BASE_PFX);
                usernameToken.addAttribute(new QName(IWSSecurityConfig.SUB_UNTOKEN_PFX), IWSSecurityConfig.SUB_UNTOKEN_VALUE);

                SOAPElement username = usernameToken.addChildElement(IWSSecurityConfig.PARAM_USERNAME, IWSSecurityConfig.HEADER_PARENT_BASE_PFX);
                username.addTextNode(this.consumer.getUsername());

                SOAPElement password = usernameToken.addChildElement(IWSSecurityConfig.PARAM_PASSWORD, IWSSecurityConfig.HEADER_PARENT_BASE_PFX);
                password.setAttribute(IWSSecurityConfig.COMMON_ATTRIBUTE_TYPE, IWSSecurityConfig.COMMON_ATRIBUTE_PWD_VALUE);
                password.addTextNode(this.consumer.getPassword());
                context.getMessage().saveChanges();

                if (printOption) {
                    /* Imprime el mensaje SOAP Request en System.out */
                    //message.writeTo(System.out);
                    System.out.println("");

                    /* Imprime el mensaje SOAP Request en LOG4J */
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    message.writeTo(baos);
//                    if (logger.isInfoEnabled()) {
//                        logger.info("handleMessage() [REQUEST] Mensaje SOAP: \n" + baos.toString());
//                    }
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
                //logger.error("handleMessage() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            }
        } else {
            try {
                if (printOption) {
                    /* Imprime el mensaje SOAP Response en System.out */
                    SOAPMessage message = context.getMessage();
//                    message.writeTo(System.out);
                    /* Imprime el mensaje SOAP Response en LOG4J */
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    message.writeTo(baos);
//                    if (logger.isInfoEnabled()) {
//                        logger.info("handleMessage() [RESPONSE] Mensaje SOAP: \n" + baos.toString());
//                    }
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
                //logger.error("handleMessage() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            }
        }
        return outboundProperty;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    } //handleFault


    @Override
    public void close(MessageContext messageContext) {

    }
}
