package service.cloud.request.clientRequest.service.extractor;

import org.springframework.http.ResponseEntity;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

public interface CloudInterface {

    ResponseEntity<TransacctionDTO[]> proccessDocument(String ejemploString);

}
