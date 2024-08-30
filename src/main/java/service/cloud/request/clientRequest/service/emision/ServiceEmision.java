package service.cloud.request.clientRequest.service.emision;

import org.eclipse.persistence.internal.oxm.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.PDFBasicGenerateHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.ose.IOSEClient;
import service.cloud.request.clientRequest.ose.model.CdrStatusResponse;
import service.cloud.request.clientRequest.prueba.Client;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.utils.CertificateUtils;

import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServiceEmision implements IServiceEmision {

    Logger logger = LoggerFactory.getLogger(ServiceEmision.class);

    @Autowired
    ClientProperties clientProperties;

    private final String docUUID = "123123";

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    IOSEClient ioseClient;

    @Autowired
    ProcessorCoreInterface processorCoreInterface;


    @Autowired
    DocumentFormatInterface documentFormatInterface;

    @Override
    public TransaccionRespuesta transactionDocument(TransacctionDTO transaction, String doctype)
            throws Exception {

        TransaccionRespuesta transactionResponse = null;

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
        boolean isContingencia = false;
        List<Map<String, String>> contractdocrefs = transaction.getTransactionContractDocRefListDTOS();
        /**revisar*/
        /*for (Map<String, String> contractdocref : contractdocrefs) {
            if ("cu31".equalsIgnoreCase(contractdocref.getUsuariocampos().getNombre())) {
                isContingencia = "Si".equalsIgnoreCase(contractdocref.getValor());
                break;
            }
        }¨*/


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
        UBLDocumentWRP documentWRP = UBLDocumentWRP.getInstance();
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
            ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());
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
            ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());
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
            ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());
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
            ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());
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
            ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());
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

        /**Guardando el documento xml UBL en DISCO*/
        String documentPath = fileHandler.storeDocumentInDisk(ublDocument, documentName);
        logger.info("Documento XML guardado en disco : " + documentPath);

        /*String valor = transaction.getTransaccionContractdocrefList().stream()
                .filter(x -> x.getUsuariocampos().getNombre().equals("pdfadicional"))
                .map(x -> x.getValor())
                .findFirst()
                .orElse("No");*/

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
                //.pdfIngles(valor)
                .build();

        if (configuracion.getIntegracionWs().equals("OSE"))
            logger.info("Url Service: " + applicationProperties.getUrlOse());
        else if (configuracion.getIntegracionWs().equals("SUNAT"))
            logger.info("Url Service: " + applicationProperties.getUrlSunat());


        // Compress the signedXmlDocument in memory
        DataHandler zipDocument = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml");

        if (null != zipDocument) {
            if (transaction.getFE_Estado().equalsIgnoreCase("N")) {
                Thread.sleep(50);

                CdrStatusResponse cdrStatusResponse = ioseClient.sendBill(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
                if (cdrStatusResponse.getContent() != null) {
                    transactionResponse = processorCoreInterface.processCDRResponseV2(cdrStatusResponse.getContent()/*response.getResponse()*/, signedXmlDocument, documentWRP, transaction, configuracion);
                    saveAllFiles(transactionResponse, documentName, attachmentPath);
                } else {
                    logger.error("Error: " + transactionResponse.getMensaje());
                }
            } else {
                if (transaction.getFE_Estado().equalsIgnoreCase("C")) {

                    String documentRuc = transaction.getDocIdentidad_Nro();
                    String documentType = transaction.getDOC_Codigo();
                    String documentSerie = transaction.getDOC_Serie();
                    Integer documentNumber = Integer.valueOf(transaction.getDOC_Numero());
                    CdrStatusResponse statusResponse = ioseClient.getStatusCDR(documentRuc, documentType, documentSerie, documentNumber/*, configuracion*/);
                    if (statusResponse.getContent() != null) {
                        transactionResponse = processorCoreInterface.processCDRResponseV2(statusResponse.getContent(), signedXmlDocument, documentWRP, transaction, configuracion);
                        saveAllFiles(transactionResponse, documentName, attachmentPath);
                    } else {
                        transactionResponse = processorCoreInterface.processResponseSinCDR(transaction);
                    }

                }
            }
        }

        if (client.getPdfBorrador().equals("true")) {
            transactionResponse.setPdfBorrador(documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion));
        }

        transactionResponse.setIdentificador(documentName);
        transactionResponse.setDigestValue(digestValue);
        transactionResponse.setBarcodeValue(barcodeValue);
        return transactionResponse;
    }



    private DataHandler compressUBLDocumentv2(byte[] document, String documentName) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("+compressUBLDocument() [" + this.docUUID + "]");
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();

             ZipOutputStream zos = new ZipOutputStream(bos)) {
            zos.putNextEntry(new ZipEntry(documentName));
            zos.write(document);
            zos.closeEntry();

            if (logger.isDebugEnabled()) {
                logger.debug("compressUBLDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP correctamente.");
            }
            return new DataHandler(new ByteArrayDataSource(bos.toByteArray(), "application/zip"));

        } catch (IOException e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage(), e);
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug("-compressUBLDocument() [" + this.docUUID + "]");
            }
        }
    }


    private String formatIssueDate(XMLGregorianCalendar xmlGregorianCal) {
        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(IPDFCreatorConfig.PATTERN_DATE, new Locale(IPDFCreatorConfig.LOCALE_ES, IPDFCreatorConfig.LOCALE_PE));
        return sdf.format(inputDate);
    }

    // Método genérico para convertir cualquier tipo de documento a bytes
    private <T> byte[] convertDocumentToBytes(T document) {
        return convertToBytes(document, (Class<T>) document.getClass());
    }

    // Método privado para realizar la conversión a bytes
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

    // Método para deserializar un documento XML firmado basado en su tipo de documento.
    private Object getSignedDocumentV(byte[] signedXmlDocument, String documentCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getSignedDocument()");
        }

        Map<String, Class<?>> documentTypeMap = new HashMap<>();
        documentTypeMap.put(IUBLConfig.DOC_INVOICE_CODE, InvoiceType.class);
        documentTypeMap.put(IUBLConfig.DOC_BOLETA_CODE, InvoiceType.class);
        documentTypeMap.put(IUBLConfig.DOC_CREDIT_NOTE_CODE, CreditNoteType.class);
        documentTypeMap.put(IUBLConfig.DOC_DEBIT_NOTE_CODE, DebitNoteType.class);
        documentTypeMap.put(IUBLConfig.DOC_RETENTION_CODE, RetentionType.class);
        documentTypeMap.put(IUBLConfig.DOC_PERCEPTION_CODE, PerceptionType.class);
        documentTypeMap.put(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, DespatchAdviceType.class);

        Class<?> documentClass = documentTypeMap.get(documentCode);
        if (documentClass == null) {
            logger.error("Código de documento no reconocido: " + documentCode);
            return null;
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(documentClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(new ByteArrayInputStream(signedXmlDocument));
        } catch (Exception e) {
            logger.error("getSignedDocument() ERROR: {}", e.getMessage(), e);
            return null;
        }
    }

    private void saveAllFiles(TransaccionRespuesta transactionResponse, String documentName, String attachmentPath) throws Exception {
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);
        fileHandler.storeDocumentInDisk(transactionResponse.getXml(), documentName, "xml");
        fileHandler.storeDocumentInDisk(transactionResponse.getPdf(), documentName, "pdf");
        fileHandler.storeDocumentInDisk(transactionResponse.getZip(), documentName, "zip");
    }

}
