/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "TRANSACCION_BAJA")
public class TransaccionBaja implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "fecha")
    private Long fecha;

    @Basic(optional = false)
    @Column(name = "ID")
    private long id;

    @Column(name = "serie")
    private String serie;

    @Override
    public String toString() {
        return "TransaccionBaja[ fecha=" + fecha + " ]";
    }

}
