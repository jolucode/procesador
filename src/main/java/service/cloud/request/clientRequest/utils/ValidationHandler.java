package service.cloud.request.clientRequest.utils;

import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.cloud.request.clientRequest.utils.exception.ValidationException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.unqualifieddatatypesschemamodule._2.IdentifierType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.AddressType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.PartyType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Esta clase contiene metodos para validar los documento UBL.
 *
 * @author Jose Manuel Lucas Barrera (josemlucasb@gmail.com)
 */
public class ValidationHandler {

    //private final Logger logger = Logger.getLogger(ValidationHandler.class);
    //private final Logger logger = Logger.getLogger(ValidationHandler.class);}
    private final Logger logger = LoggerFactory.getLogger(ValidationHandler.class);

    private List<String> totalIDList = null;

    private List<String> typeOfCreditNoteList = null;

    private List<String> typeOfDebitNoteList = null;

    private List<String> refDocumentTypeList = null;

    private final String docUUID;

    /**
     * Constructor privado para evitar instancias.
     *
     * @param docUUID UUID que identifica al documento.
     */
    private ValidationHandler(String docUUID) {
        this.docUUID = docUUID;

        /*
         * Cargando ID de TOTALES
         */
        totalIDList = new ArrayList<String>();
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_1001);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_1002);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_1003);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_1004);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_1005);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_2001);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_2002);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_2003);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_2004);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_2005);
        totalIDList.add(IUBLConfig.ADDITIONAL_MONETARY_3001);

        /*
         * Cargando los codigos de los tipos de Nota de Credito
         */
        typeOfCreditNoteList = new ArrayList<String>();
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_01);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_02);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_03);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_04);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_05);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_06);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_07);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_08);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_09);
        typeOfCreditNoteList.add(IUBLConfig.CREDIT_NOTE_TYPE_10);

        /*
         * Cargando los codigos de los tipos de Nota de Debito
         */
        typeOfDebitNoteList = new ArrayList<String>();
        typeOfDebitNoteList.add(IUBLConfig.DEBIT_NOTE_TYPE_01);
        typeOfDebitNoteList.add(IUBLConfig.DEBIT_NOTE_TYPE_02);
        typeOfDebitNoteList.add(IUBLConfig.DEBIT_NOTE_TYPE_03);

        /*
         * Cargando los codigos de los tipos de Nota de Debito
         */
        refDocumentTypeList = new ArrayList<String>();
        refDocumentTypeList.add(IUBLConfig.DOC_INVOICE_CODE);
        refDocumentTypeList.add(IUBLConfig.DOC_BOLETA_CODE);
        refDocumentTypeList.add(IUBLConfig.DOC_MACHINE_TICKET_CODE);
        refDocumentTypeList.add(IUBLConfig.DOC_FINANCIAL_BANKS_CODE);
        refDocumentTypeList.add(IUBLConfig.DOC_BANK_INSURANCE_CODE);
        refDocumentTypeList.add(IUBLConfig.DOC_ISSUED_BY_AFP_CODE);
    } //ValidationHandler

    /**
     * Este metodo crea una nueva instancia de la clase ValidationHandler.
     *
     * @param docUUID UUID que identifica al documento.
     * @return Retorna una nueva instancia de la clase ValidationHandler.
     */
    public static synchronized ValidationHandler newInstance(String docUUID) {
        return new ValidationHandler(docUUID);
    } //newInstance

    /**
     * Este metodo verifica la informacion del identificador del documento, el
     * numero RUC del emisor y la fecha de emision.
     *
     * @param docIdentifier    El identificador del documento.
     * @param senderIdentifier El numero RUC del emisor.
     * @param issueDate        La fecha de emision.
     * @throws ValidationException
     */
    public void checkBasicInformation(String docIdentifier, String senderIdentifier, Date issueDate, String correoElectronico, String ccorreoElctroinico, boolean isContingencia) throws ValidationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+checkBasicInformation() [" + this.docUUID + "]");
        }
        /*
         * Validando identificador del documento
         */
        if (StringUtils.isBlank(docIdentifier)) {
            throw new ValidationException(IVenturaError.ERROR_514);
        }
        if (IUBLConfig.SERIE_CORRELATIVE_LENGTH < docIdentifier.length()) {
            throw new ValidationException(IVenturaError.ERROR_515);
        }
        if (!docIdentifier.contains("-")) {
            throw new ValidationException(IVenturaError.ERROR_516);
        }
        if (!docIdentifier.startsWith(IUBLConfig.REMISSION_GUIDE_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.INVOICE_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.BOLETA_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.PERCEPCION_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.RETENTION_SERIE_PREFIX)) {
            if (!isContingencia)
                throw new ValidationException(IVenturaError.ERROR_517);
        }
        try {
            Integer.valueOf(docIdentifier.substring(5));
        } catch (Exception e) {
            throw new ValidationException(IVenturaError.ERROR_518);
        }

        /*
         * Validando RUC del emisor electronico
         */
        if (IUBLConfig.DOC_RUC_LENGTH != senderIdentifier.length()) {
            throw new ValidationException(IVenturaError.ERROR_519);
        }
        try {
            Long.valueOf(senderIdentifier);
        } catch (Exception e) {
            throw new ValidationException(IVenturaError.ERROR_520);
        }

        /*
         * Validando la fecha de emision
         */
        if (null == issueDate) {
            throw new ValidationException(IVenturaError.ERROR_521);
        }
        if (null == correoElectronico || correoElectronico.isEmpty()) {
            throw new ValidationException(IVenturaError.ERROR_549);
        }
        if (null == ccorreoElctroinico || ccorreoElctroinico.isEmpty()) {
            throw new ValidationException(IVenturaError.ERROR_550);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-checkBasicInformation() [" + this.docUUID + "]");
        }
    } //checkBasicInformation

    /**
     * Este metodo verifica la informacion del identificador del documento de
     * COMUNICACION DE BAJA y RESUMEN DIARIO, el numero RUC del emisor y la
     * fecha de emision.
     *
     * @param docIdentifier    El identificador del documento.
     * @param senderIdentifier El numero RUC del emisor.
     * @param issueDate        La fecha de emision.
     * @throws ValidationException
     */
    public void checkBasicInformation2(String docIdentifier, String senderIdentifier, Date issueDate) throws ValidationException {
        if (logger.isDebugEnabled()) {
            logger.debug("+checkBasicInformation2() [" + this.docUUID + "]");
        }
        /*
         * Validando identificador del documento
         */
        if (StringUtils.isBlank(docIdentifier)) {
            throw new ValidationException(IVenturaError.ERROR_529);
        }
        if (!docIdentifier.startsWith(IUBLConfig.VOIDED_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.SUMMARY_SERIE_PREFIX) && !docIdentifier.startsWith(IUBLConfig.VOIDED_SERIE_PREFIX_CPE)) {
            throw new ValidationException(IVenturaError.ERROR_530);
        }

        /*
         * Validando RUC del emisor electronico
         */
        if (IUBLConfig.DOC_RUC_LENGTH != senderIdentifier.length()) {
            throw new ValidationException(IVenturaError.ERROR_519);
        }
        try {
            Long.valueOf(senderIdentifier);
        } catch (Exception e) {
            throw new ValidationException(IVenturaError.ERROR_520);
        }

        /*
         * Validando la fecha de emision
         */
        if (null == issueDate) {
            throw new ValidationException(IVenturaError.ERROR_521);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-checkBasicInformation2() [" + this.docUUID + "]");
        }
    } //checkBasicInformation2


    public void checkRetentionDocument(RetentionType retentionType) throws ValidationException {
        long startTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("+checkRetentionDocument() [" + this.docUUID + "]");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("+checkRetentionDocument() [getSunatRetentionPercent]" + retentionType.getSunatRetentionPercent().getValue().length());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("+checkRetentionDocument() [getSunatRetentionPercent]" + retentionType.getSunatRetentionPercent().getValue());
        }
        if (retentionType.getSunatRetentionSystemCode().getValue().length() != 2) {
            throw new ValidationException(IVenturaError.ERROR_540);
        }
        if (retentionType.getSunatRetentionPercent().getValue().length() != 1) {
            throw new ValidationException(IVenturaError.ERROR_541);
        }
        if (retentionType.getTotalInvoiceAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException(IVenturaError.ERROR_542);
        }
        if (retentionType.getTotalPaid().getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException(IVenturaError.ERROR_543);
        }
        Optional.of(retentionType).map(RetentionType::getReceiverParty).ifPresent(System.out::println);
        Optional.ofNullable(retentionType).map(RetentionType::getReceiverParty).map(PartyType::getPostalAddress).map(AddressType::getID).map(IdentifierType::getValue).ifPresent(System.out::println);
        Optional.ofNullable(retentionType.getAgentParty()).ifPresent(System.out::println);
        if (Optional.ofNullable(retentionType.getReceiverParty().getPostalAddress()).isPresent()) {
            if (retentionType.getReceiverParty().getPostalAddress().getID().getValue().compareTo("") == 0 || retentionType.getReceiverParty().getPostalAddress().getID().getValue().length() != 6) {
                throw new ValidationException(IVenturaError.ERROR_1100);
            }
        } else {
            throw new ValidationException(IVenturaError.ERROR_1100);
        }
        if (Optional.ofNullable(retentionType.getAgentParty().getPostalAddress()).isPresent()) {
            if (retentionType.getAgentParty().getPostalAddress().getID().getValue().compareTo("") == 0 || retentionType.getAgentParty().getPostalAddress().getID().getValue().length() != 6) {
                throw new ValidationException(IVenturaError.ERROR_1101);
            }
        } else {
            throw new ValidationException(IVenturaError.ERROR_1101);
        }
    }

    public void checkRemissionGuideDocument(DespatchAdviceType retentionType) throws ValidationException {
        long startTime = System.currentTimeMillis();
    }

    public void checkPerceptionDocument(PerceptionType perceptionType) throws ValidationException {
        long startTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("+checkPerceptionDocument() [" + this.docUUID + "]");
        }
        if (perceptionType.getSunatTotalCashed().getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException(IVenturaError.ERROR_531);
        }
        if (perceptionType.getTotalInvoiceAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException(IVenturaError.ERROR_532);
        }
        if (perceptionType.getTotalInvoiceAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException(IVenturaError.ERROR_532);
        }
        if (perceptionType.getReceiverParty().getPostalAddress().getID().getValue().compareTo("") == 0 || perceptionType.getReceiverParty().getPostalAddress().getID().getValue().length() != 6) {
            throw new ValidationException(IVenturaError.ERROR_1000);
        }
        if (perceptionType.getAgentParty().getPostalAddress().getID().getValue().compareTo("") == 0 || perceptionType.getAgentParty().getPostalAddress().getID().getValue().length() != 6) {
            throw new ValidationException(IVenturaError.ERROR_1001);
        }
        for (int i = 0; i < perceptionType.getSunatPerceptionDocumentReference().size(); i++) {
            if (perceptionType.getSunatPerceptionDocumentReference().get(i).getPayment().getPaidAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
                throw new ValidationException(IVenturaError.ERROR_533);
            }
            if (perceptionType.getSunatPerceptionDocumentReference().get(i).getTotalInvoiceAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
                throw new ValidationException(IVenturaError.ERROR_534);
            }
            if (perceptionType.getSunatPerceptionDocumentReference().get(i).getSunatPerceptionInformation().getPerceptionAmount().getValue().compareTo(BigDecimal.ZERO) == 0) {
                throw new ValidationException(IVenturaError.ERROR_535);
            }
            if (perceptionType.getSunatPerceptionDocumentReference().get(i).getSunatPerceptionInformation().getSunatNetTotalCashed().getValue().compareTo(BigDecimal.ZERO) == 0) {
                throw new ValidationException(IVenturaError.ERROR_536);
            }
        }
    }

} //ValidationHandler
