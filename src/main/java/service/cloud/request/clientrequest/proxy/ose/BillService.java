package service.cloud.request.clientrequest.proxy.ose;

import service.cloud.request.clientrequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientrequest.proxy.object.ObjectFactory;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://service.sunat.gob.pe", name = "billService")
@XmlSeeAlso({ObjectFactory.class})
public interface BillService {

    @WebMethod(action = "urn:sendBill")
    @RequestWrapper(localName = "sendBill", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.SendBill")
    @ResponseWrapper(localName = "sendBillResponse", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.SendBillResponse")
    @WebResult(name = "applicationResponse", targetNamespace = "")
    byte[] sendBill(

            @WebParam(name = "fileName", targetNamespace = "")
            String fileName,
            @WebParam(name = "contentFile", targetNamespace = "")
            javax.activation.DataHandler contentFile
    );


    @WebMethod(action = "urn:sendSummary")
    @RequestWrapper(localName = "sendSummary", targetNamespace = "http://service.sunat.gob.pe", className = "service.cloud.request.clientRequest.proxy.ose.object.SendSummary")
    @ResponseWrapper(localName = "sendSummaryResponse", targetNamespace = "http://service.sunat.gob.pe", className = "service.cloud.request.clientRequest.proxy.ose.object.SendSummaryResponse")
    @WebResult(name = "ticket", targetNamespace = "")
    String sendSummary(

            @WebParam(name = "fileName", targetNamespace = "")
            String fileName,
            @WebParam(name = "contentFile", targetNamespace = "")
            javax.activation.DataHandler contentFile
    );


    @WebMethod(action = "urn:getStatus")
    @RequestWrapper(localName = "getStatus", targetNamespace = "http://service.sunat.gob.pe", className = "service.cloud.request.clientRequest.proxy.ose.object.GetStatus")
    @ResponseWrapper(localName = "getStatusResponse", targetNamespace = "http://service.sunat.gob.pe", className = "service.cloud.request.clientRequest.proxy.ose.object.GetStatusResponse")
    @WebResult(name = "status", targetNamespace = "")
    CdrStatusResponse getStatus(
            @WebParam(name = "ticket", targetNamespace = "")
            String ticket
    );


    @WebMethod(action = "urn:getStatusCdr")
    @RequestWrapper(localName = "getStatusCdr", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatusCdr")
    @ResponseWrapper(localName = "getStatusCdrResponse", targetNamespace = "http://service.sunat.gob.pe", className = "pe.gob.sunat.GetStatusCdrResponse")
    @WebResult(name = "status", targetNamespace = "")
    CdrStatusResponse getStatusCdr(
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