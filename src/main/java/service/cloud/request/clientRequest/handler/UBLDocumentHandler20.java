
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service.cloud.request.clientRequest.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.entity.*;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.codelist.specification._54217._2001.CurrencyCodeContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.LocationType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.TotalInvoiceAmountType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.ExtensionContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.AdditionalInformationType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Esta clase HANDLER contiene metodos para generar objetos UBL, necesarios para
 * armar el documento UBL validado por Sunat.
 */

/**
 * @author VSUSER
 */
public class UBLDocumentHandler20 {

    private final Logger logger = Logger.getLogger(UBLDocumentHandler20.class);

    /* Campos Adicionales */
    /*
     *
     * */
    /* Identificador del objeto */
    private final String identifier;

    /**
     * Constructor privado para evitar la creacion de instancias usando el
     * constructor.
     *
     * @param identifier Identificador del objeto UBLDocumentHandler20 creado.
     */
    private UBLDocumentHandler20(String identifier) {
        this.identifier = identifier;
    } // UBLDocumentHandler20

    /**
     * Este metodo crea una nueva instancia de la clase UBLDocumentHandler20.
     *
     * @param identifier Identificador del objeto UBLDocumentHandler20 creado.
     * @return Retorna una nueva instancia de la clase UBLDocumentHandler20.
     */
    public static synchronized UBLDocumentHandler20 newInstance(String identifier) {
        return new UBLDocumentHandler20(identifier);
    } // newInstance

    /**
     * Este metodo genera el objeto SignatureType, que es utilizado para armar
     * el documento UBL.
     *
     * @param identifier   El identificador (numero RUC) del contribuyente.
     * @param socialReason La razon social del contribuyente.
     * @param signerName   El nombre del firmante (puede ser un valor
     *                     identificador).
     * @return Retorna el objeto SignatureType con los datos del contribuyente.
     * @throws UBLDocumentException
     */
    public SignatureType generateSignature(String identifier, String socialReason, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateSignature() [" + this.identifier + "]");
        }
        SignatureType signature = null;

