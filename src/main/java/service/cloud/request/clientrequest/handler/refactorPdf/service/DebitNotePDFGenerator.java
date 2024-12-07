package service.cloud.request.clientrequest.handler.refactorPdf.service;

import service.cloud.request.clientrequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;

import java.util.List;

public interface DebitNotePDFGenerator {
    byte[] generateDebitNotePDF(UBLDocumentWRP debitNoteType, List<TransactionTotalesDTO> transactionTotalList, ConfigData configData);
}
