package service.cloud.request.clientRequest.ose;


import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

public interface IOSEClient {

    CdrStatusResponse getStatusCDR(String rucComprobante,
                                   String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception;


}
