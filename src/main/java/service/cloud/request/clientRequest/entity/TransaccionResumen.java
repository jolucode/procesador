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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_RESUMEN")
public class TransaccionResumen implements Serializable {

    @Id
    @Column(name = "Id_Transaccion")
    private String idTransaccion;

    @Column(name = "Numero_Ruc")
    private String numeroRuc;

    @Column(name = "DocIdentidad_Tipo")
    private String docIdentidadTipo;

    @Column(name = "RazonSocial")
    private String razonSocial;

    @Column(name = "NombreComercial")
    private String nombreComercial;

    @Column(name = "PersonContacto")
    private String personContacto;

    @Column(name = "EMail")
    private String eMail;

    @Column(name = "DIR_Pais")
    private String dIRPais;

    @Column(name = "DIR_Departamento")
    private String dIRDepartamento;

    @Column(name = "DIR_Provincia")
    private String dIRProvincia;

    @Column(name = "DIR_Distrito")
    private String dIRDistrito;

    @Lob
    @Column(name = "DIR_Direccion")
    private String dIRDireccion;

    @Column(name = "DIR_NomCalle")
    private String dIRNomCalle;

    @Column(name = "DIR_NroCasa")
    private String dIRNroCasa;

    @Column(name = "DIR_Ubigeo")
    private String dIRUbigeo;

    @Column(name = "Fecha_Emision")
    private String fechaEmision;

    @Column(name = "Fecha_Generacion")
    private String fechaGeneracion;

    @Column(name = "Estado")
    private String estado;

    @Column(name = "Numero_Ticket")
    private String numeroTicket;

    @Column(name = "key_sociedad")
    private String keySociedad;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionResumen")
    private List<TransaccionResumenLinea> transaccionResumenLineas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionResumen")
    private List<TransaccionResumenLineaAnexo> transaccionResumenLineaAnexos = new ArrayList<>();

    //@OneToOne(cascade = CascadeType.ALL, mappedBy = "transaccionResumen")
    //private TransaccionResumenLineaAnexo transaccionResumenLineaAnexo;

    @Override
    public String toString() {
        return "TransaccionResumen[ idTransaccion=" + idTransaccion + " ]";
    }
}
