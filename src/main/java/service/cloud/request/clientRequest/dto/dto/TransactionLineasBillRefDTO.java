package service.cloud.request.clientRequest.dto.dto;

import lombok.Data;


@Data
public class TransactionLineasBillRefDTO {


    private Integer LineId;

    private Integer NroOrden;

    private String AdtDocRef_Id;

    private String AdtDocRef_SchemaId;

    private String InvDocRef_DocTypeCode;

    private String InvDocRef_Id;

}
