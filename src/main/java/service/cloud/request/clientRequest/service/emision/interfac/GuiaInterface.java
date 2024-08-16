package service.cloud.request.clientRequest.service.emision.interfac;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;

public interface GuiaInterface {


    public TransaccionRespuesta transactionRemissionGuideDocumentRest(TransacctionDTO transaction, String docType) throws Exception;

}
