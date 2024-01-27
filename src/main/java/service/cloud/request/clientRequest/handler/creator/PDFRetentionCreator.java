package service.cloud.request.clientRequest.handler.creator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
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

    private final Logger logger = Logger.getLogger(PDFRetentionCreator.class);

    private static PDFRetentionCreator instance = null;

    private JasperDesign iJasperDesign;

    private JasperReport iJasperReport;

    private Map<String, Object> parameterMap;

    private String legendSubReportPath;

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

    /**
     * Este metodo obtiene la instancia actual del objeto PDFInvoiceCreator.
     *
     * @param perceptionReportPath Ruta de la plantilla de la PERCEPCION.
     * @param legendSubReportPath  Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFPerceptionCreator.
     * @throws PDFReportException
     */
    public static PDFRetentionCreator getInstance(String retentionReportPath,
                                                  String legendSubReportPath) throws PDFReportException {
        /*if (null == instance) {
         instance = new PDFRetentionCreator(retentionReportPath,legendSubReportPath);
         }*/
        instance = new PDFRetentionCreator(retentionReportPath, legendSubReportPath);
        return instance;
    }

    /**
     * Este metodo crea un PDF que es la representacion impresa de la factura
     * electronica.
     *
     * @param invoiceObj Objeto que contiene informacion de la percepcion.
     * @param docUUID    Identificador unica de la percepcion.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
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
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                // ================================================================================================
                // ================================= AGREGANDO INFORMACION AL
                // MAP =================================
                // ================================================================================================
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

                /*
                 * IMPORTANTE!!
                 *
                 * Agregar la ruta del directorio en donde se encuentran los
                 * sub-reportes en formato (.jasper)
                 */
                // parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR,
                // this.legendSubReportPath);
                // parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE,
                // new
                // JRBeanCollectionDataSource(retentionObject.getLegends()));
                // Map<String, String> legendMap = new HashMap<String,
                // String>();
                // legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE,
                // IPDFCreatorConfig.LEGEND_INVOICE_DOCUMENT);
                // legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE,
                // retentionObject.getResolutionCodeValue());
                // parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP,
                // legendMap);

                /*
                 * Generar el reporte con la informacion de la factura
                 * electronica
                 */
                JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap, new JRBeanCollectionDataSource(retentionObject.getItemListDynamic()));

                /*
                 * Exportar el reporte PDF en una ruta en DISCO
                 */
                outputPath = USER_TEMPORARY_PATH + File.separator + docUUID + IPDFCreatorConfig.EE_PDF;
                JasperExportManager.exportReportToPdfFile(iJasperPrint, outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createInvoicePDF() [" + docUUID + "] Se guardo el PDF en una ruta temportal: " + outputPath);
                }

                /*
                 * Convirtiendo el documento PDF generado en bytes.
                 */
                pdfDocument = convertFileInBytes(outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createInvoicePDF() [" + docUUID + "] Se convirtio el PDF en bytes: " + pdfDocument);
                }

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
