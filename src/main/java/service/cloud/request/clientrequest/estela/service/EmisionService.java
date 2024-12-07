package service.cloud.request.clientrequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientrequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientrequest.estela.proxy.EmisionProxy;

@Service
public class EmisionService {

    private final EmisionProxy fileProxy;

    private final DocumentBuilder fileBuilder;

    @Autowired
    public EmisionService(EmisionProxy fileProxy, DocumentBuilder fileBuilder) {
        this.fileProxy = fileProxy;
        this.fileBuilder = fileBuilder;
    }

    public Mono<FileResponseDTO> processAndSaveFile(String url, FileRequestDTO soapRequest) {
        return fileProxy.sendFileToExternalService(url, fileBuilder.emisionBuildSoapRequest(soapRequest))
                .map(response -> new FileResponseDTO("Success", "File processed successfully"))
                .onErrorResume(error -> {
                    // Captura el error lanzado desde el proxy y construye el DTO
                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Mono.just(new FileResponseDTO("Error", errorMessage));
                });
    }

}

