package service.cloud.request.clientRequest.extras.pdf;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.utils.Utils;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DocumentCreator {

    Logger logger = LoggerFactory.getLogger(DocumentCreator.class);

    protected final String USER_TEMPORARY_PATH = System.getProperty("java.io.tmpdir");


    /**
     * Este metodo recibe como entrada la ruta de un archivo, luego obtiene
     * el archivo de la ruta y lo convierte a bytes.
     *
     * @param filePath La ruta del archivo.
     * @return Retorna un archivo en bytes.
     * @throws PDFReportException
     */
    @SuppressWarnings("resource")
    protected synchronized byte[] convertFileInBytes(String filePath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+convertFileInBytes() filePath: " + filePath);
        }
        byte[] objectInBytes = null;
        ByteArrayOutputStream bos = null;
        InputStream fis = null;

        try {
            bos = new ByteArrayOutputStream();
            fis = new java.io.FileInputStream(filePath);

            byte[] buffer = new byte[1024];
            int read;

            while ((read = fis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, read);
            }
            objectInBytes = bos.toByteArray();
        } catch (Exception e) {
            logger.error("convertFileInBytes() Exception(" + e.getClass().getName() + ") ERROR: " + e.getMessage());
            logger.error("convertFileInBytes() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_15);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-convertFileInBytes()");
        }
        return objectInBytes;
    } //convertFileInBytes

} //DocumentCreator
