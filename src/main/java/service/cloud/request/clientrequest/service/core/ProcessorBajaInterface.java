package service.cloud.request.clientrequest.service.core;

import org.springframework.stereotype.Component;
import service.cloud.request.clientrequest.dto.TransaccionRespuesta;
import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;

@Component
public interface ProcessorBajaInterface {

    TransaccionRespuesta consultVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception ;


}
