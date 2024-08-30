package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.CertificateUtils;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.ValidationHandler;
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
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class ServiceBaja implements IServiceBaja {

    Logger logger = LoggerFactory.getLogger(ServiceBaja.class);

    @Autowired
    ClientProperties clientProperties;

    @Autowired
    ApplicationProperties applicationProperties;

    private final String docUUID = "123123";

    @Autowired
    DocumentFormatInterface documentFormatInterface;


    @Override
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception {

        if (transaction.getFE_Comentario().isEmpty()) {
            TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
            transactionResponse.setMensaje("Ingresar razón de anulación, y colocar APROBADO y volver a consultar");
            return transactionResponse;
        }

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation2(transaction.getANTICIPO_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision());

        byte[] certificado = CertificateUtils.getCertificateInBytes(applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator
                + client.getCertificadoName());
        String certiPassword = client.getCertificadoPassword();
        String ksProvider = client.getCertificadoProveedor();
        String ksType = client.getCertificadoTipoKeystore();

        /**validar certificado*/
        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        /** Generando el objeto VoidedDocumentsType para la COMUNICACION DE BAJA */
        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);

        SummaryDocumentsType summaryVoidedDocumentType = null;
        VoidedDocumentsType voidedDocumentType = null;


        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        /** Se genera el nombre del documento de tipo COMUNICACION DE BAJA*/
        String documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
        String documentPath = null;

        Calendar fecha = Calendar.getInstance();
        fecha.setTime(transaction.getDOC_FechaEmision());
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);


        /**Setear la ruta del directorio*/
        String attachmentPath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() +
                File.separator + "anexo" + File.separator + anio + File.separator + mes + File.separator + dia + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
        fileHandler.setBaseDirectory(attachmentPath);
        if (transaction.getDOC_Serie().startsWith("B")) {
            documentName = documentName.replace("RA", "RC");
            summaryVoidedDocumentType = ublHandler.generateSummaryDocumentsTypeV2(transaction, signerName);
            /**Guardando el documento UBL en DISCO*/
            documentPath = fileHandler.storeDocumentInDisk(summaryVoidedDocumentType, documentName);
            logger.info("Documento XML guardado en disco : " + documentPath);
        } else {
            voidedDocumentType = ublHandler.generateVoidedDocumentType(transaction, signerName);
            /**Guardando el documento UBL en DISCO*/
            documentPath = fileHandler.storeDocumentInDisk(voidedDocumentType, documentName);
            logger.info("Documento XML guardado en disco : " + documentPath);
        }
        LoggerTrans.getCDThreadLogger().log(Level.INFO, "[" + this.docUUID + "] Se genero el objeto VoidedDocumentsType de la COMUNICACION DE BAJA.");

        if (logger.isDebugEnabled()) {
            logger.debug("transactionVoidedDocument() [" + this.docUUID + "] Ruta para los archivos adjuntos: " + attachmentPath);
        }

        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);

        File signedDocument = signerHandler.signDocument(documentPath, docUUID);
        logger.info("Documento XML firmado correctamente");

        DataHandler zipDocument = fileHandler.compressUBLDocument(signedDocument, documentName, transaction.getSN_DocIdentidad_Nro(), transaction.getDocIdentidad_Nro());
        if (logger.isInfoEnabled()) {
            logger.info("transactionVoidedDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP.");
        }

        ConfigData configuracion = ConfigData
                .builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .mostrarSoap(client.getMostrarSoap())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDoc())
                .build();

        logger.info("Se esta apuntando al ambiente : " + configuracion.getAmbiente() + " - " + configuracion.getIntegracionWs());
        if (configuracion.getIntegracionWs().equals("OSE"))
            logger.info("Url Service: " + applicationProperties.getUrlOse());
        else if (configuracion.getIntegracionWs().equals("SUNAT"))
            logger.info("Url Service: " + applicationProperties.getUrlSunat());
        logger.info("Usuario Sol: " + configuracion.getUsuarioSol());
        logger.info("Clave Sol: " + configuracion.getClaveSol());

        if (null != zipDocument) {
            LoggerTrans.getCDThreadLogger().log(Level.INFO, "[" + this.docUUID + "] Enviando WS sendSummary.");


            try {
                /**Generacion ticket en caso no este se genera uno nuevo*/
                String ticket = "";
                if (transaction.getTicket_Baja() == null || transaction.getTicket_Baja().isEmpty()) {
                    //ticket = wsConsumer.sendSummary(zipDocument, documentName, configuracion);
                    //transaction.getTicket_Baja(ticket);
                    //transaccionRepository.save(transaction);
                } else {
                    ticket = transaction.getTicket_Baja();
                }

                /**Enviamos ticket a Sunat*/
                if (configuracion.getIntegracionWs().equals("OSE")) {
                    //WSConsumer oseConsumer = WSConsumer.newInstance(transaction.getFE_Id());
                    //oseConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion);

                    /**envia ticket a sunat para consultar zip*/
                    //service.cloud.request.clientRequest.proxy.ose.object.StatusResponse response = oseConsumer.getStatus(ticket, configuracion);
                    //if (response.getContent() != null) {
                    transactionResponse = null;//processOseResponseBAJA(response.getContent(), transaction, fileHandler, documentName, configuracion);
                    // }
                }
                transactionResponse.setTicketRest(ticket);
                transactionResponse.setIdentificador(documentName);
            } catch (SOAPFaultException e) {
                ObjectMapper objectMapper = new ObjectMapper();
                ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
                transactionResponse.setMensaje(exceptionProxy.getDescripcion());
            }
        }

        return transactionResponse;
    }


    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, FileHandler fileHandler, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            /**se realiza el anexo del documento de baja*/
            if (null != statusResponse && 0 < statusResponse.length) {
                fileHandler.storePDFDocumentInDisk(statusResponse, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
            }

            LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] El documento [{1}] fue APROBADO por SUNAT.", new Object[]{this.docUUID, documentName});
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);

        } else {
            LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] El documento [{1}] fue RECHAZADO por SUNAT.", new Object[]{this.docUUID, documentName});
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);
        }

        return transactionResponse;
    }

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
                logger.info(descripcionRespuesta);
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
