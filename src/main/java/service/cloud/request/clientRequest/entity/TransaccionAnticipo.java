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
@Table(name = "TRANSACCION_ANTICIPO")
public class TransaccionAnticipo implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionAnticipoPK transaccionAnticipoPK;

    @Column(name = "DOC_Moneda")
    private String dOCMoneda;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "Anticipo_Monto")
    private BigDecimal anticipoMonto;

    @Column(name = "AntiDOC_Tipo")
    private String antiDOCTipo;

    @Column(name = "AntiDOC_Serie_Correlativo")
    private String antiDOCSerieCorrelativo;

    @Column(name = "DOC_Numero")
    private String dOCNumero;

    @Column(name = "DOC_Tipo")
    private String dOCTipo;

    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionAnticipo[ transaccionAnticipoPK=" + transaccionAnticipoPK + " ]";
    }

}
