package service.cloud.request.clientRequest.proxy.sunat;

import service.cloud.request.clientRequest.proxy.ose.object.ObjectFactory;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://service.sunat.gob.pe", name = "billService")
@XmlSeeAlso({ObjectFactory.class})
public interface BillService2 {

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
}