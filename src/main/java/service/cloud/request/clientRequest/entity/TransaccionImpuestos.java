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
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_IMPUESTOS")
public class TransaccionImpuestos implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionImpuestosPK transaccionImpuestosPK;

    @Column(name = "Nombre")
    private String nombre;

    @Column(name = "ValorVenta")
    private BigDecimal valorVenta;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionImpuestos[ transaccionImpuestosPK=" + transaccionImpuestosPK + " ]";
    }

}
