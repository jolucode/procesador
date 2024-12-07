package service.cloud.request.clientrequest.proxy.sunat.consulta;


import service.cloud.request.clientrequest.proxy.model.CdrStatusResponse;

public interface ISUNATClientConsult {


    /**
     * Este metodo ha implementar consultara el estado de un documento ('Factura', 'Nota de Credito'
     * o 'Nota de Debito'), retornando un objeto StatusResponse que contiene el CDR OSE de respuesta
     * y el codigo de respuesta.
     *
     * @param ticket El numero de ticket de consulta.
     * @return Retorna el objeto StatusResponse que contiene el CDR OSE de respuesta y el codigo de respuesta.
     * @throws Exception
     */
    CdrStatusResponse getStatus(String ruc, String ticket) throws Exception;


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
