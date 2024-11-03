package service.cloud.request.clientRequest.handler.creator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.extras.pdf.DocumentCreator;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.object.PerceptionObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PDFPerceptionCreator extends DocumentCreator {

    private final Logger logger = Logger.getLogger(PDFPerceptionCreator.class);

    private static PDFPerceptionCreator instance = null;

    private final JasperDesign iJasperDesign;

    private final JasperReport iJasperReport;

    private Map<String, Object> parameterMap;

    private final String legendSubReportPath;

    private PDFPerceptionCreator(String perceptionReportPath,
                                 String legendSubReportPath) throws PDFReportException {

        if (logger.isDebugEnabled()) {
            logger.debug("+PDFRetentiCreator() constructor");
        }

        try {
            File perceptionTemplate = new File(perceptionReportPath);
            if (!perceptionTemplate.isFile()) {
                throw new FileNotFoundException(
                        IVenturaError.ERROR_401.getMessage());
            }

            InputStream inputStream = new BufferedInputStream(
                    new FileInputStream(perceptionTemplate));

            /* Carga el template .jrxml */
            iJasperDesign = JRXmlLoader.load(inputStream);

            /* Compila el reporte */
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);

            /*
             * Guardando en la instancia la ruta del subreporte de leyendas
             */
            this.legendSubReportPath = legendSubReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFPerceptionCreator() FileNotFoundException - ERROR: "
                    + e.getMessage());
            logger.error("PDFPerceptionCreator() FileNotFoundException -->"
                    + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFPerceptionCreator() Exception("
                    + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFPerceptionCreator() Exception("
                    + e.getClass().getName() + ") -->"
                    + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFPerceptionCreator() constructor");
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
    public static PDFPerceptionCreator getInstance(String perceptionReportPath,
                                                   String legendSubReportPath) throws PDFReportException {
        instance = new PDFPerceptionCreator(perceptionReportPath, legendSubReportPath);
        return instance;
    }

    public byte[] createPerceptionPDF(PerceptionObject perceptionObj,
                                      String docUUID) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createInvoicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == perceptionObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER,
                        perceptionObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE,
                        perceptionObj.getIssueDate());

                if (StringUtils.isNotBlank(perceptionObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL,
                            IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE,
                            perceptionObj.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON,
                        perceptionObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC,
                        perceptionObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS,
                        perceptionObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST,
                        perceptionObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT,
                        perceptionObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL,
                        perceptionObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL,
                        perceptionObj.getTelValue());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1,
                        perceptionObj.getTel2Value());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH,
                        perceptionObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB,
                        perceptionObj.getWebValue());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, perceptionObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON,
                        perceptionObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC,
                        perceptionObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE,
                        perceptionObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE,
                        perceptionObj.getLetterAmountValue());

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE,
                        IPDFCreatorConfig.LEGEND_PERCEPTION_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE,
                        perceptionObj.getResolutionCodeValue());

                JasperPrint iJasperPrint = JasperFillManager.fillReport(
                        iJasperReport,
                        parameterMap,
                        new JRBeanCollectionDataSource(perceptionObj
                                .getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") - ERROR: "
                        + e.getMessage());
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") -->"
                        + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createInvoicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } // createInvoicePDF

}
