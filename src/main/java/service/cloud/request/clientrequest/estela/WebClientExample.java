package service.cloud.request.clientrequest.estela;

import org.springframework.web.reactive.function.client.WebClient;

public class WebClientExample {
    public static void main(String[] args) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService") // Cambiar a tu URL
                .defaultHeader("Content-Type", "application/xml")
                .build();

        String requestBody = """
                <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <S:Header>
                        <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                            <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>20510910517MODDATOS</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">QAEUW1YLSD</wsse:Password>
                            </wsse:UsernameToken>
                        </wsse:Security>
                    </S:Header>
                    <S:Body>
                        <ns2:sendBill xmlns:ns2="http://service.sunat.gob.pe">
                            <fileName>20510910517-01-F001-00003913.zip</fileName>
                            <contentFile>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</contentFile>
                        </ns2:sendBill>
                    </S:Body>
                </S:Envelope>
                """;

        String response = webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Si es llamado en un entorno no bloqueante, evita usar `block`

        System.out.println(response);
    }
}

