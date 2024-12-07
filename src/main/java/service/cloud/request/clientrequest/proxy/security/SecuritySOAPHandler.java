package service.cloud.request.clientrequest.proxy.security;

import org.apache.log4j.Logger;
import service.cloud.request.clientrequest.proxy.model.Consumer;
import service.cloud.request.clientrequest.utils.IWSSecurityConfig;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class SecuritySOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private final Logger logger = Logger.getLogger(SecuritySOAPHandler.class);

    private final Consumer consumer;

    private boolean printOption;

    public SecuritySOAPHandler(Consumer consumer) {
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
            try {
                SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                message.writeTo(out);
                String soapString = new String(out.toByteArray(), StandardCharsets.UTF_8);
                //System.out.println("Trama SOAP enviada: " + soapString);
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

                if (true) {
                    //message = context.getMessage();
                    ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                    context.getMessage().writeTo(out2);
                    String soapString2 = new String(out2.toByteArray(), StandardCharsets.UTF_8);
                    System.out.println("Trama SOAP enviada: " + soapString2);
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
            }
        } else {
            try {
                if (printOption) {
                    SOAPMessage message = context.getMessage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String soapTrama = baos.toString(StandardCharsets.UTF_8);
                    logger.info("Trama SOAP generada: " + soapTrama); // Registrar en logs
                    message.writeTo(baos);
                }
            } catch (Exception e) {
                logger.error("handleMessage() ERROR: " + e.getMessage());
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
