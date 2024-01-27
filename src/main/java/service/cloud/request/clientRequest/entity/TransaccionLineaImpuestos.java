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
@Table(name = "TRANSACCION_LINEA_IMPUESTOS")
public class TransaccionLineaImpuestos implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionLineaImpuestosPK transaccionLineaImpuestosPK;

    @Column(name = "Nombre")
    private String nombre;

    @Column(name = "ValorVenta")
    private BigDecimal ValorVenta;

    @Column(name = "Abreviatura")
    private String abreviatura;

    @Column(name = "Codigo")
    private String codigo;

    @Column(name = "Moneda")
    private String moneda;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Monto")
    private BigDecimal monto;

    @Column(name = "Porcentaje")
    private BigDecimal porcentaje;

    @Column(name = "TipoTributo")
    private String tipoTributo;

    @Column(name = "TipoAfectacion")
    private String tipoAfectacion;

    @Column(name = "TierRange")
    private String tierRange;

    @JoinColumns({
            @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false),
            @JoinColumn(name = "NroOrden", referencedColumnName = "NroOrden", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private TransaccionLineas transaccionLineas;

    public TransaccionLineaImpuestos(String feID, int nroOrden, Integer lineId) {
    }

    @Override
    public String toString() {
        return "org.ventura.cpe.core.dto.hb.TransaccionLineaImpuestos[ transaccionLineaImpuestosPK=" + transaccionLineaImpuestosPK + " ]";
    }
}
