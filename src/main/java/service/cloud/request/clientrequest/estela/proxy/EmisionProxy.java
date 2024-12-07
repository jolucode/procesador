package service.cloud.request.clientrequest.estela.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EmisionProxy implements InterfaceEmisionProxy {

    private final WebClient webClient;

    @Autowired
    public EmisionProxy(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<String> sendFileToExternalService(String url, String soapRequest) {
        return webClient.post()
                .uri(url)
                .bodyValue(soapRequest)
                .retrieve()
                // Manejar errores basados en el código HTTP OSE
                .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(body -> {
                                            if (body.contains("<SOAP-ENV:Fault") || body.contains("s:Fault")) {
                                                // Extraer el mensaje de error
                                                String errorMessage = extractErrorMessage(body);
                                                return Mono.error(new RuntimeException(errorMessage));
                                            }
                                            return Mono.error(new RuntimeException(response.statusCode() + " - " + body));
                                        })
                )
                .bodyToMono(String.class)
                // Manejar errores dentro del cuerpo cuando el código HTTP es 200
                .flatMap(responseBody -> {
                    if (responseBody.contains("<soap-env:Fault")) {
                        String errorMessage = extractErrorMessage(responseBody);
                        return Mono.error(new RuntimeException("Error en SUNAT: " + errorMessage));
                    }
                    return Mono.just(responseBody);
                });
    }

    private String extractErrorMessage(String xmlResponse) {
        try {
            org.jsoup.nodes.Document document = org.jsoup.Jsoup.parse(xmlResponse, "", org.jsoup.parser.Parser.xmlParser());
            // Intentar extraer el texto de <faultstring>

            // Si no hay <faultstring>, intentar extraer <message>
            String message = document.select("message").text();
            if (!message.isEmpty()) {
                // Reemplazar tanto \ como / en el mensaje
                message = message.replace("\\", "").replace("/", "");
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

