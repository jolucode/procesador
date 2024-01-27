/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

import static lombok.EqualsAndHashCode.Include;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Embeddable
public class TransaccionContractdocrefPK implements Serializable {

    @Include
    @Column(name = "FE_Id")
    private String fEId;

    @Include
    @Column(name = "USUCMP_Id")
    private int uSUCMPId;

    @Override
    public String toString() {
        return "TransaccionContractdocrefPK[ fEId=" + fEId + ", uSUCMPId=" + uSUCMPId + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransaccionContractdocrefPK that = (TransaccionContractdocrefPK) o;
        return uSUCMPId == that.uSUCMPId && fEId.equals(that.fEId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fEId, uSUCMPId);
    }
}
