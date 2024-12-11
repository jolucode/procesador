package service.cloud.request.clientRequest.estela.util;

import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;

public class FileUtils {

    public static boolean isValidFileRequest(FileRequestDTO requestDTO) {
        return requestDTO.getUsername() != null && !requestDTO.getUsername().isEmpty()
                && requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()
                && requestDTO.getFileName() != null && !requestDTO.getFileName().isEmpty()
                && requestDTO.getContentFile() != null && !requestDTO.getContentFile().isEmpty();
    }
}
