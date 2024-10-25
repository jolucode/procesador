package service.cloud.request.clientRequest.handler;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.*;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.PDFInvoiceCreator;
import service.cloud.request.clientRequest.handler.creator.*;
import service.cloud.request.clientRequest.handler.object.*;
import service.cloud.request.clientRequest.handler.object.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.object.legend.BoletaObject;
import service.cloud.request.clientRequest.handler.object.legend.LegendObject;
import service.cloud.request.clientRequest.utils.DateConverter;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.TaxCategoryType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.TaxSchemeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.TaxSubtotalType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.TaxTotalType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.LineExtensionAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NameType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TaxableAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATPerceptionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PDFGenerateHandler extends PDFBasicGenerateHandler {

    private final Logger logger = Logger.getLogger(PDFGenerateHandler.class);

    /*
     * Ruta de los templates de reportes
     */
    private String documentReportPath;

    private String legendSubReportPath;

    private String paymentDetailReportPath;

    /* Logo del emisor electronico */
    private String senderLogo;

    /*
     * Codigo de resolucion del emisor electronico
     */
    private String resolutionCode;

    /**
     * Constructor privador para evitar instancias.
     *
     * @param docUUID UUID identificador del documento
     */
    private PDFGenerateHandler(String docUUID) {
        super(docUUID);
    } // PDFGenerateHandler

    /**
     * Este metodo obtiene la instancia de la clase PDFGenerateHandler.
     *
     * @param docUUID UUID identificador del documento.
     * @return Retorna la instancia de la clase PDFGenerateHandler.
     */
    public static synchronized PDFGenerateHandler newInstance(String docUUID) {
        return new PDFGenerateHandler(docUUID);
    } // newInstance

    /**
     * Este metodo guarda las ruta de los templates de reportes.
     *
     * @param documentReportPath  Ruta del template del documento, del cual se
     *                            generara el PDF.
     * @param legendSubReportPath Ruta del template del subreporte de legendas.
     * @param senderLogo          El logo del emisor electronico
     * @param resolutionCode      Codigo de resolucion del emisor electronico
     */
    public void setConfiguration(String documentReportPath, String legendSubReportPath, String paymentDetailReportPath, String senderLogo, String resolutionCode) {
        this.documentReportPath = documentReportPath;
        this.legendSubReportPath = legendSubReportPath;
        this.paymentDetailReportPath = paymentDetailReportPath;
        this.senderLogo = senderLogo;
        this.resolutionCode = resolutionCode;
    } // setConfiguration

    public static InputStream generateQRCode(String qrCodeData, String filePath) {

        try {

            String charset = "utf-8"; // or "ISO-8859-1"
            Map hintMap = new HashMap();

            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
            createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException | IOException ex) {
            java.util.logging.Logger.getLogger(PDFGenerateHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static InputStream generatePDF417Code(String qrCodeData, String filePath, int width, int height, int margin) {

        try {
            BitMatrix bitMatrixFromEncoder = null;
            PDF417 objPdf417 = new PDF417();
            objPdf417.generateBarcodeLogic(qrCodeData, 5);
            objPdf417.setEncoding(StandardCharsets.UTF_8);
            int aspectRatio = 4;
            byte[][] originalScale = objPdf417.getBarcodeMatrix().getScaledMatrix(1, aspectRatio);

            boolean rotated = false;
            if ((height > width) != (originalScale[0].length < originalScale.length)) {
                originalScale = rotateArray(originalScale);
                rotated = true;
            }

            int scaleX = width / originalScale[0].length;
            int scaleY = height / originalScale.length;

            int scale;
            if (scaleX < scaleY) {
                scale = scaleX;
            } else {
                scale = scaleY;
            }

            if (scale > 1) {
                byte[][] scaledMatrix = objPdf417.getBarcodeMatrix().getScaledMatrix(scale, scale * aspectRatio);
                if (rotated) {
                    scaledMatrix = rotateArray(scaledMatrix);
                }
                bitMatrixFromEncoder = bitMatrixFromBitArray(scaledMatrix, margin);
            }
            bitMatrixFromEncoder = bitMatrixFromBitArray(originalScale, margin);

            MatrixToImageWriter.writeToFile(bitMatrixFromEncoder, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));

            FileInputStream fis = new FileInputStream(filePath);
            InputStream is = fis;
            return is;

        } catch (WriterException ex) {

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PDFGenerateHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    private static BitMatrix bitMatrixFromBitArray(byte[][] input, int margin) {
        // Creates the bit matrix with extra space for whitespace
        BitMatrix output = new BitMatrix(input[0].length + 2 * margin, input.length + 2 * margin);
        output.clear();
        for (int y = 0, yOutput = output.getHeight() - margin - 1; y < input.length; y++, yOutput--) {
            byte[] inputY = input[y];
            for (int x = 0; x < input[0].length; x++) {
                // Zero is white in the byte matrix
                if (inputY[x] == 1) {
                    output.set(x + margin, yOutput);
                }
            }
        }
        return output;
    }

    private static byte[][] rotateArray(byte[][] bitarray) {
        byte[][] temp = new byte[bitarray[0].length][bitarray.length];
        for (int ii = 0; ii < bitarray.length; ii++) {
            // This makes the direction consistent on screen when rotating the
            // screen;
            int inverseii = bitarray.length - ii - 1;
            for (int jj = 0; jj < bitarray[0].length; jj++) {
                temp[jj][inverseii] = bitarray[ii][jj];
            }
        }
        return temp;
    }

    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
    }

    public synchronized byte[] generateRetentionPDF(UBLDocumentWRP retentionType, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateRetentionPDF() [" + this.docUUID + "]");
        }
        byte[] perceptionBytes = null;

        try {

            RetentionObject retentionObject = new RetentionObject();
            if (logger.isDebugEnabled()) {
                logger.debug("generateRetentionPDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            retentionObject.setDocumentIdentifier(retentionType.getRetentionType().getId().getValue());
            retentionObject.setIssueDate(formatIssueDate(retentionType.getRetentionType().getIssueDate().getValue()));

            /* Informacion de SUNATTransaction */
            if (logger.isDebugEnabled()) {
                logger.debug("generateRetentionPDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }

            retentionObject.setSenderSocialReason(retentionType.getRetentionType().getAgentParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            retentionObject.setSenderRuc(retentionType.getRetentionType().getAgentParty().getPartyIdentification().get(0).getID().getValue());
            retentionObject.setSenderFiscalAddress(retentionType.getRetentionType().getAgentParty().getPostalAddress().getStreetName().getValue());
            retentionObject.setSenderDepProvDist(formatDepProvDist(retentionType.getRetentionType().getAgentParty().getPostalAddress()));
            retentionObject.setSenderLogo(this.senderLogo);

            retentionObject.setComentarios(retentionType.getTransaccion().getFE_Comentario());
            retentionObject.setTel(retentionType.getTransaccion().getTelefono());
            retentionObject.setTel1(retentionType.getTransaccion().getTelefono_1());
            retentionObject.setSenderMail(retentionType.getTransaccion().getEMail());
            retentionObject.setWeb(retentionType.getTransaccion().getWeb());
            retentionObject.setRegimenRET(retentionType.getTransaccion().getRET_Tasa());
            if (logger.isDebugEnabled()) {
                logger.debug("generateRetentionPDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            retentionObject.setReceiverSocialReason(retentionType.getRetentionType().getReceiverParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            retentionObject.setReceiverRuc(retentionType.getRetentionType().getReceiverParty().getPartyIdentification().get(0).getID().getValue());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            retentionObject.setRetentionItems(getRetentionItems(retentionType.getRetentionType().getSunatRetentionDocumentReference(), retentionType.getRetentionType().getSunatRetentionPercent().getValue()));

            retentionObject.setTotalAmountValue(retentionType.getRetentionType().getTotalInvoiceAmount().getValue().toString());
            BigDecimal montoSoles = new BigDecimal("0.00");
            BigDecimal importeTotal = null;
            BigDecimal importeTotalDOC = new BigDecimal("0.00");
            BigDecimal importeDocumento = new BigDecimal("0.00");
            for (int i = 0; i < retentionType.getTransaccion().getTransaccionComprobantePagoList().size(); i++) {
                montoSoles = montoSoles.add(retentionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getCP_Importe().add(retentionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getCP_ImporteTotal()));
                importeTotalDOC = importeTotalDOC.add(retentionType.getRetentionType().getSunatRetentionDocumentReference().get(i).getPayment().getPaidAmount().getValue());
                importeDocumento = importeDocumento.add(retentionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getCP_ImporteTotal());
            }

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                retentionObject.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                retentionObject.setValidezPDF("");
            }
            retentionObject.setMontoenSoles(montoSoles.toString());
            retentionObject.setMontoTotalDoc(importeTotalDOC.toString());
            retentionObject.setTotal_doc_value(importeDocumento.toString());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            for (int i = 0; i < retentionType.getTransaccion().getTransaccionPropiedadesList().size(); i++) {
                if (retentionType.getTransaccion().getTransaccionPropiedadesList().get(i).getTransaccionPropiedadesPK().getId().equalsIgnoreCase("1000")) {
                    retentionObject.setLetterAmountValue(retentionType.getTransaccion().getTransaccionPropiedadesList().get(i).getValor());
                }
            }

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (TransaccionComprobantePago transaccion : retentionType.getTransaccion().getTransaccionComprobantePagoList()) {

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<String, String>();
                List<String> newlist = new ArrayList<String>();

                /***/
                // Utilizar reflección para obtener los campos y valores del objeto
                for (Field field : transaccion.getClass().getDeclaredFields()) {
                    field.setAccessible(true); // Permitir acceso a campos privados
                    try {
                        Object value = field.get(transaccion);
                        if (value != null) {
                            itemObjectHash.put(field.getName(), value.toString());
                            newlist.add(value.toString());
                        }
                        if(field.getName().equals("DOC_FechaEmision"))
                        {
                            itemObjectHash.put("DOC_FechaEmision", DateConverter.convertToDate(value));
                        }

                        if(field.getName().equals("CP_Fecha"))
                        {
                            itemObjectHash.put("CP_Fecha", DateConverter.convertToDate(value));
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);

            }

            retentionObject.setItemListDynamic(listaItem);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            // retentionObject.setLegends(getLegendList(legendsMap));

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }

            String barcodeValue = generateBarcodeInfoV2(retentionType.getRetentionType().getId().getValue(), IUBLConfig.DOC_RETENTION_CODE, retentionObject.getIssueDate(), retentionType.getRetentionType().getTotalInvoiceAmount().getValue(), BigDecimal.ZERO, retentionType.getRetentionType().getAgentParty(), retentionType.getRetentionType().getReceiverParty(), retentionType.getRetentionType().getUblExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateInvoicePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }

            InputStream inputStream;
            InputStream inputStreamPDF;

            String rutaPath = configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + retentionType.getRetentionType().getId().getValue() + ".png";
            File f = new File(configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            retentionObject.setCodeQR(inputStream);

            f = new File(configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417");
            rutaPath = configData.getRutaBaseDoc() + File.separator + retentionType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + retentionType.getRetentionType().getId().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            retentionObject.setBarcodeValue(inputStreamPDF);

            String digestValue = generateDigestValue(retentionType.getRetentionType().getUblExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            retentionObject.setDigestValue(digestValue);

            retentionObject.setResolutionCodeValue(this.resolutionCode);

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            perceptionBytes = PDFRetentionCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createRetentionPDF(retentionObject, docUUID, configData);

        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return perceptionBytes;

    }

    public synchronized byte[] generateBoletaPDF(UBLDocumentWRP boletaType, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateBoletaPDF() [" + this.docUUID + "]");
        }
        byte[] boletaInBytes = null;

        try {
            BoletaObject boletaObj = new BoletaObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            boletaObj.setDocumentIdentifier(boletaType.getInvoiceType().getID().getValue());
            boletaObj.setIssueDate(formatIssueDate(boletaType.getInvoiceType().getIssueDate().getValue()));

            boletaObj.setFormSap(boletaType.getTransaccion().getFE_FormSAP());

            if (StringUtils.isNotBlank(boletaType.getInvoiceType().getDocumentCurrencyCode().getName())) {
                boletaObj.setCurrencyValue(boletaType.getInvoiceType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                boletaObj.setCurrencyValue(boletaType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }

            if (null != boletaType.getInvoiceType().getNote() && 0 < boletaType.getInvoiceType().getNote().size()) {
                boletaObj.setDueDate(formatDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento()));
            } else {
                boletaObj.setDueDate(formatDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento()));
                //boletaObj.setDueDate(IPDFCreatorConfig.EMPTY_VALUE);
            }
            List<TaxTotalType> taxTotal = boletaType.getInvoiceType().getTaxTotal();
            for (TaxTotalType taxTotalType : taxTotal) {
                List<TaxSubtotalType> taxSubtotal = taxTotalType.getTaxSubtotal();
                for (TaxSubtotalType taxSubtotalType : taxSubtotal) {
                    TaxCategoryType taxCategory = taxSubtotalType.getTaxCategory();
                    TaxableAmountType taxableAmount = taxSubtotalType.getTaxableAmount();
                    TaxSchemeType taxScheme = taxCategory.getTaxScheme();
                    NameType name = taxScheme.getName();
                    String nombreImpuesto = name.getValue();
                    if ("ICBPER".equalsIgnoreCase(nombreImpuesto)) {
                        boletaObj.setImpuestoBolsa(taxableAmount.getCurrencyID() + " " + taxableAmount.getValue().toString());
                        boletaObj.setImpuestoBolsaMoneda(taxableAmount.getCurrencyID());
                    }
                }
            }
            /* Informacion de SUNATTransaction */
            String sunatTransInfo = getSunatTransactionInfo(boletaType.getInvoiceType().getUBLExtensions().getUBLExtension());
            if (StringUtils.isNotBlank(sunatTransInfo)) {
                boletaObj.setSunatTransaction(sunatTransInfo);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo guias de remision.");
            }
            boletaObj.setRemissionGuides(getRemissionGuides(boletaType.getInvoiceType().getDespatchDocumentReference()));

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] Guias de remision: " + boletaObj.getRemissionGuides());
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "]============= remision");
            }
            boletaObj.setPaymentCondition(boletaType.getTransaccion().getDOC_CondPago());
            // No se encontraron impuestos en uno de los items de la transaccion.
            //boletaObj.setPaymentCondition(getContractDocumentReference(boletaType.getBoletaType().getContractDocumentReference(),
            // IUBLConfig.CONTRACT_DOC_REF_PAYMENT_COND_CODE));

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] Condicion_pago: " + boletaObj.getPaymentCondition());
            }

            if (null != boletaType.getTransaccion().getTransaccionContractdocrefList() && 0 < boletaType.getTransaccion().getTransaccionContractdocrefList().size()) {
                Map<String, String> hashedMap = new HashMap<String, String>();
                for (int i = 0; i < boletaType.getTransaccion().getTransaccionContractdocrefList().size(); i++) {
                    hashedMap.put(boletaType.getTransaccion().getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre(), boletaType.getTransaccion().getTransaccionContractdocrefList().get(i).getValor());
                }
                boletaObj.setInvoicePersonalizacion(hashedMap);
            }

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (int i = 0; i < boletaType.getTransaccion().getTransaccionLineasList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateBoletaPDF() [" + this.docUUID + "] Agregando datos al HashMap" + boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<String, String>();
                List<String> newlist = new ArrayList<String>();
                for (int j = 0; j < boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre(), boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    newlist.add(boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }

                }
                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);

            }

            boletaObj.setItemsDynamic(listaItem);

            for (int i = 0; i < boletaObj.getItemsDynamic().size(); i++) {

                for (int j = 0; j < boletaObj.getItemsDynamic().get(i).getLstDinamicaItem().size(); j++) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + boletaObj.getItemsDynamic().get(i).getLstDinamicaItem().get(j));
                    }

                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion de los MONTOS.");
            }
            String currencyCode = boletaType.getInvoiceType().getDocumentCurrencyCode().getValue();

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de Percepcion.");
            }
            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < boletaType.getTransaccion().getTransaccionTotalesList().size(); i++) {
                if (boletaType.getTransaccion().getTransaccionTotalesList().get(i).getTransaccionTotalesPK().getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = boletaType.getTransaccion().getTransaccionTotalesList().get(i).getMonto();
                    perceptionPercentage = boletaType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt();
                    boletaObj.setPerceptionAmount(currencyCode + " " + boletaType.getTransaccion().getTransaccionTotalesList().get(i).getMonto().toString());
                    boletaObj.setPerceptionPercentage(boletaType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < boletaType.getTransaccion().getTransaccionLineasList().size(); i++) {
                for (int j = 0; j < boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().size(); j++) {
                    if (boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        boletaObj.setPorcentajeISC(boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje().setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        retentionpercentage = boletaType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                boletaObj.setPorcentajeISC(retentionpercentage.ZERO.toString());
            }

            if (percepctionAmount == null) {
                boletaObj.setPerceptionAmount(boletaType.getInvoiceType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                boletaObj.setPerceptionPercentage(perceptionPercentage.ZERO.toString());
            }

            BigDecimal prepaidAmount = null;
            /* Agregando el monto de ANTICIPO con valor NEGATIVO */

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo monto de Anticipo. " + boletaType.getTransaccion().getANTICIPO_Monto());
            }

            if (null != boletaType.getInvoiceType().getPrepaidPayment() && !boletaType.getInvoiceType().getPrepaidPayment().isEmpty() && null != boletaType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateBoletaPDF() [" + this.docUUID + "] Monto de Anticipo Mayor a 0. ");
                }

                prepaidAmount = boletaType.getInvoiceType().getLegalMonetaryTotal().getPrepaidAmount().getValue().negate();
                // invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount().getValue().negate();
                boletaObj.setPrepaidAmountValue(getCurrencyV3(prepaidAmount, currencyCode));
            } else {
                boletaObj.setPrepaidAmountValue(currencyCode + " 0.00");
                prepaidAmount = BigDecimal.ZERO;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            boletaObj.setSenderSocialReason(boletaType.getTransaccion().getRazonSocial());
            boletaObj.setSenderRuc(boletaType.getTransaccion().getDocIdentidad_Nro());
            boletaObj.setSenderFiscalAddress(boletaType.getTransaccion().getDIR_NomCalle());
            boletaObj.setSenderDepProvDist(boletaType.getTransaccion().getDIR_Distrito() + " " + boletaType.getTransaccion().getDIR_Provincia() + " " + boletaType.getTransaccion().getDIR_Departamento());
            boletaObj.setSenderContact(boletaType.getTransaccion().getPersonContacto());
            boletaObj.setSenderMail(boletaType.getTransaccion().getEMail());
            boletaObj.setSenderLogo(this.senderLogo);
            boletaObj.setTelefono(boletaType.getTransaccion().getTelefono());
            boletaObj.setTelefono_1(boletaType.getTransaccion().getTelefono_1());
            boletaObj.setWeb(boletaType.getTransaccion().getWeb());
            boletaObj.setWeb(boletaType.getTransaccion().getWeb());
            boletaObj.setPorcentajeIGV(boletaType.getTransaccion().getDOC_PorcImpuesto());
            boletaObj.setComentarios(boletaType.getTransaccion().getFE_Comentario());
//            boletaObj.setImpuestoBolsa(boletaType.getBoletaType());

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                boletaObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                boletaObj.setValidezPDF("");
            }
            List<TransaccionAnticipo> anticipoList = boletaType.getTransaccion().getTransaccionAnticipoList();
            String anticipos = anticipoList.parallelStream().map(TransaccionAnticipo::getAntiDOCSerieCorrelativo).collect(Collectors.joining(" "));
//            String Anticipos = "";
//            for (int i = 0; i < boletaType.getTransaccion().getTransaccionAnticipoList().size(); i++) {
//                Anticipos.concat(boletaType.getTransaccion().getTransaccionAnticipoList().get(i).getAntiDOCSerieCorrelativo() + " ");
//            }
            boletaObj.setAnticipos(anticipos);

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            boletaObj.setReceiverFullname(boletaType.getTransaccion().getSN_RazonSocial());
            boletaObj.setReceiverIdentifier(boletaType.getTransaccion().getSN_DocIdentidad_Nro());
            boletaObj.setReceiverIdentifierType(boletaType.getTransaccion().getSN_DocIdentidad_Tipo());
            boletaObj.setReceiverFiscalAddress(boletaType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + boletaType.getTransaccion().getSN_DIR_Departamento().toUpperCase());
            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            // boletaObj.setBoletaItems(getBoletaItems(boletaType.getBoletaType().getInvoiceLine()));

            BigDecimal subtotalValue = getTransaccionTotales(boletaType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != boletaType.getInvoiceType().getPrepaidPayment() && !boletaType.getInvoiceType().getPrepaidPayment().isEmpty() && null != boletaType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue.add(prepaidAmount.multiply(BigDecimal.ONE.negate())), currencyCode));
            } else {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + boletaType.getTransaccion().getTransaccionContractdocrefList().size());
            }

            BigDecimal igvValue = getTaxTotalValue2(boletaType.getTransaccion().getTransaccionImpuestosList(), IUBLConfig.TAX_TOTAL_IGV_ID);
            boletaObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue(boletaType.getInvoiceType().getTaxTotal(), IUBLConfig.TAX_TOTAL_ISC_ID);
            boletaObj.setIscValue(getCurrency(iscValue, currencyCode));
            boolean exist = Optional.ofNullable(boletaType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount()).isPresent();
            BigDecimal lineExtensionAmount = exist ? boletaType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue() : BigDecimal.ZERO;
            boletaObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));
            if (null != boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount() && null != boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue()) {
                boletaObj.setDiscountValue(getCurrency(boletaType.getInvoiceType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue(), currencyCode));
            } else {
                boletaObj.setDiscountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            BigDecimal payableAmount = boletaType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue();
            boletaObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            boletaObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            boletaObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            boletaObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateBoletaPDF() [" + this.docUUID + "] Existe Op. Gratuitas.");
                }
                boletaObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            } else {
                boletaObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }

            String barcodeValue = generateBarCodeInfoString(boletaType.getTransaccion().getDocIdentidad_Nro(), boletaType.getTransaccion().getDOC_Codigo(), boletaType.getTransaccion().getDOC_Serie(), boletaType.getTransaccion().getDOC_Numero(), boletaType.getInvoiceType().getTaxTotal(), boletaObj.getIssueDate(), boletaType.getTransaccion().getDOC_MontoTotal().toString(), boletaType.getTransaccion().getSN_DocIdentidad_Tipo(), boletaType.getTransaccion().getSN_DocIdentidad_Nro(), boletaType.getInvoiceType().getUBLExtensions());

//            String barcodeValue = generateBarCodeInfoString(invoiceType.getInvoiceType().getID().getValue(), invoiceType.getInvoiceType().getInvoiceTypeCode().getValue(),invoiceObj.getIssueDate(), invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue(), invoiceType.getInvoiceType().getTaxTotal(), invoiceType.getInvoiceType().getAccountingSupplierParty(), invoiceType.getInvoiceType().getAccountingCustomerParty(),invoiceType.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //invoiceObj.setBarcodeValue(barcodeValue);


            String rutaPath = configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + "03" + File.separator + boletaType.getInvoiceType().getID().getValue() + ".png";
            File f = new File(configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoQR" + File.separator + "03");
            if (!f.exists()) {
                f.mkdirs();
            }

            InputStream inputStream = generateQRCode(barcodeValue, rutaPath);
            boletaObj.setCodeQR(inputStream);

            f = new File(configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + "03");
            rutaPath = configData.getRutaBaseDoc() + File.separator + boletaType.getTransaccion().getDocIdentidad_Nro() + File.separator + "CodigoPDF417" + File.separator + "03" + File.separator + boletaType.getInvoiceType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            InputStream inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            boletaObj.setBarcodeValue(inputStreamPDF);
            String digestValue = generateDigestValue(boletaType.getInvoiceType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            boletaObj.setDigestValue(digestValue);

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }

            Map<String, LegendObject> legendsMap = null;

            //if (TipoVersionUBL.boleta.equals("21")) {
            legendsMap = getaddLeyends(boletaType.getInvoiceType().getNote());
            /*} else if (TipoVersionUBL.boleta.equals("20")) {
                legendsMap = getAdditionalProperties(boletaType.getInvoiceType().getUBLExtensions().getUBLExtension());
            }*/
            /*
            Map<String, LegendObject> legendsMap = getaddLeyends(boletaType.getBoletaType().getNote());
             */
            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            boletaObj.setLetterAmountValue(legendLetters.getLegendValue());
            legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            boletaObj.setLegends(getLegendList(legendsMap));

            boletaObj.setResolutionCodeValue(this.resolutionCode);

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            boletaInBytes = PDFBoletaCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createBoletaPDF(boletaObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return boletaInBytes;
    } // generateBoletaPDF


    public synchronized byte[] generateInvoicePDF(UBLDocumentWRP invoiceType, ConfigData configuracion) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateInvoicePDF() [" + this.docUUID + "]");
        }
        byte[] invoiceInBytes = null;
        try {
            InvoiceObject invoiceObj = new InvoiceObject();
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            invoiceObj.setDocumentIdentifier(invoiceType.getInvoiceType().getID().getValue());
            invoiceObj.setIssueDate(formatIssueDate(invoiceType.getInvoiceType().getIssueDate().getValue()));
            List<TaxTotalType> taxTotal = invoiceType.getInvoiceType().getTaxTotal();
            for (TaxTotalType taxTotalType : taxTotal) {
                List<TaxSubtotalType> taxSubtotal = taxTotalType.getTaxSubtotal();
                for (TaxSubtotalType taxSubtotalType : taxSubtotal) {
                    TaxCategoryType taxCategory = taxSubtotalType.getTaxCategory();
                    TaxableAmountType taxableAmount = taxSubtotalType.getTaxableAmount();
                    TaxSchemeType taxScheme = taxCategory.getTaxScheme();
                    NameType name = taxScheme.getName();
                    String nombreImpuesto = name.getValue();
                    if ("ICBPER".equalsIgnoreCase(nombreImpuesto)) {
                        invoiceObj.setImpuestoBolsa(taxableAmount.getValue().toString());
                        invoiceObj.setImpuestoBolsaMoneda(taxableAmount.getCurrencyID());
                    }
                }
            }
            invoiceObj.setFormSap(invoiceType.getTransaccion().getFE_FormSAP());
            if (StringUtils.isNotBlank(invoiceType.getInvoiceType().getDocumentCurrencyCode().getName())) {
                invoiceObj.setCurrencyValue(invoiceType.getInvoiceType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                invoiceObj.setCurrencyValue(invoiceType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }
            if (null != invoiceType.getInvoiceType().getNote() && 0 < invoiceType.getInvoiceType().getNote().size()) {
                invoiceObj.setDueDate(formatDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento()));
                //invoiceObj.setDueDate(formatDueDate(invoiceType.getInvoiceType().getNote().get(0).getValue()));
            } else {
                invoiceObj.setDueDate(formatDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento()));
            }

            String Anticipos = "";
            for (int i = 0; i < invoiceType.getTransaccion().getTransaccionAnticipoList().size(); i++) {
                Anticipos.concat(invoiceType.getTransaccion().getTransaccionAnticipoList().get(i).getAntiDOCSerieCorrelativo() + " ");
            }
            invoiceObj.setAnticipos(Anticipos);
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo guias de remision.");
            }
            invoiceObj.setRemissionGuides(getRemissionGuides(invoiceType.getInvoiceType().getDespatchDocumentReference()));
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoicePDF() [" + this.docUUID + "]============= condicion pago------");
            }
            invoiceObj.setPaymentCondition(invoiceType.getTransaccion().getDOC_CondPago());
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }

            invoiceObj.setSenderSocialReason(invoiceType.getTransaccion().getRazonSocial());
            invoiceObj.setSenderRuc(invoiceType.getTransaccion().getDocIdentidad_Nro());
            invoiceObj.setSenderFiscalAddress(invoiceType.getTransaccion().getDIR_NomCalle());
            invoiceObj.setSenderDepProvDist(invoiceType.getTransaccion().getDIR_Distrito() + " " + invoiceType.getTransaccion().getDIR_Provincia() + " " + invoiceType.getTransaccion().getDIR_Departamento());
            invoiceObj.setSenderContact(invoiceType.getTransaccion().getPersonContacto());
            invoiceObj.setSenderMail(invoiceType.getTransaccion().getEMail());
            invoiceObj.setSenderLogo(this.senderLogo);
            invoiceObj.setTelefono(invoiceType.getTransaccion().getTelefono());
            invoiceObj.setTelefono_1(invoiceType.getTransaccion().getTelefono_1());
            invoiceObj.setWeb(invoiceType.getTransaccion().getWeb());
            invoiceObj.setPorcentajeIGV(invoiceType.getTransaccion().getDOC_PorcImpuesto());
            invoiceObj.setComentarios(invoiceType.getTransaccion().getFE_Comentario());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            invoiceObj.setReceiverSocialReason(invoiceType.getTransaccion().getSN_RazonSocial());
            invoiceObj.setReceiverRuc(invoiceType.getTransaccion().getSN_DocIdentidad_Nro());
            invoiceObj.setReceiverFiscalAddress(invoiceType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + invoiceType.getTransaccion().getSN_DIR_Departamento().toUpperCase());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            invoiceObj.setInvoiceItems(getInvoiceItems(invoiceType.getInvoiceType().getInvoiceLine()));

            String currencyCode = invoiceType.getInvoiceType().getDocumentCurrencyCode().getValue();

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de Percepcion.");
            }

            if (Boolean.parseBoolean(configuracion.getPdfBorrador())) {
                invoiceObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                invoiceObj.setValidezPDF("");
            }

            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < invoiceType.getTransaccion().getTransaccionTotalesList().size(); i++) {
                if (invoiceType.getTransaccion().getTransaccionTotalesList().get(i).getTransaccionTotalesPK().getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = invoiceType.getTransaccion().getTransaccionTotalesList().get(i).getMonto();
                    perceptionPercentage = invoiceType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt();
                    invoiceObj.setPerceptionAmount(currencyCode + " " + invoiceType.getTransaccion().getTransaccionTotalesList().get(i).getMonto().toString());
                    invoiceObj.setPerceptionPercentage(invoiceType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < invoiceType.getTransaccion().getTransaccionLineasList().size(); i++) {
                for (int j = 0; j < invoiceType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().size(); j++) {
                    if (invoiceType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        invoiceObj.setRetentionPercentage(invoiceType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = invoiceType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                invoiceObj.setRetentionPercentage(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                invoiceObj.setPerceptionAmount(invoiceType.getInvoiceType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                invoiceObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de los MONTOS.");
            }

            BigDecimal prepaidAmount = null;

            /* Agregando el monto de ANTICIPO con valor NEGATIVO */
            if (null != invoiceType.getInvoiceType().getPrepaidPayment() && !invoiceType.getInvoiceType().getPrepaidPayment().isEmpty() && null != invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                prepaidAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getPrepaidAmount().getValue().negate();
                // invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount().getValue().negate();
                invoiceObj.setPrepaidAmountValue(getCurrencyV3(prepaidAmount, currencyCode));
            } else {
                invoiceObj.setPrepaidAmountValue(currencyCode + " 0.00");
                prepaidAmount = BigDecimal.ZERO;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de LINEAS personalizados." + invoiceType.getTransaccion().getTransaccionLineasList().size());
            }

            // Cuotas

            List<WrapperItemObject> listaItemC = new ArrayList<>();

            List<TransaccionCuotas> transaccionCuotas = invoiceType.getTransaccion().getTransaccionCuotas();

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            BigDecimal montoRetencion = (invoiceType.getTransaccion().getMontoRetencion() != null ? invoiceType.getTransaccion().getMontoRetencion() : new BigDecimal("0.0"));

            Integer totalCuotas = 0;
            BigDecimal montoPendiente = BigDecimal.ZERO;
            String metodoPago = "";
            BigDecimal m1 = BigDecimal.ZERO, m2 = BigDecimal.ZERO, m3 = BigDecimal.ZERO;
            String f1 = "", f2 = "", f3 = "";
            String c1 = "", c2 = "", c3 = "";

            for (TransaccionCuotas transaccionCuota : transaccionCuotas) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                itemObjectHash.put("Cuota", transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                newlist.add(transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                itemObjectHash.put("FechaCuota", df.format(transaccionCuota.getFechaCuota()));
                newlist.add(df.format(transaccionCuota.getFechaCuota()));
                itemObjectHash.put("MontoCuota", transaccionCuota.getMontoCuota().toString());
                newlist.add(transaccionCuota.getMontoCuota().toString());
                itemObjectHash.put("FormaPago", transaccionCuota.getFormaPago());
                newlist.add(transaccionCuota.getFormaPago());

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItemC.add(itemObject);
                totalCuotas++;
                montoPendiente = montoPendiente.add(transaccionCuota.getMontoCuota());
                if (totalCuotas == 1) {
                    metodoPago = transaccionCuota.getFormaPago();
                    m1 = transaccionCuota.getMontoCuota();
                    f1 = df.format(transaccionCuota.getFechaCuota());
                    c1 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 2) {
                    m2 = transaccionCuota.getMontoCuota();
                    f2 = df.format(transaccionCuota.getFechaCuota());
                    c2 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 3) {
                    m3 = transaccionCuota.getMontoCuota();
                    f3 = df.format(transaccionCuota.getFechaCuota());
                    c3 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
            }
            invoiceObj.setItemListDynamicC(listaItemC);
            invoiceObj.setTotalCuotas(totalCuotas);
            invoiceObj.setMontoPendiente(montoPendiente);
            invoiceObj.setMetodoPago(metodoPago);
            invoiceObj.setM1(m1);
            invoiceObj.setM2(m2);
            invoiceObj.setM3(m3);
            invoiceObj.setF1(f1);
            invoiceObj.setF2(f2);
            invoiceObj.setF3(f3);
            invoiceObj.setC1(c1);
            invoiceObj.setC2(c2);
            invoiceObj.setC3(c3);
            invoiceObj.setPorcentajeRetencion(retentionpercentage == null ? BigDecimal.ZERO : retentionpercentage);
            invoiceObj.setMontoRetencion(montoRetencion);
            BigDecimal baseImponibleRetencion;

            if (invoiceObj.getPorcentajeRetencion() != BigDecimal.ZERO) {
                baseImponibleRetencion = montoRetencion.divide(invoiceObj.getPorcentajeRetencion().divide(new BigDecimal("100.0")));
            } else {
                baseImponibleRetencion = BigDecimal.ZERO;
            }
            invoiceObj.setBaseImponibleRetencion(baseImponibleRetencion);

            // fin Cuotas


            List<WrapperItemObject> listaItem = new ArrayList<>();

            List<TransaccionLineas> transaccionLineas = invoiceType.getTransaccion().getTransaccionLineasList();
            for (TransaccionLineas transaccionLinea : transaccionLineas) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateInvoicePDF() [" + this.docUUID + "] Agregando datos al HashMap" + transaccionLinea.getTransaccionLineasUsucamposList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();
                List<TransaccionLineasUsucampos> transaccionLineasUsucampos = transaccionLinea.getTransaccionLineasUsucamposList();
                for (TransaccionLineasUsucampos lineasUsucampos : transaccionLineasUsucampos) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + lineasUsucampos.getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(lineasUsucampos.getUsuariocampos().getNombre(), lineasUsucampos.getValor());
                    newlist.add(lineasUsucampos.getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }

                }

                List<TransaccionContractdocref> contractdocrefs = invoiceType.getTransaccion().getTransaccionContractdocrefList();
                for (TransaccionContractdocref contractdocref : contractdocrefs) {
                    itemObjectHash.put(contractdocref.getUsuariocampos().getNombre(), contractdocref.getValor());
                    newlist.add(contractdocref.getValor());
                }


                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }
            invoiceObj.setItemListDynamic(listaItem);
            for (int i = 0; i < invoiceObj.getItemListDynamic().size(); i++) {
                for (int j = 0; j < invoiceObj.getItemListDynamic().get(i).getLstDinamicaItem().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + invoiceObj.getItemListDynamic().get(i).getLstDinamicaItem().get(j));
                    }
                }
            }
            BigDecimal subtotalValue = getTransaccionTotales(invoiceType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != invoiceType.getInvoiceType().getPrepaidPayment() && !invoiceType.getInvoiceType().getPrepaidPayment().isEmpty() && null != invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                invoiceObj.setSubtotalValue(getCurrency(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue(), currencyCode));
            } else {
                invoiceObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + invoiceType.getTransaccion().getTransaccionContractdocrefList().size());
            }
            if (null != invoiceType.getTransaccion().getTransaccionContractdocrefList() && 0 < invoiceType.getTransaccion().getTransaccionContractdocrefList().size()) {
                Map<String, String> hashedMap = new HashMap<String, String>();
                for (int i = 0; i < invoiceType.getTransaccion().getTransaccionContractdocrefList().size(); i++) {
                    hashedMap.put(invoiceType.getTransaccion().getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre(), invoiceType.getTransaccion().getTransaccionContractdocrefList().get(i).getValor());
                }
                invoiceObj.setInvoicePersonalizacion(hashedMap);
            }

            BigDecimal igvValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransaccionImpuestosList(), IUBLConfig.TAX_TOTAL_IGV_ID);
            invoiceObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransaccionImpuestosList(), IUBLConfig.TAX_TOTAL_ISC_ID);
            invoiceObj.setIscValue(getCurrency(iscValue, currencyCode));

            Optional<LineExtensionAmountType> optional = Optional.ofNullable(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount());
            if (optional.isPresent()) {
                BigDecimal lineExtensionAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue();
                invoiceObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));
            } else {
                invoiceObj.setAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            BigDecimal descuento = null;
            Transaccion transaccion = invoiceType.getTransaccion();
            BigDecimal docDescuentoTotal = transaccion.getDOC_Descuento();
            if (BigDecimal.ZERO.compareTo(docDescuentoTotal) != 0) {
                descuento = docDescuentoTotal.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_PAYABLEAMOUNT, RoundingMode.HALF_UP);
                invoiceObj.setDiscountValue(getCurrencyV3(descuento, currencyCode));
            } else {
                invoiceObj.setDiscountValue(getCurrencyV3(BigDecimal.ZERO, currencyCode));
                descuento = BigDecimal.ZERO;
            }

            BigDecimal payableAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue();
            invoiceObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            invoiceObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            invoiceObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            invoiceObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateInvoicePDF() [" + this.docUUID + "] Existe Op. Gratuitas.");
                }
                invoiceObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            }
            if (gratuitaAmount.equals(BigDecimal.ZERO)) {
                invoiceObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            invoiceObj.setNuevoCalculo(getCurrency(subtotalValue.add(prepaidAmount.multiply(BigDecimal.ONE.negate()).add(prepaidAmount).subtract(descuento)), currencyCode));

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }

            String barcodeValue = generateBarCodeInfoString(invoiceType.getTransaccion().getDocIdentidad_Nro(), invoiceType.getTransaccion().getDOC_Codigo(), invoiceType.getTransaccion().getDOC_Serie(), invoiceType.getTransaccion().getDOC_Numero(), taxTotal, invoiceObj.getIssueDate(), invoiceType.getTransaccion().getDOC_MontoTotal().toString(), invoiceType.getTransaccion().getSN_DocIdentidad_Tipo(), invoiceType.getTransaccion().getSN_DocIdentidad_Nro(), invoiceType.getInvoiceType().getUBLExtensions());

