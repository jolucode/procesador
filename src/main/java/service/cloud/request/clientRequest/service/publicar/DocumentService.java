package service.cloud.request.clientRequest.service.publicar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dao.PublicardocRepository;
import service.cloud.request.clientRequest.entity.PublicardocWs;
import service.cloud.request.clientRequest.entity.Transaccion;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private PublicardocRepository publicardocRepository;

    public Optional<PublicardocWs> obtenerDocumentoPorId(String id) {
        return publicardocRepository.findById(id);
    }

    public void  crearObjectPublicardocWs(Transaccion tc, Map<String, String> listaRuta, boolean band) {
        PublicardocWs publicardocWs = new PublicardocWs();
        publicardocWs.setFEId(tc.getFE_Id());
        publicardocWs.setRSRuc(tc.getDocIdentidad_Nro());
        publicardocWs.setDOCId(tc.getDOC_Id());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        System.out.println("****************************************************************************************");
        publicardocWs.setFechaPublicacionPortal(calendar.getTime());
        publicardocWs.setDOCFechaEmision(tc.getDOC_FechaEmision());
        publicardocWs.setDOCMontoTotal(tc.getDOC_MontoTotal() == null ? BigDecimal.ZERO : tc.getDOC_MontoTotal());
        publicardocWs.setDOCCodigo(tc.getDOC_Codigo());
        publicardocWs.setSNDocIdentidadNro(tc.getSN_DocIdentidad_Nro());
        publicardocWs.setSNRazonSocial(tc.getSN_RazonSocial());
        publicardocWs.setSNEMail(tc.getSN_EMail());
        if (Optional.ofNullable(listaRuta).isPresent()) {
            System.out.println();
            listaRuta.forEach((key, value) -> System.out.println(key + ": " + value));
            publicardocWs.setRutaPDF(listaRuta.get("rutaPDF"));
            publicardocWs.setRutaXML(listaRuta.get("rutaXML"));
            publicardocWs.setRutaZIP(listaRuta.get("rutaCDR"));
        }
        publicardocWs.setFETipoTrans(tc.getFE_TipoTrans());
        publicardocWs.setSNEMailSecundario(tc.getSN_EMail_Secundario());
        if (band) {
            publicardocWs.setEstadoSUNAT('V');
        } else {
            if ("03".equals(tc.getDOC_Codigo())) {
                publicardocWs.setEstadoSUNAT('D');
            } else {
                publicardocWs.setEstadoSUNAT('R');
            }
        }
        publicardocWs.setDOCMONCodigo(tc.getDOC_MON_Codigo());
        publicardocWs.setDOCMONNombre(tc.getDOC_MON_Nombre());
        publicardocWs.setEMailEmisor(tc.getEMail());
        publicardocWs.setEstadoPublicacion('A');
        publicardocRepository.save(publicardocWs);
    }
}