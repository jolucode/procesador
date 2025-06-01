package service.cloud.request.clientRequest.service.publicar;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.model.Client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Optional;

@Slf4j
@Data
public class DocumentoPublicado {
    private String usuarioSesion;

    private String claveSesion;

    private String rucClient;

    private String nombreClient;

    private String email;

    private String numSerie;

    private String fecEmisionDoc;

    private String tipoDoc;

    private String total;

    private String docPdf;

    private String docXml;

    private String docCdr;

    private String estadoSunat;

    private String monedaTransaccion;

    private String emailEmisor;

    private String tipoTransaccion;

    private String correoSecundario;

    private String rsRuc;

    private String rsDescripcion;

    private String serie;

    public DocumentoPublicado(Client client, TransacctionDTO docPublicar, TransaccionRespuesta transaccionRespuesta) {
        this.usuarioSesion = client.getWsUsuario();
        this.claveSesion = client.getWsClave();
        this.rucClient = docPublicar.getSN_DocIdentidad_Nro();
        this.nombreClient = docPublicar.getSN_RazonSocial();
        this.email = docPublicar.getSN_EMail();
        String docId = docPublicar.getDOC_Id();
        this.numSerie = docPublicar.getDocIdentidad_Nro() + "-"+ docId;
        String[] split = docId.split("[-]");
        this.serie = (split.length > 0) ? split[0] : "";
        this.fecEmisionDoc = new SimpleDateFormat("dd/MM/yyyy").format(docPublicar.getDOC_FechaEmision());
        this.tipoDoc = docPublicar.getDOC_Codigo();
        BigDecimal montoTotal = docPublicar.getDOC_MontoTotal() == null ? BigDecimal.ZERO : docPublicar.getDOC_MontoTotal().setScale(2, RoundingMode.HALF_UP);
        this.total = montoTotal.toString();
        this.estadoSunat = transaccionRespuesta.getEstado(); //"V" - aprobado
        this.monedaTransaccion = docPublicar.getDOC_MON_Codigo();
        this.emailEmisor = docPublicar.getEMail();
        this.tipoTransaccion = docPublicar.getFE_TipoTrans();
        this.correoSecundario = docPublicar.getSN_EMail_Secundario();
        this.rsRuc = docPublicar.getSN_DocIdentidad_Nro();
        this.rsDescripcion = docPublicar.getSN_RazonSocial();

        if ("B".equals(this.tipoTransaccion)) {
            if (transaccionRespuesta.getZip() != null) {
                this.docCdr = encodeFileToBase64Binary(transaccionRespuesta.getZip()).orElse(null);
                this.estadoSunat = "P";
            } else {
                log.error("Transacción tipo 'B' no contiene archivo ZIP. No se puede codificar.");
            }
        }else {
            boolean datosFaltantes = false;

            if (transaccionRespuesta.getPdf() == null) {
                log.error("No se publica el documento: archivo PDF es null");
                datosFaltantes = true;
            }
            if (transaccionRespuesta.getXml() == null) {
                log.error("No se publica el documento: archivo XML es null");
                datosFaltantes = true;
            }
            if (transaccionRespuesta.getZip() == null) {
                log.error("No se publica el documento: archivo ZIP es null");
                datosFaltantes = true;
            }

            if (!datosFaltantes) {
                this.docPdf = encodeFileToBase64Binary(transaccionRespuesta.getPdf()).orElse(null);
                this.docXml = encodeFileToBase64Binary(transaccionRespuesta.getXml()).orElse(null);
                this.docCdr = encodeFileToBase64Binary(transaccionRespuesta.getZip()).orElse(null);
            } else {
                log.warn("Documento no publicado porque faltan archivos requeridos.");
            }
        }
    }

    private Optional<String> encodeFileToBase64Binary(byte[] byteDocument) {
        try {
            String base64String = Base64.getEncoder().encodeToString(byteDocument);
            return Optional.of(base64String);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }
}