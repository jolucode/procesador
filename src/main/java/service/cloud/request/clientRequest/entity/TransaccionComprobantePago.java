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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_COMPROBANTE_PAGO")
public class TransaccionComprobantePago implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionComprobantePagoPK transaccionComprobantePagoPK;

    @Column(name = "DOC_Tipo")
    private String DOC_Tipo;

    @Column(name = "DOC_Numero")
    private String DOC_Numero;

    @Column(name = "DOC_FechaEmision")
    @Temporal(TemporalType.DATE)
    private Date DOC_FechaEmision;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "DOC_Importe")
    private BigDecimal DOC_Importe;

    @Column(name = "DOC_Moneda")
    private String dOCMoneda;

    @Column(name = "PagoFecha")
    @Temporal(TemporalType.DATE)
    private Date pagoFecha;

    @Column(name = "PagoNumero")
    private String PagoNumero;

    @Column(name = "PagoImporteSR")
    private BigDecimal Importe_Pago_Soles;

    @Column(name = "PagoMoneda")
    private String pagoMoneda;

    @Column(name = "CP_Importe")
    private BigDecimal CP_Importe;

    @Column(name = "CP_Moneda")
    private String cPMoneda;

    @Transient
    private String TipoMoneda;

    @Transient
    private String TipoMonedaTotal;

    @Column(name = "CP_Fecha")
    @Temporal(TemporalType.DATE)
    private Date cPFecha;

    @Column(name = "CP_ImporteTotal")
    private BigDecimal CP_ImporteTotal;

    @Column(name = "CP_MonedaMontoNeto")
    private String cPMonedaMontoNeto;

    @Column(name = "TC_MonedaRef")
    private String tCMonedaRef;

    @Column(name = "TC_MonedaObj")
    private String tCMonedaObj;

    @Column(name = "TC_Factor")
    private BigDecimal TC_Factor;

    @Column(name = "TC_Fecha")
    @Temporal(TemporalType.DATE)
    private Date tCFecha;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionComprobantePago")
    private List<TransaccionComprobantepagoUsuario> transaccionComprobantepagoUsuarioList = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionComprobantePago[ transaccionComprobantePagoPK=" + transaccionComprobantePagoPK + " ]";
    }
}
