package service.cloud.request.clientrequest.service.core;

import service.cloud.request.clientrequest.dto.TransaccionRespuesta;
import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientrequest.handler.FileHandler;
import service.cloud.request.clientrequest.proxy.model.CdrStatusResponse;

import java.io.File;

public interface ProcessorCoreInterface {

    byte[] processCDRResponseContigencia(byte[] cdrConstancy, File signedDocument, FileHandler fileHandler, String documentName, String documentCode, UBLDocumentWRP documentWRP, TransacctionDTO transaccion, ConfigData configuracion);

    TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument, UBLDocumentWRP documentWRP, TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception;

    TransaccionRespuesta processResponseSinCDR(TransacctionDTO transaction, CdrStatusResponse cdrStatusResponse);


    TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, TransacctionDTO transaction, String sunatType);


}
