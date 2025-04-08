package service.cloud.request.clientRequest.service.emision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.service.DocumentEmissionService;
import service.cloud.request.clientRequest.estela.service.DocumentQueryService;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.utils.*;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.DocumentNameUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

@Service
public class ServiceEmision implements IServiceEmision {

    Logger logger = LoggerFactory.getLogger(ServiceEmision.class);

    @Autowired
    ClientProperties clientProperties;

    private final String docUUID = "123123";

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    ProcessorCoreInterface processorCoreInterface;

    @Autowired
    DocumentFormatInterface documentFormatInterface;

    @Autowired
    DocumentEmissionService documentEmissionService;

    @Autowired
    private DocumentQueryService documentQueryService;

    @Override
    public TransaccionRespuesta transactionDocument(TransacctionDTO transaction, String doctype) throws Exception {

        /***/
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getRazonSocial());
        /***/

        boolean isContingencia = isContingencia(transaction);
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(transaction.getDOC_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision(), transaction.getSN_EMail(), transaction.getEMail(), isContingencia);

        // Obtiene datos del cliente y valida certificado
        Client client = getClientData(transaction);
        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);

        // Configura la ruta de almacenamiento del archivo
        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDoc());

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
        byte[] xmlDocument = null;
        if (doctype.equals("07")) {
            CreditNoteType creditNoteType = ublHandler.generateCreditNoteType(transaction, signerName);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(creditNoteType); //generico
        } else if (doctype.equals("08")) {
            DebitNoteType debitNoteType = ublHandler.generateDebitNoteType(transaction, signerName);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(debitNoteType);
        } else if (doctype.equals("01") || (doctype.equals("03"))) {
            InvoiceType invoiceType = ublHandler.generateInvoiceType(transaction, signerName);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(invoiceType);
        } else if (doctype.equals("40")) {
            PerceptionType perceptionType = ublHandler.generatePerceptionType(transaction, signerName);
            validationHandler.checkPerceptionDocument(perceptionType);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(perceptionType);
        } else if (doctype.equals("20")) {
            RetentionType retentionType = ublHandler.generateRetentionType(transaction, signerName);
            validationHandler.checkRetentionDocument(retentionType);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(retentionType);
        }

        /**PROCESAR CERTIFICADO*/
        String certificatePath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
        byte[] certificado = CertificateUtils.loadCertificate(certificatePath);
        CertificateUtils.validateCertificate(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());
        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);
        byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        String documentName = DocumentNameUtils.getDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), doctype);

        try {
            UtilsFile.storeDocumentInDisk(signedXmlDocument, documentName, "xml", attachmentPath);
            logger.info("Archivo firmado guardado exitosamente en: " + attachmentPath);
        } catch (IOException e) {
            logger.error("Error al guardar el archivo: " + e.getMessage());
        }


        UBLDocumentWRP documentWRP = configureDocumentWRP(signedXmlDocument, transaction.getDOC_Codigo(), doctype);
        documentWRP.setTransaccion(transaction);

        ConfigData configuracion = createConfigData(client, transaction);

        byte[] zipBytes = DocumentConverterUtils.compressUBLDocument(signedXmlDocument, documentName + ".xml");
        String base64Content = convertToBase64(zipBytes);

        log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
        TransaccionRespuesta transactionResponse = handleTransactionStatus(base64Content, transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
        log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

        String errorPdf = "";
        //if (client.getPdfBorrador().equals("true")) {
        /*try {
            transactionResponse.setPdfBorrador(documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion));
        } catch (Exception e) {
            errorPdf = e.getMessage();
        }*/
        if(transactionResponse.getPdf()!=null)
            transactionResponse.setPdfBorrador(transactionResponse.getPdf());

        transactionResponse.setIdentificador(documentName);
        log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
        log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        //String messageResponse = (JsonUtils.toJson(transactionResponse.getSunat())).equals("null") ? transactionResponse.getMensaje() : (JsonUtils.toJson(transactionResponse.getSunat()));
        log.setResponse(transactionResponse.getMensaje() );
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        transactionResponse.setLogDTO(log);
        log.setPathBase(attachmentPath + "\\" + documentName + ".json");
        transactionResponse.setMensaje(transactionResponse.getMensaje());
        return transactionResponse;
    }

    // Método para convertir los bytes a base64
    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }

    private TransaccionRespuesta handleTransactionStatus(String base64Content, TransacctionDTO transaction,
                                                         byte[] signedXmlDocument, UBLDocumentWRP documentWRP,
                                                         ConfigData configuracion, String documentName,
                                                         String attachmentPath) throws Exception {

        String estado = transaction.getFE_Estado().toUpperCase();
        switch (estado) {
            case "N":
                return processNewTransaction(base64Content, transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
            case "C":
                return processCancelledTransaction(transaction, signedXmlDocument, documentWRP, configuracion, documentName, attachmentPath);
            default:
                logger.error("Error: Unknown transaction status");
                return null;
        }
    }

    private TransaccionRespuesta processNewTransaction(String base64Content, TransacctionDTO transaction, byte[] signedXmlDocument, UBLDocumentWRP documentWRP, ConfigData configuracion, String documentName, String attachmentPath) throws Exception {
        Thread.sleep(50);

        FileRequestDTO soapRequest = new FileRequestDTO();
        String urlClient = applicationProperties.obtenerUrl(configuracion.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
        soapRequest.setService(urlClient);
        soapRequest.setUsername(configuracion.getUsuarioSol());
        soapRequest.setPassword(configuracion.getClaveSol());
        soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(documentName));
        soapRequest.setContentFile(base64Content);

        long startTime = System.currentTimeMillis(); // Tiempos de inicio

        // Realiza la llamada al servicio
        Mono<FileResponseDTO> monoResponse = documentEmissionService.processDocumentEmission(soapRequest.getService(), soapRequest);

        long endTime = System.currentTimeMillis(); // Tiempos de finalización
        long duration = endTime - startTime; // Duración en milisegundos

        // Log del tiempo
        logger.info("La llamada al servicio 'processDocumentEmission' tomó " + duration + " ms");

        FileResponseDTO responseDTO = monoResponse.block();
        if (responseDTO.getContent() != null) {
            return processorCoreInterface.processCDRResponseV2(responseDTO.getContent(), signedXmlDocument, documentWRP, transaction, configuracion, documentName, attachmentPath);
        } else {
            return processorCoreInterface.processResponseSinCDR(transaction, responseDTO);
        }
    }

    private TransaccionRespuesta processCancelledTransaction(TransacctionDTO transaction, byte[] signedXmlDocument, UBLDocumentWRP documentWRP, ConfigData configuracion, String documentName, String attachmentPath) throws Exception {

        FileRequestDTO soapRequest = new FileRequestDTO();
        String urlClient = applicationProperties.obtenerUrl(configuracion.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
        soapRequest.setService(urlClient);
        soapRequest.setUsername(configuracion.getUsuarioSol());
        soapRequest.setPassword(configuracion.getClaveSol());
        soapRequest.setRucComprobante(transaction.getDocIdentidad_Nro());
        soapRequest.setTipoComprobante(transaction.getDOC_Codigo());
        soapRequest.setSerieComprobante(transaction.getDOC_Serie());
        soapRequest.setNumeroComprobante(transaction.getDOC_Numero());

        Mono<FileResponseDTO> monoResponse = documentQueryService.processAndSaveFile(soapRequest.getService(), soapRequest);
        FileResponseDTO responseDTO = monoResponse.block();

        if (responseDTO.getContent() != null) {
            return processorCoreInterface.processCDRResponseV2(responseDTO.getContent(), signedXmlDocument, documentWRP, transaction, configuracion, documentName, attachmentPath);
        } else {
            return processorCoreInterface.processResponseSinCDR(transaction, responseDTO);
        }
    }

    private ConfigData createConfigData(Client client, TransacctionDTO transacctionDTO) {

        String valorIngles = transacctionDTO.getTransactionContractDocRefListDTOS().stream()
                .map(contractMap -> contractMap.get("pdfadicional"))
                .filter(valor -> valor != null && !valor.isEmpty())
                .findFirst()
                .orElse("");

        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDoc())
                .pdfIngles(valorIngles)
                .build();
    }

    private boolean isContingencia(TransacctionDTO transaction) {
        return false;
    }

    private Client getClientData(TransacctionDTO transaction) {
        return clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
    }

    private UBLDocumentWRP configureDocumentWRP(byte[] signedXmlDocument, String docCodigo, String doctype) {
        UBLDocumentWRP documentWRP = new UBLDocumentWRP(); // Crear directamente dentro del método
        Object ublDocument = deserializeSignedDocument(signedXmlDocument, docCodigo);

        switch (doctype) {
            case "07":
                documentWRP.setCreditNoteType((CreditNoteType) ublDocument);
                break;
            case "08":
                documentWRP.setDebitNoteType((DebitNoteType) ublDocument);
                break;
            case "01":
            case "03":
                documentWRP.setInvoiceType((InvoiceType) ublDocument);
                break;
            case "40":
                documentWRP.setPerceptionType((PerceptionType) ublDocument);
                break;
            case "20":
                documentWRP.setRetentionType((RetentionType) ublDocument);
                break;
            default:
                throw new IllegalArgumentException("Invalid document type: " + doctype);
        }
        return documentWRP;
    }

    public Object deserializeSignedDocument(byte[] signedXmlDocument, String documentCode) {


        Class<?> documentClass = DocumentConfig.DOCUMENT_TYPE_MAP.get(documentCode);

        if (documentClass == null) {
            logger.error("Código de documento no reconocido: " + documentCode);
            return null;
        }

        try {
            JAXBContext jaxbContext = DocumentConfig.getJAXBContext(documentClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new ByteArrayInputStream(signedXmlDocument));
        } catch (Exception e) {
            logger.error("Error deserializando documento: {}", e.getMessage(), e);
            return null;
        }
    }


}
