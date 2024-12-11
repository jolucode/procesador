package service.cloud.request.clientrequest.estela.dto;

import lombok.Data;

@Data
public class FileResponseDTO {

    public FileResponseDTO(String status, String message, byte[] content) {
        this.status = status;
        this.message = message;
        this.content = content;
    }

    private String status;
    private String message;
    private byte[] content;

    // Getters, setters, constructor y toString
}
