package service.cloud.request.clientRequest.service.core;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.applicationresponse_2.ApplicationResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.DocumentResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.StatusType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.DescriptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.ResponseCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public TransaccionRespuesta processResponseSinCDR(TransacctionDTO transaction, CdrStatusResponse cdrStatusResponse) {
        if (logger.isDebugEnabled()) {
            logger.debug("+processResponseSinResponse() [" + this.docUUID + "]");
        }
        TransaccionRespuesta transactionResponse = null;
        transactionResponse = new TransaccionRespuesta();
        transactionResponse.setCodigo(TransaccionRespuesta.RQT_EMITIDO_EXCEPTION);
        transactionResponse.setMensaje(cdrStatusResponse.getStatusMessage());

        logger.warn("Respuesta del servicio invocado: " + cdrStatusResponse.getStatusMessage());

        if (logger.isDebugEnabled()) {
            logger.debug("-processCDRResponse() [" + this.docUUID + "]");
        }
        return transactionResponse;
    }



}
