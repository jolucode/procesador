/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "PUBLICARDOC_WS")
public class PublicardocWs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "FEId")
    private String fEId;

    @Column(name = "RSRuc")
    private String rSRuc;

    @Column(name = "RSDescripcion")
    private String rSDescripcion;

    @Column(name = "DOCId")
    private String dOCId;

    @Column(name = "FETipoTrans")
    private String fETipoTrans;

    @Column(name = "DOCFechaEmision")
    //@Convert(converter = LocalDateAttributeConverter.class)
    private Date dOCFechaEmision;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "DOCMontoTotal")
    private BigDecimal dOCMontoTotal;

    @Column(name = "DOCCodigo")
    private String dOCCodigo;

    @Column(name = "SNDocIdentidadNro")
    private String sNDocIdentidadNro;

    @Column(name = "SNRazonSocial")
    private String sNRazonSocial;

    @Column(name = "SNEMail")
    private String sNEMail;

    @Column(name = "SNEMailSecundario")
    private String sNEMailSecundario;

    @Lob
    @Column(name = "rutaPDF")
    private String rutaPDF;

    @Lob
    @Column(name = "rutaXML")
    private String rutaXML;

    @Lob
    @Column(name = "rutaZIP")
    private String rutaZIP;

    @Column(name = "EstadoSUNAT")
    private Character estadoSUNAT;

    @Column(name = "DOCMONCodigo")
    private String dOCMONCodigo;

    @Column(name = "DOCMONNombre")
    private String dOCMONNombre;

    @Column(name = "EMailEmisor")
    private String eMailEmisor;

    @Column(name = "EstadoPublicacion")
    private Character estadoPublicacion;

    @Column(name = "fechaPublicacionPortal")
    private Date fechaPublicacionPortal;

    @Override
    public String toString() {
        return "PublicardocWs{" +
                "fEId='" + fEId + '\'' +
                ", rSRuc='" + rSRuc + '\'' +
                ", rSDescripcion='" + rSDescripcion + '\'' +
                ", dOCId='" + dOCId + '\'' +
                ", fETipoTrans='" + fETipoTrans + '\'' +
                ", dOCFechaEmision=" + dOCFechaEmision +
                ", dOCMontoTotal=" + dOCMontoTotal +
                ", dOCCodigo='" + dOCCodigo + '\'' +
                ", sNDocIdentidadNro='" + sNDocIdentidadNro + '\'' +
                ", sNRazonSocial='" + sNRazonSocial + '\'' +
                ", sNEMail='" + sNEMail + '\'' +
                ", sNEMailSecundario='" + sNEMailSecundario + '\'' +
                ", rutaPDF='" + rutaPDF + '\'' +
                ", rutaXML='" + rutaXML + '\'' +
                ", rutaZIP='" + rutaZIP + '\'' +
                ", estadoSUNAT=" + estadoSUNAT +
                ", dOCMONCodigo='" + dOCMONCodigo + '\'' +
                ", dOCMONNombre='" + dOCMONNombre + '\'' +
                ", eMailEmisor='" + eMailEmisor + '\'' +
                ", estadoPublicacion=" + estadoPublicacion +
                ", fechaPublicacionPortal=" + fechaPublicacionPortal +
                '}';
    }
}
