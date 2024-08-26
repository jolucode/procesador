package service.cloud.request.clientRequest.ose;

import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://service.sunat.gob.pe", name = "billService")
@XmlSeeAlso({service.cloud.request.clientRequest.ose.object.ObjectFactory.class})
public interface BillService {


    @WebMethod(action = "urn:getStatusCdr")
    @RequestWrapper(localName = "getStatusCdr", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatusCdr")
    @ResponseWrapper(localName = "getStatusCdrResponse", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatusCdrResponse")
    @WebResult(name = "status", targetNamespace = "")
    public CdrStatusResponse getStatusCdr(
            @WebParam(name = "rucComprobante", targetNamespace = "")
            String rucComprobante,
            @WebParam(name = "tipoComprobante", targetNamespace = "")
            String tipoComprobante,
            @WebParam(name = "serieComprobante", targetNamespace = "")
            String serieComprobante,
            @WebParam(name = "numeroComprobante", targetNamespace = "")
            Integer numeroComprobante
    );
}