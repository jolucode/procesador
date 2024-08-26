package service.cloud.request.clientRequest.ose;


import service.cloud.request.clientRequest.ose.model.Consumer;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

public class HeaderHandlerResolver implements HandlerResolver {

    private Consumer consumer;

    private boolean printOption;

    public HeaderHandlerResolver(Consumer consumer) {
        this.consumer = consumer;
    }

    public void setPrintSOAP(boolean printOption) {
        this.printOption = printOption;
    } //setPrint

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        List<Handler> handlerChain = new ArrayList<Handler>();

        SecuritySOAPHandler securitySOAP = new SecuritySOAPHandler(consumer, printOption);
        handlerChain.add(securitySOAP);

        return handlerChain;
    }
}
