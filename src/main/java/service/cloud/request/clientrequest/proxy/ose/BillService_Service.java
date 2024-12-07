package service.cloud.request.clientrequest.proxy.ose;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.URL;

@WebServiceClient(name = "billService")
public class BillService_Service extends Service {

    public BillService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    @WebEndpoint(name = "BillServicePort")
    public BillService getBillServicePort(QName portName) {
        return super.getPort(portName, BillService.class);
    }
}


