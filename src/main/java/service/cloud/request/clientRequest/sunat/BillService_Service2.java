package service.cloud.request.clientRequest.sunat;

import service.cloud.request.clientRequest.ose.BillService;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import java.net.MalformedURLException;
import java.net.URL;

@WebServiceClient(name = "billService",
        wsdlLocation = "https://www.sunat.gob.pe/ol-ti-itcpgem-beta/billService?wsdl",
        //wsdlLocation = "https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl",
        targetNamespace = "http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/")
public class BillService_Service2 extends Service {

    public final static QName SERVICE = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "billService");

    public final static QName BillServicePort = new QName("http://service.gem.factura.comppago.registro.servicio.sunat.gob.pe/", "BillServicePort");


    public BillService_Service2(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    @WebEndpoint(name = "BillServicePort")
    public BillService2 getBillServicePort() {
        return super.getPort(BillServicePort, BillService2.class);
    }
}


