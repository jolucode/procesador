/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * @author VSUser
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_LINEAS_USUCAMPOS")
public class TransaccionLineasUsucampos implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionLineasUsucamposPK transaccionLineasUsucamposPK;

    @Column(name = "Valor")
    private String valor;

    @JoinColumn(name = "USUCMP_Id", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Usuariocampos usuariocampos;

    @JoinColumns({
            @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false),
            @JoinColumn(name = "NroOrden", referencedColumnName = "NroOrden", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private TransaccionLineas transaccionLineas;

    @Override
    public String toString() {
        return "TransaccionLineasUsucampos[ transaccionLineasUsucamposPK=" + transaccionLineasUsucamposPK + " ]";
    }
}
