package service.cloud.request.clientRequest.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.dto.dto.*;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.utils.DateUtil;
import service.cloud.request.clientRequest.utils.Utils;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.codelist.specification._54217._2001.CurrencyCodeContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.LocationType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.ExtensionContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATPerceptionDateType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.SUNATRetentionDateType;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public abstract class UBLBasicHandler {

    Logger logger = LoggerFactory.getLogger(UBLBasicHandler.class);

    /* Identificador de la transaccion */
    protected String identifier;


    /**
     * Constructor para la clase UBLBasicHandler.
     *
     * @param identifier Identificador de la transaccion.
     */
    protected UBLBasicHandler(String identifier) {
        this.identifier = identifier;
    } //UBLBasicHandler


    protected UBLExtensionsType getUBLExtensionsSigner() {
        UBLExtensionsType ublExtensions = new UBLExtensionsType();

        UBLExtensionType ublExtension = new UBLExtensionType();
        ExtensionContentType extensionContent = new ExtensionContentType();
        ublExtension.setExtensionContent(extensionContent);

        ublExtensions.getUBLExtension().add(ublExtension);

        return ublExtensions;
    } //getUBLExtensionsSigner

    protected UBLVersionIDType getUBLVersionID_2_0() {
        UBLVersionIDType ublVersionID = new UBLVersionIDType();
        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_2_0);

        return ublVersionID;
    } //getUBLVersionID_2_0

    protected UBLVersionIDType getUBLVersionID_2_1() {
        UBLVersionIDType ublVersionID = new UBLVersionIDType();
        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_2_1);

        return ublVersionID;
    } //getUBLVersionID_2_1

    protected CustomizationIDType getCustomizationID_1_0() {
        CustomizationIDType customizationID = new CustomizationIDType();
        customizationID.setValue(IUBLConfig.CUSTOMIZATION_ID_1_0);

        return customizationID;
    } //getCustomizationID_1_0

    protected CustomizationIDType getCustomizationID_2_0() {
        CustomizationIDType customizationID = new CustomizationIDType();
        customizationID.setValue(IUBLConfig.CUSTOMIZATION_ID_2_0);
        //customizationID.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);

        return customizationID;
    } //getCustomizationID_2_0

    protected ProfileIDType getProfileID(String profileIDValue) {
        ProfileIDType profileID = new ProfileIDType();
        profileID.setValue(profileIDValue);
        profileID.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
        profileID.setSchemeName("Tipo de Operacion");
        profileID.setSchemeURI(IUBLConfig.URI_CATALOG_51);

        return profileID;
    } //getProfileID

    protected IDType getIDGUIAS(String value, String type_value) {
        IDType id = new IDType();
        id.setSchemeID(type_value);
        id.setSchemeName(IUBLConfig.GUIAS_SCHEMA_NAME);
        id.setSchemeAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
        id.setSchemeURI(IUBLConfig.URI_CATALOG_06);
        id.setValue(value);
        return id;
    }

    protected IDType getID(String idValue) {
        IDType id = new IDType();
        id.setValue(idValue);
        return id;
    } //getID

    protected IDType getID(String idValue, String schemeIDValue) {
        IDType id = new IDType();
        id.setValue(idValue);
        id.setSchemeID(schemeIDValue);

        return id;
    } //getID

    protected UUIDType getUUID(String uuidValue) {
        UUIDType uuid = new UUIDType();
        uuid.setValue(uuidValue);
        return uuid;
    } //getUUID

    protected NoteType getNote(String value) {
        NoteType note = new NoteType();
        note.setValue(value);
        return note;
    } //getNote

    protected IssueDateType getIssueDate(Date issueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueDate() [" + this.identifier + "]");
        }

        if (null == issueDateValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_312);
        }


        IssueDateType issueDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUEDATE_FORMAT);
            String dateString = sdf.format(issueDateValue);

            /* <cbc:IssueDate> */
            issueDate = new IssueDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            issueDate.setValue(datatypeFact.newXMLGregorianCalendar(dateString));
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_313.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_313);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueDate() [" + this.identifier + "]");
        }
        return issueDate;
    } //getIssueDate

    protected IssueTimeType getIssueTimeDefault() throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getIssueTimeDefault() [" + this.identifier + "]");
        }
        IssueTimeType issueTime = null;

        try {

            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.ISSUETIME_FORMAT);
            String dateString = sdf.format(new Date());
            /* <cbc:IssueTime> */
            issueTime = new IssueTimeType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            issueTime.setValue(datatypeFact.newXMLGregorianCalendar(dateString));
        } catch (Exception e) {
            logger.error("getIssueTimeDefault() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_353.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_353);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getIssueTimeDefault() [" + this.identifier + "]");
        }
        return issueTime;
    } //getIssueTimeDefault

    protected DueDateType getDueDate(Date dueDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDueDate() [" + this.identifier + "]");
        }
        DueDateType dueDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.DUEDATE_FORMAT);
            String dateString = sdf.format(dueDateValue);

            /* <cbc:DueDate> */
            dueDate = new DueDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            dueDate.setValue(datatypeFact.newXMLGregorianCalendar(dateString));
        } catch (Exception e) {
            logger.error("getDueDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_315.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_315);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDueDate() [" + this.identifier + "]");
        }
        return dueDate;
    } //getDueDate

    protected StartDateType getStartDate(Object startDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getStartDate() [" + this.identifier + "]");
        }

        if (null == startDateValue || (!(startDateValue instanceof String) && !(startDateValue instanceof Date))) {
            throw new UBLDocumentException(IVenturaError.ERROR_367);
        }


        StartDateType startDate = null;

        try {
            /* <cbc:StartDate> */
            startDate = new StartDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            if (startDateValue instanceof String) {
                startDate.setValue(datatypeFact.newXMLGregorianCalendar((String) startDateValue));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.STARTDATE_FORMAT);
                String dateString = sdf.format((Date) startDateValue);

                startDate.setValue(datatypeFact.newXMLGregorianCalendar(dateString));
            }
        } catch (Exception e) {
            logger.error("getStartDate() [" + this.identifier + "] " + IVenturaError.ERROR_368.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_368);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getStartDate() [" + this.identifier + "]");
        }
        return startDate;
    } //getStartDate

    protected PaidDateType getPaidDate(Date paidDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPaidDate() [" + this.identifier + "]");
        }

        if (null == paidDateValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_360);
        }


        PaidDateType paidDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.PAIDDATE_FORMAT);
            String date = sdf.format(paidDateValue);

            /* <cbc:PaidDate> */
            paidDate = new PaidDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            paidDate.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (Exception e) {
            logger.error("getPaidDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_361.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_361);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getPaidDate() [" + this.identifier + "]");
        }
        return paidDate;
    } //getPaidDate

    protected SUNATPerceptionDateType getSUNATPerceptionDate(Date sunatPerceptionDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getSUNATPerceptionDate() [" + this.identifier + "]");
        }

        if (null == sunatPerceptionDateValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_362);
        }


        SUNATPerceptionDateType sunatPerceptionDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.SUNATPERCEPTIONDATE_FORMAT);
            String date = sdf.format(sunatPerceptionDateValue);

            /* <sac:SUNATPerceptionDate> */
            sunatPerceptionDate = new SUNATPerceptionDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            sunatPerceptionDate.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_363.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_363);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getSUNATPerceptionDate() [" + this.identifier + "]");
        }
        return sunatPerceptionDate;
    } //getSUNATPerceptionDate

    protected SUNATRetentionDateType getSUNATRetentionDate(Date sunatRetentionDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getSUNATRetentionDate() [" + this.identifier + "]");
        }

        if (null == sunatRetentionDateValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_364);
        }


        SUNATRetentionDateType sunatRetentionDate = null;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.SUNATRETENTIONDATE_FORMAT);
            String date = sdf.format(sunatRetentionDateValue);

            /* <sac:SUNATRetentionDate> */
            sunatRetentionDate = new SUNATRetentionDateType();

            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            sunatRetentionDate.setValue(datatypeFact.newXMLGregorianCalendar(date));
        } catch (Exception e) {
            logger.error("getIssueDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_365.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_365);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getSUNATRetentionDate() [" + this.identifier + "]");
        }
        return sunatRetentionDate;
    } //getSUNATRetentionDate

    protected ReferenceDateType getReferenceDate(Date referenceDateValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getReferenceDate() [" + this.identifier + "]");
        }
        if (null == referenceDateValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_340);
        }
        ReferenceDateType referenceDate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.REFERENCEDATE_FORMAT);
            String dateString = sdf.format(referenceDateValue);
            /* <cbc:ReferenceDate> */
            referenceDate = new ReferenceDateType();
            DatatypeFactory datatypeFact = DatatypeFactory.newInstance();
            referenceDate.setValue(datatypeFact.newXMLGregorianCalendar(dateString));
        } catch (Exception e) {
            logger.error("getReferenceDate() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_347.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_347);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getReferenceDate() [" + this.identifier + "]");
        }
        return referenceDate;
    } //getReferenceDate

    protected DateType getDate(Date dateValue) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDate() [" + this.identifier + "]");
        }
        DateType date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(IUBLConfig.DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            String dateString = sdf.format(dateValue);
            /* <cbc:Date> */
            date = new DateType();
            XMLGregorianCalendar xmlGCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
            date.setValue(xmlGCalendar);
        } catch (Exception e) {
            logger.error("getDate() [" + this.identifier + "] ERROR: " + e.getMessage());
            throw e;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDate() [" + this.identifier + "]");
        }
        return date;
    } //getDate

    protected InvoiceTypeCodeType getInvoiceTypeCode(String value, String listIDValue) {
        InvoiceTypeCodeType invoiceTypeCode = new InvoiceTypeCodeType();
        invoiceTypeCode.setValue(value);
        invoiceTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
        invoiceTypeCode.setListID(listIDValue); //Posible error - XML DIFERENTE A UNO ENVIADO EN PRODUCCION
        invoiceTypeCode.setListName("Tipo de Documento");
        invoiceTypeCode.setListSchemeURI(IUBLConfig.URI_CATALOG_51);
        invoiceTypeCode.setListURI(IUBLConfig.URI_CATALOG_01);
        invoiceTypeCode.setName("Tipo de Operacion");
        return invoiceTypeCode;
    } //getInvoiceTypeCode

    protected DespatchAdviceTypeCodeType getDespatchAdviceTypeCode(String value) {

        DespatchAdviceTypeCodeType despatchAdviceTypeCode = new DespatchAdviceTypeCodeType();
        despatchAdviceTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
        despatchAdviceTypeCode.setListName(IUBLConfig.GUIAS_LIST_NAME);
        despatchAdviceTypeCode.setListURI(IUBLConfig.URI_CATALOG_01);
        despatchAdviceTypeCode.setValue(value);
        return despatchAdviceTypeCode;
    } //getDespatchAdviceTypeCode

    protected List<NoteType> getNotes(List<TransactionPropertiesDTO> transaccionPropiedades) {
        List<NoteType> noteList = new ArrayList<>();
        List<TransactionPropertiesDTO> propiedades = new ArrayList<>();
        for (TransactionPropertiesDTO tp : transaccionPropiedades) {
            //System.out.println(tp.getTransaccionPropiedadesPK().getId());
            if ("1000".equalsIgnoreCase(tp.getId()) || "2006".equalsIgnoreCase(tp.getId()))
                propiedades.add(tp);
        }
        for (TransactionPropertiesDTO transaccionPropiedad : propiedades) {
            if (logger.isDebugEnabled()) {
                logger.debug("getNotes() [" + this.identifier + "] xxxxxxxxxxxxxxx Agregando PROPIEDAD(" + transaccionPropiedad.getId() + ", " + transaccionPropiedad.getValor() + ")");
            }
            NoteType note = new NoteType();
            note.setLanguageLocaleID(transaccionPropiedad.getId());
            note.setValue(transaccionPropiedad.getValor());
            noteList.add(note);
        }
        return noteList;
    } //getNotes

    protected List<NoteType> getNotesWithIfSentence(List<TransactionPropertiesDTO> transaccionPropiedadesList) {
        List<NoteType> noteList = new ArrayList<NoteType>();
        for (TransactionPropertiesDTO transaccionPropiedad : transaccionPropiedadesList) {
            if (transaccionPropiedad.getId().startsWith("10") ||
                    transaccionPropiedad.getId().startsWith("20")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getNotes() [" + this.identifier + "] xxxxxxxxxxxxxxx Agregando PROPIEDAD(" + transaccionPropiedad.getId() + ", " + transaccionPropiedad.getValor() + ")");
                }
                NoteType note = new NoteType();
                note.setLanguageLocaleID(transaccionPropiedad.getId());
                note.setValue(transaccionPropiedad.getValor());
                noteList.add(note);
            }
        }
        return noteList;
    } //getNotes

    protected DocumentCurrencyCodeType getDocumentCurrencyCode(String value) {
        DocumentCurrencyCodeType documentCurrencyCode = new DocumentCurrencyCodeType();
        documentCurrencyCode.setValue(value);
        documentCurrencyCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
        documentCurrencyCode.setListID("ISO 4217 Alpha");
        documentCurrencyCode.setListName("Currency");
        return documentCurrencyCode;
    } //getDocumentCurrencyCode

    protected LineCountNumericType getLineCountNumeric(int value) {
        LineCountNumericType lineCountNumeric = new LineCountNumericType();
        lineCountNumeric.setValue(new BigDecimal(value));
        return lineCountNumeric;
    } //getLineCountNumeric

    protected ResponseType getDiscrepancyResponse(String responseCodeValue, String listNameValue, String listURIValue, String descriptionValue, String referenceIDValue) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDiscrepancyResponse() [" + this.identifier + "] responseCode: " + responseCodeValue + " description: " + descriptionValue + " referenceID: " + referenceIDValue);
        }
        ResponseType discrepancyResponse = new ResponseType();
        try {
            /* Agregar <cac:DiscrepancyResponse><cbc:ReferenceID> */
            ReferenceIDType referenceID = new ReferenceIDType();
            referenceID.setValue(referenceIDValue);
            discrepancyResponse.setReferenceID(referenceID);
            /* Agregar <cac:DiscrepancyResponse><cbc:ResponseCode> */
            ResponseCodeType responseCode = new ResponseCodeType();
            responseCode.setValue(responseCodeValue);
            responseCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            responseCode.setListName(listNameValue);
            responseCode.setListURI(listURIValue);
            discrepancyResponse.setResponseCode(responseCode);
            /* Agregar <cac:DiscrepancyResponse><cbc:Description> */
            DescriptionType description = new DescriptionType();
            description.setValue(descriptionValue);
            discrepancyResponse.getDescription().add(description);
        } catch (Exception e) {
            logger.error("getDiscrepancyResponse() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_337);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDiscrepancyResponse() [" + this.identifier + "]");
        }
        return discrepancyResponse;
    } //getDiscrepancyResponse

    protected BillingReferenceType getBillingReference(String referenceIDValue, String referenceDocType) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getBillingReference() [" + this.identifier + "] referenceIDValue: " + referenceIDValue + " referenceDocType: " + referenceDocType);
        }
        BillingReferenceType billingReference = new BillingReferenceType();
        try {
            /* <cac:BillingReference><cac:InvoiceDocumentReference> */
            DocumentReferenceType invoiceDocumentReference = new DocumentReferenceType();
            /* <cac:BillingReference><cac:InvoiceDocumentReference><cbc:ID> */
            IDType id = new IDType();
            id.setValue(referenceIDValue);
            invoiceDocumentReference.setID(id);
            /* <cac:BillingReference><cac:InvoiceDocumentReference><cbc:DocumentTypeCode> */
            DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
            documentTypeCode.setValue(referenceDocType);
            documentTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            documentTypeCode.setListName("Tipo de Documento");
            documentTypeCode.setListURI(IUBLConfig.URI_CATALOG_01);
            invoiceDocumentReference.setDocumentTypeCode(documentTypeCode);
            billingReference.setInvoiceDocumentReference(invoiceDocumentReference);
        } catch (Exception e) {
            logger.error("getBillingReference() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_338);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getBillingReference() [" + this.identifier + "]");
        }
        return billingReference;
    } //getBillingReference

    protected List<DocumentReferenceType> getDespatchDocumentReferences(List<TransactionDocReferDTO> transaccionDocrefersList) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDespatchDocumentReferences() [" + this.identifier + "]");
        }
        List<DocumentReferenceType> despatchDocumentReferenceList = new ArrayList<>(transaccionDocrefersList.size());
        for (TransactionDocReferDTO transaccionDocrefer : transaccionDocrefersList) {
            if (StringUtils.isNotBlank(transaccionDocrefer.getId())) {
                String[] remissionGuides = transaccionDocrefer.getId().trim().split(",");
                for (String rGuide : remissionGuides) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getNotes() [" + this.identifier + "] xxxxxxxxxxxxxxx Agregando DespatchDocumentReference(" + rGuide.trim() + ")");
                    }
                    DocumentReferenceType despatchDocumentReference = new DocumentReferenceType();
                    /* <cac:DespatchDocumentReference><cbc:ID> */
                    IDType id = new IDType();
                    id.setValue(rGuide.trim());
                    despatchDocumentReference.setID(id);
                    /* <cac:DespatchDocumentReference><cbc:DocumentTypeCode> */
                    DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
                    documentTypeCode.setValue(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                    documentTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                    documentTypeCode.setListName("Tipo de Documento");
                    documentTypeCode.setListURI(IUBLConfig.URI_CATALOG_01);
                    despatchDocumentReference.setDocumentTypeCode(documentTypeCode);
                    /* Agregar a la lista */
                    despatchDocumentReferenceList.add(despatchDocumentReference);
                }
            } else {
                logger.error("getDespatchDocumentReferences() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_0.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_0);
            }
        } //for
        if (logger.isDebugEnabled()) {
            logger.debug("-getDespatchDocumentReferences() [" + this.identifier + "]");
        }
        return despatchDocumentReferenceList;
    } //getDespatchDocumentReferences

    protected List<DocumentReferenceType> getAdditionalDocumentReferences(List<TransactionActicipoDTO> transaccionAnticipoList, String identifier, String identifierType) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAdditionalDocumentReferences() [" + this.identifier + "]");
        }
        List<DocumentReferenceType> additionalDocumentReferenceList = new ArrayList<DocumentReferenceType>();
        if (logger.isDebugEnabled()) {
            logger.debug("getAdditionalDocumentReferences() [" + this.identifier + "] size: " + transaccionAnticipoList.size());
        }
        for (TransactionActicipoDTO transaccionAnticipo : transaccionAnticipoList) {
            try {
                DocumentReferenceType additionalDocumentReference = new DocumentReferenceType();
                /* <cac:AdditionalDocumentReference><cbc:ID> */
                IDType id = new IDType();
                id.setValue(transaccionAnticipo.getAntiDOC_Serie_Correlativo());
                additionalDocumentReference.setID(id);
                /* <cac:AdditionalDocumentReference><cbc:DocumentTypeCode> */
                DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
                documentTypeCode.setValue(transaccionAnticipo.getAntiDOC_Tipo());
                documentTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                documentTypeCode.setListName("Documento Relacionado");
                documentTypeCode.setListURI(IUBLConfig.URI_CATALOG_12);
                additionalDocumentReference.setDocumentTypeCode(documentTypeCode);
                String nroAnticipo = String.format("%02d", transaccionAnticipo.getNroAnticipo());
                /* <cac:AdditionalDocumentReference><cbc:DocumentStatusCode> */
                DocumentStatusCodeType documentStatusCode = new DocumentStatusCodeType();
                documentStatusCode.setValue(nroAnticipo);
                documentStatusCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                documentStatusCode.setListName("Anticipo");
                additionalDocumentReference.setDocumentStatusCode(documentStatusCode);
                /* <cac:AdditionalDocumentReference><cac:IssuerParty> */
                PartyType issuerParty = new PartyType();
                PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
                IDType id2 = new IDType();
                id2.setValue(identifier);
                id2.setSchemeID(identifierType);
                id2.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                id2.setSchemeName("Documento de Identidad");
                id2.setSchemeURI(IUBLConfig.URI_CATALOG_06);
                partyIdentificationType.setID(id2);
                issuerParty.getPartyIdentification().add(partyIdentificationType);
                additionalDocumentReference.setIssuerParty(issuerParty);
                additionalDocumentReferenceList.add(additionalDocumentReference);
            } catch (Exception e) {
                logger.error("getAdditionalDocumentReferences() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_327.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_327);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAdditionalDocumentReferences() [" + this.identifier + "]");
        }
        return additionalDocumentReferenceList;
    } //getAdditionalDocumentReference

    //Autor Yosmel
    protected DocumentReferenceType getContractDocumentReference(String value, String code) {
        DocumentReferenceType contractDocumentReference = new DocumentReferenceType();
        IDType id = new IDType();
        id.setValue(value);
        contractDocumentReference.setID(id);
        DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
        documentTypeCode.setValue(code);
        contractDocumentReference.setDocumentTypeCode(documentTypeCode);
        return contractDocumentReference;
    }

    protected SignatureType getSignature(String identifier, String socialReason, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getSignature() [" + this.identifier + "]");
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
            logger.debug("-getSignature() [" + this.identifier + "] signature: " + signature);
        }
        return signature;
    } //getSignature

    protected SupplierPartyType getAccountingSupplierPartyV20(String identifier, String identifierType, String socialReason, String commercialName)
            throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAccountingSupplierPartyV20() [" + this.identifier + "]");
        }
        SupplierPartyType accountingSupplierParty = new SupplierPartyType();

        try {
            /* <cac:AccountingSupplierParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);
            accountingSupplierParty.setCustomerAssignedAccountID(customerAssignedAccountID);

            /* <cac:AccountingSupplierParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);
            accountingSupplierParty.getAdditionalAccountID().add(additionalAccountID);

            /* <cac:AccountingSupplierParty><cac:Party> */
            PartyType party = new PartyType();

            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyName> */
            if (StringUtils.isNotBlank(commercialName)) {
                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(commercialName);
                partyName.setName(name);

                party.getPartyName().add(partyName);
            }

            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyLegalEntity> */
            PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(socialReason);
            partyLegalEntity.setRegistrationName(registrationName);

            party.getPartyLegalEntity().add(partyLegalEntity);

            accountingSupplierParty.setParty(party);
        } catch (Exception e) {
            logger.error("getAccountingSupplierPartyV20() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_302.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("getAccountingSupplierPartyV20() [" + this.identifier + "] -->" + ExceptionUtils.getStackTrace(e));
            }

            throw new UBLDocumentException(IVenturaError.ERROR_302);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAccountingSupplierPartyV20() [" + this.identifier + "] accountingSupplierParty: " + accountingSupplierParty);
        }
        return accountingSupplierParty;
    } //getAccountingSupplierPartyV20

    protected SupplierPartyType getAccountingSupplierPartyV21(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress,
                                                              String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAccountingSupplierPartyV21() [" + this.identifier + "]");
        }
        SupplierPartyType accountingSupplierParty = new SupplierPartyType();

        try {
            PartyType party = new PartyType();

            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyIdentification> */
            PartyIdentificationType partyIdentification = new PartyIdentificationType();
            {
                IDType id = new IDType();
                id.setValue(identifier);
                id.setSchemeID(identifierType);
                id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                id.setSchemeName("Documento de Identidad");
                id.setSchemeURI(IUBLConfig.URI_CATALOG_06);

                partyIdentification.setID(id);
            }
            party.getPartyIdentification().add(partyIdentification);

            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyName> */
            if (StringUtils.isNotBlank(commercialName)) {
                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(commercialName);
                partyName.setName(name);

                party.getPartyName().add(partyName);
            }

//            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyTaxScheme> */
//            PartyTaxSchemeType partyTaxScheme = new PartyTaxSchemeType();
//            {
//                RegistrationNameType registrationName = new RegistrationNameType();
//                registrationName.setValue(socialReason);
//                partyTaxScheme.setRegistrationName(registrationName);
//
//                CompanyIDType companyID = new CompanyIDType();
//                companyID.setValue(identifier);
//                companyID.setSchemeID(identifierType);
//                companyID.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
//                companyID.setSchemeName("Documento de Identidad");
//                companyID.setSchemeURI(IUBLConfig.URI_CATALOG_06);
//                partyTaxScheme.setCompanyID(companyID);
//
//                AddressType registrationAddress = new AddressType();
//                AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
//                addressTypeCode.setValue(IUBLConfig.ADDRESS_TYPE_CODE_DEFAULT);
//                registrationAddress.setAddressTypeCode(addressTypeCode);
//                partyTaxScheme.setRegistrationAddress(registrationAddress);
//
//                TaxSchemeType taxScheme = new TaxSchemeType();
//                IDType id = new IDType();
//                id.setValue("-");
//                taxScheme.setID(id);
//                partyTaxScheme.setTaxScheme(taxScheme);
//            }
//            party.getPartyTaxScheme().add(partyTaxScheme);

            /* <cac:AccountingSupplierParty><cac:Party><cac:PartyLegalEntity> */
            PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();
            {
                RegistrationNameType registrationName = new RegistrationNameType();
                registrationName.setValue(socialReason);
                partyLegalEntity.setRegistrationName(registrationName);

                AddressType registrationAddress = new AddressType();

                IDType id = new IDType();
                id.setValue(ubigeo);
                id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                id.setSchemeName("Ubigeos");
                registrationAddress.setID(id);

                AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
                addressTypeCode.setValue(IUBLConfig.ADDRESS_TYPE_CODE_DEFAULT);
                addressTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                addressTypeCode.setListName("Establecimientos anexos");
                registrationAddress.setAddressTypeCode(addressTypeCode);

                CityNameType cityName = new CityNameType();
                cityName.setValue(province);
                registrationAddress.setCityName(cityName);

                CountrySubentityType countrySubentity = new CountrySubentityType();
                countrySubentity.setValue(department);
                registrationAddress.setCountrySubentity(countrySubentity);

                DistrictType districtType = new DistrictType();
                districtType.setValue(district);
                registrationAddress.setDistrict(districtType);

                AddressLineType addressLine = new AddressLineType();
                LineType line = new LineType();
                line.setValue(fiscalAddress);
                addressLine.setLine(line);
                registrationAddress.getAddressLine().add(addressLine);

                CountryType country = new CountryType();
                IdentificationCodeType identificationCode = new IdentificationCodeType();
                identificationCode.setValue(countryCode);
                identificationCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
                identificationCode.setListID("ISO 3166-1");
                identificationCode.setListName("Country");
                country.setIdentificationCode(identificationCode);
                registrationAddress.setCountry(country);

                partyLegalEntity.setRegistrationAddress(registrationAddress);
            }
            party.getPartyLegalEntity().add(partyLegalEntity);

            accountingSupplierParty.setParty(party);
        } catch (Exception e) {
            logger.error("getAccountingSupplierPartyV21() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_302.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("getAccountingSupplierPartyV21() [" + this.identifier + "] -->" + ExceptionUtils.getStackTrace(e));
            }

            throw new UBLDocumentException(IVenturaError.ERROR_302);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAccountingSupplierPartyV21() [" + this.identifier + "] accountingSupplierParty: " + accountingSupplierParty);
        }
        return accountingSupplierParty;
    } //getAccountingSupplierPartyV21

    protected SupplierPartyType getDespatchSupplierParty(String num_document, String typeDocument, String razonSocial) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDespatchSupplierParty() [" + this.identifier + "]");
        }
        SupplierPartyType despatchSupplierParty = new SupplierPartyType();

        /* <cac:DespatchSupplierParty><cac:Party> */
        PartyType party = new PartyType();
        {

            PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
            partyIdentificationType.setID(getIDGUIAS(num_document, typeDocument));

            PartyLegalEntityType partyLegalEntityType = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(razonSocial);
            partyLegalEntityType.setRegistrationName(registrationName);

            party.getPartyIdentification().add(partyIdentificationType);
            party.getPartyLegalEntity().add(partyLegalEntityType);
        }

        despatchSupplierParty.setParty(party);

        if (logger.isDebugEnabled()) {
            logger.debug("-getDespatchSupplierParty() [" + this.identifier + "]");
        }
        return despatchSupplierParty;
    } //getDespatchSupplierParty


    protected List<PaymentTermsType> getPaymentTermsInvoice(boolean contado, TransacctionDTO transaccion) {


        List<PaymentTermsType> paymentTermsTypes = new ArrayList<>();
        try {
            PaymentTermsType paymentTermsType = null;
            PaymentTermsType paymentTermsType1 = null;
            PaymentTermsType paymentTermsType2 = null;
            if (contado) {

                for (TransactionCuotasDTO lineaCuota : transaccion.getTransactionCuotasDTOList()) {
                    List<PaymentMeansIDType> paymentMeansIDList = new ArrayList<>();
                    paymentTermsType = new PaymentTermsType();
                    IDType idType = new IDType();
                    idType.setValue("FormaPago");
                    PaymentMeansIDType paymentMeansIDType = new PaymentMeansIDType();
                    paymentMeansIDType.setValue(lineaCuota.getFormaPago());
                    paymentMeansIDList.add(paymentMeansIDType);

                    paymentTermsType.setID(idType);
                    paymentTermsType.setPaymentMeansID(paymentMeansIDList);
                    paymentTermsTypes.add(paymentTermsType);
                }

            } else {
                paymentTermsType1 = new PaymentTermsType();


                List<PaymentMeansIDType> paymentMeansIDList1 = new ArrayList<>();
                IDType idType = new IDType();

                /**FORMA PAGO*/
                idType.setValue("FormaPago");
                PaymentMeansIDType paymentMeansIDType = new PaymentMeansIDType();

                /** AL CREDITO / CONTADO */
                paymentMeansIDType.setValue("Credito");
                paymentMeansIDList1.add(paymentMeansIDType);

                /**TIPO CurrencyID CODIGO*/
                AmountType amountType = new AmountType();
                amountType.setCurrencyID(transaccion.getDOC_MON_Codigo());

                /**MONTO PAGO*/
                BigDecimal sumaTotalCuotas = BigDecimal.ZERO;
                for (TransactionCuotasDTO lineaCuota : transaccion.getTransactionCuotasDTOList()) {
                    BigDecimal pagoCuota = Optional.ofNullable(lineaCuota.getMontoCuota().setScale(2, RoundingMode.HALF_UP)).orElse(BigDecimal.ZERO);
                    sumaTotalCuotas = sumaTotalCuotas.add(pagoCuota);
                }
                amountType.setValue(sumaTotalCuotas.setScale(2, RoundingMode.HALF_UP));


                /**SET VALORES */
                paymentTermsType1.setID(idType);
                paymentTermsType1.setPaymentMeansID(paymentMeansIDList1);
                paymentTermsType1.setAmount(amountType);

                paymentTermsTypes.add(paymentTermsType1);

                for (TransactionCuotasDTO lineaCuota : transaccion.getTransactionCuotasDTOList()) {
                    List<PaymentMeansIDType> paymentMeansIDList2 = new ArrayList<>();
                    paymentTermsType2 = new PaymentTermsType();

                    /**FORMA PAGO**/
                    paymentTermsType2.setID(idType);

                    /** #CUOTA **/
                    PaymentMeansIDType paymentMeansIDType2 = new PaymentMeansIDType();
                    paymentMeansIDType2.setValue(lineaCuota.getCuota());
                    paymentMeansIDList2.add(paymentMeansIDType2);

                    /**CURRENCY COD*/
                    AmountType amountType2 = new AmountType();
                    amountType2.setCurrencyID(transaccion.getDOC_MON_Codigo());

                    /**MONTO PAGAR POR CUOTA**/
                    BigDecimal pagoCuota = Optional.ofNullable(lineaCuota.getMontoCuota().setScale(2, RoundingMode.HALF_UP)).orElse(BigDecimal.ZERO);
                    amountType2.setValue(pagoCuota.setScale(2, RoundingMode.HALF_UP));

                    /**FECHA**/
                    PaymentDueDateType paymentDueDateType = new PaymentDueDateType();
                    paymentDueDateType.setValue(Utils.stringDateToDateGregory(DateUtil.parseDate(lineaCuota.getFechaCuota())));

                    paymentTermsType2.setID(idType);
                    paymentTermsType2.setPaymentMeansID(paymentMeansIDList2);
                    paymentTermsType2.setAmount(amountType2);
                    paymentTermsType2.setPaymentDueDate(paymentDueDateType);
                    paymentTermsTypes.add(paymentTermsType2);
                }
            }
        } catch (Exception e) {
            this.logger.trace("Se presenta un error aqu" + e.getMessage() + e.getLocalizedMessage());
        }
        return paymentTermsTypes;
    }


    protected List<PaymentTermsType> getPaymentTermsNoteCredit(TransacctionDTO transaccion) {
        List<PaymentTermsType> paymentTermsTypes = new ArrayList<>();
        try {
            PaymentTermsType paymentTermsType1 = null;
            PaymentTermsType paymentTermsType2 = null;

            paymentTermsType1 = new PaymentTermsType();

            List<PaymentMeansIDType> paymentMeansIDList1 = new ArrayList<>();
            IDType idType = new IDType();

            /**FORMA PAGO*/
            idType.setValue("FormaPago");
            PaymentMeansIDType paymentMeansIDType = new PaymentMeansIDType();

            /** AL CREDITO / CONTADO */
            paymentMeansIDType.setValue("Credito");
            paymentMeansIDList1.add(paymentMeansIDType);

            /**TIPO CurrencyID CODIGO*/
            AmountType amountType = new AmountType();
            amountType.setCurrencyID(transaccion.getDOC_MON_Codigo());

            /**MONTO PAGO*/

            BigDecimal sumTotalCuotas = BigDecimal.ZERO;


            /**SET VALORES */
            paymentTermsType1.setID(idType);
            paymentTermsType1.setPaymentMeansID(paymentMeansIDList1);
            paymentTermsType1.setAmount(amountType);

            paymentTermsTypes.add(paymentTermsType1);

            for (TransactionCuotasDTO lineaCuota : transaccion.getTransactionCuotasDTOList()) {

                /** Se suma todos los montos para la cabecera de la cuota*/
                sumTotalCuotas = sumTotalCuotas.add(lineaCuota.getMontoCuota());
                amountType.setValue(sumTotalCuotas.setScale(2, RoundingMode.HALF_UP));

                List<PaymentMeansIDType> paymentMeansIDList2 = new ArrayList<>();
                paymentTermsType2 = new PaymentTermsType();

                /**FORMA PAGO**/
                paymentTermsType2.setID(idType);

                /** #CUOTA **/
                PaymentMeansIDType paymentMeansIDType2 = new PaymentMeansIDType();
                paymentMeansIDType2.setValue(lineaCuota.getCuota());
                paymentMeansIDList2.add(paymentMeansIDType2);

                /**CURRENCY COD*/
                AmountType amountType2 = new AmountType();
                amountType2.setCurrencyID(transaccion.getDOC_MON_Codigo());

                /**MONTO PAGAR POR CUOTA**/
                BigDecimal pagoCuota = Optional.ofNullable(lineaCuota.getMontoCuota().setScale(2, RoundingMode.HALF_UP)).orElse(BigDecimal.ZERO);
                amountType2.setValue(pagoCuota.setScale(2, RoundingMode.HALF_UP));

                /**FECHA**/
                PaymentDueDateType paymentDueDateType = new PaymentDueDateType();
                paymentDueDateType.setValue(Utils.stringDateToDateGregory(DateUtil.parseDate(lineaCuota.getFechaCuota())));

                paymentTermsType2.setID(idType);
                paymentTermsType2.setPaymentMeansID(paymentMeansIDList2);
                paymentTermsType2.setAmount(amountType2);
                paymentTermsType2.setPaymentDueDate(paymentDueDateType);
                paymentTermsTypes.add(paymentTermsType2);
            }

        } catch (Exception e) {
            this.logger.trace("Se presenta un error aqu" + e.getMessage() + e.getLocalizedMessage());
        }
        return paymentTermsTypes;
    }



    protected CustomerPartyType getAccountingCustomerPartyV21(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress,
                                                              String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAccountingCustomerPartyV21() [" + this.identifier + "]");
        }
        CustomerPartyType accountingCustomerParty = new CustomerPartyType();

        try {
            PartyType party = new PartyType();

            /* <cac:AccountingCustomerParty><cac:Party><cac:PartyIdentification> */
            PartyIdentificationType partyIdentification = new PartyIdentificationType();
            {
                IDType id = new IDType();
                id.setValue(identifier);
                id.setSchemeID(identifierType);
                id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                id.setSchemeName("Documento de Identidad");
                id.setSchemeURI(IUBLConfig.URI_CATALOG_06);

                partyIdentification.setID(id);
            }
            party.getPartyIdentification().add(partyIdentification);

            /* <cac:AccountingCustomerParty><cac:Party><cac:PartyName> */
            if (StringUtils.isNotBlank(commercialName)) {
                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(socialReason);
                partyName.setName(name);

                party.getPartyName().add(partyName);
            }

            /* <cac:AccountingCustomerParty><cac:Party><cac:PhysicalLocation> */
            if (identifier.startsWith(IUBLConfig.BOLETA_SERIE_PREFIX) && StringUtils.isNotBlank(fiscalAddress) &&
                    StringUtils.isNotBlank(department) && StringUtils.isNotBlank(province) && StringUtils.isNotBlank(district)) {
                LocationType physicalLocation = new LocationType();
                DescriptionType description = new DescriptionType();
                description.setValue(fiscalAddress + " - " + district + " - " + province + " - " + department);
                physicalLocation.setDescription(description);

                party.setPhysicalLocation(physicalLocation);
            }

            /* <cac:AccountingCustomerParty><cac:Party><cac:PartyTaxScheme> */
//            PartyTaxSchemeType partyTaxScheme = new PartyTaxSchemeType();
//            {
//                RegistrationNameType registrationName = new RegistrationNameType();
//                registrationName.setValue(socialReason);
//                partyTaxScheme.setRegistrationName(registrationName);
//
//                CompanyIDType companyID = new CompanyIDType();
//                companyID.setValue(identifier);
//                companyID.setSchemeID(identifierType);
//                companyID.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
//                companyID.setSchemeName("Documento de Identidad");
//                companyID.setSchemeURI(IUBLConfig.URI_CATALOG_06);
//                partyTaxScheme.setCompanyID(companyID);
//
//                AddressType registrationAddress = new AddressType();
//                AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
//                addressTypeCode.setValue(IUBLConfig.ADDRESS_TYPE_CODE_DEFAULT);
//                registrationAddress.setAddressTypeCode(addressTypeCode);
//                partyTaxScheme.setRegistrationAddress(registrationAddress);
//
//                TaxSchemeType taxScheme = new TaxSchemeType();
//                IDType id = new IDType();
//                id.setValue("-");
//                taxScheme.setID(id);
//                partyTaxScheme.setTaxScheme(taxScheme);
//            }
//            party.getPartyTaxScheme().add(partyTaxScheme);

            /* <cac:AccountingCustomerParty><cac:Party><cac:PartyLegalEntity> */
            PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();
            {
                RegistrationNameType registrationName = new RegistrationNameType();
                registrationName.setValue(socialReason);
                partyLegalEntity.setRegistrationName(registrationName);

                if (identifier.startsWith(IUBLConfig.INVOICE_SERIE_PREFIX) && StringUtils.isNotBlank(fiscalAddress) && StringUtils.isNotBlank(department) && StringUtils.isNotBlank(province) &&
                        StringUtils.isNotBlank(district) && StringUtils.isNotBlank(ubigeo) && StringUtils.isNotBlank(countryCode)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAccountingCustomerPartyV21() [" + this.identifier + "] Si tiene DIRECCION FISCAL.");
                    }
                    AddressType registrationAddress = new AddressType();

                    IDType id = new IDType();
                    id.setValue(ubigeo);
                    id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                    id.setSchemeName("Ubigeos");
                    registrationAddress.setID(id);

                    AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
                    addressTypeCode.setValue(IUBLConfig.ADDRESS_TYPE_CODE_DEFAULT);
                    addressTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                    addressTypeCode.setListName("Establecimientos anexos");
                    registrationAddress.setAddressTypeCode(addressTypeCode);

                    CityNameType cityName = new CityNameType();
                    cityName.setValue(province);
                    registrationAddress.setCityName(cityName);

                    CountrySubentityType countrySubentity = new CountrySubentityType();
                    countrySubentity.setValue(department);
                    registrationAddress.setCountrySubentity(countrySubentity);

                    DistrictType districtType = new DistrictType();
                    districtType.setValue(district);
                    registrationAddress.setDistrict(districtType);

                    AddressLineType addressLine = new AddressLineType();
                    LineType line = new LineType();
                    line.setValue(fiscalAddress);
                    addressLine.setLine(line);
                    registrationAddress.getAddressLine().add(addressLine);

                    CountryType country = new CountryType();
                    IdentificationCodeType identificationCode = new IdentificationCodeType();
                    identificationCode.setValue(countryCode);
                    identificationCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
                    identificationCode.setListID("ISO 3166-1");
                    identificationCode.setListName("Country");
                    country.setIdentificationCode(identificationCode);
                    registrationAddress.setCountry(country);

                    partyLegalEntity.setRegistrationAddress(registrationAddress);
                }
            }
            party.getPartyLegalEntity().add(partyLegalEntity);

            accountingCustomerParty.setParty(party);
        } catch (Exception e) {
            logger.error("getAccountingCustomerPartyV21() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_303.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("getAccountingCustomerPartyV21() [" + this.identifier + "] -->" + ExceptionUtils.getStackTrace(e));
            }

            throw new UBLDocumentException(IVenturaError.ERROR_303);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAccountingCustomerPartyV21() [" + this.identifier + "] accountingCustomerParty: " + accountingCustomerParty);
        }
        return accountingCustomerParty;
    } //getAccountingCustomerPartyV21

    protected SupplierPartyType getSellerSupplierParty(String num_document, String typeDocument, String razonSocial) {


        SupplierPartyType supplierPartyType = new SupplierPartyType();
        PartyType party = new PartyType();
        {

            PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
            partyIdentificationType.setID(getIDGUIAS(num_document, typeDocument)); //BD RUC y TIPO DOC

            PartyLegalEntityType partyLegalEntityType = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(razonSocial); //BD RAZON SOCIAL
            partyLegalEntityType.setRegistrationName(registrationName);

            party.getPartyIdentification().add(partyIdentificationType);
            party.getPartyLegalEntity().add(partyLegalEntityType);
        }

        supplierPartyType.setParty(party);
        return supplierPartyType;
    }

    protected CustomerPartyType getBuyerCustomerParty(String num_document, String typeDocument, String razonSocial) {

        CustomerPartyType customerPartyType = new CustomerPartyType();
        PartyType party = new PartyType();

        {
            PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
            partyIdentificationType.setID(getIDGUIAS(num_document, typeDocument)); //BD RUC y TIPO DOC

            PartyLegalEntityType partyLegalEntityType = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(razonSocial); //BD RAZON SOCIAL
            partyLegalEntityType.setRegistrationName(registrationName);

            party.getPartyIdentification().add(partyIdentificationType);
            party.getPartyLegalEntity().add(partyLegalEntityType);
        }
        customerPartyType.setParty(party);
        return customerPartyType;

    }

    protected CustomerPartyType getDeliveryCustomerParty(String num_document, String typeDocument, String razonSocial) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDeliveryCustomerParty() [" + this.identifier + "]");
        }
        CustomerPartyType deliveryCustomerParty = new CustomerPartyType();

        /* <cac:DeliveryCustomerParty><cac:Party> */
        PartyType party = new PartyType();
        {

            PartyIdentificationType partyIdentificationType = new PartyIdentificationType();
            partyIdentificationType.setID(getIDGUIAS(num_document, typeDocument));

            PartyLegalEntityType partyLegalEntityType = new PartyLegalEntityType();
            RegistrationNameType registrationName = new RegistrationNameType();
            registrationName.setValue(razonSocial);
            partyLegalEntityType.setRegistrationName(registrationName);

            party.getPartyIdentification().add(partyIdentificationType);
            party.getPartyLegalEntity().add(partyLegalEntityType);
        }

        deliveryCustomerParty.setParty(party);

        if (logger.isDebugEnabled()) {
            logger.debug("-getDeliveryCustomerParty() [" + this.identifier + "]");
        }
        return deliveryCustomerParty;
    } //getDeliveryCustomerParty

    protected PartyType getAgentParty(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress,
                                      String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAgentParty() [" + this.identifier + "]");
        }
        PartyType agentParty = new PartyType();

        try {
            /* <cac:AgentParty><cac:PartyIdentification> */
            PartyIdentificationType partyIdentification = new PartyIdentificationType();
            {
                IDType id = new IDType();
                id.setValue(identifier);
                id.setSchemeID(identifierType);

                partyIdentification.setID(id);
            }
            agentParty.getPartyIdentification().add(partyIdentification);

            /* <cac:AgentParty><cac:PartyName> */
            if (StringUtils.isNotBlank(commercialName)) {
                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(commercialName);
                partyName.setName(name);

                agentParty.getPartyName().add(partyName);
            }

            /* <cac:AgentParty><cac:PostalAddress> */
            if (StringUtils.isNotBlank(fiscalAddress) && StringUtils.isNotBlank(department) && StringUtils.isNotBlank(province) && StringUtils.isNotBlank(district) &&
                    StringUtils.isNotBlank(ubigeo) && StringUtils.isNotBlank(countryCode)) {
                AddressType postalAddress = new AddressType();

                /* <cac:AgentParty><cac:PostalAddress><cbc:ID> */
                IDType id = new IDType();
                id.setValue(ubigeo);
                postalAddress.setID(id);

                /* <cac:AgentParty><cac:PostalAddress><cbc:StreetName> */
                StreetNameType streetName = new StreetNameType();
                streetName.setValue(fiscalAddress);
                postalAddress.setStreetName(streetName);

                /* <cac:AgentParty><cac:PostalAddress><cbc:CityName> */
                CityNameType cityName = new CityNameType();
                cityName.setValue(province);
                postalAddress.setCityName(cityName);

                /* <cac:AgentParty><cac:PostalAddress><cbc:CountrySubentity> */
                CountrySubentityType countrySubentity = new CountrySubentityType();
                countrySubentity.setValue(department);
                postalAddress.setCountrySubentity(countrySubentity);

                /* <cac:AgentParty><cac:PostalAddress><cbc:District> */
                DistrictType districtType = new DistrictType();
                districtType.setValue(district);
                postalAddress.setDistrict(districtType);

                /* <cac:AgentParty><cac:PostalAddress><cac:Country> */
                CountryType country = new CountryType();
                IdentificationCodeType identificationCode = new IdentificationCodeType();
                identificationCode.setValue(countryCode);
                country.setIdentificationCode(identificationCode);
                postalAddress.setCountry(country);

                agentParty.setPostalAddress(postalAddress);
            }

            /* <cac:AgentParty><cac:PartyLegalEntity> */
            PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();
            {
                RegistrationNameType registrationName = new RegistrationNameType();
                registrationName.setValue(socialReason);
                partyLegalEntity.setRegistrationName(registrationName);
            }
            agentParty.getPartyLegalEntity().add(partyLegalEntity);

            /* <cac:AgentParty><cac:Contact> */
            ContactType contact = new ContactType();
            {
                /* <cac:AgentParty><cac:Contact><cbc:Name> */
                NameType name = new NameType();
                name.setValue(StringUtils.isBlank(contactName) ? "-" : contactName);
                contact.setName(name);

                /* <cac:AgentParty><cac:Contact><cbc:ElectronicMail> */
                ElectronicMailType electronicMailType = new ElectronicMailType();
                electronicMailType.setValue(StringUtils.isBlank(electronicMail) ? "-" : electronicMail);
                contact.setElectronicMail(electronicMailType);
            }
            agentParty.setContact(contact);
        } catch (Exception e) {
            logger.error("getAgentParty() [" + this.identifier + "] ERROR: " + e.getMessage());
            throw e;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAgentParty() [" + this.identifier + "]");
        }
        return agentParty;
    } //getAgentParty

    protected PartyType getReceiverParty(String identifier, String identifierType, String socialReason, String commercialName, String fiscalAddress,
                                         String department, String province, String district, String ubigeo, String countryCode, String contactName, String electronicMail) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("+getReceiverParty() [" + this.identifier + "]");
        }
        PartyType receiverParty = new PartyType();

        try {
            /* <cac:ReceiverParty><cac:PartyIdentification> */
            PartyIdentificationType partyIdentification = new PartyIdentificationType();
            {
                IDType id = new IDType();
                id.setValue(identifier);
                id.setSchemeID(identifierType);

                partyIdentification.setID(id);
            }
            receiverParty.getPartyIdentification().add(partyIdentification);

            /* <cac:ReceiverParty><cac:PartyName> */
            if (StringUtils.isNotBlank(commercialName)) {
                PartyNameType partyName = new PartyNameType();
                NameType name = new NameType();
                name.setValue(commercialName);
                partyName.setName(name);

                receiverParty.getPartyName().add(partyName);
            }

            /* <cac:ReceiverParty><cac:PostalAddress> */
            if (StringUtils.isNotBlank(fiscalAddress) && StringUtils.isNotBlank(department) && StringUtils.isNotBlank(province) && StringUtils.isNotBlank(district) &&
                    StringUtils.isNotBlank(ubigeo) && StringUtils.isNotBlank(countryCode)) {
                AddressType postalAddress = new AddressType();

                /* <cac:ReceiverParty><cac:PostalAddress><cbc:ID> */
                IDType id = new IDType();
                id.setValue(ubigeo);
                postalAddress.setID(id);

                /* <cac:ReceiverParty><cac:PostalAddress><cbc:StreetName> */
                StreetNameType streetName = new StreetNameType();
                streetName.setValue(fiscalAddress);
                postalAddress.setStreetName(streetName);

                /* <cac:ReceiverParty><cac:PostalAddress><cbc:CityName> */
                CityNameType cityName = new CityNameType();
                cityName.setValue(province);
                postalAddress.setCityName(cityName);

                /* <cac:ReceiverParty><cac:PostalAddress><cbc:CountrySubentity> */
                CountrySubentityType countrySubentity = new CountrySubentityType();
                countrySubentity.setValue(department);
                postalAddress.setCountrySubentity(countrySubentity);

                /* <cac:ReceiverParty><cac:PostalAddress><cbc:District> */
                DistrictType districtType = new DistrictType();
                districtType.setValue(district);
                postalAddress.setDistrict(districtType);

                /* <cac:ReceiverParty><cac:PostalAddress><cac:Country> */
                CountryType country = new CountryType();
                IdentificationCodeType identificationCode = new IdentificationCodeType();
                identificationCode.setValue(countryCode);
                country.setIdentificationCode(identificationCode);
                postalAddress.setCountry(country);

                receiverParty.setPostalAddress(postalAddress);
            }

            /* <cac:ReceiverParty><cac:PartyLegalEntity> */
            PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();
            {
                RegistrationNameType registrationName = new RegistrationNameType();
                registrationName.setValue(socialReason);
                partyLegalEntity.setRegistrationName(registrationName);
            }
            receiverParty.getPartyLegalEntity().add(partyLegalEntity);

            /* <cac:ReceiverParty><cac:Contact> */
            ContactType contact = new ContactType();
            {
                /* <cac:ReceiverParty><cac:Contact><cbc:Name> */
                NameType name = new NameType();
                name.setValue(StringUtils.isBlank(contactName) ? "-" : contactName);
                contact.setName(name);

                /* <cac:ReceiverParty><cac:Contact><cbc:ElectronicMail> */
                ElectronicMailType electronicMailType = new ElectronicMailType();
                electronicMailType.setValue(StringUtils.isBlank(electronicMail) ? "-" : electronicMail);
                contact.setElectronicMail(electronicMailType);
            }
            receiverParty.setContact(contact);
        } catch (Exception e) {
            logger.error("getReceiverParty() [" + this.identifier + "] ERROR: " + e.getMessage());
            throw e;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getReceiverParty() [" + this.identifier + "]");
        }
        return receiverParty;
    } //getReceiverParty

    protected PaymentMeansType getPaymentMeans(String cuentaDetraccion, String codigoPago) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPaymentMeans() [" + this.identifier + "]");
        }
        PaymentMeansType paymentMeans = new PaymentMeansType();

        try {
            /* <cac:PaymentMeans><cbc:PaymentMeansCode> */
            PaymentMeansCodeType paymentMeansCode = new PaymentMeansCodeType();
            paymentMeansCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            paymentMeansCode.setListName("Medio de pago");
            paymentMeansCode.setListURI(IUBLConfig.URI_CATALOG_59);
            paymentMeansCode.setValue(codigoPago);
            paymentMeans.setPaymentMeansCode(paymentMeansCode);

            /* <cac:PaymentMeans><cbc:PayeeFinancialAccount> */
            FinancialAccountType payeeFinancialAccount = new FinancialAccountType();
            IDType id = new IDType();
            id.setValue(cuentaDetraccion);
            payeeFinancialAccount.setID(id);
            paymentMeans.setPayeeFinancialAccount(payeeFinancialAccount);

            IDType idType = new IDType();
            idType.setValue("Detraccion");
            paymentMeans.setID(idType);
        } catch (Exception e) {
            logger.error("getPaymentMeans() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_327.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_327);
        }
        return paymentMeans;
    } //getPaymentMeans

    protected PaymentTermsType getPaymentTerms(String codigoDetraccion, BigDecimal montoDetraccion, BigDecimal porcDetraccion) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPaymentTerms() [" + this.identifier + "]");
        }
        PaymentTermsType paymentTerms = new PaymentTermsType();
        try {
            /* <cac:PaymentTerms><cbc:PaymentMeansID> */
            PaymentMeansIDType paymentMeansID = new PaymentMeansIDType();
            paymentMeansID.setValue(codigoDetraccion);
            paymentMeansID.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
            paymentMeansID.setSchemeName("Codigo de detraccion");
            paymentMeansID.setSchemeURI(IUBLConfig.URI_CATALOG_54);
            paymentTerms.getPaymentMeansID().add(paymentMeansID);

            /* <cac:PaymentTerms><cbc:PaymentPercent> */
            PaymentPercentType paymentPercent = new PaymentPercentType();
            paymentPercent.setValue(porcDetraccion.setScale(IUBLConfig.DECIMAL_PAYMENTTERMS_PAYMENTPERCENT, RoundingMode.HALF_UP));
            paymentTerms.setPaymentPercent(paymentPercent);

            /* <cac:PaymentTerms><cbc:Amount> */
            AmountType amount = new AmountType();
            amount.setCurrencyID("PEN"); //El monto de detracciones siempre es en moneda Nacional
            amount.setValue(montoDetraccion.setScale(IUBLConfig.DECIMAL_PAYMENTTERMS_AMOUNT, RoundingMode.HALF_UP));
            paymentTerms.setAmount(amount);

            /* <cac:PaymentTerms><cbc:ID> */
            IDType idType = new IDType();
            idType.setValue("Detraccion");
            paymentTerms.setID(idType);
        } catch (Exception e) {
            logger.error("getPaymentTerms() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_327.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_327);
        }
        return paymentTerms;
    } //getPaymentTerms

    protected TotalInvoiceAmountType getTotalInvoiceAmount(BigDecimal totalInvoiceAmountValue, String currencyCode) {
        TotalInvoiceAmountType totalInvoiceAmount = new TotalInvoiceAmountType();
        totalInvoiceAmount.setValue(totalInvoiceAmountValue.setScale(2, RoundingMode.HALF_UP));
        totalInvoiceAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());

        return totalInvoiceAmount;
    } //getTotalInvoiceAmount

    protected List<PaymentType> getPrepaidPaymentV21(List<TransactionActicipoDTO> lstAnticipo) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPrepaidPaymentV21() [" + this.identifier + "]");
        }
        List<PaymentType> prepaidPaymentList = new ArrayList<PaymentType>();

        if (logger.isDebugEnabled()) {
            logger.debug("getPrepaidPaymentV21() [" + this.identifier + "] size: " + lstAnticipo.size());
        }
        for (int i = 0; i < lstAnticipo.size(); i++) {
            try {
                PaymentType prepaidPayment = new PaymentType();

                String nroAnticipo = String.format("%02d", lstAnticipo.get(i).getNroAnticipo());

                /* <cac:PrepaidPayment><cbc:ID> */
                IDType id = new IDType();
                id.setValue(nroAnticipo);
                id.setSchemeID(lstAnticipo.get(i).getAntiDOC_Tipo());
                id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                id.setSchemeName("Anticipo");
                prepaidPayment.setID(id);

                /* <cac:PrepaidPayment><cbc:PaidAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("+getPrepaidPaymentV21() [" + this.identifier + "] AnticipoMonto: " + lstAnticipo.get(i).getAnticipo_Monto() + " DOCMoneda: " + lstAnticipo.get(i).getDOC_Moneda());
                }
                PaidAmountType paidAmount = new PaidAmountType();
                paidAmount.setValue(lstAnticipo.get(i).getAnticipo_Monto().setScale(IUBLConfig.DECIMAL_PREPAIDPAYMENT_PAIDAMOUNT, RoundingMode.HALF_UP));
                paidAmount.setCurrencyID(CurrencyCodeContentType.valueOf(lstAnticipo.get(i).getDOC_Moneda()).value());
                prepaidPayment.setPaidAmount(paidAmount);

                /* <cac:PrepaidPayment><cbc:InstructionID> */
                if (logger.isDebugEnabled()) {
                    logger.debug("+getPrepaidPaymentV21() [" + this.identifier + "] DOCNumero: " + lstAnticipo.get(i).getDOC_Numero() + " DOCTipo: " + lstAnticipo.get(i).getDOC_Tipo() + "]");
                }
                InstructionIDType instructionID = new InstructionIDType();
                instructionID.setValue(lstAnticipo.get(i).getDOC_Numero());
                instructionID.setSchemeID(lstAnticipo.get(i).getDOC_Tipo());
                prepaidPayment.setInstructionID(instructionID);

                prepaidPaymentList.add(prepaidPayment);
            } catch (Exception e) {
                logger.error("getPrepaidPaymentV21() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_336.getMessage());
                logger.error("getPrepaidPaymentV21() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
                throw new UBLDocumentException(IVenturaError.ERROR_336);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("getPrepaidPaymentV21() [" + this.identifier + "] Genero correctamente el TAG <cac:PrepaidPayment>");
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getPrepaidPaymentV21() [" + this.identifier + "]");
        }
        return prepaidPaymentList;
    } //getPrepaidPaymentV21

    protected ShipmentType getShipment(TransactionGuiasDTO transaccionGuiaRemision, TransacctionDTO transaccion) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getShipment() [" + this.identifier + "]");
        }
        ShipmentType shipment = new ShipmentType();

        try {
            /* <cac:Shipment><cbc:ID> */
            shipment.setID(getID("SUNAT_Envio"));

            /* <cac:Shipment><cbc:HandlingCode> */
            HandlingCodeType handlingCode = new HandlingCodeType();
            handlingCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            handlingCode.setListName(IUBLConfig.TRANSPORT_MOT);
            handlingCode.setListURI(IUBLConfig.URI_CATALOG_20);
            handlingCode.setValue(transaccionGuiaRemision.getCodigoMotivo());
            shipment.setHandlingCode(handlingCode);

            /* <cac:Shipment><cbc:HandlingInstructions> */
            HandlingInstructionsType handlingInstructions = new HandlingInstructionsType();
            handlingInstructions.setValue(transaccionGuiaRemision.getDescripcionMotivo());
            shipment.setHandlingInstructions(handlingInstructions);

            /* <cac:Shipment><cbc:GrossWeightMeasure> */
            GrossWeightMeasureType grossWeightMeasure = new GrossWeightMeasureType();
            if (transaccionGuiaRemision.getPeso() != null) {
                grossWeightMeasure.setValue(new BigDecimal(transaccionGuiaRemision.getPeso().setScale(3, RoundingMode.HALF_UP).toString()));
            }
            grossWeightMeasure.setUnitCode(transaccionGuiaRemision.getUnidadMedida());
            shipment.setGrossWeightMeasure(grossWeightMeasure);


            if (transaccionGuiaRemision.getCodigoMotivo() != null && ( transaccionGuiaRemision.getCodigoMotivo().equals("08") || transaccionGuiaRemision.getCodigoMotivo().equals("09"))) {
                if (transaccionGuiaRemision.getNumeroBultos() != null && transaccionGuiaRemision.getNumeroBultos().compareTo(BigDecimal.ZERO) > 0) {
                    /* <cac:Shipment><cbc:TotalTransportHandlingUnitQuantity> */
                    TotalTransportHandlingUnitQuantityType totalTransportHandlingUnitQuantity = new TotalTransportHandlingUnitQuantityType();
                    totalTransportHandlingUnitQuantity.setValue(new BigDecimal(transaccionGuiaRemision.getNumeroBultos().toBigInteger()));
                    shipment.setTotalTransportHandlingUnitQuantity(totalTransportHandlingUnitQuantity);
                }
            }


            if (transaccionGuiaRemision.getIndicadorTransbordo() != null && transaccionGuiaRemision.getIndicadorTransbordo().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorTransbordoProgramado");
                shipment.getSpecialInstructions().add(specialInstructions);
            }

            if (transaccionGuiaRemision.getIndicadorTraslado() != null && transaccionGuiaRemision.getIndicadorTraslado().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorTrasladoVehiculoM1L");
                shipment.getSpecialInstructions().add(specialInstructions);
            }

            if (transaccionGuiaRemision.getIndicadorRetorno() != null && transaccionGuiaRemision.getIndicadorRetorno().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorRetornoVehiculoEnvaseVacio");
                shipment.getSpecialInstructions().add(specialInstructions);
            }

            if (transaccionGuiaRemision.getIndicadorRetornoVehiculo() != null && transaccionGuiaRemision.getIndicadorRetornoVehiculo().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorRetornoVehiculoVacio");
                shipment.getSpecialInstructions().add(specialInstructions);
            }

            if (transaccionGuiaRemision.getIndicadorTrasladoTotal() != null && transaccionGuiaRemision.getIndicadorTrasladoTotal().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorTrasladoTotalDAMoDS");
                shipment.getSpecialInstructions().add(specialInstructions);
            }

            if (transaccionGuiaRemision.getIndicadorRegistro() != null && transaccionGuiaRemision.getIndicadorRegistro().equals("Y")) {
                SpecialInstructionsType specialInstructions = new SpecialInstructionsType();
                specialInstructions.setValue("SUNAT_Envio_IndicadorVehiculoConductoresTransp");
                shipment.getSpecialInstructions().add(specialInstructions);
            }



            ShipmentStageType shipmentStage = new ShipmentStageType();
            {
                /* <cac:Shipment><cac:ShipmentStage><cbc:TransportModeCode> */
                TransportModeCodeType transportModeCode = new TransportModeCodeType();
                transportModeCode.setListName(IUBLConfig.TRANSPORT_MOD);
                transportModeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                transportModeCode.setListURI(IUBLConfig.URI_CATALOG_18);
                transportModeCode.setValue(transaccionGuiaRemision.getModalidadTraslado());
                shipmentStage.setTransportModeCode(transportModeCode);

                /* <cac:Shipment><cac:ShipmentStage><cac:TransitPeriod> */
                /* <cac:Shipment><cac:ShipmentStage><cac:TransitPeriod> */
                if (transaccionGuiaRemision.getFechaInicioTraslado() != null) {
                    PeriodType transitPeriod = new PeriodType();
                    transitPeriod.setStartDate(getStartDate(transaccionGuiaRemision.getFechaInicioTraslado()));
                    shipmentStage.setTransitPeriod(transitPeriod);
                }


                if (transaccionGuiaRemision.getModalidadTraslado() != null && transaccionGuiaRemision.getModalidadTraslado().equalsIgnoreCase("01")) {

                    /* <cac:Shipment><cac:ShipmentStage><cac:CarrierParty> */
                    PartyType carrierParty = new PartyType();
                    /* <cac:Shipment><cac:ShipmentStage><cac:CarrierParty><cac:PartyIdentification> */
                    PartyIdentificationType partyIdentification = new PartyIdentificationType();
                    IDType id = new IDType();
                    id.setSchemeID(transaccionGuiaRemision.getTipoDOCTransportista()); /**VALOR VIENE DE BASE DE DATOS*/
                    id.setSchemeName(IUBLConfig.GUIAS_SCHEMA_NAME);
                    id.setSchemeAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                    id.setSchemeURI(IUBLConfig.URI_CATALOG_06);
                    id.setValue(transaccionGuiaRemision.getRUCTransporista());/**VALOR VIENE DE BASE DE DATOS*/
                    partyIdentification.setID(id);
                    carrierParty.getPartyIdentification().add(partyIdentification);

                    /* <cac:Shipment><cac:ShipmentStage><cac:CarrierParty><cac:PartyLegalEntity> */
                    PartyLegalEntityType partyLegalEntity = new PartyLegalEntityType();

                    RegistrationNameType registrationName = new RegistrationNameType();
                    registrationName.setValue(transaccionGuiaRemision.getNombreRazonTransportista());/**VALOR VIENE DE BASE DE DATOS*/
                    partyLegalEntity.setRegistrationName(registrationName);

                    CompanyIDType companyID = new CompanyIDType();
                    companyID.setValue(transaccionGuiaRemision.getNroRegistroMTC());
                    partyLegalEntity.setCompanyID(companyID);

                    carrierParty.getPartyLegalEntity().add(partyLegalEntity);
                    shipmentStage.getCarrierParty().add(carrierParty);
                }

                /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson> */
                if (transaccionGuiaRemision.getNombreApellidosConductor() != null && !transaccionGuiaRemision.getNombreApellidosConductor().isEmpty()) {

                    /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson><cbc:ID> */
                    PersonType driverPerson = new PersonType();
                    IDType id = new IDType();
                    id.setValue(transaccionGuiaRemision.getDocumentoConductor());
                    id.setSchemeID(transaccionGuiaRemision.getTipoDocConductor());
                    id.setSchemeName(IUBLConfig.GUIAS_SCHEMA_NAME);
                    id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                    id.setSchemeURI(IUBLConfig.URI_CATALOG_06);
                    driverPerson.setID(id);

                    /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson><cbc:FirstName> */
                    FirstNameType firstName = new FirstNameType();
                    firstName.setValue(transaccionGuiaRemision.getNombreApellidosConductor());
                    driverPerson.setFirstName(firstName);

                    /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson><cbc:FamilyName> */
                    FamilyNameType familyName = new FamilyNameType();
                    familyName.setValue(transaccionGuiaRemision.getNombreApellidosConductor());
                    driverPerson.setFamilyName(familyName);

                    /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson><cbc:JobTitle> */
                    JobTitleType jobTitle = new JobTitleType();
                    jobTitle.setValue("Principal");
                    driverPerson.setJobTitle(jobTitle);

                    /* <cac:Shipment><cac:ShipmentStage><cac:DriverPerson><cac:IdentityDocumentReference><cbc:ID> */
                    IdentityDocumentReferenceType identityDocumentReference = new IdentityDocumentReferenceType();
                    IDType idd = new IDType();
                    idd.setValue(transaccionGuiaRemision.getLicenciaConductor());
                    identityDocumentReference.setID(idd);
                    driverPerson.setIdentityDocumentReference(identityDocumentReference);

                    shipmentStage.getDriverPerson().add(driverPerson);
                }
            }
            shipment.getShipmentStage().add(shipmentStage);


            /* <cac:Shipment><cac:Delivery>*/
            DeliveryType delivery = new DeliveryType();
            /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress>*/
            AddressType deliveryAddress = new AddressType();


            if (transaccion.getTransactionGuias().getCodigoMotivo() != null && (transaccion.getTransactionGuias().getCodigoMotivo().equals("02") || transaccion.getTransactionGuias().getCodigoMotivo().equals("08"))) {

                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cbc:ID> */
                IDType idDelivery = new IDType();
                idDelivery.setSchemeName("Ubigeos");
                idDelivery.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                idDelivery.setValue(transaccionGuiaRemision.getUbigeoPartida());
                deliveryAddress.setID(idDelivery);

                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine> */
                AddressLineType addressLine = new AddressLineType();
                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine><cbc:Line> */
                LineType lineType = new LineType();
                lineType.setValue(transaccionGuiaRemision.getDireccionPartida());
                addressLine.setLine(lineType);
                deliveryAddress.getAddressLine().add(addressLine);

                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cbc:AddressTypeCode> */
                AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
                addressTypeCode.setListID(transaccion.getDocIdentidad_Nro());
                addressTypeCode.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                addressTypeCode.setListName(IUBLConfig.ESTABLECIMIENTOS_GUIAS);
                addressTypeCode.setValue("0");
                deliveryAddress.setAddressTypeCode(addressTypeCode);

            } else {
                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cbc:ID> */
                IDType idDelivery = new IDType();
                idDelivery.setSchemeName("Ubigeos");
                idDelivery.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                idDelivery.setValue(transaccionGuiaRemision.getUbigeoLlegada());
                deliveryAddress.setID(idDelivery);

                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine> */
                AddressLineType addressLine = new AddressLineType();
                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine><cbc:Line> */
                LineType lineType = new LineType();
                lineType.setValue(transaccionGuiaRemision.getDireccionLlegada());
                addressLine.setLine(lineType);
                deliveryAddress.getAddressLine().add(addressLine);

                if (transaccion.getSN_DocIdentidad_Tipo().equals("6")) {
                    AddressTypeCodeType addressTypeCode = new AddressTypeCodeType();
                    addressTypeCode.setListID(transaccion.getSN_DocIdentidad_Nro());
                    addressTypeCode.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                    addressTypeCode.setListName(IUBLConfig.ESTABLECIMIENTOS_GUIAS);
                    addressTypeCode.setValue("0");
                    deliveryAddress.setAddressTypeCode(addressTypeCode);
                }


            }


            delivery.setDeliveryAddress(deliveryAddress);

            /* <cac:Shipment><cac:Delivery><cac:Despatch>*/
            DespatchType despatch = new DespatchType();

            /* <cac:Shipment><cac:Delivery><cac:Despatch>*/
            /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress>*/
            AddressType despatchAddress = new AddressType();

            if (transaccion.getTransactionGuias().getCodigoMotivo() !=null && (transaccion.getTransactionGuias().getCodigoMotivo().equals("02") || transaccion.getTransactionGuias().getCodigoMotivo().equals("08"))) {
                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:ID>*/
                IDType idDespatch = new IDType();
                idDespatch.setSchemeName("Ubigeos");
                idDespatch.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                idDespatch.setValue(transaccionGuiaRemision.getUbigeoLlegada());

                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:AddressTypeCode>*/
                AddressTypeCodeType addressTypeCodeDespatche = new AddressTypeCodeType();
                addressTypeCodeDespatche.setListID(transaccion.getSN_DocIdentidad_Nro());
                addressTypeCodeDespatche.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                addressTypeCodeDespatche.setListName(IUBLConfig.ESTABLECIMIENTOS_GUIAS);
                addressTypeCodeDespatche.setValue("0");

                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:AddressLine>*/
                AddressLineType addressLineDespatch = new AddressLineType();
                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine><cbc:Line> */
                LineType lineTypeDespatch = new LineType();
                lineTypeDespatch.setValue(transaccionGuiaRemision.getDireccionLlegada());
                addressLineDespatch.setLine(lineTypeDespatch);

                despatchAddress.setID(idDespatch);
                despatchAddress.setAddressTypeCode(addressTypeCodeDespatche);
                despatchAddress.getAddressLine().add(addressLineDespatch);

            } else {


                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:ID>*/
                IDType idDespatch = new IDType();
                idDespatch.setSchemeName("Ubigeos");
                idDespatch.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
                idDespatch.setValue(transaccionGuiaRemision.getUbigeoPartida());

                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:AddressTypeCode>*/
                AddressTypeCodeType addressTypeCodeDespatche = new AddressTypeCodeType();
                addressTypeCodeDespatche.setListID(transaccion.getDocIdentidad_Nro());
                addressTypeCodeDespatche.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                addressTypeCodeDespatche.setListName(IUBLConfig.ESTABLECIMIENTOS_GUIAS);
                addressTypeCodeDespatche.setValue("0");

                /* <cac:Shipment><cac:Delivery><cac:Despatch><cac:DespatchAddress><cbc:AddressLine>*/
                AddressLineType addressLineDespatch = new AddressLineType();
                /* <cac:Shipment><cac:Delivery><cac:DeliveryAddress><cac:AddressLine><cbc:Line> */
                LineType lineTypeDespatch = new LineType();
                lineTypeDespatch.setValue(transaccionGuiaRemision.getDireccionPartida());
                addressLineDespatch.setLine(lineTypeDespatch);

                despatchAddress.setID(idDespatch);
                despatchAddress.setAddressTypeCode(addressTypeCodeDespatche);
                despatchAddress.getAddressLine().add(addressLineDespatch);
            }
            ///
            despatch.setDespatchAddress(despatchAddress);
            delivery.setDespatch(despatch);
            shipment.setDelivery(delivery);

            /* <cac:Shipment><cac:TransportHandlingUnit> */
            TransportHandlingUnitType transportHandlingUnit = new TransportHandlingUnitType();


            if (transaccionGuiaRemision.getPlacaVehiculo() != null && !transaccionGuiaRemision.getPlacaVehiculo().isEmpty()) {
                /* <cac:Shipment><cac:TransportHandlingUnit><cac:TransportEquipment> */
                TransportEquipmentType transportEquipment = new TransportEquipmentType();
                /* <cac:Shipment><cac:TransportHandlingUnit><cac:TransportEquipment><cbc:ID> */
                IDType idEquipment = new IDType();
                idEquipment.setValue(transaccionGuiaRemision.getPlacaVehiculo());
                transportEquipment.setID(idEquipment);

                if (transaccionGuiaRemision.getTarjetaCirculacion() != null && !transaccionGuiaRemision.getTarjetaCirculacion().isEmpty()) {/* <cac:Shipment><cac:TransportHandlingUnit><cac:TransportEquipment><cac:ApplicableTransportMeans> */
                    TransportMeansType applicableTransportMeans = new TransportMeansType();
                    /* <cac:Shipment><cac:TransportHandlingUnit><cac:TransportEquipment><cac:ApplicableTransportMeans><cbc:RegistrationNationalityID> */
                    RegistrationNationalityIDType registrationNationalityID = new RegistrationNationalityIDType();
                    registrationNationalityID.setValue(transaccionGuiaRemision.getTarjetaCirculacion());
                    applicableTransportMeans.setRegistrationNationalityID(registrationNationalityID);
                    transportEquipment.setApplicableTransportMeans(applicableTransportMeans);
                }
                transportHandlingUnit.getTransportEquipment().add(transportEquipment);
            }


            if (transaccionGuiaRemision.getCodigoMotivo() != null && ( transaccionGuiaRemision.getCodigoMotivo().equals("08") || (transaccionGuiaRemision.getCodigoMotivo().equals("09")))) {
                if (transaccionGuiaRemision.getNumeroContenedor() != null && !transaccionGuiaRemision.getNumeroContenedor().isEmpty()) {
                    transportHandlingUnit.getPackage().add(getPackage(transaccionGuiaRemision.getNumeroContenedor(), transaccionGuiaRemision.getNumeroPrecinto()));
                }
                if (transaccionGuiaRemision.getNumeroContenedor2() != null && !transaccionGuiaRemision.getNumeroContenedor2().isEmpty()) {
                    transportHandlingUnit.getPackage().add(getPackage(transaccionGuiaRemision.getNumeroContenedor2(), transaccionGuiaRemision.getNumeroPrecinto2()));
                }
            }
            shipment.getTransportHandlingUnit().add(transportHandlingUnit);


            //</cac:Shipment><cac:FirstArrivalPortLocation>

            if (transaccionGuiaRemision.getCodigoMotivo() != null && ( transaccionGuiaRemision.getCodigoMotivo().equals("08") || (transaccionGuiaRemision.getCodigoMotivo().equals("09")))) {
                if (transaccionGuiaRemision.getCodigoPuerto() != null && !transaccionGuiaRemision.getCodigoPuerto().isEmpty()) {
                    shipment.setFirstArrivalPortLocation(getFirstArrivalPortLocation("Puertos", transaccionGuiaRemision.getCodigoPuerto(), "1", transaccionGuiaRemision.getDescripcionPuerto()));
                }

                if (transaccionGuiaRemision.getCodigoAereopuerto() != null && !transaccionGuiaRemision.getCodigoAereopuerto().isEmpty()) {
                    shipment.setFirstArrivalPortLocation(getFirstArrivalPortLocation("Aereopuerto", transaccionGuiaRemision.getCodigoAereopuerto(), "2", transaccionGuiaRemision.getDescripcionAereopuerto()));
                }
            }

        } catch (Exception e) {
            logger.error("getShipment() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_366.getMessage());
            logger.error("getShipment() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_366);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getShipment() [" + this.identifier + "]");
        }
        return shipment;
    } //getShipment

    protected PackageType getPackage(String numeroContenedor, String numeroPrecinto) {
        /* <cac:Shipment><cac:TransportHandlingUnit><cac:Package>*/
        PackageType packageT = new PackageType();
        /* <cac:Shipment><cac:TransportHandlingUnit><cac:Package><cbc:ID>*/
        IDType idPackage = new IDType();
        idPackage.setValue(numeroContenedor);
        packageT.setID(idPackage);
        /* <cac:Shipment><cac:TransportHandlingUnit><cac:Package><cbc:TraceID>*/
        TraceIDType traceIDT = new TraceIDType();
        traceIDT.setValue(numeroPrecinto);
        packageT.setTraceID(traceIDT);

        return packageT;

        //transportHandlingUnit.getActualPackage().add(packageT);
    }

    protected LocationType getFirstArrivalPortLocation(String nameSchema, String codigo, String locationType, String descriccion) {

        /* <cac:Shipment><cac:FirstArrivalPortLocation>*/
        LocationType firstArrivalPortLocation = new LocationType();

        /* <cac:Shipment><cac:FirstArrivalPortLocation><cbc:ID>*/
        IDType idTypeLocation = new IDType();
        idTypeLocation.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
        idTypeLocation.setSchemeName(nameSchema);
        idTypeLocation.setSchemeURI(IUBLConfig.URI_CATALOG_63);
        idTypeLocation.setValue(codigo);
        firstArrivalPortLocation.setID(idTypeLocation);

        /* <cac:Shipment><cac:FirstArrivalPortLocation><cbc:LocationTypeCode>*/
        LocationTypeCodeType locationTypeCode = new LocationTypeCodeType();
        locationTypeCode.setValue(locationType);
        firstArrivalPortLocation.setLocationTypeCode(locationTypeCode);

        /* <cac:Shipment><cac:FirstArrivalPortLocation><cbc:Name></*/
        NameType Name = new NameType();
        Name.setValue(descriccion);
        firstArrivalPortLocation.setName(Name);

        return firstArrivalPortLocation;

    }

    protected AllowanceChargeType getAllowanceCharge(BigDecimal docImporteTotal, BigDecimal montoAnticipo, boolean chargeIndicatorValue, BigDecimal multiplierFactorNumericValue, BigDecimal amountValue, BigDecimal baseAmountValue,
                                                     String currencyCode, String discountReason, BigDecimal montoRetencion, BigDecimal getDOCMontoTotal) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllowanceCharge() [" + this.identifier + "]");
        }
        AllowanceChargeType allowanceCharge = new AllowanceChargeType();

        try {
            /* <cac:AllowanceCharge><cbc:ChargeIndicator> */
            ChargeIndicatorType chargeIndicator = new ChargeIndicatorType();
            chargeIndicator.setValue(chargeIndicatorValue);
            allowanceCharge.setChargeIndicator(chargeIndicator);

            /* <cac:AllowanceCharge><cbc:AllowanceChargeReasonCode> */
            AllowanceChargeReasonCodeType allowanceChargeReasonCode = new AllowanceChargeReasonCodeType();
            allowanceChargeReasonCode.setValue(discountReason);
            allowanceChargeReasonCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            allowanceChargeReasonCode.setListName("Cargo/descuento");
            allowanceChargeReasonCode.setListURI(IUBLConfig.URI_CATALOG_53);
            allowanceCharge.setAllowanceChargeReasonCode(allowanceChargeReasonCode);

            if (discountReason.equals("04")) { //anticipos
                //Valor en duro "anticipos"
                AllowanceChargeReasonType allowanceChargeReasonType = new AllowanceChargeReasonType();
                allowanceChargeReasonType.setValue("Anticipos");
                allowanceCharge.setAllowanceChargeReason(allowanceChargeReasonType);

                /* <cac:AllowanceCharge><cbc:MultiplierFactorNumeric> */
                MultiplierFactorNumericType multiplierFactorNumeric = new MultiplierFactorNumericType();

                BigDecimal var = new BigDecimal("1");
                multiplierFactorNumeric.setValue(var);
                allowanceCharge.setMultiplierFactorNumeric(multiplierFactorNumeric);

                /* <cac:AllowanceCharge><cbc:Amount> */
                AmountType amount = new AmountType();
                amount.setValue(montoAnticipo.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_AMOUNT, RoundingMode.HALF_UP));
                amount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setAmount(amount);

                /* <cac:AllowanceCharge><cbc:BaseAmount> */
                BaseAmountType baseAmount = new BaseAmountType();
                baseAmount.setValue(montoAnticipo.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_BASEAMOUNT, RoundingMode.HALF_UP));
                baseAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setBaseAmount(baseAmount);
            } else if (discountReason.equals("02")) {//descuento global

                /* <cac:AllowanceCharge><cbc:MultiplierFactorNumeric> */
                MultiplierFactorNumericType multiplierFactorNumeric = new MultiplierFactorNumericType();

                multiplierFactorNumeric.setValue(multiplierFactorNumericValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_MULTIPLIERFACTORNUMERIC, RoundingMode.HALF_UP));
                allowanceCharge.setMultiplierFactorNumeric(multiplierFactorNumeric);

                /* <cac:AllowanceCharge><cbc:Amount> */
                AmountType amount = new AmountType();
                amount.setValue(amountValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_AMOUNT, RoundingMode.HALF_UP));
                amount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setAmount(amount);

                /* <cac:AllowanceCharge><cbc:BaseAmount> */
                BaseAmountType baseAmount = new BaseAmountType();
                baseAmount.setValue((amountValue.add(docImporteTotal)).setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_BASEAMOUNT, RoundingMode.HALF_UP));
                baseAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setBaseAmount(baseAmount);
            } else if (discountReason.equals("00")) { // descuento lineal
                /* <cac:AllowanceCharge><cbc:MultiplierFactorNumeric> */
                MultiplierFactorNumericType multiplierFactorNumeric = new MultiplierFactorNumericType();
                multiplierFactorNumeric.setValue(multiplierFactorNumericValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_MULTIPLIERFACTORNUMERIC, RoundingMode.HALF_UP));
                allowanceCharge.setMultiplierFactorNumeric(multiplierFactorNumeric);

                /* <cac:AllowanceCharge><cbc:Amount> */
                AmountType amount = new AmountType();
                amount.setValue(amountValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_AMOUNT, RoundingMode.HALF_UP));
                amount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setAmount(amount);

                /* <cac:AllowanceCharge><cbc:BaseAmount> */
                BaseAmountType baseAmount = new BaseAmountType();
                baseAmount.setValue(baseAmountValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_BASEAMOUNT, RoundingMode.HALF_UP));
                baseAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setBaseAmount(baseAmount);

            } else if (discountReason.equals("62")) {
                MultiplierFactorNumericType multiplierFactorNumeric = new MultiplierFactorNumericType();
                //multiplierFactorNumeric.setValue(multiplierFactorNumericValue.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_MULTIPLIERFACTORNUMERIC, RoundingMode.HALF_UP));
                BigDecimal factorFix = new BigDecimal("0.03");
                multiplierFactorNumeric.setValue(factorFix);
                allowanceCharge.setMultiplierFactorNumeric(multiplierFactorNumeric);

                /* <cac:AllowanceCharge><cbc:Amount> */
                AmountType amount = new AmountType();
                amount.setValue(montoRetencion.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_AMOUNT, RoundingMode.HALF_UP));
                amount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setAmount(amount);

                /* <cac:AllowanceCharge><cbc:BaseAmount> */
                BaseAmountType baseAmount = new BaseAmountType();
                baseAmount.setValue(getDOCMontoTotal.setScale(IUBLConfig.DECIMAL_ALLOWANCECHARGE_BASEAMOUNT, RoundingMode.HALF_UP));
                baseAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                allowanceCharge.setBaseAmount(baseAmount);
            }


        } catch (Exception e) {
            logger.error("getAllowanceCharge() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_327.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_327);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllowanceCharge() [" + this.identifier + "]");
        }
        return allowanceCharge;
    } //getAllowanceCharge

    protected TaxTotalType getTaxTotalV21(TransacctionDTO transaccion, List<TransactionImpuestosDTO> transaccionImpuestos, BigDecimal impuestoTotal, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getTaxTotalV21() [" + this.identifier + "]");
        }

        if (null == transaccionImpuestos || transaccionImpuestos.isEmpty()) {
            logger.error("getTaxTotalV21() [" + this.identifier + "] " + IVenturaError.ERROR_316.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_316);
        }

        TaxTotalType taxTotal = new TaxTotalType();
        boolean contieneGratificacion;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        BigDecimal totalTaxableAmount = BigDecimal.ZERO;
        List<TransactionLineasDTO> transaccionLineas = transaccion.getTransactionLineasDTOList();
        for (TransactionLineasDTO transaccionLinea : transaccionLineas) {
            List<TransactionLineasImpuestoDTO> transaccionLineaImpuestos = transaccionLinea.getTransactionLineasImpuestoListDTO();
            for (TransactionLineasImpuestoDTO lineaImpuesto : transaccionLineaImpuestos) {
                if (IUBLConfig.TAX_TOTAL_GRA_NAME.equalsIgnoreCase(lineaImpuesto.getNombre())) {
                    totalTaxAmount = totalTaxAmount.add(lineaImpuesto.getMonto());
                    totalTaxableAmount = totalTaxableAmount.add(lineaImpuesto.getValorVenta());
                }
            }
        }

        try {
            /* <cac:TaxTotal><cbc:TaxAmount> */
            if (logger.isDebugEnabled()) {
                logger.debug("getTaxTotalV21() [" + this.identifier + "] Agregando IMPUESTO_TOTAL(" + impuestoTotal + ") - TAG TaxAmount.");
            }
            for (TransactionImpuestosDTO transaccionImpuesto : transaccionImpuestos) {
                // System.out.println(transaccionImpuesto.getNombre());
            }
            ArrayList<TransactionImpuestosDTO> impuestos = new ArrayList<>(transaccionImpuestos);
            Optional<TransactionImpuestosDTO> impuestosOptional = impuestos.stream().filter(impuesto -> impuesto.getNombre().equalsIgnoreCase("ICBPER")).findAny();
            TaxAmountType taxAmountTotal = new TaxAmountType();
            taxAmountTotal.setValue(Utils.round(impuestoTotal, 2));
            taxAmountTotal.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            taxTotal.setTaxAmount(taxAmountTotal);
            /*impuestosOptional.ifPresent(impuesto -> {
                taxAmountTotal.setValue(impuestoTotal.add(impuesto.getValorVenta()).setScale(2, RoundingMode.HALF_UP));
                taxTotal.setTaxAmount(taxAmountTotal);
            });*/

            for (TransactionImpuestosDTO transaccionImpuesto : transaccionImpuestos) {
                TaxSubtotalType taxSubtotal = new TaxSubtotalType();
                contieneGratificacion = IUBLConfig.TAX_TOTAL_GRA_NAME.equalsIgnoreCase(transaccionImpuesto.getNombre());
                boolean isImpuestoBolsa = IUBLConfig.TAX_TOTAL_BPT_NAME.equalsIgnoreCase(transaccionImpuesto.getNombre());
                /* <cac:TaxTotal><cac:TaxSubtotal><cbc:TaxableAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getTaxTotalV21() [" + this.identifier + "] Agregando VALOR_VENTA(" + transaccionImpuesto.getValorVenta() + ") MONEDA(" + transaccionImpuesto.getMoneda() + ") - TAG TaxableAmount.");
                }
                TaxableAmountType taxableAmount = new TaxableAmountType();
                if (contieneGratificacion)
                    taxableAmount.setValue(totalTaxableAmount.setScale(2, RoundingMode.HALF_UP));
                else {
                    taxableAmount.setValue(transaccionImpuesto.getValorVenta().setScale(2, RoundingMode.HALF_UP));
                    //taxableAmount.setValue((transaccion.getDOCImporteTotal().subtract(transaccion.getANTICIPOMonto())).setScale(2, RoundingMode.HALF_UP));
                }
                taxableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionImpuesto.getMoneda()).value());
                taxSubtotal.setTaxableAmount(taxableAmount);

                /* <cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getTaxTotalV21() [" + this.identifier + "] Agregando MONTO(" + transaccionImpuesto.getMonto() + ") MONEDA(" + transaccionImpuesto.getMoneda() + ") - TAG TaxAmount.");
                }
                TaxAmountType taxAmount = new TaxAmountType();
                if (contieneGratificacion) {
                    taxAmount.setValue(Utils.round(totalTaxAmount, 2));
                } else if (isImpuestoBolsa) {
                    taxAmount.setValue(Utils.round(transaccionImpuesto.getValorVenta(), 2));
                } else {
                    taxAmount.setValue(Utils.round(transaccionImpuesto.getMonto(), 2));
                }
                taxAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionImpuesto.getMoneda()).value());
                taxSubtotal.setTaxAmount(taxAmount);

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory> */
                TaxCategoryType taxCategory = new TaxCategoryType();

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cbc:ID> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getTaxTotalV21() [" + this.identifier + "] Agregando CODIGO(" + transaccionImpuesto.getCodigo() + ") - TAG TaxCategory_ID.");
                }
                IDType idTaxCategory = new IDType();
                idTaxCategory.setValue(transaccionImpuesto.getCodigo());
                idTaxCategory.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_UNECE);
                idTaxCategory.setSchemeID("UN/ECE 5305");
                idTaxCategory.setSchemeName("Tax Category Identifier");
                taxCategory.setID(idTaxCategory);

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme> */
                TaxSchemeType taxScheme = new TaxSchemeType();

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:ID> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getTaxTotalV21() [" + this.identifier + "] Agregando TIPO_TRIBUTO(" + transaccionImpuesto.getTipoTributo() + ") - TAG TaxCategory_TaxScheme_ID.");
                }
                IDType idTaxScheme = new IDType();
                idTaxScheme.setValue(transaccionImpuesto.getTipoTributo());
                idTaxScheme.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                idTaxScheme.setSchemeName("Codigo de tributos");
                idTaxScheme.setSchemeURI(IUBLConfig.URI_CATALOG_05);
                taxScheme.setID(idTaxScheme);

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:Name> */
                NameType name = new NameType();
                name.setValue(transaccionImpuesto.getNombre());
                taxScheme.setName(name);

                /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:TaxTypeCode> */
                TaxTypeCodeType taxTypeCode = new TaxTypeCodeType();
                taxTypeCode.setValue(transaccionImpuesto.getAbreviatura());
                taxScheme.setTaxTypeCode(taxTypeCode);
                taxCategory.setTaxScheme(taxScheme);
                taxSubtotal.setTaxCategory(taxCategory);
                /* Agregar TAG TaxSubtotalType */
                taxTotal.getTaxSubtotal().add(taxSubtotal);
            } //for
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getTaxTotalV21() [" + this.identifier + "] " + IVenturaError.ERROR_317.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_317);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getTaxTotalV21() [" + this.identifier + "]");
        }
        return taxTotal;
    } //getTaxTotalV21

    protected MonetaryTotalType getMonetaryTotal(final TransacctionDTO transaccion, final BigDecimal lineExtensionAmountValue, final BigDecimal taxInclusiveAmountValue, final boolean noContainsFreeItem, final BigDecimal chargeTotalAmountValue, final BigDecimal prepaidAmountValue, final BigDecimal payableAmountValue, final BigDecimal descuento, final String currencyCode, final boolean isInvoiceOrBoleta) throws UBLDocumentException {
        final boolean bandera = false;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("+getMonetaryTotal() [" + this.identifier + "]");
        }
        if (null == lineExtensionAmountValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_335);
        }
        if (null == taxInclusiveAmountValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_370);
        }
        if (null == payableAmountValue) {
            throw new UBLDocumentException(IVenturaError.ERROR_321);
        }
        final MonetaryTotalType monetaryTotal = new MonetaryTotalType();
        if (isInvoiceOrBoleta && noContainsFreeItem) {
            final LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
            lineExtensionAmount.setValue(lineExtensionAmountValue.setScale(2, RoundingMode.HALF_UP));
            lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            monetaryTotal.setLineExtensionAmount(lineExtensionAmount);
            final TaxInclusiveAmountType taxInclusiveAmount = new TaxInclusiveAmountType();
            final BigDecimal b2 = new BigDecimal("1.18");
            if (transaccion.getANTICIPO_Monto().intValue() > 0) {
                taxInclusiveAmount.setValue(lineExtensionAmountValue.multiply(b2).setScale(2, RoundingMode.HALF_UP));
                taxInclusiveAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            } else if (transaccion.getTransactionImpuestosDTOList() != null) {
                boolean existIGV = false;
                for (int i = 0; i < transaccion.getTransactionImpuestosDTOList().size(); ++i) {
                    if (transaccion.getTransactionImpuestosDTOList().get(i).getNombre().equals("IGV")) {
                        existIGV = true;
                        taxInclusiveAmount.setValue(lineExtensionAmountValue.add(transaccion.getTransactionImpuestosDTOList().get(i).getMonto()).setScale(2, RoundingMode.HALF_UP));
                        taxInclusiveAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    }
                }
                if (!existIGV) {
                    taxInclusiveAmount.setValue(taxInclusiveAmountValue.setScale(2, RoundingMode.HALF_UP));
                    taxInclusiveAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                }
            }
            if (transaccion.getTransactionImpuestosDTOList() != null) {
                for (int j = 0; j < transaccion.getTransactionImpuestosDTOList().size(); ++j) {
                    if (transaccion.getTransactionImpuestosDTOList().get(j).getNombre().equals("ISC")) {
                        taxInclusiveAmount.setValue(lineExtensionAmountValue.add(transaccion.getTransactionImpuestosDTOList().get(j).getMonto()).multiply(b2).setScale(2, RoundingMode.HALF_UP));
                        taxInclusiveAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                    }
                }
            }
            monetaryTotal.setTaxInclusiveAmount(taxInclusiveAmount);
        }
        if (null != chargeTotalAmountValue && chargeTotalAmountValue.compareTo(BigDecimal.ZERO) > 0) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("getMonetaryTotal() [" + this.identifier + "] La transaccion tiene un TOTAL DE OTROS CARGOS.");
            }
            final ChargeTotalAmountType chargeTotalAmount = new ChargeTotalAmountType();
            chargeTotalAmount.setValue(chargeTotalAmountValue.setScale(2, RoundingMode.HALF_UP));
            chargeTotalAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            monetaryTotal.setChargeTotalAmount(chargeTotalAmount);
        }
        if (null != prepaidAmountValue && prepaidAmountValue.compareTo(BigDecimal.ZERO) > 0) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("getMonetaryTotal() [" + this.identifier + "] La transaccion tiene un TOTAL DE ANTICIPOS.");
            }
            final PrepaidAmountType prepaidAmount = new PrepaidAmountType();
            final BigDecimal b3 = new BigDecimal("1.18");
            prepaidAmount.setValue(prepaidAmountValue.multiply(b3).setScale(2, RoundingMode.HALF_UP));
            prepaidAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            monetaryTotal.setPrepaidAmount(prepaidAmount);
        }
        if (transaccion.getTransactionPropertiesDTOList().size() == 2) {
            TransactionPropertiesDTO propiedad1 = transaccion.getTransactionPropertiesDTOList().get(0);
            TransactionPropertiesDTO propiedad2 = transaccion.getTransactionPropertiesDTOList().get(1);

            boolean descripcion1ContieneGratuita = propiedad1 != null && propiedad1.getDescription() != null && propiedad1.getDescription().contains("GRATUITA");
            boolean descripcion2ContieneGratuita = propiedad2 != null && propiedad2.getDescription() != null && propiedad2.getDescription().contains("GRATUITA");

            if (descripcion1ContieneGratuita || descripcion2ContieneGratuita) {
                final LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
                lineExtensionAmount.setValue(lineExtensionAmountValue.setScale(2, RoundingMode.HALF_UP));
                lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                monetaryTotal.setLineExtensionAmount(lineExtensionAmount);
                final TaxInclusiveAmountType taxInclusiveAmount = new TaxInclusiveAmountType();
                taxInclusiveAmount.setValue(lineExtensionAmountValue.setScale(2, RoundingMode.HALF_UP));
                taxInclusiveAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
                monetaryTotal.setTaxInclusiveAmount(taxInclusiveAmount);
            }

        }
        final PayableAmountType payableAmount = new PayableAmountType();
        payableAmount.setValue(payableAmountValue.setScale(2, RoundingMode.HALF_UP));
        payableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
        monetaryTotal.setPayableAmount(payableAmount);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("-getMonetaryTotal() [" + this.identifier + "]");
        }
        return monetaryTotal;
    }

    protected InvoicedQuantityType getInvoicedQuantity(BigDecimal value, String unitCodeValue) throws Exception {
        InvoicedQuantityType invoicedQuantity = new InvoicedQuantityType();
        invoicedQuantity.setValue(value.setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
        invoicedQuantity.setUnitCode(unitCodeValue);
        invoicedQuantity.setUnitCodeListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
        invoicedQuantity.setUnitCodeListID("UN/ECE rec 20");

        return invoicedQuantity;
    } //getInvoicedQuantity

    protected CreditedQuantityType getCreditedQuantity(BigDecimal value, String unitCodeValue) throws Exception {
        CreditedQuantityType creditedQuantity = new CreditedQuantityType();
        creditedQuantity.setValue(value.setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
        creditedQuantity.setUnitCode(unitCodeValue);
        creditedQuantity.setUnitCodeListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
        creditedQuantity.setUnitCodeListID("UN/ECE rec 20");

        return creditedQuantity;
    } //getCreditedQuantity

    protected DebitedQuantityType getDebitedQuantity(BigDecimal value, String unitCodeValue) throws Exception {
        DebitedQuantityType debitedQuantity = new DebitedQuantityType();
        debitedQuantity.setValue(value.setScale(IUBLConfig.DECIMAL_LINE_QUANTITY, RoundingMode.HALF_UP));
        debitedQuantity.setUnitCode(unitCodeValue);
        debitedQuantity.setUnitCodeListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
        debitedQuantity.setUnitCodeListID("UN/ECE rec 20");

        return debitedQuantity;
    } //getDebitedQuantity

    protected DeliveredQuantityType getDeliveredQuantity(BigDecimal value, String unitCodeValue) throws Exception {
        DeliveredQuantityType deliveredQuantity = new DeliveredQuantityType();
        deliveredQuantity.setUnitCode(unitCodeValue);
        deliveredQuantity.setUnitCodeListID("UN/ECE rec 20");
        deliveredQuantity.setUnitCodeListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
        //deliveredQuantity.setValue(value.setScale(0, RoundingMode.HALF_UP));
        deliveredQuantity.setValue(value);
        return deliveredQuantity;
    } //getDeliveredQuantity

    protected LineExtensionAmountType getLineExtensionAmount(BigDecimal lineExtensionAmountValue, String currencyCode) throws Exception {
        LineExtensionAmountType lineExtensionAmount = new LineExtensionAmountType();
        lineExtensionAmount.setValue(lineExtensionAmountValue.setScale(IUBLConfig.DECIMAL_LINE_LINEEXTENSIONAMOUNT, RoundingMode.HALF_UP));
        lineExtensionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());

        return lineExtensionAmount;
    } //getLineExtensionAmount

    protected PricingReferenceType getPricingReference(String priceTypeCodeValue, BigDecimal priceAmountValue, String currencyCode, TransactionLineasDTO linea) {
        PricingReferenceType pricingReference = new PricingReferenceType();

        PriceType alternativeConditionPrice = new PriceType();
        BigDecimal cantidad = linea.getCantidad();
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        List<TransactionLineasImpuestoDTO> lineaImpuestos = linea.getTransactionLineasImpuestoListDTO();
        boolean contieneGratificacion = false;
        for (TransactionLineasImpuestoDTO lineaImpuesto : lineaImpuestos) {
            if (IUBLConfig.TAX_TOTAL_GRA_NAME.equalsIgnoreCase(lineaImpuesto.getNombre())) {
                totalTaxAmount = totalTaxAmount.add(lineaImpuesto.getValorVenta());
                contieneGratificacion = true;
            }
        }
        boolean isTaxTotalNotZero = totalTaxAmount.compareTo(BigDecimal.ZERO) > 0;
        if (contieneGratificacion && isTaxTotalNotZero) {
            priceAmountValue = totalTaxAmount.divide(cantidad, 6, RoundingMode.HALF_EVEN);
        }
        /* <cac:PricingReference><cac:AlternativeConditionPrice><cbc:PriceAmount> */
        PriceAmountType priceAmount = new PriceAmountType();
        priceAmount.setValue(priceAmountValue.setScale(6, RoundingMode.HALF_UP));
        priceAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
        alternativeConditionPrice.setPriceAmount(priceAmount);

        /* <cac:PricingReference><cac:AlternativeConditionPrice><cbc:PriceTypeCode> */
        PriceTypeCodeType priceTypeCode = new PriceTypeCodeType();
        priceTypeCode.setValue(priceTypeCodeValue);
        priceTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
        priceTypeCode.setListName("Tipo de Precio");
        priceTypeCode.setListURI(IUBLConfig.URI_CATALOG_16);
        alternativeConditionPrice.setPriceTypeCode(priceTypeCode);

        pricingReference.getAlternativeConditionPrice().add(alternativeConditionPrice);
        return pricingReference;
    } //getPricingReference

    protected DeliveryType getDeliveryForLine(String codUbigeoDestino, String direccionDestino, String codUbigeoOrigen, String direccionOrigen, String detalleViaje,
                                              BigDecimal valorCargaEfectiva, BigDecimal valorCargaUtil, BigDecimal valorTransporte, String confVehicular, BigDecimal cUtilVehiculo, BigDecimal cEfectivaVehiculo,
                                              BigDecimal valorRefTM, BigDecimal valorPreRef, String factorRetorno) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getDeliveryForLine() [" + this.identifier + "]");
        }
        DeliveryType delivery = new DeliveryType();

        try {
            /* <cac:Delivery><cac:DeliveryLocation> */
            if (StringUtils.isNotBlank(codUbigeoDestino) && StringUtils.isNotBlank(direccionDestino)) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDeliveryForLine() [" + this.identifier + "] Agregar PUNTO DE DESTINO.");
                }
                delivery.setDeliveryLocation(getDeliveryLocationForDelivery(codUbigeoDestino, direccionDestino));
            }

            /* <cac:Delivery><cac:Despatch> */
            if (StringUtils.isNotBlank(codUbigeoOrigen) && StringUtils.isNotBlank(direccionOrigen)) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDeliveryForLine() [" + this.identifier + "] Agregar PUNTO DE ORIGEN.");
                }
                delivery.setDespatch(getDespatchForDelivery(codUbigeoOrigen, direccionOrigen, detalleViaje));
            }

            /*
             * <cac:Delivery><cac:DeliveryTerms>
             *
             * Valor Referencial del Servicio de Transporte
             */
            if (null != valorTransporte && valorTransporte.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDeliveryForLine() [" + this.identifier + "] Agregar VALOR REFERENCIAL DEL SERVICIO DE TRANSPORTE.");
                }
                delivery.getDeliveryTerms().add(getDeliveryTermsForDelivery("01", valorTransporte));
            }

            /*
             * <cac:Delivery><cac:DeliveryTerms>
             *
             * Valor Referencial sobre la Carga Efectiva
             */
            if (null != valorCargaEfectiva && valorCargaEfectiva.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDeliveryForLine() [" + this.identifier + "] Agregar VALOR REFERENCIAL SOBRE LA CARGA EFECTIVA.");
                }
                delivery.getDeliveryTerms().add(getDeliveryTermsForDelivery("02", valorCargaEfectiva));
            }

            /*
             * <cac:Delivery><cac:DeliveryTerms>
             *
             * Valor Referencial sobre la Carga Util Nominal
             */
            if (null != valorCargaUtil && valorCargaUtil.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getDeliveryForLine() [" + this.identifier + "] Agregar VALOR REFERENCIAL SOBRE LA CARGA UTIL NOMINAL.");
                }
                delivery.getDeliveryTerms().add(getDeliveryTermsForDelivery("03", valorCargaUtil));
            }

            /* <cac:Delivery><cac:Shipment> */
            delivery.setShipment(getShipmentForDelivery(confVehicular, cUtilVehiculo, cEfectivaVehiculo, valorRefTM, valorPreRef, factorRetorno));
        } catch (Exception e) {
            logger.error("getDeliveryForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_356.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_356);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getDeliveryForLine() [" + this.identifier + "]");
        }
        return delivery;
    } //getDeliveryForLine

    protected TaxTotalType getTaxTotalLineV21(List<TransactionLineasImpuestoDTO> transaccionLineaImpuestos, BigDecimal impuestoTotalLinea, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getTaxTotalLineV21() [" + this.identifier + "]");
        }
        if (null == transaccionLineaImpuestos || transaccionLineaImpuestos.isEmpty()) {
            logger.error("getTaxTotalLineV21() [" + this.identifier + "] " + IVenturaError.ERROR_322.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_322);
        }
        TaxTotalType taxTotal = new TaxTotalType();
        try {
            /* <cac:TaxTotal><cbc:TaxAmount> */
            if (logger.isDebugEnabled()) {
                logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando IMPUESTO_TOTAL_LINEA(" + impuestoTotalLinea + ") - TAG TaxAmount.");
            }
            BigDecimal taxAmountValue = BigDecimal.ZERO;
            boolean contieneGratificacion = transaccionLineaImpuestos.stream().map(TransactionLineasImpuestoDTO::getNombre).anyMatch(IUBLConfig.TAX_TOTAL_GRT_NAME::equalsIgnoreCase);
//            for (TransaccionLineaImpuestoDTO lineaImpuesto : transaccionLineaImpuestos) {
//                if (IUBLConfig.TAX_TOTAL_GRA_NAME.equalsIgnoreCase(lineaImpuesto.getNombre())) {
//                    contieneGratificacion = true;
//                    break;
//                }
//            }
            if (!contieneGratificacion) {
                taxAmountValue = impuestoTotalLinea;
            }

            TaxAmountType taxAmountTotal = new TaxAmountType();
            taxAmountTotal.setValue(Utils.round(taxAmountValue, 2));
            taxAmountTotal.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            taxTotal.setTaxAmount(taxAmountTotal);

            List<BigDecimal> valoresImpuesto = new ArrayList<>();
            for (TransactionLineasImpuestoDTO lineaImpuesto : transaccionLineaImpuestos) {
                String tributo = lineaImpuesto.getTipoTributo();
                if (tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_IGV_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_ISC_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXP_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_GRT_ID)
                        || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXO_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_INA_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_OTR_ID)) {
                    TaxSubtotalType taxSubtotal = new TaxSubtotalType();

                    BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
                    BigDecimal baseImponible = lineaImpuesto.getValorVenta();
                    BigDecimal porcentajeImpuesto = lineaImpuesto.getPorcentaje();
                    BigDecimal montoImpuesto = lineaImpuesto.getMonto();
                    BigDecimal valorCalculado = baseImponible.multiply(porcentajeImpuesto).divide(ONE_HUNDRED, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal montoImpuestoRedondeado = montoImpuesto;
                    if (valorCalculado.compareTo(montoImpuestoRedondeado) != 0) {
                        montoImpuestoRedondeado = valorCalculado;
                    }

                    /* <cac:TaxTotal><cac:TaxSubtotal><cbc:TaxableAmount> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando VALOR_VENTA(" + lineaImpuesto.getValorVenta() + ") MONEDA(" + lineaImpuesto.getMoneda() + ") - TAG TaxableAmount.");
                    }
                    TaxableAmountType taxableAmount = new TaxableAmountType();
                    taxableAmount.setValue(lineaImpuesto.getValorVenta().setScale(2, RoundingMode.HALF_UP));
                    taxableAmount.setCurrencyID(CurrencyCodeContentType.valueOf(lineaImpuesto.getMoneda()).value());
                    taxSubtotal.setTaxableAmount(taxableAmount);
                    /* <cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando MONTO(" + lineaImpuesto.getMonto() + ") MONEDA(" + lineaImpuesto.getMoneda() + ") - TAG TaxAmount.");
                    }
                    TaxAmountType taxAmount = new TaxAmountType();
                    //taxAmount.setValue(montoImpuestoRedondeado.setScale(2, RoundingMode.HALF_UP));
                    taxAmount.setValue(Utils.round(montoImpuesto, 2));
                    if (!IUBLConfig.TAX_TOTAL_GRT_ID.equalsIgnoreCase(tributo)) {
                        valoresImpuesto.add(montoImpuesto);
                    }
                    taxAmount.setCurrencyID(CurrencyCodeContentType.valueOf(lineaImpuesto.getMoneda()).value());
                    taxSubtotal.setTaxAmount(taxAmount);
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory> */
                    TaxCategoryType taxCategory = new TaxCategoryType();
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando CODIGO(" + lineaImpuesto.getCodigo() + ") - TAG TaxCategory_ID.");
                    }
                    IDType idTaxCategory = new IDType();
                    idTaxCategory.setValue(lineaImpuesto.getCodigo());
                    idTaxCategory.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_UNECE);
                    idTaxCategory.setSchemeID("UN/ECE 5305");
                    idTaxCategory.setSchemeName("Tax Category Identifier");
                    taxCategory.setID(idTaxCategory);
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cbc:Percent> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando PORCENTAJE(" + lineaImpuesto.getPorcentaje() + ") - TAG TaxCategory_Percent.");
                    }
                    PercentType percent = new PercentType();
                    percent.setValue(lineaImpuesto.getPorcentaje().setScale(2, RoundingMode.HALF_UP));
                    taxCategory.setPercent(percent);

                    if (tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_IGV_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXP_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_EXO_ID)
                            || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_INA_ID) || tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_GRT_ID)) {
                        /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cbc:TaxExemptionReasonCode> */
                        if (logger.isDebugEnabled()) {
                            logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando TIPO_AFECTACION(" + lineaImpuesto.getTipoAfectacion() + ") - TAG TaxExemptionReasonCode.");
                        }
                        TaxExemptionReasonCodeType taxExemptionReasonCode = new TaxExemptionReasonCodeType();
                        taxExemptionReasonCode.setValue(lineaImpuesto.getTipoAfectacion());
                        taxExemptionReasonCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
                        taxExemptionReasonCode.setListName("Afectacion del IGV");
                        taxExemptionReasonCode.setListURI(IUBLConfig.URI_CATALOG_07);
                        taxCategory.setTaxExemptionReasonCode(taxExemptionReasonCode);
                    } else if (tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_ISC_ID)) {
                        /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cbc:TierRange> */
                        if (logger.isDebugEnabled()) {
                            logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando TIER_RANGE(" + lineaImpuesto.getTierRange() + ") - TAG TierRange.");
                        }
                        TierRangeType tierRange = new TierRangeType();
                        tierRange.setValue(lineaImpuesto.getTierRange());
                        taxCategory.setTierRange(tierRange);
                    }
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme> */
                    TaxSchemeType taxScheme = new TaxSchemeType();
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:ID> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getTaxTotalLineV21() [" + this.identifier + "] Agregando TIPO_TRIBUTO(" + tributo + ") - TAG TaxCategory_TaxScheme_ID.");
                    }
                    IDType idTaxScheme = new IDType();
                    idTaxScheme.setValue(tributo);
                    idTaxScheme.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                    idTaxScheme.setSchemeName("Codigo de tributos");
                    idTaxScheme.setSchemeURI(IUBLConfig.URI_CATALOG_05);
                    taxScheme.setID(idTaxScheme);
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:Name> */
                    NameType name = new NameType();
                    name.setValue(lineaImpuesto.getNombre());
                    taxScheme.setName(name);
                    /* <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory><cac:TaxScheme><cbc:TaxTypeCode> */
                    TaxTypeCodeType taxTypeCode = new TaxTypeCodeType();
                    taxTypeCode.setValue(lineaImpuesto.getAbreviatura());
                    taxScheme.setTaxTypeCode(taxTypeCode);
                    taxCategory.setTaxScheme(taxScheme);
                    taxSubtotal.setTaxCategory(taxCategory);
                    /* Agregar TAG TaxSubtotalType */
                    taxTotal.getTaxSubtotal().add(taxSubtotal);
                } else if (tributo.equalsIgnoreCase(IUBLConfig.TAX_TOTAL_BPT_ID)) {
                    /**revisar*/
                    BigDecimal cantidad = null;// lineaImpuesto.getTransaccionLineas().getCantidad();

                    TaxSubtotalType taxSubtotal = new TaxSubtotalType();
                    TaxAmountType bolsaTaxAmount = new TaxAmountType();
                    bolsaTaxAmount.setValue(lineaImpuesto.getValorVenta().setScale(2, RoundingMode.HALF_UP));
                    valoresImpuesto.add(lineaImpuesto.getValorVenta());
                    bolsaTaxAmount.setCurrencyID(CurrencyCodeContentType.valueOf(lineaImpuesto.getMoneda()).value());
                    taxSubtotal.setTaxAmount(bolsaTaxAmount);

                    BaseUnitMeasureType baseUnitMeasure = new BaseUnitMeasureType();
                    baseUnitMeasure.setUnitCode(null/*lineaImpuesto.getTransaccionLineas().getUnidadSunat()*/); /**revisar*/
                    baseUnitMeasure.setValue(cantidad.setScale(0, RoundingMode.HALF_UP));

                    taxSubtotal.setBaseUnitMeasure(baseUnitMeasure);

                    TaxCategoryType bolsaTaxCategory = new TaxCategoryType();
                    IDType bolsaIdTaxSchemeCategory = new IDType();
                    bolsaIdTaxSchemeCategory.setSchemeID("UN/ECE 5305");
                    bolsaIdTaxSchemeCategory.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
                    bolsaIdTaxSchemeCategory.setSchemeName("Codigo de tributos");
                    bolsaIdTaxSchemeCategory.setSchemeURI(IUBLConfig.URI_CATALOG_05);
                    bolsaIdTaxSchemeCategory.setValue(tributo);

                    TaxSchemeType bolsaTaxScheme = new TaxSchemeType();
                    bolsaTaxScheme.setID(bolsaIdTaxSchemeCategory);

                    PerUnitAmountType bolsaPerUnitAmountType = new PerUnitAmountType();
                    bolsaPerUnitAmountType.setValue(lineaImpuesto.getMonto().setScale(2, RoundingMode.HALF_UP));
                    bolsaPerUnitAmountType.setCurrencyID(CurrencyCodeContentType.valueOf(lineaImpuesto.getMoneda()).value());
                    bolsaTaxCategory.setPerUnitAmount(bolsaPerUnitAmountType);

                    bolsaTaxCategory.setTaxScheme(bolsaTaxScheme);
                    taxSubtotal.setTaxCategory(bolsaTaxCategory);

                    NameType bolsaName = new NameType();
                    bolsaName.setValue(IUBLConfig.TAX_TOTAL_BPT_NAME);
                    bolsaTaxScheme.setName(bolsaName);

                    TaxTypeCodeType bolsaTaxTypeCode = new TaxTypeCodeType();
                    bolsaTaxTypeCode.setValue(IUBLConfig.TAX_TOTAL_OTR_CODE);
                    bolsaTaxScheme.setTaxTypeCode(bolsaTaxTypeCode);
                    taxTotal.getTaxSubtotal().add(taxSubtotal);
                }
            }
            BigDecimal totalImpuestoLinea = valoresImpuesto.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            TaxAmountType taxAmountTotalBolsa = new TaxAmountType();
            taxAmountTotalBolsa.setValue(totalImpuestoLinea.setScale(2, RoundingMode.HALF_UP));
            taxAmountTotalBolsa.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
            taxTotal.setTaxAmount(taxAmountTotalBolsa);
        } catch (Exception e) {
            logger.error("getTaxTotalLineV21() [" + this.identifier + "] " + IVenturaError.ERROR_323.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_323);
        }
        return taxTotal;
    }

    protected ItemType getItemForLine(TransacctionDTO transaccion, TransactionLineasDTO linea, String descriptionValue, String sellersItemIdentificationIDValue, String commodityClassificationCodeValue, String standardItemIdentificationIDValue,
                                      List<TransactionPropertiesDTO> transaccionPropiedadesList) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getItemForLine() [" + this.identifier + "]");
        }
        if (StringUtils.isBlank(descriptionValue)) {
            throw new UBLDocumentException(IVenturaError.ERROR_325);
        }
        ItemType item = new ItemType();
        try {
            /* Agregar <cac:Item><cbc:Description> */
            DescriptionType description = new DescriptionType();
            description.setValue(descriptionValue);
            item.getDescription().add(description);
            if (StringUtils.isNotBlank(sellersItemIdentificationIDValue)) {
                /* Agregar <cac:Item><cac:SellersItemIdentification> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getItemForLine() [" + this.identifier + "] Agregando el CODIGO DE PRODUCTO.");
                }
                ItemIdentificationType sellersItemIdentification = new ItemIdentificationType();
                IDType id = new IDType();
                id.setValue(sellersItemIdentificationIDValue);
                sellersItemIdentification.setID(id);
                item.setSellersItemIdentification(sellersItemIdentification);
            }
            if (StringUtils.isNotBlank(standardItemIdentificationIDValue)) {
                /* Agregar <cac:Item><cac:StandardItemIdentification> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getItemForLine() [" + this.identifier + "] Agregando el CODIGO DE PRODUCTO GS1.");
                }
                ItemIdentificationType standardItemIdentification = new ItemIdentificationType();
                IDType id = new IDType();
                id.setValue(standardItemIdentificationIDValue);
                id.setSchemeID("GTIN-" + standardItemIdentificationIDValue.length());
                standardItemIdentification.setID(id);
                item.setStandardItemIdentification(standardItemIdentification);
            }
            if (StringUtils.isNotBlank(commodityClassificationCodeValue)) {
                /* Agregar <cac:Item><cac:CommodityClassification/> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getItemForLine() [" + this.identifier + "] Agregando el CODIGO DE PRODUCTO SUNAT.");
                }
                CommodityClassificationType commodityClassification = new CommodityClassificationType();
                ItemClassificationCodeType itemClassificationCode = new ItemClassificationCodeType();
                itemClassificationCode.setValue(commodityClassificationCodeValue);
                itemClassificationCode.setListAgencyName("GS1 US");
                itemClassificationCode.setListID("UNSPSC");
                itemClassificationCode.setListName("Item Classification");
                commodityClassification.setItemClassificationCode(itemClassificationCode);
                item.getCommodityClassification().add(commodityClassification);
            }
            /*for (TransaccionPropiedades transaccionPropiedad : transaccionPropiedadesList) {
                if (transaccionPropiedad.getTransaccionPropiedadesPK().getId().substring(0, 2).equalsIgnoreCase("30")) {
                    item.getAdditionalItemProperty().add(getAdditionalItemProperty(transaccionPropiedad));
                }
            }*/

            if (transaccion.getCodigoDetraccion() != null && transaccion.getCodigoDetraccion().equals("004")) {
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3001", "Detracciones: NUMERO DE CTA EN EL BN"));
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3002", "Detracciones: Recursos Hidrobiológicos-Nombre y matrícula de la embarcación"));
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3003", "Detracciones: Recursos Hidrobiológicos-Tipo y cantidad de especie vendida"));
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3004", "Detracciones: Recursos Hidrobiológicos -Lugar de descarga"));
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3005", "Detracciones: Recursos Hidrobiológicos -Fecha de descarga"));
                item.getAdditionalItemProperty().add(getAdditionalItemProperty(linea, "3006", "Detracciones: Transporte Bienes vía terrestre - Numero Registro MTC"));
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
    } //getItemForLine

    protected ItemType getItemForLineRest(TransactionLineasDTO transaccionLineas) {//String descriptionValue, String sellersItemIdentificationIDValue) {
        ItemType item = new ItemType();
        /* <cac:Item><cbc:Name> */
        DescriptionType description = new DescriptionType();
        description.setValue(transaccionLineas.getDescripcion());
        item.getDescription().add(description);
        /* <cac:Item><cac:SellersItemIdentification> */
        ItemIdentificationType sellersItemIdentification = new ItemIdentificationType();
        IDType id = new IDType();
        id.setValue(transaccionLineas.getCodArticulo());
        sellersItemIdentification.setID(id);
        item.setSellersItemIdentification(sellersItemIdentification);

        /* <cac:Item><cac:StandardItemIdentification> */

        if (StringUtils.isNotBlank(transaccionLineas.getCodProdGS1())) {
            /* Agregar <cac:Item><cac:StandardItemIdentification> */
            ItemIdentificationType standardItemIdentification = new ItemIdentificationType();
            IDType idType = new IDType();
            idType.setSchemeID("GTIN-" + transaccionLineas.getCodProdGS1().length());
            idType.setValue(transaccionLineas.getCodProdGS1());
            standardItemIdentification.setID(idType);
            item.setStandardItemIdentification(standardItemIdentification);

        }

        /* <cac:Item><cac:CommodityClassification> */
        if (StringUtils.isNotBlank(transaccionLineas.getCodSunat())) {
            if (logger.isDebugEnabled()) {
                logger.debug("getItemForLine() [" + this.identifier + "] Agregando el CODIGO DE PRODUCTO SUNAT.");
            }
            CommodityClassificationType commodityClassification = new CommodityClassificationType();
            ItemClassificationCodeType itemClassificationCodeType = new ItemClassificationCodeType();
            itemClassificationCodeType.setListAgencyName("GS1 US");
            itemClassificationCodeType.setListID("UNSPSC");
            itemClassificationCodeType.setListName("Item Classification");
            itemClassificationCodeType.setValue(transaccionLineas.getCodSunat());
            commodityClassification.setItemClassificationCode(itemClassificationCodeType);
            item.getCommodityClassification().add(commodityClassification);
        }


        //</cac:Item><cac:AdditionalItemProperty>
        if (transaccionLineas.getSubPartida() != null && !transaccionLineas.getSubPartida().isEmpty())
            item.getAdditionalItemProperty().add(getAdditionalItemPropertyRest(IUBLConfig.INDICATOR_SUBPART, "7020", transaccionLineas.getSubPartida()));
        //</cac:Item><cac:AdditionalItemProperty>
        if (transaccionLineas.getIndicadorBien() != null && !transaccionLineas.getIndicadorBien().isEmpty())
            item.getAdditionalItemProperty().add(getAdditionalItemPropertyRest(IUBLConfig.INDICATOR_REGULADO_SUNAT, "7022", transaccionLineas.getIndicadorBien()));
        //</cac:Item><cac:AdditionalItemProperty>
        if (transaccionLineas.getNumeracion() != null && !transaccionLineas.getNumeracion().isEmpty())
            item.getAdditionalItemProperty().add(getAdditionalItemPropertyRest(IUBLConfig.INDICATOR_NUMERACION_DAM_DS, "7021", transaccionLineas.getNumeracion()));
        //</cac:Item><cac:AdditionalItemProperty>
        if (transaccionLineas.getNumeroSerie() != null && !transaccionLineas.getNumeroSerie().isEmpty())
            item.getAdditionalItemProperty().add(getAdditionalItemPropertyRest(IUBLConfig.INDICATOR_SERIE_DAM_DS, "7023", transaccionLineas.getNumeroSerie()));

        return item;
    } //getItemForLine

    protected ItemPropertyType getAdditionalItemPropertyRest(String name, String valueNameCode, String value) {//TransaccionLineas transaccionLineas){
        //<cac:AdditionalItemProperty><cbc:Name>
        ItemPropertyType additionalItemProperty = new ItemPropertyType();
        NameType nameAdditional = new NameType();
        nameAdditional.setValue(name);
        additionalItemProperty.setName(nameAdditional);
        //<cbc:NameCode
        NameCodeType nameCode = new NameCodeType();
        nameCode.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
        nameCode.setListName(IUBLConfig.PROPERTIES_NAME_ITEM);
        nameCode.setListURI(IUBLConfig.URI_CATALOG_55);
        nameCode.setValue(valueNameCode);
        additionalItemProperty.setNameCode(nameCode);
        //<cbc:Value>
        ValueType valueT = new ValueType();
        valueT.setValue(value);
        additionalItemProperty.getValue().add(valueT);
        return additionalItemProperty;
    }

    protected PriceType getPriceForLine(List<TransactionLineasBillRefDTO> transaccionLineasBillrefList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getPriceForLine() [" + this.identifier + "]");
        }
        if (null == transaccionLineasBillrefList || 0 == transaccionLineasBillrefList.size()) {
            logger.error("getPriceForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_333.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_333);
        }
        PriceType price = null;
        try {
            String hidden_unitValue = null;
            for (TransactionLineasBillRefDTO billReference : transaccionLineasBillrefList) {
                if (billReference.getAdtDocRef_SchemaId().equalsIgnoreCase(IUBLConfig.HIDDEN_UVALUE)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getPriceForLine() [" + this.identifier + "] Se encontro el " + IUBLConfig.HIDDEN_UVALUE);
                    }
                    hidden_unitValue = billReference.getAdtDocRef_Id();
                    break;
                }
            }
            if (StringUtils.isBlank(hidden_unitValue)) {
                logger.error("getPriceForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_334.getMessage());
                throw new UBLDocumentException(IVenturaError.ERROR_334);
            }
            price = new PriceType();
            /* <cac:Price><cbc:PriceAmount> */
            PriceAmountType priceAmount = new PriceAmountType();
            if (0D == Double.valueOf(hidden_unitValue)) {
                priceAmount.setValue(BigDecimal.valueOf(Double.valueOf(hidden_unitValue)).setScale(2, RoundingMode.HALF_UP));
            } else {
                priceAmount.setValue(BigDecimal.valueOf(Double.valueOf(hidden_unitValue)).setScale(IUBLConfig.DECIMAL_LINE_UNIT_VALUE, RoundingMode.HALF_UP));
            }
            priceAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());

            price.setPriceAmount(priceAmount);
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
    } //getPriceForLine

    protected PaymentType getPaymentForLine(String idValue, BigDecimal paidAmountValue, String currencyCode, Date paidDateValue)
            throws UBLDocumentException {
        PaymentType payment = new PaymentType();
        /* <cac:Payment><cbc:ID> */
        IDType id = new IDType();
        id.setValue(idValue);
        payment.setID(id);
        /* <cac:Payment><cbc:PaidAmount> */
        PaidAmountType paidAmount = new PaidAmountType();
        paidAmount.setValue(paidAmountValue.setScale(2, RoundingMode.HALF_UP));
        paidAmount.setCurrencyID(CurrencyCodeContentType.valueOf(currencyCode).value());
        payment.setPaidAmount(paidAmount);
        /* <cac:Payment><cbc:PaidDate> */
        payment.setPaidDate(getPaidDate(paidDateValue));
        return payment;
    } //getPaymentForLine

    protected OrderLineReferenceType getOrderLineReference(String lineIDValue) {
        OrderLineReferenceType orderLineReference = new OrderLineReferenceType();
        LineIDType lineID = new LineIDType();
        lineID.setValue(lineIDValue);
        orderLineReference.setLineID(lineID);
        return orderLineReference;
    } //getOrderLineReference

    private ItemPropertyType getAdditionalItemProperty(TransactionLineasDTO lineas, String codi, String valuer) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAdditionalItemProperty() [" + this.identifier + "]");
        }
        ItemPropertyType additionalItemProperty = new ItemPropertyType();
        try {
            /* <cac:AdditionalItemProperty><cbc:Name> */
            NameType name = new NameType();
            name.setValue(valuer);
            additionalItemProperty.setName(name);
            /* <cac:AdditionalItemProperty><cbc:NameCode> */
            NameCodeType nameCode = new NameCodeType();
            nameCode.setValue(codi);
            nameCode.setListURI(IUBLConfig.URI_CATALOG_55);
            nameCode.setListName("Propiedad del item");
            nameCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
            additionalItemProperty.setNameCode(nameCode);
            /*
             * <cac:AdditionalItemProperty><cbc:Value>
             *
             * 3001: Matricula de la Embarcacion Pesquera
             * 3002: Nombre de la Embarcacion Pesquera
             * 3003: Tipo de la Especie Vendida
             * 3004: Lugar de Descarga
             */
            if (codi.equals("3001")) {
                ValueType value = new ValueType();
                value.setValue("-");
                additionalItemProperty.getValue().add(value);
            } else if (codi.equals("3002")) {
                ValueType value = new ValueType();
                value.setValue(lineas.getNombreEmbarcacion());
                additionalItemProperty.getValue().add(value);
            } else if (codi.equals("3003")) {
                ValueType value = new ValueType();
                value.setValue(lineas.getTipoEspecieVendida());
                additionalItemProperty.getValue().add(value);
            } else if (codi.equals("3004")) {
                ValueType value = new ValueType();
                value.setValue(lineas.getLugarDescarga());
                additionalItemProperty.getValue().add(value);
            }
            /*
             * <cac:AdditionalItemProperty><cbc:ValueQuantity>
             *
             * Cantidad de la Especie Vendida
             */
            if (codi.equals("3006")) {
                ValueQuantityType valueQuantityType = new ValueQuantityType();
                valueQuantityType.setValue(lineas.getCantidadEspecieVendida().setScale(IUBLConfig.DECIMAL_LINE_ITEM_ADDITIONALITEMPROPERTY_VALUEQUANTITY, RoundingMode.HALF_UP));
                valueQuantityType.setUnitCodeListID("UN/ECE rec 20");
                valueQuantityType.setUnitCodeListAgencyName(IUBLConfig.LIST_AGENCY_NAME_UNECE);
                valueQuantityType.setUnitCode("TNE");
                additionalItemProperty.setValueQuantity(valueQuantityType);
            }
            /*
             * <cac:AdditionalItemProperty><cac:UsabilityPeriod>
             *
             * Fecha de Descarga
             */
            if (codi.equals("3005")) {
                PeriodType usabilityPeriod = new PeriodType();
                //usabilityPeriod.setStartDate(getStartDate("2022-12-12"));
                //date.ToString("yyyy-MM-dd");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String format = formatter.format(lineas.getFechaDescarga());

                usabilityPeriod.setStartDate(getStartDate(format));

                additionalItemProperty.setUsabilityPeriod(usabilityPeriod);
            }

        } catch (UBLDocumentException e) {
            logger.error("getAdditionalItemProperty() [" + this.identifier + "] ERROR: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAdditionalItemProperty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_326.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_326);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAdditionalItemProperty() [" + this.identifier + "]");
        }
        return additionalItemProperty;
    } //getAdditionalItemProperty

    private LocationType getDeliveryLocationForDelivery(String idValue, String lineValue) throws Exception {
        LocationType deliveryLocation = new LocationType();
        /* <cac:DeliveryLocation><cac:Address> */
        AddressType address = new AddressType();
        /* <cac:DeliveryLocation><cac:Address><cbc:ID> */
        IDType id = new IDType();
        id.setValue(idValue);
        id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
        id.setSchemeName("Ubigeos");
        address.setID(id);
        /* <cac:DeliveryLocation><cac:Address><cac:AddressLine> */
        AddressLineType addressLine = new AddressLineType();
        LineType line = new LineType();
        line.setValue(lineValue);
        addressLine.setLine(line);
        address.getAddressLine().add(addressLine);
        deliveryLocation.setAddress(address);
        return deliveryLocation;
    } //getDeliveryLocationForDelivery

    private DespatchType getDespatchForDelivery(String idValue, String lineValue, String instructionsValue) throws Exception {
        DespatchType despatch = new DespatchType();
        /* <cac:Despatch><cac:DespatchAddress> */
        AddressType despatchAddress = new AddressType();
        /* <cac:Despatch><cac:DespatchAddress><cbc:ID> */
        IDType id = new IDType();
        id.setValue(idValue);
        id.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_INEI);
        id.setSchemeName("Ubigeos");
        despatchAddress.setID(id);

        /* <cac:Despatch><cac:DespatchAddress><cac:AddressLine> */
        AddressLineType addressLine = new AddressLineType();
        LineType line = new LineType();
        line.setValue(lineValue);
        addressLine.setLine(line);
        despatchAddress.getAddressLine().add(addressLine);
        despatch.setDespatchAddress(despatchAddress);
        /* <cac:Despatch><cbc:Instructions> */
        InstructionsType instructions = new InstructionsType();
        instructions.setValue(instructionsValue);
        despatch.setInstructions(instructions);
        return despatch;
    } //getDespatchForDelivery

    private DeliveryTermsType getDeliveryTermsForDelivery(String codigo, BigDecimal valorCarga) throws UBLDocumentException {
        DeliveryTermsType deliveryTerms = new DeliveryTermsType();
        try {
            /* <cac:DeliveryTerms><cbc:ID> */
            IDType id = new IDType();
            id.setValue(codigo);
            deliveryTerms.setID(id);
            /* <cac:DeliveryTerms><cbc:Amount> */
            AmountType amount = new AmountType();
            amount.setCurrencyID("PEN");
            amount.setValue(valorCarga.setScale(IUBLConfig.DECIMAL_LINE_DELIVERY_DELIVERYTERMS_AMOUNT, RoundingMode.HALF_UP));
            deliveryTerms.setAmount(amount);
        } catch (Exception e) {
            logger.error("getDeliveryTermsForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_357.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_357);
        }
        return deliveryTerms;
    } //getDeliveryTermsForDelivery

    private ShipmentType getShipmentForDelivery(String confVehicular, BigDecimal cUtilVehiculoTM, BigDecimal cEfectivaVehiculoTM, BigDecimal valorReferencialTM,
                                                BigDecimal valorPreliminarRef, String factorRetorno) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getShipmentForDelivery() [" + this.identifier + "]");
        }
        ShipmentType shipment = new ShipmentType();
        try {
//            /* <cac:Shipment><cbc:ID> */
            shipment.setID(getID("01"));
            /* <cac:Shipment><cac:Consigment> */
            ConsignmentType consignment = new ConsignmentType();
//            /* <cac:Shipment><cac:Consigment><cbc:ID> */
            consignment.setID(getID("01"));
            /* <cac:Shipment><cac:Consigment><cbc:ID> */
            if (null != valorPreliminarRef && valorPreliminarRef.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getShipmentForDelivery() [" + this.identifier + "] Agregar VALOR PRELIMINAR REFERENCIAL POR CARGA UTIL NOMINAL.");
                }
                DeclaredForCarriageValueAmountType declaredForCarriageValueAmount = new DeclaredForCarriageValueAmountType();
                declaredForCarriageValueAmount.setValue(valorPreliminarRef);
                declaredForCarriageValueAmount.setCurrencyID("PEN");
                consignment.setDeclaredForCarriageValueAmount(declaredForCarriageValueAmount);
            }
            /* <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit> */
            TransportHandlingUnitType transportHandlingUnit = new TransportHandlingUnitType();
            /* <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:TransportEquipment> */
            TransportEquipmentType transportEquipment = new TransportEquipmentType();
            /* <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:TransportEquipment><cbc:SizeTypeCode> */
            if (StringUtils.isNotBlank(confVehicular)) {
                if (logger.isInfoEnabled()) {
                    logger.info("getShipmentForDelivery() [" + this.identifier + "] Agregar CONFIGURACION VEHICULAR.");
                }
                SizeTypeCodeType sizeTypeCode = new SizeTypeCodeType();
                sizeTypeCode.setValue(confVehicular);
                sizeTypeCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_MTC);
                sizeTypeCode.setListName("Configuracion Vehícular");
                transportEquipment.setSizeTypeCode(sizeTypeCode);
            }
            /* <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:TransportEquipment><cac:Delivery> */
            if (null != valorReferencialTM && valorReferencialTM.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getShipmentForDelivery() [" + this.identifier + "] Agregar VALOR REFERENCIAL POR TM.");
                }
                DeliveryType delivery = new DeliveryType();
                DeliveryTermsType deliveryTerms = new DeliveryTermsType();
                AmountType amount = new AmountType();
                amount.setValue(valorReferencialTM);
                amount.setCurrencyID("PEN");
                deliveryTerms.setAmount(amount);
                delivery.getDeliveryTerms().add(deliveryTerms);
                transportEquipment.setDelivery(delivery);
            }
            /* <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:TransportEquipment><cbc:ReturnabilityIndicator> */
            ReturnabilityIndicatorType returnabilityIndicator = new ReturnabilityIndicatorType();
            returnabilityIndicator.setValue(valorPreliminarRef.equals("Y"));
            transportEquipment.setReturnabilityIndicator(returnabilityIndicator);
            transportHandlingUnit.getTransportEquipment().add(transportEquipment);
            /*
             * <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:MeasurementDimension>
             *
             * Carga Util en TM del Vehiculo.
             * Tipo de Carga: Carga Util (01)
             */
            if (null != cUtilVehiculoTM && cUtilVehiculoTM.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getShipmentForDelivery() [" + this.identifier + "] Agregar CARGA UTIL EN TM DEL VEHICULO.");
                }
                transportHandlingUnit.getMeasurementDimension().add(getMeasurementDimension("01", cUtilVehiculoTM));
            }
            /*
             * <cac:Shipment><cac:Consigment><cac:TransportHandlingUnit><cac:MeasurementDimension>
             *
             * Carga Efectiva en TM del Vehiculo.
             * Tipo de Carga: Carga Efectiva (02)
             */
            if (null != cEfectivaVehiculoTM && cEfectivaVehiculoTM.compareTo(BigDecimal.ZERO) == 1) {
                if (logger.isInfoEnabled()) {
                    logger.info("getShipmentForDelivery() [" + this.identifier + "] Agregar CARGA EFECTIVA EN TM DEL VEHICULO.");
                }
                transportHandlingUnit.getMeasurementDimension().add(getMeasurementDimension("02", cEfectivaVehiculoTM));
            }
            consignment.getTransportHandlingUnit().add(transportHandlingUnit);

