package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;

import java.io.File;
import java.io.IOException;

public interface ProcessorCoreInterface {

  public byte[] processCDRResponseContigencia(byte[] cdrConstancy, File signedDocument,
                                              FileHandler fileHandler, String documentName,
                                              String documentCode, UBLDocumentWRP documentWRP,
                                              TransacctionDTO transaccion, ConfigData configuracion);

  public TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument, UBLDocumentWRP documentWRP,
                                                   TransacctionDTO transaction, ConfigData configuracion) throws IOException;

  public TransaccionRespuesta processResponseSinCDR(TransacctionDTO transaction);


  public TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, TransacctionDTO transaction, String sunatType);

  public TransaccionRespuesta processResponseService(TransacctionDTO transaction, CdrStatusResponse response);

}
