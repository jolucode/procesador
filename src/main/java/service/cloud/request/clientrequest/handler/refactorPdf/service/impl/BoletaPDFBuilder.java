package service.cloud.request.clientrequest.handler.refactorPdf.service.impl;

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
import service.cloud.request.clientrequest.dto.dto.*;
import service.cloud.request.clientrequest.dto.finalClass.ConfigData;
import service.cloud.request.clientrequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientrequest.extras.IUBLConfig;
import service.cloud.request.clientrequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientrequest.handler.refactorPdf.dto.WrapperItemObject;
import service.cloud.request.clientrequest.handler.refactorPdf.dto.legend.BoletaObject;
import service.cloud.request.clientrequest.handler.refactorPdf.dto.legend.LegendObject;
import service.cloud.request.clientrequest.handler.refactorPdf.config.JasperReportConfig;
import service.cloud.request.clientrequest.handler.refactorPdf.service.BoletaPDFGenerator;
import service.cloud.request.clientrequest.utils.exception.PDFReportException;
import service.cloud.request.clientrequest.utils.exception.error.ErrorObj;
import service.cloud.request.clientrequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientrequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientrequest.xmlFormatSunat.xsd.commonbasiccomponents_2.NameType;
import service.cloud.request.clientrequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TaxableAmountType;
import service.cloud.request.clientrequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class BoletaPDFBuilder extends BaseDocumentService  implements BoletaPDFGenerator {

    private static final Logger logger = Logger.getLogger(BoletaPDFBuilder.class);

    @Autowired
    private JasperReportConfig jasperReportConfig;

    String docUUID = "asd";


    @Override
    public byte[] generateBoletaPDF(UBLDocumentWRP boletaType, ConfigData configData) {
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
                boletaObj.setDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento());
            } else {
                boletaObj.setDueDate(boletaType.getTransaccion().getDOC_FechaVencimiento());
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

            if (null != boletaType.getTransaccion().getTransactionContractDocRefListDTOS()
                    && !boletaType.getTransaccion().getTransactionContractDocRefListDTOS().isEmpty()) {

                Map<String, String> hashedMap = new HashMap<>();

                for (Map<String, String> map : boletaType.getTransaccion().getTransactionContractDocRefListDTOS()) {
                    // Suponiendo que cada mapa tiene solo un par clave-valor
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        hashedMap.put(entry.getKey(), entry.getValue());
                    }
                }

                boletaObj.setInvoicePersonalizacion(hashedMap);
            }

            List<WrapperItemObject> listaItem = new ArrayList<WrapperItemObject>();

            for (int i = 0; i < boletaType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("generateBoletaPDF() [" + this.docUUID + "] Agregando datos al HashMap");
                }

                WrapperItemObject itemObject = new WrapperItemObject();
                Map<String, String> itemObjectHash = new HashMap<>();
                List<String> newlist = new ArrayList<>();

                // Obtener el mapa de transaccionLineasCamposUsuario
                Map<String, String> camposUsuarioMap = boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransaccionLineasCamposUsuario();

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
            for (int i = 0; i < boletaType.getTransaccion().getTransactionTotalesDTOList().size(); i++) {
                if (boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getId().equalsIgnoreCase("2001")) {
                    percepctionAmount = boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto();
                    perceptionPercentage = boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt();
                    boletaObj.setPerceptionAmount(currencyCode + " " + boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getMonto().toString());
                    boletaObj.setPerceptionPercentage(boletaType.getTransaccion().getTransactionTotalesDTOList().get(i).getPrcnt().toString() + "%");
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo monto de ISC.");
            }
            BigDecimal retentionpercentage = null;

            for (int i = 0; i < boletaType.getTransaccion().getTransactionLineasDTOList().size(); i++) {
                for (int j = 0; j < boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().size(); j++) {
                    if (boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getTipoTributo().equalsIgnoreCase("2000")) {
                        boletaObj.setPorcentajeISC(boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje().setScale(1, RoundingMode.HALF_UP).toString());
                        retentionpercentage = boletaType.getTransaccion().getTransactionLineasDTOList().get(i).getTransactionLineasImpuestoListDTO().get(j).getPorcentaje();
                        break;
                    }
                }
            }

            if (retentionpercentage == null) {
                boletaObj.setPorcentajeISC(BigDecimal.ZERO.toString());
            }

            if (percepctionAmount == null) {
                boletaObj.setPerceptionAmount(boletaType.getInvoiceType().getDocumentCurrencyCode().getValue() + " 0.00");
            }
            if (perceptionPercentage == null) {
                boletaObj.setPerceptionPercentage(BigDecimal.ZERO.toString());
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
            boletaObj.setSenderLogo("C:\\clientes\\files\\20510910517\\COMPANY_LOGO.jpg");
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
            List<TransactionActicipoDTO> anticipoList = boletaType.getTransaccion().getTransactionActicipoDTOList();
            String anticipos = anticipoList.parallelStream().map(TransactionActicipoDTO::getAntiDOC_Serie_Correlativo).collect(Collectors.joining(" "));
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

            BigDecimal subtotalValue = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1005);
            if (null != boletaType.getInvoiceType().getPrepaidPayment() && !boletaType.getInvoiceType().getPrepaidPayment().isEmpty() && null != boletaType.getInvoiceType().getPrepaidPayment().get(0).getPaidAmount()) {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue.add(prepaidAmount.multiply(BigDecimal.ONE.negate())), currencyCode));
            } else {
                boletaObj.setSubtotalValue(getCurrency(subtotalValue, currencyCode));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoicePDF() [" + this.docUUID + "] Extrayendo Campos de usuarios personalizados." + boletaType.getTransaccion().getTransactionContractDocRefListDTOS().size());
            }

            BigDecimal igvValue = getTaxTotalValue2(boletaType.getTransaccion().getTransactionImpuestosDTOList(), IUBLConfig.TAX_TOTAL_IGV_ID);
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

            BigDecimal gravadaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1001);
            boletaObj.setGravadaAmountValue(getCurrency(gravadaAmount, currencyCode));

            BigDecimal inafectaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1002);
            boletaObj.setInafectaAmountValue(getCurrency(inafectaAmount, currencyCode));

            BigDecimal exoneradaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1003);
            boletaObj.setExoneradaAmountValue(getCurrency(exoneradaAmount, currencyCode));

            BigDecimal gratuitaAmount = getTransaccionTotales(boletaType.getTransaccion().getTransactionTotalesDTOList(), IUBLConfig.ADDITIONAL_MONETARY_1004);
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

            boletaObj.setResolutionCodeValue("resolutionCde");

            /*
             * Generando el PDF de la FACTURA con la informacion recopilada.
             */
            boletaInBytes = createBoletaPDF(boletaObj, configData);//PDFBoletaCreator.getInstance(this.documentReportPath, this.legendSubReportPath).createBoletaPDF(boletaObj, docUUID, configData);
        } catch (PDFReportException e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] PDFReportException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
        } catch (Exception e) {
            logger.error("generateInvoicePDF() [" + this.docUUID + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            ErrorObj error = new ErrorObj(IVenturaError.ERROR_2.getId(), e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoicePDF() [" + this.docUUID + "]");
        }
        return boletaInBytes;
    } // generateBoletaPDF

    public byte[] createBoletaPDF(BoletaObject boletaObj, ConfigData configData) throws PDFReportException {

        Map<String, Object> parameterMap;
        Map<String, Object> cuotasMap;

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
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DIR, "C:\\clientes\\files\\20510910517\\formatos\\legendReport.jasper");
                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_DATASOURCE, new JRBeanCollectionDataSource(boletaObj.getLegends()));

                Map<String, String> legendMap = new HashMap<String, String>();
                legendMap.put(IPDFCreatorConfig.LEGEND_DOCUMENT_TYPE, IPDFCreatorConfig.LEGEND_BOLETA_DOCUMENT);
                legendMap.put(IPDFCreatorConfig.RESOLUTION_CODE_VALUE, boletaObj.getResolutionCodeValue());

                parameterMap.put(IPDFCreatorConfig.SUBREPORT_LEGENDS_MAP, legendMap);

                /*
                 * Generar el reporte con la informacion de la boleta
                 * de venta electronica
                 */
                String documentName = (configData.getPdfIngles() != null && configData.getPdfIngles().equals("Si")) ? "boletaDocument_Ing.jrxml" : "boletaDocument.jrxml";
                JasperReport jasperReport = jasperReportConfig.getJasperReportForRuc(boletaObj.getSenderRuc(), documentName);
                JasperPrint iJasperPrint = JasperFillManager.fillReport(jasperReport, parameterMap,
                        new JRBeanCollectionDataSource(boletaObj.getItemsDynamic()));

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JasperExportManager.exportReportToPdfStream(iJasperPrint, outputStream);
                pdfDocument = outputStream.toByteArray();
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
    }

    protected String getSunatTransactionInfo(
            List<UBLExtensionType> ublExtensionList) throws PDFReportException {
        String sunatTransactionInfo = null;
        try {
            String sunatTransCode = null;
            NodeList nodeList = ublExtensionList.get(0).getExtensionContent()
                    .getAny()
                    .getElementsByTagName(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName()
                        .equalsIgnoreCase(IUBLConfig.UBL_SUNAT_TRANSACTION_TAG)) {
                    sunatTransCode = nodeList.item(i).getTextContent();
                    break;
                }
            }

            if (StringUtils.isNotBlank(sunatTransCode)) {
                sunatTransCode = sunatTransCode.replace("\n", "").trim();

                switch (sunatTransCode) {
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_EXPORTACION_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_NO_DOMICILIADOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_INTERNA_ANTICIPOS_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ITINERANTE_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_GUIA_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_VENTA_ARROZ_PILADO_DSC;
                        break;
                    case IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_ID:
                        sunatTransactionInfo = IPDFCreatorConfig.SUNAT_TRANS_FACTURA_COMP_PERCEPCION_DSC;
                        break;
                    default:
                        throw new PDFReportException(IVenturaError.ERROR_421);
                }
            }
        } catch (PDFReportException e) {
            logger.error("getSunatTransactionInfo() ["
                    + "] ERROR: " + e.getError().getId() + "-"
                    + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getSunatTransactionInfo() ["
                    + "] Exception -->" + e.getMessage());
        }
        return sunatTransactionInfo;
    } // getSunatTransactionInfo

}
