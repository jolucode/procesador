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
import service.cloud.request.clientRequest.handler.refactorPdf.dto.DebitNoteObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientRequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientRequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientRequest.handler.refactorPdf.service.DebitNotePDFGenerator;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class DebitNotePDFBuilder extends BaseDocumentService implements DebitNotePDFGenerator {

    private static final Logger logger = Logger.getLogger(DebitNotePDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";

    @Override
    public  byte[] generateDebitNotePDF(UBLDocumentWRP debitNoteType, List<TransactionTotalesDTO> transactionTotalList, ConfigData configData) {
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
            debitNoteObj.setDateDocumentReference(debitNoteType.getTransaccion().getFechaDOCRe());

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo informacion del EMISOR del documento.");
            }
            debitNoteObj.setSenderSocialReason(debitNoteType.getTransaccion().getRazonSocial());
            debitNoteObj.setSenderRuc(debitNoteType.getTransaccion().getDocIdentidad_Nro());
            debitNoteObj.setSenderFiscalAddress(debitNoteType.getTransaccion().getDIR_NomCalle());
            debitNoteObj.setSenderDepProvDist(debitNoteType.getTransaccion().getDIR_Distrito() + " " + debitNoteType.getTransaccion().getDIR_Provincia() + " " + debitNoteType.getTransaccion().getDIR_Departamento());
            debitNoteObj.setSenderContact(debitNoteType.getTransaccion().getPersonContacto());
            debitNoteObj.setSenderMail(debitNoteType.getTransaccion().getEMail());
            debitNoteObj.setSenderLogo(debitNoteType.getTransaccion().getDocIdentidad_Nro());
            debitNoteObj.setWeb(debitNoteType.getTransaccion().getWeb());
            debitNoteObj.setPorcentajeIGV(debitNoteType.getTransaccion().getDOC_PorcImpuesto());
            debitNoteObj.setComentarios(debitNoteType.getTransaccion().getFE_Comentario());

            debitNoteObj.setTelefono(debitNoteType.getTransaccion().getTelefono());
            debitNoteObj.setTelefono_1(debitNoteType.getTransaccion().getTelefono_1());

            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + debitNoteType.getTransaccion().getTransactionContractDocRefListDTOS().size());
            }
            if (null != debitNoteType.getTransaccion().getTransactionContractDocRefListDTOS()
                    && 0 < debitNoteType.getTransaccion().getTransactionContractDocRefListDTOS().size()) {
                Map<String, String> hashedMap = new HashMap<>();
                List<Map<String, String>> contractDocRefs = debitNoteType.getTransaccion().getTransactionContractDocRefListDTOS();

                for (Map<String, String> contractDocRefMap : contractDocRefs) {
                    // Asumimos que cada mapa en la lista contiene pares clave-valor para el nombre del campo y el valor
                    hashedMap.putAll(contractDocRefMap);
                }

                debitNoteObj.setInvoicePersonalizacion(hashedMap);
            }

            List<WrapperItemObject> listaItem = new ArrayList<>();

            for (int i = 0; i < debitNoteType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateDebitNotePDF() [" + this.docUUID + "] Agregando datos al HashMap");
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de transaccionLineasCamposUsuario
                Map<String, String> camposUsuarioMap = debitNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();

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
            BigDecimal igvValue = getTaxTotalValue2(debitNoteType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);

            debitNoteObj.setIgvValue(getCurrency(igvValue, currencyCode));

            BigDecimal iscValue = getTaxTotalValue(debitNoteType.getDebitNoteType().getTaxTotal(), IUBLConfig.TAX_TOTAL_ISC_ID);
            debitNoteObj.setIscValue(getCurrency(iscValue, currencyCode));

            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNotePDF() [" + this.docUUID + "] Extrayendo informacion de la percepción.");
            }

            BigDecimal percepctionAmount = null;
            BigDecimal perceptionPercentage = null;
            for (int i = 0; i < debitNoteType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (debitNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = debitNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = debitNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    debitNoteObj.setPerceptionAmount(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getValue() + " " + debitNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    debitNoteObj.setPerceptionPercentage(debitNoteType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < debitNoteType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < debitNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (debitNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        debitNoteObj.setISCPercetange(debitNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = debitNoteType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                debitNoteObj.setISCPercetange(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                debitNoteObj.setPerceptionAmount(debitNoteType.getDebitNoteType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                debitNoteObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
            }

            if (null != debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount() && null != debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount().getValue()) {
                debitNoteObj.setDiscountValue(getCurrency(debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getAllowanceTotalAmount().getValue(), currencyCode));
            } else {
                debitNoteObj.setDiscountValue(getCurrency(BigDecimal.ZERO, currencyCode));
            }

            BigDecimal payableAmount = debitNoteType.getDebitNoteType().getRequestedMonetaryTotal().getPayableAmount().getValue();
            debitNoteObj.setTotalAmountValue(getCurrency(payableAmount, currencyCode));

            BigDecimal gravadaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            debitNoteObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            debitNoteObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            debitNoteObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(debitNoteType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
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
            String rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "08" + File.separator + debitNoteType.getDebitNoteType().getID().getValue() + ".png";
            File f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoQR" + File.separator + "08");
            if (!f.exists()) {
                f.mkdirs();
            }

            if (logger.isInfoEnabled()) {
                logger.debug("generateDebitNotePDF() [" + this.docUUID + "] rutaPath: \n" + rutaPath);
            }

            inputStream = generateQRCode(barcodeValue, rutaPath);

            debitNoteObj.setCodeQR(inputStream);

            f = new File(".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + "08");
            rutaPath = ".." + File.separator + "ADJUNTOS" + File.separator + "CodigoPDF417" + File.separator + debitNoteType.getDebitNoteType().getID().getValue() + ".png";
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

            debitNoteObj.setResolutionCodeValue("resolutionCde");

            debitNoteInBytes = createDebitNotePDF(debitNoteObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateDebitNotePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateDebitNotePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateDebitNotePDF() [" + this.docUUID + "]");
        }
        return debitNoteInBytes;
    } // generateDebitNotePDF

    public byte[] createDebitNotePDF(DebitNoteObject debitNoteObj, String docUUID, ConfigData configData) throws PDFReportException {
        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;

        if (logger.isDebugEnabled()) {
            logger.debug("+createDebitNotePDF() [" + docUUID + "]");
        }
        byte[] pdfDocument = null;

        if (null == debitNoteObj) {
            throw new PDFReportException(IVenturaError.ERROR_409);
        } else {
            try {
                /* Crea instancia del MAP */
                parameterMap = new HashMap<String, Object>();
                parameterMap.put(IPDFCreatorConfig.DOCUMENT_IDENTIFIER, debitNoteObj.getDocumentIdentifier());
                parameterMap.put(IPDFCreatorConfig.ISSUE_DATE, debitNoteObj.getIssueDate());
                parameterMap.put(IPDFCreatorConfig.DUE_DATE, debitNoteObj.getDueDate());
                parameterMap.put(IPDFCreatorConfig.CURRENCY_VALUE, debitNoteObj.getCurrencyValue());

                if (StringUtils.isNotBlank(debitNoteObj.getSunatTransaction())) {
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_LABEL, IPDFCreatorConfig.OPERATION_TYPE_DSC);
                    parameterMap.put(IPDFCreatorConfig.OPERATION_TYPE_VALUE, debitNoteObj.getSunatTransaction());
                }

                parameterMap.put(IPDFCreatorConfig.PAYMENT_CONDITION, debitNoteObj.getPaymentCondition());
                parameterMap.put(IPDFCreatorConfig.SELL_ORDER, debitNoteObj.getSellOrder());
                parameterMap.put(IPDFCreatorConfig.SELLER_NAME, debitNoteObj.getSellerName());
                parameterMap.put(IPDFCreatorConfig.REMISSION_GUIDE, debitNoteObj.getRemissionGuides());
                parameterMap.put(IPDFCreatorConfig.PORCIGV, debitNoteObj.getPorcentajeIGV());
                parameterMap.put(IPDFCreatorConfig.DEBIT_NOTE_TYPE_VALUE, debitNoteObj.getTypeOfDebitNote());
                parameterMap.put(IPDFCreatorConfig.DEBIT_NOTE_DESC_VALUE, debitNoteObj.getDescOfDebitNote());
                parameterMap.put(IPDFCreatorConfig.REFERENCE_DOC_VALUE, debitNoteObj.getDocumentReferenceToCn());
                parameterMap.put(IPDFCreatorConfig.DATE_REFERENCE_DOC_VALUE, debitNoteObj.getDateDocumentReference());
                parameterMap.put(IPDFCreatorConfig.SENDER_SOCIAL_REASON, debitNoteObj.getSenderSocialReason());
                parameterMap.put(IPDFCreatorConfig.SENDER_RUC, debitNoteObj.getSenderRuc());
                parameterMap.put(IPDFCreatorConfig.SENDER_FISCAL_ADDRESS, debitNoteObj.getSenderFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.SENDER_DEP_PROV_DIST, debitNoteObj.getSenderDepProvDist());
                parameterMap.put(IPDFCreatorConfig.SENDER_CONTACT, debitNoteObj.getSenderContact());
                parameterMap.put(IPDFCreatorConfig.SENDER_MAIL, debitNoteObj.getSenderMail());
                parameterMap.put(IPDFCreatorConfig.SENDER_LOGO_PATH, debitNoteObj.getSenderLogo());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL, debitNoteObj.getTelefono());
                parameterMap.put(IPDFCreatorConfig.SENDER_TEL_1, debitNoteObj.getTelefono_1());
                parameterMap.put(IPDFCreatorConfig.SENDER_WEB, debitNoteObj.getWeb());
                parameterMap.put(IPDFCreatorConfig.COMMENTS, debitNoteObj.getComentarios());
                parameterMap.put(IPDFCreatorConfig.VALIDEZPDF, debitNoteObj.getValidezPDF());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_REGISTRATION_NAME, debitNoteObj.getReceiverRegistrationName());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER, debitNoteObj.getReceiverIdentifier());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_IDENTIFIER_TYPE, debitNoteObj.getReceiverIdentifierType());
                parameterMap.put(IPDFCreatorConfig.RECEIVER_FISCAL_ADDRESS, debitNoteObj.getReceiverFiscalAddress());
                parameterMap.put(IPDFCreatorConfig.CAMPOS_USUARIO_CAB, debitNoteObj.getInvoicePersonalizacion());
                parameterMap.put(IPDFCreatorConfig.PERCENTAGE_PERCEPTION, debitNoteObj.getPerceptionPercentage());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_PERCEPTION, debitNoteObj.getPerceptionAmount());
                parameterMap.put(IPDFCreatorConfig.PORCISC, debitNoteObj.getISCPercetange());
                parameterMap.put(IPDFCreatorConfig.SUBTOTAL_VALUE, debitNoteObj.getSubtotalValue());
                parameterMap.put(IPDFCreatorConfig.IGV_VALUE, debitNoteObj.getIgvValue());
                parameterMap.put(IPDFCreatorConfig.ISC_VALUE, debitNoteObj.getIscValue());
                parameterMap.put(IPDFCreatorConfig.AMOUNT_VALUE, debitNoteObj.getAmountValue());
                parameterMap.put(IPDFCreatorConfig.DISCOUNT_VALUE, debitNoteObj.getDiscountValue());
                parameterMap.put(IPDFCreatorConfig.TOTAL_AMOUNT_VALUE, debitNoteObj.getTotalAmountValue());
                parameterMap.put(IPDFCreatorConfig.GRAVADA_AMOUNT_VALUE, debitNoteObj.getGravadaAmountValue());
                parameterMap.put(IPDFCreatorConfig.EXONERADA_AMOUNT_VALUE, debitNoteObj.getExoneradaAmountValue());
                parameterMap.put(IPDFCreatorConfig.INAFECTA_AMOUNT_VALUE, debitNoteObj.getInafectaAmountValue());

                if (StringUtils.isNotBlank(debitNoteObj.getGratuitaAmountValue())) {
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL, IPDFCreatorConfig.GRATUITA_AMOUNT_LABEL_DSC);
                    parameterMap.put(IPDFCreatorConfig.GRATUITA_AMOUNT_VALUE, debitNoteObj.getGratuitaAmountValue());
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Codigo QR")) {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, debitNoteObj.getCodeQR());
                } else {
                    parameterMap.put(IPDFCreatorConfig.CODEQR, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("PDF 417")) {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, debitNoteObj.getBarcodeValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.BARCODE_VALUE, null);
                }

                if (configData.getImpresionPDF().equalsIgnoreCase("Valor Resumen")) {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, debitNoteObj.getDigestValue());
                } else {
                    parameterMap.put(IPDFCreatorConfig.DIGESTVALUE, null);
                }

                parameterMap.put(IPDFCreatorConfig.LETTER_AMOUNT_VALUE, debitNoteObj.getLetterAmountValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, "C:\\clientes\\files\\20510910517\\formatos\\legendReport.jasper"/*this.legendSubReportPath*/); /*this.legendSubReportPath);*/
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(debitNoteObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_DEBIT_NOTE_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, debitNoteObj.getResolutionCodeValue());
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "debitNoteDocument_Ing.jrxml" : "debitNoteDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(debitNoteObj.getSenderRuc(), documentName);
                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(debitNoteObj.getItemsListDynamic()));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
            } catch (Exception e) {
                logger.error("createDebitNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                logger.error("createDebitNotePDF() [" + docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new PDFReportException(IVenturaError.ERROR_444);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-createDebitNotePDF() [" + docUUID + "]");
        }
        return pdfDocument;
    }

}
