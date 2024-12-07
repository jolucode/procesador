package service.cloud.request.clientrequest.proxy.sunat.consulta;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.URL;

@WebServiceClient(name = "billService")
public class BillService_Service_Sunat_Consult extends Service {

    public BillService_Service_Sunat_Consult(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    @WebEndpoint(name = "BillServicePort")
    public BillServiceSunatConsult getBillServicePort(QName portName) {
        return super.getPort(portName, BillServiceSunatConsult.class);
    }
}


