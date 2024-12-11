package service.cloud.request.clientRequest.service.extractor;

import org.springframework.http.ResponseEntity;

public interface CloudInterface {

    ResponseEntity<Void> proccessDocument(String ejemploString);

}
