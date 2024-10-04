package service.cloud.request.clientRequest.extras.pdf;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.handler.object.DebitNoteObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo NOTA DE DEBITO utilizando una plantilla
 * de IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */

@Service
public class PDFDebitNoteCreator {

    private final Logger logger = Logger.getLogger(PDFDebitNoteCreator.class);

    private Map<String, Object> populateParameterMap(DebitNoteObject debitNoteObj, ConfigData configuracion) {
        Map<String, Object> parameterMap = new HashMap<>();
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

        if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
            parameterMap.put(IPDFCreatorConfig.CODEQR, debitNoteObj.getCodeQR());
        } else {
            parameterMap.put(IPDFCreatorConfig.CODEQR, null);
        }

        if (configuracion.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
            parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, debitNoteObj.getBarcodeValue());
        } else {
            parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
        }

        if (configuracion.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
            parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, debitNoteObj.getDigestValue());
        } else {
            parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
        }

        parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, debitNoteObj.getLetterAmountValue());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, configuracion.getLegendSubReportPath());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(debitNoteObj.getLegends()));

        Map<String, String> legendMap = new HashMap<String, String>();
        legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_DEBIT_NOTE_DOCUMENT);
        legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, debitNoteObj.getResolutionCodeValue());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

        return parameterMap;
    }


    public byte[] createDebitNotePDF(DebitNoteObject debitNoteObj, ConfigData configuracion) throws PDFReportException {
        byte[] pdfDocument;
        JasperReport dnJasperReport;
        JasperDesign dnJasperDesign;
        try {
            File debitNoteTemplate = new File(configuracion.getDocumentReportPath());
            if (!debitNoteTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_404.getMessage());
            }

            // Usa los métodos auxiliares para poblar los mapas
            Map<String, Object> parameterMap = populateParameterMap(debitNoteObj, configuracion);

            InputStream inputStream = new BufferedInputStream(new FileInputStream(debitNoteTemplate));
            dnJasperDesign = JRXmlLoader.load(inputStream);
            dnJasperReport = JasperCompileManager.compileReport(dnJasperDesign);

            JasperPrint dnJasperPrint = JasperFillManager.fillReport(dnJasperReport, parameterMap,
                    new JRBeanCollectionDataSource(debitNoteObj.getItemsListDynamic()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(dnJasperPrint, outputStream);
            pdfDocument = outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("createDebitNotePDF() [" + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("createDebitNotePDF() [" + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_441);
        }
        return pdfDocument;
    }

} //PDFDebitNoteCreator
