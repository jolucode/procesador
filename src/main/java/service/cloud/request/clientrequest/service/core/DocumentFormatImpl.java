package service.cloud.request.clientrequest.service.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.cloud.request.clientrequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientrequest.dto.dto.TransactionTotalesDTO;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientrequest.extras.IUBLConfig;
import service.cloud.request.clientrequest.handler.refactorPdf.service.impl.*;
import service.cloud.request.clientrequest.utils.exception.ConfigurationException;
import service.cloud.request.clientrequest.utils.exception.error.IVenturaError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class DocumentFormatImpl implements DocumentFormatInterface {

    Logger logger = LoggerFactory.getLogger(DocumentFormatImpl.class);

    @Autowired
    InvoicePDFBuilder invoiceBuilder;

    @Autowired
    BoletaPDFBuilder boletaBuilder;

    @Autowired
    CreditNotePDFBuilder ncBuilder;

    @Autowired
    DebitNotePDFBuilder ndBuilder;

    @Autowired
    PerceptionPDFBuilder percepcionBuilder;

    @Autowired
    RetentionPDFBuilder retencionBuilder;

    @Autowired
    DespatchAdvicePDFBuilder despatchAdviceBuilder;

    @Override
    public byte[] createPDFDocument(UBLDocumentWRP wrp, TransacctionDTO transaction, ConfigData configuracion) {

        byte[] pdfBytes = null;
        List<TransactionTotalesDTO> transaccionTotales = new ArrayList<>(transaction.getTransactionTotalesDTOList());
        try {
            switch (transaction.getDOC_Codigo()) {
                case IUBLConfig.DOC_INVOICE_CODE:
                    pdfBytes = invoiceBuilder.generateInvoicePDF(wrp, configuracion); //pdfHandler.generateInvoicePDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_BOLETA_CODE:
                    pdfBytes = boletaBuilder.generateBoletaPDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_CREDIT_NOTE_CODE:
                    pdfBytes = ncBuilder.generateCreditNotePDF(wrp, transaccionTotales, configuracion);
                    break;

                case IUBLConfig.DOC_DEBIT_NOTE_CODE:
                    pdfBytes = ndBuilder.generateDebitNotePDF(wrp, transaccionTotales, configuracion);
                    break;

                case IUBLConfig.DOC_PERCEPTION_CODE:
                    pdfBytes = percepcionBuilder.generatePerceptionPDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_RETENTION_CODE:
                    pdfBytes = retencionBuilder.generateRetentionPDF(wrp, configuracion);
                    break;

                case IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE:
                    pdfBytes = despatchAdviceBuilder.generateDespatchAdvicePDF(wrp, configuracion);
                    break;
                default:
                    throw new ConfigurationException(IVenturaError.ERROR_460.getMessage());
            }
        } catch (ConfigurationException e) {
            logger.error(e.getMessage());
        }
        return pdfBytes;
    }

    @Override
    public Optional<byte[]> unzipResponse(byte[] cdr) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(cdr);
        ZipInputStream zis = new ZipInputStream(bais);
        ZipEntry entry = zis.getNextEntry();
        byte[] xml = null;
        if (entry != null) { // valida dos veces lo mismo
            while (entry != null) {
                if (!entry.isDirectory()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] bytesIn = new byte['?'];
                    int read;
                    while ((read = zis.read(bytesIn)) != -1) {
                        baos.write(bytesIn, 0, read);
                    }
                    baos.close();
                    xml = baos.toByteArray();
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
            zis.close();
            return Optional.ofNullable(xml);
        } else {
            zis.close();
            return Optional.empty();
        }
    }
}
