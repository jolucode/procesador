package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.entity.Transaccion;

@Component
public interface ServiceInterface {


    /**emision*/
    public TransaccionRespuesta transactionDocument(Transaccion transaction, String doctype) throws Exception;

    /**dar de baja*/
    public TransaccionRespuesta transactionVoidedDocument(Transaccion transaction, String doctype) throws Exception ;

}
