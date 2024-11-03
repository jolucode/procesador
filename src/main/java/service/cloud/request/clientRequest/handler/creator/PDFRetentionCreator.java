package service.cloud.request.clientRequest.handler.creator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.extras.pdf.DocumentCreator;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.object.RetentionObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PDFRetentionCreator extends DocumentCreator {

    Logger logger = LoggerFactory.getLogger(PDFRetentionCreator.class);

    private static PDFRetentionCreator instance = null;

    private final JasperDesign iJasperDesign;

    private final JasperReport iJasperReport;

    private Map<String, Object> parameterMap;

    private final String legendSubReportPath;

    private PDFRetentionCreator(String retentionReportPath,
                                String legendSubReportPath) throws PDFReportException {

        if (logger.isDebugEnabled()) {
            logger.debug("+PDFRetentiCreator() constructor");
        }

        try {
            File retentionTemplate = new File(retentionReportPath);
            if (!retentionTemplate.isFile()) {
                throw new FileNotFoundException(
                        IVenturaError.ERROR_401.getMessage());
            }

            InputStream inputStream = new BufferedInputStream(
                    new FileInputStream(retentionTemplate));

            /* Carga el template .jrxml */
            iJasperDesign = JRXmlLoader.load(inputStream);

            /* Compila el reporte */
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);

            /*
             * Guardando en la instancia la ruta del subreporte de leyendas
             */
            this.legendSubReportPath = legendSubReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFRetentionCreator() FileNotFoundException - ERROR: "
                    + e.getMessage());
            logger.error("PDFRetentionCreator() FileNotFoundException -->"
                    + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFRetentionCreator() Exception("
                    + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFRetentionCreator() Exception("
                    + e.getClass().getName() + ") -->"
                    + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFRetentionCreator() constructor");
        }
    } // PDFInv


    public static PDFRetentionCreator getInstance(String retentionReportPath,
                                                  String legendSubReportPath) throws PDFReportException {
        instance = new PDFRetentionCreator(retentionReportPath, legendSubReportPath);
        return instance;
    }

    public byte[] createRetentionPDF(RetentionObject retentionObject,
                                     String docUUID, ConfigData configData) throws PDFReportException {

        String outputPath = "";
        if (logger.isDebugEnabled()) {
            logger.debug("+createRetentionPDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == retentionObject) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {

            try {
                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, retentionObject.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, retentionObject.getIssueDate());
                if (StringUtils.isNotBlank(retentionObject
                        .getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, retentionObject.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, retentionObject.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, retentionObject.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, retentionObject.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, retentionObject.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, retentionObject.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, retentionObject.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, retentionObject.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, retentionObject.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, retentionObject.getTel());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, retentionObject.getTel1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, retentionObject.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, retentionObject.getComentarios());
                parameterMap.put(IPDFCreatorConfig.TOTAL_DOCUMENTO, retentionObject.getTotal_doc_value());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, retentionObject.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.REGIMENRET, retentionObject.getRegimenRET());
                parameterMap.put(IPDFCreatorConfig.IMPORTETOTALDOC, retentionObject.getMontoTotalDoc());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON, retentionObject.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC, retentionObject.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, retentionObject.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE_SOLES, retentionObject.getMontoenSoles());

                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, retentionObject.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 147")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, retentionObject.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, retentionObject.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, retentionObject.getLetterAmountValue());

                JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap, new JRBeanCollectionDataSource(retentionObject.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument =  outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createInvoicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createInvoicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }

        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createInvoicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
        // createInvoicePDF
    }

}