        try {
            /* <cac:Signature><cbc:ID> */
            IDType idSigner = new IDType();
            idSigner.setValue(signerName);

            /* <cac:Signature><cac:SignatoryParty> */
            PartyType signatoryParty = new PartyType();
            {
                PartyIdentificationType partyIdentification = new PartyIdentificationType();
                IDType idRUC = new IDType();
                idRUC.setValue(identifier);
                partyIdentification.setID(idRUC);
                signatoryParty.getPartyIdentification().add(partyIdentification);

                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(socialReason);
                partyName.setName(name);
                signatoryParty.getPartyName().add(partyName);
            }

            /* <cac:Signature><cac:DigitalSignatureAttachment> */
            AttachmentType digitalSignatureAttachment = new AttachmentType();
            {
                ExternalReferenceType externalReference = new ExternalReferenceType();
                URIType uri = new URIType();
                uri.setValue("#" + signerName);
                externalReference.setURI(uri);
                digitalSignatureAttachment.setExternalReference(externalReference);
            }

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            signature = new SignatureType();
            signature.setID(idSigner);
            signature.setSignatoryParty(signatoryParty);
            signature.setDigitalSignatureAttachment(digitalSignatureAttachment);
        } catch (Exception e) {
            throw new UBLDocumentException(IVenturaError.ERROR_301);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateSignature() [" + this.identifier + "] signature: " + signature);
        }
        return signature;
    } // generateSignature

    /**
     * Este metodo genera el objeto SupplierPartyType que es utilizado para
     * contener los datos del emisor electronico y armar el documento UBL.
     *
     * @param identifier     El identificador (numero de RUC) del contribuyente
     *                       emisor.
     * @param identifierType El tipo de identificador del contribuyente emisor.
     * @param socialReason   La razon social del contribuyente emisor.
     * @param commercialName El nombre comercial del contribuyente emisor.
     * @param fiscalAddress  La direccion fiscal del contribuyente emisor.
     * @param department     El nombre del departamento del contribuyente emisor.
     * @param province       El nombre de la provincia del contribuyente emisor.
     * @param district       El nombre del distrito del contribuyente emisor.
     * @param ubigeo         El codigo de ubigeo del contribuyente emisor.
     * @param countryCode    El codigo de pais del contribuyente emisor.
     * @param contactName    El nombre del contacto emisor.
     * @param electronicMail El correo electronico del emisor.
     * @return Retorna el objeto SupplierPartyType con los datos del
     * contribuyente emisor.
     * @throws UBLDocumentException
     */
    public SupplierPartyType generateAccountingSupplierParty(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress, String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateAccountingSupplierParty() [" + this.identifier + "]");
        }
        SupplierPartyType accountingSupplierParty = null;

        try {

            if (logger.isDebugEnabled()) {
                logger.debug("+CustomerAssignedAccountIDType() [" + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);

            if (logger.isDebugEnabled()) {
                logger.debug("+AdditionalAccountIDType() [" + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);

            if (logger.isDebugEnabled()) {
                logger.debug("+PartyType() [" + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cac:Party> */
            PartyType party = generateParty(socialReason, commercialName, fiscalAddress, department, province, district, ubigeo, countryCode, contactName, electronicMail);

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            accountingSupplierParty = new SupplierPartyType();
            accountingSupplierParty.setCustomerAssignedAccountID(customerAssignedAccountID);
            accountingSupplierParty.getAdditionalAccountID().add(additionalAccountID);
            accountingSupplierParty.setParty(party);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("-generateAccountingSupplierParty() [" + this.identifier + "] accountingSupplierParty: " + e.getMessage());
            }
            throw new UBLDocumentException(IVenturaError.ERROR_302);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateAccountingSupplierParty() [" + this.identifier + "] accountingSupplierParty: " + accountingSupplierParty);
        }
        return accountingSupplierParty;
    } // generateAccountingSupplierParty

    public SupplierPartyType generateAccountingSupplierPartyV2(String identifier, String identifierType, String socialReason, String commercialName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateAccountingSupplierParty() [" + this.identifier + "]");
        }
        SupplierPartyType accountingSupplierParty = null;

        try {
            /* <cac:AccountingSupplierParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);
            /* <cac:AccountingSupplierParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);
            /* <cac:AccountingSupplierParty><cac:Party> */
            PartyType party = generatePartyV2(socialReason, commercialName);
            /*
             * Armar el objeto con sus respectivos TAG's
             */
            accountingSupplierParty = new SupplierPartyType();
            accountingSupplierParty.setCustomerAssignedAccountID(customerAssignedAccountID);
            accountingSupplierParty.getAdditionalAccountID().add(additionalAccountID);
            accountingSupplierParty.setParty(party);
        } catch (Exception e) {
            throw new UBLDocumentException(IVenturaError.ERROR_302);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateAccountingSupplierParty() [" + this.identifier + "] accountingSupplierParty: " + accountingSupplierParty);
        }
        return accountingSupplierParty;
    } // generateAccountingSupplierParty

    public List<SUNATRetentionDocumentReferenceType> generateDocumentReferenceV2(List<TransaccionComprobantePago> tscp) throws UBLDocumentException {
        List<SUNATRetentionDocumentReferenceType> lst = new ArrayList<SUNATRetentionDocumentReferenceType>();
        for (TransaccionComprobantePago transaccionComprobantePago : tscp) {
            SUNATRetentionDocumentReferenceType documentReferenceType = new SUNATRetentionDocumentReferenceType();
            IDType objIdType = new IDType();
            /**
             * ****************** <cbc:ID schemeID=></cbc:ID>
             * ****************************
             */
            objIdType.setSchemeID(transaccionComprobantePago.getTipoComprobante());
            objIdType.setValue(transaccionComprobantePago.getDOC_Numero());
            documentReferenceType.setId(objIdType);
            /**
             * ********************** <cbc:IssueDate></cbc:IssueDate>
             * ***************************************************
             */
            documentReferenceType.setIssueDate(getIssueDate(transaccionComprobantePago.getDOC_FechaEmision()));

            /**
             * ****
             * <cbc:TotalInvoiceAmount currencyID></cbc:TotalInvoiceAmount> *
             */
            TotalInvoiceAmountType totalInvoiceAmountType = new TotalInvoiceAmountType();
            totalInvoiceAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getDOC_Moneda()).value());
            totalInvoiceAmountType.setValue(transaccionComprobantePago.getDOC_Importe().setScale(2, RoundingMode.HALF_UP));

            documentReferenceType.setTotalInvoiceAmount(totalInvoiceAmountType);

            SUNATRetentionInformationType sunatRetentionInformationType = new SUNATRetentionInformationType();
            PaymentType paymentType = new PaymentType();
            paymentType.setPaidDate(getIssueDate2(transaccionComprobantePago.getPagoFecha()));
            IDType numero = new IDType();
            numero.setValue(String.valueOf(transaccionComprobantePago.getPagoNumero()));
            paymentType.setID(numero);
            PaidAmountType objPaidAmountType = new PaidAmountType();
            objPaidAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getPagoMoneda()).value());
            objPaidAmountType.setValue(transaccionComprobantePago.getPagoImporteSR().setScale(2, RoundingMode.HALF_UP));
            paymentType.setPaidAmount(objPaidAmountType);
            documentReferenceType.setPayment(paymentType);
            SUNATRetentionAmountType objPerceptionAmountType = new SUNATRetentionAmountType();
            objPerceptionAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMoneda()).value());
            objPerceptionAmountType.setValue(transaccionComprobantePago.getCP_Importe().setScale(2, RoundingMode.HALF_UP));

            sunatRetentionInformationType.setSunatRetentionAmount(objPerceptionAmountType);
            SUNATNetTotalPaidType objCashedType = new SUNATNetTotalPaidType();
            objCashedType.setCurrencyID(CurrencyCodeContentType.valueOf(tscp.get(0).getCPMonedaMontoNeto()).value());
            objCashedType.setValue(transaccionComprobantePago.getCP_ImporteTotal().setScale(2, RoundingMode.HALF_UP));

            sunatRetentionInformationType.setSunatNetTotalPaid(objCashedType);
            sunatRetentionInformationType.setSunatRetentionDate(getIssueDate5(transaccionComprobantePago.getCP_Fecha()));

            ExchangeRateType exchangeRateType = new ExchangeRateType();
            SourceCurrencyCodeType currencyCodeType = new SourceCurrencyCodeType();
            currencyCodeType.setValue(transaccionComprobantePago.getTCMonedaRef());
            exchangeRateType.setSourceCurrencyCode(currencyCodeType);
            TargetCurrencyCodeType targetCurrencyCodeType = new TargetCurrencyCodeType();
            targetCurrencyCodeType.setValue(transaccionComprobantePago.getTCMonedaObj());
            exchangeRateType.setTargetCurrencyCode(targetCurrencyCodeType);
            CalculationRateType calculationRateType = new CalculationRateType();
            calculationRateType.setValue(transaccionComprobantePago.getTC_Factor().setScale(3));

            exchangeRateType.setCalculationRate(calculationRateType);
            exchangeRateType.setDate(getIssueDate4(transaccionComprobantePago.getTCFecha()));
            sunatRetentionInformationType.setExchangeRate(exchangeRateType);
            documentReferenceType.setSunatRetentionInformation(sunatRetentionInformationType);
            lst.add(documentReferenceType);
        }
        return lst;
    }

    public List<SUNATPerceptionDocumentReferenceType> generateDocumentReference(List<TransaccionComprobantePago> tscp) throws UBLDocumentException {
        List<SUNATPerceptionDocumentReferenceType> sunatPerceptionDocumentReferenceTypes = new ArrayList<SUNATPerceptionDocumentReferenceType>();
        SUNATPerceptionDocumentReferenceType documentReferenceType = new SUNATPerceptionDocumentReferenceType();

        for (TransaccionComprobantePago transaccionComprobantePago : tscp) {
            IDType objIdType = new IDType();
            if (logger.isInfoEnabled()) {
                logger.info("SUNATPerceptionDocumentReference() ID - " + transaccionComprobantePago.getTipoComprobante() + " - " + transaccionComprobantePago.getDOC_Numero());
            }
            /**
             * ****************** <cbc:ID schemeID=></cbc:ID>
             * ****************************
             */
            objIdType.setSchemeID(transaccionComprobantePago.getTipoComprobante());
            objIdType.setValue(transaccionComprobantePago.getDOC_Numero());

            documentReferenceType.setId(objIdType);

            /**
             * ********************** <cbc:IssueDate></cbc:IssueDate>
             * ***************************************************
             */
            if (logger.isInfoEnabled()) {
                logger.info("SUNATPerceptionDocumentReference()  - ISSUEDATE " + transaccionComprobantePago.getDOC_FechaEmision());
            }
            documentReferenceType.setIssueDate(getIssueDate(transaccionComprobantePago.getDOC_FechaEmision()));
            /**
             * ****
             * <cbc:TotalInvoiceAmount currencyID></cbc:TotalInvoiceAmount> *
             */
            if (logger.isInfoEnabled()) {
                logger.info("SUNATPerceptionDocumentReference()  - TOTALAMOUNT " + transaccionComprobantePago.getDOC_Importe());
            }
            TotalInvoiceAmountType totalInvoiceAmountType = new TotalInvoiceAmountType();
            totalInvoiceAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getDOC_Moneda()).value());
            totalInvoiceAmountType.setValue(transaccionComprobantePago.getDOC_Importe().setScale(2));

            documentReferenceType.setTotalInvoiceAmount(totalInvoiceAmountType);
            /**
             * ****************** <cac:Payment></cac:Payment>
             * **************************
             */
            if (logger.isInfoEnabled()) {
                logger.info("Payment()  - PaidDate " + transaccionComprobantePago.getPagoFecha());
            }
            PaymentType paymentType = new PaymentType();
            paymentType.setPaidDate(getIssueDate2(transaccionComprobantePago.getPagoFecha()));

            if (logger.isInfoEnabled()) {
                logger.info("Payment()  - ID " + transaccionComprobantePago.getPagoNumero());
            }
            IDType numero = new IDType();
            numero.setValue(String.valueOf(transaccionComprobantePago.getPagoNumero()));
            paymentType.setID(numero);

            if (logger.isInfoEnabled()) {
                logger.info("Payment()  - PaidAmountT " + transaccionComprobantePago.getPagoMoneda());
            }
            PaidAmountType objPaidAmountType = new PaidAmountType();
            objPaidAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getPagoMoneda()).value());
            objPaidAmountType.setValue(transaccionComprobantePago.getPagoImporteSR().setScale(2));
            paymentType.setPaidAmount(objPaidAmountType);

            documentReferenceType.setPayment(paymentType);

            SUNATPerceptionInformationType objPerceptionInformationType = new SUNATPerceptionInformationType();

            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation() - SUNATPerceptionAmount " + tscp.get(0).getCP_Importe());
            }
            SUNATPerceptionAmountType objPerceptionAmountType = new SUNATPerceptionAmountType();
            objPerceptionAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(tscp.get(0).getCPMoneda()).value());
            objPerceptionAmountType.setValue(tscp.get(0).getCP_Importe().setScale(2));

            objPerceptionInformationType.setPerceptionAmount(objPerceptionAmountType);

            /**
             * *****************************************
             * <sac:SUNATNetTotalCashed currencyID></sac:SUNATNetTotalCashed>
             * ***************************************************************
             */
            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - SUNATNetTotalCashed " + transaccionComprobantePago.getCPMonedaMontoNeto());
            }
            SUNATNetTotalCashedType objCashedType = new SUNATNetTotalCashedType();
            objCashedType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMonedaMontoNeto()).value());
            objCashedType.setValue(transaccionComprobantePago.getCP_ImporteTotal().setScale(2));

            objPerceptionInformationType.setSunatNetTotalCashed(objCashedType);

            /**
             * *****************************************
             * <sac:SUNATPerceptionDate></sac:SUNATPerceptionDate>
             * ***************************************************************
             */
            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - SUNATNetTotalCashed " + transaccionComprobantePago.getCP_Fecha());
            }
            objPerceptionInformationType.setPerceptionDate(getIssueDate3(transaccionComprobantePago.getCP_Fecha()));

            /**
             * ************************************
             * <cac:ExchangeRate></cac:ExchangeRate>
             * ****************************************************************
             */
            ExchangeRateType exchangeRateType = new ExchangeRateType();

            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - SourceCurrencyCode " + transaccionComprobantePago.getTCMonedaRef());
            }

            SourceCurrencyCodeType currencyCodeType = new SourceCurrencyCodeType();
            currencyCodeType.setValue(transaccionComprobantePago.getTCMonedaRef());
            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - targetCurrencyCode " + transaccionComprobantePago.getTCMonedaObj());
            }

            TargetCurrencyCodeType targetCurrencyCodeType = new TargetCurrencyCodeType();
            targetCurrencyCodeType.setValue(transaccionComprobantePago.getTCMonedaObj());

            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - CalculationRate " + transaccionComprobantePago.getTC_Factor());
            }

            CalculationRateType calculationRateType = new CalculationRateType();
            calculationRateType.setValue(transaccionComprobantePago.getTC_Factor());

            exchangeRateType.setSourceCurrencyCode(currencyCodeType);
            exchangeRateType.setTargetCurrencyCode(targetCurrencyCodeType);
            exchangeRateType.setCalculationRate(calculationRateType);
            if (logger.isInfoEnabled()) {
                logger.info("sac:SUNATPerceptionInformation()  - CPFecha" + transaccionComprobantePago.getTCFecha());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = sdf.format(transaccionComprobantePago.getTCFecha());
            XMLGregorianCalendar xmlCal = null;
            try {
                xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(date);
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }

            DateType dateType = new DateType();
            dateType.setValue(xmlCal);
            exchangeRateType.setDate(dateType);
            objPerceptionInformationType.setExchangeRate(exchangeRateType);
            documentReferenceType.setSunatPerceptionInformation(objPerceptionInformationType);
            sunatPerceptionDocumentReferenceTypes.add(documentReferenceType);
        }
        return sunatPerceptionDocumentReferenceTypes;
    }

    /**
     * Este metodo genera el objeto CustomerPartyType que es utilizado para
     * contener los datos del emisor electronico y armar el documento UBL.
     *
     * @param identifier     El identificador del contribuyente receptor.
     * @param identifierType El tipo de identificador del contribuyente
     *                       receptor.
     * @param socialReason   La razon social del contribuyente receptor.
     * @param commercialName El nombre comercial del contribuyente receptor.
     * @param fiscalAddress  La direccion fiscal del contribuyente receptor.
     * @param department     El nombre del departamento del contribuyente receptor.
     * @param province       El nombre de la provincia del contribuyente receptor.
     * @param district       El nombre del distrito del contribuyente receptor.
     * @param ubigeo         El codigo de ubigeo del contribuyente receptor.
     * @param countryCode    El codigo de pais del contribuyente receptor.
     * @param contactName    El nombre del contacto del contribuyente receptor.
     * @param electronicMail El correo electronico del contribuyente receptor.
     * @return Retorna el objeto CustomerPartyType con los datos del
     * contribuyente receptor.
     * @throws UBLDocumentException
     */
    public CustomerPartyType generateAccountingCustomerParty(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress, String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateAccountingCustomerParty() [" + this.identifier + "]");
        }
        CustomerPartyType accountingCustomerParty = null;

        try {
            /* <cac:AccountingCustomerParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);

            /* <cac:AccountingCustomerParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);

            /* <cac:AccountingCustomerParty><cac:Party> */
            PartyType party = generateParty(socialReason, commercialName, fiscalAddress, department, province, district, ubigeo, countryCode, contactName, electronicMail);

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            accountingCustomerParty = new CustomerPartyType();
            accountingCustomerParty.setCustomerAssignedAccountID(customerAssignedAccountID);
            accountingCustomerParty.getAdditionalAccountID().add(additionalAccountID);
            accountingCustomerParty.setParty(party);
        } catch (Exception e) {
            logger.error("generateAccountingCustomerParty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_303.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_303);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateAccountingCustomerParty() [" + this.identifier + "] accountingCustomerParty: " + accountingCustomerParty);
        }
        return accountingCustomerParty;
    } // generateAccountingCustomerParty

    private CustomerPartyType generateAccountingCustomerPartyV2(String identifier, String identifierType, String fullname, String fiscalAddress, String department, String province, String district, String contactName, String electronicMail) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateAccountingCustomerPartyV2() [" + this.identifier + "]");
        }
        CustomerPartyType accountingCustomerParty = null;

        try {
            /* <cac:AccountingCustomerParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);

            /* <cac:AccountingCustomerParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);

            /* <cac:AccountingCustomerParty><cac:Party> */
            PartyType party = generatePartyByBoleta(fullname, fiscalAddress, department, province, district, contactName, electronicMail);

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            accountingCustomerParty = new CustomerPartyType();
            accountingCustomerParty.setCustomerAssignedAccountID(customerAssignedAccountID);
            accountingCustomerParty.getAdditionalAccountID().add(additionalAccountID);
            accountingCustomerParty.setParty(party);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("generateAccountingCustomerPartyV2() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_303.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_303);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateAccountingCustomerPartyV2() [" + this.identifier + "] accountingCustomerParty: " + accountingCustomerParty);
        }
        return accountingCustomerParty;
    } // generateAccountingSupplierPartyV2

    /**
     * Este metodo genera el objeto PartyType que es utilizado para contener los
     * datos del emisor y receptor electronico y armar el documento UBL.
     *
     * @param socialReasonValue   La razon social del contribuyente (emisor o
     *                            receptor).
     * @param commercialNameValue El nombre comercial del contribuyente (emisor
     *                            o receptor).
     * @param fiscalAddressValue  La direccion fiscal del contribuyente (emisor o
     *                            receptor).
     * @param departmentValue     El nombre del departamento del contribuyente
     *                            (emisor o receptor).
     * @param provinceValue       El nombre de la provincia del contribuyente (emisor
     *                            o receptor).
     * @param districtValue       El nombre del distrito del contribuyente (emisor o
     *                            receptor).
     * @param ubigeoValue         El codigo de ubigeo del contribuyente (emisor o
     *                            receptor).
     * @param countryCodeValue    El codigo de pais del contribuyente (emisor o
     *                            receptor).
     * @param contactNameValue    El nombre del contacto.
     * @param electronicMailValue El correo electronico del contacto.
     * @return Retorna el objeto PartyType con los datos del contribuyente
     * emisor o receptor electronico.
     */
    private PartyType generateParty(String socialReasonValue, String commercialNameValue, String fiscalAddressValue, String departmentValue, String provinceValue, String districtValue, String ubigeoValue, String countryCodeValue, String contactNameValue, String electronicMailValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateParty() [" + this.identifier + "]");
        }
        PartyType party = new PartyType();

        /* <cac:Party><cac:PartyName> */
        if (StringUtils.isNotBlank(commercialNameValue)) {
            PartyNameType partyName = new PartyNameType();
            NameType name = new NameType();
            name.setValue(commercialNameValue);
            partyName.setName(name);
            party.getPartyName().add(partyName);
        }

        /* <cac:Party><cac:PostalAddress> */
        if (StringUtils.isNotBlank(fiscalAddressValue)) {
            AddressType postalAddress = null;
            /* <cac:Party><cac:PostalAddress><cbc:ID> */
            IDType id = new IDType();
            id.setValue(ubigeoValue);
            /* <cac:Party><cac:PostalAddress><cbc:StreetName> */
            StreetNameType streetName = new StreetNameType();
            streetName.setValue(fiscalAddressValue);
            /* <cac:Party><cac:PostalAddress><cbc:CityName> */
            CityNameType cityName = new CityNameType();
            cityName.setValue(provinceValue);
            /* <cac:Party><cac:PostalAddress><cbc:CountrySubentity> */
            CountrySubentityType countrySubentity = new CountrySubentityType();
            countrySubentity.setValue(departmentValue);
            /* <cac:Party><cac:PostalAddress><cbc:District> */
            DistrictType district = new DistrictType();
            district.setValue(districtValue);
            /* <cac:Party><cac:PostalAddress><cac:Country> */
            CountryType country = new CountryType();
            IdentificationCodeType identificationCode = new IdentificationCodeType();
            identificationCode.setValue(countryCodeValue);
            country.setIdentificationCode(identificationCode);
            /*
             * Armar el objeto con sus respectivos TAG's
             */
            postalAddress = new AddressType();
            postalAddress.setID(id);
            postalAddress.setStreetName(streetName);
            postalAddress.setCityName(cityName);
            postalAddress.setCountrySubentity(countrySubentity);
            postalAddress.setDistrict(district);
            postalAddress.setCountry(country);
            party.setPostalAddress(postalAddress);
        }

        /* <cac:Party><cac:PartyLegalEntity> */
        PartyLegalEntityType partyLegalEntity = null;
        {
            partyLegalEntity = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(socialReasonValue);
            partyLegalEntity.setRegistrationName(registrationName);
            party.getPartyLegalEntity().add(partyLegalEntity);
        }

        /* <cac:Party><cac:Contact> */
        if (StringUtils.isNotBlank(electronicMailValue)) {
            ContactType contact = new ContactType();
            ElectronicMailType electronicMail = new ElectronicMailType();
            electronicMail.setValue(electronicMailValue);
            contact.setElectronicMail(electronicMail);
            if (StringUtils.isNotBlank(contactNameValue)) {
                NameType name = new NameType();
                name.setValue(contactNameValue);
                contact.setName(name);
            }
            party.setContact(contact);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-generateParty() [" + this.identifier + "]");
        }
        return party;
    } // generateParty

    private PartyType generatePartyV2(String socialReasonValue, String commercialNameValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateParty() [" + this.identifier + "]");
        }
        PartyType party = new PartyType();

        /* <cac:Party><cac:PartyName> */
        if (StringUtils.isNotBlank(commercialNameValue)) {
            PartyNameType partyName = new PartyNameType();
            NameType name = new NameType();
            name.setValue(commercialNameValue);
            partyName.setName(name);

            party.getPartyName().add(partyName);
        }

        /* <cac:Party><cac:PartyLegalEntity> */
        PartyLegalEntityType partyLegalEntity = null;
        {
            partyLegalEntity = new PartyLegalEntityType();

            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(socialReasonValue);
            partyLegalEntity.setRegistrationName(registrationName);

            party.getPartyLegalEntity().add(partyLegalEntity);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-generateParty() [" + this.identifier + "]");
        }
        return party;
    } // generateParty

    private PartyType generatePartyPerception(String identifier, String socialReasonValue, String commercialNameValue, String fiscalAddressValue, String departmentValue, String provinceValue, String districtValue, String ubigeoValue, String countryCodeValue, String contactNameValue, String electronicMailValue, String tipoDocId) {
        contactNameValue = retornarLleno(contactNameValue);
        electronicMailValue = retornarLleno(electronicMailValue);

        if (logger.isDebugEnabled()) {
            logger.debug("+generateParty() [" + this.identifier + "]");
        }
        PartyType party = new PartyType();

        PartyIdentificationType partyIdentification = new PartyIdentificationType();
        IDType idRUC = new IDType();
        idRUC.setValue(identifier);
        idRUC.setSchemeID(tipoDocId);
        partyIdentification.setID(idRUC);
        party.getPartyIdentification().add(partyIdentification);

        /* <cac:Party><cac:PartyName> */
        if (StringUtils.isNotBlank(commercialNameValue)) {
            PartyNameType partyName = new PartyNameType();
            NameType name = new NameType();
            name.setValue(commercialNameValue);
            partyName.setName(name);

            party.getPartyName().add(partyName);
        }

        /* <cac:Party><cac:PostalAddress> */
        if (StringUtils.isNotBlank(fiscalAddressValue)) {
            AddressType postalAddress = null;

            /* <cac:Party><cac:PostalAddress><cbc:ID> */
            IDType id = new IDType();
            id.setValue(ubigeoValue);

            /* <cac:Party><cac:PostalAddress><cbc:StreetName> */
            StreetNameType streetName = new StreetNameType();
            streetName.setValue(fiscalAddressValue);

            /* <cac:Party><cac:PostalAddress><cbc:CityName> */
            CityNameType cityName = new CityNameType();
            cityName.setValue(provinceValue);

            /* <cac:Party><cac:PostalAddress><cbc:CountrySubentity> */
            CountrySubentityType countrySubentity = new CountrySubentityType();
            countrySubentity.setValue(departmentValue);

            /* <cac:Party><cac:PostalAddress><cbc:District> */
            DistrictType district = new DistrictType();
            district.setValue(districtValue);

            /* <cac:Party><cac:PostalAddress><cac:Country> */
            CountryType country = new CountryType();
            IdentificationCodeType identificationCode = new IdentificationCodeType();
            identificationCode.setValue(countryCodeValue);
            country.setIdentificationCode(identificationCode);

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            postalAddress = new AddressType();
            postalAddress.setID(id);
            postalAddress.setStreetName(streetName);
            postalAddress.setCityName(cityName);
            postalAddress.setCountrySubentity(countrySubentity);
            postalAddress.setDistrict(district);
            postalAddress.setCountry(country);

            party.setPostalAddress(postalAddress);
        }

        /* <cac:Party><cac:PartyLegalEntity> */
        PartyLegalEntityType partyLegalEntity = null;
        {
            partyLegalEntity = new PartyLegalEntityType();

            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(socialReasonValue);
            partyLegalEntity.setRegistrationName(registrationName);

            party.getPartyLegalEntity().add(partyLegalEntity);
        }

        /* <cac:Party><cac:Contact> */
        if (StringUtils.isNotBlank(electronicMailValue)) {
            ContactType contact = new ContactType();

            ElectronicMailType electronicMail = new ElectronicMailType();
            electronicMail.setValue(electronicMailValue);
            contact.setElectronicMail(electronicMail);

            if (StringUtils.isNotBlank(contactNameValue)) {
                NameType name = new NameType();
                name.setValue(contactNameValue);
                contact.setName(name);
            }

            party.setContact(contact);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-generateParty() [" + this.identifier + "]");
        }
        return party;
    } // generateParty

    /**
     * Este metodo genera el objeto PartyType que es utilizado para contener los
     * datos del receptor electronico y armar el documento UBL.
     *
     * @param fullnameValue       Los apellidos y nombres del contribuyente receptor.
     * @param fiscalAddressValue  La direccion fiscal del contribuyente receptor.
     * @param departmentValue     El nombre del departamento del contribuyente
     *                            receptor.
     * @param provinceValue       El nombre de la provincia del contribuyente
     *                            receptor.
     * @param districtValue       El nombre del distrito del contribuyente receptor.
     * @param contactNameValue    El nombre del contacto del contribuyente
     *                            receptor.
     * @param electronicMailValue El correo electronico del contribuyente
     *                            receptor.
     * @return Retorna el objeto PartyType con los datos del contribuyente
     * emisor electronico.
     * @throws Exception
     */
    private PartyType generatePartyByBoleta(String fullnameValue, String fiscalAddressValue, String departmentValue, String provinceValue, String districtValue, String contactNameValue, String electronicMailValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("+generatePartyByBoleta() [" + this.identifier + "]");
        }
        PartyType party = new PartyType();

        /* <cac:Party><cac:PhysicalLocation> */
        if (StringUtils.isNotBlank(fiscalAddressValue)) {
            String descriptionValue = fiscalAddressValue + " - " + districtValue + " - " + provinceValue + " - " + departmentValue;
            LocationType physicalLocation = new LocationType();
            DescriptionType description = new DescriptionType();
            description.setValue(descriptionValue);
            physicalLocation.setDescription(description);

            party.setPhysicalLocation(physicalLocation);
        }

        /* <cac:Party><cac:PartyLegalEntity> */
        PartyLegalEntityType partyLegalEntity = null;
        {
            partyLegalEntity = new PartyLegalEntityType();

            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(fullnameValue);
            partyLegalEntity.setRegistrationName(registrationName);

            party.getPartyLegalEntity().add(partyLegalEntity);
        }

        /* <cac:Party><cac:Contact> */
        if (StringUtils.isNotBlank(electronicMailValue)) {
            ContactType contact = new ContactType();
            ElectronicMailType electronicMail = new ElectronicMailType();
            electronicMail.setValue(electronicMailValue);
            contact.setElectronicMail(electronicMail);

            if (StringUtils.isNotBlank(contactNameValue)) {
                NameType name = new NameType();
                name.setValue(contactNameValue);
                contact.setName(name);
            }
            party.setContact(contact);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-generatePartyByBoleta() [" + this.identifier + "]");
        }
        return party;
    } // generatePartyByBoleta

    public String retornarLleno(String palabra) {
        if (palabra.equalsIgnoreCase("")) {
            return "-";
        } else {
            return palabra;
        }

    }


    public InvoiceType generateInvoiceType(Transaccion transaction, String signerName) throws UBLDocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("+generateInvoiceType() [" + this.identifier + "]");
        }
        InvoiceType invoiceType = null;

        try {
            /* Instanciar objeto InvoiceType para la factura */
            invoiceType = new InvoiceType();

            /* Agregar <Invoice><ext:UBLExtensions> */
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] Agregando TOTALES Y PROPIEDADES.");
            }
            UBLExtensionsType ublExtensions = new UBLExtensionsType();
            {
                /*
                 * Agregar Informacion de TOTALES y PROPIEDADES
                 * <Invoice><ext:UBLExtensions
                 * ><ext:UBLExtension><ext:ExtensionContent
                 * ><sac:AdditionalInformation>
                 */
                ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty(transaction, transaction.getTransaccionTotalesList(), transaction.getTransaccionPropiedadesList(), transaction.getSUNAT_Transact()));

                /*
                 * Agregar TAG para colocar la FIRMA
                 * <Invoice><ext:UBLExtensions>
                 * <ext:UBLExtension><ext:ExtensionContent>
                 */
                ublExtensions.getUBLExtension().add(getUBLExtensionSigner());

                //

                invoiceType.setUBLExtensions(ublExtensions);
            }

            /* Agregar <Invoice><cbc:UBLVersionID> */
            invoiceType.setUBLVersionID(getUBLVersionID());

            /* Agregar <Invoice><cbc:CustomizationID> */
            invoiceType.setCustomizationID(getCustomizationID());

            /* Agregar <Invoice><cbc:ID> */
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            IDType idDocIdentifier = new IDType();
            idDocIdentifier.setValue(transaction.getDOC_Id());
            invoiceType.setID(idDocIdentifier);

            /* Agregar <Invoice><cbc:UUID> */
            invoiceType.setUUID(getUUID(this.identifier));

            /* Agregar <Invoice><cbc:IssueDate> */
            invoiceType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <Invoice><cbc:InvoiceTypeCode> */
            invoiceType.setInvoiceTypeCode(getInvoiceTypeCode(transaction.getDOC_Codigo()));

            if (null != transaction.getDOC_FechaVencimiento()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene FECHA DE VENCIMIENTO.");
                }
                /*
                 * Agregar una nota para colocar la fecha de vencimiento.
                 */
                NoteType dueDateNote = new NoteType();
                dueDateNote.setValue(getDueDateValue(transaction.getDOC_FechaVencimiento()));
                invoiceType.getNote().add(dueDateNote);
            }

            /* Agregar <Invoice><cbc:DocumentCurrencyCode> */
            invoiceType.setDocumentCurrencyCode(getDocumentCurrencyCode(transaction.getDOC_MON_Nombre(), transaction.getDOC_MON_Codigo()));

            /*
             * Agregar las guias de remision
             *
             * <Invoice><cac:DespatchDocumentReference>
             */
            if (null != transaction.getTransaccionDocrefersList() && 0 < transaction.getTransaccionDocrefersList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] Verificar si existen GUIAS DE REMISION en la FACTURA.");
                }

                invoiceType.getDespatchDocumentReference().addAll(getDespatchDocumentReferences(transaction.getTransaccionDocrefersList()));

                if (logger.isDebugEnabled()) {
                    logger.debug("generateInvoiceType() [" + this.identifier + "] Se agregaron [" + invoiceType.getDespatchDocumentReference().size() + "] guias de remision.");
                }
            }

            /*
             * Extraer la condicion de pago de ser el caso.
             */
            if (StringUtils.isNotBlank(transaction.getDOC_CondPago())) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] Extraer la CONDICION DE PAGO.");
                }
                invoiceType.getContractDocumentReference().add(getContractDocumentReference(transaction.getDOC_CondPago(), IUBLConfig.CONTRACT_DOC_REF_PAYMENT_COND_CODE));
            }

            /*
             * Extraer la orden de venta y nombre del vendedor de ser el caso
             */
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] CAMPOS PERSONALIZADOS ");
            }
            List<TransaccionContractdocref> transaccionContractdocrefList = transaction.getTransaccionContractdocrefList();
            Optional<TransaccionContractdocref> optional = transaccionContractdocrefList.parallelStream().filter(docRefer -> IUBLConfig.CONTRACT_DOC_REF_SELL_ORDER_CODE.equalsIgnoreCase(docRefer.getUsuariocampos().getNombre())).findAny();
            if (optional.isPresent()) {
                TransaccionContractdocref docRefer = optional.get();
                invoiceType.getContractDocumentReference().add(getContractDocumentReference(docRefer.getValor(), "OC"));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] SignatureType");
            }

            /* Agregar <Invoice><cac:Signature> */
            SignatureType signature = generateSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName);
            invoiceType.getSignature().add(signature);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDocIdentidad_Nro() + "] getDocIdentidadNro()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDocIdentidad_Tipo() + "] getDocIdentidadTipo()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getRazonSocial() + "] getRazonSocial(),");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getNombreComercial() + "] getNombreComercial()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Direccion() + "] getDIRDireccion()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Departamento() + "] getDIRDepartamento()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Provincia() + "] getDIRProvincia()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Distrito() + "] getDIRDistrito()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Ubigeo() + "] getDIRUbigeo()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Pais() + "] getDIRPais()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Pais() + "] getDIRPais()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getPersonContacto() + "] getPersonContacto()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getEMail() + "] getEMail()");
            }

            /* Agregar <Invoice><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = generateAccountingSupplierParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            invoiceType.setAccountingSupplierParty(accountingSupplierParty);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] CustomerPartyType");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DocIdentidad_Nro() + "] getDocIdentidadNro()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DocIdentidad_Tipo() + "] getDocIdentidadTipo()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_RazonSocial() + "] getRazonSocial(),");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_NombreComercial() + "] getNombreComercial()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DIR_Direccion() + "] getDIRDireccion()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DIR_Departamento() + "] getDIRDepartamento()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DIR_Provincia() + "] getDIRProvincia()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DIR_Distrito() + "] getDIRDistrito()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getSN_DIR_Ubigeo() + "] getDIRUbigeo()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Pais() + "] getDIRPais()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getDIR_Pais() + "] getDIRPais()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getPersonContacto() + "] getPersonContacto()");
            }

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + transaction.getEMail() + "] getEMail()");
            }

            /* Agregar <Invoice><cac:AccountingCustomerParty> */
            CustomerPartyType accountingCustomerParty = generateAccountingCustomerParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_NomCalle(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getSN_SegundoNombre(), transaction.getSN_EMail());
            invoiceType.setAccountingCustomerParty(accountingCustomerParty);

            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] PaymentType");
            }

            /*
             * Anticipos relacionados al comprobante de pago
             * <Invoice><cac:PrepaidPayment>
             */
            if (null != transaction.getANTICIPO_Monto() && transaction.getANTICIPO_Monto().compareTo(BigDecimal.ZERO) > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene informacion de ANTICIPO.");
                }
                List<PaymentType> prepaidPayment = generatePrepaidPaymentV2(transaction.getTransaccionAnticipoList());
                invoiceType.getPrepaidPayment().addAll(prepaidPayment);
            }

            /* Agregar impuestos <Invoice><cac:TaxTotal> */
            invoiceType.getTaxTotal().addAll(getAllTaxTotal(transaction.getTransaccionImpuestosList()));

            /*
             * Agregar - Importe (Total de Op.Gravada, Op.Inafecta,
             * Op.Exonerada) - Importe total de la Factura - Descuento GLOBAL de
             * la Factura - Monto total de anticipos
             */
            //SETEAR OTROS CARGOS
            invoiceType.setLegalMonetaryTotal(getMonetaryTotal(transaction.getDOC_Importe(), transaction.getDOC_MontoTotal(), transaction.getDOC_OtrosCargos(), transaction.getANTICIPO_Monto(), transaction.getDOC_MON_Codigo()));

            /* Agregar items <Invoice><cac:InvoiceLine> */
            invoiceType.getInvoiceLine().addAll(getAllInvoiceLines(transaction.getTransaccionLineasList(), transaction.getDOC_MON_Codigo()));

        } catch (UBLDocumentException e) {
            logger.error("generateInvoiceType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoiceType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_341, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoiceType() [" + this.identifier + "]");
        }
        return invoiceType;
    } // generateInvoiceType


    public UBLExtensionType getUBLExtensionSigner() {
        UBLExtensionType ublExtensionSigner = new UBLExtensionType();
        ExtensionContentType extensionContent = new ExtensionContentType();
        ublExtensionSigner.setExtensionContent(extensionContent);

        return ublExtensionSigner;
    } // getUBLExtensionSigner

    private UBLVersionIDType getUBLVersionID() {
        UBLVersionIDType ublVersionID = new UBLVersionIDType();
        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_2_0);

        return ublVersionID;
    }

    private UBLVersionIDType getUBLVersionID12_0() {
        UBLVersionIDType ublVersionID = new UBLVersionIDType();
        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_2_1);

        return ublVersionID;
    }

