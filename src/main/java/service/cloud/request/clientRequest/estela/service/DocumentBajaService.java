package service.cloud.request.clientRequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.proxy.ServiceProxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentBajaService {

    private final ServiceProxy serviceClient;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentBajaService(ServiceProxy serviceClient, DocumentBuilder soapRequestBuilder) {
        this.serviceClient = serviceClient;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processBajaRequest(String url, FileRequestDTO soapRequest) {
        return serviceClient.sendSoapRequest(url, soapRequestBuilder.buildEmisionBajaSoapRequest(soapRequest))
                .flatMap(this::handleBajaResponse)
                .onErrorResume(error -> {

                    String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
                    return Mono.just(new FileResponseDTO("Error", errorMessage, null, null));
                });
    }

    private Mono<FileResponseDTO> handleBajaResponse(String soapResponse) {
        if (soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")) {
            return Mono.error(new RuntimeException("Error en SUNAT: " + soapResponse));
        }

        String ticket = extractTicketFromResponse(soapResponse);
        return Mono.just(new FileResponseDTO("Success", "Ticket retrieved successfully", null, ticket));
    }

    private String extractTicketFromResponse(String soapResponse) {
        Pattern pattern = Pattern.compile("<ticket>(.*?)</ticket>");
        Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("No se encontró <ticket> en la respuesta SOAP.");
    }
}

