package service.cloud.request.clientrequest.estela.dto;

import lombok.Data;

@Data
public class FileRequestDTO {
    private String service;

    private String username;
    private String password;
    private String filename;
    private String contentFile;

    private String rucComprobante;
    private String tipoComprobante;
    private String serieComprobante;
    private String numeroComprobante;

    // Getters, setters, constructor y toString
}