//    private UBLVersionIDType getUBLVersionID1_0() {
//        UBLVersionIDType ublVersionID = new UBLVersionIDType();
//        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_1_0);
//
//        return ublVersionID;
//    }

    private CustomizationIDType getCustomizationID() {
        CustomizationIDType customizationID = new CustomizationIDType();
        customizationID.setValue(IUBLConfig.CUSTOMIZATION_ID_1_0);

        return customizationID;
    } // getCustomizationID

    private UBLExtensionType getUBLExtensionTotalAndProperty(Transaccion transaccion, List<TransaccionTotales> transactionTotalList, List<TransaccionPropiedades> transactionPropertyList, String sunatTransactionID) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getUBLExtensionTotalAndProperty() [" + this.identifier + "] transactionTotalList: " + transactionTotalList + " transactionPropertyList: " + transactionPropertyList + " sunatTransactionID: " + sunatTransactionID);
        }
        UBLExtensionType ublExtension = null;

        try {
            ublExtension = new UBLExtensionType();

            AdditionalInformationType additionalInformation = new AdditionalInformationType();

            if (null == transactionTotalList) {
                throw new UBLDocumentException(IVenturaError.ERROR_330);
            } else {
                if (StringUtils.isNotBlank(sunatTransactionID)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Existe valor SUNATTransaction.");
                    }
                    SUNATTransactionType sunatTransaction = new SUNATTransactionType();
                    IDType id = new IDType();
                    id.setValue(sunatTransactionID.trim());
                    sunatTransaction.setID(id);
                    additionalInformation.setSUNATTransaction(sunatTransaction);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Agregando informacion de TOTALES.");
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("getUBLExtensionTotalAndProperty() Se encontró un total de  [" + transactionTotalList.size() + "] Agregando informacion de TOTALES.");
                }

                for (TransaccionTotales transactionTotal : transactionTotalList) {
                    AdditionalMonetaryTotalType additionalMonetaryTotal = new AdditionalMonetaryTotalType();

                    if (logger.isDebugEnabled()) {
                        logger.debug("getUBLExtensionTotalAndProperty() [" + transactionTotal.getMonto() + "-" + transactionTotal.getTransaccionTotalesPK().getId() + "] Agregando informacion de TOTALES.");
                    }

                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation
                     * ><sac:AdditionalMonetaryTotal><cbc:ID>
                     */
                    IDType id = new IDType();
                    id.setValue(transactionTotal.getTransaccionTotalesPK().getId());

                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation
                     * ><sac:AdditionalMonetaryTotal><cbc:PayableAmount>
                     */
                    PayableAmountType payableAmount = new PayableAmountType();
                    payableAmount.setValue(transactionTotal.getMonto().setScale(IUBLConfig.DECIMAL_ADDITIONAL_MONETARY_TOTAL_PAYABLE_AMOUNT, RoundingMode.HALF_UP));
                    payableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transactionTotal.getTransaccion().getDOC_MON_Codigo()).value());

                    /* Agregar ID y PayableAmount al objeto */
                    additionalMonetaryTotal.setID(id);
                    additionalMonetaryTotal.setPayableAmount(payableAmount);

                    /* Agregar el objeto AdditionalMonetaryTotalType a la lista */
                    additionalInformation.getAdditionalMonetaryTotal().add(additionalMonetaryTotal);
                }


            }

            if (null == transactionPropertyList) {
                throw new UBLDocumentException(IVenturaError.ERROR_331);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Agregando informacion de PROPIEDADES.");
                }
                for (TransaccionPropiedades transactionProperty : transactionPropertyList) {
                    AdditionalPropertyType additionalProperty = new AdditionalPropertyType();

                    int cadena1en = transactionProperty.getValor().length();
                    if (" ".equals(transactionProperty.getValor().substring(0, 1))) {
                        transactionProperty.setValor(transactionProperty.getValor().substring(1, cadena1en));
                    }
                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:ID>
                     */
                    IDType id = new IDType();
                    id.setValue(transactionProperty.getTransaccionPropiedadesPK().getId());

                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:Value>
                     */
                    ValueType value = new ValueType();
                    value.setValue(transactionProperty.getValor());

                    /* Agregar ID y Value al objeto */
                    additionalProperty.setID(id);
                    additionalProperty.setValue(value);

                    /* Agregar el objeto AdditionalPropertyType a la lista */
                    additionalInformation.getAdditionalProperty().add(additionalProperty);
                }
            }

            if (transaccion.getTransaccionContractdocrefList() != null && !transaccion.getTransaccionContractdocrefList().isEmpty()) {
                Optional<TransaccionContractdocref> optional = transaccion.getTransaccionContractdocrefList()
                        .parallelStream().filter(docRefer -> IUBLConfig.CONTRACT_DOC_REF_SELL_ORDER_INCO.equalsIgnoreCase(docRefer.getUsuariocampos().getNombre())).findAny();
                if (optional.isPresent()) {
                    AdditionalPropertyType additionalProperty = new AdditionalPropertyType();
                    TransaccionContractdocref docRefer = optional.get();
                    /***/
                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:ID>
                     */
                    IDType id = new IDType();
                    id.setValue("7003");

                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:Value>
                     */
                    ValueType value = new ValueType();
                    value.setValue(docRefer.getValor());

                    /* Agregar ID y Value al objeto */
                    additionalProperty.setID(id);
                    additionalProperty.setValue(value);

                    /* Agregar el objeto AdditionalPropertyType a la lista */
                    additionalInformation.getAdditionalProperty().add(additionalProperty);
                    //invoiceType.getContractDocumentReference().add(getContractDocumentReference(docRefer.getValor(), "OC"));
                }
            }


            /* Colocar la informacion en el TAG UBLExtension */
            /*ExtensionContentType extensionContent = new ExtensionContentType();
            extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));
            ublExtension.setExtensionContent(extensionContent);*/

            /* Colocar la informacion en el TAG UBLExtension */
            ExtensionContentType extensionContent = new ExtensionContentType();
//extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));

            boolean texto = false;
            for (int i = 0; i < transaccion.getTransaccionContractdocrefList().size(); i++) {
                if ("texto_amplio_certimin".equals(transaccion.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                    TransaccionContractdocref objecto = transaccion.getTransaccionContractdocrefList().get(i);
                    if (objecto != null && !objecto.getValor().isEmpty()) {
                        NoteType noteType = new NoteType();
                        noteType.setValue(objecto.getValor());
                        extensionContent.setNote(noteType);
                        texto = true;
                        break;
                    }
                }
            }

            if (!texto) {
                extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));
            }
            ublExtension.setExtensionContent(extensionContent);
        } catch (UBLDocumentException e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_328.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_328);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getUBLExtensionTotalAndProperty() [" + this.identifier + "]");
        }
        return ublExtension;
    } // getUBLExtensionTotalAndProperty

    private UBLExtensionType getUBLExtensionTotalAndPropertyForNCND(List<TransaccionTotales> transactionTotalList, List<TransaccionPropiedades> transactionPropertyList, Transaccion transaccion) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getUBLExtensionTotalAndProperty() [" + this.identifier + "] transactionTotalList: " + transactionTotalList + " transactionPropertyList: " + transactionPropertyList + " sunatTransactionID: " + transaccion.getSUNAT_Transact());
        }
        UBLExtensionType ublExtension = null;
        try {
            ublExtension = new UBLExtensionType();
            AdditionalInformationType additionalInformation = new AdditionalInformationType();
            if (null == transactionTotalList) {
                throw new UBLDocumentException(IVenturaError.ERROR_330);
            } else {
                if (StringUtils.isNotBlank(transaccion.getSUNAT_Transact())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Existe valor SUNATTransaction.");
                    }
                    SUNATTransactionType sunatTransaction = new SUNATTransactionType();
                    IDType id = new IDType();
                    id.setValue(transaccion.getSUNAT_Transact().trim());
                    sunatTransaction.setID(id);
                    additionalInformation.setSUNATTransaction(sunatTransaction);
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Agregando informacion de TOTALES.");
                }

                for (TransaccionTotales transactionTotal : transactionTotalList) {
                    AdditionalMonetaryTotalType additionalMonetaryTotal = new AdditionalMonetaryTotalType();
                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation
                     * ><sac:AdditionalMonetaryTotal><cbc:ID>
                     */
                    if (!transactionTotal.getTransaccionTotalesPK().getId().equalsIgnoreCase(IUBLConfig.ADDITIONAL_MONETARY_1005)) {
                        IDType id = new IDType();
                        id.setValue(transactionTotal.getTransaccionTotalesPK().getId());
                        /*
                         * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                         * AdditionalInformation
                         * ><sac:AdditionalMonetaryTotal><cbc:PayableAmount>
                         */
                        PayableAmountType payableAmount = new PayableAmountType();
                        payableAmount.setValue(transactionTotal.getMonto().setScale(IUBLConfig.DECIMAL_ADDITIONAL_MONETARY_TOTAL_PAYABLE_AMOUNT, RoundingMode.HALF_UP));
                        payableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transactionTotal.getTransaccion().getDOC_MON_Codigo()).value());
                        /* Agregar ID y PayableAmount al objeto */
                        additionalMonetaryTotal.setID(id);
                        additionalMonetaryTotal.setPayableAmount(payableAmount);
                        /* Agregar el objeto AdditionalMonetaryTotalType a la lista */
                        additionalInformation.getAdditionalMonetaryTotal().add(additionalMonetaryTotal);
                    }
                }
            }
            if (null == transactionPropertyList) {
                throw new UBLDocumentException(IVenturaError.ERROR_331);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("getUBLExtensionTotalAndProperty() [" + this.identifier + "] Agregando informacion de PROPIEDADES.");
                }
                for (TransaccionPropiedades transactionProperty : transactionPropertyList) {
                    AdditionalPropertyType additionalProperty = new AdditionalPropertyType();
                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:ID>
                     */
                    IDType id = new IDType();
                    id.setValue(transactionProperty.getTransaccionPropiedadesPK().getId());
                    /*
                     * Agregar <ext:UBLExtension><ext:ExtensionContent><sac:
                     * AdditionalInformation><sac:AdditionalProperty><cbc:Value>
                     */
                    ValueType value = new ValueType();
                    value.setValue(transactionProperty.getValor());
                    /* Agregar ID y Value al objeto */
                    additionalProperty.setID(id);
                    additionalProperty.setValue(value);
                    /* Agregar el objeto AdditionalPropertyType a la lista */
                    additionalInformation.getAdditionalProperty().add(additionalProperty);
                }
            }
            /* Colocar la informacion en el TAG UBLExtension */
            /*ExtensionContentType extensionContent = new ExtensionContentType();
            extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));
            ublExtension.setExtensionContent(extensionContent);*/


            /* Colocar la informacion en el TAG UBLExtension */
            ExtensionContentType extensionContent = new ExtensionContentType();
//extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));

            boolean texto = false;
            for (int i = 0; i < transaccion.getTransaccionContractdocrefList().size(); i++) {
                if ("texto_amplio_certimin".equals(transaccion.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                    TransaccionContractdocref objecto = transaccion.getTransaccionContractdocrefList().get(i);
                    if (objecto != null && !objecto.getValor().isEmpty()) {
                        NoteType noteType = new NoteType();
                        noteType.setValue(objecto.getValor());
                        extensionContent.setNote(noteType);
                        texto = true;
                        break;
                    }
                }
            }

            if (!texto) {
                extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));
            }

            ublExtension.setExtensionContent(extensionContent);
        } catch (UBLDocumentException e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_328.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_328);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getUBLExtensionTotalAndProperty() [" + this.identifier + "]");
        }
        return ublExtension;
    } // getUBLExtensionTotalAndProperty

    private GrossWeightMeasureType returnGrossWeightMeasureType(String peso, String unidad) {
        GrossWeightMeasureType gvmt = new GrossWeightMeasureType();
        gvmt.setUnitCode(unidad);
        BigDecimal bd = new BigDecimal(peso);
        gvmt.setValue(bd);
        return gvmt;
    }

    private DriverPartyType returnDriverPartyType(String identificacion) {
        DriverPartyType dpt = new DriverPartyType();
        PartyType pt = new PartyType();
        PartyIdentificationType pit = new PartyIdentificationType();
        IDType idt = new IDType();
        idt.setValue(identificacion);
        pit.setID(idt);
        pt.getPartyIdentification().add(pit);
        dpt.setParty(pt);
        return dpt;
    }

    private TransportModeCodeType returnTransportModeCodeType(String value) {
        TransportModeCodeType tmct = new TransportModeCodeType();
        tmct.setValue(value);
        return tmct;
    }

    private SUNATRoadTransportType returnSUNATRoadTransportType(String licencia, String autorizacion, String marca) {
        SUNATRoadTransportType sunatrtt = new SUNATRoadTransportType();
        LicensePlateIDType lpidt = new LicensePlateIDType();
        lpidt.setValue(licencia);
        TransportAuthorizationCodeType tact = new TransportAuthorizationCodeType();
        tact.setValue(autorizacion);
        BrandNameType bnt = new BrandNameType();
        bnt.setValue(marca);
        sunatrtt.setLicensePlateID(lpidt);
        sunatrtt.setTransportAuthorizationCode(tact);
        sunatrtt.setBrandName(bnt);
        return sunatrtt;
    }

    private SUNATCarrierPartyType returnSUNATCarrierPartyType(String documIden, String documentTipoId, String name) {
        SUNATCarrierPartyType sunatcpt = new SUNATCarrierPartyType();
        CustomerAssignedAccountIDType caaidt = new CustomerAssignedAccountIDType();
        caaidt.setValue(documIden);
        AdditionalAccountIDType aaidt = new AdditionalAccountIDType();
        aaidt.setValue(documentTipoId);
        PartyLegalEntityType plet = new PartyLegalEntityType();
        RegistrationNameType rnt = new RegistrationNameType();
        rnt.setValue(name);
        plet.setRegistrationName(rnt);
        PartyType pt = new PartyType();
        pt.getPartyLegalEntity().add(plet);
        sunatcpt.setCustomerAssignedAccountID(caaidt);
        sunatcpt.getAdditionalAccountID().add(aaidt);
        sunatcpt.setParty(pt);
        return sunatcpt;
    }

    private AddressType returnAddressType(String id, String streetName, String distrito, String provincia, String departamento, String pais) {
        AddressType at = new AddressType();
        IDType idx = new IDType();
        idx.setValue(id);
        StreetNameType streetNameType = new StreetNameType();
        streetNameType.setValue(streetName);
        CitySubdivisionNameType csnt = new CitySubdivisionNameType();
        csnt.setValue("Urb. Los Laureles");
        CityNameType cnt = new CityNameType();
        cnt.setValue(provincia);
        CountrySubentityType cst = new CountrySubentityType();
        cst.setValue(departamento);
        DistrictType dt = new DistrictType();
        dt.setValue(distrito);
        IdentificationCodeType ict = new IdentificationCodeType();
        ict.setValue(pais);
        CountryType ct = new CountryType();
        ct.setIdentificationCode(ict);
        at.setID(idx);
        at.setStreetName(streetNameType);
        at.setCitySubdivisionName(csnt);
        at.setCityName(cnt);
        at.setCountrySubentity(cst);
        at.setDistrict(dt);
        at.setCountry(ct);
        return at;
    }

    private org.w3c.dom.Node getExtensionContentNode(Object additionalInformationObj) throws UBLDocumentException {
        org.w3c.dom.Node node = null;
        try {
            DocumentBuilderFactory docBuilderFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFact.newDocumentBuilder();
            Document document = docBuilder.newDocument();
            /* Generando proceso JAXB */
            JAXBContext jaxbContext = JAXBContext.newInstance(additionalInformationObj.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(additionalInformationObj, document);
            /* Extrayendo el NODO */
            node = document.getFirstChild();
        } catch (Exception e) {
            logger.error("getExtensionContentNode() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_329.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_329);
        }
        return node;
    } // getExtensionContentNode

    private UUIDType getUUID(String docUUID) {
        UUIDType uuid = new UUIDType();
        uuid.setValue(docUUID);

        return uuid;
    } // getUUID

    private InvoiceTypeCodeType getInvoiceTypeCode(String value) {
        InvoiceTypeCodeType invoiceTypeCode = new InvoiceTypeCodeType();
        invoiceTypeCode.setValue(value);

        return invoiceTypeCode;
    } // getInvoiceTypeCode

    private DespatchAdviceTypeCodeType getDespatchAdviceTypeCode(String value) {
        DespatchAdviceTypeCodeType despatchAdviceTypeCodeType = new DespatchAdviceTypeCodeType();
        despatchAdviceTypeCodeType.setValue(value);

        return despatchAdviceTypeCodeType;
    } // getInvoiceTypeCode

    private DocumentCurrencyCodeType getDocumentCurrencyCode(String currencyName, String currencyCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getDocumentCurrencyCode() currencyName: " + currencyName + " currencyCode: " + currencyCode);
        }
        DocumentCurrencyCodeType documentCurrencyCode = new DocumentCurrencyCodeType();
        documentCurrencyCode.setName(currencyName);
        documentCurrencyCode.setValue(currencyCode);

        return documentCurrencyCode;
    } // getDocumentCurrencyCode

    private IssueDateType getIssueDate(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        IssueDateType issueDateType = null;

        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new IssueDateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private IssueDateType getIssueDate8(String issueDateValue_1) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date issueDateValue = null;
        try {
            issueDateValue = formatter.parse(issueDateValue_1);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        IssueDateType issueDateType = null;
        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);
            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new IssueDateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private DateType getIssueDate4(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        DateType issueDateType = null;
        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);
            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new DateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private PaidDateType getIssueDate2(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        PaidDateType issueDateType = null;

        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);
            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new PaidDateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private SUNATPerceptionDateType getIssueDate3(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        SUNATPerceptionDateType issueDateType = null;

        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new SUNATPerceptionDateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private SUNATRetentionDateType getIssueDate5(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }
        SUNATRetentionDateType issueDateType = null;

        try {
            if (null == issueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_312);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String date = sdf.format(issueDateValue);

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            issueDateType = new SUNATRetentionDateType();
            issueDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getIssueDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDateType;
    } // getIssueDate

    private ReferenceDateType getReferenceDate2(String referenceDateValue_1) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getReferenceDate() [" + this.identifier + "]");
        }

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date referenceDateValue = null;
        try {
            referenceDateValue = formatter.parse(referenceDateValue_1);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        ReferenceDateType referenceDateType = null;

        try {
            if (null == referenceDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_340);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.REFERENCEDATE_FORMAT);
            String date = sdf.format(referenceDateValue);

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            referenceDateType = new ReferenceDateType();
            referenceDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getReferenceDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getReferenceDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_347);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getReferenceDate() [" + this.identifier + "]");
        }
        return referenceDateType;
    } // getReferenceDate

    private ReferenceDateType getReferenceDate(Date referenceDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getReferenceDate() [" + this.identifier + "]");
        }

        ReferenceDateType referenceDateType = null;

        try {
            if (null == referenceDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_340);
            }

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.REFERENCEDATE_FORMAT);
            String date = sdf.format(referenceDateValue);

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();

            /* Agregando la fecha de emision <cbc:IssueDate> */
            referenceDateType = new ReferenceDateType();
            referenceDateType.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (UBLDocumentException e) {
            logger.error("getReferenceDate() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getReferenceDate() [" + this.identifier + "] " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_347);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getReferenceDate() [" + this.identifier + "]");
        }
        return referenceDateType;
    } // getReferenceDate

    private String getDueDateValue(Date dueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDueDateValue() [" + this.identifier + "]");
        }
        String dueDate = null;
        try {
            if (null == dueDateValue) {
                throw new UBLDocumentException(IVenturaError.ERROR_314);
            }
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.DUEDATE_FORMAT);
            dueDate = sdf.format(dueDateValue);
        } catch (UBLDocumentException e) {
            logger.error("getDueDateValue() UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getDueDateValue() [" + this.identifier + "] " + IVenturaError.ERROR_315.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_315);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDueDateValue() [" + this.identifier + "]");
        }
        return dueDate;
    } // getDueDateValue

    private List<PaymentType> generatePrepaidPaymentV2(List<TransaccionAnticipo> lstAnticipo) throws UBLDocumentException {
        List<PaymentType> prepaidPayment_2 = new ArrayList<PaymentType>();
        if (lstAnticipo != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("+generatePrepaidPayment() [" + this.identifier + "]");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("+generatePrepaidPayment() [" + lstAnticipo.size() + "]");
            }
            for (int i = 0; i < lstAnticipo.size(); i++) {
                PaymentType prepaidPayment = null;
                try {
                    /* Agregar <Invoice><cac:PrepaidPayment><cbc:PaidAmount> */
                    PaidAmountType paidAmount = new PaidAmountType();
                    paidAmount.setValue(lstAnticipo.get(i).getAnticipoMonto().setScale(IUBLConfig.DECIMAL_PREPAIDPAYMENT_PAIDAMOUNT, RoundingMode.HALF_UP));
                    paidAmount.setCurrencyID(CurrencyCodeContentType.valueOf(lstAnticipo.get(i).getDOCMoneda()).value());

                    if (logger.isDebugEnabled()) {
                        logger.debug("+generatePrepaidPayment() [" + lstAnticipo.get(i).getAnticipoMonto() + lstAnticipo.get(i).getDOCMoneda() + "]");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("+cac:PrepaidPayment() [" + this.identifier + "]");
                    }

                    /* Agregar <Invoice><cac:PrepaidPayment><cbc:ID> */
                    IDType id = new IDType();
                    id.setValue(lstAnticipo.get(i).getAntiDOCSerieCorrelativo());
                    id.setSchemeID(lstAnticipo.get(i).getAntiDOCTipo());

                    if (logger.isDebugEnabled()) {
                        logger.debug("+generatePrepaidPayment() [" + lstAnticipo.get(i).getAntiDOCSerieCorrelativo() + lstAnticipo.get(i).getAntiDOCTipo() + "]");
                    }

                    /* Agregar <Invoice><cac:PrepaidPayment><cbc:InstructionID> */
                    InstructionIDType instructionID = new InstructionIDType();
                    instructionID.setValue(lstAnticipo.get(i).getDOCNumero());
                    instructionID.setSchemeID(lstAnticipo.get(i).getDOCTipo());

                    if (logger.isDebugEnabled()) {
                        logger.debug("+generatePrepaidPayment() [" + lstAnticipo.get(i).getDOCNumero() + lstAnticipo.get(i).getDOCTipo() + "]");
                    }
                    /*
                     * Agregar los tag's
                     */
                    prepaidPayment = new PaymentType();
                    prepaidPayment.setPaidAmount(paidAmount);
                    prepaidPayment.setID(id);
                    prepaidPayment.setInstructionID(instructionID);
                    prepaidPayment_2.add(prepaidPayment);
                } catch (Exception e) {
                    logger.error("generatePrepaidPayment() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
                    throw new UBLDocumentException(IVenturaError.ERROR_336);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("-generatePrepaidPayment() [" + this.identifier + "]");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("-Genero correctamente el tag<cac:PrepaidPayment>  [" + this.identifier + "]");
                }
            }
        }
        return prepaidPayment_2;
    } // generatePrepaidPayment

    private List<TaxTotalType> getAllTaxTotal(List<TransaccionImpuestos> taxTotalTransactions) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllTaxTotal() [" + this.identifier + "] taxTotalTransactions: " + taxTotalTransactions);
        }
        List<TaxTotalType> taxTotalList = new ArrayList<>();
        if (null != taxTotalTransactions && !taxTotalTransactions.isEmpty()) {
            try {
                for (TransaccionImpuestos taxTotalTrans : taxTotalTransactions) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllTaxTotal() [" + this.identifier + "] " + "\nTipoTributo: " + taxTotalTrans.getTipoTributo() + "\tMonto: " + taxTotalTrans.getMonto() + "\tPorcentaje: " + taxTotalTrans.getPorcentaje() + "\nMoneda: " + taxTotalTrans.getMoneda() + "\nTierRange: " + taxTotalTrans.getTierRange() + "\tTransaccion: " + taxTotalTrans.getTransaccion());
                    }
                    TaxTotalType taxTotalType = new TaxTotalType();
                    /*
                     * Creando TaxAmountType
                     * <Invoice><cac:TaxTotal><cbc:TaxAmount>
                     * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount>
                     */
                    TaxAmountType taxAmountType = new TaxAmountType();
                    taxAmountType.setValue(taxTotalTrans.getMonto().setScale(IUBLConfig.DECIMAL_TAX_TOTAL_AMOUNT, RoundingMode.HALF_UP));
                    taxAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(taxTotalTrans.getMoneda()).value());
                    taxTotalType.setTaxAmount(taxAmountType);
                    if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_IGV_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /* <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:Percent> */
                        PercentType percentType = new PercentType();
                        percentType.setValue(taxTotalTrans.getPorcentaje().setScale(IUBLConfig.DECIMAL_TAX_TOTAL_IGV_PERCENT, RoundingMode.HALF_UP));
                        taxSubtotalType.setPercent(percentType);
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_IGV_NAME, IUBLConfig.TAX_TOTAL_IGV_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_ISC_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_ISC_NAME, IUBLConfig.TAX_TOTAL_ISC_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXP_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_EXP_NAME, IUBLConfig.TAX_TOTAL_EXP_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_GRA_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);

                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_GRA_NAME, IUBLConfig.TAX_TOTAL_GRA_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);

                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXO_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);

                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_EXO_NAME, IUBLConfig.TAX_TOTAL_EXO_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);

                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_INA_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_INA_NAME, IUBLConfig.TAX_TOTAL_INA_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTrans.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_OTR_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount
                         * >
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory
                         * >
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * <Invoice><cac:TaxTotal><cac:TaxSubtotal><cac:
                         * TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTrans.getTipoTributo(), IUBLConfig.TAX_TOTAL_OTR_NAME, IUBLConfig.TAX_TOTAL_OTR_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else {
                        logger.error("getAllTaxTotal() [" + this.identifier + "] " + IVenturaError.ERROR_318.getMessage());
                        throw new UBLDocumentException(IVenturaError.ERROR_318);
                    }
                    /* Agregando el impuesto a la lista */
                    taxTotalList.add(taxTotalType);
                }
            } catch (UBLDocumentException e) {
                logger.error("getAllTaxTotal() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllTaxTotal() [" + this.identifier + "] " + IVenturaError.ERROR_317.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_317);
            }
        } else {
            logger.error("getAllTaxTotal() [" + this.identifier + "] " + IVenturaError.ERROR_316.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_316);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllTaxTotal() [" + this.identifier + "]");
        }
        return taxTotalList;
    } // getAllTaxTotal

    private List<TaxTotalType> getAllTaxTotalLines(List<TransaccionLineaImpuestos> taxTotalTransactionLines) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllTaxTotalLines() [" + this.identifier + "]");
        }
        List<TaxTotalType> taxTotalLineList = new ArrayList<>();
        if (null != taxTotalTransactionLines && !taxTotalTransactionLines.isEmpty()) {
            try {
                for (TransaccionLineaImpuestos taxTotalTransLine : taxTotalTransactionLines) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllTaxTotalLines() [" + this.identifier + "] LineId: " + taxTotalTransLine.getTransaccionLineaImpuestosPK().getLineId() + " NroOrden: " + taxTotalTransLine.getTransaccionLineaImpuestosPK().getNroOrden() + "\nTipoTributo: " + taxTotalTransLine.getTipoTributo() + "\tMonto: " + taxTotalTransLine.getMonto() + "\tPorcentaje: " + taxTotalTransLine.getPorcentaje() + "\nMoneda: " + taxTotalTransLine.getMoneda() + "\nTierRange: " + taxTotalTransLine.getTierRange() + "\tTransaccionLineas: " + taxTotalTransLine.getTransaccionLineas());
                    }

                    TaxTotalType taxTotalType = new TaxTotalType();

                    /*
                     * Creando TaxAmountType
                     * <Invoice><cac:InvoiceLine><cac:TaxTotal><cbc:TaxAmount>
                     * <Invoice
                     * ><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal><
                     * cbc:TaxAmount>
                     */
                    TaxAmountType taxAmountType = new TaxAmountType();
                    taxAmountType.setValue(taxTotalTransLine.getMonto().setScale(IUBLConfig.DECIMAL_LINE_TAX_AMOUNT, RoundingMode.HALF_UP));
                    taxAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(taxTotalTransLine.getMoneda()).value());
                    taxTotalType.setTaxAmount(taxAmountType);
                    if (taxTotalTransLine.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_IGV_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cbc:TaxAmount>
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /* Agregar <Invoice><cac:InvoiceLine> */
                        PercentType percent = new PercentType();
                        percent.setValue(taxTotalTransLine.getPorcentaje().setScale(IUBLConfig.DECIMAL_LINE_TAX_IGV_PERCENT, RoundingMode.HALF_UP));
                        taxSubtotalType.setPercent(percent);
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cac:TaxCategory>
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac
                         * :TaxSubtotal
                         * ><cac:TaxCategory><cbc:TaxExemptionReasonCode>
                         */
                        TaxExemptionReasonCodeType taxExemptionReasonCode = new TaxExemptionReasonCodeType();
                        taxExemptionReasonCode.setValue(taxTotalTransLine.getTipoAfectacion());
                        taxCategoryType.setTaxExemptionReasonCode(taxExemptionReasonCode);
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac
                         * :TaxSubtotal><cac:TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTransLine.getTipoTributo(), IUBLConfig.TAX_TOTAL_IGV_NAME, IUBLConfig.TAX_TOTAL_IGV_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTransLine.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_ISC_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cbc:TaxAmount>
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cac:TaxCategory>
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac
                         * :TaxSubtotal><cac:TaxCategory><cbc:TierRange>
                         */
                        TierRangeType tierRange = new TierRangeType();
                        tierRange.setValue(taxTotalTransLine.getTierRange());
                        taxCategoryType.setTierRange(tierRange);

                        /*
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:
                         * TaxSubtotal><cac:TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTransLine.getTipoTributo(), IUBLConfig.TAX_TOTAL_ISC_NAME, IUBLConfig.TAX_TOTAL_ISC_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);

                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else if (taxTotalTransLine.getTipoTributo().equalsIgnoreCase(IUBLConfig.TAX_TOTAL_OTR_ID)) {
                        TaxSubtotalType taxSubtotalType = new TaxSubtotalType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cbc:TaxAmount>
                         */
                        taxSubtotalType.setTaxAmount(taxAmountType);
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac:TaxSubtotal
                         * ><cac:TaxCategory>
                         */
                        TaxCategoryType taxCategoryType = new TaxCategoryType();
                        /*
                         * Agregar
                         * <Invoice><cac:InvoiceLine><cac:TaxTotal><cac
                         * :TaxSubtotal><cac:TaxCategory><cac:TaxScheme>
                         */
                        taxCategoryType.setTaxScheme(getTaxScheme(taxTotalTransLine.getTipoTributo(), IUBLConfig.TAX_TOTAL_OTR_NAME, IUBLConfig.TAX_TOTAL_OTR_CODE));
                        taxSubtotalType.setTaxCategory(taxCategoryType);
                        /* Agregando TaxSubtotal de tipo IGV */
                        taxTotalType.getTaxSubtotal().add(taxSubtotalType);
                    } else {
                        logger.error("getAllTaxTotal() [" + this.identifier + "] " + IVenturaError.ERROR_318.getMessage());
                        throw new UBLDocumentException(IVenturaError.ERROR_324);
                    }

                    /* Agregando el impuesto de item a la lista */
                    taxTotalLineList.add(taxTotalType);
                }
            } catch (UBLDocumentException e) {
                logger.error("getAllTaxTotalLines() UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllTaxTotalLines() [" + this.identifier + "] " + IVenturaError.ERROR_323.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_323);
            }
        } else {
            logger.error("getAllTaxTotalLines() [" + this.identifier + "] " + IVenturaError.ERROR_322.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_322);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllTaxTotalLines() [" + this.identifier + "]");
        }
        return taxTotalLineList;
    } // getAllTaxTotalLines

    private TaxSchemeType getTaxScheme(String taxTotalID, String taxTotalName, String taxTotalCode) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTaxScheme() taxTotalID: " + taxTotalID + " taxTotalName: " + taxTotalName + " taxTotalCode: " + taxTotalCode);
        }
        TaxSchemeType taxScheme = new TaxSchemeType();
        IDType id = new IDType();
        id.setValue(taxTotalID);
        NameType name = new NameType();
        name.setValue(taxTotalName);
        TaxTypeCodeType taxTypeCode = new TaxTypeCodeType();
        taxTypeCode.setValue(taxTotalCode);
        /* Agregando los tag's */
        taxScheme.setID(id);
        taxScheme.setName(name);
        taxScheme.setTaxTypeCode(taxTypeCode);
        return taxScheme;
    } // getTaxScheme

    private MonetaryTotalType getMonetaryTotal(BigDecimal amountValue, BigDecimal payableAmountVal, BigDecimal chargeAmountVal, BigDecimal prepaidTotalAmount, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getLegalMonetaryTotal() [" + this.identifier + "]");
        }
        MonetaryTotalType legalMonetaryTotal = null;
        if (null != amountValue) {
            legalMonetaryTotal = new MonetaryTotalType();
            LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
            lineExtensionAmount.setValue(amountValue.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
            lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            /* Agregar el IMPORTE */
            legalMonetaryTotal.setLineExtensionAmount(lineExtensionAmount);
        } else {
            throw new UBLDocumentException(IVenturaError.ERROR_335);
        }
        if (null != payableAmountVal) {
            PayableAmountType payableAmount = new PayableAmountType();
            payableAmount.setValue(payableAmountVal.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_PAYABLEAMOUNT, RoundingMode.HALF_UP));
            payableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            /* Agregar el IMPORTE TOTAL */
            legalMonetaryTotal.setPayableAmount(payableAmount);
        } else {
            throw new UBLDocumentException(IVenturaError.ERROR_321);
        }
        /* Agregar LOS OTROS CARGOS */
        if (chargeAmountVal.compareTo(BigDecimal.ZERO) == 1) {
            ChargeTotalAmountType chargeAmount = new ChargeTotalAmountType();
            chargeAmount.setValue(chargeAmountVal.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_CHARGETOTALAMOUNT, RoundingMode.HALF_UP));
            chargeAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            legalMonetaryTotal.setChargeTotalAmount(chargeAmount);
        }
        if (null != prepaidTotalAmount && prepaidTotalAmount.compareTo(BigDecimal.ZERO) == 1) {
            if (logger.isInfoEnabled()) {
                logger.info("getLegalMonetaryTotal() [" + this.identifier + "] La transaccion tiene un TOTAL DE ANTICIPOS.");
            }
            PrepaidAmountType prepaidAmount = new PrepaidAmountType();
            prepaidAmount.setValue(prepaidTotalAmount.setScale(IUBLConfig.DECIMAL_MONETARYTOTAL_PREPAIDAMOUNT, RoundingMode.HALF_UP));
            prepaidAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            /* Agregar el TOTAL DE ANTICIPOS */
            legalMonetaryTotal.setPrepaidAmount(prepaidAmount);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getLegalMonetaryTotal() [" + this.identifier + "]");
        }
        return legalMonetaryTotal;
    } // getLegalMonetaryTotal

    private List<InvoiceLineType> getAllInvoiceLines(List<TransaccionLineas> transactionLines, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllInvoiceLines() [" + this.identifier + "] transactionLines: " + transactionLines + " currencyCode: " + currencyCode);
        }
        List<InvoiceLineType> invoiceLineList = null;
        if (null != transactionLines && !transactionLines.isEmpty()) {
            try {
                invoiceLineList = new ArrayList<>(transactionLines.size());

                for (TransaccionLineas transLine : transactionLines) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Extrayendo informacion del item...");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] NroOrden: " + transLine.getTransaccionLineasPK().getNroOrden() + "\nCantidad: " + transLine.getCantidad() + "\tUnidad: " + transLine.getUnidad() + "\tUnidadSunat: " + transLine.getUnidadSunat() + "\tTotalLineaSinIGV: " + transLine.getTotalLineaSinIGV() + "\nPrecioRefCodigo: " + transLine.getPrecioRefCodigo() + "\tPrecioIGV: " + transLine.getPrecioIGV() + "\tPrecioRefMonto: " + transLine.getPrecioRefMonto() + "\nDCTOMonto: " + transLine.getDSCTOMonto() + "\tDescripcion: " + transLine.getDescripcion() + "\tCodArticulo: " + transLine.getCodArticulo());
                    }
                    InvoiceLineType invoiceLine = new InvoiceLineType();
                    /* Agregar <Invoice><cac:InvoiceLine><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando el ID.");
                    }
                    IDType idNumber = new IDType();
                    idNumber.setValue(String.valueOf(transLine.getTransaccionLineasPK().getNroOrden()));
                    invoiceLine.setID(idNumber);
                    /*
                     * Agregar UNIDAD DE MEDIDA segun SUNAT
                     * <Invoice><cac:InvoiceLine><cbc:InvoicedQuantity>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando CANTIDAD y UNIDAD SUNAT.");
                    }
                    InvoicedQuantityType invoicedQuantity = new InvoicedQuantityType();
                    invoicedQuantity.setValue(transLine.getCantidad().setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
                    invoicedQuantity.setUnitCode(transLine.getUnidadSunat());
                    invoiceLine.setInvoicedQuantity(invoicedQuantity);
                    /*
                     * Agregar UNIDAD DE MEDIDA segun VENTURA
                     * <Invoice><cac:InvoiceLine><cbc:Note>
                     */
                    if (StringUtils.isNotBlank(transLine.getUnidad())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando UNIDAD de VENTURA.");
                        }
                        NoteType note = new NoteType();
                        note.setValue(transLine.getUnidad());
                        invoiceLine.getNote().add(note);
                    }

                    /*
                     * Agregar
                     * <Invoice><cac:InvoiceLine><cbc:LineExtensionAmount>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando TOTAL_LINEAS_SIN_IGV.");
                    }
                    LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
                    lineExtensionAmount.setValue(transLine.getTotalLineaSinIGV().setScale(IUBLConfig.DECIMAL_LINE_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
                    lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    invoiceLine.setLineExtensionAmount(lineExtensionAmount);

                    /*
                     * - Op. Onerosa : tiene precio unitario - Op. No Onerosa :
                     * tiene valor referencial.
                     *
                     * <Invoice><cac:InvoiceLine><cac:PricingReference>
                     */
                    if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllInvoiceLines() [" + this.identifier + "] PricingReference: PRECIO_UNITARIO");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));
                        invoiceLine.setPricingReference(pricingReference);
                    } else if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllInvoiceLines() [" + this.identifier + "] PricingReference: VALOR_REFERENCIAL");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        /*
                         * El formato del precio unitario sera a 2 decimales en
                         * esta opcion porque se entiende que debe ser un valor
                         * 0.00
                         */
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(2, RoundingMode.HALF_UP), currencyCode, IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE));
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));
                        invoiceLine.setPricingReference(pricingReference);
                    }
                    /*
                     * Agregar impuestos de linea
                     * <Invoice><cac:InvoiceLine><cac:AllowanceCharge>
                     */
                    if (null != transLine.getDSCTOMonto() && transLine.getDSCTOMonto().compareTo(BigDecimal.ZERO) > 0) {
                        /*
                         * ChargeIndicatorType
                         *
                         * El valor FALSE representa que es un descuento de
                         * ITEM.
                         */
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DESCUENTO_LINEA.");
                        }
                        invoiceLine.getAllowanceCharge().add(getAllowanceCharge(transLine.getDSCTOMonto(), currencyCode, false));
                    }
                    /*
                     * Agregar impuestos de linea
                     * <Invoice><cac:InvoiceLine><cac:TaxTotal>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando IMPUESTO DE LINEA.");
                    }
                    invoiceLine.getTaxTotal().addAll(getAllTaxTotalLines(transLine.getTransaccionLineaImpuestosList()));

                    /*
                     * Agregar DESCRIPCION y CODIGO DEL ITEM
                     * <Invoice><cac:InvoiceLine><cac:Item>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transLine.getDescripcion() + "] y COD_ARTICULO[" + transLine.getCodArticulo() + "]");
                    }
                    invoiceLine.setItem(getItemForLine(transLine.getDescripcion(), transLine.getCodArticulo()));
                    /*
                     * Agregar el VALOR UNITARIO del item
                     * <Invoice><cac:InvoiceLine><cac:Price>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando VALOR UNITARIO.");
                    }
                    invoiceLine.setPrice(getPriceForLine(transLine.getTransaccionLineasBillrefList(), currencyCode));
                    invoiceLineList.add(invoiceLine);
                }
                for (int i = 0; i < IUBLConfig.lstImporteIGV.size(); i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Totales con IGV" + IUBLConfig.lstImporteIGV.get(i));
                    }
                }
            } catch (UBLDocumentException e) {
                logger.error("getAllInvoiceLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllInvoiceLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
                logger.error("getAllInvoiceLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new UBLDocumentException(IVenturaError.ERROR_320);
            }
        } else {
            logger.error("getAllInvoiceLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllInvoiceLines() [" + this.identifier + "]");
        }
        return invoiceLineList;
    } // getAllInvoiceLines

    private List<InvoiceLineType> getAllBoletaLines(List<TransaccionLineas> transactionLines, String currencyCode) throws UBLDocumentException {

        if (logger.isDebugEnabled()) {
            logger.debug("+getAllBoletaLines() [" + this.identifier + "] transactionLines: " + transactionLines + " currencyCode: " + currencyCode);
        }
        List<InvoiceLineType> boletaLineList = null;

        if (null != transactionLines && !transactionLines.isEmpty()) {
            try {
                boletaLineList = new ArrayList<InvoiceLineType>(transactionLines.size());
                for (int i = 0; i < transactionLines.size(); i++) {
                    IUBLConfig.lstImporteIGV.add(i, transactionLines.get(i).getTotalLineaConIGV());
                    if (logger.isDebugEnabled()) {
                        logger.debug("getImportesConIGV()" + IUBLConfig.lstImporteIGV.get(i));
                    }
                }
                // IUBLConfig.lstImporteIGV = new ArrayList<BigDecimal>();
                for (TransaccionLineas transLine : transactionLines) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Extrayendo informacion del item...");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] NroOrden: " + transLine.getTransaccionLineasPK().getNroOrden() + "\nCantidad: " + transLine.getCantidad() + "\tUnidad: " + transLine.getUnidad() + "\tUnidadSunat: " + transLine.getUnidadSunat() + "\tTotalLineaSinIGV: " + transLine.getTotalLineaSinIGV() + "\tTotalLineaConIGV: " + transLine.getTotalLineaConIGV() + "\nPrecioRefCodigo: " + transLine.getPrecioRefCodigo() + "\tPrecioIGV: " + transLine.getPrecioIGV() + "\tPrecioRefMonto: " + transLine.getPrecioRefMonto() + "\nDCTOMonto: " + transLine.getDSCTOMonto() + "\tDescripcion: " + transLine.getDescripcion() + "\tCodArticulo: " + transLine.getCodArticulo());

                    }

                    InvoiceLineType boletaLine = new InvoiceLineType();

                    /* Agregar <Invoice><cac:InvoiceLine><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando el ID.");
                    }
                    IDType idNumber = new IDType();
                    idNumber.setValue(String.valueOf(transLine.getTransaccionLineasPK().getNroOrden()));
                    boletaLine.setID(idNumber);

                    /*
                     * Agregar UNIDAD DE MEDIDA segun SUNAT
                     * <Invoice><cac:InvoiceLine><cbc:InvoicedQuantity>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando CANTIDAD y UNIDAD SUNAT.");
                    }
                    InvoicedQuantityType invoicedQuantity = new InvoicedQuantityType();
                    invoicedQuantity.setValue(transLine.getCantidad().setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
                    invoicedQuantity.setUnitCode(transLine.getUnidadSunat());
                    boletaLine.setInvoicedQuantity(invoicedQuantity);
                    /*
                     * Agregar UNIDAD DE MEDIDA segun VENTURA
                     * <Invoice><cac:InvoiceLine><cbc:Note>
                     */
                    if (StringUtils.isNotBlank(transLine.getUnidad())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando UNIDAD de VENTURA.");
                        }
                        NoteType note = new NoteType();
                        note.setValue(transLine.getUnidad());
                        boletaLine.getNote().add(note);
                    }

                    /*
                     * Agregar
                     * <Invoice><cac:InvoiceLine><cbc:LineExtensionAmount>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando TOTAL_LINEAS_SIN_IGV.");
                    }
                    LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
                    lineExtensionAmount.setValue(transLine.getTotalLineaSinIGV().setScale(IUBLConfig.DECIMAL_LINE_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
                    lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    boletaLine.setLineExtensionAmount(lineExtensionAmount);

                    /*
                     * - Op. Onerosa : tiene precio unitario - Op. No Onerosa :
                     * tiene valor referencial.
                     *
                     * <Invoice><cac:InvoiceLine><cac:PricingReference>
                     */
                    if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllBoletaLines() [" + this.identifier + "] PricingReference: PRECIO_UNITARIO");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));

                        boletaLine.setPricingReference(pricingReference);
                    } else if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllBoletaLines() [" + this.identifier + "] PricingReference: VALOR_REFERENCIAL");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();

                        /*
                         * El formato del precio unitario sera a 2 decimales en
                         * esta opcion porque se entiende que debe ser un valor
                         * 0.00
                         */
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(2, RoundingMode.HALF_UP), currencyCode, IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE));
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));

                        boletaLine.setPricingReference(pricingReference);
                    }

                    /*
                     * Agregar impuestos de linea
                     * <Invoice><cac:InvoiceLine><cac:AllowanceCharge>
                     */
                    if (null != transLine.getDSCTOMonto() && transLine.getDSCTOMonto().compareTo(BigDecimal.ZERO) == 1) {
                        /*
                         * ChargeIndicatorType
                         *
                         * El valor FALSE representa que es un descuento de
                         * ITEM.
                         */
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando DESCUENTO_LINEA.");
                        }
                        boletaLine.getAllowanceCharge().add(getAllowanceCharge(transLine.getDSCTOMonto(), currencyCode, false));
                    }

                    /*
                     * Agregar impuestos de linea
                     * <Invoice><cac:InvoiceLine><cac:TaxTotal>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando IMPUESTO DE LINEA.");
                    }
                    boletaLine.getTaxTotal().addAll(getAllTaxTotalLines(transLine.getTransaccionLineaImpuestosList()));
                    /*
                     * Agregar DESCRIPCION y CODIGO DEL ITEM
                     * <Invoice><cac:InvoiceLine><cac:Item>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transLine.getDescripcion() + "] y COD_ARTICULO[" + transLine.getCodArticulo() + "]");
                    }
                    boletaLine.setItem(getItemForLine(transLine.getDescripcion(), transLine.getCodArticulo()));
                    /*
                     * Agregar el VALOR UNITARIO del item
                     * <Invoice><cac:InvoiceLine><cac:Price>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando VALOR UNITARIO.");
                    }
                    boletaLine.setPrice(getPriceForLine(transLine.getTransaccionLineasBillrefList(), currencyCode));
                    boletaLineList.add(boletaLine);
                }
                for (int i = 0; i < IUBLConfig.lstImporteIGV.size(); i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLinesImporteIGV." + IUBLConfig.lstImporteIGV.get(i));
                    }
                }

            } catch (UBLDocumentException e) {
                logger.error("getAllBoletaLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllBoletaLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
                logger.error("getAllBoletaLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new UBLDocumentException(IVenturaError.ERROR_320);
            }
        } else {
            logger.error("getAllBoletaLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllBoletaLines() [" + this.identifier + "]");
        }
        return boletaLineList;
    } // getAllBoletaLines

    private List<CreditNoteLineType> getAllCreditNoteLines(List<TransaccionLineas> transactionLines, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllCreditNoteLines() [" + this.identifier + "] transactionLines: " + transactionLines + " currencyCode: " + currencyCode);
        }
        List<CreditNoteLineType> creditNoteLineList = null;
        if (null != transactionLines && !transactionLines.isEmpty()) {
            try {
                creditNoteLineList = new ArrayList<>(transactionLines.size());
                for (TransaccionLineas transLine : transactionLines) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Extrayendo informacion del item...");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] NroOrden: " + transLine.getTransaccionLineasPK().getNroOrden() + "\nCantidad: " + transLine.getCantidad() + "\tUnidad: " + transLine.getUnidad() + "\tUnidadSunat: " + transLine.getUnidadSunat() + "\tTotalLineaSinIGV: " + transLine.getTotalLineaSinIGV() + "\nPrecioRefCodigo: " + transLine.getPrecioRefCodigo() + "\tPrecioIGV: " + transLine.getPrecioIGV() + "\tPrecioRefMonto: " + transLine.getPrecioRefMonto() + "\nDCTOMonto: " + transLine.getDSCTOMonto() + "\tDescripcion: " + transLine.getDescripcion() + "\tCodArticulo: " + transLine.getCodArticulo());
                    }
                    CreditNoteLineType creditNoteLine = new CreditNoteLineType();

                    /* Agregar <CreditNote><cac:CreditNoteLine><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando el ID.");
                    }
                    IDType idNumber = new IDType();
                    idNumber.setValue(String.valueOf(transLine.getTransaccionLineasPK().getNroOrden()));
                    creditNoteLine.setID(idNumber);

                    /*
                     * Agregar UNIDAD DE MEDIDA segun SUNAT
                     * <CreditNote><cac:CreditNoteLine><cbc:CreditedQuantity>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando CANTIDAD y UNIDAD SUNAT.");
                    }
                    CreditedQuantityType creditedQuantity = new CreditedQuantityType();
                    creditedQuantity.setValue(transLine.getCantidad().setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
                    creditedQuantity.setUnitCode(transLine.getUnidadSunat());
                    creditNoteLine.setCreditedQuantity(creditedQuantity);

                    /*
                     * Agregar UNIDAD DE MEDIDA segun VENTURA
                     * <CreditNote><cac:CreditNoteLine><cbc:Note>
                     */
                    if (StringUtils.isNotBlank(transLine.getUnidad())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando UNIDAD de VENTURA.");
                        }
                        NoteType note = new NoteType();
                        note.setValue(transLine.getUnidad());
                        creditNoteLine.getNote().add(note);
                    }

                    /*
                     * Agregar
                     * <CreditNote><cac:CreditNoteLine><cbc:LineExtensionAmount>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando TOTAL_LINEAS_SIN_IGV.");
                    }
                    LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
                    lineExtensionAmount.setValue(transLine.getTotalLineaSinIGV().setScale(IUBLConfig.DECIMAL_LINE_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
                    lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    creditNoteLine.setLineExtensionAmount(lineExtensionAmount);

                    /*
                     * - Op. Onerosa : tiene precio unitario - Op. No Onerosa :
                     * tiene valor referencial.
                     *
                     * <CreditNote><cac:CreditNoteLine><cac:PricingReference>
                     */
                    if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllCreditNoteLines() [" + this.identifier + "] PricingReference: PRECIO_UNITARIO");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));
                        creditNoteLine.setPricingReference(pricingReference);
                    } else if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllCreditNoteLines() [" + this.identifier + "] PricingReference: VALOR_REFERENCIAL");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        /*
                         * El formato del precio unitario sera a 2 decimales en
                         * esta opcion porque se entiende que debe ser un valor
                         * 0.00
                         */
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(2, RoundingMode.HALF_UP), currencyCode, IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE));
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));
                        creditNoteLine.setPricingReference(pricingReference);
                    }                    /*                     * Agregar impuestos de linea                     * <CreditNote><cac:CreditNoteLine><cac:TaxTotal>                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando IMPUESTO DE LINEA.");
                    }
                    creditNoteLine.getTaxTotal().addAll(getAllTaxTotalLines(transLine.getTransaccionLineaImpuestosList()));

                    /*
                     * Agregar DESCRIPCION y CODIGO DEL ITEM
                     * <CreditNote><cac:CreditNoteLine><cac:Item>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transLine.getDescripcion() + "] y COD_ARTICULO[" + transLine.getCodArticulo() + "]");
                    }
                    creditNoteLine.setItem(getItemForLine(transLine.getDescripcion(), transLine.getCodArticulo()));

                    /*
                     * Agregar el VALOR UNITARIO del item
                     * <CreditNote><cac:CreditNoteLine><cac:Price>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando VALOR UNITARIO.");
                    }
                    creditNoteLine.setPrice(getPriceForLine(transLine.getTransaccionLineasBillrefList(), currencyCode));
                    creditNoteLineList.add(creditNoteLine);
                }
            } catch (UBLDocumentException e) {
                logger.error("getAllCreditNoteLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllCreditNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
                logger.error("getAllCreditNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new UBLDocumentException(IVenturaError.ERROR_320);
            }
        } else {
            logger.error("getAllCreditNoteLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllCreditNoteLines() [" + this.identifier + "]");
        }
        return creditNoteLineList;
    } // getAllCreditNoteLines

    private List<DebitNoteLineType> getAllDebitNoteLines(List<TransaccionLineas> transactionLines, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllDebitNoteLines() [" + this.identifier + "] transactionLines: " + transactionLines + " currencyCode: " + currencyCode);
        }
        List<DebitNoteLineType> debitNoteLineList = new ArrayList<>(transactionLines.size());
        if (null != transactionLines && !transactionLines.isEmpty()) {
            try {
                for (TransaccionLineas transLine : transactionLines) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Extrayendo informacion del item...");
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] NroOrden: " + transLine.getTransaccionLineasPK().getNroOrden() + "\nCantidad: " + transLine.getCantidad() + "\tUnidad: " + transLine.getUnidad() + "\tUnidadSunat: " + transLine.getUnidadSunat() + "\tTotalLineaSinIGV: " + transLine.getTotalLineaSinIGV() + "\nPrecioRefCodigo: " + transLine.getPrecioRefCodigo() + "\tPrecioIGV: " + transLine.getPrecioIGV() + "\tPrecioRefMonto: " + transLine.getPrecioRefMonto() + "\nDCTOMonto: " + transLine.getDSCTOMonto() + "\tDescripcion: " + transLine.getDescripcion() + "\tCodArticulo: " + transLine.getCodArticulo());
                    }

                    DebitNoteLineType debitNoteLine = new DebitNoteLineType();

                    /* Agregar <DebitNote><cac:DebitNoteLine><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando el ID.");
                    }
                    IDType idNumber = new IDType();
                    idNumber.setValue(String.valueOf(transLine.getTransaccionLineasPK().getNroOrden()));
                    debitNoteLine.setID(idNumber);

                    /*
                     * Agregar UNIDAD DE MEDIDA segun SUNAT
                     * <DebitNote><cac:DebitNoteLine><cbc:CreditedQuantity>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando CANTIDAD y UNIDAD SUNAT.");
                    }
                    DebitedQuantityType debitedQuantity = new DebitedQuantityType();
                    debitedQuantity.setValue(transLine.getCantidad().setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
                    debitedQuantity.setUnitCode(transLine.getUnidadSunat());
                    debitNoteLine.setDebitedQuantity(debitedQuantity);

                    /*
                     * Agregar UNIDAD DE MEDIDA segun VENTURA
                     * <DebitNote><cac:DebitNoteLine><cbc:Note>
                     */
                    if (StringUtils.isNotBlank(transLine.getUnidad())) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando UNIDAD de VENTURA.");
                        }
                        NoteType note = new NoteType();
                        note.setValue(transLine.getUnidad());
                        debitNoteLine.getNote().add(note);
                    }

                    /*
                     * Agregar
                     * <DebitNote><cac:DebitNoteLine><cbc:LineExtensionAmount>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando TOTAL_LINEAS_SIN_IGV.");
                    }
                    LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
                    lineExtensionAmount.setValue(transLine.getTotalLineaSinIGV().setScale(IUBLConfig.DECIMAL_LINE_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
                    lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    debitNoteLine.setLineExtensionAmount(lineExtensionAmount);

                    /*
                     * - Op. Onerosa : tiene precio unitario - Op. No Onerosa :
                     * tiene valor referencial.
                     *
                     * <DebitNote><cac:DebitNoteLine><cac:PricingReference>
                     */
                    if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllDebitNoteLines() [" + this.identifier + "] PricingReference: PRECIO_UNITARIO");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));

                        debitNoteLine.setPricingReference(pricingReference);
                    } else if (transLine.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getAllDebitNoteLines() [" + this.identifier + "] PricingReference: VALOR_REFERENCIAL");
                        }
                        PricingReferenceType pricingReference = new PricingReferenceType();

                        /*
                         * El formato del precio unitario sera a 2 decimales en
                         * esta opcion porque se entiende que debe ser un valor
                         * 0.00
                         */
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioIGV().setScale(2, RoundingMode.HALF_UP), currencyCode, IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE));
                        pricingReference.getAlternativeConditionPrice().add(getAlternativeConditionPrice(transLine.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transLine.getPrecioRefCodigo()));

                        debitNoteLine.setPricingReference(pricingReference);
                    }

                    /*
                     * Agregar impuestos de linea
                     * <DebitNote><cac:DebitNoteLine><cac:TaxTotal>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando IMPUESTO DE LINEA.");
                    }
                    debitNoteLine.getTaxTotal().addAll(getAllTaxTotalLines(transLine.getTransaccionLineaImpuestosList()));

                    /*
                     * Agregar DESCRIPCION y CODIGO DEL ITEM
                     * <DebitNote><cac:DebitNoteLine><cac:Item>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transLine.getDescripcion() + "] y COD_ARTICULO[" + transLine.getCodArticulo() + "]");
                    }
                    debitNoteLine.setItem(getItemForLine(transLine.getDescripcion(), transLine.getCodArticulo()));

                    /*
                     * Agregar el VALOR UNITARIO del item
                     * <DebitNote><cac:DebitNoteLine><cac:Price>
                     */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando VALOR UNITARIO.");
                    }
                    debitNoteLine.setPrice(getPriceForLine(transLine.getTransaccionLineasBillrefList(), currencyCode));
                    debitNoteLineList.add(debitNoteLine);
                }
            } catch (UBLDocumentException e) {
                logger.error("getAllDebitNoteLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("getAllDebitNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
                logger.error("getAllDebitNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new UBLDocumentException(IVenturaError.ERROR_320);
            }
        } else {
            logger.error("getAllDebitNoteLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllDebitNoteLines() [" + this.identifier + "]");
        }
        return debitNoteLineList;
    } // getAllDebitNoteLines

    private AllowanceChargeType getAllowanceCharge(BigDecimal amountVal, String currencyCode, boolean chargeIndicatorVal) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllowanceCharge() [" + this.identifier + "]");
        }
        AllowanceChargeType allowanceCharge = null;

        try {
            allowanceCharge = new AllowanceChargeType();

            /*
             * Agregar
             * <Invoice><cac:InvoiceLine><cac:AllowanceCharge><cbc:ChargeIndicator
             * >
             */
            ChargeIndicatorType chargeIndicator = new ChargeIndicatorType();
            chargeIndicator.setValue(chargeIndicatorVal);

            /*
             * Agregar
             * <Invoice><cac:InvoiceLine><cac:AllowanceCharge><cbc:Amount>
             */
            AmountType amount = new AmountType();
            amount.setValue(amountVal.setScale(2, RoundingMode.HALF_UP));
            amount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());

            /* Agregar los tag's */
            allowanceCharge.setChargeIndicator(chargeIndicator);
            allowanceCharge.setAmount(amount);
        } catch (Exception e) {
            logger.error("getAllowanceCharge() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_327.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_327);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllowanceCharge() [" + this.identifier + "]");
        }
        return allowanceCharge;
    } // getAllowanceCharge

    private ItemType getItemForLine(String descriptionVal, String articleCodeVal) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getItemForLine() [" + this.identifier + "]");
        }
        ItemType item = null;

        try {
            item = new ItemType();

            if (StringUtils.isNotBlank(descriptionVal)) {
                /* Agregar <Invoice><cac:InvoiceLine><cac:Item><cbc:Description> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getItemForLine() [" + this.identifier + "] Agregando la DESCRIPCION del item.");
                }
                DescriptionType description = new DescriptionType();
                description.setValue(descriptionVal);

                item.getDescription().add(description);
            } else {
                throw new UBLDocumentException(IVenturaError.ERROR_325);
            }

            if (StringUtils.isNotBlank(articleCodeVal)) {
                /*
                 * Agregar <Invoice><cac:InvoiceLine><cac:Item><cac:
                 * SellersItemIdentification>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getItemForLine() [" + this.identifier + "] Agregando el CODIGO DE ARTICULO.");
                }
                ItemIdentificationType sellersItemIdentification = new ItemIdentificationType();
                IDType id = new IDType();
                id.setValue(articleCodeVal);
                sellersItemIdentification.setID(id);

                item.setSellersItemIdentification(sellersItemIdentification);
            }
        } catch (UBLDocumentException e) {
            logger.error("getItemForLine() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getItemForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_326.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_326);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getItemForLine() [" + this.identifier + "]");
        }
        return item;
    } // getItemForLine

    private PriceType getPriceForLine(List<TransaccionLineasBillref> transBillReferenceList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPriceForLine() [" + this.identifier + "]");
        }
        PriceType price = null;

        try {
            String unitValue = null;

            if (null != transBillReferenceList && 0 < transBillReferenceList.size()) {
                for (TransaccionLineasBillref billReference : transBillReferenceList) {
                    if (billReference.getAdtDocRefSchemaId().equalsIgnoreCase(IUBLConfig.HIDDEN_UVALUE)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("getPriceForLine() [" + this.identifier + "] Se encontro el " + IUBLConfig.HIDDEN_UVALUE);
                        }
                        unitValue = billReference.getAdtDocRefId();
                        break;
                    }
                }

                if (StringUtils.isNotBlank(unitValue)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getPriceForLine() [" + this.identifier + "] Agregando el VALOR UNITARIO al tag.");
                    }
                    price = new PriceType();

                    PriceAmountType priceAmount = new PriceAmountType();
                    priceAmount.setValue(BigDecimal.valueOf(Double.valueOf(unitValue)).setScale(IUBLConfig.DECIMAL_LINE_UNIT_VALUE, RoundingMode.HALF_UP));
                    priceAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    price.setPriceAmount(priceAmount);
                } else {
                    logger.error("getPriceForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_334.getMessage());
                    throw new UBLDocumentException(IVenturaError.ERROR_334);
                }
            } else {
                logger.error("getPriceForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_333.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_333);
            }
        } catch (UBLDocumentException e) {
            logger.error("getPriceForLine() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getPriceForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_332.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_332);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getPriceForLine() [" + this.identifier + "]");
        }
        return price;
    } // getPriceForLine

    private PriceType getAlternativeConditionPrice(BigDecimal value, String currencyCode, String type) {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getAlternativeConditionPrice() value: " + value + " currencyCode: " + currencyCode + " type: " + type);
        }
        PriceType alternativeConditionPrice = new PriceType();

        /*
         * <cac:PricingReference><cac:AlternativeConditionPrice><cbc:PriceAmount>
         */
        PriceAmountType priceAmount = new PriceAmountType();
        priceAmount.setValue(value);
        priceAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());

        /*
         * <cac:PricingReference><cac:AlternativeConditionPrice><cbc:PriceTypeCode
         * >
         */
        PriceTypeCodeType priceTypeCode = new PriceTypeCodeType();
        priceTypeCode.setValue(type);

        alternativeConditionPrice.setPriceAmount(priceAmount);
        alternativeConditionPrice.setPriceTypeCode(priceTypeCode);

        return alternativeConditionPrice;
    } // getAlternativeConditionPrice

    private ResponseType getDiscrepancyResponse(String referenceIDValue, String responseCodeValue, String descriptionValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDiscrepancyResponse() [" + this.identifier + "] referenceIDValue: " + referenceIDValue + " responseCodeValue: " + responseCodeValue + " descriptionValue: " + descriptionValue);
        }
        ResponseType discrepancyResponse = null;

        try {
            /* Agregar <cac:DiscrepancyResponse><cbc:ReferenceID> */
            ReferenceIDType referenceID = new ReferenceIDType();
            referenceID.setValue(referenceIDValue);

            /* Agregar <cac:DiscrepancyResponse><cbc:ResponseCode> */
            ResponseCodeType responseCode = new ResponseCodeType();
            responseCode.setValue(responseCodeValue);

            /* Agregar <cac:DiscrepancyResponse><cbc:Description> */
            DescriptionType description = new DescriptionType();
            description.setValue(descriptionValue);

            /* Agregar los TAG's */
            discrepancyResponse = new ResponseType();
            discrepancyResponse.setReferenceID(referenceID);
            discrepancyResponse.setResponseCode(responseCode);
            discrepancyResponse.getDescription().add(description);
        } catch (Exception e) {
            logger.error("getDiscrepancyResponse() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_337);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDiscrepancyResponse() [" + this.identifier + "]");
        }
        return discrepancyResponse;
    } // getDiscrepancyResponse

    private BillingReferenceType getBillingReference(String referenceIDValue, String referenceDocType) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getBillingReference() [" + this.identifier + "] referenceIDValue: " + referenceIDValue + " referenceDocType: " + referenceDocType);
        }
        BillingReferenceType billingReference = null;

        try {
            /* <cac:BillingReference><cac:InvoiceDocumentReference> */
            DocumentReferenceType invoiceDocumentReference = new DocumentReferenceType();

            /*
             * Agregar
             * <cac:BillingReference><cac:InvoiceDocumentReference><cbc:ID>
             */
            IDType id = new IDType();
            id.setValue(referenceIDValue);
            invoiceDocumentReference.setID(id);

            /*
             * Agregar <cac:BillingReference><cac:InvoiceDocumentReference><cbc:
             * DocumentTypeCode>
             */
            DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
            documentTypeCode.setValue(referenceDocType);
            invoiceDocumentReference.setDocumentTypeCode(documentTypeCode);

            /* Agregar los TAG's */
            billingReference = new BillingReferenceType();
            billingReference.setInvoiceDocumentReference(invoiceDocumentReference);
        } catch (Exception e) {
            logger.error("getBillingReference() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_338);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getBillingReference() [" + this.identifier + "]");
        }
        return billingReference;
    } // getBillingReference

    private BigDecimal getSubtotalValueFromTransaction(List<TransaccionTotales> transactionTotalList) throws UBLDocumentException {
        BigDecimal subtotal = null;
        if (null != transactionTotalList && !transactionTotalList.isEmpty()) {
            for (int i = 0; i < transactionTotalList.size(); i++) {
                if (transactionTotalList.get(i).getTransaccionTotalesPK().getId().equalsIgnoreCase(IUBLConfig.ADDITIONAL_MONETARY_1005)) {
                    subtotal = transactionTotalList.get(i).getMonto();
                    transactionTotalList.remove(i);
                    break;
                }
            }
            if (subtotal == null) {
                subtotal = BigDecimal.ZERO;
            }
        } else {
            logger.error("getSubtotalValueFromTransaction() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_330.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_330);
        }

        //if (null == subtotal) {
        //    logger.error("getSubtotalValueFromTransaction() ["
        //            + this.identifier + "] ERROR: "
        //            + IVenturaError.ERROR_348.getMessage());
        //    throw new UBLDocumentException(IVenturaError.ERROR_348);
        //}
        return subtotal;
    }

    private List<DocumentReferenceType> getDespatchDocumentReferences(List<TransaccionDocrefers> transaccionDocrefersList) throws UBLDocumentException {
        if (logger.isInfoEnabled()) {
            logger.info("+getDespatchDocumentReferences() [" + this.identifier + "]");
        }
        List<DocumentReferenceType> despatchDocRefList = new ArrayList<>();
        if (null != transaccionDocrefersList && 0 < transaccionDocrefersList.size()) {
            for (TransaccionDocrefers transDocrefer : transaccionDocrefersList) {
                DocumentReferenceType despatchDocumentReference = new DocumentReferenceType();
                /* <cac:DespatchDocumentReference><cbc:ID> */
                IDType id = new IDType();
                id.setValue(transDocrefer.getId().replace(transDocrefer.getTipo() + "-", ""));
                despatchDocumentReference.setID(id);
                /*
                 * <cac:DespatchDocumentReference><cbc:DocumentTypeCode
                 * >
                 */
                DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
                documentTypeCode.setValue(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                despatchDocumentReference.setDocumentTypeCode(documentTypeCode);
                /* Agregar a la lista */
                despatchDocRefList.add(despatchDocumentReference);
            } // for
        }
        if (logger.isInfoEnabled()) {
            logger.info("-getDespatchDocumentReferences() [" + this.identifier + "]");
        }
        return despatchDocRefList;
    } // getDespatchDocumentReferences

    private DocumentReferenceType getContractDocumentReference(String value, String code) {
        DocumentReferenceType contractDocumentReference = new DocumentReferenceType();
        IDType id = new IDType();
        id.setValue(value);
        contractDocumentReference.setID(id);
        DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
        documentTypeCode.setValue(code);
        contractDocumentReference.setDocumentTypeCode(documentTypeCode);
        return contractDocumentReference;
    } // getContractDocumentReference

    private SupplierPartyType getDespatchSupplierParty(String tipoDoc, String docIdentidad, String razonSocial) {

        SupplierPartyType cpt = new SupplierPartyType();
        CustomerAssignedAccountIDType caaidt = new CustomerAssignedAccountIDType();
        caaidt.setValue(docIdentidad);
        caaidt.setSchemeID(tipoDoc);
        PartyType pt = new PartyType();
        PartyLegalEntityType entityType = new PartyLegalEntityType();
        RegistrationNameType rnt = new RegistrationNameType();
        rnt.setValue(razonSocial);
        entityType.setRegistrationName(rnt);
        pt.getPartyLegalEntity().add(entityType);
        cpt.setCustomerAssignedAccountID(caaidt);
        cpt.setParty(pt);
        return cpt;
    }

    private CustomerPartyType getDespatchCustomerParty(String tipoDoc, String docIdentidad, String razonSocial) {

        CustomerPartyType cpt = new CustomerPartyType();
        CustomerAssignedAccountIDType caaidt = new CustomerAssignedAccountIDType();
        caaidt.setValue(docIdentidad);
        caaidt.setSchemeID(tipoDoc);
        PartyType pt = new PartyType();
        PartyLegalEntityType entityType = new PartyLegalEntityType();
        RegistrationNameType rnt = new RegistrationNameType();
        rnt.setValue(razonSocial);
        entityType.setRegistrationName(rnt);
        pt.getPartyLegalEntity().add(entityType);
        cpt.setCustomerAssignedAccountID(caaidt);
        cpt.setParty(pt);
        return cpt;
    }

    private ShipmentType getShipment(Transaccion tr) {
        ShipmentType st = new ShipmentType();
        HandlingCodeType handlingCodeType = new HandlingCodeType();
        handlingCodeType.setValue(tr.getTransaccionGuiaRemision().getCodigoMotivo());

        InformationType informationType = new InformationType();
        informationType.setValue(tr.getTransaccionGuiaRemision().getDescripcionMotivo());

        SplitConsignmentIndicatorType splitConsignmentIndicatorType = new SplitConsignmentIndicatorType();
        if (tr.getTransaccionGuiaRemision().getIndicadorTransbordoProgramado().equalsIgnoreCase("0")) {
            splitConsignmentIndicatorType.setValue(Boolean.FALSE);
        } else {
            splitConsignmentIndicatorType.setValue(Boolean.TRUE);
        }

        GrossWeightMeasureType grossWeightMeasureType = new GrossWeightMeasureType();
        grossWeightMeasureType.setValue(tr.getTransaccionGuiaRemision().getPeso());
        grossWeightMeasureType.setUnitCode(tr.getTransaccionGuiaRemision().getUnidadMedida());
        TotalTransportHandlingUnitQuantityType totalTransportHandlingUnitQuantityType = new TotalTransportHandlingUnitQuantityType();
        if (tr.getTransaccionGuiaRemision().getNumeroBultos().intValue() > 0) {
            totalTransportHandlingUnitQuantityType.setValue(tr.getTransaccionGuiaRemision().getNumeroBultos());
        }

        ShipmentStageType shipmentStageType = new ShipmentStageType();
        TransportModeCodeType transportModeCodeType = new TransportModeCodeType();
        transportModeCodeType.setValue(tr.getTransaccionGuiaRemision().getModalidadTraslado());
        shipmentStageType.setTransportModeCode(transportModeCodeType);

        PeriodType periodType = new PeriodType();
        StartDateType startDateType = new StartDateType();
        startDateType.setValue(returnXMLGregorianCalendar(tr.getTransaccionGuiaRemision().getFechaInicioTraslado()));
        periodType.setStartDate(startDateType);
        shipmentStageType.setTransitPeriod(periodType);

        if (tr.getTransaccionGuiaRemision().getModalidadTraslado().equalsIgnoreCase("01")) {
            PartyType partyType = new PartyType();
            PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
            IDType iDType = new IDType();
            iDType.setValue(tr.getTransaccionGuiaRemision().getRUCTransporista());
            iDType.setSchemeID(tr.getTransaccionGuiaRemision().getTipoDOCTransportista());
            partyIdentificationType.setID(iDType);

            RegistrationNameType registrationNameType = new RegistrationNameType();
            registrationNameType.setValue(tr.getTransaccionGuiaRemision().getNombreRazonTransportista());
            PartyLegalEntityType entityType = new PartyLegalEntityType();
            entityType.setRegistrationName(registrationNameType);

            partyType.getPartyIdentification().add(partyIdentificationType);
            partyType.getPartyLegalEntity().add(entityType);
            shipmentStageType.getCarrierParty().add(partyType);
        } else {
            TransportMeansType transportMeansType = new TransportMeansType();
            RoadTransportType roadTransportType = new RoadTransportType();
            LicensePlateIDType licensePlateIDType = new LicensePlateIDType();
            licensePlateIDType.setValue(tr.getTransaccionGuiaRemision().getPlacaVehiculo());
            roadTransportType.setLicensePlateID(licensePlateIDType);
            transportMeansType.setRoadTransport(roadTransportType);

            shipmentStageType.setTransportMeans(transportMeansType);
            PersonType personType = new PersonType();
            IDType dType = new IDType();
            dType.setSchemeID(tr.getTransaccionGuiaRemision().getTipoDocConductor());
            dType.setValue(tr.getTransaccionGuiaRemision().getDocumentoConductor());
            personType.setID(dType);
            shipmentStageType.getDriverPerson().add(personType);
        }

        DeliveryType dirSN = new DeliveryType();
        AddressType addressSN = new AddressType();
        IDType iDTypeSN = new IDType();
        iDTypeSN.setValue(tr.getSN_DIR_Ubigeo());
        addressSN.setID(iDTypeSN);
        StreetNameType streetNameTypeSN = new StreetNameType();
        streetNameTypeSN.setValue(tr.getSN_DIR_Direccion());
        addressSN.setStreetName(streetNameTypeSN);

        dirSN.setDeliveryAddress(addressSN);

        if (tr.getTransaccionGuiaRemision().getCodigoMotivo().equalsIgnoreCase("08")) {
            TransportHandlingUnitType transportHandlingUnitType = new TransportHandlingUnitType();
            TransportEquipmentType transportEquipmentType = new TransportEquipmentType();
            IDType iDType1 = new IDType();
            iDType1.setValue(tr.getTransaccionGuiaRemision().getNumeroContenedor());
            transportEquipmentType.setID(iDType1);
            transportHandlingUnitType.getTransportEquipment().add(transportEquipmentType);
            st.getTransportHandlingUnit().add(transportHandlingUnitType);
        }

        AddressType AddressEM = new AddressType();
        IDType iDTypeEM = new IDType();
        iDTypeEM.setValue(tr.getTransaccionGuiaRemision().getUbigeoPartida());
        AddressEM.setID(iDTypeEM);
        StreetNameType streetNameTypeEM = new StreetNameType();
        streetNameTypeEM.setValue(tr.getTransaccionGuiaRemision().getDireccionPartida());
        AddressEM.setStreetName(streetNameTypeEM);

        LocationType locationType = new LocationType();
        IDType iDType1 = new IDType();
        iDType1.setValue(tr.getTransaccionGuiaRemision().getCodigoPuerto());
        locationType.setID(iDType1);

        IDType idt = new IDType();
        idt.setValue("1");
        st.setID(idt);

        st.setHandlingCode(handlingCodeType);
        st.setInformation(informationType);
        st.setSplitConsignmentIndicator(splitConsignmentIndicatorType);
        st.setGrossWeightMeasure(grossWeightMeasureType);
        if (tr.getTransaccionGuiaRemision().getNumeroBultos().intValue() > 0) {
            st.setTotalTransportHandlingUnitQuantity(totalTransportHandlingUnitQuantityType);
        }

        st.getShipmentStage().add(shipmentStageType);
        st.setDelivery(dirSN);
        st.setOriginAddress(AddressEM);
        st.setFirstArrivalPortLocation(locationType);
        return st;
    }

    public XMLGregorianCalendar returnXMLGregorianCalendar(Date date) {
        try {
            String FORMATER = "yyyy-MM-dd";
            DateFormat format = new SimpleDateFormat(FORMATER);
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(format.format(date));
            return xmlDate;
        } catch (DatatypeConfigurationException ex) {
            logger.error("Error : " + ex.getMessage());
        }
        return null;
    }

}
