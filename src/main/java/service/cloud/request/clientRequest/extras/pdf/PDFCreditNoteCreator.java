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
import service.cloud.request.clientRequest.handler.object.CreditNoteObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo NOTA DE CREDITO utilizando una
 * plantilla de IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
@Service
public class PDFCreditNoteCreator{

    private final Logger logger = Logger.getLogger(PDFCreditNoteCreator.class);

    private Map<String, Object> populateCuotasMap(CreditNoteObject creditNoteObj) {
        Map<String, Object> cuotasMap = new HashMap<>();
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

        return cuotasMap;
    }

    // Función que separa la creación del mapa de parámetros para la nota de crédito
    private Map<String, Object> populateCreditNoteParameterMap(CreditNoteObject creditNoteObj, ConfigData configData) {
        Map<String, Object> parameterMap = new HashMap<>();
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
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, configData.getPaymentDetailReportPath());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getItemListDynamicC()));
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, configData.getLegendSubReportPath());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getLegends()));

        Map<String, String> legendMap = new HashMap<String, String>();
        legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_CREDIT_NOTE_DOCUMENT);
        legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, creditNoteObj.getResolutionCodeValue());

        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, populateCuotasMap(creditNoteObj)); // parametros subreporte de cuotas (se pasa como HashMap)
        return parameterMap;
    }

    public byte[] createCreditNotePDF(CreditNoteObject creditNoteObj, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createCreditNotePDF() [" + "]");
        }

        if (creditNoteObj == null) {
            throw new PDFReportException(IVenturaError.ERROR_408);
        }

        byte[] pdfDocument = null;
        JasperReport iJasperReport;
        JasperDesign iJasperDesign;
        try {

            File invoiceTemplate = new File(configData.getDocumentReportPath());
            if (!invoiceTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_401.getMessage());
            }
            // Separar la población del mapa de parámetros en una función
            Map<String, Object> parameterMap = populateCreditNoteParameterMap(creditNoteObj, configData);
            Map<String, Object> cuotasMap = populateCuotasMap(creditNoteObj);

            parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, cuotasMap);

            InputStream inputStream = new BufferedInputStream(new FileInputStream(invoiceTemplate));
            iJasperDesign = JRXmlLoader.load(inputStream);
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);

            // Generación del PDF
            JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap, new JRBeanCollectionDataSource(creditNoteObj.getItemsListDynamic()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
            pdfDocument = outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("createCreditNotePDF() [" + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("createCreditNotePDF() [" + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_443);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-createCreditNotePDF() [" + "]");
        }

        return pdfDocument;
    }



} //PDFCreditNoteCreator
