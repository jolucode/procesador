package service.cloud.request.clientRequest.service.publicar;

import lombok.Data;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.entity.PublicardocWs;
import service.cloud.request.clientRequest.prueba.Client;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Optional;

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

    public DocumentoPublicado(Client client, PublicardocWs docPublicar, TransaccionRespuesta transaccionRespuesta) {
        this.usuarioSesion = client.getWsUsuario();
        this.claveSesion = client.getWsClave();
        this.rucClient = docPublicar.getSNDocIdentidadNro();
        this.nombreClient = docPublicar.getSNRazonSocial();
        this.email = docPublicar.getSNEMail();
        String docId = docPublicar.getDOCId();
        this.numSerie = docPublicar.getRSRuc() + "-" + docId;
        String[] split = docId.split("[-]");
        this.serie = (split.length > 0) ? split[0] : "";
        this.fecEmisionDoc = new SimpleDateFormat("dd/MM/yyyy").format(docPublicar.getDOCFechaEmision());
        this.tipoDoc = docPublicar.getDOCCodigo();
        BigDecimal montoTotal = docPublicar.getDOCMontoTotal().setScale(2, RoundingMode.HALF_UP);
        this.total = montoTotal.toString();
        this.estadoSunat = String.valueOf(docPublicar.getEstadoSUNAT());
        this.monedaTransaccion = docPublicar.getDOCMONCodigo();
        this.emailEmisor = docPublicar.getEMailEmisor();
        this.tipoTransaccion = docPublicar.getFETipoTrans();
        this.correoSecundario = docPublicar.getSNEMailSecundario();
        this.rsRuc = docPublicar.getRSRuc();
        this.rsDescripcion = docPublicar.getRSDescripcion();
        this.docPdf = encodeFileToBase64Binary(transaccionRespuesta.getPdf()).get();
        this.docXml = encodeFileToBase64Binary(transaccionRespuesta.getXml()).get();
        this.docCdr = encodeFileToBase64Binary(transaccionRespuesta.getZip()).get();
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