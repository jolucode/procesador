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
import service.cloud.request.clientRequest.handler.refactorPdf.dto.InvoiceObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.item.InvoiceItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.InvoicePDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.LineExtensionAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NameType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TaxableAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class InvoicePDFBuilder implements InvoicePDFGenerator {

    private static final Logger logger = Logger.getLogger(InvoicePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";


    @Override
    public byte[] generateInvoicePDF(UBLDocumentWRP invoiceType, ConfigData configuracion) {
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
                invoiceObj.setDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento());
                //invoiceObj.setDueDate(formatDueDate(invoiceType.getInvoiceType().getNote().get(0).getValue()));
            } else {
                invoiceObj.setDueDate(invoiceType.getTransaccion().getDOC_FechaVencimiento());
            }

            String Anticipos = "";
            for (int i = 0; i < invoiceType.getTransaccion().getTransactionActicipoDTOList().size(); i++) {
                Anticipos.concat(invoiceType.getTransaccion().getTransactionActicipoDTOList().get(i).getAntiDOC_Serie_Correlativo() + " ");
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
            invoiceObj.setSenderLogo(configuracion.getCompletePathLogo());
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
            for (int i = 0; i < invoiceType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    invoiceObj.setPerceptionAmount(currencyCode + " " + invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    invoiceObj.setPerceptionPercentage(invoiceType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < invoiceType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        invoiceObj.setRetentionPercentage(invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = invoiceType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
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
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de LINEAS personalizados." + invoiceType.getTransaccion().getTransactionLineasDTOList().size());
            }

            // Cuotas

            List<WrapperItemObject> listaItemC = new ArrayList<>();

            List<TransactionCuotasDTO> transaccionCuotas = invoiceType.getTransaccion().getTransactionCuotasDTOList();

            // DateFormat para el formato de entrada (yyyy-MM-dd)
            DateFormat dfEntrada = new SimpleDateFormat("yyyy-MM-dd");

            // DateFormat para el formato de salida (dd/MM/yyyy)
            DateFormat dfSalida = new SimpleDateFormat("dd/MM/yyyy");

            BigDecimal montoRetencion = (invoiceType.getTransaccion().getMontoRetencion() != null ? new BigDecimal(invoiceType.getTransaccion().getMontoRetencion()) : new BigDecimal("0.0"));

            Integer totalCuotas = 0;
            BigDecimal montoPendiente = BigDecimal.ZERO;
            String metodoPago = "";
            BigDecimal m1 = BigDecimal.ZERO, m2 = BigDecimal.ZERO, m3 = BigDecimal.ZERO;
            String f1 = "", f2 = "", f3 = "";
            String c1 = "", c2 = "", c3 = "";

            for (TransactionCuotasDTO transaccionCuota : transaccionCuotas) {
                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                itemObjectHash.put("Cuota", transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                newlist.add(transaccionCuota.getCuota().replaceAll("[^0-9]", ""));
                itemObjectHash.put("FechaCuota", dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota())));
                newlist.add(dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota())));
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
                    f1 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
                    c1 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 2) {
                    m2 = transaccionCuota.getMontoCuota();
                    f2 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
                    c2 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 3) {
                    m3 = transaccionCuota.getMontoCuota();
                    f3 = dfSalida.format(dfEntrada.parse(transaccionCuota.getFechaCuota()));
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

            List<TransactionLineasDTO> transaccionLineas = invoiceType.getTransaccion().getTransactionLineasDTOList();
            for (TransactionLineasDTO transaccionLinea : transaccionLineas) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateInvoicePDF() [" + this.docUUID + "] Agregando datos al HashMap");
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de campos de usuario desde transaccionLinea
                Map<String, String> camposUsuarioMap = transaccionLinea.getTransaccionLineasCamposUsuario();
                if (camposUsuarioMap != null && !camposUsuarioMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        if (logger.isDebugEnabled()) {
                            logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + nombreCampo);
                        }

                        if (nombreCampo.startsWith("U_")) {
                            // Cortar "U_" del nombreCampo
                            nombreCampo = nombreCampo.substring(2);
                        }
                        itemObjectHash.put(nombreCampo, valorCampo);
                        newlist.add(valorCampo);
                    }
                }

                // Obtener el mapa de campos de contrato desde invoiceType
                List<Map<String, String>> contractDocRefs = invoiceType.getTransaccion().getTransactionContractDocRefListDTOS();
                for (Map<String, String> contractDocRefMap : contractDocRefs) {
                    for (Map.Entry<String, String> entry : contractDocRefMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        itemObjectHash.put(nombreCampo, valorCampo);
                        newlist.add(valorCampo);
                    }
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
            BigDecimal subtotalValue = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != invoiceType.getInvoiceType().getPrepaidPayment() && !invoiceType.getInvoiceType().getPrepaidPayment().isEmpty() && null != invoiceType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                invoiceObj.setSubtotalValue(getCurrency(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue(), currencyCode));
            } else {
                invoiceObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + invoiceType.getTransaccion().getTransactionContractDocRefListDTOS().size());
            }
            if (null != invoiceType.getTransaccion().getTransactionContractDocRefListDTOS() && 0 < invoiceType.getTransaccion().getTransactionContractDocRefListDTOS().size()) {
                Map<String, String> hashedMap = new HashMap<>();
                List<Map<String, String>> contractDocRefs = invoiceType.getTransaccion().getTransactionContractDocRefListDTOS();
                for (Map<String, String> contractDocRefMap : contractDocRefs) {
                    // Asumimos que cada mapa en la lista contiene solo un par clave-valor para el campo y el valor.
                    for (Map.Entry<String, String> entry : contractDocRefMap.entrySet()) {
                        String nombreCampo = entry.getKey();
                        String valorCampo = entry.getValue();

                        hashedMap.put(nombreCampo, valorCampo);
                    }
                }
                invoiceObj.setInvoicePersonalizacion(hashedMap);
            }

            BigDecimal igvValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);
            invoiceObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue2(invoiceType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_ISC_ID);
            invoiceObj.setIscValue(getCurrency(iscValue, currencyCode));

            Optional<LineExtensionAmountType> optional = Optional.ofNullable(invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount());
            if (optional.isPresent()) {
                BigDecimal lineExtensionAmount = invoiceType.getInvoiceType().getLegalMonetaryTotal().getLineExtensionAmount().getValue();
                invoiceObj.setAmountValue(getCurrency(lineExtensionAmount, currencyCode));
            } else {
                invoiceObj.setAmountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }
            BigDecimal descuento = null;
            TransacctionDTO transaccion = invoiceType.getTransaccion();
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

            BigDecimal gravadaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            invoiceObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            invoiceObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            invoiceObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(invoiceType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
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

//          String barcodeValue = generateBarCodeInfoString(invoiceType.getInvoiceType().getID().getValue(), invoiceType.getInvoiceType().getInvoiceTypeCode().getValue(),invoiceObj.getIssueDate(), invoiceType.getInvoiceType().getLegalMonetaryTotal().getPayableAmount().getValue(), invoiceType.getInvoiceType().getTaxTotal(), invoiceType.getInvoiceType().getAccountingSupplierParty(), invoiceType.getInvoiceType().getAccountingCustomerParty(),invoiceType.getInvoiceType().getUBLExtensions());
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoicePDF() [" + this.docUUID + "] BARCODE: \n" + barcodeValue);
            }
            //invoiceObj.setBarcodeValue(barcodeValue);

            InputStream inputStream;
            InputStream inputStreamPDF;
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "01");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            invoiceObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "01");
            rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "01" + File.separator + invoiceType.getInvoiceType().getID().getValue() + ".png";
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

            legendsMap = getaddLeyends(invoiceType.getInvoiceType().getNote());

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando el importe en LETRAS.");
            }
            if(!legendsMap.isEmpty()) {
                LegendObject legendLetters = legendsMap.get(IUBLConfig.ADDITIONAL_PROPERTY_1000);
                invoiceObj.setLetterAmountValue(legendLetters.getLegendValue());
                legendsMap.remove(IUBLConfig.ADDITIONAL_PROPERTY_1000);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Colocando la lista de LEYENDAS.");
            }
            invoiceObj.setLegends(getLegendList(legendsMap));

            invoiceObj.setResolutionCodeValue("resolutionCde");

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            invoiceInBytes = createInvoicePDF(invoiceObj, docUUID, configuracion);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return invoiceInBytes;
    } // generateInvoicePDF

    public byte[] createInvoicePDF(InvoiceObject invoiceObj, String docUUID, ConfigData configuracion) throws PDFReportException {
        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;
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
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, "C:\\clientes\\files\\"+ invoiceObj.getSenderRuc() +"\\formatos\\InvoiceDocumentPaymentDetail.jasper"/*this.paymentDetailReportPath*/);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(new ArrayList<>(invoiceObj.getItemListDynamicC())));

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, "C:\\clientes\\files\\"+ invoiceObj.getSenderRuc() +"\\formatos\\legendReport.jasper"/*this.legendSubReportPath*/);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(new ArrayList<>(invoiceObj.getLegends())));

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

                String documentName = (configuracion.getPdfIngles() != null && configuracion.getPdfIngles().equals("Si")) ? "invoiceDocument_Ing.jrxml" : "invoiceDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(invoiceObj.getSenderRuc(), documentName);

                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(invoiceObj.getItemListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
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

    protected String getRemissionGuides(
            List<DocumentReferenceType> despatchDocumentReferences) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getRemissionGuides()");
        }
        String remissionGuides = null;

        if (null != despatchDocumentReferences
                && 0 < despatchDocumentReferences.size()) {
            for (DocumentReferenceType despatchDocRef : despatchDocumentReferences) {
                if (null == remissionGuides) {
                    remissionGuides = "";
                } else {
                    remissionGuides += ", ";
                }

                remissionGuides += despatchDocRef.getID().getValue();
            }
        } else {
            remissionGuides = IPDFCreatorConfig.EMPTY_VALUE;
        }

        return remissionGuides;
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

    protected String getCurrencyV3(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;

        /*
         * Obtener el objeto Locale de PERU
         */
        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);

        /*
         * Obtener el formato de moneda local
         */
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);

            /*
             * Establecer el currency en el formato
             */
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        "###,###.00", new DecimalFormatSymbols(Locale.US));
                //System.out.println(decF.format(money));
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected BigDecimal getTransaccionTotales(List<TransactionTotalesDTO> lstTotales, String addiMonetaryValue) {

        if (lstTotales != null) {
            if (lstTotales.size() > 0) {
                for (int i = 0; i < lstTotales.size(); i++) {
                    if (lstTotales.get(i).getId().equalsIgnoreCase(addiMonetaryValue)) {
                        return lstTotales.get(i).getMonto();
                    }
                }
            }
        }
        return BigDecimal.ZERO;
    }

    protected List<InvoiceItemObject> getInvoiceItems(
            List<InvoiceLineType> invoiceLines) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getInvoiceItems() [" + this.docUUID
                    + "] invoiceLines: " + invoiceLines);
        }
        List<InvoiceItemObject> itemList = null;

        if (null != invoiceLines && 0 < invoiceLines.size()) {
            /* Instanciando la lista de objetos */
            itemList = new ArrayList<InvoiceItemObject>(invoiceLines.size());

            try {
                for (InvoiceLineType iLine : invoiceLines) {
                    InvoiceItemObject invoiceItemObj = new InvoiceItemObject();

                    invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity()
                            .getValue());
                    // invoiceItemObj.setQuantityItem(iLine.getInvoicedQuantity().getValue().setScale(IPDFCreatorConfig.DECIMAL_ITEM_QUANTITY,
                    // RoundingMode.HALF_UP).toString());
                    invoiceItemObj
                            .setUnitMeasureItem((null != iLine.getNote()) ? iLine
                                    .getNote().get(0).getValue() : "");
                    invoiceItemObj.setDescriptionItem(iLine.getItem()
                            .getDescription().get(0).getValue().toUpperCase());
                    invoiceItemObj.setUnitValueItem(iLine.getPrice()
                            .getPriceAmount().getValue());
                    // invoiceItemObj.setUnitValueItem(getCurrencyV2(iLine.getPrice().getPriceAmount().getValue(),
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    BigDecimal unitPrice = getPricingReferenceValue(iLine
                                    .getPricingReference()
                                    .getAlternativeConditionPrice(),
                            IPDFCreatorConfig.ALTERNATIVE_COND_UNIT_PRICE);
                    invoiceItemObj.setUnitPriceItem(unitPrice);
                    // invoiceItemObj.setUnitPriceItem(getCurrencyV2(unitPrice,
                    // null, IPDFCreatorConfig.PATTERN_FLOAT_DEC_3));

                    invoiceItemObj.setDiscountItem(getCurrency(
                            getDiscountItem(iLine.getAllowanceCharge()), null));
                    invoiceItemObj.setAmountItem(getCurrency(iLine
                            .getLineExtensionAmount().getValue(), null));

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
    } // getInvoiceItems

    protected String getCurrency(BigDecimal value, String currencyCode)
            throws Exception {
        String moneyStr = null;

        /*
         * Obtener el objeto Locale de PERU
         */
        Locale locale = new Locale(IPDFCreatorConfig.LANG_PATTERN,
                IPDFCreatorConfig.LOCALE_PE);

        /*
         * Obtener el formato de moneda local
         */
        java.text.NumberFormat format = java.text.NumberFormat
                .getCurrencyInstance(locale);

        if (StringUtils.isNotBlank(currencyCode)) {
            Currency currency = Currency.getInstance(currencyCode);

            /*
             * Establecer el currency en el formato
             */
            format.setCurrency(currency);
        }

        if (null != value) {
            Double money = value.doubleValue();

            money = money * 100;
            long tmp = Math.round(money);
            money = (double) tmp / 100;

            if (0 > money) {
                String tempDesc = format.format(money);
                java.text.DecimalFormat decF = new java.text.DecimalFormat(
                        IPDFCreatorConfig.PATTERN_FLOAT_DEC);
                //System.out.println(decF.format(money));
                moneyStr = tempDesc.substring(1, 4) + " " + decF.format(money);
            } else {
                moneyStr = format.format(money);
            }
        }

        if (null == currencyCode) {
            moneyStr = moneyStr.substring(4);
        }

        return moneyStr;
    } // getCurrency

    protected BigDecimal getTaxTotalValue2(List<TransactionImpuestosDTO> taxTotalList,
                                           String taxTotalCode) throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTaxTotalValue() [" + this.docUUID
                    + "] taxTotalCode: " + taxTotalCode);
        }
        BigDecimal taxValue = BigDecimal.ZERO;

        for (int i = 0; i < taxTotalList.size(); i++) {
            if (taxTotalList.get(i).getTipoTributo().equalsIgnoreCase(taxTotalCode)) {
                taxValue = taxValue.add(taxTotalList.get(i).getMonto());
                if (logger.isDebugEnabled()) {
                    logger.debug("+-getTaxTotalValue() [" + this.docUUID
                            + "] taxValue: " + taxValue);
                }
            }
        }

        if (null == taxTotalList && 0 > taxTotalList.size()) {
            logger.error("getTaxTotalValue() ERROR: "
                    + IVenturaError.ERROR_462.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_462);
        }
        return taxValue;
    }

    public String generateBarCodeInfoString(String RUC_emisor_electronico, String documentType, String serie, String correlativo, List<TaxTotalType> taxTotalList, String issueDate, String Importe_total_venta, String Tipo_documento_adquiriente, String Numero_documento_adquiriente, UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = "";
        try {

            /***/
            String digestValue = getDigestValue(ublExtensions);
            logger.debug("Digest Value" + digestValue);

            /**El elemento opcional KeyInfo contiene información sobre la llave que se necesita para validar la firma, como lo muestra*/
            String signatureValue = getSignatureValue(ublExtensions);
            logger.debug("signatureValue" + signatureValue);

            String Sumatoria_IGV = "";
            if (taxTotalList != null) {
                Sumatoria_IGV = getTaxTotalValueV21(taxTotalList).toString();
                logger.debug("Sumatoria_IGV" + Sumatoria_IGV);
            }
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, RUC_emisor_electronico, documentType, serie, correlativo, Sumatoria_IGV, Importe_total_venta, issueDate, Tipo_documento_adquiriente, Numero_documento_adquiriente, digestValue);

        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }

        return barcodeValue;
    }

    public String generateDigestValue(UBLExtensionsType ublExtensions)
            throws PDFReportException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateDigestValueInfo()");
        }

        String digestValue = null;
        try {


            /* i.) Valor resumen <ds:DigestValue> */
            digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */

        } catch (PDFReportException e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBarcodeInfo() [" + this.docUUID + "] ERROR: "
                    + IVenturaError.ERROR_418.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateBarcodeInfo()");
        }
        return digestValue;
    }

    private String getDigestValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String digestValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_DIGESTVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_DIGESTVALUE_TAG)) {
                    digestValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(digestValue)) {
                throw new PDFReportException(IVenturaError.ERROR_423);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("getDigestValue() Exception -->" + e.getMessage());
            throw e;
        }
        return digestValue;
    } // getDigestValue

    protected Map<String, LegendObject> getaddLeyends(List<NoteType> lstNote) throws PDFReportException {
        Map<String, LegendObject> legendsMap = null;
        String id = null;
        String value = null;

        try {
            legendsMap = new HashMap<String, LegendObject>();
            if (lstNote != null) {
                if (lstNote.size() > 0) {
                    for (int i = 0; i < lstNote.size(); i++) {
                        LegendObject legendObj = new LegendObject();
                        legendObj.setLegendValue(lstNote.get(i).getValue());
                        legendsMap.put(lstNote.get(i).getLanguageLocaleID(), legendObj);
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("getAdditionalProperties() [" + this.docUUID
                    + "] ERROR: " + IVenturaError.ERROR_420.getMessage());
            throw new PDFReportException(IVenturaError.ERROR_420);
        }
        return legendsMap;
    }


    protected List<LegendObject> getLegendList(
            Map<String, LegendObject> legendsMap) {
        List<LegendObject> legendList = null;
        if (null != legendsMap && 0 < legendsMap.size()) {
            legendList = new ArrayList<LegendObject>(legendsMap.values());
        } else {
            /*
             * No existe LEYENDAS.
             */
            LegendObject legendObj = new LegendObject();
            legendObj.setLegendValue(IPDFCreatorConfig.LEGEND_DEFAULT_EMPTY);

            legendList = new ArrayList<LegendObject>();
            legendList.add(legendObj);
        }

        return legendList;
    } // getLegendList

    private String getSignatureValue(UBLExtensionsType ublExtensions)
            throws Exception {
        String signatureValue = null;
        try {
            int lastIndex = ublExtensions.getUBLExtension().size() - 1;
            UBLExtensionType ublExtension = ublExtensions.getUBLExtension()
                    .get(lastIndex);

            NodeList nodeList = ublExtension.getExtensionContent().getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SIGNATUREVALUE_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SIGNATUREVALUE_TAG)) {
                    signatureValue = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isBlank(signatureValue)) {
                throw new PDFReportException(IVenturaError.ERROR_424);
            }
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            logger.error("getSignatureValue() Exception -->" + e.getMessage());
            throw e;
        }
        return signatureValue;
    } // getSignatureValue

    protected BigDecimal getTaxTotalValueV21(List<TaxTotalType> taxTotalList) {

        for (int i = 0; i < taxTotalList.size(); i++) {
            for (int j = 0; j < taxTotalList.get(i).getTaxSubtotal().size(); j++) {
                if (taxTotalList.get(i).getTaxSubtotal().get(j).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase("1000")) {
                    return taxTotalList.get(i).getTaxAmount().getValue();
                }
            }

        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getDiscountItem(
            List<AllowanceChargeType> allowanceCharges) {
        BigDecimal value = BigDecimal.ZERO;

        if (null != allowanceCharges && 0 < allowanceCharges.size()) {
            /*
             * Se entiende que en el caso exista una lista, solo debe tener un
             * solo valor
             */
            value = allowanceCharges.get(0).getAmount().getValue();
        }
        return value;
    } // getDiscountItem

    private BigDecimal getPricingReferenceValue(
            List<PriceType> alternativeConditionPrice, String priceTypeCode)
            throws PDFReportException {
        BigDecimal value = null;

        if (null != alternativeConditionPrice
                && 0 < alternativeConditionPrice.size()) {
            for (PriceType altCondPrice : alternativeConditionPrice) {
                if (altCondPrice.getPriceTypeCode().getValue()
                        .equalsIgnoreCase(priceTypeCode)) {
                    value = altCondPrice.getPriceAmount().getValue();
                }
            }

            //if (null == value) {
            //  throw new PDFReportException(IVenturaError.ERROR_417);
            //}
        } else {
            throw new PDFReportException(IVenturaError.ERROR_416);
        }
        return value;
    } // getPricingReferenceValue

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
            logger.error(ex.getMessage());
        }
        return null;
    }

    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
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
            logger.error(ex.getMessage());
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

}
