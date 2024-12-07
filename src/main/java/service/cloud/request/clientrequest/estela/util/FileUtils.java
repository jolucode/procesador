package service.cloud.request.clientrequest.estela.util;

import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;

public class FileUtils {

    public static boolean isValidFileRequest(FileRequestDTO requestDTO) {
        return requestDTO.getUsername() != null && !requestDTO.getUsername().isEmpty()
                && requestDTO.getPassword() != null && !requestDTO.getPassword().isEmpty()
                && requestDTO.getFilename() != null && !requestDTO.getFilename().isEmpty()
                && requestDTO.getContentFile() != null && !requestDTO.getContentFile().isEmpty();
    }
}
