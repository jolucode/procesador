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
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_PROPIEDADES")
public class TransaccionPropiedades implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionPropiedadesPK transaccionPropiedadesPK;

    @Column(name = "Valor")
    private String valor;

    @Column(name = "Description")
    private String description;

    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionPropiedades[ transaccionPropiedadesPK=" + transaccionPropiedadesPK + " ]";
    }
}
