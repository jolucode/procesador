package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;

import java.io.IOException;
import java.util.Optional;

public interface DocumentFormatInterface {

    byte[] createPDFDocument(UBLDocumentWRP wrp, TransacctionDTO transaction, ConfigData configuracion);

    Optional<byte[]> unzipResponse(byte[] cdr) throws IOException;

}
