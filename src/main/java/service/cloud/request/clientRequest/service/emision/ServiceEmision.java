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

import service.cloud.request.clientRequest.utils.DocumentNameUtils;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.SignerDocumentException;
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
import java.io.*;
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
    public TransaccionRespuesta transactionDocument(TransacctionDTO transaction, String doctype) throws Exception {

        TransaccionRespuesta transactionResponse = null;


        boolean isContingencia = isContingencia(transaction);
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(transaction.getDOC_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision(), transaction.getSN_EMail(), transaction.getEMail(), isContingencia);

        // Obtiene datos del cliente y valida certificado
        Client client = getClientData(transaction);
        byte[] certificado = loadCertificate(client, transaction.getDocIdentidad_Nro());
        validateCertificate(certificado, client);

        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        UBLDocumentWRP documentWRP = UBLDocumentWRP.getInstance();
        String digestValue = "";
        String barcodeValue = "";
        PDFBasicGenerateHandler db = new PDFBasicGenerateHandler(docUUID);

        // Configura la ruta de almacenamiento del archivo
        String attachmentPath = getAttachmentPath(transaction, doctype);
        fileHandler.setBaseDirectory(attachmentPath);


        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        byte[] signedXmlDocument = null;
        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), client.getCertificadoTipoKeystore(), client.getCertificadoProveedor(), signerName);

        if (doctype.equals("07")) {
            CreditNoteType creditNoteType = ublHandler.generateCreditNoteType(transaction, signerName);
            byte[] xmlDocument = convertDocumentToBytes(creditNoteType); //generico
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        } else if (doctype.equals("08")) {
            DebitNoteType debitNoteType = ublHandler.generateDebitNoteType(transaction, signerName);
            byte[] xmlDocument = convertDocumentToBytes(debitNoteType);
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        } else if (doctype.equals("01")) {
            InvoiceType invoiceType = ublHandler.generateInvoiceType(transaction, signerName);
            byte[] xmlDocument = convertDocumentToBytes(invoiceType);
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        } else if (doctype.equals("03")) {
            InvoiceType invoiceType = ublHandler.generateInvoiceType(transaction, signerName);
            byte[] xmlDocument = convertDocumentToBytes(invoiceType);
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        } else if (doctype.equals("40")) {
            PerceptionType perceptionType = ublHandler.generatePerceptionType(transaction, signerName);
            validationHandler.checkPerceptionDocument(perceptionType);
            byte[] xmlDocument = convertDocumentToBytes(perceptionType);
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        } else if (doctype.equals("20")) {
            RetentionType retentionType = ublHandler.generateRetentionType(transaction, signerName);
            validationHandler.checkRetentionDocument(retentionType);
            byte[] xmlDocument = convertDocumentToBytes(retentionType);
            signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        }

        String documentName = DocumentNameUtils.getDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id(), doctype);
        documentWRP = configureDocumentWRP(documentWRP, signedXmlDocument, transaction.getDOC_Codigo(), doctype);
        documentWRP.setTransaccion(transaction);
        Object ublDocument = getSignedDocumentV(signedXmlDocument, transaction.getDOC_Codigo());

        /**Guardando el documento xml UBL en DISCO*/
        String documentPath = fileHandler.storeDocumentInDisk(ublDocument, documentName);

        logger.info("Documento XML guardado en disco : " + documentPath);

        /*String valor = transaction.getTransaccionContractdocrefList().stream()
                .filter(x -> x.getUsuariocampos().getNombre().equals("pdfadicional"))
                .map(x -> x.getValor())
                .findFirst()
                .orElse("No");*/

        ConfigData configuracion = ConfigData.builder().usuarioSol(client.getUsuarioSol()).claveSol(client.getClaveSol()).integracionWs(client.getIntegracionWs()).ambiente(applicationProperties.getAmbiente()).mostrarSoap(client.getMostrarSoap()).pdfBorrador(client.getPdfBorrador()).impresionPDF(client.getImpresion()).rutaBaseDoc(applicationProperties.getRutaBaseDoc())
                //.pdfIngles(valor)
                .build();

        DataHandler zipDocument = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml");

        if (null != zipDocument) {
            if (transaction.getFE_Estado().equalsIgnoreCase("N")) {
                Thread.sleep(50);

                CdrStatusResponse cdrStatusResponse = ioseClient.sendBill(DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
                if (cdrStatusResponse.getContent() != null) {
                    transactionResponse = processorCoreInterface.processCDRResponseV2(cdrStatusResponse.getContent(), signedXmlDocument, documentWRP, transaction, configuracion);
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
                    CdrStatusResponse statusResponse = ioseClient.getStatusCDR(documentRuc, documentType, documentSerie, documentNumber);
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

    private boolean isContingencia(TransacctionDTO transaction) {
        List<Map<String, String>> contractdocrefs = transaction.getTransactionContractDocRefListDTOS();
        for (Map<String, String> contractdocref : contractdocrefs) {
            //if ("cu31".equalsIgnoreCase(contractdocref.get("Usuariocampos").get("Nombre"))) {
            //    return "Si".equalsIgnoreCase(contractdocref.get("Valor"));
           // }
        }
        return false;
    }


    private String getAttachmentPath(TransacctionDTO transaction, String doctype) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTime(transaction.getDOC_FechaEmision());
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);

        return applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + "anexo"
                + File.separator + anio + File.separator + mes + File.separator + dia
                + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
    }

    private Client getClientData(TransacctionDTO transaction) {
        return clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
    }

    private byte[] loadCertificate(Client client, String docIdentidadNuumero) throws ConfigurationException, FileNotFoundException {
        String certificatePath = applicationProperties.getRutaBaseDoc() + docIdentidadNuumero
                + File.separator + client.getCertificadoName();
        return CertificateUtils.getCertificateInBytes(certificatePath);
    }

    private void validateCertificate(byte[] certificate, Client client) throws SignerDocumentException {
        CertificateUtils.checkDigitalCertificateV2(certificate, client.getCertificadoPassword(),
                client.getCertificadoProveedor(), client.getCertificadoTipoKeystore());
    }

    private UBLDocumentWRP configureDocumentWRP(UBLDocumentWRP documentWRP, byte[] signedXmlDocument, String docCodigo, String doctype) throws Exception {
        Object ublDocument = getSignedDocumentV(signedXmlDocument, docCodigo);

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
