package service.cloud.request.clientRequest.ose;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;

@WebServiceClient(name = "billService")
public class BillService_Service extends Service {

    public final static QName SERVICE = new QName("http://service.sunat.gob.pe", "billService");

    public final static QName BillServicePort = new QName("http://service.sunat.gob.pe", "BillServicePort");

    public BillService_Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    @WebEndpoint(name = "BillServicePort")
    public BillService getBillServicePort() {
        return super.getPort(BillServicePort, BillService.class);
    }
}


