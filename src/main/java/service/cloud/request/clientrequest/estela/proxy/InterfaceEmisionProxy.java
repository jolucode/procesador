package service.cloud.request.clientrequest.estela.proxy;

import reactor.core.publisher.Mono;

public interface InterfaceEmisionProxy {
    Mono<String> sendFileToExternalService(String url, String soapRequest);
}
