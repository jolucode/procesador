package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class TransactionLineasImpuestoDTO {

    private int LineId;

    private String NroOrden;

    private String Nombre;

    private BigDecimal ValorVenta;

    private String Abreviatura;

    private String Codigo;

    private String Moneda;

    private BigDecimal Monto;

    private BigDecimal Porcentaje;

    private String TipoTributo;

    private String TipoAfectacion;

    private String TierRange;
}
