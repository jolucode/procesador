package service.cloud.request.clientrequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientrequest.dto.TransaccionRespuesta;
import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;

@Component
public interface IServiceBaja {


    /**dar de baja*/
    TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception ;

}