//            //Punto Origen
//            PlannedPickupTransportEventType plannedPickupTransportEventType = new PlannedPickupTransportEventType();
//            LocationType  locationPickup = new LocationType();
//            IDType iDPickup = new IDType();
//            iDPickup.setValue("010101");
//            iDPickup.setSchemeAgencyName("PE:INEI");
//            iDPickup.setSchemeName("Ubigeos");
////            AddressType addressType = new AddressType();
////            
////            addressType.setID(iDPickup);
////            locationPickup.setAddress(addressType);
//            
//            locationPickup.setID(iDPickup); 
//            
//            plannedPickupTransportEventType.setLocation(locationPickup);
//
//            //Punto Destino
//            
//            PlannedDeliveryTransportEventType plannedDeliveryTransportEventType = new PlannedDeliveryTransportEventType();
//            LocationType  locationDelivery = new LocationType();
//            IDType iDDelivery = new IDType();
//            
//            iDDelivery.setValue("010103");
//            iDDelivery.setSchemeAgencyName("PE:INEI");
//            iDDelivery.setSchemeName("Ubigeos");


//            AddressType addressDelivery = new AddressType();
//            
//            addressDelivery.setID(iDPickup);
//            locationDelivery.setAddress(addressDelivery);

//            locationDelivery.setID(iDDelivery);
//            plannedDeliveryTransportEventType.setLocation(locationDelivery);
//            
//           //Descripcion del Tramo
//            CarrierServiceInstructionsType carrierServiceInstructionsType = new CarrierServiceInstructionsType();
//            carrierServiceInstructionsType.setValue("Descripcion");
//            
//            //Valor Preliminar Referencial virtual
//            DeliveryTermsType  deliveryTermsVirtual = new DeliveryTermsType();       
//            AmountType  amountVirtual = new AmountType();
//            
//            amountVirtual.setCurrencyID("PEN");
//            amountVirtual.setValue(BigDecimal.ONE);                  
//            deliveryTermsVirtual.setAmount(amountVirtual);          


            //Insert Consigment
