/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
public class PublicacionDocumento implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "FE_Id")
    private String fEId;

    @Column(name = "nombreConsumidor")
    private String nombreConsumidor;

    @Column(name = "emailConsumidor")
    private String emailConsumidor;

    @Column(name = "numeroSerie")
    private String numeroSerie;

    @Column(name = "TipoDocumento")
    private String tipoDocumento;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "estadoSunat")
    private String estadoSunat;

    @Column(name = "moneda")
    private String moneda;

    @Column(name = "EMailEmisor")
    private String emailEmisor;

    @Column(name = "Ruta_Pdf")
    private String rutaPdf;

    @Column(name = "Ruta_Xml")
    private String rutaXml;

    @Column(name = "Ruta_Cdr")
    private String rutaCdr;

    @Column(name = "Estado")
    private Integer estado;

    @Column(name = "Msj_Error")
    private String msjError;

    @Column(name = "Ruc")
    private String ruc;

    @Column(name = "FechaEmision")
    private String fechaEmision;

    @Override
    public String toString() {
        return "PublicacionDocumento[ fEId=" + fEId + " ]";
    }
}
