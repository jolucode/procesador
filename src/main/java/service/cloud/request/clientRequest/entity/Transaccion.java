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
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "TRANSACCION")
public class Transaccion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    public static List<TransaccionUsucampos> propiedades = new ArrayList<>();

    @Id
    @NonNull
    @Column(name = "FE_Id")
    private String FE_Id;

    @Column(name = "TipoOperacionSunat")
    private String TipoOperacionSunat;

    @Column(name = "SN_DIR_Pais_Descripcion")
    private String SN_DIR_Pais_Descripcion;

    @Column(name = "FechaDOCRef")
    private String FechaDOCRef;

    @Column(name = "FechaVenDOCRef")
    private String FechaVenDOCRef;

    @Column(name = "DocIdentidad_Nro")
    private String DocIdentidad_Nro;

    @Column(name = "DocIdentidad_Tipo")
    private String DocIdentidad_Tipo;

    @Column(name = "RazonSocial")
    private String RazonSocial;

    @Column(name = "NombreComercial", columnDefinition = "TEXT")
    private String NombreComercial;

    @Column(name = "PersonContacto", columnDefinition = "TEXT")
    private String PersonContacto;

    @Column(name = "EMail", columnDefinition = "TEXT")
    private String EMail;

    @Column(name = "Telefono")
    private String Telefono;

    @Column(name = "Telefono_1")
    private String Telefono_1;

    @Column(name = "Web")
    private String Web;

    @Column(name = "DIR_Pais")
    private String DIR_Pais;

    @Column(name = "DIR_Departamento")
    private String DIR_Departamento;

    @Column(name = "DIR_Provincia")
    private String DIR_Provincia;

    @Column(name = "DIR_Distrito")
    private String DIR_Distrito;

    @Column(name = "DIR_Direccion", columnDefinition = "TEXT")
    private String DIR_Direccion;

    @Column(name = "DIR_NomCalle")
    private String DIR_NomCalle;

    @Column(name = "DIR_NroCasa")
    private String DIR_NroCasa;

    @Column(name = "DIR_Ubigeo")
    private String DIR_Ubigeo;

    @Column(name = "DIR_Urbanizacion")
    private String DIR_Urbanizacion;

    @Column(name = "SN_DocIdentidad_Nro")
    private String SN_DocIdentidad_Nro;

    @Column(name = "SN_DocIdentidad_Tipo")
    private String SN_DocIdentidad_Tipo;

    @Column(name = "SN_RazonSocial")
    private String SN_RazonSocial;

    @Column(name = "SN_NombreComercial", columnDefinition = "TEXT")
    private String SN_NombreComercial;

    @Column(name = "SN_EMail", columnDefinition = "TEXT")
    private String SN_EMail;

    @Column(name = "SN_EMail_Secundario", columnDefinition = "TEXT")
    private String SN_EMail_Secundario;

    @Column(name = "SN_SegundoNombre", columnDefinition = "TEXT")
    private String SN_SegundoNombre;

    @Column(name = "SN_DIR_Pais")
    private String SN_DIR_Pais;

    @Column(name = "SN_DIR_Departamento")
    private String SN_DIR_Departamento;

    @Column(name = "SN_DIR_Provincia")
    private String SN_DIR_Provincia;

    @Column(name = "SN_DIR_Distrito")
    private String SN_DIR_Distrito;

    @Column(name = "SN_DIR_Direccion", columnDefinition = "TEXT")
    private String SN_DIR_Direccion;

    @Column(name = "SN_DIR_NomCalle")
    private String SN_DIR_NomCalle;

    @Column(name = "SN_DIR_NroCasa")
    private String SN_DIR_NroCasa;

    @Column(name = "SN_DIR_Ubigeo")
    private String SN_DIR_Ubigeo;

    @Column(name = "SN_DIR_Urbanizacion")
    private String SN_DIR_Urbanizacion;

    @Column(name = "DOC_Serie")
    private String DOC_Serie;

    @Column(name = "DOC_Numero")
    private String DOC_Numero;

    @Column(name = "DOC_Id")
    private String DOC_Id;

    @Column(name = "DOC_FechaEmision")
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date DOC_FechaEmision;

    @Column(name = "DOC_FechaVencimiento")
    @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date DOC_FechaVencimiento;

    @Column(name = "DOC_Dscrpcion")
    private String DOC_Dscrpcion;

    @Column(name = "DOC_Codigo")
    private String DOC_Codigo;

    @Column(name = "DOC_MON_Nombre", columnDefinition = "TEXT")
    private String DOC_MON_Nombre;

    @Column(name = "DOC_MON_Codigo")
    private String DOC_MON_Codigo;

    @Column(name = "DOC_Descuento")
    private BigDecimal DOC_Descuento;

    @Column(name = "DOC_PorDescuento")
    private BigDecimal DOC_PorDescuento;

    @Column(name = "DOC_MontoTotal")
    private BigDecimal DOC_MontoTotal;

    @Column(name = "DOC_DescuentoTotal")
    private BigDecimal DOC_DescuentoTotal;

    @Column(name = "DOC_Importe")
    private BigDecimal DOC_Importe;

    @Column(name = "DOC_ImporteTotal")
    private BigDecimal DOC_ImporteTotal;

    @Column(name = "ImporteDOCRef")
    private BigDecimal ImporteDOCRef;

    @Column(name = "DOC_MonPercepcion")
    private BigDecimal DOC_MonPercepcion;

    @Column(name = "DOC_PorPercepcion")
    private BigDecimal DOC_PorPercepcion;

    @Column(name = "MontoRetencion")
    private BigDecimal MontoRetencion;

    @Column(name = "DOC_PorcImpuesto")
    private String DOC_PorcImpuesto;

    @Column(name = "ANTICIPO_Id")
    private String ANTICIPO_Id;

    @Column(name = "ANTICIPO_Tipo")
    private String ANTICIPO_Tipo;

    @Column(name = "ANTICIPO_Monto")
    private BigDecimal ANTICIPO_Monto = new BigDecimal("0.0");

    @Column(name = "ANTCIPO_Tipo_Doc_ID")
    private String ANTCIPO_Tipo_Doc_ID;

    @Column(name = "ANTICIPO_Nro_Doc_ID")
    private String ANTICIPO_Nro_Doc_ID;

    @Column(name = "SUNAT_Transact")
    private String SUNAT_Transact;

    @Column(name = "FE_DocEntry")
    private Integer FE_DocEntry;

    @Column(name = "FE_ObjectType")
    private String FE_ObjectType;

    @Column(name = "FE_Estado")
    private String FE_Estado;

    //@NotNull(message = "El campo tipo de transacci\u00fen no debe ser nula")
    @Column(name = "FE_TipoTrans")
    private String FE_TipoTrans;

    @Column(name = "FE_DocNum")
    private Integer FE_DocNum;

    @Column(name = "FE_FormSAP")
    private String FE_FormSAP;

    @Column(name = "REFDOC_Tipo")
    private String REFDOC_Tipo;

    @Column(name = "REFDOC_Id")
    private String REFDOC_Id;

    @Column(name = "REFDOC_MotivCode")
    private String REFDOC_MotivCode;

    @Column(name = "REFDOC_MotivDesc")
    private String REFDOC_MotivDesc;

    @Column(name = "FE_Comentario")
    private String FE_Comentario;

    @Column(name = "FE_ErrCod")
    private String FE_ErrCod;

    @Column(name = "FE_ErrMsj")
    private String FE_ErrMsj;

    @Column(name = "FE_Errores")
    private Integer FE_Errores;

    @Column(name = "FE_MaxSalto")
    private Integer FE_MaxSalto;

    @Column(name = "FE_Saltos")
    private Integer FE_Saltos;

    @Column(name = "DOC_CondPago")
    private String DOC_CondPago;

    @Column(name = "RET_Regimen")
    private String RET_Regimen;

    @Column(name = "RET_Tasa")
    private String RET_Tasa;

    @Column(name = "Observaciones")
    private String Observaciones;

    @Column(name = "ImportePagado")
    private BigDecimal ImportePagado;

    @Column(name = "MonedaPagado")
    private String MonedaPagado;

    @Column(name = "DOC_OtrosCargos")
    private BigDecimal DOC_OtrosCargos;

    @Column(name = "DOC_SinPercepcion")
    private BigDecimal DOC_SinPercepcion;

    @Column(name = "DOC_ImpuestoTotal")
    private BigDecimal DOC_ImpuestoTotal;

    @Column(name = "CuentaDetraccion")
    private String CuentaDetraccion;

    @Column(name = "CodigoDetraccion")
    private String CodigoDetraccion = "";

    @Column(name = "PorcDetraccion")
    private BigDecimal PorcDetraccion;

    @Column(name = "MontoDetraccion")
    private BigDecimal MontoDetraccion = new BigDecimal("0.0");

    @Column(name = "CodigoPago")
    private String CodigoPago;

    //@NotNull(message = "La clave de la sociedad no puede ser nula.")
    @Column(name = "key_sociedad")
    private String key_sociedad;

    @Column(name = "TicketBaja")
    private String TicketBaja;


    @Transient
    private String dbName;

    @Transient
    private Boolean isPdfBorrador;

    //@Transient
    //private String pdfAdicional;


    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionAnticipo> transaccionAnticipoList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionImpuestos> transaccionImpuestosList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionLineas>     transaccionLineasList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionCuotas> transaccionCuotas ;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionDocrefers> transaccionDocrefersList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionPropiedades> transaccionPropiedadesList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionComprobantePago> transaccionComprobantePagoList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionContractdocref> transaccionContractdocrefList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<TransaccionTotales> transaccionTotalesList;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaccion", orphanRemoval = true)
    private List<TransaccionUsucampos> transaccionUsucamposList;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "transaccion")
    private TransaccionGuiaRemision transaccionGuiaRemision;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "transaccion")

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaccion that = (Transaccion) o;
        return Objects.equals(FE_Id, that.FE_Id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(FE_Id);
    }


}
