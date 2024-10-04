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
import service.cloud.request.clientRequest.handler.object.PerceptionObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PDFPerceptionCreator {

    private final Logger logger = Logger.getLogger(PDFPerceptionCreator.class);

    private Map<String, Object> populateParameterMap(PerceptionObject perceptionObj, ConfigData configuracion) {
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, perceptionObj.getDocumentIdentifier());
        parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, perceptionObj.getIssueDate());
        if (StringUtils.isNotBlank(perceptionObj.getSunatTransaction())) {
            parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL,
                    IPDFCreatorConfig.OPERATION_TYPE_DSC);
            parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE,
                    perceptionObj.getSunatTransaction());
        }
        parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, perceptionObj.getSenderSocialReason());
        parameterMap.put(IPDFCreatorConfig.SENDER_RUC, perceptionObj.getSenderRuc());
        parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, perceptionObj.getSenderFiscalAddress());
        parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, perceptionObj.getSenderDepProvDist());
        parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, perceptionObj.getSenderContact());
        parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, perceptionObj.getSenderMail());
        parameterMap.put(IPDFCreatorConfig.SENDER_TEL, perceptionObj.getTelValue());
        parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, perceptionObj.getTel2Value());
        parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, perceptionObj.getSenderLogo());
        parameterMap.put(IPDFCreatorConfig.SENDER_WEB, perceptionObj.getWebValue());
        parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, perceptionObj.getValidezPDF());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON, perceptionObj.getReceiverSocialReason());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC, perceptionObj.getReceiverRuc());
        parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, perceptionObj.getTotalAmountValue());
        parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, perceptionObj.getLetterAmountValue());
        return  parameterMap;
    }

    public byte[] createPerceptionPDF(PerceptionObject perceptionObject, ConfigData configuracion) throws PDFReportException {
        byte[] pdfDocument = null;
        JasperReport iJasperReport;
        JasperDesign iJasperDesign;
        try {
            File perceptionTemplate = new File(configuracion.getDocumentReportPath());
            if (!perceptionTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_401.getMessage());
            }

            // Usa los métodos auxiliares para poblar los mapas
            Map<String, Object> parameterMap = populateParameterMap(perceptionObject, configuracion);

            InputStream inputStream = new BufferedInputStream(new FileInputStream(perceptionTemplate));
            iJasperDesign = JRXmlLoader.load(inputStream);
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);

            JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap,
                    new JRBeanCollectionDataSource(perceptionObject.getItemListDynamic()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
            pdfDocument =  outputStream.toByteArray();;

        } catch (Exception e) {
            logger.error("createInvoicePDF() [" + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("createInvoicePDF() [" +"] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_441);
        }
        return pdfDocument;
    }

}

