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
import java.util.Date;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "LOG_TRANS")
public class LogTrans implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "Keylog")
    private Integer keylog;

    @Column(name = "Doc_Id")
    private String docId;

    @Lob
    @Column(name = "TramaEnvio")
    private String tramaEnvio;

    @Column(name = "Fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @Column(name = "Hora")
    private Integer hora;

    @Lob
    @Column(name = "TramaRespuesta")
    private String tramaRespuesta;

    @Column(name = "Proceso")
    private String proceso;

    @Column(name = "Tarea")
    private String tarea;

    @Column(name = "Conector")
    private String conector;

    @Override
    public String toString() {
        return "LogTrans[ keylog=" + keylog + " ]";
    }

}
