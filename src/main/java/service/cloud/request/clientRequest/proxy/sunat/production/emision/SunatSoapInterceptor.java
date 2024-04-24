package service.cloud.request.clientRequest.proxy.sunat.production.emision;


import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.config.ISunatConfig;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

public class SunatSoapInterceptor implements SOAPHandler<SOAPMessageContext> {

    private final Consumer consumer;

    public SunatSoapInterceptor(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outboundProperty = (Boolean) context.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            SOAPMessage message = context.getMessage();
            if (outboundProperty) {
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
                message.saveChanges();
                File file = new File("xmlEnvio.xml");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                message.writeTo(fileOutputStream);
            } else {
                try {
                    File file = new File("xmlEnvio.xml");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    message.writeTo(fileOutputStream);
                    System.out.println("");
                } catch (SOAPException | IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SOAPException | IOException e) {
            e.printStackTrace();
        }
        return outboundProperty;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(javax.xml.ws.handler.MessageContext context) {

    }
}
