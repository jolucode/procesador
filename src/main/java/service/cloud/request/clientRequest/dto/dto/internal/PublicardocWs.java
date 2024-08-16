package service.cloud.request.clientRequest.dto.dto.internal;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PublicardocWs {


    private String fEId;
    private String rSRuc;
    private String rSDescripcion;
    private String dOCId;
    private String fETipoTrans;
    private Date dOCFechaEmision;
    private BigDecimal dOCMontoTotal;
    private String dOCCodigo;
    private String sNDocIdentidadNro;
    private String sNRazonSocial;
    private String sNEMail;
    private String sNEMailSecundario;
    private String rutaPDF;
    private String rutaXML;
    private String rutaZIP;
    private Character estadoSUNAT;
    private String dOCMONCodigo;
    private String dOCMONNombre;
    private String eMailEmisor;
    private Character estadoPublicacion;
    private Date fechaPublicacionPortal;



}








