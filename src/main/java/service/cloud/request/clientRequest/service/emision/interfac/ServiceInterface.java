package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

@Component
public interface ServiceInterface {


    /**emision*/
    public TransaccionRespuesta transactionDocument(TransacctionDTO transaction, String doctype) throws Exception;

    /**dar de baja*/
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception ;

}
