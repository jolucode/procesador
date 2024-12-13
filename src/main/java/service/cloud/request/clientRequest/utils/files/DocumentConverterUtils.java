package service.cloud.request.clientRequest.utils.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocumentConverterUtils {

    private final static org.apache.log4j.Logger logger = Logger.getLogger(DocumentConverterUtils.class);


    // Método genérico para convertir cualquier tipo de documento a bytes
    public static <T> byte[] convertDocumentToBytes(T document) {
        return convertToBytes(document, (Class<T>) document.getClass());
    }

    // Método privado para realizar la conversión a bytes
    private static <T> byte[] convertToBytes(T document, Class<T> documentClass) {
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

    public static byte[] compressUBLDocument(byte[] document, String documentName) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("+compressUBLDocument()");
        }

        byte[] zipDocument;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(document);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            byte[] buffer = new byte[10000];
            int read;

            zos.putNextEntry(new ZipEntry(documentName));

            while ((read = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
            }

            zos.closeEntry();
            zipDocument = bos.toByteArray(); // Devuelve directamente los bytes comprimidos

            if (logger.isDebugEnabled()) {
                logger.debug("El documento UBL fue convertido a formato ZIP correctamente.");
            }
        } catch (Exception e) {
            logger.error("Error al comprimir el documento: " + e.getMessage(), e);
            throw new IOException("Error al comprimir el documento UBL.", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-compressUBLDocument()");
        }

        return zipDocument;
    }
}
