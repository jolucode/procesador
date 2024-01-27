/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Embeddable
public class TransaccionLineaImpuestosPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "FE_Id")
    private String fEId;

    @Basic(optional = false)
    @Column(name = "NroOrden")
    private int nroOrden;

    @Basic(optional = false)
    @Column(name = "LineId")
    private int lineId;

    @Override
    public String toString() {
        return "TransaccionLineaImpuestosPK[ fEId=" + fEId + ", nroOrden=" + nroOrden + ", lineId=" + lineId + " ]";
    }

}
