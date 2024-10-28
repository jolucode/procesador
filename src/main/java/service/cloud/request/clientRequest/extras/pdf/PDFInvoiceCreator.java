package service.cloud.request.clientRequest.extras.pdf;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.handler.object.InvoiceObject;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Este metodo crea un documento de tipo FACTURA utilizando una plantilla de
 * IREPORT.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class PDFInvoiceCreator extends DocumentCreator {

    private final Logger logger = Logger.getLogger(PDFInvoiceCreator.class);

    /* Patron SINGLETON */
    private static PDFInvoiceCreator instance = null;

    private final JasperDesign iJasperDesign;

    private final JasperReport iJasperReport;

    private Map<String, Object> parameterMap;

    private Map<String, Object> cuotasMap;

    private final String legendSubReportPath;

    private final String paymentDetailReportPath;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param invoiceReportPath   Ruta de la plantilla de la FACTURA.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     */
    private PDFInvoiceCreator(String invoiceReportPath, String legendSubReportPath, String paymentDetailReportPath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+PDFInvoiceCreator() constructor");
        }
        try {
            File invoiceTemplate = new File(invoiceReportPath);
            if (!invoiceTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_401.getMessage());
            }

            InputStream inputStream = new BufferedInputStream(new FileInputStream(invoiceTemplate));
            /* Carga el template .jrxml */
            iJasperDesign = JRXmlLoader.load(inputStream);

            /* Compila el reporte */
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);

            /*
             * Guardando en la instancia la ruta del subreporte de
             * leyendas
             */
            this.legendSubReportPath = legendSubReportPath;
            this.paymentDetailReportPath = paymentDetailReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFInvoiceCreator() FileNotFoundException - ERROR: " + e.getMessage());
            logger.error("PDFInvoiceCreator() FileNotFoundException -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFInvoiceCreator() Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFInvoiceCreator() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFInvoiceCreator() constructor");
        }
    } //PDFInvoiceCreator

    /**
     * Este metodo obtiene la instancia actual del objeto PDFInvoiceCreator.
     *
     * @param invoiceReportPath   Ruta de la plantilla de la FACTURA.
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFInvoiceCreator.
     * @throws PDFReportException
     */
    public static PDFInvoiceCreator getInstance(String invoiceReportPath, String legendSubReportPath, String paymentDetailPath) throws PDFReportException {
        instance = new PDFInvoiceCreator(invoiceReportPath, legendSubReportPath, paymentDetailPath);
        return instance;
    } //getInstance

    /**
     * Este metodo crea un PDF que es la representacion impresa de la factura
     * electronica.
     *
     * @param invoiceObj Objeto que contiene informacion de la factura.
     * @param docUUID    Identificador unica de la factura.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
    public byte[] createInvoicePDF(InvoiceObject invoiceObj, String docUUID, ConfigData configuracion) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createInvoicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == invoiceObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<>();

                cuotasMap = new HashMap<>();

                //================================================================================================
                //================================= AGREGANDO INFORMACION AL MAP =================================
                //================================================================================================
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, invoiceObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, invoiceObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, invoiceObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, invoiceObj.getCurrencyValue());

                /*if (StringUtils.isNotBlank(invoiceObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, invoiceObj.getFormSap());
                }*/

                parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, invoiceObj.getFormSap());

                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, invoiceObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, invoiceObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, invoiceObj.getPorcentajeIGV());

                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, invoiceObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, invoiceObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, invoiceObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, invoiceObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, invoiceObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, invoiceObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, invoiceObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, invoiceObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, invoiceObj.getTelefono_1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, invoiceObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, invoiceObj.getComentarios());
                parameterMap.put(IPDFCreatorConfig.ANTICIPO_APLICADO, invoiceObj.getAnticipos());

                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, invoiceObj.getValidezPDF());

                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON, invoiceObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC, invoiceObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, invoiceObj.getReceiverFiscalAddress());

                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, invoiceObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, invoiceObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PORCISC, invoiceObj.getRetentionPercentage());

                parameterMap.put(IPDFCreatorConfig.PREPAID_AMOUNT_VALUE, invoiceObj.getPrepaidAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, invoiceObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, invoiceObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, invoiceObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, invoiceObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, invoiceObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, invoiceObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, invoiceObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, invoiceObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, invoiceObj.getInafectaAmountValue());
                parameterMap.put(IPDFCreatorConfig.NEW_TOTAL_VALUE, invoiceObj.getNuevoCalculo());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA, invoiceObj.getImpuestoBolsa());
                parameterMap.put(IPDFCreatorConfig.IMPUESTO_BOLSA_MONEDA, invoiceObj.getImpuestoBolsaMoneda());

                if (StringUtils.isNotBlank(invoiceObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, invoiceObj.getGratuitaAmountValue());
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, invoiceObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, invoiceObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, invoiceObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, invoiceObj.getLetterAmountValue());

                /*
                 * IMPORTANTE!!
                 *
                 * Agregar la ruta del directorio en donde se encuentran los
                 * sub-reportes en formato (.jasper)
                 */
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, this.paymentDetailReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(invoiceObj.getItemListDynamicC()));

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, this.legendSubReportPath);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(invoiceObj.getLegends()));

                Map<String, String> legendMap = new HashMap<>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_INVOICE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, invoiceObj.getResolutionCodeValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);


                cuotasMap.put("M1", invoiceObj.getM1());
                cuotasMap.put("M2", invoiceObj.getM2());
                cuotasMap.put("M3", invoiceObj.getM3());

                cuotasMap.put("C1", invoiceObj.getC1());
                cuotasMap.put("C2", invoiceObj.getC2());
                cuotasMap.put("C3", invoiceObj.getC3());

                cuotasMap.put("F1", invoiceObj.getF1());
                cuotasMap.put("F2", invoiceObj.getF2());
                cuotasMap.put("F3", invoiceObj.getF3());

                cuotasMap.put("totalCuotas", invoiceObj.getTotalCuotas());
                cuotasMap.put("metodoPago", invoiceObj.getMetodoPago());
                cuotasMap.put("baseImponibleRetencion", invoiceObj.getBaseImponibleRetencion());

                cuotasMap.put("porcentajeRetencion", invoiceObj.getPorcentajeRetencion());
                cuotasMap.put("montoRetencion", invoiceObj.getMontoRetencion());
                cuotasMap.put("montoPendiente", invoiceObj.getMontoPendiente());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, cuotasMap); // parametros subreporte de cuotas (se pasa como HashMap)

                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, invoiceObj.getInvoicePersonalizacion());

                /*
                 * Generar el reporte con la informacion de la factura
                 * electronica
                 */
                JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap,
                        new JRBeanCollectionDataSource(invoiceObj.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument =  outputStream.toByteArray();;
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
    }
}
