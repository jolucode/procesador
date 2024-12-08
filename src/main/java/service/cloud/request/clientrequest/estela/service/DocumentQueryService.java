package service.cloud.request.clientrequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientrequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientrequest.estela.proxy.ServiceProxy;

@Service
public class DocumentQueryService {

    private final ServiceProxy serviceClient;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentQueryService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        return serviceClient.sendSoapRequest(url, soapRequestBuilder.buildConsultaSoapRequest(soapRequest))
                .map(response -> new FileResponseDTO("Success", "File processed successfully"))
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Mono.just(new FileResponseDTO("Error", errorMessage));
                });
    }
}

