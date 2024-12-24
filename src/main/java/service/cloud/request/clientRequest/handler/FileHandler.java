package service.cloud.request.clientRequest.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.exceptions.VenturaExcepcion;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Esta clase HANDLER contiene metodos para transformar archivos y carpetas.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class FileHandler {

    Logger logger = LoggerFactory.getLogger(FileHandler.class);
    private String baseDirectory;

    private final String docUUID;

    private FileHandler(String docUUID) {
        this.docUUID = docUUID;
    } //FileHandler

    public static synchronized FileHandler newInstance(String docUUID) {
        return new FileHandler(docUUID);
    } //newInstance

    public void setBaseDirectory(String directoryPath) {
        this.baseDirectory = directoryPath;
        if (logger.isDebugEnabled()) {
            logger.debug("+-setBaseDirectory() [" + this.docUUID + "] baseDirectory: " + this.baseDirectory);
        }
    } //setBaseDirectory

    public String storeDocumentInDisk(byte[] fileData, String documentName, String fileExtension) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+storeDocumentInDisk() [" + this.docUUID + "]");
        }
        String documentPath = null;
        try {
            String separator = File.separator;
            documentPath = this.baseDirectory + separator + documentName + "." + fileExtension;

            File file = new File(this.baseDirectory);
            if (!file.exists()) {
                file.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(documentPath)) {
                fos.write(fileData);
            }

            logger.info("Se guardo el documento " + fileExtension + " : " + documentPath);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("storeDocumentInDisk() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_454.getMessage() + e.getMessage());
            throw new Exception(IVenturaError.ERROR_454.getMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-storeDocumentInDisk() [" + this.docUUID + "]");
        }
        return documentPath;
    }

    public String storeDocumentInDisk(Object ublDocument, String documentName) throws Exception {


        if (logger.isDebugEnabled()) {
            logger.debug("+storeDocumentInDisk() [" + this.docUUID + "]");
        }
        String documentPath = null;
        try {
            String separator = File.separator;
            documentPath = this.baseDirectory + separator + documentName + ISunatConnectorConfig.EE_XML;

            if (logger.isDebugEnabled()) {
                logger.info("storeDocumentInDisk() [" + this.docUUID + "] Se guardo el documento UBL en: " + documentPath);
            }
            File file = new File(this.baseDirectory);
            if (!file.exists()) {
                file.mkdirs();
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(ublDocument.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, ISunatConnectorConfig.ENCODING_UTF);
            marshaller.marshal(ublDocument, new File(documentPath));
            if (logger.isDebugEnabled()) {
                logger.debug("storeDocumentInDisk() [" + this.docUUID + "] Se guardo el documento UBL en: " + documentPath);
            }
        } catch (JAXBException e) {
            e.printStackTrace();
            logger.error("storeDocumentInDisk() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_454.getMessage() + e.getMessage());
            throw new Exception(IVenturaError.ERROR_454.getMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-storeDocumentInDisk() [" + this.docUUID + "]");
        }
        return documentPath;
    } //storeDocumentInDisk

    /**
     * Este metodo convierte un archivo, que se encuentra en una ruta
     * especifica, en bytes.
     *
     * @param signedDocument La ruta del archivo a convertir en bytes.
     * @return Retorna el archivo convertido en bytes.
     */
    public byte[] convertFileToBytes(File signedDocument) {
        if (logger.isDebugEnabled()) {
            logger.debug("+convertFileToBytes() signedDocument: " + signedDocument.getAbsolutePath());
        }
        try {
            Path path = Paths.get(signedDocument.toURI());
            byte[] documentBytes = Files.readAllBytes(path);
            return documentBytes;
        } catch (Exception e) {
            logger.error("convertFileToBytes() Exception(" + e.getClass().getName() + ") ERROR: " + e.getMessage());
            return null;
        }
    } //convertFileToBytes


    public Object getSignedDocument(File signedDocument, String documentCode) {
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
            } else if (documentCode.equalsIgnoreCase(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE) || documentCode.equalsIgnoreCase(IUBLConfig.DOC_SENDER_CARRIER_GUIDE_CODE)) {
                jaxbContext = JAXBContext.newInstance(DespatchAdviceType.class);
            }
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            object = unmarshaller.unmarshal(signedDocument);
        } catch (Exception e) {
            logger.error("getSignedDocument() ERROR: " + e.getMessage());
        }
        return object;
    } //getSignedDocument


} //FileHandler
