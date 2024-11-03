package service.cloud.request.clientRequest.service.core;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

@Component
public interface ProcessorBajaInterface {

    TransaccionRespuesta consultVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception ;


}
