package service.cloud.request.clientrequest.estela.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientrequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientrequest.estela.service.ConsultaFileService;
import service.cloud.request.clientrequest.estela.service.EmisionService;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final EmisionService emisionService;
    private final ConsultaFileService consultaFileService;

    public FileController(EmisionService emisionService, ConsultaFileService consultaFileService) {
        this.emisionService = emisionService;
        this.consultaFileService = consultaFileService;
    }

    @PostMapping("/upload")
    public Mono<ResponseEntity<FileResponseDTO>> emisionDoc(@RequestBody FileRequestDTO requestDTO) {
        String url;

        // Determinar la URL según el valor del campo 'service' en requestDTO
        switch (requestDTO.getService()) {
            case "SUNAT":
                url = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService";
                break;
            case "OSE":
                url = "https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl";
                break;
            case "ESTELA":
                url = "https://ose-test.com/ol-ti-itcpe/billService3";
                break;
            default:
                // En caso de que el servicio no sea uno de los conocidos, puedes asignar una URL por defecto o lanzar un error.
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new FileResponseDTO("Error", "Invalid service")));
        }

        // Llamar al servicio con la URL determinada
        return emisionService.processAndSaveFile(url, requestDTO)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }


    @PostMapping("/consulta")
    public Mono<ResponseEntity<FileResponseDTO>> consultaDoc(@RequestBody FileRequestDTO requestDTO) {
        return consultaFileService.processAndSaveFile("https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl", requestDTO)
                .map(response -> ResponseEntity.ok(response)).defaultIfEmpty(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}