//            consignmentType.setPlannedPickupTransportEvent(plannedPickupTransportEventType);
//            consignmentType.setPlannedDeliveryTransportEvent(plannedDeliveryTransportEventType);
//            consignmentType.setCarrierServiceInstructions(carrierServiceInstructionsType);
//            consignmentType.setDeliveryTerms(deliveryTermsVirtual);


            shipment.getConsignment().add(consignment);
        } catch (UBLDocumentException e) {
            logger.error("getShipmentForLine() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getShipmentForLine() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_358.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_358);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getShipmentForLine() [" + this.identifier + "]");
        }
        return shipment;
    } //getShipmentForDelivery

    private DimensionType getMeasurementDimension(String attributeIDValue, BigDecimal measureValue)
            throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getMeasurementDimension() [" + this.identifier + "] attributeIDValue: " + attributeIDValue + " measureValue: " + measureValue);
        }
        DimensionType measurementDimension = new DimensionType();

        try {
            /* <cac:MeasurementDimension><cbc:AttributeID> */
            AttributeIDType attributeID = new AttributeIDType();
            attributeID.setValue(attributeIDValue);
            measurementDimension.setAttributeID(attributeID);
            /* <cac:MeasurementDimension><cbc:Measure> */
            MeasureType measure = new MeasureType();
            measure.setValue(measureValue.setScale(IUBLConfig.DECIMAL_LINE_DELIVERY_SHIPMENT_CONSIGMENT_TRANSPORTHANDLINGUNIT_MEASUREMENTDIMENSION_MEASURE, RoundingMode.HALF_UP));
            measure.setUnitCode("TNE");
            measure.setUnitCodeListVersionID("UN/ECE rec 20");
            measurementDimension.setMeasure(measure);
        } catch (Exception e) {
            logger.error("getMeasurementDimension() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_359.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_359);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getMeasurementDimension() [" + this.identifier + "]");
        }
        return measurementDimension;
    } //getMeasurementDimension

    protected BigDecimal getSubtotalValueFromTransaction(
            List<TransactionTotalesDTO> transactionTotalList)
            throws UBLDocumentException {
        BigDecimal subtotal = null;

        if (null != transactionTotalList && 0 < transactionTotalList.size()) {
            for (int i = 0; i < transactionTotalList.size(); i++) {
                if (transactionTotalList.get(i)
                        .getId()
                        .equalsIgnoreCase(IUBLConfig.ADDITIONAL_MONETARY_1005)) {
                    subtotal = transactionTotalList.get(i).getMonto();
//                    transactionTotalList.remove(i);
                    break;
                }
            }
            if (subtotal == null) {
                subtotal = BigDecimal.ZERO;
            }
        } else {
            logger.error("getSubtotalValueFromTransaction() ["
                    + this.identifier + "] ERROR: "
                    + IVenturaError.ERROR_330.getMessage());
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

} //UBLBasicHandler
