package service.cloud.request.clientRequest.proxy.sunat.consulta;

import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.object.ObjectFactory;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://service.sunat.gob.pe", name = "billService")
@XmlSeeAlso({ObjectFactory.class})
public interface BillServiceSunatConsult {

    @WebResult(name = "status", targetNamespace = "")
    @RequestWrapper(localName = "getStatus", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatus")
    @WebMethod(action = "urn:getStatus")
    @ResponseWrapper(localName = "getStatusResponse", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatusResponse")
    CdrStatusResponse getStatus(
            @WebParam(name = "ticket", targetNamespace = "")
            String ticket
    );


    @WebMethod(action = "urn:getStatusCdr")
    @WebResult(name = "statusCdr", targetNamespace = "")
    @RequestWrapper(localName = "getStatusCdr", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.service.consult.GetStatusCdr")
    @ResponseWrapper(localName = "getStatusCdrResponse", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.service.consult.GetStatusCdrResponse")
    CdrStatusResponse getStatusCdr(@WebParam(name = "rucComprobante", targetNamespace = "") String rucComprobante, @WebParam(name = "tipoComprobante", targetNamespace = "") String tipoComprobante, @WebParam(name = "serieComprobante", targetNamespace = "") String serieComprobante, @WebParam(name = "numeroComprobante", targetNamespace = "") Integer numeroComprobante);


}