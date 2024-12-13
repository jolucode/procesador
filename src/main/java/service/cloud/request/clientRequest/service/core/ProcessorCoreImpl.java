package service.cloud.request.clientRequest.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

@Service
public class ProcessorCoreImpl implements ProcessorCoreInterface {

    Logger logger = LoggerFactory.getLogger(ProcessorCoreImpl.class);

    private final String docUUID = Constants.DOC_UUID;

    @Autowired
    DocumentFormatInterface documentFormatInterface;


    @Override
    public TransaccionRespuesta processCDRResponseV2(byte[] statusResponse, byte[] signedDocument,
                                                     UBLDocumentWRP documentWRP,
                                                     TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception {

        TransaccionRespuesta transactionResponse = null;
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(cdrConstancy, transaction, configuracion.getIntegracionWs());

        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {
            byte[] pdfBytes = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
            transactionResponse = new TransaccionRespuesta();
            transactionResponse.setCodigo(TransaccionRespuesta.RQT_EMITDO_ESPERA);

            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setSunat(sunatResponse);
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(statusResponse);
            transactionResponse.setPdf(pdfBytes);
        } else {
            //documento rechazado
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(statusResponse);
        }

        logger.info("Respuesta del servicio invocado: " + sunatResponse.getMensaje());

        saveAllFiles(transactionResponse, documentName, attachmentPath);
        return transactionResponse;
    } //processCDRResponse

    private void saveAllFiles(TransaccionRespuesta transactionResponse, String documentName, String attachmentPath) throws Exception {
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);

        fileHandler.storeDocumentInDisk(transactionResponse.getXml(), documentName, "xml");
        fileHandler.storeDocumentInDisk(transactionResponse.getPdf(), documentName, "pdf");
        fileHandler.storeDocumentInDisk(transactionResponse.getZip(), documentName, "zip");

    }

    @Override
    //interface nueva:
    public TransaccionRespuesta processResponseSinCDR(TransacctionDTO transaction, FileResponseDTO responseDTO) {
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        transactionResponse.setMensaje(responseDTO.getMessage());
        return transactionResponse;
    }


}
