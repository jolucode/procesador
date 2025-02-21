package service.cloud.request.clientRequest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.service.extractor.CloudService;

@RestController()
public class CloudController {

    @Autowired
    private CloudService cloudService;

    @PostMapping("/procesar")
    public Mono<ResponseEntity<Object>> processDocument(@RequestBody String stringRequestOnpremise) {
        return cloudService.proccessDocument(stringRequestOnpremise);
    }

    /*@PostMapping("/procesar")
    public Mono<ResponseEntity<Object>> processDocument(@RequestBody String stringRequestOnpremise) {
        return repo.findAll()
                .take(100)
                .flatMap(log -> {
                    String jsonRequest = log.getRequest();
                    // Modificar los valores en el JSON
                    String modifiedJsonRequest = modifyJsonFields(jsonRequest);
                    return cloudService.proccessDocument(modifiedJsonRequest); // Procesar cada log
                })
                .concatWith(cloudService.proccessDocument(stringRequestOnpremise)) // Añadir el request recibido
                .collectList() // Combinar los resultados de las llamadas
                .map(responseList -> ResponseEntity.ok((Object) responseList)) // Retornar todos los resultados
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body((Object) "Error processing request")));
    }

    private String modifyJsonFields(String jsonRequest) {
        try {
            // Convertir el JSON a un JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonRequest);

            // Si es un array, manejarlo como ArrayNode
            if (rootNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode node : arrayNode) {
                    if (node.isObject()) {
                        ObjectNode objectNode = (ObjectNode) node;

                        // Modificar los valores de los campos
                        if (objectNode.has("SN_EMail")) {
                            objectNode.put("SN_EMail", "lbecerra@vsperu.com");
                        }
                        if (objectNode.has("EMail")) {
                            objectNode.put("EMail", "lbecerra@vsperu.com");
                        }
                        if (objectNode.has("DocIdentidad_Nro")) {
                            objectNode.put("DocIdentidad_Nro", "20510910517");
                        }
                        if (objectNode.has("ANTICIPO_Nro_Doc_ID")) {
                            objectNode.put("ANTICIPO_Nro_Doc_ID", "20510910517");
                        }
                    }
                }
            } else if (rootNode.isObject()) {
                // Si es un objeto JSON, manejarlo como ObjectNode
                ObjectNode objectNode = (ObjectNode) rootNode;

                // Modificar los valores de los campos
                if (objectNode.has("SN_EMail")) {
                    objectNode.put("SN_EMail", "lbecerra@vsperu.com");
                }
                if (objectNode.has("EMail")) {
                    objectNode.put("EMail", "lbecerra@vsperu.com");
                }
                if (objectNode.has("DocIdentidad_Nro")) {
                    objectNode.put("DocIdentidad_Nro", "20510910517");
                }
                if (objectNode.has("ANTICIPO_Nro_Doc_ID")) {
                    objectNode.put("ANTICIPO_Nro_Doc_ID", "20510910517");
                }
            }

            // Convertir de nuevo el JsonNode a String
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Error modifying JSON fields", e);
        }
    }*/

}
