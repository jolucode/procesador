package service.cloud.request.clientRequest.proxy.sunat;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.URL;

@WebServiceClient(name = "billService")
public class BillService_Service2 extends Service {

    public BillService_Service2(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    @WebEndpoint(name = "BillServicePort")
    public BillService2 getBillServicePort(QName portName) {
        return super.getPort(portName, BillService2.class);
    }
}


