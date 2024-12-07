package service.cloud.request.clientrequest.estela.dto;

import lombok.Data;

@Data
public class FileResponseDTO {

    public FileResponseDTO(String status, String message) {
        this.status = status;
        this.message = message;
    }

    private String status;
    private String message;

    // Getters, setters, constructor y toString
}
