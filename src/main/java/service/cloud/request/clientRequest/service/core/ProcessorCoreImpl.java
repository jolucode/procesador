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
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.utils.Constants;
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
    public TransaccionRespuesta processCDRResponseV2(byte[] cdrConstancy, byte[] signedDocument,
                                                     UBLDocumentWRP documentWRP,
                                                     TransacctionDTO transaction, ConfigData configuracion, String documentName, String attachmentPath) throws Exception {

        TransaccionRespuesta transactionResponse = null;
        TransaccionRespuesta.Sunat sunatResponse = proccessResponse(cdrConstancy, transaction, configuracion.getIntegracionWs());

        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {
            byte[] pdfBytes = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
            transactionResponse = new TransaccionRespuesta();
            transactionResponse.setCodigo(TransaccionRespuesta.RQT_EMITDO_ESPERA);

            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setSunat(sunatResponse);
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(cdrConstancy);
            transactionResponse.setPdf(pdfBytes);
        } else {
            //documento rechazado
            transactionResponse.setXml(signedDocument);
            transactionResponse.setZip(cdrConstancy);
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

    @Override
    public byte[] processCDRResponseContigencia(byte[] cdrConstancy, File signedDocument, FileHandler fileHandler,
                                                String documentName, String documentCode, UBLDocumentWRP documentWRP, TransacctionDTO
                                                        transaccion, ConfigData configuracion) {
        byte[] pdfBytes = null;

        if (logger.isDebugEnabled()) {
            logger.debug("+processCDRResponse() [" + this.docUUID + "]");
        }

        try {
            pdfBytes = documentFormatInterface.createPDFDocument(documentWRP, transaccion, configuracion);

            if (null != pdfBytes && 0 < pdfBytes.length) {
                if (logger.isDebugEnabled()) {
                    logger.debug("processCDRResponse() [" + this.docUUID + "] Si existe PDF en bytes.");
                }
                /*
                 * Guardar el PDF en DISCO
                 */
                boolean isPDFOk = fileHandler.storePDFContigenciaDocumentInDisk(pdfBytes, documentName, transaccion.getSN_DocIdentidad_Nro(), transaccion.getDocIdentidad_Nro());
                logger.info("processCDRResponse() [" + this.docUUID + "] El documento PDF fue almacenado en DICO: " + isPDFOk);
            } else {
                logger.error("processCDRResponse() [" + this.docUUID + "] " + IVenturaError.ERROR_461.getMessage());
            }

        } catch (Exception e) {
            logger.error("processCDRResponse() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("processCDRResponse() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-processCDRResponse() [" + this.docUUID + "]");
        }

        return pdfBytes;

    }

    @Override
    public TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, TransacctionDTO transaction, String
            sunatType) {
        try {
            String descripcionRespuesta = "";
            Optional<byte[]> unzipedResponse = documentFormatInterface.unzipResponse(cdrConstancy);
            int codigoObservacion = 0;
            int codigoRespuesta = 0;
            String identificador = Constants.IDENTIFICATORID_OSE;
            if (unzipedResponse.isPresent()) {
                StringBuilder descripcion = new StringBuilder();
                JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationResponseType.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ApplicationResponseType> jaxbElement = unmarshaller.unmarshal(new ByteArraySource(unzipedResponse.get()), ApplicationResponseType.class);
                ApplicationResponseType applicationResponse = jaxbElement.getValue();
                List<DocumentResponseType> documentResponse = applicationResponse.getDocumentResponse();
                List<TransaccionRespuesta.Observacion> observaciones = new ArrayList<>();
                for (DocumentResponseType documentResponseType : documentResponse) {
                    ResponseType response = documentResponseType.getResponse();
                    ResponseCodeType responseCode = response.getResponseCode();
                    codigoRespuesta = Optional.ofNullable(responseCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                    List<DescriptionType> descriptions = response.getDescription();
                    for (DescriptionType description : descriptions) {
                        descripcion.append(description.getValue());
                    }
                    if (sunatType.equalsIgnoreCase(Constants.IDENTIFICATORID_OSE)) { //cambio aqui NUMA
                        identificador = documentResponseType.getDocumentReference().getID().getValue();
                    } else {
                        identificador = documentResponseType.getResponse().getReferenceID().getValue();
                    }
                    List<StatusType> statusTypes = response.getStatus();
                    for (StatusType statusType : statusTypes) {
                        List<StatusReasonType> statusReason = statusType.getStatusReason();
                        String mensajes = statusReason.parallelStream().map(TextType::getValue).collect(Collectors.joining("\n"));
                        StatusReasonCodeType statusReasonCode = statusType.getStatusReasonCode();
                        codigoObservacion = Optional.ofNullable(statusReasonCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                        TransaccionRespuesta.Observacion observacion = new TransaccionRespuesta.Observacion();
                        observacion.setCodObservacion(codigoObservacion);
                        observacion.setMsjObservacion(mensajes);
                        observaciones.add(observacion);
                    }
                }
                descripcionRespuesta = descripcion.toString();
                TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
                sunatResponse.setListaObs(observaciones);
                sunatResponse.setId(identificador);
                sunatResponse.setCodigo(codigoRespuesta);
                sunatResponse.setMensaje(descripcionRespuesta);
                sunatResponse.setEmisor(transaction.getDocIdentidad_Nro());
                return sunatResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TransaccionRespuesta.Sunat();
    }
}
