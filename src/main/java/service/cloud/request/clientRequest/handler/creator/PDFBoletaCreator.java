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
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
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
public class PDFBoletaCreator extends DocumentCreator {

    Logger logger = LoggerFactory.getLogger(PDFBoletaCreator.class);

    /* Patron SINGLETON */
    private static PDFBoletaCreator instance = null;

    private JasperDesign bJasperDesign;

    private JasperReport bJasperReport;

    private Map<String, Object> parameterMap;

    private String legendSubReportPath;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param boletaReportPath    Ruta de la plantilla de la BOLETA.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @throws PDFReportException
     */
    private PDFBoletaCreator(String boletaReportPath, String legendSubReportPath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+PDFBoletaCreator() constructor");
        }
        try {
            File boletaTemplate = new File(boletaReportPath);
            if (!boletaTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_402.getMessage());
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(boletaTemplate));

            /* Carga el template .jrxml */
            bJasperDesign = JRXmlLoader.load(inputStream);

            /* Compila el reporte */
            bJasperReport = JasperCompileManager.compileReport(bJasperDesign);

            /*
             * Guardando en la instancia la ruta del subreporte de
             * leyendas
             */
            this.legendSubReportPath = legendSubReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFBoletaCreator() FileNotFoundException - ERROR: " + e.getMessage());
            logger.error("PDFBoletaCreator() FileNotFoundException -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFBoletaCreator() Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFBoletaCreator() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFBoletaCreator() constructor");
        }
    } //PDFBoletaCreator

    /**
     * Este metodo obtiene la instancia actual del objeto PDFBoletaCreator.
     *
     * @param boletaReportPath    Ruta de la plantilla de la BOLETA.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFBoletaCreator.
     * @throws PDFReportException
     */
    public static PDFBoletaCreator getInstance(String boletaReportPath, String legendSubReportPath) throws PDFReportException {
        /*if (null == instance) {
            instance = new PDFBoletaCreator(boletaReportPath, legendSubReportPath);
        }*/
        instance = new PDFBoletaCreator(boletaReportPath, legendSubReportPath);
        return instance;
    } //getInstance

    /**
     * Este metodo crea un PDF que es la representacion impresa de la boleta de
     * venta electronica.
     *
     * @param boletaObj Objeto que contiene informacion de la boleta de venta.
     * @param docUUID   Identificador unica de la boleta de venta.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
    public byte[] createBoletaPDF(BoletaObject boletaObj, String docUUID, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createBoletaPDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == boletaObj) {
            throw new PDFReportException(IVenturaError.ERROR_407);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                //================================================================================================
                //================================= AGREGANDO INFORMACION AL MAP =================================
                //================================================================================================
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, boletaObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, boletaObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, boletaObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, boletaObj.getCurrencyValue());

                /*if (StringUtils.isNotBlank(boletaObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, boletaObj.getSunatTransaction());
                }*/

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

                /*
                 * IMPORTANTE!!
                 *
                 * Agregar la ruta del directorio en donde se encuentran los
                 * sub-reportes en formato (.jasper)
                 */
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, this.legendSubReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(boletaObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_BOLETA_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, boletaObj.getResolutionCodeValue());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                /*
                 * Generar el reporte con la informacion de la boleta
                 * de venta electronica
                 */
                JasperPrint iJasperPrint = JasperFillManager.fillReport(bJasperReport, parameterMap,
                        new JRBeanCollectionDataSource(boletaObj.getItemsDynamic()));

                /*
                 * Exportar el reporte PDF en una ruta en DISCO
                 */
                String outputPath = USER_TEMPORARY_PATH+ docUUID + IPDFCreatorConfig.EE_PDF;
                JasperExportManager.exportReportToPdfFile(iJasperPrint, outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createBoletaPDF() [" + docUUID + "] Se guardo el PDF en una ruta temportal: " + outputPath);
                }

                /*
                 * Convirtiendo el documento PDF generado en bytes.
                 */
                pdfDocument = convertFileInBytes(outputPath);
                if (logger.isInfoEnabled()) {
                    logger.info("createBoletaPDF() [" + docUUID + "] Se convirtio el PDF en bytes");
                }
            } catch (Exception e) {
                logger.error("createBoletaPDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createBoletaPDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_442);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createBoletaPDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createBoletaPDF

} //PDFBoletaCreator
