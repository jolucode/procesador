package service.cloud.request.clientRequest.ose;


import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

import javax.activation.DataHandler;

public interface IOSEClient {


    /**
     * Este metodo ha implementar enviara el documento ('Factura', 'Boleta', 'Nota de Credito' o
     * 'Nota de Debito') al Servicio Web OSE de TCI, retornando un CDR OSE de respuesta.
     *
     * @param fileName    El nombre del archivo adjunto
     * @param contentFile El archivo ZIP adjunto.
     * @return Retorna el CDR OSE de respuesta.
     * @throws Exception
     */
    byte[] sendBill(String fileName, DataHandler contentFile) throws Exception;


    /**
     * Este metodo ha implementar consultara el estado de un CDR OSE, retornando un objeto CdrStatusResponse
     * que contiene el CDR OSE de respuesta, el codigo de respuesta y el mensaje de respuesta.
     *
     * @param rucComprobante    Numero RUC del comprobante a consultar.
     * @param tipoComprobante   Tipo del comprobante.
     * @param serieComprobante  Serie del comprobante.
     * @param numeroComprobante Numero del comprobante.
     * @return Retorna el objeto CdrStatusResponse que contiene el CDR OSE de respuesta, el codigo de respuesta
     * y el mensaje de respuesta.
     * @throws Exception
     */
    CdrStatusResponse getStatusCDR(String rucComprobante,
                                   String tipoComprobante, String serieComprobante, Integer numeroComprobante) throws Exception;


}
