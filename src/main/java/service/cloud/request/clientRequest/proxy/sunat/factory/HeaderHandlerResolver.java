package service.cloud.request.clientRequest.proxy.sunat.factory;

import service.cloud.request.clientRequest.proxy.consumer.Consumer;
import service.cloud.request.clientRequest.proxy.sunat.security.soap.SecuritySOAPHandler;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Esta clase implementa los metodos de la interfaz HandlerResolver, estableciendo en su
 * metodo 'getHandlerChain' la funcionalidad para agregar la seguridad (WS Security) de
 * cabecera en el mensaje SOAP de salida.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class HeaderHandlerResolver implements HandlerResolver {

    private Consumer consumer;

    private boolean printOption;


    /**
     * Constructor para la clase HeaderHandlerResolver.
     *
     * @param consumer El objeto consumidor que contiene los datos del emisor electronico.
     */
    public HeaderHandlerResolver(Consumer consumer) {
        this.consumer = consumer;
    } //HeaderHandlerResolver


    /**
     * Este metodo habilita o desabilita la opcion de mostrar la trama
     * SOAP (Consulta y respuesta).
     *
     * @param printOption Valor para habilitar (true) o desabilitar (false) la opcion de
     *                    mostrar la trama SOAP.
     */
    public void setPrintSOAP(boolean printOption) {
        this.printOption = printOption;
    } //setPrintSOAP

    /**
     * Este metodo agrega la seguridad al mensaje SOAP de salida.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {
        List<Handler> handlerChain = new ArrayList<Handler>();

        SecuritySOAPHandler securitySOAP = new SecuritySOAPHandler(consumer, printOption);
        handlerChain.add(securitySOAP);

        return handlerChain;
    } //getHandlerChain

} //HeaderHandlerResolver
