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
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_ERROR")
public class TransaccionError implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "Docnum")
    private Integer docnum;

    @Column(name = "FE_FormSAP")
    private String fEFormSAP;

    @Id
    @Basic(optional = false)
    @Column(name = "FE_Id")
    private String fEId;

    @Column(name = "Docentry")
    private Integer docentry;

    @Column(name = "FE_ObjectType")
    private String fEObjectType;

    @Column(name = "FE_TipoTrans")
    private String fETipoTrans;

    @Column(name = "Err_Codigo")
    private Integer errCodigo;

    @Column(name = "Err_Mensaje")
    private String errMensaje;

    @Override
    public String toString() {
        return "TransaccionError[ fEId=" + fEId + " ]";
    }

}
