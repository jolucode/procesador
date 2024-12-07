package service.cloud.request.clientrequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientrequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientrequest.estela.proxy.EmisionProxy;

@Service
public class ConsultaFileService {

    private final EmisionProxy fileProxy;

    private final DocumentBuilder fileBuilder;

    @Autowired
    public ConsultaFileService(EmisionProxy fileProxy, DocumentBuilder fileBuilder) {
        this.fileProxy = fileProxy;
        this.fileBuilder = fileBuilder;
    }

    public Mono<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        return fileProxy.sendFileToExternalService(url, fileBuilder.consultaBuildSoapRequest(soapRequest))
                .map(response -> new FileResponseDTO("Success", "File processed successfully"))
                .onErrorResume(error -> {
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Mono.just(new FileResponseDTO("Error", errorMessage));
                });
    }
}

