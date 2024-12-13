package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;

public interface ProcessorCoreInterface {

    TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument, UBLDocumentWRP documentWRP, TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception;

    TransaccionRespuesta processResponseSinCDR(TransacctionDTO transaction, FileResponseDTO cdrStatusResponse);
}
