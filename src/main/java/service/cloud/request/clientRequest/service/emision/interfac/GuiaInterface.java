package service.cloud.request.clientRequest.service.emision.interfac;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.entity.Transaccion;

public interface GuiaInterface {


    public TransaccionRespuesta transactionRemissionGuideDocumentRest(Transaccion transaction, String docType) throws Exception;

}
