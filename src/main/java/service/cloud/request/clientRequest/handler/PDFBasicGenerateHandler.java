package service.cloud.request.clientRequest.handler;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;

public class PDFBasicGenerateHandler {

    Logger logger = LoggerFactory.getLogger(PDFBasicGenerateHandler.class);

    protected String docUUID;

    public PDFBasicGenerateHandler(String docUUID) {
        this.docUUID = docUUID;
    } // PDFBasicGenerateHandler

    public String generateDigestValue(UBLExtensionsType ublExtensions)
            throws PDFReportException {

        String digestValue = null;
        try {
            digestValue = getDigestValue(ublExtensions);
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            throw new PDFReportException(IVenturaError.ERROR_418);
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
            throw e;
        }
        return digestValue;
    } // getDigestValue

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
            throw e;
        }
        return signatureValue;
    } // getSignatureValue

    protected String getContractDocumentReference(
            List<DocumentReferenceType> contractDocumentReferences, String code) {
        String contractDocRefResponse = null;

        if (null != contractDocumentReferences
                && 0 < contractDocumentReferences.size()) {
            for (DocumentReferenceType contDocumentRef : contractDocumentReferences) {
                if (contDocumentRef.getDocumentTypeCode().getValue()
                        .equalsIgnoreCase(code)) {
                    contractDocRefResponse = contDocumentRef.getID().getValue();
                }
            }
        }

        if (StringUtils.isBlank(contractDocRefResponse)) {
            contractDocRefResponse = IPDFCreatorConfig.EMPTY_VALUE;
        }
        return contractDocRefResponse;
    } // getContractDocumentReference

    public String generateGuiaBarcodeInfoV2(String identifier, String documentType, String issueDate, BigDecimal payableAmountVal, BigDecimal taxTotalList, SupplierPartyType accSupplierParty, CustomerPartyType accCustomerParty, UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = null;
        try {
            /* a.) Numero de RUC del emisor electronico */
            String senderRuc = accSupplierParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* b.) Tipo de comprobante de pago electronico */
            /* Parametro de entrada del metodo */
            /* c.) Numeracion conformada por serie y numero correlativo */
            String serie = identifier.substring(0, 4);
            String correlative = Integer.valueOf(identifier.substring(5)).toString();
            /* d.) Sumatoria IGV, de ser el caso */
            String igvTax = null;
            BigDecimal igvTaxBigDecimal = BigDecimal.ZERO;
            igvTax = igvTaxBigDecimal.setScale(2, RoundingMode.HALF_UP).toString();
            /* e.) Importe total de la venta, cesion en uso o servicio prestado */
            String payableAmount = payableAmountVal.toString();
            /* f.) Fecha de emision */
            /* Parametro de entrada del metodo */
            /* g,) Tipo de documento del adquiriente o usuario */
            String receiverDocType = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getSchemeID();
            /* h.) Numero de documento del adquiriente */
            String receiverDocNumber = accCustomerParty.getParty().getPartyIdentification().get(0).getID().getValue();
            /* i.) Valor resumen <ds:DigestValue> */
            String digestValue = getDigestValue(ublExtensions);
            // String digestValue=null;
            /* j.) Valor de la Firma digital <ds:SignatureValue> */
            String signatureValue = getSignatureValue(ublExtensions);
            /*
             * Armando el codigo de barras
             */
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, senderRuc, documentType, serie, correlative, igvTax, payableAmount, issueDate, receiverDocType, receiverDocNumber, digestValue, signatureValue);
        } catch (PDFReportException e) {
            throw e;
        } catch (Exception e) {
            throw new PDFReportException(IVenturaError.ERROR_418);
        }
        return barcodeValue;
    }
} // PDFBasicGenerateHandler
