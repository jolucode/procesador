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
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "TRANSACCION_GUIA_REMISION")
public class TransaccionGuias implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "FE_Id")
    private String fEId;

    @Column(name = "CodigoPuerto")
    private String codigoPuerto;

    @Column(name = "NumeroContenedor")
    private String numeroContenedor;

    @Column(name = "TipoDocConductor")
    private String tipoDocConductor;

    @Column(name = "DocumentoConductor")
    private String documentoConductor;

    @Column(name = "PlacaVehiculo")
    private String placaVehiculo;

    @Column(name = "RUCTransporista")
    private String rUCTransporista;

    @Column(name = "TipoDOCTransportista")
    private String tipoDOCTransportista;

    @Column(name = "NombreRazonTransportista")
    private String nombreRazonTransportista;

    @Column(name = "FechaInicioTraslado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicioTraslado;

    @Column(name = "ModalidadTraslado")
    private String modalidadTraslado;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "NumeroBultos")
    private BigDecimal numeroBultos;

    @Column(name = "UnidadMedida")
    private String unidadMedida;

    @Column(name = "Peso")
    private BigDecimal peso;

    @Column(name = "IndicadorTransbordoProgramado")
    private String indicadorTransbordoProgramado;

    @Column(name = "CodigoMotivo")
    private String codigoMotivo;

    @Column(name = "DescripcionMotivo")
    private String descripcionMotivo;

    @Column(name = "LicenciaConducir")
    private String licenciaConducir;

    @Column(name = "DireccionPartida")
    private String direccionPartida;

    @Column(name = "UbigeoPartida")
    private String ubigeoPartida;

    @OneToOne(optional = false)
    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    private Transaccion transaccion;

    @Override
    public String toString() {
        return "TransaccionGuias[ fEId=" + fEId + " ]";
    }

}
