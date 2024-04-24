package service.cloud.request.clientRequest.proxy.sunat.production.consult;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.config.ISunatConfig;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.util.Set;

public class SunatSoapConsultInterceptor implements SOAPHandler<SOAPMessageContext> {

    private static final String PREFERRED_PREFIX = "S";

    private static final String OTHER_PREFIX = "ns2";

    private final Consumer consumer;

    public SunatSoapConsultInterceptor(Consumer consumer) {
        this.consumer = consumer;
    }

    private void alterSoapEnvelope(SOAPMessage soapMessage) {
        try {
            SOAPPart soapPart = soapMessage.getSOAPPart();
            SOAPEnvelope envelope = soapPart.getEnvelope();
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
            envelope.removeNamespaceDeclaration(envelope.getPrefix());
//            envelope.addNamespaceDeclaration(PREFERRED_PREFIX, SOAP_ENV_NAMESPACE);
            SOAPBody body = soapMessage.getSOAPBody();
            SOAPFault fault = body.getFault();
            envelope.setPrefix(PREFERRED_PREFIX);
            header.setPrefix(PREFERRED_PREFIX);
            body.setPrefix(PREFERRED_PREFIX);
            NodeList childNodes = body.getChildNodes();
            if (childNodes.getLength() > 0) {
                Node node = childNodes.item(0);
                if (node.getLocalName().equalsIgnoreCase("sendBill")) {
                    int cantidad = node.getAttributes().getLength();
                    for (int i = cantidad - 1; i >= 0; i--) {
                        Node item = node.getAttributes().item(i);
                        node.getAttributes().removeNamedItem(item.getNodeName());
                    }
                    node.setPrefix(OTHER_PREFIX);
                }
            }
            if (fault != null) {
                fault.setPrefix(PREFERRED_PREFIX);
            }
            soapMessage.saveChanges();
            soapMessage.writeTo(System.out);
            System.out.println();
        } catch (SOAPException | IOException e) {
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage message = context.getMessage();
        Boolean outboundProperty = (Boolean) context.get(javax.xml.ws.handler.MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty) {
            alterSoapEnvelope(message);
        } else {
            try {
                message.writeTo(System.out);
                System.out.println("");
            } catch (SOAPException | IOException e) {
            }
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
