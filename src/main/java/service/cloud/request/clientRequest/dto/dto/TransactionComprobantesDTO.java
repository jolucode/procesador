package service.cloud.request.clientRequest.dto.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionComprobantesDTO {
    private BigDecimal CP_ImporteTotal;
    private String U_TipoMonedaTotal;
    private String CP_MonedaMontoNeto;
    private String TC_MonedaObj;
    private String CP_Moneda;
    private BigDecimal U_TC_Factor;
    private Integer NroOrden;
    private String U_TipoMoneda;
    private BigDecimal U_PagoImporteSR;
    private BigDecimal CP_Importe;
    private BigDecimal U_Importe_Pago_Soles;
    private String U_DOC_Tipo;
    private String DOC_Numero;
    private String DOC_FechaEmision;
    private BigDecimal DOC_Importe;
    private String U_DOC_Moneda;
    private String PagoFecha;
    private String PagoNumero;
    private String PagoMoneda;
    private String U_CP_Fecha;
    private String TC_MonedaRef;
    private String TC_Fecha;
    private String U_TipoComprobante;
}
