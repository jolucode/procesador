package service.cloud.request.clientRequest.handler.refactorPdf.service;

import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;

public interface BoletaPDFGenerator {
    byte[] generateBoletaPDF(UBLDocumentWRP boletaType, ConfigData configData);
}