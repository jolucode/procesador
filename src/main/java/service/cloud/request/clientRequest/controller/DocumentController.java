package service.cloud.request.clientRequest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.service.emision.ServicePrincipal;

@RestController
public class DocumentController {

    @Autowired
    private ServicePrincipal servicePrincipal;

    @Autowired
    ClientProperties clientProperties;


    /*@PostMapping("/procesar")
    public ResponseEntity<RequestPost> crearEjemplo(@RequestBody String ejemploString) {
        try {
                RequestPost requestPost = servicePrincipal.EnviarTransacciones();
            if (requestPost.getStatus() == 200) {
                return new ResponseEntity<>(requestPost, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(requestPost, HttpStatus.CONFLICT);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }*/


    @PostMapping("/ejemplo")
    public String procesarDocumentos(@RequestBody String ejemploString) {
        return "HOLA MUNDO ";
    }

}
