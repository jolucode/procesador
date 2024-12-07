package service.cloud.request.clientrequest.handler.refactorPdf.service;

import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;

public interface BoletaPDFGenerator {
    byte[] generateBoletaPDF(UBLDocumentWRP boletaType, ConfigData configData);
}