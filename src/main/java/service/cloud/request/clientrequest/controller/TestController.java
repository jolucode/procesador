package service.cloud.request.clientrequest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.proxy.ose.OSEClient;
import service.cloud.request.clientrequest.proxy.model.CdrStatusResponse;

@RestController
@RequestMapping("/ose")
@RequiredArgsConstructor
public class TestController {


    @Autowired
    OSEClient oseClient;

    @GetMapping
    public Mono<ResponseEntity<CdrStatusResponse>> consultaDoc() throws Exception {

        String documentRUC = "20552572565";
        String documentType = "03";
        String documentSerie = "B001";
        Integer documentNumber = 932;

        CdrStatusResponse statusResponseOSE = oseClient.getStatusCDR(documentRUC, documentType, documentSerie, documentNumber);

        return Mono.just(ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(statusResponseOSE))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
