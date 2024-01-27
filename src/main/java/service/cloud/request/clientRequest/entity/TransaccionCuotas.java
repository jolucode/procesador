/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author joseLuis
 */
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "TRANSACCION_CUOTAS")
public class TransaccionCuotas implements Serializable {

    private static final long serialVersionUID = 1L;

/*
    @Id
    @Column(name = "FE_Id")
    private String fEId;
*/

    @NonNull
    @EmbeddedId
    protected TransaccionCuotasPK transaccionLineasPK;


    @Column(name = "Cuota")
    private String cuota;

    @Column(name = "FechaCuota")
    @Temporal(TemporalType.DATE)
    private Date fechaCuota;

    @Column(name = "MontoCuota")
    private BigDecimal montoCuota;


    @Column(name = "FechaEmision")
    @Temporal(TemporalType.DATE)
    private Date fechaEmision;

    @Column(name = "FormaPago")
    private String formaPago;

    //@JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id" , insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    private Transaccion transaccion;


    public TransaccionCuotasPK getTransaccionLineasPK() {
        return transaccionLineasPK;
    }

    public void setTransaccionLineasPK(TransaccionCuotasPK transaccionLineasPK) {
        this.transaccionLineasPK = transaccionLineasPK;
    }

    public String getCuota() {
        return cuota;
    }

    public void setCuota(String cuota) {
        this.cuota = cuota;
    }

    public Date getFechaCuota() {
        return fechaCuota;
    }

    public void setFechaCuota(Date fechaCuota) {
        this.fechaCuota = fechaCuota;
    }

    public BigDecimal getMontoCuota() {
        return montoCuota;
    }

    public void setMontoCuota(BigDecimal montoCuota) {
        this.montoCuota = montoCuota;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(Transaccion transaccion) {
        this.transaccion = transaccion;
    }
}
