package service.cloud.request.clientRequest.proxy.sunat.production.consult;




import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

public class HeaderHandlerResolver implements HandlerResolver {

    private List<Handler> handlers = new ArrayList<>();

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        return handlers;
    }

}
