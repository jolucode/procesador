package service.cloud.request.clientRequest.handler.creator;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.handler.object.DebitNoteObject;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo NOTA DE DEBITO utilizando una plantilla
 * de IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class PDFDebitNoteCreator {

    private final Logger logger = Logger.getLogger(PDFDebitNoteCreator.class);

    /* Patron SINGLETON */
    private static PDFDebitNoteCreator instance = null;

    private final JasperDesign dnJasperDesign;

    private final JasperReport dnJasperReport;

    private Map<String, Object> parameterMap;

    private final String legendSubReportPath;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param debitNoteReportPath Ruta de la plantilla de la NOTA DE DEBITO.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @throws PDFReportException
     */
    private PDFDebitNoteCreator(String debitNoteReportPath, String legendSubReportPath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+PDFDebitNoteCreator() constructor");
        }
        try {
            File debitNoteTemplate = new File(debitNoteReportPath);
            if (!debitNoteTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_404.getMessage());
            }
            InputStream inputStream = new BufferedInputStream(new FileInputStream(debitNoteTemplate));
            dnJasperDesign = JRXmlLoader.load(inputStream);
            dnJasperReport = JasperCompileManager.compileReport(dnJasperDesign);
            this.legendSubReportPath = legendSubReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFDebitNoteCreator() FileNotFoundException - ERROR: " + e.getMessage());
            logger.error("PDFDebitNoteCreator() FileNotFoundException -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFDebitNoteCreator() Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFDebitNoteCreator() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFDebitNoteCreator() constructor");
        }
    } //PDFDebitNoteCreator

    /**
     * Este metodo obtiene la instancia actual del objeto PDFDebitNoteCreator.
     *
     * @param debitNoteReportPath Ruta de la plantilla de la NOTA DE DEBITO.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFDebitNoteCreator.
     * @throws PDFReportException
     */
    public static PDFDebitNoteCreator getInstance(String debitNoteReportPath, String legendSubReportPath) throws PDFReportException {
        instance = new PDFDebitNoteCreator(debitNoteReportPath, legendSubReportPath);
        return instance;
    } //getInstance

    /**
     * Este metodo crea un PDF que es la representacion impresa de la nota de
     * debito electronica.
     *
     * @param debitNoteObj Objeto que contiene informacion de la nota de debito.
     * @param docUUID      Identificador unica de la nota de debito.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
    public byte[] createDebitNotePDF(DebitNoteObject debitNoteObj, String docUUID, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createDebitNotePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == debitNoteObj) {
            throw new PDFReportException(IVenturaError.ERROR_409);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, debitNoteObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, debitNoteObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, debitNoteObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, debitNoteObj.getCurrencyValue());

                if (StringUtils.isNotBlank(debitNoteObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, debitNoteObj.getSunatTransaction());
                }

                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, debitNoteObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.SELL_ORDER, debitNoteObj.getSellOrder());
                parameterMap.put(IPDFCreatorConfig.SELLER_NAME, debitNoteObj.getSellerName());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, debitNoteObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, debitNoteObj.getPorcentajeIGV());
                parameterMap.put(IPDFCreatorConfig.DEBIT_NOTE_TYPE_VALUE, debitNoteObj.getTypeOfDebitNote());
                parameterMap.put(IPDFCreatorConfig.DEBIT_NOTE_DESC_VALUE, debitNoteObj.getDescOfDebitNote());
                parameterMap.put(IPDFCreatorConfig.REFERENCE_DOC_VALUE, debitNoteObj.getDocumentReferenceToCn());
                parameterMap.put(IPDFCreatorConfig.DATE_REFERENCE_DOC_VALUE, debitNoteObj.getDateDocumentReference());
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, debitNoteObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, debitNoteObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, debitNoteObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, debitNoteObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, debitNoteObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, debitNoteObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, debitNoteObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, debitNoteObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, debitNoteObj.getTelefono_1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, debitNoteObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, debitNoteObj.getComentarios());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, debitNoteObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_REGISTRATION_NAME, debitNoteObj.getReceiverRegistrationName());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER, debitNoteObj.getReceiverIdentifier());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER_TYPE, debitNoteObj.getReceiverIdentifierType());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, debitNoteObj.getReceiverFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, debitNoteObj.getInvoicePersonalizacion());
                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, debitNoteObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, debitNoteObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PORCISC, debitNoteObj.getISCPercetange());
                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, debitNoteObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, debitNoteObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, debitNoteObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, debitNoteObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, debitNoteObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, debitNoteObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, debitNoteObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, debitNoteObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, debitNoteObj.getInafectaAmountValue());

                if (StringUtils.isNotBlank(debitNoteObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, debitNoteObj.getGratuitaAmountValue());
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, debitNoteObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, debitNoteObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, debitNoteObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, debitNoteObj.getLetterAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, this.legendSubReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(debitNoteObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_DEBIT_NOTE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, debitNoteObj.getResolutionCodeValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                JasperPrint iJasperPrint = JasperFillManager.fillReport(dnJasperReport, parameterMap,
                        new JRBeanCollectionDataSource(debitNoteObj.getItemsListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument =  outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createDebitNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createDebitNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_444);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createDebitNotePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createDebitNotePDF

} //PDFDebitNoteCreator