//            String barcodeValue = generateBarCodeInfoString(invoiceType.getInvoiceType().getID().getValue(), invoiceType.getInvoiceType().getInvoiceTypeCode().getValue(),invoiceObj.getIssueDate(), invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue(), invoiceType.getInvoiceType().getTaxTotal(), invoiceType.getInvoiceType().getAccountingSupplierParty(), invoiceType.getInvoiceType().getAccountingCustomerParty(),invoiceType.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoicePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //invoiceObj.setBarcodeValue(barcodeValue);

            InputStream inputStream;
            InputStream inputStreamPDF;
            String rutaPath = ".." + File.separator + "CodigoQR" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "CodigoQR" + File.separator + "01");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            invoiceObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "CodigoPDF417" + File.separator + "01");
            rutaPath = ".." + File.separator + "CodigoPDF417" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            invoiceObj.setBarcodeValue(inputStreamPDF);
            String digestValue = generateDigestValue(invoiceType.getInvoiceType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            invoiceObj.setDigestValue(digestValue);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }
            Map<String, LegendObject> legendsMap = null;

            //if (TipoVersionUBL.factura.equals("21")) {
            legendsMap = getaddLeyends(invoiceType.getInvoiceType().getNote());
            //} else if (TipoVersionUBL.factura.equals("20")) {
            //    legendsMap = getAdditionalProperties(invoiceType.getInvoiceType().getUBLExtensions().getUBLExtension());

            /*
            Map<String, LegendObject> legendsMap = getAdditionalProperties(invoiceType
                    .getInvoiceType().getUBLExtensions().getUBLExtension());
             */

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            invoiceObj.setLetterAmountValue(legendLetters.getLegendValue());
            legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            invoiceObj.setLegends(getLegendList(legendsMap));

            invoiceObj.setResolutionCodeValue(this.resolutionCode);

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            invoiceInBytes = PDFInvoiceCreator.getInstance(this.documentReportPath, this.legendSubReportPath, this.paymentDetailReportPath).createInvoicePDF(invoiceObj, docUUID, configuracion);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return invoiceInBytes;
    } // generateInvoicePDF

    public synchronized byte[] generateDebitNotePDF(UBLDocumentWRP debitNoteType, List<TransaccionTotales> transactionTotalList, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateDebitNotePDF() [" + this.docUUID + "]");
        }
        byte[] debitNoteInBytes = null;

        try {
            DebitNoteObject debitNoteObj = new DebitNoteObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            debitNoteObj.setDocumentIdentifier(debitNoteType.getDebitNoteType().getID().getValue());
            debitNoteObj.setIssueDate(formatIssueDate(debitNoteType.getDebitNoteType().getIssueDate().getValue()));

            if (StringUtils.isNotBlank(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getName())) {
                debitNoteObj.setCurrencyValue(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                debitNoteObj.setCurrencyValue(debitNoteType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }

            /* Informacion de SUNATTransaction */
            String sunatTransInfo = getSunatTransactionInfo(debitNoteType.getDebitNoteType().getUBLExtensions().getUBLExtension());
            if (StringUtils.isNotBlank(sunatTransInfo)) {
                debitNoteObj.setSunatTransaction(sunatTransInfo);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo guias de remision.");
            }
            debitNoteObj.setRemissionGuides(getRemissionGuides(debitNoteType.getDebitNoteType().getDespatchDocumentReference()));

            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNotePDF() [" + this.docUUID + "] Guias de remision: " + debitNoteObj.getRemissionGuides());
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNotePDF() [" + this.docUUID + "]============= remision");
            }

            debitNoteObj.setPaymentCondition(debitNoteType.getTransaccion().getDOC_CondPago());

            debitNoteObj.setSellOrder(getContractDocumentReference(debitNoteType.getDebitNoteType().getContractDocumentReference(), IUBLConfig.CONTRACT_DOC_REF_SELL_ORDER_CODE));
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNotePDF() [" + this.docUUID + "] Condicion_pago: " + debitNoteObj.getPaymentCondition());
                logger.info("generateDebitNotePDF() [" + this.docUUID + "] Orden de venta: " + debitNoteObj.getSellOrder());
                logger.info("generateDebitNotePDF() [" + this.docUUID + "] Nombre_vendedor: " + debitNoteObj.getSellerName());
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNotePDF() [" + this.docUUID + "]============= condicion pago------");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion de la NOTA DE DEBITO");
            }
            debitNoteObj.setTypeOfDebitNote(debitNoteType.getTransaccion().getREFDOC_MotivCode());
            debitNoteObj.setDescOfDebitNote(debitNoteType.getDebitNoteType().getDiscrepancyResponse().get(0).getDescription().get(0).getValue().toUpperCase());
            debitNoteObj.setDocumentReferenceToCn(getDocumentReferenceValue(debitNoteType.getDebitNoteType().getBillingReference().get(0)));
            debitNoteObj.setDateDocumentReference(debitNoteType.getTransaccion().getFechaDOCRef());

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            debitNoteObj.setSenderSocialReason(debitNoteType.getTransaccion().getRazonSocial());
            debitNoteObj.setSenderRuc(debitNoteType.getTransaccion().getDocIdentidad_Nro());
            debitNoteObj.setSenderFiscalAddress(debitNoteType.getTransaccion().getDIR_NomCalle());
            debitNoteObj.setSenderDepProvDist(debitNoteType.getTransaccion().getDIR_Distrito() + " " + debitNoteType.getTransaccion().getDIR_Provincia() + " " + debitNoteType.getTransaccion().getDIR_Departamento());
            debitNoteObj.setSenderContact(debitNoteType.getTransaccion().getPersonContacto());
            debitNoteObj.setSenderMail(debitNoteType.getTransaccion().getEMail());
            debitNoteObj.setSenderLogo(this.senderLogo);
            debitNoteObj.setWeb(debitNoteType.getTransaccion().getWeb());
            debitNoteObj.setPorcentajeIGV(debitNoteType.getTransaccion().getDOC_PorcImpuesto());
            debitNoteObj.setComentarios(debitNoteType.getTransaccion().getFE_Comentario());

            debitNoteObj.setTelefono(debitNoteType.getTransaccion().getTelefono());
            debitNoteObj.setTelefono_1(debitNoteType.getTransaccion().getTelefono_1());

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + debitNoteType.getTransaccion().getTransaccionContractdocrefList().size());
            }
            if (null != debitNoteType.getTransaccion().getTransaccionContractdocrefList() && 0 < debitNoteType.getTransaccion().getTransaccionContractdocrefList().size()) {
                Map<String, String> hashedMap = new HashMap<>();
                for (int i = 0; i < debitNoteType.getTransaccion().getTransaccionContractdocrefList().size(); i++) {
                    hashedMap.put(debitNoteType.getTransaccion().getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre(), debitNoteType.getTransaccion().getTransaccionContractdocrefList().get(i).getValor());
                }
                debitNoteObj.setInvoicePersonalizacion(hashedMap);
            }

            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < debitNoteType.getTransaccion().getTransaccionLineasList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Agregando datos al HashMap" + debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();
                for (int j = 0; j < debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre(), debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    newlist.add(debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }

                }
                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);

            }

            debitNoteObj.setItemsListDynamic(listaItem);

            for (int i = 0; i < debitNoteObj.getItemsListDynamic().size(); i++) {

                for (int j = 0; j < debitNoteObj.getItemsListDynamic().get(i).getLstDinamicaItem().size(); j++) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + debitNoteObj.getItemsListDynamic().get(i).getLstDinamicaItem().get(j));
                    }

                }

            }
            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            debitNoteObj.setReceiverRegistrationName(debitNoteType.getTransaccion().getSN_RazonSocial());
            debitNoteObj.setReceiverIdentifier(debitNoteType.getTransaccion().getSN_DocIdentidad_Nro());
            debitNoteObj.setReceiverIdentifierType(debitNoteType.getTransaccion().getSN_DocIdentidad_Tipo());

            if (debitNoteType.getDebitNoteType().getID().getValue().startsWith(IUBLConfig.INVOICE_SERIE_PREFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDebitNotePDF() [" + this.docUUID + "] El receptor es de un documento afectado de tipo FACTURA.");
                }
                debitNoteObj.setReceiverFiscalAddress(debitNoteType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Departamento().toUpperCase());
            } else if (debitNoteType.getDebitNoteType().getID().getValue().startsWith(IUBLConfig.BOLETA_SERIE_PREFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDebitNotePDF() [" + this.docUUID + "] El receptor es de un documento afectado de tipo BOLETA.");
                }
                debitNoteObj.setReceiverFiscalAddress(debitNoteType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + debitNoteType.getTransaccion().getSN_DIR_Departamento().toUpperCase());
            } else {
                logger.error("generateDebitNotePDF() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_431.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_431);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            // debitNoteObj.setDebitNoteItems(getDebitNoteItems(debitNoteType.getDebitNoteType().getDebitNoteLine()));

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion de los MONTOS.");
            }
            String currencyCode = debitNoteType.getTransaccion().getDOC_MON_Codigo();
            BigDecimal subtotalValue = getSubtotalValueFromTransaction(transactionTotalList, debitNoteObj.getDocumentIdentifier());
//
            debitNoteObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            BigDecimal igvValue = getTaxTotalValue2(debitNoteType.getTransaccion().getTransaccionImpuestosList(), IUBLConfig.TAX_TOTAL_IGV_ID);

            debitNoteObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue(debitNoteType.getDebitNoteType().getTaxTotal(), IUBLConfig.TAX_TOTAL_ISC_ID);
            debitNoteObj.setIscValue(getCurrency(iscValue, currencyCode));

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de la percepción.");
            }

            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < debitNoteType.getTransaccion().getTransaccionTotalesList().size(); i++) {
                if (debitNoteType.getTransaccion().getTransaccionTotalesList().get(i).getTransaccionTotalesPK().getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = debitNoteType.getTransaccion().getTransaccionTotalesList().get(i).getMonto();
                    perceptionPercentage = debitNoteType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt();
                    debitNoteObj.setPerceptionAmount(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getValue() + " " + debitNoteType.getTransaccion().getTransaccionTotalesList().get(i).getMonto().toString());
                    debitNoteObj.setPerceptionPercentage(debitNoteType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < debitNoteType.getTransaccion().getTransaccionLineasList().size(); i++) {
                for (int j = 0; j < debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().size(); j++) {
                    if (debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        debitNoteObj.setISCPercetange(debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje().setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        retentionpercentage = debitNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                debitNoteObj.setISCPercetange(retentionpercentage.ZERO.toString());
            }

            if (percepctionAmount == null) {
                debitNoteObj.setPerceptionAmount(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                debitNoteObj.setPerceptionPercentage(perceptionPercentage.ZERO.toString());
            }

            if (null != debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount() && null != debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount().getValue()) {
                debitNoteObj.setDiscountValue(getCurrency(debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount().getValue(), currencyCode));
            } else {
                debitNoteObj.setDiscountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            BigDecimal payableAmount = debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getPayableAmount().getValue();
            debitNoteObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            debitNoteObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            debitNoteObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            debitNoteObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Existe Op. Gratuitas.");
                }
                debitNoteObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            } else {
                debitNoteObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }

            String barcodeValue = generateBarCodeInfoString(debitNoteType.getTransaccion().getDocIdentidad_Nro(), debitNoteType.getTransaccion().getDOC_Codigo(), debitNoteType.getTransaccion().getDOC_Serie(), debitNoteType.getTransaccion().getDOC_Numero(), debitNoteType.getDebitNoteType().getTaxTotal(), debitNoteObj.getIssueDate(), debitNoteType.getTransaccion().getDOC_MontoTotal().toString(), debitNoteType.getTransaccion().getSN_DocIdentidad_Tipo(), debitNoteType.getTransaccion().getSN_DocIdentidad_Nro(), debitNoteType.getDebitNoteType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }

            InputStream inputStream;
            InputStream inputStreamPDF;
            String rutaPath = ".." + File.separator + "CodigoQR" + File.separator + "08" + File.separator + debitNoteType.getDebitNoteType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "CodigoQR" + File.separator + "08");
            if (!f.exists()) {
                f.mkdirs();
            }

            if (logger.isInfoEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] rutaPath: \n" + rutaPath);
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            debitNoteObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "CodigoPDF417" + File.separator + "08");
            rutaPath = ".." + File.separator + "CodigoPDF417" + File.separator + debitNoteType.getDebitNoteType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            debitNoteObj.setBarcodeValue(inputStreamPDF);

            String digestValue = generateDigestValue(debitNoteType.getDebitNoteType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            debitNoteObj.setDigestValue(digestValue);

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                debitNoteObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                debitNoteObj.setValidezPDF("");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }

            Map<String, LegendObject> legendsMap = null;
            legendsMap = getaddLeyends(debitNoteType.getDebitNoteType().getNote());
            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            debitNoteObj.setLetterAmountValue(legendLetters.getLegendValue());
            legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            debitNoteObj.setLegends(getLegendList(legendsMap));

            debitNoteObj.setResolutionCodeValue(this.resolutionCode);

            debitNoteInBytes = PDFDebitNoteCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createDebitNotePDF(debitNoteObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateDebitNotePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateDebitNotePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateDebitNotePDF() [" + this.docUUID + "]");
        }
        return debitNoteInBytes;
    } // generateDebitNotePDF

    public synchronized byte[] generateCreditNotePDF(UBLDocumentWRP creditNoteType, List<TransaccionTotales> transaccionTotales, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateCreditNotePDF() [" + this.docUUID + "]");
        }
        byte[] creditNoteInBytes = null;

        try {
            CreditNoteObject creditNoteObj = new CreditNoteObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            creditNoteObj.setDocumentIdentifier(creditNoteType.getCreditNoteType().getID().getValue());
            creditNoteObj.setIssueDate(formatIssueDate(creditNoteType.getCreditNoteType().getIssueDate().getValue()));

            if (StringUtils.isNotBlank(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getName())) {
                creditNoteObj.setCurrencyValue(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getName().toUpperCase());
            } else {
                creditNoteObj.setCurrencyValue(creditNoteType.getTransaccion().getDOC_MON_Nombre().toUpperCase());
            }

            if (null != creditNoteType.getCreditNoteType().getNote() && 0 < creditNoteType.getCreditNoteType().getNote().size()) {
                creditNoteObj.setDueDate(formatDueDate(creditNoteType.getTransaccion().getDOC_FechaVencimiento()));
            } else {
                creditNoteObj.setDueDate(formatDueDate(creditNoteType.getTransaccion().getDOC_FechaVencimiento()));
            }

            /* Informacion de SUNATTransaction */
            String sunatTransInfo = getSunatTransactionInfo(creditNoteType.getCreditNoteType().getUBLExtensions().getUBLExtension());
            if (StringUtils.isNotBlank(sunatTransInfo)) {
                creditNoteObj.setSunatTransaction(sunatTransInfo);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo guias de remision.");
            }
            creditNoteObj.setRemissionGuides(getRemissionGuides(creditNoteType.getCreditNoteType().getDespatchDocumentReference()));

            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "] Guias de remision: " + creditNoteObj.getRemissionGuides());
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "]============= remision");
            }

            creditNoteObj.setPaymentCondition(creditNoteType.getTransaccion().getDOC_CondPago());

            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "]============= remision");
            }
            creditNoteObj.setDateDocumentReference(creditNoteType.getTransaccion().getFechaDOCRef());

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + creditNoteType.getTransaccion().getTransaccionContractdocrefList().size());
            }
            if (null != creditNoteType.getTransaccion().getTransaccionContractdocrefList() && 0 < creditNoteType.getTransaccion().getTransaccionContractdocrefList().size()) {
                Map<String, String> hashedMap = new HashMap<String, String>();
                for (int i = 0; i < creditNoteType.getTransaccion().getTransaccionContractdocrefList().size(); i++) {
                    hashedMap.put(creditNoteType.getTransaccion().getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre(), creditNoteType.getTransaccion().getTransaccionContractdocrefList().get(i).getValor());
                }
                creditNoteObj.setInvoicePersonalizacion(hashedMap);
            }
            // Cuotas

            List<WrapperItemObject> listaItemC = new ArrayList<>();

            List<TransaccionCuotas> transaccionCuotas = creditNoteType.getTransaccion().getTransaccionCuotas();

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            //BigDecimal montoRetencion  = (creditNoteType.getTransaccion().getMontoRetencion() != null ? creditNoteType.getTransaccion().getMontoRetencion() : new BigDecimal(0.0));
            BigDecimal montoRetencion = BigDecimal.ZERO; // ocultar retencion en el subreporte


            Integer totalCuotas = 0;
            BigDecimal montoPendiente = BigDecimal.ZERO;
            String metodoPago = "";
            BigDecimal m1 = BigDecimal.ZERO, m2 = BigDecimal.ZERO, m3 = BigDecimal.ZERO;
            String f1 = "", f2 = "", f3 = "";
            String c1 = "", c2 = "", c3 = "";

            for (TransaccionCuotas transaccionCuota : transaccionCuotas) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                itemObjectHash.put("Cuota", transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                newlist.add(transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                itemObjectHash.put("FechaCuota", df.format(transaccionCuota.getFechaCuota()));
                newlist.add(df.format(transaccionCuota.getFechaCuota()));
                itemObjectHash.put("MontoCuota", transaccionCuota.getMontoCuota().toString());
                newlist.add(transaccionCuota.getMontoCuota().toString());
                itemObjectHash.put("FormaPago", transaccionCuota.getFormaPago());
                newlist.add(transaccionCuota.getFormaPago());

                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItemC.add(itemObject);
                totalCuotas++;
                montoPendiente = montoPendiente.add(transaccionCuota.getMontoCuota());
                if (totalCuotas == 1) {
                    metodoPago = transaccionCuota.getFormaPago();
                    m1 = transaccionCuota.getMontoCuota();
                    f1 = df.format(transaccionCuota.getFechaCuota());
                    c1 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 2) {
                    m2 = transaccionCuota.getMontoCuota();
                    f2 = df.format(transaccionCuota.getFechaCuota());
                    c2 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 3) {
                    m3 = transaccionCuota.getMontoCuota();
                    f3 = df.format(transaccionCuota.getFechaCuota());
                    c3 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
            }
            creditNoteObj.setItemListDynamicC(listaItemC);
            creditNoteObj.setTotalCuotas(totalCuotas);
            creditNoteObj.setMontoPendiente(montoPendiente);
            creditNoteObj.setMetodoPago(metodoPago);
            creditNoteObj.setM1(m1);
            creditNoteObj.setM2(m2);
            creditNoteObj.setM3(m3);
            creditNoteObj.setF1(f1);
            creditNoteObj.setF2(f2);
            creditNoteObj.setF3(f3);
            creditNoteObj.setC1(c1);
            creditNoteObj.setC2(c2);
            creditNoteObj.setC3(c3);
            //creditNoteObj.setPorcentajeRetencion( retentionpercentage == null ? BigDecimal.ZERO : retentionpercentage);
            creditNoteObj.setPorcentajeRetencion(BigDecimal.ZERO);
            creditNoteObj.setMontoRetencion(montoRetencion);
            BigDecimal baseImponibleRetencion = BigDecimal.ZERO;

            /*if (creditNoteObj.getPorcentajeRetencion()!=BigDecimal.ZERO){
                baseImponibleRetencion = montoRetencion.divide(creditNoteObj.getPorcentajeRetencion().divide(new BigDecimal(100.0)));
            } else {
                baseImponibleRetencion = BigDecimal.ZERO;
            }*/

            creditNoteObj.setBaseImponibleRetencion(baseImponibleRetencion);

            // fin Cuotas

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (int i = 0; i < creditNoteType.getTransaccion().getTransaccionLineasList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Agregando datos al HashMap" + creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<String, String>();
                List<String> newlist = new ArrayList<String>();
                for (int j = 0; j < creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre(), creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    newlist.add(creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }

                }
                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);

            }

            creditNoteObj.setItemsListDynamic(listaItem);

            for (int i = 0; i < creditNoteObj.getItemsListDynamic().size(); i++) {

                for (int j = 0; j < creditNoteObj.getItemsListDynamic().get(i).getLstDinamicaItem().size(); j++) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + creditNoteObj.getItemsListDynamic().get(i).getLstDinamicaItem().get(j));
                    }

                }

            }

            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "] Condicion_pago: " + creditNoteObj.getPaymentCondition());

            }
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "]============= condicion pago------");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de la NOTA DE CREDITO");
            }

            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                creditNoteObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                creditNoteObj.setValidezPDF("");
            }

            creditNoteObj.setTypeOfCreditNote(creditNoteType.getTransaccion().getREFDOC_MotivCode());
            creditNoteObj.setDescOfCreditNote(creditNoteType.getCreditNoteType().getDiscrepancyResponse().get(0).getDescription().get(0).getValue().toUpperCase());
            creditNoteObj.setDocumentReferenceToCn(getDocumentReferenceValue(creditNoteType.getCreditNoteType().getBillingReference().get(0)));

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            creditNoteObj.setSenderSocialReason(creditNoteType.getTransaccion().getRazonSocial());
            creditNoteObj.setSenderRuc(creditNoteType.getTransaccion().getDocIdentidad_Nro());
            creditNoteObj.setSenderFiscalAddress(creditNoteType.getTransaccion().getDIR_NomCalle());
            creditNoteObj.setSenderDepProvDist(creditNoteType.getTransaccion().getDIR_Distrito() + " " + creditNoteType.getTransaccion().getDIR_Provincia() + " " + creditNoteType.getTransaccion().getDIR_Departamento());
            creditNoteObj.setSenderContact(creditNoteType.getTransaccion().getPersonContacto());
            creditNoteObj.setSenderMail(creditNoteType.getTransaccion().getEMail());
            creditNoteObj.setSenderLogo(this.senderLogo);
            creditNoteObj.setWeb(creditNoteType.getTransaccion().getWeb());
            creditNoteObj.setPorcentajeIGV(creditNoteType.getTransaccion().getDOC_PorcImpuesto());
            creditNoteObj.setComentarios(creditNoteType.getTransaccion().getFE_Comentario());

            creditNoteObj.setTelefono(creditNoteType.getTransaccion().getTelefono());
            creditNoteObj.setTelefono1(creditNoteType.getTransaccion().getTelefono_1());

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            creditNoteObj.setReceiverRegistrationName(creditNoteType.getTransaccion().getSN_RazonSocial());
            creditNoteObj.setReceiverIdentifier(creditNoteType.getTransaccion().getSN_DocIdentidad_Nro());
            creditNoteObj.setReceiverIdentifierType(creditNoteType.getTransaccion().getSN_DocIdentidad_Tipo());

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de la percepción.");
            }

            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < creditNoteType.getTransaccion().getTransaccionTotalesList().size(); i++) {
                if (creditNoteType.getTransaccion().getTransaccionTotalesList().get(i).getTransaccionTotalesPK().getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = creditNoteType.getTransaccion().getTransaccionTotalesList().get(i).getMonto();
                    perceptionPercentage = creditNoteType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt();
                    creditNoteObj.setPerceptionAmount(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getValue() + " " + creditNoteType.getTransaccion().getTransaccionTotalesList().get(i).getMonto().toString());
                    creditNoteObj.setPerceptionPercentage(creditNoteType.getTransaccion().getTransaccionTotalesList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < creditNoteType.getTransaccion().getTransaccionLineasList().size(); i++) {
                for (int j = 0; j < creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().size(); j++) {
                    if (creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        creditNoteObj.setISCPercetange(creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje().setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                        retentionpercentage = creditNoteType.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineaImpuestosList().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                creditNoteObj.setISCPercetange(retentionpercentage.ZERO.toString());
            }

            if (percepctionAmount == null) {
                creditNoteObj.setPerceptionAmount(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                creditNoteObj.setPerceptionPercentage(perceptionPercentage.ZERO.toString());
            }

            if (creditNoteType.getCreditNoteType().getID().getValue().startsWith(IUBLConfig.INVOICE_SERIE_PREFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateCreditNotePDF() [" + this.docUUID + "] El receptor es de un documento afectado de tipo FACTURA.");
                }
                creditNoteObj.setReceiverFiscalAddress(creditNoteType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Departamento().toUpperCase());

            } else if (creditNoteType.getCreditNoteType().getID().getValue().startsWith(IUBLConfig.BOLETA_SERIE_PREFIX)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateCreditNotePDF() [" + this.docUUID + "] El receptor es de un documento afectado de tipo BOLETA.");
                }
                creditNoteObj.setReceiverFiscalAddress(creditNoteType.getTransaccion().getSN_DIR_NomCalle().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Distrito().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Provincia().toUpperCase() + " - " + creditNoteType.getTransaccion().getSN_DIR_Departamento().toUpperCase());
            } else {
                logger.error("generateCreditNotePDF() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_431.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_431);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            // creditNoteObj.setCreditNoteItems(getCreditNoteItems(creditNoteType.getCreditNoteType().getCreditNoteLine()));

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de los MONTOS.");
            }
            String currencyCode = creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getValue();

            BigDecimal subtotalValue = getSubtotalValueFromTransaction(transaccionTotales, creditNoteObj.getDocumentIdentifier());
            creditNoteObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            BigDecimal igvValue = getTaxTotalValue2(creditNoteType.getTransaccion().getTransaccionImpuestosList(), IUBLConfig.TAX_TOTAL_IGV_ID);

            creditNoteObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue(creditNoteType.getCreditNoteType().getTaxTotal(), IUBLConfig.TAX_TOTAL_ISC_ID);
            creditNoteObj.setIscValue(getCurrency(iscValue, currencyCode));
//
//            BigDecimal lineExtensionAmount = creditNoteType.getCreditNoteType()
//                    .getLegalMonetaryTotal().getLineExtensionAmount()
//                    .getValue();
//            creditNoteObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));

            if (null != creditNoteType.getCreditNoteType().getLegalMonetaryTotal().getAllowanceTotalAmount() && null != creditNoteType.getCreditNoteType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue()) {
                creditNoteObj.setDiscountValue(getCurrency(creditNoteType.getCreditNoteType().getLegalMonetaryTotal().getAllowanceTotalAmount().getValue(), currencyCode));
            } else {
                creditNoteObj.setDiscountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            BigDecimal payableAmount = creditNoteType.getCreditNoteType().getLegalMonetaryTotal().getPayableAmount().getValue();
            creditNoteObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            creditNoteObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            creditNoteObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            creditNoteObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransaccionTotalesList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
            if (!gratuitaAmount.equals(BigDecimal.ZERO)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Existe Op. Gratuitas.");
                }
                creditNoteObj.setGratuitaAmountValue(getCurrency(gratuitaAmount, currencyCode));
            } else {
                creditNoteObj.setGratuitaAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }

            String barcodeValue = generateBarCodeInfoString(creditNoteType.getTransaccion().getDocIdentidad_Nro(), creditNoteType.getTransaccion().getDOC_Codigo(), creditNoteType.getTransaccion().getDOC_Serie(), creditNoteType.getTransaccion().getDOC_Numero(), creditNoteType.getCreditNoteType().getTaxTotal(), creditNoteObj.getIssueDate(), creditNoteType.getTransaccion().getDOC_MontoTotal().toString(), creditNoteType.getTransaccion().getSN_DocIdentidad_Tipo(), creditNoteType.getTransaccion().getSN_DocIdentidad_Nro(), creditNoteType.getCreditNoteType().getUBLExtensions());

//            String barcodeValue = generateBarCodeInfoString(invoiceType.getInvoiceType().getID().getValue(), invoiceType.getInvoiceType().getInvoiceTypeCode().getValue(),invoiceObj.getIssueDate(), invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue(), invoiceType.getInvoiceType().getTaxTotal(), invoiceType.getInvoiceType().getAccountingSupplierParty(), invoiceType.getInvoiceType().getAccountingCustomerParty(),invoiceType.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //invoiceObj.setBarcodeValue(barcodeValue);

            InputStream inputStream;
            InputStream inputStreamPDF;
            String rutaPath = ".." + File.separator + "CodigoQR" + File.separator + "07" + File.separator + creditNoteType.getCreditNoteType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "CodigoQR" + File.separator + "07");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            creditNoteObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "CodigoPDF417" + File.separator + "07");
            rutaPath = ".." + File.separator + "CodigoPDF417" + File.separator + "07" + File.separator + creditNoteType.getCreditNoteType().getID().getValue() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }
            inputStreamPDF = generatePDF417Code(barcodeValue, rutaPath, 200, 200, 1);

            creditNoteObj.setBarcodeValue(inputStreamPDF);
            String digestValue = generateDigestValue(creditNoteType.getCreditNoteType().getUBLExtensions());

            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNotePDF() [" + this.docUUID + "] VALOR RESUMEN: \n" + digestValue);
            }

            creditNoteObj.setDigestValue(digestValue);

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }

            /*
            Map<String, LegendObject> legendsMap = getaddLeyends(creditNoteType.getCreditNoteType().getNote());
             */
            Map<String, LegendObject> legendsMap = null;

            //if (TipoVersionUBL.notacredito.equals("21")) {
            legendsMap = getaddLeyends(creditNoteType.getCreditNoteType().getNote());
            /*} else if (TipoVersionUBL.notadebito.equals("20")) {
                legendsMap = getAdditionalProperties(creditNoteType.getCreditNoteType().getUBLExtensions().getUBLExtension());
            }*/

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            creditNoteObj.setLetterAmountValue(legendLetters.getLegendValue());
            legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);

            if (logger.isDebugEnabled()) {
                logger.debug("generateBoletaPDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            creditNoteObj.setLegends(getLegendList(legendsMap));

            creditNoteObj.setResolutionCodeValue(this.resolutionCode);

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            creditNoteInBytes = PDFCreditNoteCreator.getInstance(this.documentReportPath, this.legendSubReportPath, this.paymentDetailReportPath).createCreditNotePDF(creditNoteObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return creditNoteInBytes;
    } // generateCreditNotePDF

    public synchronized byte[] generateDespatchAdvicePDF(UBLDocumentWRP despatchAdvice, ConfigData configData) throws PDFReportException {

        if (logger.isDebugEnabled()) {
            logger.debug("+generateDespatchAdvicePDF() [" + this.docUUID + "]");
        }
        byte[] despatchInBytes = null;

        try {
            DespatchAdviceObject despatchAdviceObject = new DespatchAdviceObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            despatchAdviceObject.setCodigoEmbarque(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getCodigoPuerto());
            despatchAdviceObject.setCodigoMotivoTraslado(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getCodigoMotivo());
            despatchAdviceObject.setDescripcionMotivoTraslado(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getDescripcionMotivo());
            despatchAdviceObject.setDireccionDestino(despatchAdvice.getTransaccion().getSN_DIR_Direccion());
            despatchAdviceObject.setDireccionPartida(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getDireccionPartida());
            despatchAdviceObject.setDocumentoConductor(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getDocumentoConductor());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            String fechaEmision = format.format(despatchAdvice.getTransaccion().getDOC_FechaEmision());
            despatchAdviceObject.setFechaEmision(fechaEmision);
            String fechaInicioTraslado = format.format(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getFechaInicioTraslado());
            despatchAdviceObject.setFechaTraslado(fechaInicioTraslado);
            despatchAdviceObject.setModalidadTraslado(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getModalidadTraslado());
            despatchAdviceObject.setNombreConsumidor(despatchAdvice.getTransaccion().getSN_RazonSocial());
            despatchAdviceObject.setNombreEmisor(despatchAdvice.getTransaccion().getRazonSocial());
            despatchAdviceObject.setNumeroBultos(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getNumeroBultos());
            despatchAdviceObject.setNumeroContenedor(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getNumeroContenedor());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getTransaccion().getDOC_Serie() + "-" + despatchAdvice.getTransaccion().getDOC_Numero());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservaciones());
            despatchAdviceObject.setPesoBruto(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getPeso());
            despatchAdviceObject.setTipoDocumentoConductor(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getTipoDocConductor());
            despatchAdviceObject.setTipoDocumentoTransportista(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getTipoDOCTransportista());
            despatchAdviceObject.setUMPesoBruto(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getUnidadMedida());
            despatchAdviceObject.setNumeroDocConsumidor(despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro());
            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());

            if (despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getModalidadTraslado().equalsIgnoreCase("01")) {
                despatchAdviceObject.setPlacaVehiculo(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getPlacaVehiculo());
                despatchAdviceObject.setLicenciaConducir(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getLicenciaConducir());
                despatchAdviceObject.setRUCTransportista(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getRUCTransporista());
                despatchAdviceObject.setNombreTransportista(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getNombreRazonTransportista());
            } else {
                despatchAdviceObject.setRUCTransportista(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getRUCTransporista());
                despatchAdviceObject.setNombreTransportista(despatchAdvice.getTransaccion().getTransaccionGuiaRemision().getNombreRazonTransportista());
            }

            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < despatchAdvice.getTransaccion().getTransaccionLineasList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Agregando datos al HashMap" + despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();
                for (int j = 0; j < despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getUsuariocampos().getNombre(), despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    newlist.add(despatchAdvice.getTransaccion().getTransaccionLineasList().get(i).getTransaccionLineasUsucamposList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }

                }
                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);

            }

            if (null != despatchAdvice.getTransaccion().getTransaccionContractdocrefList() && 0 < despatchAdvice.getTransaccion().getTransaccionContractdocrefList().size()) {
                Map<String, String> hashedMap = new HashMap<String, String>();
                for (int i = 0; i < despatchAdvice.getTransaccion().getTransaccionContractdocrefList().size(); i++) {
                    hashedMap.put(despatchAdvice.getTransaccion().getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre(), despatchAdvice.getTransaccion().getTransaccionContractdocrefList().get(i).getValor());
                }
                despatchAdviceObject.setDespatchAdvicePersonalizacion(hashedMap);
            }

            despatchAdviceObject.setNumeroDocEmisor(despatchAdvice.getTransaccion().getDocIdentidad_Nro());
            despatchAdviceObject.setNumeroGuia(despatchAdvice.getAdviceType().getID().getValue());
            despatchAdviceObject.setTelefono(despatchAdvice.getTransaccion().getTelefono());
            despatchAdviceObject.setTelefono1(despatchAdvice.getTransaccion().getTelefono_1());
            despatchAdviceObject.setEmail(despatchAdvice.getTransaccion().getEMail());
            despatchAdviceObject.setPaginaWeb(despatchAdvice.getTransaccion().getWeb());
            despatchAdviceObject.setObervaciones(despatchAdvice.getTransaccion().getObservaciones());
            despatchAdviceObject.setItemListDynamic(listaItem);

            for (int i = 0; i < despatchAdviceObject.getItemListDynamic().size(); i++) {

                for (int j = 0; j < despatchAdviceObject.getItemListDynamic().get(i).getLstDinamicaItem().size(); j++) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateDespatchAdvicePDF() [" + this.docUUID + "] Fila " + i + " Columna " + j);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Fila " + i + " Contenido " + despatchAdviceObject.getItemListDynamic().get(i).getLstDinamicaItem().get(j));
                    }

                }
            }

            String barcodeValue = generateBarCodeInfoString(despatchAdvice.getTransaccion().getDocIdentidad_Nro(), despatchAdvice.getTransaccion().getDOC_Codigo(), despatchAdvice.getTransaccion().getDOC_Serie(), despatchAdvice.getTransaccion().getDOC_Numero(), null, despatchAdvice.getTransaccion().getDOC_FechaEmision().toString(), "00", despatchAdvice.getTransaccion().getSN_DocIdentidad_Tipo(), despatchAdvice.getTransaccion().getSN_DocIdentidad_Nro(), despatchAdvice.getAdviceType().getUBLExtensions());

            //String barcodeValue = generateBarCodeInfoString(despatchAdvice.getInvoiceType().getID().getValue(), despatchAdvice.getInvoiceType().getInvoiceTypeCode().getValue(), despatchAdvice.getAdviceType().getIssueDate(), despatchAdvice.getTransaccion().getDOCNumero(), null, invoiceType.getInvoiceType().getAccountingSupplierParty(), despatchAdvice.getInvoiceType().getAccountingCustomerParty(), despatchAdvice.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                //logger.info("generateInvoicePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //despatchAdviceObject.setBarcodeValue(barcodeValue);

            InputStream inputStream;
            InputStream inputStreamPDF;
            //String rutaPath = Directorio.ADJUNTOS + File.separator + "CodigoQR" + File.separator + "09" + File.separator + despatchAdvice.getTransaccion().getDOCNumero() + ".png";
            //File f = new File(Directorio.ADJUNTOS + File.separator + "CodigoQR" + File.separator + "09");
            // if (!f.exists()) {
            //     f.mkdirs();
            //}

            File f = new File(".." + File.separator + "CodigoPDF417" + File.separator + "09");
            String rutaPath = ".." + File.separator + "CodigoPDF417" + File.separator + "09" + File.separator + despatchAdvice.getTransaccion().getDOC_Numero() + ".png";
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            despatchAdviceObject.setCodeQR(inputStream);

            despatchAdviceObject.setSenderLogo(this.senderLogo);

            despatchInBytes = PDFDespatchAdviceCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createDespatchAdvicePDF(despatchAdviceObject, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateDespatchAdvicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateDespatchAdvicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateDespatchAdvicePDF() [" + this.docUUID + "]");
        }
        return despatchInBytes;
    }

    private BigDecimal getSubtotalValueFromTransaction(List<TransaccionTotales> transactionTotalList, String identifier) throws UBLDocumentException {
        if (null != transactionTotalList && !transactionTotalList.isEmpty()) {
            BigDecimal subtotal = BigDecimal.ZERO;
            for (TransaccionTotales transaccionTotal : transactionTotalList) {
                if (Objects.equals("1005", transaccionTotal.getTransaccionTotalesPK().getId())) {
                    subtotal = transaccionTotal.getMonto();
                    break;
                }
            }
            return subtotal;
        } else {
            this.logger.error("getSubtotalValueFromTransaction() [" + identifier + "] ERROR: " + IVenturaError.ERROR_330.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_330);
        }
    }


    public synchronized byte[] generatePerceptionPDF(UBLDocumentWRP perceptionType, ConfigData configData) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateInvoicePDF() [" + this.docUUID + "]");
        }
        byte[] perceptionBytes = null;

        try {
            PerceptionObject perceptionObj = new PerceptionObject();

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion GENERAL del documento.");
            }
            perceptionObj.setDocumentIdentifier(perceptionType.getPerceptionType().getId().getValue());
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de la fecha." + perceptionType.getPerceptionType().getIssueDate().getValue());
            }
            perceptionObj.setIssueDate(formatIssueDate(perceptionType.getPerceptionType().getIssueDate().getValue()));
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            perceptionObj.setSenderSocialReason(perceptionType.getPerceptionType().getAgentParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setSenderRuc(perceptionType.getPerceptionType().getAgentParty().getPartyIdentification().get(0).getID().getValue());
            perceptionObj.setSenderFiscalAddress(perceptionType.getPerceptionType().getAgentParty().getPostalAddress().getStreetName().getValue());
            perceptionObj.setSenderDepProvDist(formatDepProvDist(perceptionType.getPerceptionType().getAgentParty().getPostalAddress()));
            perceptionObj.setSenderLogo(this.senderLogo);
            perceptionObj.setTelValue(perceptionType.getTransaccion().getTelefono());
            //perceptionObj.setTel2Value(perceptionType.getTransaccion().getTelefono1());
            perceptionObj.setWebValue(perceptionType.getTransaccion().getWeb());
            perceptionObj.setSenderMail(perceptionType.getTransaccion().getEMail());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del RECEPTOR del documento.");
            }
            perceptionObj.setReceiverSocialReason(perceptionType.getPerceptionType().getReceiverParty().getPartyLegalEntity().get(0).getRegistrationName().getValue().toUpperCase());
            perceptionObj.setReceiverRuc(perceptionType.getPerceptionType().getReceiverParty().getPartyIdentification().get(0).getID().getValue());
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion de los ITEMS.");
            }
            perceptionObj.setPerceptionItems(getPerceptionItems(perceptionType.getPerceptionType().getSunatPerceptionDocumentReference(), new BigDecimal(perceptionType.getPerceptionType().getSunatPerceptionPercent().getValue())));
            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();
            for (int i = 0; i < perceptionType.getTransaccion().getTransaccionComprobantePagoList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generatePerceptionPDF() [" + this.docUUID + "] Agregando datos al HashMap" + perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().size());
                }
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<String, String>();
                List<String> newlist = new ArrayList<String>();
                for (int j = 0; j < perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().size(); j++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("generatePerceptionPDF() [" + this.docUUID + "] Extrayendo Campos " + perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getUsuariocampos().getNombre());
                    }
                    itemObjectHash.put(perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getUsuariocampos().getNombre(), perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getValor());
                    newlist.add(perceptionType.getTransaccion().getTransaccionComprobantePagoList().get(i).getTransaccionComprobantepagoUsuarioList().get(j).getValor());
                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Nuevo Tamanio " + newlist.size());
                    }
                }
                itemObject.setLstItemHashMap(itemObjectHash);
                itemObject.setLstDinamicaItem(newlist);
                listaItem.add(itemObject);
            }

            perceptionObj.setItemListDynamic(listaItem);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo la informacion de PROPIEDADES (AdditionalProperty).");
            }
            // Map<String, LegendObject> legendsMap = new HashMap<String,
            // LegendObject>();

            perceptionObj.setTotalAmountValue(perceptionType.getPerceptionType().getTotalInvoiceAmount().getValue().toString());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            perceptionType.getTransaccion();
            for (int i = 0; i < perceptionType.getTransaccion().getTransaccionPropiedadesList().size(); i++) {
                if (perceptionType.getTransaccion().getTransaccionPropiedadesList().get(i).getTransaccionPropiedadesPK().getId().equalsIgnoreCase("1000")) {
                    perceptionObj.setLetterAmountValue(perceptionType.getTransaccion().getTransaccionPropiedadesList().get(i).getValor());
                }

            }
            if (Boolean.parseBoolean(configData.getPdfBorrador())) {
                perceptionObj.setValidezPDF("Este documento no tiene validez fiscal.");
            } else {
                perceptionObj.setValidezPDF("");
            }

            // LegendObject legendLetters =
            // legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            // perceptionObj.setLetterAmountValue(legendLetters.getLegendValue());
            // legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo informacion del CODIGO DE BARRAS.");
            }
            // String barcodeValue =
            // generateBarcodeInfoV2(perceptionType.getPerceptionType().getId().getValue(),
            // "40",
            // perceptionType.getPerceptionType().getIssueDate().toString(),
            // perceptionType.getPerceptionType().getTotalInvoiceAmount().getValue(),
            // perceptionType.getPerceptionType().getAgentParty(),
            // perceptionType.getPerceptionType().getReceiverParty(),
            // perceptionType.getPerceptionType().getUblExtensions());
            // if (logger.isInfoEnabled()) {logger.info("generateInvoicePDF() ["
            // + this.docUUID + "] BARCODE: \n" + barcodeValue);}
            // perceptionObj.setBarcodeValue(barcodeValue);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            // perceptionObj.setLegends(getLegendList(legendsMap));

            perceptionObj.setResolutionCodeValue(this.resolutionCode);
            perceptionObj.setImporteTexto(perceptionType.getTransaccion().getTransaccionPropiedadesList().get(0).getValor());

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            perceptionBytes = PDFPerceptionCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createPerceptionPDF(perceptionObj, docUUID);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
            throw new PDFReportException(error);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return perceptionBytes;
    } // generateInvoicePDF


    protected List<PerceptionItemObject> getPerceptionItems(List<SUNATPerceptionDocumentReferenceType> perceptionLines, BigDecimal porcentaje) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getInvoiceItems() [" + this.docUUID + "] invoiceLines: " + perceptionLines);
        }
        List<PerceptionItemObject> itemList = null;

        if (null != perceptionLines && 0 < perceptionLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<PerceptionItemObject>(
                    perceptionLines.size());

            try {
                for (SUNATPerceptionDocumentReferenceType iLine : perceptionLines) {
                    PerceptionItemObject invoiceItemObj = new PerceptionItemObject();

                    invoiceItemObj.setFechaEmision(formatIssueDate(iLine
                            .getIssueDate().getValue()));
                    invoiceItemObj
                            .setTipoDocumento(iLine.getId().getSchemeID());
                    invoiceItemObj.setNumSerieDoc(iLine.getId().getValue());
                    invoiceItemObj.setMonedaMontoTotal(iLine.getTotalInvoiceAmount().getCurrencyID() + " ");
                    invoiceItemObj.setPrecioVenta(iLine.getTotalInvoiceAmount()
                            .getValue());
                    invoiceItemObj.setMonedaPercepcion("PEN ");
                    invoiceItemObj.setImportePercepcion(iLine
                            .getSunatPerceptionInformation()
                            .getPerceptionAmount().getValue());
                    invoiceItemObj.setMontoTotalCobrado(iLine
                            .getSunatPerceptionInformation()
                            .getSunatNetTotalCashed().getValue());
                    invoiceItemObj.setPorcentajePercepcion(porcentaje);
                    itemList.add(invoiceItemObj);
                }
            } catch (PDFReportException e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                        + IVenturaError.ERROR_415.getMessage());
                throw new PDFReportException(IVenturaError.ERROR_415);
            }
        } else {
            logger.error("getInvoiceItems() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_411.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_411);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getInvoiceItems()");
        }
        return itemList;
    }


} // PDFGenerateHandler
