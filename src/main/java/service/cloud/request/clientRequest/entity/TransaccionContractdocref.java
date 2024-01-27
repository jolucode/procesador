/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "TRANSACCION_CONTRACTDOCREF")
public class TransaccionContractdocref implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionContractdocrefPK transaccionContractdocrefPK;

    @Column(name = "Valor")
    private String valor;

    @JoinColumn(name = "USUCMP_Id", referencedColumnName = "Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Usuariocampos usuariocampos;

    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionContractdocref[ transaccionContractdocrefPK=" + transaccionContractdocrefPK + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransaccionContractdocref that = (TransaccionContractdocref) o;
        return transaccionContractdocrefPK.equals(that.transaccionContractdocrefPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transaccionContractdocrefPK);
    }
}
