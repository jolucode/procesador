/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Yosmel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "TRANSACCION_LINEAS")
public class TransaccionLineas implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull
    @EmbeddedId
    protected TransaccionLineasPK transaccionLineasPK;

    @Column(name = "CodSunat")
    private String codSunat;

    @Column(name = "CodProdGS1")
    private String codProdGS1;

    @Column(name = "CodUbigeoOrigen")
    private String codUbigeoOrigen;

    @Column(name = "DirecOrigen")
    private String direcOrigen;

    @Column(name = "CodUbigeoDestino")
    private String codUbigeoDestino;

    @Column(name = "DirecDestino")
    private String direcDestino;

    @Column(name = "DetalleViaje")
    private String detalleViaje;

    @Column(name = "ValorTransporte")
    private BigDecimal valorTransporte;

    @Column(name = "ValorCargaEfectiva")
    private BigDecimal valorCargaEfectiva;

    @Column(name = "ValorCargaUtil")
    private BigDecimal valorCargaUtil;

    @Column(name = "ConfVehicular")
    private String confVehicular;

    @Column(name = "CUtilVehiculo")
    private BigDecimal cUtilVehiculo;

    @Column(name = "CEfectivaVehiculo")
    private BigDecimal cEfectivaVehiculo;

    @Column(name = "ValorRefTM")
    private BigDecimal valorRefTM;

    @Column(name = "NombreEmbarcacion")
    private String nombreEmbarcacion;

    @Column(name = "TipoEspecieVendida")
    private String tipoEspeciaVendida;

    @Column(name = "LugarDescarga")
    private String lugarDescarga;

    @Column(name = "FechaDescarga")
    private Date fechaDescarga;

    @Column(name = "CantidadEspecieVendida")
    private BigDecimal cantidadEspecieVendida;

    @Column(name = "ValorPreRef")
    private BigDecimal valorPreRef;

    @Column(name = "FactorRetorno")
    private String factorRetorno;

    @Column(name = "TotalLineaConIGV")
    private BigDecimal totalLineaConIGV;

    @Column(name = "UnidadSunat")
    private String unidadSunat;

    @Column(name = "CodArticulo")
    private String codArticulo;

    @Column(name = "PrecioIGV")
    private BigDecimal precioIGV;

    @Column(name = "PrecioDscto")
    private BigDecimal precioDscto;

    @Column(name = "TotalLineaSinIGV")
    private BigDecimal totalLineaSinIGV;

    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "DSCTO_Porcentaje")
    private BigDecimal dSCTOPorcentaje;

    @Column(name = "DSCTO_Monto")
    private BigDecimal dSCTOMonto;

    @Column(name = "Descripcion")
    private String descripcion;

    @Column(name = "PrecioRef_Monto")
    private BigDecimal precioRefMonto;

    @Column(name = "PrecioRef_Codigo")
    private String precioRefCodigo;

    @Column(name = "Cantidad")
    private BigDecimal cantidad;

    @Column(name = "Unidad")
    private String unidad;

    @Column(name = "ItmBolsa")
    private Character itemBolsa;

    @Column(name = "TotalBruto")
    private BigDecimal totalBruto;

    @Column(name = "LineaImpuesto")
    private BigDecimal lineaImpuesto;

    //GUIAS REST SUNAT
    @Column(name = "SubPartida")
    private String subPartida;

    @Column(name = "IndicadorBien")
    private String indicadorBien;

    @Column(name = "Numeracion")
    private String numeracion;

    @Column(name = "NumeroSerie")
    private String numeroSerie;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionLineas", fetch = FetchType.EAGER)
    private List<TransaccionLineasBillref> transaccionLineasBillrefList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionLineas", fetch = FetchType.EAGER)
    private List<TransaccionLineaImpuestos> transaccionLineaImpuestosList;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FE_Id", referencedColumnName = "FE_Id", insertable = false, updatable = false)
    private Transaccion transaccion;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccionLineas", fetch = FetchType.EAGER)
    private List<TransaccionLineasUsucampos> transaccionLineasUsucamposList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransaccionLineas that = (TransaccionLineas) o;
        return transaccionLineasPK.equals(that.transaccionLineasPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transaccionLineasPK);
    }

    @Override
    public String toString() {
        return "TransaccionLineas[ transaccionLineasPK=" + transaccionLineasPK + " ]";
    }
}
