package service.cloud.request.clientRequest.service.extractor;

import org.springframework.http.ResponseEntity;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;

public interface CloudInterface {

    ResponseEntity<RequestPost> proccessDocument(String ejemploString);

}
