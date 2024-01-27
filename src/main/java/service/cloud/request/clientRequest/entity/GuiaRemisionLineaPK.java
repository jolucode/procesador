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
 * @author VSUser
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Embeddable
public class GuiaRemisionLineaPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "ID")
    private String id;

    @Basic(optional = false)
    @Column(name = "ID_Linea")
    private int iDLinea;

    @Override
    public String toString() {
        return "GuiaRemisionLineaPK[ id=" + id + ", iDLinea=" + iDLinea + " ]";
    }

}
