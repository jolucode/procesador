package service.cloud.request.clientrequest.dto.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionTotalesDTO {
    private BigDecimal Monto;
    private String Id;
    private BigDecimal Prcnt;
}
