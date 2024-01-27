package service.cloud.request.clientRequest.service.core;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.entity.Transaccion;

@Component
public interface ProcessorBajaInterface {

    public TransaccionRespuesta consultVoidedDocument(Transaccion transaction, String doctype) throws Exception ;


}
