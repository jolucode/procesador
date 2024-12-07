package service.cloud.request.clientrequest.service.emision.interfac;

import service.cloud.request.clientrequest.dto.TransaccionRespuesta;
import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;

public interface GuiaInterface {


    TransaccionRespuesta transactionRemissionGuideDocumentRest(TransacctionDTO transaction, String docType) throws Exception;

}
