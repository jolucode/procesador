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
@Table(name = "REGLAS_IDIOMAS_DOC")
public class ReglasIdiomasDoc implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "FEKey")
    private Integer fEKey;

    @Column(name = "FETipoDOC")
    private String fETipoDOC;

    @Column(name = "FECampoDOC")
    private String fECampoDOC;

    @Column(name = "FEOperador")
    private String fEOperador;

    @Column(name = "FEValorComparativo")
    private String fEValorComparativo;

    @Column(name = "FEDOCFinal")
    private String fEDOCFinal;

    @Override
    public String toString() {
        return "ReglasIdiomasDoc[ fEKey=" + fEKey + " ]";
    }

}
