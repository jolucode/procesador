package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class TransactionLineasDTO {
    private String IndicadorBien;
    private BigDecimal ValorCargaEfectiva;
    private BigDecimal DSCTO_Monto;
    private BigDecimal TotalLineaConIGV;
    private String CodUbigeoDestino;
    private String CodProdGS1;
    private String NombreEmbarcacion;
    private String NumeroSerie;
    private BigDecimal LineaImpuesto;
    private BigDecimal CantidadEspecieVendida;
    private String Numeracion;
    private String Descripcion;
    private String CodArticulo;
    private BigDecimal TotalLineaSinIGV;
    private String ConfVehicular;
    private BigDecimal CEfectivaVehiculo;
    private BigDecimal ValorPreRef;
    private BigDecimal PrecioRef_Monto;
    private BigDecimal ValorRefTM;
    private String LugarDescarga;
    private String PrecioRef_Codigo;
    private BigDecimal CUtilVehiculo;
    private Integer NroOrden;
    private BigDecimal PrecioDscto;
    private String UnidadSunat;
    private String CodSunat;
    private String DirecOrigen;
    private String FactorRetorno;
    private String TipoEspecieVendida;
    private String DirecDestino;
    private String SubPartida;
    private String CodUbigeoOrigen;
    private BigDecimal TotalBruto;
    private BigDecimal ValorTransporte;
    private String DetalleViaje;
    private String ItmBolsa;
    private BigDecimal Cantidad;
    private BigDecimal ValorCargaUtil;
    private BigDecimal DSCTO_Porcentaje;
    private BigDecimal PrecioIGV;
    private String Unidad;
    private String U_TotalLineaConIGV;
    private String U_Cantidad;
    private String U_CodArticulo;
    private String U_Unidad;
    private String U_TotalLineaSinIGV;
    private String U_PrecioSinIGV;
    private String U_Descripcion;
    private String U_pmp_CodFabricante;
    private String U_PrecioIGV;
    private String U_pmp_NumSerie;
    private String U_TipoAfectacion;
    private String U_DSCTO_Monto;
    private Date FechaDescarga;
    List<TransactionLineasImpuestoDTO> transactionLineasImpuestoListDTO;
    List<TransactionLineasBillRefDTO> transaccionLineasBillrefListDTO;
    private Map<String, String> transaccionLineasCamposUsuario;
}