package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.handler.FileHandler;

import java.io.File;
import java.io.IOException;

public interface ProcessorCoreInterface {

  public byte[] processCDRResponseContigencia(byte[] cdrConstancy, File signedDocument,
                                              FileHandler fileHandler, String documentName,
                                              String documentCode, UBLDocumentWRP documentWRP,
                                              Transaccion transaccion, ConfigData configuracion);

  public TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument, UBLDocumentWRP documentWRP,
                                                   Transaccion transaction, ConfigData configuracion) throws IOException;

  public TransaccionRespuesta processResponseSinCDR(Transaccion transaction);


  public TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, Transaccion transaction, String sunatType);

  public TransaccionRespuesta processResponseService(Transaccion transaction, Response response);

}
