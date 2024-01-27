package service.cloud.request.clientRequest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.service.extractor.CloudService;

@RestController()
public class CloudController {

    @Autowired
    private CloudService cloudService;

    @PostMapping("/procesar")
    public ResponseEntity<TransacctionDTO[]> proccessDocument(@RequestBody String listDocument) {
        return cloudService.proccessDocument(listDocument);
    }

}
