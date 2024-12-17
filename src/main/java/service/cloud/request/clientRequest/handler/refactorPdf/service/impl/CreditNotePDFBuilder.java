package service.cloud.request.clientRequest.handler.refactorPdf.service.impl;

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
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.CreditNoteObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.CreditNotePDFGenerator;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CreditNotePDFBuilder extends BaseDocumentService implements CreditNotePDFGenerator {

    private static final Logger logger = Logger.getLogger(CreditNotePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";


    @Override
    public byte[] generateCreditNotePDF(UBLDocumentWRP creditNoteType, List<TransactionTotalesDTO> transaccionTotales, ConfigData configData) {
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
                creditNoteObj.setDueDate(formatDueDate(DateUtil.parseDate(creditNoteType.getTransaccion().getDOC_FechaVencimiento())));
            } else {
                creditNoteObj.setDueDate(formatDueDate(DateUtil.parseDate(creditNoteType.getTransaccion().getDOC_FechaVencimiento())));
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
            creditNoteObj.setDateDocumentReference(creditNoteType.getTransaccion().getFechaDOCRe());

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + creditNoteType.getTransaccion().getTransactionContractDocRefListDTOS().size());
            }
            if (creditNoteType.getTransaccion().getTransactionContractDocRefListDTOS() != null
                    && !creditNoteType.getTransaccion().getTransactionContractDocRefListDTOS().isEmpty()) {

                Map<String, String> hashedMap = new HashMap<>();

                for (Map<String, String> map : creditNoteType.getTransaccion().getTransactionContractDocRefListDTOS()) {
                    // Asumiendo que cada mapa contiene un único par clave-valor
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        hashedMap.put(entry.getKey(), entry.getValue());
                    }
                }

                creditNoteObj.setInvoicePersonalizacion(hashedMap);
            }
            // Cuotas

            List<WrapperItemObject> listaItemC = new ArrayList<>();

            List<TransactionCuotasDTO> transaccionCuotas = creditNoteType.getTransaccion().getTransactionCuotasDTOList();

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

            //BigDecimal montoRetencion  = (creditNoteType.getTransaccion().getMontoRetencion() != null ? creditNoteType.getTransaccion().getMontoRetencion() : new BigDecimal(0.0));
            BigDecimal montoRetencion = BigDecimal.ZERO; // ocultar retencion en el subreporte


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
                itemObjectHash.put("FechaCuota", df.format(DateUtil.parseDate(transaccionCuota.getFechaCuota())));
                newlist.add(df.format(DateUtil.parseDate(transaccionCuota.getFechaCuota())));
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
                    f1 = df.format(DateUtil.parseDate(transaccionCuota.getFechaCuota()));
                    c1 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 2) {
                    m2 = transaccionCuota.getMontoCuota();
                    f2 = df.format(DateUtil.parseDate(transaccionCuota.getFechaCuota()));
                    c2 = transaccionCuota.getCuota().replaceAll("[^0-9]", "");
                }
                if (totalCuotas == 3) {
                    m3 = transaccionCuota.getMontoCuota();
                    f3 = df.format(DateUtil.parseDate(transaccionCuota.getFechaCuota()));
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

            for (int i = 0; i < creditNoteType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Agregando datos al HashMap");
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de transaccionLineasCamposUsuario
                Map<String, String> camposUsuarioMap = creditNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();

                // Iterar sobre las entradas del mapa
                for (Map.Entry<String, String> entry : camposUsuarioMap.entrySet()) {
                    String nombreCampo = entry.getKey();
                    String valorCampo = entry.getValue();

                    if (logger.isDebugEnabled()) {
                        logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos " + nombreCampo);
                    }

                    itemObjectHash.put(nombreCampo, valorCampo);
                    newlist.add(valorCampo);

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
            creditNoteObj.setSenderLogo("C:\\clientes\\files\\20510910517\\COMPANY_LOGO.jpg");
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
            for (int i = 0; i < creditNoteType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (creditNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = creditNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = creditNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    creditNoteObj.setPerceptionAmount(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getValue() + " " + creditNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    creditNoteObj.setPerceptionPercentage(creditNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < creditNoteType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < creditNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (creditNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        creditNoteObj.setISCPercetange(creditNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = creditNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                creditNoteObj.setISCPercetange(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                creditNoteObj.setPerceptionAmount(creditNoteType.getCreditNoteType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                creditNoteObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
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
            BigDecimal igvValue = getTaxTotalValue2(creditNoteType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);

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

            BigDecimal gravadaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            creditNoteObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            creditNoteObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            creditNoteObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(creditNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
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
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "07" + File.separator + creditNoteType.getCreditNoteType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "07");
            if (!f.exists()) {
                f.mkdirs();
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            creditNoteObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "07");
            rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "07" + File.separator + creditNoteType.getCreditNoteType().getID().getValue() + ".png";
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

            creditNoteObj.setResolutionCodeValue("resolutionCde");

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            creditNoteInBytes = createCreditNotePDF(creditNoteObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return creditNoteInBytes;
    } // generateCreditNotePDF

    public byte[] createCreditNotePDF(CreditNoteObject creditNoteObj, String docUUID, ConfigData configData) throws PDFReportException {
        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;

        if (logger.isDebugEnabled()) {
            logger.debug("+createCreditNotePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == creditNoteObj) {
            throw new PDFReportException(IVenturaError.ERROR_408);
        } else {
            try {
                parameterMap = new HashMap<String, Object>();
                cuotasMap = new HashMap<>();

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
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DIR, "C:\\clientes\\files\\20510910517\\formatos\\InvoiceDocumentPaymentDetail.jasper"/*this.paymentDetailReportPath*/);
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_PAYMENTS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getItemListDynamicC()));
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, "C:\\clientes\\files\\20510910517\\formatos\\legendReport.jasper"/*this.legendSubReportPath*/); /*this.legendSubReportPath*/
                ;
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(creditNoteObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_CREDIT_NOTE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, creditNoteObj.getResolutionCodeValue());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);
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

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_CUOTAS_MAP, cuotasMap); // parametros subreporte de cuotas (se pasa como HashMap)

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "creditNoteDocument_Ing.jrxml" : "creditNoteDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(creditNoteObj.getSenderRuc(), documentName);
                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(creditNoteObj.getItemsListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createCreditNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createCreditNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_443);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createCreditNotePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    } //createCreditNotePDF


    protected String formatDueDate(Date inputDueDate) throws Exception {
        String dueDate = null;
        try {

            SimpleDateFormat sdf = null;
            sdf = new SimpleDateFormat(IPDFCreatorConfig.PATTERN_DATE);
            dueDate = sdf.format(inputDueDate);
        } catch (Exception e) {
            logger.error("formatDueDate() [" + this.docUUID + "] ERROR: "
                    + e.getMessage());
        }
        return dueDate;
    }



}
