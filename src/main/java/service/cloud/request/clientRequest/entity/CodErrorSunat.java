/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @author joseLuis
 */
@Data
@Table(name = "COD_ERROR_SUNAT")
@Entity
@NamedQueries({
        @NamedQuery(name = "CodErrorSunat.findByIdCodError", query = "SELECT c FROM CodErrorSunat c WHERE c.idCodError = :idCodError"),
})
public class CodErrorSunat implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "idCodError")
    private String idCodError;

    @Column(name = "codDescription")
    private String codDescription;


}
