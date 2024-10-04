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
import service.cloud.request.clientRequest.handler.object.legend.BoletaObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo BOLETA utilizando una plantilla de
 * IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */

@Service
public class PDFBoletaCreator {

    private final Logger logger = Logger.getLogger(PDFBoletaCreator.class);

    private Map<String, Object> populateParameterMapBoleta(BoletaObject boletaObj, ConfigData configData) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, boletaObj.getDocumentIdentifier());
        parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, boletaObj.getIssueDate());
        parameterMap.put(IPDFCreatorConfig.DUE_DATE, boletaObj.getDueDate());
        parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, boletaObj.getCurrencyValue());
        parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, boletaObj.getFormSap());
        parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, boletaObj.getPaymentCondition());
        parameterMap.put(IPDFCreatorConfig.SELL_ORDER, boletaObj.getSellOrder());
        parameterMap.put(IPDFCreatorConfig.SELLER_NAME, boletaObj.getSellerName());
        parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, boletaObj.getRemissionGuides());
        parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, boletaObj.getSenderSocialReason());
        parameterMap.put(IPDFCreatorConfig.SENDER_RUC, boletaObj.getSenderRuc());
        parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, boletaObj.getSenderFiscalAddress());
        parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, boletaObj.getSenderDepProvDist());
        parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, boletaObj.getSenderContact());
        parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, boletaObj.getSenderMail());
        parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, boletaObj.getSenderLogo());
        parameterMap.put(IPDFCreatorConfig.SENDER_TEL, boletaObj.getTelefono());
        parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, boletaObj.getTelefono_1());
        parameterMap.put(IPDFCreatorConfig.SENDER_WEB, boletaObj.getWeb());
        parameterMap.put(IPDFCreatorConfig.ANTICIPO_APLICADO, boletaObj.getAnticipos());
        parameterMap.put(IPDFCreatorConfig.COMMENTS, boletaObj.getComentarios());
        parameterMap.put(IPDFCreatorConfig.PORCIGV, boletaObj.getPorcentajeIGV());
        parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, boletaObj.getValidezPDF());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_FULLNAME, boletaObj.getReceiverFullname());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER, boletaObj.getReceiverIdentifier());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER_TYPE, boletaObj.getReceiverIdentifierType());
        parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, boletaObj.getReceiverFiscalAddress());
        parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, boletaObj.getPerceptionPercentage());
        parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, boletaObj.getPerceptionAmount());
        parameterMap.put(IPDFCreatorConfig.PREPAID_AMOUNT_VALUE, boletaObj.getPrepaidAmountValue());
        parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, boletaObj.getSubtotalValue());
        parameterMap.put(IPDFCreatorConfig.IGV_VALUE, boletaObj.getIgvValue());
        parameterMap.put(IPDFCreatorConfig.ISC_VALUE, boletaObj.getIscValue());
        parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, boletaObj.getAmountValue());
        parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, boletaObj.getDiscountValue());
        parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, boletaObj.getTotalAmountValue());
        parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, boletaObj.getGravadaAmountValue());
        parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, boletaObj.getExoneradaAmountValue());
        parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, boletaObj.getInafectaAmountValue());
        parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, boletaObj.getInvoicePersonalizacion());
        parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA, boletaObj.getImpuestoBolsa());
        parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA_MONEDA, boletaObj.getImpuestoBolsaMoneda());
        parameterMap.put(IPDFCreatorConfig.PORCISC, boletaObj.getPorcentajeISC());
        if (StringUtils.isNotBlank(boletaObj.getGratuitaAmountValue())) {
            parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
            parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, boletaObj.getGratuitaAmountValue());
        }
        if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
            parameterMap.put(IPDFCreatorConfig.CODEQR, boletaObj.getCodeQR());
        } else {
            parameterMap.put(IPDFCreatorConfig.CODEQR, null);
        }
        if (configData.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
            parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, boletaObj.getBarcodeValue());
        } else {
            parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
        }
        if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
            parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, boletaObj.getDigestValue());
        } else {
            parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
        }
        parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, boletaObj.getLetterAmountValue());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, configData.getLegendSubReportPath());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(boletaObj.getLegends()));
        Map<String, String> legendMap = new HashMap<String, String>();
        legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_BOLETA_DOCUMENT);
        legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, boletaObj.getResolutionCodeValue());
        parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);
        return parameterMap;
    }


    public byte[] createBoletaPDF(BoletaObject boletaObj, ConfigData configuracion) throws PDFReportException {
        byte[] pdfDocument;
        JasperReport boletaJasperReport;
        JasperDesign boletaJasperDesign;
        try {
            File boletaTemplate = new File(configuracion.getDocumentReportPath());
            if (!boletaTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_404.getMessage());
            }

            // Usa un método auxiliar para poblar los parámetros del mapa
            Map<String, Object> parameterMap = populateParameterMapBoleta(boletaObj, configuracion);

            InputStream inputStream = new BufferedInputStream(new FileInputStream(boletaTemplate));
            boletaJasperDesign = JRXmlLoader.load(inputStream);
            boletaJasperReport = JasperCompileManager.compileReport(boletaJasperDesign);

            JasperPrint boletaJasperPrint = JasperFillManager.fillReport(boletaJasperReport, parameterMap,
                    new JRBeanCollectionDataSource(boletaObj.getItemsListDynamic()));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JasperExportManager.exportReportToPdfStream(boletaJasperPrint, outputStream);
            pdfDocument = outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("createBoletaPDF() [" + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("createBoletaPDF() [" + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_441);
        }
        return pdfDocument;
    }


} //PDFBoletaCreator
