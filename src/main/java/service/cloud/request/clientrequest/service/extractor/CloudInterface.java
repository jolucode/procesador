package service.cloud.request.clientrequest.service.extractor;

import org.springframework.http.ResponseEntity;

public interface CloudInterface {

    ResponseEntity<Void> proccessDocument(String ejemploString);

}
