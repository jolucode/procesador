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
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.handler.object.CreditNoteObject;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo NOTA DE CREDITO utilizando una
 * plantilla de IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class PDFCreditNoteCreator extends DocumentCreator {

    Logger logger = LoggerFactory.getLogger(PDFCreditNoteCreator.class);

    /* Patron SINGLETON */
    private static PDFCreditNoteCreator instance = null;

    private JasperDesign cnJasperDesign;

    private JasperReport cnJasperReport;

    private Map<String, Object> parameterMap;

    private Map<String, Object> cuotasMap;

    private String legendSubReportPath;

    private String paymentDetailReportPath;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param creditNoteReportPath Ruta de la plantilla de la NOTA DE CREDITO.
     * @param legendSubReportPath  Ruta del subreporte de legendas.
     * @throws PDFReportException
     */
    private PDFCreditNoteCreator(String creditNoteReportPath, String legendSubReportPath, String paymentDetailReportPath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+PDFCreditNoteCreator() constructor");
        }
        try {
            File creditNoteTemplate = new File(creditNoteReportPath);
            if (!creditNoteTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_403.getMessage());
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(creditNoteTemplate));

            /* Carga el template .jrxml */
            cnJasperDesign = JRXmlLoader.load(inputStream);

            /* Compila el reporte */
            cnJasperReport = JasperCompileManager.compileReport(cnJasperDesign);

            /*
             * Guardando en la instancia la ruta del subreporte de
             * leyendas
             */
            this.legendSubReportPath = legendSubReportPath;
            this.paymentDetailReportPath = paymentDetailReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFCreditNoteCreator() FileNotFoundException - ERROR: " + e.getMessage());
            logger.error("PDFCreditNoteCreator() FileNotFoundException -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFCreditNoteCreator() Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFCreditNoteCreator() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFCreditNoteCreator() constructor");
        }
    } //PDFCreditNoteCreator

    /**
     * Este metodo obtiene la instancia actual del objeto PDFCreditNoteCreator.
     *
     * @param creditNoteReportPath Ruta de la plantilla de la NOTA DE CREDITO.
     * @param legendSubReportPath  Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFCreditNoteCreator.
     * @throws PDFReportException
     */
    public static PDFCreditNoteCreator getInstance(String creditNoteReportPath, String legendSubReportPath, String paymentDetailReportPath) throws PDFReportException {
        /*if (null == instance) {
            instance = new PDFCreditNoteCreator(creditNoteReportPath, legendSubReportPath);
        }*/
        instance = new PDFCreditNoteCreator(creditNoteReportPath, legendSubReportPath, paymentDetailReportPath);
        return instance;
    } //getInstance

    /**
     * Este metodo crea un PDF que es la representacion impresa de la noa de
     * credito electronica.
     *
     * @param creditNoteObj Objeto que contiene informacion de la nota de
     *                      credito.
     * @param docUUID       Identificador unica de la nota de credito.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
    public byte[] createCreditNotePDF(CreditNoteObject creditNoteObj, String docUUID, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createCreditNotePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == creditNoteObj) {
            throw new PDFReportException(IVenturaError.ERROR_408);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                cuotasMap = new HashMap<>();

                //================================================================================================
                //================================= AGREGANDO INFORMACION AL MAP =================================
                //================================================================================================
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, creditNoteObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, creditNoteObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, creditNoteObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, creditNoteObj.getCurrencyValue());

                if (StringUtils.isNotBlank(creditNoteObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, creditNoteObj.getSunatTransaction());
                }

                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, creditNoteObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.SELL_ORDER, creditNoteObj.getSellOrder());
                parameterMap.put(IPDFCreatorConfig.SELLER_NAME, creditNoteObj.getSellerName());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, creditNoteObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, creditNoteObj.getPorcentajeIGV());

                parameterMap.put(IPDFCreatorConfig.CREDIT_NOTE_TYPE_VALUE, creditNoteObj.getTypeOfCreditNote());
                parameterMap.put(IPDFCreatorConfig.CREDIT_NOTE_DESC_VALUE, creditNoteObj.getDescOfCreditNote());
                parameterMap.put(IPDFCreatorConfig.REFERENCE_DOC_VALUE, creditNoteObj.getDocumentReferenceToCn());
                parameterMap.put(IPDFCreatorConfig.DATE_REFERENCE_DOC_VALUE, creditNoteObj.getDateDocumentReference());

                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, creditNoteObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, creditNoteObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, creditNoteObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, creditNoteObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, creditNoteObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, creditNoteObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, creditNoteObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, creditNoteObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, creditNoteObj.getTelefono1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, creditNoteObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, creditNoteObj.getComentarios());

                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, creditNoteObj.getValidezPDF());

                parameterMap.put(IPDFCreatorConfig.RECEIVER_REGISTRATION_NAME, creditNoteObj.getReceiverRegistrationName());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER, creditNoteObj.getReceiverIdentifier());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER_TYPE, creditNoteObj.getReceiverIdentifierType());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, creditNoteObj.getReceiverFiscalAddress());

                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, creditNoteObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, creditNoteObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PORCISC, creditNoteObj.getISCPercetange());

                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, creditNoteObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, creditNoteObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, creditNoteObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, creditNoteObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, creditNoteObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, creditNoteObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, creditNoteObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, creditNoteObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, creditNoteObj.getInafectaAmountValue());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, creditNoteObj.getInvoicePersonalizacion());
                if (StringUtils.isNotBlank(creditNoteObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, creditNoteObj.getGratuitaAmountValue());
                }


                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, creditNoteObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, creditNoteObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, creditNoteObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, creditNoteObj.getLetterAmountValue());

                /*
                 * IMPORTANTE!!
                 *
                 * Agregar la ruta del directorio en donde se encuentran los
                 * sub-reportes en formato (.jasper)
                 */

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, this.paymentDetailReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getItemListDynamicC()));

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, this.legendSubReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_CREDIT_NOTE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, creditNoteObj.getResolutionCodeValue());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                cuotasMap.put("M1", creditNoteObj.getM1());
                cuotasMap.put("M2", creditNoteObj.getM2());
                cuotasMap.put("M3", creditNoteObj.getM3());

                cuotasMap.put("C1", creditNoteObj.getC1());
                cuotasMap.put("C2", creditNoteObj.getC2());
                cuotasMap.put("C3", creditNoteObj.getC3());

                cuotasMap.put("F1", creditNoteObj.getF1());
                cuotasMap.put("F2", creditNoteObj.getF2());
                cuotasMap.put("F3", creditNoteObj.getF3());

                cuotasMap.put("totalCuotas", creditNoteObj.getTotalCuotas());
                cuotasMap.put("metodoPago", creditNoteObj.getMetodoPago());
                cuotasMap.put("baseImponibleRetencion", creditNoteObj.getBaseImponibleRetencion());
                cuotasMap.put("porcentajeRetencion", creditNoteObj.getPorcentajeRetencion());
                cuotasMap.put("montoRetencion", creditNoteObj.getMontoRetencion());
                cuotasMap.put("montoPendiente", creditNoteObj.getMontoPendiente());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, cuotasMap); // parametros subreporte de cuotas (se pasa como HashMap)
                /*

                 * Generar el reporte con la informacion de la nota
                 * de credito electronica
                 */
                JasperPrint iJasperPrint = JasperFillManager.fillReport(cnJasperReport, parameterMap,
                        new JRBeanCollectionDataSource(creditNoteObj.getItemsListDynamic()));

                /*
                 * Exportar el reporte PDF en una ruta en DISCO
                 */
                String outputPath = USER_TEMPORARY_PATH + File.separator + docUUID + IPDFCreatorConfig.EE_PDF;
                JasperExportManager.exportReportToPdfFile(iJasperPrint, outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createCreditNotePDF() [" + docUUID + "] Se guardo el PDF en una ruta temportal: " + outputPath);
                }

                /*
                 * Convirtiendo el documento PDF generado en bytes.
                 */
                pdfDocument = convertFileInBytes(outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createCreditNotePDF() [" + docUUID + "] Se convirtio el PDF en bytes: " + pdfDocument);
                }
            } catch (Exception e) {
                logger.error("createCreditNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createCreditNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_443);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createCreditNotePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createCreditNotePDF

} //PDFCreditNoteCreator
