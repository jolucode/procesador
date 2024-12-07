package service.cloud.request.clientrequest.handler.refactorPdf.service;

import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;

public interface RetentionPDFGenerator {
    byte[] generateRetentionPDF(UBLDocumentWRP retentionType, ConfigData configData);
}
