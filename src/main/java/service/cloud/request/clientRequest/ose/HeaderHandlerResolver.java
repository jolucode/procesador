package service.cloud.request.clientRequest.ose;


import service.cloud.request.clientRequest.ose.model.Consumer;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

public class HeaderHandlerResolver implements HandlerResolver {

    private Consumer consumer;

    public HeaderHandlerResolver(Consumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        List<Handler> handlerChain = new ArrayList<Handler>();

        SecuritySOAPHandler securitySOAP = new SecuritySOAPHandler(consumer);
        handlerChain.add(securitySOAP);

        return handlerChain;
    }
}
