package service.cloud.request.clientRequest.service.core;

import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.Transaccion;

import java.io.IOException;
import java.util.Optional;

public interface DocumentFormatInterface {

    public byte[] createPDFDocument(Object ublDocument, String documentCode, UBLDocumentWRP wrp, Transaccion transaction, ConfigData configuracion);

    public Optional<byte[]> unzipResponse(byte[] cdr) throws IOException;

    public String cargarAnalizarReglasFormatoDOC(Transaccion transaction, String documentCode);
}
