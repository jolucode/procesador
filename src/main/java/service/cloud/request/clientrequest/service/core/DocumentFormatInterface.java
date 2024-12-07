package service.cloud.request.clientrequest.service.core;

import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;

import java.io.IOException;
import java.util.Optional;

public interface DocumentFormatInterface {

    byte[] createPDFDocument(UBLDocumentWRP wrp, TransacctionDTO transaction, ConfigData configuracion);

    Optional<byte[]> unzipResponse(byte[] cdr) throws IOException;

}
