package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.eclipse.persistence.internal.oxm.ByteArrayDataSource;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dao.TransaccionRepository;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.entity.TransaccionContractdocref;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.PDFBasicGenerateHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.proxy.object.StatusResponse;
import service.cloud.request.clientRequest.prueba.Client;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.ServiceInterface;
import service.cloud.request.clientRequest.utils.CertificateUtils;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.ws.WSConsumer;
import service.cloud.request.clientRequest.ws.WSConsumerConsult;
import service.cloud.request.clientRequest.ws.WSConsumerNew;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.applicationresponse_2.ApplicationResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.DocumentResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.StatusType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.DescriptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.ResponseCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.Detail;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServiceImpl implements ServiceInterface {


    Logger logger = LoggerFactory.getLogger(ServiceImpl.class);
    private final String docUUID = "123123";
    @Autowired
    ProcessorCoreInterface processorCoreInterface;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    ClientProperties clientProperties;

    @Autowired
    TransaccionRepository transaccionRepository;

    @Autowired
    DocumentFormatInterface documentFormatInterface;


    @Override
    public TransaccionRespuesta transactionVoidedDocument(Transaccion transaction, String doctype) throws Exception {

        if (transaction.getFE_Comentario().isEmpty()) {
            TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
            transactionResponse.setMensaje("Ingresar razón de anulación, y colocar APROBADO y volver a consultar");
            return transactionResponse;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("+transactionVoidedDocument() [" + this.docUUID + "] Identifier: " + transaction.getANTICIPO_Id() + " DocIdentidad_Nro: " + transaction.getDocIdentidad_Nro());
        }
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        /* Generando el nombre del firmante */

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation2(transaction.getANTICIPO_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision());

        byte[] certificado = CertificateUtils.getCertificateInBytes(applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator
                + client.getCertificadoName());
        String certiPassword = client.getCertificadoPassword();
        String ksProvider = client.getCertificadoProveedor();
        String ksType = client.getCertificadoTipoKeystore();


        if (logger.isDebugEnabled()) {
            logger.debug("transactionVoidedDocument() [" + this.docUUID + "] Certificado en bytes: " + certificado);
        }

        /**validar certificado*/
        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        /** Generando el objeto VoidedDocumentsType para la COMUNICACION DE BAJA */
        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);

        //
        SummaryDocumentsType summaryVoidedDocumentType = null;
        VoidedDocumentsType voidedDocumentType = null;


        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        /** Se genera el nombre del documento de tipo COMUNICACION DE BAJA*/
        String documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
        if (logger.isDebugEnabled()) {
            logger.debug("transactionVoidedDocument() [" + this.docUUID + "] El nombre del documento: " + documentName);
        }

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

        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);

        File signedDocument = signerHandler.signDocument(documentPath, docUUID);
        logger.info("Documento XML firmado correctamente");

        DataHandler zipDocument = fileHandler.compressUBLDocument(signedDocument, documentName, transaction.getSN_DocIdentidad_Nro(), transaction.getDocIdentidad_Nro());

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
            WSConsumerNew wsConsumer = WSConsumerNew.newInstance(this.docUUID);
            wsConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion, fileHandler, doctype);

            try {
                /**Generacion ticket en caso no este se genera uno nuevo*/
                String ticket = "";
                if (transaction.getTicketBaja() == null || transaction.getTicketBaja().isEmpty()) {
                    ticket = wsConsumer.sendSummary(zipDocument, documentName, configuracion);
                    transaction.setTicketBaja(ticket);
                    transaccionRepository.save(transaction);
                } else {
                    ticket = transaction.getTicketBaja();
                }

                /**Enviamos ticket a Sunat*/
                if (configuracion.getIntegracionWs().equals("OSE")) {
                    WSConsumer oseConsumer = WSConsumer.newInstance(transaction.getFE_Id());
                    oseConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion);

                    /**envia ticket a sunat para consultar zip*/
                    service.cloud.request.clientRequest.proxy.ose.object.StatusResponse response = oseConsumer.getStatus(ticket, configuracion);
                    if (response.getContent() != null) {
                        transactionResponse = processOseResponseBAJA(response.getContent(), transaction, fileHandler, documentName, configuracion);
                    }
                }else if(configuracion.getIntegracionWs().equals("SUNAT")){

                    /**envia ticket a sunat para consultar zip*/
                    StatusResponse response = wsConsumer.getStatus(ticket, configuracion);
                    if (response.getContent() != null) {
                        transactionResponse = processOseResponseBAJA(response.getContent(), transaction, fileHandler, documentName, configuracion);
                    }
                }
                transactionResponse.setTicketRest(ticket);
                transactionResponse.setIdentificador(documentName);
            } catch (SOAPFaultException e) {
                if (configuracion.getIntegracionWs().equalsIgnoreCase("SUNAT")) {
                    Detail detail = e.getFault().getDetail();
                    if (null != detail) {
                        transactionResponse.setMensaje(detail.getTextContent());

                    }

                } else if (configuracion.getIntegracionWs().equalsIgnoreCase("OSE")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
                    transactionResponse.setMensaje(exceptionProxy.getDescripcion());
                }
            }
        }

        return transactionResponse;
    }

    private String formatIssueDate(XMLGregorianCalendar xmlGregorianCal)
            throws Exception {

        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();

        Locale locale = new Locale(IPDFCreatorConfig.LOCALE_ES,
                IPDFCreatorConfig.LOCALE_PE);

        SimpleDateFormat sdf = new SimpleDateFormat(
                IPDFCreatorConfig.PATTERN_DATE, locale);
        String issueDate = sdf.format(inputDate);


        return issueDate;
    }

    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, Transaccion transaction, FileHandler fileHandler, String documentName, ConfigData configuracion) {
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

    public TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, Transaccion transaction, String
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

    @Override
    public TransaccionRespuesta transactionDocument(Transaccion transaction, String doctype)
            throws Exception {

        TransaccionRespuesta transactionResponse = null;

        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());


        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        boolean isContingencia = false;
        List<TransaccionContractdocref> contractdocrefs = transaction.getTransaccionContractdocrefList();
        for (TransaccionContractdocref contractdocref : contractdocrefs) {
            if ("cu31".equalsIgnoreCase(contractdocref.getUsuariocampos().getNombre())) {
                isContingencia = "Si".equalsIgnoreCase(contractdocref.getValor());
                break;
            }
        }

        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(transaction.getDOC_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision(), transaction.getSN_EMail(), transaction.getEMail(), isContingencia);

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        byte[] certificado = CertificateUtils.getCertificateInBytes(applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator
                + client.getCertificadoName());
        String certiPassword = client.getCertificadoPassword();
        String ksProvider = client.getCertificadoProveedor();
        String ksType = client.getCertificadoTipoKeystore();

        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        InvoiceType invoiceType = null;
        CreditNoteType creditNoteType = null;
        DebitNoteType debitNoteType = null;
        PerceptionType perceptionType = null;
        RetentionType retentionType = null;
        String documentName = "";
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        UBLDocumentWRP documentWRP = new UBLDocumentWRP();
        String digestValue = "";
        String barcodeValue = "";
        PDFBasicGenerateHandler db = new PDFBasicGenerateHandler(docUUID);

        Calendar fecha = Calendar.getInstance();
        fecha.setTime(transaction.getDOC_FechaEmision());
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);

        String attachmentPath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() +
                File.separator + "anexo" + File.separator + anio + File.separator + mes + File.separator + dia + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
        fileHandler.setBaseDirectory(attachmentPath);
        byte[] signedXmlDocument = null;

        Object ublDocument = null;
        if (doctype.equals("07")) {
            creditNoteType = ublHandler.generateCreditNoteType(transaction, signerName);
            documentName = DocumentNameHandler.getInstance().getCreditNoteName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            // Convert the creditNoteType to a byte array
            byte[] xmlDocument = convertDocumentToBytes(creditNoteType);
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
            // Sign the xmlDocument in memory
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            // Convert the signedXmlDocument back to a CreditNoteType object
            ublDocument = getSignedDocumentV2(signedXmlDocument, transaction.getDOC_Codigo());
            documentWRP.setTransaccion(transaction);
            documentWRP.setCreditNoteType((CreditNoteType) ublDocument);

            digestValue = db.generateDigestValue(documentWRP.getCreditNoteType().getUBLExtensions());
            barcodeValue = db.generateBarCodeInfoString(
                    documentWRP.getTransaccion().getDocIdentidad_Nro(),
                    documentWRP.getTransaccion().getDOC_Codigo(),
                    documentWRP.getTransaccion().getDOC_Serie(),
                    documentWRP.getTransaccion().getDOC_Numero(),
                    documentWRP.getCreditNoteType().getTaxTotal(),
                    formatIssueDate(creditNoteType.getIssueDate().getValue()),
                    documentWRP.getTransaccion().getDOC_MontoTotal().toString(),
                    documentWRP.getTransaccion().getSN_DocIdentidad_Tipo(),
                    documentWRP.getTransaccion().getSN_DocIdentidad_Nro(),
                    documentWRP.getCreditNoteType().getUBLExtensions());

        } else if (doctype.equals("08")) {
            debitNoteType = ublHandler.generateDebitNoteType(transaction, signerName);
            documentName = DocumentNameHandler.getInstance().getDebitNoteName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            // Convert the debitNoteType to a byte array
            byte[] xmlDocument = convertDocumentToBytes(debitNoteType);
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
            // Sign the xmlDocument in memory
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            // Convert the signedXmlDocument back to a DebitNoteType object
            ublDocument = getSignedDocumentV2(signedXmlDocument, transaction.getDOC_Codigo());
            documentWRP.setTransaccion(transaction);
            documentWRP.setDebitNoteType((DebitNoteType) ublDocument);

            digestValue = db.generateDigestValue(documentWRP.getDebitNoteType().getUBLExtensions());
            barcodeValue = db.generateBarCodeInfoString(
                    documentWRP.getTransaccion().getDocIdentidad_Nro(),
                    documentWRP.getTransaccion().getDOC_Codigo(),
                    documentWRP.getTransaccion().getDOC_Serie(),
                    documentWRP.getTransaccion().getDOC_Numero(),
                    documentWRP.getDebitNoteType().getTaxTotal(),
                    documentWRP.getTransaccion().getDOC_FechaVencimiento().toString(),
                    documentWRP.getTransaccion().getDOC_MontoTotal().toString(),
                    documentWRP.getTransaccion().getSN_DocIdentidad_Tipo(),
                    documentWRP.getTransaccion().getSN_DocIdentidad_Nro(),
                    documentWRP.getDebitNoteType().getUBLExtensions());

        } else if (doctype.equals("01") || doctype.equals("03")) {
            invoiceType = ublHandler.generateInvoiceType(transaction, signerName);
            documentName = DocumentNameHandler.getInstance().getInvoiceName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), doctype);
            // Convert the invoiceType to a byte array
            byte[] xmlDocument = convertDocumentToBytes(invoiceType);
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
            // Sign the xmlDocument in memory
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            // Convert the signedXmlDocument back to an InvoiceType object
            ublDocument = getSignedDocumentV2(signedXmlDocument, transaction.getDOC_Codigo());
            //InvoiceType signedInvoiceType = convertBytesToInvoiceType(signedXmlDocument);
            documentWRP.setTransaccion(transaction);
            //documentWRP.setInvoiceType(signedInvoiceType);
            documentWRP.setInvoiceType((InvoiceType) ublDocument);

            if (doctype.equals("03")) {
                digestValue = db.generateDigestValue(documentWRP.getInvoiceType().getUBLExtensions());
                barcodeValue = db.generateBarCodeInfoString(
                        documentWRP.getTransaccion().getDocIdentidad_Nro(),
                        documentWRP.getTransaccion().getDOC_Codigo(),
                        documentWRP.getTransaccion().getDOC_Serie(),
                        documentWRP.getTransaccion().getDOC_Numero(),
                        documentWRP.getInvoiceType().getTaxTotal(),
                        documentWRP.getTransaccion().getDOC_FechaVencimiento().toString(),
                        documentWRP.getTransaccion().getDOC_MontoTotal().toString(),
                        documentWRP.getTransaccion().getSN_DocIdentidad_Tipo(),
                        documentWRP.getTransaccion().getSN_DocIdentidad_Nro(),
                        documentWRP.getInvoiceType().getUBLExtensions());
            } else {
                digestValue = db.generateDigestValue(documentWRP.getInvoiceType().getUBLExtensions());
                barcodeValue = db.generateBarCodeInfoString(documentWRP.getTransaccion().getDocIdentidad_Nro(), documentWRP.getTransaccion().getDOC_Codigo(), documentWRP.getTransaccion().getDOC_Serie(), documentWRP.getTransaccion().getDOC_Numero(),
                        documentWRP.getInvoiceType().getTaxTotal(), formatIssueDate(invoiceType.getIssueDate().getValue()), documentWRP.getTransaccion().getDOC_MontoTotal().toString(), documentWRP.getTransaccion().getSN_DocIdentidad_Tipo(),
                        documentWRP.getTransaccion().getSN_DocIdentidad_Nro(), documentWRP.getInvoiceType().getUBLExtensions());
            }
        } else if (doctype.equals("40")) {
            perceptionType = ublHandler.generatePerceptionType(transaction, signerName);
            validationHandler.checkPerceptionDocument(perceptionType);
            documentName = DocumentNameHandler.getInstance().getPerceptionName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            // Convert the perceptionType to a byte array
            byte[] xmlDocument = convertDocumentToBytes(perceptionType);
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
            // Sign the xmlDocument in memory
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            // Convert the signedXmlDocument back to a PerceptionType object
            ublDocument = getSignedDocumentV2(signedXmlDocument, transaction.getDOC_Codigo());
            documentWRP.setTransaccion(transaction);
            documentWRP.setPerceptionType((PerceptionType) ublDocument);

            digestValue = db.generateDigestValue(documentWRP.getPerceptionType().getUblExtensions());
            barcodeValue = db.generateBarcodeInfoV2(
                    documentWRP.getRetentionType().getId().getValue(),
                    IUBLConfig.DOC_RETENTION_CODE,
                    formatIssueDate(perceptionType.getIssueDate().getValue()),
                    documentWRP.getPerceptionType().getTotalInvoiceAmount().getValue(),
                    BigDecimal.ZERO,
                    documentWRP.getPerceptionType().getAgentParty(),
                    documentWRP.getPerceptionType().getReceiverParty(),
                    documentWRP.getPerceptionType().getUblExtensions());
        } else if (doctype.equals("20")) {
            retentionType = ublHandler.generateRetentionType(transaction, signerName);
            validationHandler.checkRetentionDocument(retentionType);
            documentName = DocumentNameHandler.getInstance().getRetentionName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            // Convert the retentionType to a byte array
            byte[] xmlDocument = convertDocumentToBytes(retentionType);
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
            // Sign the xmlDocument in memory
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            // Convert the signedXmlDocument back to a RetentionType object
            ublDocument = getSignedDocumentV2(signedXmlDocument, transaction.getDOC_Codigo());
            documentWRP.setTransaccion(transaction);
            documentWRP.setRetentionType((RetentionType) ublDocument);

            digestValue = db.generateDigestValue(documentWRP.getRetentionType().getUblExtensions());
            barcodeValue = db.generateBarcodeInfoV2(
                    documentWRP.getRetentionType().getId().getValue(),
                    IUBLConfig.DOC_RETENTION_CODE,
                    formatIssueDate(retentionType.getIssueDate().getValue()),
                    documentWRP.getRetentionType().getTotalInvoiceAmount().getValue(),
                    BigDecimal.ZERO,
                    documentWRP.getRetentionType().getAgentParty(),
                    documentWRP.getRetentionType().getReceiverParty(),
                    documentWRP.getRetentionType().getUblExtensions());
        }

        log.setThirdPartyRequestXml(new String(signedXmlDocument, StandardCharsets.UTF_8));

        /**Guardando el documento xml UBL en DISCO*/
        String documentPath = fileHandler.storeDocumentInDisk(ublDocument, documentName);
        logger.info("Documento XML guardado en disco : " + documentPath);

        String valor = transaction.getTransaccionContractdocrefList().stream()
                .filter(x -> x.getUsuariocampos().getNombre().equals("pdfadicional"))
                .map(x -> x.getValor())
                .findFirst()
                .orElse("No");

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
                .pdfIngles(valor)
                .build();

        if (configuracion.getIntegracionWs().equals("OSE"))
            logger.info("Url Service: " + applicationProperties.getUrlOse());
        else if (configuracion.getIntegracionWs().equals("SUNAT"))
            logger.info("Url Service: " + applicationProperties.getUrlSunat());


        // Compress the signedXmlDocument in memory
        DataHandler zipDocument = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml", transaction.getSN_DocIdentidad_Nro(), transaction.getDocIdentidad_Nro());

        if (null != zipDocument) {
            if (transaction.getFE_Estado().equalsIgnoreCase("N")) {
                WSConsumerNew wsConsumer = WSConsumerNew.newInstance(this.docUUID);
                wsConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion, fileHandler, doctype);
                Thread.sleep(50);

                log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
                Response response = wsConsumer.sendBill(zipDocument, documentName, configuracion);
                log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

                if (null != response.getResponse()) {
                    log.setThirdPartyResponseXml(new String(response.getResponse(), StandardCharsets.UTF_8));
                    transactionResponse = processorCoreInterface.processCDRResponseV2(response.getResponse(), signedXmlDocument, documentWRP, transaction, configuracion);

                    /**Metodo para guardar en disco los documentos*/
                    saveAllFiles(transactionResponse, documentName, attachmentPath);

                    log.setResponse(new Gson().toJson(transactionResponse.getSunat()));
                } else {
                    log.setThirdPartyResponseXml(response.getErrorMessage());
                    transactionResponse = processorCoreInterface.processResponseService(transaction, response);
                    logger.error("Error: " + transactionResponse.getMensaje());
                }

            } else {
                if (transaction.getFE_Estado().equalsIgnoreCase("C")) {
                    WSConsumerConsult wsConsumer = WSConsumerConsult.newInstance(this.docUUID);
                    wsConsumer.setConfiguration(configuracion, transaction.getDocIdentidad_Nro());

                    String documentRuc = transaction.getDocIdentidad_Nro();
                    String documentType = transaction.getDOC_Codigo();
                    String documentSerie = transaction.getDOC_Serie();
                    Integer documentNumber = Integer.valueOf(transaction.getDOC_Numero());

                    log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
                    StatusResponse statusResponse = wsConsumer.getStatusCDR(documentRuc, documentType, documentSerie, documentNumber, configuracion);
                    log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));


                    if (statusResponse.getContent() != null) {
                        transactionResponse = processorCoreInterface.processCDRResponseV2(statusResponse.getContent(), signedXmlDocument, documentWRP, transaction, configuracion);
                        saveAllFiles(transactionResponse, documentName, attachmentPath);
                        log.setThirdPartyResponseXml(new String(statusResponse.getContent(), StandardCharsets.UTF_8));
                    } else {
                        transactionResponse = processorCoreInterface.processResponseSinCDR(transaction);
                        transactionResponse.setMensaje(statusResponse.getStatusMessage());
                    }

                }
            }
        }
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        log.setResponse(new Gson().toJson(transactionResponse.getSunat()));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));


        if (client.getPdfBorrador().equals("true")){
            transactionResponse.setPdfBorrador(documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion));
        }


        transactionResponse.setIdentificador(documentName);
        transactionResponse.setDigestValue(digestValue);
        transactionResponse.setBarcodeValue(barcodeValue);

        transactionResponse.setLogDTO(log);
        log.setResponse(new Gson().toJson(transactionResponse.getMensaje()));
        return transactionResponse;
    }


    public void saveAllFiles(TransaccionRespuesta transactionResponse, String documentName, String attachmentPath) throws Exception {

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);
        fileHandler.storeDocumentInDisk(transactionResponse.getXml(), documentName, "xml");
        fileHandler.storeDocumentInDisk(transactionResponse.getPdf(), documentName, "pdf");
        fileHandler.storeDocumentInDisk(transactionResponse.getZip(), documentName, "zip");
        //fileHandler.storeDocumentInDisk.storeDocumentInDisk(ublDocument, documentName);
    }


    public byte[] convertDocumentToBytes(InvoiceType invoiceType) {
        return convertToBytes(invoiceType, InvoiceType.class);
    }

    public byte[] convertDocumentToBytes(RetentionType retentionType) {
        return convertToBytes(retentionType, RetentionType.class);
    }

    public byte[] convertDocumentToBytes(PerceptionType perceptionType) {
        return convertToBytes(perceptionType, PerceptionType.class);
    }

    public byte[] convertDocumentToBytes(CreditNoteType creditNoteType) {
        return convertToBytes(creditNoteType, CreditNoteType.class);
    }

    public byte[] convertDocumentToBytes(DebitNoteType debitNoteType) {
        return convertToBytes(debitNoteType, DebitNoteType.class);
    }

    private <T> byte[] convertToBytes(T document, Class<T> documentClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(documentClass);
            Marshaller marshaller = jaxbContext.createMarshaller();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(document, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir " + documentClass.getSimpleName() + " a byte[]", e);
        }
    }


    public Object getSignedDocumentV2(byte[] signedXmlDocument, String documentCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getSignedDocument()");
        }
        JAXBContext jaxbContext = null;
        Object object = null;

        try {
            if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_INVOICE_CODE) || documentCode.equalsIgnoreCase(IUBLConfig.DOC_BOLETA_CODE)) {
                jaxbContext = JAXBContext.newInstance(InvoiceType.class);
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_CREDIT_NOTE_CODE)) {
                jaxbContext = JAXBContext.newInstance(CreditNoteType.class);
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_DEBIT_NOTE_CODE)) {
                jaxbContext = JAXBContext.newInstance(DebitNoteType.class);
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_RETENTION_CODE)) {
                jaxbContext = JAXBContext.newInstance(RetentionType.class);
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_PERCEPTION_CODE)) {
                jaxbContext = JAXBContext.newInstance(PerceptionType.class);
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE)) {
                jaxbContext = JAXBContext.newInstance(DespatchAdviceType.class);
            }
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            object = unmarshaller.unmarshal(new ByteArrayInputStream(signedXmlDocument));
        } catch (Exception e) {
            logger.error("getSignedDocument() ERROR: " + e.getMessage());
        }
        return object;
    }

    public DataHandler compressUBLDocumentv2(byte[] document, String documentName, String rucCliente, String rucEmpresa) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("+compressUBLDocument() [" + this.docUUID + "]");
        }
        DataHandler zipDocument = null;
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(document)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                    byte[] array = new byte[10000];
                    int read = 0;
                    zos.putNextEntry(new ZipEntry(documentName));
                    while ((read = bis.read(array, 0, array.length)) != -1) {
                        zos.write(array, 0, read);
                    }
                    zos.closeEntry();
                }
                /* Retornando el objeto DATAHANDLER */
                zipDocument = new DataHandler(new ByteArrayDataSource(bos.toByteArray(), "application/zip"));
                if (logger.isDebugEnabled()) {
                    logger.debug("compressUBLDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP correctamente.");
                }
            }
        } catch (Exception e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-compressUBLDocument() [" + this.docUUID + "]");
        }
        return zipDocument;
    }

}
