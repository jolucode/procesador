package service.cloud.request.clientRequest.service.publicar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.entity.PublicardocWs;
import service.cloud.request.clientRequest.prueba.Client;

import java.util.Optional;

@Service
public class PublicacionManager {

    Logger logger = LoggerFactory.getLogger(PublicacionManager.class);
    @Autowired
    private DocumentService documentService;
    @Autowired
    ClientProperties clientProperties;

    public void publicarDocumento(String documentoEmpresa, String feId, TransaccionRespuesta transaccionRespuesta) {
        try {
            Client client = clientProperties.listaClientesOf(documentoEmpresa);
            Optional<PublicardocWs> optionalPublicardocWs = documentService.obtenerDocumentoPorId(feId);

            if (optionalPublicardocWs.isPresent()) {
                PublicardocWs publicardocWs = optionalPublicardocWs.get();
                DocumentoPublicado documentoPublicado = new DocumentoPublicado(client, publicardocWs, transaccionRespuesta);
                realizarPublicacion(client, documentoPublicado);
            } else {
                System.err.println("El documento con ID " + feId + " no fue encontrado.");
            }
        } catch (Exception e) {
            System.err.println("Error durante la publicación: " + e.getMessage());
        }
    }

    private void realizarPublicacion(Client client, DocumentoPublicado documentoPublicado) {
        String respuesta = publicarDocumentoWs(client.getWsLocation(), documentoPublicado)
                .doOnSuccess(response -> logger.info("Publicado!!"))
                .doOnError(error -> logError("Error en la publicación", error))
                .block();

        if (respuesta.contains("correctamente")) {
            logger.info("Publicación exitosa: " + respuesta);
            logger.info("Publicado en: " + client.getWsLocation());
        }
    }

    private void logError(String message, Throwable error) {
        System.err.println(message + ": " + error.getMessage());
    }

    public Mono<String> publicarDocumentoWs(String apiUrl, DocumentoPublicado documentoPublicado) {

        WebClient webClient = WebClient.create(apiUrl);
        return webClient.post()
                .uri("/api/publicar")
                .body(BodyInserters.fromValue(documentoPublicado))
                .retrieve()
                .bodyToMono(String.class);
    }
}