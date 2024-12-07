/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientrequest.handler;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientrequest.handler.refactorPdf.dto.DespatchAdviceObject;
import service.cloud.request.clientrequest.utils.exception.PDFReportException;
import service.cloud.request.clientrequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VS-LT-06
 */
public class PDFDespatchAdviceCreator {

    Logger logger = LoggerFactory.getLogger(PDFDespatchAdviceCreator.class);

    private static PDFDespatchAdviceCreator instance = null;

    private final JasperDesign iJasperDesign;

    private final JasperReport iJasperReport;

    private Map<String, Object> parameterMap;

    private final String legendSubReportPath;

    private PDFDespatchAdviceCreator(String despatchAdviceReportPath, String legendSubReportPath) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+PDFDespatchAdviceCreator() constructor");
        }
        try {
            File despatchAdviceTemplate = new File(despatchAdviceReportPath);
            if (!despatchAdviceTemplate.isFile()) {
                throw new FileNotFoundException(IVenturaError.ERROR_401.getMessage());
            }
            InputStream inputStream = new BufferedInputStream(new FileInputStream(despatchAdviceTemplate));
            iJasperDesign = JRXmlLoader.load(inputStream);
            iJasperReport = JasperCompileManager.compileReport(iJasperDesign);
            this.legendSubReportPath = legendSubReportPath;
        } catch (FileNotFoundException e) {
            logger.error("PDFDespatchAdviceCreator() FileNotFoundException - ERROR: " + e.getMessage());
            logger.error("PDFDespatchAdviceCreator() FileNotFoundException -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(e.getMessage());
        } catch (Exception e) {
            logger.error("PDFDespatchAdviceCreator() Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            logger.error("PDFDespatchAdviceCreator() Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new PDFReportException(IVenturaError.ERROR_405);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-PDFDespatchAdviceCreator() constructor");
        }
    } //PDFDespatchAdviceCreator

    /**
     * Este metodo obtiene la instancia actual del objeto PDFInvoiceCreator.
     *
     * @param legendSubReportPath Ruta del subreporte de legendas.
     * @return Retorna la instancia de la clase PDFDespatchAdviceCreator.
     * @throws PDFReportException
     */
    public static PDFDespatchAdviceCreator getInstance(String despatchAdviceReportPath, String legendSubReportPath) throws PDFReportException {
        instance = new PDFDespatchAdviceCreator(despatchAdviceReportPath, legendSubReportPath);
        return instance;
    } //getInstance

    /**
     * Este metodo crea un PDF que es la representacion impresa de la guia de
     * remisión electronica.
     *
     * @param despatchAdviceObject Objeto que contiene informacion de la
     *                             factura.
     * @param docUUID              Identificador unica de la factura.
     * @return Retorna un PDF en bytes.
     * @throws PDFReportException
     */
    public byte[] createDespatchAdvicePDF(DespatchAdviceObject despatchAdviceObject, String docUUID, ConfigData configuracion) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+createDespatchAdvicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == despatchAdviceObject) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {

                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.CODIGO_EMBARQUE, despatchAdviceObject.getCodigoEmbarque());
                parameterMap.put(IPDFCreatorConfig.CODIGO_MOTIVO, despatchAdviceObject.getCodigoMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DESCRIPCION_MOTIVO, despatchAdviceObject.getDescripcionMotivoTraslado());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_DESTINO, despatchAdviceObject.getDireccionDestino());
                parameterMap.put(IPDFCreatorConfig.DIRECCION_PARTIDA, despatchAdviceObject.getDireccionPartida());
                parameterMap.put(IPDFCreatorConfig.DOCUMENTO_CONDUCTOR, despatchAdviceObject.getDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.FECHA_EMISION, despatchAdviceObject.getFechaEmision());
                parameterMap.put(IPDFCreatorConfig.FECHA_TRASLADO, despatchAdviceObject.getFechaTraslado());
                parameterMap.put(IPDFCreatorConfig.MODALIDAD_TRASLADO, despatchAdviceObject.getModalidadTraslado());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_CONSUMIDOR, despatchAdviceObject.getNombreConsumidor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_EMISOR, despatchAdviceObject.getNombreEmisor());
                parameterMap.put(IPDFCreatorConfig.NOMBRE_TRANSPORTISTA, despatchAdviceObject.getNombreTransportista());
                parameterMap.put(IPDFCreatorConfig.NUMERO_BULTOS, despatchAdviceObject.getNumeroBultos());
                parameterMap.put(IPDFCreatorConfig.PLACA_VEHICULO, despatchAdviceObject.getPlacaVehiculo());
                parameterMap.put(IPDFCreatorConfig.RUC_TRANSPORTISTA, despatchAdviceObject.getRUCTransportista());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_CONDUCTOR, despatchAdviceObject.getTipoDocumentoConductor());
                parameterMap.put(IPDFCreatorConfig.TIPO_DOCUMENTO_TRANSPORTISTA, despatchAdviceObject.getTipoDocumentoTransportista());
                parameterMap.put(IPDFCreatorConfig.UNIDAD_MEDIDA_PESONETO, despatchAdviceObject.getUMPesoBruto());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, despatchAdviceObject.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, despatchAdviceObject.getNumeroGuia());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, despatchAdviceObject.getEmail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, despatchAdviceObject.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, despatchAdviceObject.getTelefono1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, despatchAdviceObject.getPaginaWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, despatchAdviceObject.getObervaciones());
                parameterMap.put(IPDFCreatorConfig.RUC_CONSUMIDOR, despatchAdviceObject.getNumeroDocConsumidor());
                parameterMap.put(IPDFCreatorConfig.RUC_EMISOR, despatchAdviceObject.getNumeroDocEmisor());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, despatchAdviceObject.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.LICENCIA_CONDUCIR, despatchAdviceObject.getLicenciaConducir());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, despatchAdviceObject.getDespatchAdvicePersonalizacion());

                if (configuracion.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, despatchAdviceObject.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                JasperPrint iJasperPrint = JasperFillManager.fillReport(iJasperReport, parameterMap,
                        new JRBeanCollectionDataSource(despatchAdviceObject.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument =  outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createDespatchAdvicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createDespatchAdvicePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createDespatchAdvicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createInvoicePDF

}
