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

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_RESUMEN_LINEA_ANEXO")
public class TransaccionResumenLineaAnexo implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected TransaccionResumenLineaAnexoPK transaccionResumenLineaAnexoPK;

    @Column(name = "Serie")
    private String serie;

    @Column(name = "Correlativo")
    private String correlativo;

    @Column(name = "DocEntry")
    private String docEntry;

    @Column(name = "TipoTransaccion")
    private String tipoTransaccion;

    @Column(name = "SN")
    private String sn;

    @Column(name = "ObjcType")
    private String objcType;

    @Column(name = "TipoDocumento")
    private String tipoDocumento;

    @Column(name = "TipoEstado")
    private String tipoEstado;

    @Column(name = "Posicion")
    private Integer posicion;

    @JoinColumn(name = "Id_Transaccion", referencedColumnName = "Id_Transaccion", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TransaccionResumen transaccionResumen;

    @Override
    public String toString() {
        return "TransaccionResumenLineaAnexo[ transaccionResumenLineaAnexoPK=" + transaccionResumenLineaAnexoPK + " ]";
    }
}
