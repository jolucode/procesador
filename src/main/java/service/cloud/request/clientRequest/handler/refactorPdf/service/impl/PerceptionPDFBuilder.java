package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.encoder.PDF417;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.PerceptionObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.PerceptionItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.PerceptionPDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATPerceptionDocumentReferenceType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class PerceptionPDFBuilder implements PerceptionPDFGenerator {

    private static final Logger logger = Logger.getLogger(PerceptionPDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public synchronized byte[] generatePerceptionPDF(UBLDocumentWRP perceptionType, ConfigData configData) {
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
            perceptionObj.setSenderLogo(configData.getCompletePathLogo());
            perceptionObj.setTelValue(perceptionType.getTransaccion().getTelefono());
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
            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < perceptionType.getTransaccion().getTransactionComprobantesDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generatePerceptionPDF() [" + this.docUUID + "] Agregando datos al HashMap - Total comprobantes: "
                            + perceptionType.getTransaccion().getTransactionComprobantesDTOList().size());
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                for (TransactionComprobantesDTO comprobantesDTO : perceptionType.getTransaccion().getTransactionComprobantesDTOList()) {
                    // Obtener todas las variables del objeto TransactionComprobantesDTO
                    Field[] fields = comprobantesDTO.getClass().getDeclaredFields();

                    // Iterar sobre cada campo
                    for (Field field : fields) {
                        field.setAccessible(true); // Asegurarse de que el campo es accesible

                        try {
                            // Obtener el nombre del campo y su valor
                            String nombreCampo = field.getName();
                            Object valorCampo = field.get(comprobantesDTO);

                            if (valorCampo != null) {
                                // Si el campo empieza con "U_", se elimina antes de guardarlo
                                if (nombreCampo.startsWith("U_")) {
                                    nombreCampo = nombreCampo.substring(2);
                                }

                                // Añadir el nombre y el valor al HashMap, convirtiendo el valor a String
                                itemObjectHash.put(nombreCampo, valorCampo.toString());
                                newlist.add(valorCampo.toString());
                            }
                        } catch (IllegalAccessException e) {
                            logger.error("Error accediendo al campo: " + field.getName(), e);
                        }
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
            for (int i = 0; i < perceptionType.getTransaccion().getTransactionPropertiesDTOList().size(); i++) {
                if (perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getId().equalsIgnoreCase("1000")) {
                    perceptionObj.setLetterAmountValue(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(i).getValor());
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

            perceptionObj.setResolutionCodeValue("resolutionCde");
            perceptionObj.setImporteTexto(perceptionType.getTransaccion().getTransactionPropertiesDTOList().get(0).getValor());

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            perceptionBytes = createPerceptionPDF(perceptionObj, docUUID, configData);// PDFPerceptionCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createPerceptionPDF(perceptionObj, docUUID);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return perceptionBytes;
    } // generateInvoicePDF

    public byte[] createPerceptionPDF(PerceptionObject perceptionObj,
                                      String docUUID, ConfigData configData) throws PDFReportException {
        Map<String, Object> parameterMap;
        if (logger.isDebugEnabled()) {
            logger.debug("+createInvoicePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == perceptionObj) {
            throw new PDFReportException(IVenturaError.ERROR_406);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();

                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER,
                        perceptionObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE,
                        perceptionObj.getIssueDate());

                if (StringUtils.isNotBlank(perceptionObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL,
                            IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE,
                            perceptionObj.getSunatTransaction());
                }
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON,
                        perceptionObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC,
                        perceptionObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS,
                        perceptionObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST,
                        perceptionObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT,
                        perceptionObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL,
                        perceptionObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL,
                        perceptionObj.getTelValue());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1,
                        perceptionObj.getTel2Value());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH,
                        perceptionObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB,
                        perceptionObj.getWebValue());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, perceptionObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_SOCIAL_REASON,
                        perceptionObj.getReceiverSocialReason());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_RUC,
                        perceptionObj.getReceiverRuc());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE,
                        perceptionObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE,
                        perceptionObj.getLetterAmountValue());

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE,
                        IPDFCreatorConfig.LEGEND_PERCEPTION_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE,
                        perceptionObj.getResolutionCodeValue());

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "perceptionDocument_Ing.jrxml" : "perceptionDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(perceptionObj.getSenderRuc(), documentName);

                JasperPrint iJasperPrint = JasperFillManager.fillReport(
                        jasperReport,
                        parameterMap,
                        new JRBeanCollectionDataSource(perceptionObj
                                .getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") - ERROR: "
                        + e.getMessage());
                logger.error("createInvoicePDF() [" + docUUID + "] Exception("
                        + e.getClass().getName() + ") -->"
                        + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_441);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createInvoicePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    }

    protected String formatIssueDate(XMLGregorianCalendar xmlGregorianCal)
            throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+formatIssueDate() [" + this.docUUID + "]");
        }
        Date inputDate = xmlGregorianCal.toGregorianCalendar().getTime();

        Locale locale = new Locale(IPDFCreatorConfig.LOCALE_ES,
                IPDFCreatorConfig.LOCALE_PE);

        SimpleDateFormat sdf = new SimpleDateFormat(
                IPDFCreatorConfig.PATTERN_DATE, locale);
        String issueDate = sdf.format(inputDate);

        if (logger.isDebugEnabled()) {
            logger.debug("-formatIssueDate() [" + this.docUUID + "]");
        }
        return issueDate;
    }

    protected String formatDepProvDist(AddressType postalAddress)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-formatDepProvDist() [" + this.docUUID + "]");
        }
        String depProvDist = null;
        if (null != postalAddress) {
            String department = postalAddress.getCountrySubentity().getValue();
            String province = postalAddress.getCityName().getValue();
            String district = postalAddress.getDistrict().getValue();

            depProvDist = district + " - " + province + " - " + department;

        } else {
            throw new PDFReportException(IVenturaError.ERROR_410);
        }
        return depProvDist;
    }

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

}
