package service.cloud.request.clientRequest.extras.pdf;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.handler.object.RetentionObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFRetentionCreator  {

    private final Logger logger = Logger.getLogger(PDFRetentionCreator.class);

    // Método para poblar el parameterMap
    private Map<String, Object> populateRetentionParameterMap(RetentionObject retentionObject, ConfigData configData) {
        Map<String, Object> parameterMap = new HashMap<>();
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
        return parameterMap;
    }

    public byte[] createRetentionPDF(RetentionObject retentionObject, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createRetentionPDF() [" + "]");
        }

        if (retentionObject == null) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        }

        byte[] pdfDocument = null;
        JasperReport iJasperReport;
        JasperDesign iJasperDesign;
        try {

            File invoiceTemplate = new File(configData.getDocumentReportPath());
            if (!invoiceTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_401.getMessage());
            }
            // Usa el método auxiliar para poblar el parameterMap
            Map<String, Object> parameterMap = populateRetentionParameterMap(retentionObject, configData);

            InputStream inputStream = new BufferedInputStream(new FileInputStream(invoiceTemplate));
            iJasperDesign = JRXmlLoader.load(inputStream);
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);
            JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap, new JRBeanCollectionDataSource(retentionObject.getItemListDynamic()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
            pdfDocument = outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("createRetentionPDF() [" + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("createRetentionPDF() [" + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_441);
        }

        return pdfDocument;
    }
}
