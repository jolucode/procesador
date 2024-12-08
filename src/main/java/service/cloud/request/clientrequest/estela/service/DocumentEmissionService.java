package service.cloud.request.clientrequest.estela.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.estela.builder.DocumentBuilder;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientrequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientrequest.estela.proxy.ServiceProxy;

@Service
public class DocumentEmissionService {

    private final ServiceProxy serviceProxy;

    private final DocumentBuilder soapRequestBuilder;

    @Autowired
    public DocumentEmissionService(ServiceProxy serviceProxy, DocumentBuilder soapRequestBuilder) {
        this.serviceProxy = serviceProxy;
        this.soapRequestBuilder = soapRequestBuilder;
    }

    public Mono<FileResponseDTO> processDocumentEmission(String url, FileRequestDTO soapRequest) {
        return serviceProxy.sendSoapRequest(url, soapRequestBuilder.buildEmissionSoapRequest(soapRequest))
                .flatMap(this::handleSoapResponse)
                .onErrorResume(error -> {
                    String errorMessage = extractErrorMessage(error.getMessage());
                    String cleanErrorMessage = errorMessage.replace("\\", "").replace("\"", "'");
                    return Mono.just(new FileResponseDTO("Error", cleanErrorMessage));
                });
    }

    private Mono<FileResponseDTO> handleSoapResponse(String soapResponse) {
        if (soapResponse.contains("<soap-env:Fault") || soapResponse.contains("<faultstring xml:lang=\"es-PE\">")) {
            return Mono.error(new RuntimeException("Error en SUNAT: " + soapResponse));
        }
        return Mono.just(new FileResponseDTO("Success", "File processed successfully"));
    }

    private String extractErrorMessage(String xmlResponse) {
        try {

            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(xmlResponse, "", org.jsoup.parser.Parser.xmlParser());

            String message = document.select("message").text();
            if (!message.isEmpty()) {
                return message;
            }

            String faultString = document.select("faultstring").text();
            if (!faultString.isEmpty()) {
                return faultString;
            }
            return "Error no especificado en la respuesta";

        } catch (Exception e) {
            return "Error no especificado";
        }
    }
}

