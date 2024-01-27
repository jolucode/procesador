/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_RESUMEN_LINEA")
public class TransaccionResumenLinea implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionResumenLineaPK transaccionResumenLineaPK;

    @Column(name = "Numero_Correlativo")
    private String numeroCorrelativo;

    @Column(name = "Total_OP_Gratuitas")
    private BigDecimal totalOPGratuitas;

    @Column(name = "DocIdentidad")
    private String docIdentidad;

    @Column(name = "TipoDocIdentidad")
    private String tipoDocIdentidad;

    @Column(name = "DocId_Modificado")
    private String docIdModificado;

    @Column(name = "TipoDocIdentidad_Modificado")
    private String tipoDocIdentidadModificado;

    @Column(name = "Regimen_Percepcion")
    private String regimenPercepcion;

    @Column(name = "Tasa_Percepcion")
    private BigDecimal tasaPercepcion;

    @Column(name = "Monto_Percepcion")
    private BigDecimal montoPercepcion;

    @Column(name = "Monto_Total_Cobrar")
    private BigDecimal montoTotalCobrar;

    @Column(name = "Base_Imponible")
    private BigDecimal baseImponible;

    @Column(name = "Estado")
    private String estado;

    @Column(name = "Tipo_Documento")
    private String tipoDocumento;

    @Column(name = "Numero_Serie")
    private String numeroSerie;

    @Column(name = "Numero_Correlativo_Inicio")
    private String numeroCorrelativoInicio;

    @Column(name = "Numero_Correlativo_Fin")
    private String numeroCorrelativoFin;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Total_OP_Gravadas")
    private BigDecimal totalOPGravadas;

    @Column(name = "Total_OP_Exoneradas")
    private BigDecimal totalOPExoneradas;

    @Column(name = "Total_OP_Inafectas")
    private BigDecimal totalOPInafectas;

    @Column(name = "Importe_Otros_Cargos")
    private BigDecimal importeOtrosCargos;

    @Column(name = "Total_ISC")
    private BigDecimal totalISC;

    @Column(name = "Tota_IGV")
    private BigDecimal totaIGV;

    @Column(name = "Total_Otros_Tributos")
    private BigDecimal totalOtrosTributos;

    @Column(name = "Importe_Total")
    private BigDecimal importeTotal;

    @Column(name = "CodMoneda")
    private String codMoneda;

    @JoinColumn(name = "Id_Transaccion", referencedColumnName = "Id_Transaccion", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private TransaccionResumen transaccionResumen;

    @Override
    public String toString() {
        return "TransaccionResumenLinea[ transaccionResumenLineaPK=" + transaccionResumenLineaPK + " ]";
    }
}
