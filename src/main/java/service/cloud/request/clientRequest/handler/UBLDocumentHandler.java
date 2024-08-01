package service.cloud.request.clientRequest.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import service.cloud.request.clientRequest.entity.*;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.utils.exception.UBLDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.codelist.specification._54217._2001.CurrencyCodeContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.unqualifieddatatypesschemamodule._2.IdentifierType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.ExtensionContentType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.creditnote_2.CreditNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.debitnote_2.DebitNoteType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.invoice_2.InvoiceType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.perception_1.PerceptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.retention_1.RetentionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.AdditionalInformationType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.sunataggregatecomponents_1.*;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Esta clase HANDLER contiene metodos para generar objetos UBL, necesarios para
 * armar el documento UBL validado por Sunat.
 */
public class UBLDocumentHandler extends UBLBasicHandler {

    private final Logger logger = Logger.getLogger(UBLDocumentHandler.class);

    /**
     * Constructor privado para evitar la creacion de instancias usando el
     * constructor.
     *
     * @param identifier Identificador del objeto UBLDocumentHandler creado.
     */
    private UBLDocumentHandler(String identifier) {
        super(identifier);
    } //UBLDocumentHandler

    /**
     * Este metodo crea una nueva instancia de la clase UBLDocumentHandler.
     *
     * @param identifier Identificador del objeto UBLDocumentHandler creado.
     * @return Retorna una nueva instancia de la clase UBLDocumentHandler.
     */
    public static synchronized UBLDocumentHandler newInstance(String identifier) {
        return new UBLDocumentHandler(identifier);
    } //newInstance

    public UBLExtensionType getUBLExtensionSigner() {
        UBLExtensionType ublExtensionSigner = new UBLExtensionType();
        ExtensionContentType extensionContent = new ExtensionContentType();
        ublExtensionSigner.setExtensionContent(extensionContent);

        return ublExtensionSigner;
    } // getUBLExtensionSigner


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
            }

            if (transaccion.getTransaccionContractdocrefList() != null && !transaccion.getTransaccionContractdocrefList().isEmpty()) {

                Optional<TransaccionContractdocref> listTran = transaccion.getTransaccionContractdocrefList().stream().filter(docRefer -> "incoterms".equalsIgnoreCase(docRefer.getUsuariocampos().getNombre())).findFirst();

                //con Id vamos a la tabla transaccionContractDocRef
                List<TransaccionContractdocref> listContract = transaccion.getTransaccionContractdocrefList();//.parallelStream().filter(docRefer -> "incoterms".equalsIgnoreCase(docRefer.getUsuariocampos().getNombre())).findAny();

                TransaccionContractdocref objecto = null;
                for (int i = 0; i < listContract.size(); i++) {
                    //if(listContract.get(i).getUsuariocampos().getNombre())
                    //System.out.println(listContract.get(i).getUsuariocampos().getNombre());
                    if ("incoterms".equals(listContract.get(i).getUsuariocampos().getNombre())) {
                        objecto = listContract.get(i);
                    }
                }

                //System.out.println(f.getValor());

                if (objecto != null) {
                    AdditionalPropertyType additionalProperty = new AdditionalPropertyType();
                    //TransaccionContractdocref docRefer = optional;
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
                    value.setValue(objecto.getValor());

                    /* Agregar ID y Value al objeto */
                    additionalProperty.setID(id);
                    additionalProperty.setValue(value);

                    /* Agregar el objeto AdditionalPropertyType a la lista */
                    additionalInformation.getAdditionalProperty().add(additionalProperty);
                    //invoiceType.getContractDocumentReference().add(getContractDocumentReference(docRefer.getValor(), "OC"));
                }
            }




            for (int i = 0; i < transaccion.getTransaccionContractdocrefList().size(); i++) {
                if ("texto_amplio".equals(transaccion.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                    TransaccionContractdocref objecto = transaccion.getTransaccionContractdocrefList().get(i);
                    if (objecto != null && !objecto.getValor().isEmpty()) {
                        NoteType noteType = new NoteType();
                        noteType.setValue(objecto.getValor());
                        ublExtension.setNote(noteType);
                        break;
                    }
                }
            }

            /* Colocar la informacion en el TAG UBLExtension */
            ExtensionContentType extensionContent = new ExtensionContentType();
            extensionContent.setAny((org.w3c.dom.Element) getExtensionContentNode(additionalInformation));
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

    private UBLExtensionType getUBLExtensionTotalAndProperty2(TransaccionContractdocref object) throws UBLDocumentException {
        UBLExtensionType ublExtension = null;
        try {
            ublExtension = new UBLExtensionType();

            OrderReferenceType orderReference = new OrderReferenceType();
            SalesOrderIDType salesOrderID = new SalesOrderIDType();

            salesOrderID.setValue(object.getValor());
            orderReference.setSalesOrderID(salesOrderID);

            /* Colocar la informacion en el TAG UBLExtension */
            ExtensionContentType extensionContent = new ExtensionContentType();
            extensionContent.setOrderReference(orderReference);
            ublExtension.setExtensionContent(extensionContent);
        } catch (Exception e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_328.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_328);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getUBLExtensionTotalAndProperty() [" + this.identifier + "]");
        }
        return ublExtension;
    } // getUBLExtensionTotalAndProperty

    private UBLExtensionType getUBLExtensionTotalAndProperty3(TransaccionContractdocref object) throws UBLDocumentException {

        UBLExtensionType ublExtension = null;

        try {
            ublExtension = new UBLExtensionType();

            DatoAdicionalType datoAdicional = new DatoAdicionalType();
            CodigoType codigo = new CodigoType();
            ValorType valor = new ValorType();

            codigo.setValue("10");
            valor.setValue(object.getValor());

            datoAdicional.setCodigo(codigo);
            datoAdicional.setValor(valor);

            ExtensionContentType extensionContent = new ExtensionContentType();
            extensionContent.setDatoAdicional(datoAdicional);
            ublExtension.setExtensionContent(extensionContent);
        } catch (Exception e) {
            logger.error("getUBLExtensionTotalAndProperty() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_328.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_328);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getUBLExtensionTotalAndProperty() [" + this.identifier + "]");
        }
        return ublExtension;
    } // getUBLExtensionTotalAndProperty

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

    public InvoiceType generateInvoiceType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateInvoiceType() [" + this.identifier + "]");
        }
        InvoiceType invoiceType = null;
        try {
            /* Instanciar el objeto InvoiceType para la FACTURA */
            invoiceType = new InvoiceType();

            //UBLExtensionsType ublExtensions = new UBLExtensionsType();

            /* Agregar <Invoice><ext:UBLExtensions> */
            //invoiceType.setUBLExtensions(getUBLExtensionsSigner());

            UBLExtensionsType ublExtensions = new UBLExtensionsType();


            ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty(transaction, transaction.getTransaccionTotalesList(), transaction.getTransaccionPropiedadesList(), transaction.getSUNAT_Transact()));

            TransaccionContractdocref objecto = null;
            if (transaction.getTransaccionContractdocrefList() != null && !transaction.getTransaccionContractdocrefList().isEmpty()) {
                for (int i = 0; i < transaction.getTransaccionContractdocrefList().size(); i++) {
                    if ("orden_venta".equals(transaction.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                        objecto = transaction.getTransaccionContractdocrefList().get(i);
                        if (objecto != null && !objecto.getValor().isEmpty()) {
                            ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty2(objecto));
                            break;
                        }
                    }
                }
                for (int i = 0; i < transaction.getTransaccionContractdocrefList().size(); i++) {
                    if ("cerper_mensaje".equals(transaction.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                        objecto = transaction.getTransaccionContractdocrefList().get(i);
                        if (objecto != null && !objecto.getValor().isEmpty()) {
                            ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty3(objecto));
                            break;
                        }
                    }
                }
            }


            //ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty3(transaction, transaction.getTransaccionTotalesList(), transaction.getTransaccionPropiedadesList(), transaction.getSUNATTransact()));

            ublExtensions.getUBLExtension().add(getUBLExtensionSigner());
            invoiceType.setUBLExtensions(ublExtensions);
            //ublExtensions.getUBLExtension().add(getUBLExtensionsSigner());

            //ublExtensions.getUBLExtension().add(getUBLExtensionTotalAndProperty(transaction, transaction.getTransaccionTotalesList(), transaction.getTransaccionPropiedadesList(), transaction.getSUNATTransact()));
            /* Agregar <Invoice><cbc:UBLVersionID> */
            invoiceType.setUBLVersionID(getUBLVersionID_2_1());

            /* Agregar <Invoice><cbc:CustomizationID> */
            invoiceType.setCustomizationID(getCustomizationID_2_0());

            /* Agregar <Invoice><cbc:ProfileID> */
            invoiceType.setProfileID(getProfileID(transaction.getTipoOperacionSunat()));
            List<TransaccionContractdocref> transaccionContractdocrefList = transaction.getTransaccionContractdocrefList();
            Optional<TransaccionContractdocref> optional = transaccionContractdocrefList.parallelStream().filter(docRefer -> IUBLConfig.CONTRACT_DOC_REF_SELL_ORDER_CODE.equalsIgnoreCase(docRefer.getUsuariocampos().getNombre())).findAny();
            if (optional.isPresent()) {
                TransaccionContractdocref docRefer = optional.get();
                invoiceType.getContractDocumentReference().add(getContractDocumentReference(docRefer.getValor(), "OC"));
            }
//            if (!transaccionContractdocrefList.isEmpty()) {
//                for (TransaccionContractdocref transContractdocref : transaccionContractdocrefList) {
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("generateInvoiceType() [" + this.identifier + "] CAMPOS PERSONALIZADOS" + transContractdocref.getUsuariocampos().getNombre() + " :" + transContractdocref.getValor());
//                    }
//                    invoiceType.getContractDocumentReference().add(getContractDocumentReference(transContractdocref.getValor(), transContractdocref.getUsuariocampos().getNombre()));
//                }
//            }

            /* Agregar <Invoice><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoiceType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            invoiceType.setID(getID(transaction.getDOC_Id()));

            /* Agregar <Invoice><cbc:UUID> */
            invoiceType.setUUID(getUUID(this.identifier));

            /* Agregar <Invoice><cbc:IssueDate> */
            invoiceType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <Invoice><cbc:IssueTime> */
            invoiceType.setIssueTime(getIssueTimeDefault());

            /* Agregar <Invoice><cbc:DueDate> */
            if (null != transaction.getDOC_FechaVencimiento()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene FECHA DE VENCIMIENTO.");
                }
                invoiceType.setDueDate(getDueDate(transaction.getDOC_FechaVencimiento()));
            }

            /* Agregar <Invoice><cbc:InvoiceTypeCode> */
            invoiceType.setInvoiceTypeCode(getInvoiceTypeCode(transaction.getDOC_Codigo(), transaction.getTipoOperacionSunat()));
            if (transaction.getTransaccionPropiedadesList().size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene PROPIEDADES.");
                }
                invoiceType.getNote().addAll(getNotesWithIfSentence(transaction.getTransaccionPropiedadesList()));
            }

            /* Agregar <Invoice><cbc:Note> */
            List<TransaccionContractdocref> contractdocrefs = transaction.getTransaccionContractdocrefList();
            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("nro_hes".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    String valorNote = contract.getValor();
                    Optional<String> optionalValor = Optional.ofNullable(valorNote).map(s -> s.isEmpty() ? null : s);
                    if (optionalValor.isPresent()) invoiceType.getNote().add(generateNote(valorNote));
                    break;
                }
            }




            /* Agregar <Invoice><cbc:DocumentCurrencyCode> */
            invoiceType.setDocumentCurrencyCode(getDocumentCurrencyCode(transaction.getDOC_MON_Codigo()));

            /* Agregar <Invoice><cbc:LineCountNumeric> */
            List<TransaccionLineas> transaccionLineas = transaction.getTransaccionLineasList();
            invoiceType.setLineCountNumeric(getLineCountNumeric(transaccionLineas.size()));
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoiceType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx LineCountNumeric(" + invoiceType.getLineCountNumeric() + ") xxxxxxxxxxxxxxxxxxx");
            }

            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("aromas_note".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    String valorNote = contract.getValor();
                    Optional<String> optionalValor = Optional.ofNullable(valorNote).map(s -> s.isEmpty() ? null : s);
                    if (optionalValor.isPresent()) invoiceType.getNote().add(generateNote(valorNote));
                    break;
                }
            }
            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("orden_compra".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    String valorOrderRefence = contract.getValor();
                    Optional<String> optionalValor = Optional.ofNullable(valorOrderRefence).map(s -> s.isEmpty() ? null : s);
                    if (optionalValor.isPresent()) {
                        invoiceType.setOrderReference(generateOrderReferenceType(valorOrderRefence));
                    }
                    break;
                }
            }
            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("nro_guia".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    String valorDocumentReference = contract.getValor();
                    Optional<String> optionalValor = Optional.ofNullable(valorDocumentReference).map(s -> s.isEmpty() ? null : s);
                    if (optionalValor.isPresent())
                        invoiceType.getDespatchDocumentReference().add(generateDocumentReferenceType(valorDocumentReference));
                    break;
                }
            }
            BigDecimal monPercepcion = transaction.getDOC_MonPercepcion();

            /*
             * Agregar las guias de remision
             *
             * <Invoice><cac:DespatchDocumentReference>
             */
            if (null != transaction.getTransaccionDocrefersList() && 0 < transaction.getTransaccionDocrefersList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene GUIAS DE REMISION.");
                }
                invoiceType.getDespatchDocumentReference().addAll(getDespatchDocumentReferences(transaction.getTransaccionDocrefersList()));
            }

            /*
             * Extraer la condicion de pago de ser el caso.
             */
 /*if (StringUtils.isNotBlank(transaction.getDOCCondPago())) {
             if (logger.isInfoEnabled()) {
             logger.info("generateInvoiceType() [" + this.identifier
             + "] Extraer la CONDICION DE PAGO.");
             }
             invoiceType.getContractDocumentReference().add(getContractDocumentReference(transaction.getDOCCondPago(),IUBLConfig.CONTRACT_DOC_REF_PAYMENT_COND_CODE));
             }*/

            /*
             * Extraer la orden de venta y nombre del vendedor de ser el caso
             */
 /*if (logger.isDebugEnabled()) {
             logger.debug("generateInvoiceType() [" + this.identifier + "] CAMPOS PERSONALIZADOS ");
             }
             if (null != transaction.getTransaccionContractdocrefList() && 0 < transaction.getTransaccionContractdocrefList().size()) {
             for (TransaccionContractdocref transContractdocref : transaction.getTransaccionContractdocrefList()) {

             if (logger.isDebugEnabled()) {
             logger.debug("generateInvoiceType() [" + this.identifier + "] CAMPOS PERSONALIZADOS" + transContractdocref.getUsuariocampos().getNombre() + " :" + transContractdocref.getValor());
             }
             invoiceType.getContractDocumentReference().add(getContractDocumentReference(transContractdocref.getValor(), transContractdocref.getUsuariocampos().getNombre()));
             }
             }*/
            /*
             * Agregar DEDUCCION DE ANTICIPOS
             */
            if (null != transaction.getANTICIPO_Monto() && transaction.getANTICIPO_Monto().compareTo(BigDecimal.ZERO) > 0 && null != transaction.getTransaccionAnticipoList() && 0 < transaction.getTransaccionAnticipoList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene informacion de ANTICIPOS.");
                }
                invoiceType.getAdditionalDocumentReference().addAll(getAdditionalDocumentReferences(transaction.getTransaccionAnticipoList(), transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo()));
            }
            //Esto es para CPPC
            boolean contieneCampoCppc = false;
            String valorCampoCppc = "";
            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("campo_cppc".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    Optional<String> optionalCampo = Optional.ofNullable(contract.getValor()).map(valor -> valor.isEmpty() ? null : valor);
                    contieneCampoCppc = optionalCampo.isPresent();
                    if (optionalCampo.isPresent()) {
                        valorCampoCppc = optionalCampo.get();
                        break;
                    }
                }
            }
            if (contieneCampoCppc) {
                invoiceType.getAdditionalDocumentReference().add(generateCampoCppc(valorCampoCppc));
            }
            /* Agregar <Invoice><cac:Signature> */
            invoiceType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <Invoice><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = getAccountingSupplierPartyV21(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            invoiceType.setAccountingSupplierParty(accountingSupplierParty);

            /* Agregar <Invoice><cac:AccountingCustomerParty> */
            CustomerPartyType accountingCustomerParty = getAccountingCustomerPartyV21(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_NomCalle(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getSN_SegundoNombre(), transaction.getSN_EMail());
            invoiceType.setAccountingCustomerParty(accountingCustomerParty);

            /** SE AGREGA LA LOGICA PARA FACTURA **/
            List<PaymentTermsType> paymentTerms = new ArrayList<>();

            if (!(transaction.getTransaccionCuotas() == null || transaction.getTransaccionCuotas().size() == 0)) {
                if (transaction.getTransaccionCuotas().get(0).getFormaPago().equals("Contado")) {
                    //contado
                    paymentTerms = getPaymentTermsInvoice(true, transaction);
                } else {
                    //credito
                    paymentTerms = getPaymentTermsInvoice(false, transaction);
                }
                invoiceType.setPaymentTerms(paymentTerms);
            }



            /*
             * Agregar DETRACCIONES
             */
            if (transaction.getMontoDetraccion().compareTo(BigDecimal.ZERO) > 0 && transaction.getPorcDetraccion().compareTo(BigDecimal.ZERO) > 0 && StringUtils.isNotBlank(transaction.getCuentaDetraccion())) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene informacion de DETRACCIONES.");
                }
                invoiceType.getPaymentMeans().add(getPaymentMeans(transaction.getCuentaDetraccion(), transaction.getCodigoPago()));
                invoiceType.getPaymentTerms().add(getPaymentTerms(transaction.getCodigoDetraccion(), transaction.getMontoDetraccion(), transaction.getPorcDetraccion()));
            }
            /*
             * ANTICIPOS RELACIONADOS al comprobante de pago
             *
             * <Invoice><cac:PrepaidPayment>
             */
            if (null != transaction.getANTICIPO_Monto() && transaction.getANTICIPO_Monto().compareTo(BigDecimal.ZERO) > 0 && null != transaction.getTransaccionAnticipoList() && 0 < transaction.getTransaccionAnticipoList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene informacion de ANTICIPOS RELACIONADOS.");
                }
                invoiceType.getPrepaidPayment().addAll(getPrepaidPaymentV21(transaction.getTransaccionAnticipoList()));
                invoiceType.getAllowanceCharge().add(getAllowanceCharge(transaction.getDOC_ImporteTotal(), transaction.getANTICIPO_Monto(), false, transaction.getDOC_PorDescuento(), transaction.getDOC_Descuento(), transaction.getDOC_ImporteTotal(), transaction.getDOC_MON_Codigo(), "04", transaction.getMontoRetencion(), transaction.getDOC_MontoTotal()));

            }
            /*
             * Agregar DESCUENTO GLOBAL
             */

            if (transaction.getDOC_Descuento().compareTo(BigDecimal.ZERO) > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] La transaccion contiene informacion de DESCUENTO GLOBAL");
                }
                invoiceType.getAllowanceCharge().add(getAllowanceCharge(transaction.getDOC_ImporteTotal(), transaction.getANTICIPO_Monto(), false, transaction.getDOC_PorDescuento(), transaction.getDOC_Descuento(), transaction.getDOC_ImporteTotal(), transaction.getDOC_MON_Codigo(), "02", transaction.getMontoRetencion(), transaction.getDOC_MontoTotal()));
            }

            if (transaction.getMontoRetencion() != null) {
                if (transaction.getMontoRetencion().compareTo(BigDecimal.ZERO) > 0) {
                    invoiceType.getAllowanceCharge().add(getAllowanceCharge(transaction.getDOC_ImporteTotal(), transaction.getANTICIPO_Monto(), false, transaction.getDOC_PorDescuento(), transaction.getDOC_Descuento(), transaction.getDOC_ImporteTotal(), transaction.getDOC_MON_Codigo(), "62", transaction.getMontoRetencion(), transaction.getDOC_MontoTotal()));
                }

            }



            /* Agregar <Invoice><cac:TaxTotal> */
            Set<TransaccionImpuestos> transaccionImpuestosList = new HashSet<>(transaction.getTransaccionImpuestosList());
            invoiceType.getTaxTotal().add(getTaxTotalV21(transaction, new ArrayList<>(transaccionImpuestosList), transaction.getDOC_ImpuestoTotal(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoiceType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TaxTotal - IMPUESTOS TOTALES xxxxxxxxxxxxxxxxxxx");
            }
            /* Agregar <Invoice><cac:LegalMonetaryTotal> */
            boolean noContainsFreeItem = false;
            for (TransaccionLineas transaccionLinea : transaccionLineas) {
                if (Objects.equals(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE, transaccionLinea.getPrecioRefCodigo())) {
                    noContainsFreeItem = true;
                    break;
                }
            }
            BigDecimal taxInclusiveAmount = transaction.getDOC_SinPercepcion();
            BigDecimal payableAmount = transaction.getDOC_MontoTotal();
            BigDecimal lineExtensionAmount = transaction.getDOC_ImporteTotal();
            String socioDocIdentidad = transaction.getSN_DocIdentidad_Tipo();
            BigDecimal otrosCargosValue = transaction.getDOC_OtrosCargos();
            String formSap = transaction.getFE_FormSAP();
            System.out.println("*******************************************************************************************************************************************************************************");
            if (socioDocIdentidad.equalsIgnoreCase("0") && formSap.contains("exportacion")) {
                logger.info("Entro a esta parte de la validacion");
                lineExtensionAmount = transaction.getDOC_MontoTotal();
                taxInclusiveAmount = lineExtensionAmount;
                if(Objects.nonNull(otrosCargosValue)) payableAmount = taxInclusiveAmount.add(otrosCargosValue);
            }
            BigDecimal docDescuentoTotal = transaction.getDOC_DescuentoTotal();
            System.out.println("*******************************************************************************************************************************************************************************");
            invoiceType.setLegalMonetaryTotal(getMonetaryTotal(transaction, lineExtensionAmount, taxInclusiveAmount, noContainsFreeItem, otrosCargosValue, transaction.getANTICIPO_Monto(), payableAmount, docDescuentoTotal, transaction.getDOC_MON_Codigo(), true));
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoiceType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx LegalMonetaryTotal - IMPORTES TOTALES xxxxxxxxxxxxxxxxxxx");
            }

            /* Agregar <Invoice><cac:InvoiceLine> */
            invoiceType.getInvoiceLine().addAll(getAllInvoiceLines(transaction, transaccionLineas, transaction.getTransaccionPropiedadesList(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateInvoiceType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx InvoiceLines(" + invoiceType.getInvoiceLine().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
            if (Optional.ofNullable(monPercepcion).isPresent()) {
                if (monPercepcion.compareTo(BigDecimal.ZERO) > 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATPerceptionDocumentReferences() xxxxxxxxxxxxxxxxxxx");
                    }
                    invoiceType.setInvoiceTypeCode(createInvoiceTypeCode(transaction));
                    invoiceType.getNote().add(createNote("2000", "COMPROBANTE DE PERCEPCIÓN", null));
                    invoiceType.getPaymentTerms().add(createPaymentTerms(transaction));
                    invoiceType.getAllowanceCharge().add(createAllowanceChargeType(transaction));
                }
            }
        } catch (UBLDocumentException e) {
            logger.error("generateInvoiceType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateInvoiceType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateInvoiceType() [" + this.identifier + "]");
        }
        return invoiceType;
    } //generateInvoiceType

    private DocumentReferenceType generateCampoCppc(String valorCampoCppc) {
        DocumentReferenceType invoiceDocumentReference = new DocumentReferenceType();
        DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
        documentTypeCode.setValue(valorCampoCppc);
        invoiceDocumentReference.setDocumentTypeCode(documentTypeCode);
        return invoiceDocumentReference;
    }

    private OrderReferenceType generateOrderReferenceType(String valor) {
        OrderReferenceType orderReferenceType = new OrderReferenceType();
        IDType idType = new IDType();
        idType.setValue(valor);
        orderReferenceType.setID(idType);
        return orderReferenceType;
    }

    private DocumentReferenceType generateDocumentReferenceType(String valor) {
        System.out.println("Valor de la Guia Remision: " + valor);
        DocumentReferenceType documentReferenceType = new DocumentReferenceType();
        DocumentTypeCodeType codeType = new DocumentTypeCodeType();
        codeType.setValue("09");
        IDType idType = new IDType();
        idType.setValue(valor);
        documentReferenceType.setID(idType);
        documentReferenceType.setDocumentTypeCode(codeType);
        return documentReferenceType;
    }

    private NoteType generateNote(String valor) {
        NoteType noteType = new NoteType();
        noteType.setValue(valor);
        return noteType;
    }

    public InvoiceType generateBoletaType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateBoletaType() [" + this.identifier + "]");
        }
        InvoiceType boletaType = null;
        try {
            /* Instanciar el objeto InvoiceType para la BOLETA */
            boletaType = new InvoiceType();
            /* Agregar <Invoice><ext:UBLExtensions> */
            boletaType.setUBLExtensions(getUBLExtensionsSigner());
            /* Agregar <Invoice><cbc:UBLVersionID> */
            boletaType.setUBLVersionID(getUBLVersionID_2_1());
            /* Agregar <Invoice><cbc:CustomizationID> */
            boletaType.setCustomizationID(getCustomizationID_2_0());
            /* Agregar <Invoice><cbc:ProfileID> */
            boletaType.setProfileID(getProfileID(transaction.getTipoOperacionSunat()));
            /* Agregar <Invoice><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            boletaType.setID(getID(transaction.getDOC_Id()));
            /* Agregar <Invoice><cbc:UUID> */
            boletaType.setUUID(getUUID(this.identifier));
            /* Agregar <Invoice><cbc:IssueDate> */
            boletaType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <Invoice><cbc:IssueTime> */
            boletaType.setIssueTime(getIssueTimeDefault());

            /* Agregar <Invoice><cbc:DueDate> */
            if (null != transaction.getDOC_FechaVencimiento()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateBoletaType() [" + this.identifier + "] La transaccion contiene FECHA DE VENCIMIENTO.");
                }
                boletaType.setDueDate(getDueDate(transaction.getDOC_FechaVencimiento()));
            }

            /* Agregar <Invoice><cbc:InvoiceTypeCode> */
            boletaType.setInvoiceTypeCode(getInvoiceTypeCode(transaction.getDOC_Codigo(), transaction.getTipoOperacionSunat()));
            if (!transaction.getTransaccionPropiedadesList().isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateBoletaType() [" + this.identifier + "] La transaccion contiene PROPIEDADES.");
                }
                boletaType.getNote().addAll(getNotes(transaction.getTransaccionPropiedadesList()));
            }

            /* Agregar <Invoice><cbc:DocumentCurrencyCode> */
            boletaType.setDocumentCurrencyCode(getDocumentCurrencyCode(transaction.getDOC_MON_Codigo()));

            /* Agregar <Invoice><cbc:LineCountNumeric> */
            List<TransaccionLineas> transaccionLineas = transaction.getTransaccionLineasList();
            boletaType.setLineCountNumeric(getLineCountNumeric(transaccionLineas.size()));
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx LineCountNumeric(" + boletaType.getLineCountNumeric() + ") xxxxxxxxxxxxxxxxxxx");
            }
            /*
             * Agregar las guias de remision
             *
             * <Invoice><cac:DespatchDocumentReference>
             */
            if (null != transaction.getTransaccionDocrefersList() && 0 < transaction.getTransaccionDocrefersList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateBoletaType() [" + this.identifier + "] La transaccion contiene GUIAS DE REMISION.");
                }
                boletaType.getDespatchDocumentReference().addAll(getDespatchDocumentReferences(transaction.getTransaccionDocrefersList()));
            }

            /*
             * Extraer la condicion de pago de ser el caso.
             */
            if (StringUtils.isNotBlank(transaction.getDOC_CondPago())) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateInvoiceType() [" + this.identifier + "] Extraer la CONDICION DE PAGO.");
                }
                boletaType.getContractDocumentReference().add(getContractDocumentReference(transaction.getDOC_CondPago(), IUBLConfig.CONTRACT_DOC_REF_PAYMENT_COND_CODE));
            }

            /*
             * Extraer la orden de venta y nombre del vendedor de ser el caso
             */
            if (logger.isDebugEnabled()) {
                logger.debug("generateInvoiceType() [" + this.identifier + "] CAMPOS PERSONALIZADOS ");
            }
            List<TransaccionContractdocref> transaccionContractdocrefList = transaction.getTransaccionContractdocrefList();
            if (!transaccionContractdocrefList.isEmpty()) {
                for (TransaccionContractdocref transContractdocref : transaccionContractdocrefList) {
                    boletaType.getContractDocumentReference().add(getContractDocumentReference(transContractdocref.getValor(), transContractdocref.getUsuariocampos().getNombre()));
                }
            }

            /*
             * Agregar DEDUCCION DE ANTICIPOS
             */
            if (null != transaction.getANTICIPO_Monto() && transaction.getANTICIPO_Monto().compareTo(BigDecimal.ZERO) > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateBoletaType() [" + this.identifier + "] La transaccion contiene informacion de ANTICIPOS.");
                }
                boletaType.getAdditionalDocumentReference().addAll(getAdditionalDocumentReferences(transaction.getTransaccionAnticipoList(), transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo()));
            }

            /* Agregar <Invoice><cac:Signature> */
            boletaType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <Invoice><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = getAccountingSupplierPartyV21(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            boletaType.setAccountingSupplierParty(accountingSupplierParty);

            /* Agregar <Invoice><cac:AccountingCustomerParty> */
            CustomerPartyType accountingCustomerParty = getAccountingCustomerPartyV21(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_NomCalle(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getSN_SegundoNombre(), transaction.getSN_EMail());
            boletaType.setAccountingCustomerParty(accountingCustomerParty);

            /*
             * ANTICIPOS RELACIONADOS al comprobante de pago
             *
             * <Invoice><cac:PrepaidPayment>
             */
            if (null != transaction.getANTICIPO_Monto() && transaction.getANTICIPO_Monto().compareTo(BigDecimal.ZERO) > 0 && null != transaction.getTransaccionAnticipoList() && 0 < transaction.getTransaccionAnticipoList().size()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateBoletaType() [" + this.identifier + "] La transaccion contiene informacion de ANTICIPOS RELACIONADOS.");
                }
                boletaType.getPrepaidPayment().addAll(getPrepaidPaymentV21(transaction.getTransaccionAnticipoList()));
            }

            /* Agregar <Invoice><cac:TaxTotal> */
            boletaType.getTaxTotal().add(getTaxTotalV21(transaction, transaction.getTransaccionImpuestosList(), transaction.getDOC_ImpuestoTotal(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TaxTotal - IMPUESTOS TOTALES xxxxxxxxxxxxxxxxxxx");
            }

            /* Agregar <Invoice><cac:LegalMonetaryTotal> */
            boolean noContainsFreeItem = false;
            for (TransaccionLineas transaccionLinea : transaccionLineas) {
                if (Objects.equals(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE, transaccionLinea.getPrecioRefCodigo())) {
                    noContainsFreeItem = true;
                    break;
                }
            }
            BigDecimal docDescuentoTotal = transaction.getDOC_DescuentoTotal();
            boletaType.setLegalMonetaryTotal(getMonetaryTotal(transaction, transaction.getDOC_Importe(), transaction.getDOC_SinPercepcion(), noContainsFreeItem, transaction.getDOC_OtrosCargos(), transaction.getANTICIPO_Monto(), transaction.getDOC_MontoTotal(), docDescuentoTotal, transaction.getDOC_MON_Codigo(), true));
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx LegalMonetaryTotal - IMPORTES TOTALES xxxxxxxxxxxxxxxxxxx");
            }

            /* Agregar <Invoice><cac:InvoiceLine> */
            boletaType.getInvoiceLine().addAll(getAllBoletaLines(transaction, transaccionLineas, transaction.getTransaccionPropiedadesList(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateBoletaType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx InvoiceLines(" + boletaType.getInvoiceLine().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (UBLDocumentException e) {
            logger.error("generateBoletaType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateBoletaType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_342, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateBoletaType() [" + this.identifier + "]");
        }
        return boletaType;
    } //generateBoletaType

    public CreditNoteType generateCreditNoteType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateCreditNoteType() [" + this.identifier + "]");
        }
        CreditNoteType creditNoteType = null;
        try {
            /* Instanciar el objeto CreditNoteType para la NOTA DE CREDITO */
            creditNoteType = new CreditNoteType();

            /* Agregar <CreditNote><ext:UBLExtensions> */
            creditNoteType.setUBLExtensions(getUBLExtensionsSigner());

            /* Agregar <CreditNote><cbc:UBLVersionID> */
            creditNoteType.setUBLVersionID(getUBLVersionID_2_1());

            /* Agregar <CreditNote><cbc:CustomizationID> */
            creditNoteType.setCustomizationID(getCustomizationID_2_0());

            /* Agregar <CreditNote><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNoteType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            creditNoteType.setID(getID(transaction.getDOC_Id()));

            /* Agregar <CreditNote><cbc:UUID> */
            creditNoteType.setUUID(getUUID(this.identifier));

            /* Agregar <CreditNote><cbc:IssueDate> */
            creditNoteType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <CreditNote><cbc:IssueTime> */
            creditNoteType.setIssueTime(getIssueTimeDefault());
            if (transaction.getTransaccionPropiedadesList().size() > 0) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateCreditNoteType() [" + this.identifier + "] La transaccion contiene PROPIEDADES.");
                }
                creditNoteType.getNote().addAll(getNotes(transaction.getTransaccionPropiedadesList()));
            }

            /* Agregar <CreditNote><cbc:DocumentCurrencyCode> */
            creditNoteType.setDocumentCurrencyCode(getDocumentCurrencyCode(transaction.getDOC_MON_Codigo()));

            /* Agregar <CreditNote><cac:DiscrepancyResponse> */
            creditNoteType.getDiscrepancyResponse().add(getDiscrepancyResponse(transaction.getREFDOC_MotivCode(), IUBLConfig.LIST_NAME_CREDIT_NOTE_TYPE, IUBLConfig.URI_CATALOG_09, transaction.getREFDOC_MotivDesc(), transaction.getREFDOC_Id()));

            /* Agregar <CreditNote><cac:BillingReference> */
            creditNoteType.getBillingReference().add(getBillingReference(transaction.getREFDOC_Id(), transaction.getREFDOC_Tipo()));
            List<TransaccionContractdocref> contractdocrefs = new ArrayList<>(transaction.getTransaccionContractdocrefList());
            for (TransaccionContractdocref contract : contractdocrefs) {
                if ("orden_compra".equalsIgnoreCase(contract.getUsuariocampos().getNombre())) {
                    String valorOrderRefence = contract.getValor();
                    Optional<String> optionalValor = Optional.ofNullable(valorOrderRefence).map(s -> s.isEmpty() ? null : s);
                    if (optionalValor.isPresent()) {
                        creditNoteType.setOrderReference(generateOrderReferenceType(valorOrderRefence));
                    }
                    break;
                }
            }


            /* Agregar <CreditNote><cac:Signature> */
            creditNoteType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <CreditNote><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = getAccountingSupplierPartyV21(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            creditNoteType.setAccountingSupplierParty(accountingSupplierParty);

            /* Agregar <CreditNote><cac:AccountingCustomerParty> */
            CustomerPartyType accountingCustomerParty = getAccountingCustomerPartyV21(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_NomCalle(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getSN_SegundoNombre(), transaction.getSN_EMail());
            creditNoteType.setAccountingCustomerParty(accountingCustomerParty);


            /*Agregar <CreditNote><cac:PaymentTerms> */
            if (transaction.getDOC_Serie().contains("F")) { //Aseguramos que sea NC solo para FACTURAS
                // Se agrega lineas solo cuando es mas de una cuota
                List<PaymentTermsType> paymentTerms = new ArrayList<>();
                if (transaction.getTransaccionCuotas().get(0).getFormaPago().equals("Credito")) {
                    paymentTerms = getPaymentTermsNoteCredit(transaction);
                }
                creditNoteType.setPaymentTerms(paymentTerms);
            }


            /* Agregar <CreditNote><cac:TaxTotal> */
            creditNoteType.getTaxTotal().add(getTaxTotalV21(transaction, transaction.getTransaccionImpuestosList(), transaction.getDOC_ImpuestoTotal(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TaxTotal - IMPUESTOS TOTALES xxxxxxxxxxxxxxxxxxx");
            }
            BigDecimal docDescuentoTotal = transaction.getDOC_DescuentoTotal();
            /* Agregar <CreditNote><cac:LegalMonetaryTotal> */
            creditNoteType.setLegalMonetaryTotal(getMonetaryTotal(transaction, transaction.getDOC_Importe(), transaction.getDOC_SinPercepcion(), false, transaction.getDOC_OtrosCargos(), null, transaction.getDOC_MontoTotal(), docDescuentoTotal, transaction.getDOC_MON_Codigo(), false));
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx LegalMonetaryTotal - IMPORTES TOTALES xxxxxxxxxxxxxxxxxxx");
            }
            BigDecimal monPercepcion = transaction.getDOC_MonPercepcion();
            if (Optional.ofNullable(monPercepcion).isPresent()) {
                if (monPercepcion.compareTo(BigDecimal.ZERO) > 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATPerceptionDocumentReferences() xxxxxxxxxxxxxxxxxxx");
                    }
                    creditNoteType.getNote().add(createNote("2000", "COMPROBANTE DE PERCEPCIÓN", null));
                    creditNoteType.getAllowanceCharge().add(createAllowanceChargeType(transaction));
                }
            }

            /*
             * En el TAG LineExtensionAmount es donde va el SUBTOTAL segun SUNAT, por lo tanto:
             * transaction.getDOCImporte() = SUBTOTAL
             * ¡¡CONSULTAR!!
             */
            if (logger.isDebugEnabled()) {
                logger.debug("generateCreditNoteType() [" + this.identifier + "] Obteniendo el SUBTOTAL");
            }
            BigDecimal subtotalValue = getSubtotalValueFromTransaction(transaction.getTransaccionTotalesList());
            if (subtotalValue == null) {
                subtotalValue = BigDecimal.ZERO;
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx Subtotal: " + subtotalValue);
            }


            /* Agregar <CreditNote><cac:CreditNoteLine> */
            creditNoteType.getCreditNoteLine().addAll(getAllCreditNoteLines(transaction, transaction.getTransaccionLineasList(), transaction.getTransaccionPropiedadesList(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateCreditNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx CreditNoteLines(" + creditNoteType.getCreditNoteLine().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (UBLDocumentException e) {
            logger.error("generateCreditNoteType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateCreditNoteType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_343, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateCreditNoteType() [" + this.identifier + "]");
        }
        return creditNoteType;
    } //generateCreditNoteType

    public DebitNoteType generateDebitNoteType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateDebitNoteType() [" + this.identifier + "]");
        }
        DebitNoteType debitNoteType = null;
        try {
            /* Instanciar el objeto DebitNoteType para la NOTA DE DEBITO */
            debitNoteType = new DebitNoteType();


            /* Agregar <DebitNote><ext:UBLExtensions> */
            debitNoteType.setUBLExtensions(getUBLExtensionsSigner());

            /* Agregar <DebitNote><cbc:UBLVersionID> */
            debitNoteType.setUBLVersionID(getUBLVersionID_2_1());

            /* Agregar <DebitNote><cbc:CustomizationID> */
            debitNoteType.setCustomizationID(getCustomizationID_2_0());

            /* Agregar <DebitNote><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNoteType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            debitNoteType.setID(getID(transaction.getDOC_Id()));

            /* Agregar <DebitNote><cbc:UUID> */
            debitNoteType.setUUID(getUUID(this.identifier));

            /* Agregar <DebitNote><cbc:IssueDate> */
            debitNoteType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <DebitNote><cbc:IssueTime> */
            debitNoteType.setIssueTime(getIssueTimeDefault());
            if (!transaction.getTransaccionPropiedadesList().isEmpty()) {
                if (logger.isInfoEnabled()) {
                    logger.info("generateDebitNoteType() [" + this.identifier + "] La transaccion contiene PROPIEDADES.");
                }
                debitNoteType.getNote().addAll(getNotes(transaction.getTransaccionPropiedadesList()));
            }

            /* Agregar <DebitNote><cbc:DocumentCurrencyCode> */
            debitNoteType.setDocumentCurrencyCode(getDocumentCurrencyCode(transaction.getDOC_MON_Codigo()));

            /* Agregar <DebitNote><cac:DiscrepancyResponse> */
            debitNoteType.getDiscrepancyResponse().add(getDiscrepancyResponse(transaction.getREFDOC_MotivCode(), IUBLConfig.LIST_NAME_DEBIT_NOTE_TYPE, IUBLConfig.URI_CATALOG_10, transaction.getREFDOC_MotivDesc(), transaction.getREFDOC_Id()));

            /* Agregar <DebitNote><cac:BillingReference> */
            debitNoteType.getBillingReference().add(getBillingReference(transaction.getREFDOC_Id(), transaction.getREFDOC_Tipo()));

            /* Agregar <DebitNote><cac:Signature> */
            debitNoteType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <DebitNote><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = getAccountingSupplierPartyV21(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            debitNoteType.setAccountingSupplierParty(accountingSupplierParty);

            /* Agregar <DebitNote><cac:AccountingCustomerParty> */
            CustomerPartyType accountingCustomerParty = getAccountingCustomerPartyV21(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_NomCalle(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getSN_SegundoNombre(), transaction.getSN_EMail());
            debitNoteType.setAccountingCustomerParty(accountingCustomerParty);

            /* Agregar <DebitNote><cac:TaxTotal> */
            debitNoteType.getTaxTotal().add(getTaxTotalV21(transaction, transaction.getTransaccionImpuestosList(), transaction.getDOC_ImpuestoTotal(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TaxTotal - IMPUESTOS TOTALES xxxxxxxxxxxxxxxxxxx");
            }
            BigDecimal docDescuentoTotal = transaction.getDOC_DescuentoTotal();
            /* Agregar <DebitNote><cac:RequestedMonetaryTotal> */
            debitNoteType.setRequestedMonetaryTotal(getMonetaryTotal(transaction, transaction.getDOC_Importe(), transaction.getDOC_SinPercepcion(), false, transaction.getDOC_OtrosCargos(), null, transaction.getDOC_MontoTotal(), docDescuentoTotal, transaction.getDOC_MON_Codigo(), false));
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx RequestedMonetaryTotal - IMPORTES TOTALES xxxxxxxxxxxxxxxxxxx");
            }

            /*
             * En el TAG LineExtensionAmount es donde va el SUBTOTAL segun SUNAT, por lo tanto:
             * transaction.getDOCImporte() = SUBTOTAL
             * ¡¡CONSULTAR!!
             */
            if (logger.isDebugEnabled()) {
                logger.debug("generateDebitNoteType() [" + this.identifier + "] Obteniendo el SUBTOTAL");
            }
            BigDecimal subtotalValue = getSubtotalValueFromTransaction(transaction.getTransaccionTotalesList());
            if (subtotalValue == null) {
                subtotalValue = BigDecimal.ZERO;
            }
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx Subtotal: " + subtotalValue);
            }

            /* Agregar items <DebitNote><cac:DebitNoteLine> */
            debitNoteType.getDebitNoteLine().addAll(getAllDebitNoteLines(transaction, transaction.getTransaccionLineasList(), transaction.getTransaccionPropiedadesList(), transaction.getDOC_MON_Codigo()));
            if (logger.isInfoEnabled()) {
                logger.info("generateDebitNoteType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx DebitNoteLines(" + debitNoteType.getDebitNoteLine().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (UBLDocumentException e) {
            logger.error("generateDebitNoteType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateDebitNoteType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_344, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateDebitNoteType() [" + this.identifier + "]");
        }
        return debitNoteType;
    } //generateDebitNoteType

    public PerceptionType generatePerceptionType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generatePerceptionType() [" + this.identifier + "]");
        }
        PerceptionType perceptionType = null;
        try {
            /* Instanciar el objeto PerceptionType para la PERCEPCION */
            perceptionType = new PerceptionType();


            /* Agregar <Perception><ext:UBLExtensions> */
            perceptionType.setUblExtensions(getUBLExtensionsSigner());

            /* Agregar <Perception><cbc:UBLVersionID> */
            perceptionType.setUblVersionID(getUBLVersionID_2_0());

            /* Agregar <Perception><cbc:CustomizationID> */
            perceptionType.setCustomizationID(getCustomizationID_1_0());

            /* Agregar <Perception><cac:Signature> */
            perceptionType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <Perception><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            perceptionType.setId(getID(transaction.getDOC_Id()));

            /* Agregar <Perception><cbc:IssueDate> */
            perceptionType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <Perception><cac:AgentParty> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx AgentParty - EMISOR xxxxxxxxxxxxxxxxxxx");
            }
            PartyType agentParty = getAgentParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            perceptionType.setAgentParty(agentParty);

            /* Agregar <Perception><cac:ReceiverParty> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx ReceiverParty - RECEPTOR xxxxxxxxxxxxxxxxxxx");
            }
            PartyType receiverParty = getAgentParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_Direccion(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getPersonContacto(), transaction.getSN_EMail());
            perceptionType.setReceiverParty(receiverParty);

            /* Agregar <Perception><sac:SUNATPerceptionSystemCode> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATPerceptionSystemCode - REGIMEN DE PERCEPCION xxxxxxxxxxxxxxxxxxx");
            }
            SUNATPerceptionSystemCodeType sunatPerceptionSystemCode = new SUNATPerceptionSystemCodeType();
            sunatPerceptionSystemCode.setValue(transaction.getRET_Regimen());
            perceptionType.setSunatPerceptionSystemCode(sunatPerceptionSystemCode);

            /* Agregar <Perception><sac:SUNATPerceptionPercent> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATRetentionPercent - TASA DE PERCEPCION xxxxxxxxxxxxxxxxxxx");
            }
            SUNATPerceptionPercentType sunatPerceptionPercent = new SUNATPerceptionPercentType();
            sunatPerceptionPercent.setValue(transaction.getRET_Tasa());
            perceptionType.setSunatPerceptionPercent(sunatPerceptionPercent);

            /* Agregar <Perception><cbc:Note> */
            if (StringUtils.isNotBlank(transaction.getObservaciones())) {
                if (logger.isInfoEnabled()) {
                    logger.info("generatePerceptionType() [" + this.identifier + "] Agregando Observaciones: " + transaction.getObservaciones());
                }
                perceptionType.setNote(getNote(transaction.getObservaciones()));
            }

            /* Agregar <Perception><cbc:TotalInvoiceAmount> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TotalInvoiceAmount - IMPORTE TOTAL PERCIBIDO xxxxxxxxxxxxxxxxxxx");
            }
            perceptionType.setTotalInvoiceAmount(getTotalInvoiceAmount(transaction.getDOC_MontoTotal(), transaction.getDOC_MON_Codigo()));

            /* Agregar <Perception><sac:SUNATTotalCashed> */
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATTotalCashed - IMPORTE TOTAL COBRADO xxxxxxxxxxxxxxxxxxx");
            }
            SUNATTotalCashedType sunatTotalCashed = new SUNATTotalCashedType();
            sunatTotalCashed.setValue(transaction.getImportePagado().setScale(2, BigDecimal.ROUND_HALF_UP));
            sunatTotalCashed.setCurrencyID(CurrencyCodeContentType.valueOf(transaction.getMonedaPagado()).value());
            perceptionType.setSunatTotalCashed(sunatTotalCashed);

            /* Agregar <Perception><sac:SUNATPerceptionDocumentReference> */
            perceptionType.getSunatPerceptionDocumentReference().addAll(getAllSUNATPerceptionDocumentReferences(transaction.getTransaccionComprobantePagoList()));
            if (logger.isInfoEnabled()) {
                logger.info("generatePerceptionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATPerceptionDocumentReferences(" + perceptionType.getSunatPerceptionDocumentReference().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (UBLDocumentException e) {
            logger.error("generatePerceptionType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generatePerceptionType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_351, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generatePerceptionType() [" + this.identifier + "]");
        }
        return perceptionType;
    } //generatePerceptionType

    public RetentionType generateRetentionType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateRetentionType() [" + this.identifier + "]");
        }
        RetentionType retentionType = null;
        try {
            /* Instanciar el objeto RetentionType para la RETENCION */
            retentionType = new RetentionType();


            /* Agregar <Retention><ext:UBLExtensions> */
            retentionType.setUblExtensions(getUBLExtensionsSigner());

            /* Agregar <Retention><cbc:UBLVersionID> */
            retentionType.setUblVersionID(getUBLVersionID_2_0());

            /* Agregar <Retention><cbc:CustomizationID> */
            retentionType.setCustomizationID(getCustomizationID_1_0());

            /* Agregar <Retention><cac:Signature> */
            retentionType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <Retention><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            retentionType.setId(getID(transaction.getDOC_Id()));

            /* Agregar <Retention><cbc:IssueDate> */
            retentionType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));

            /* Agregar <Retention><cac:AgentParty> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx AgentParty - EMISOR xxxxxxxxxxxxxxxxxxx");
            }
            PartyType agentParty = getAgentParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            retentionType.setAgentParty(agentParty);

            /* Agregar <Retention><cac:ReceiverParty> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx ReceiverParty - RECEPTOR xxxxxxxxxxxxxxxxxxx");
            }
            PartyType receiverParty = getReceiverParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial(), transaction.getSN_NombreComercial(), transaction.getSN_DIR_Direccion(), transaction.getSN_DIR_Departamento(), transaction.getSN_DIR_Provincia(), transaction.getSN_DIR_Distrito(), transaction.getSN_DIR_Ubigeo(), transaction.getSN_DIR_Pais(), transaction.getPersonContacto(), transaction.getSN_EMail());
            retentionType.setReceiverParty(receiverParty);

            /* Agregar <Retention><sac:SUNATRetentionSystemCode> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATRetentionSystemCode - REGIMEN DE RETENCION xxxxxxxxxxxxxxxxxxx");
            }
            SUNATRetentionSystemCodeType sunatRetentionSystemCode = new SUNATRetentionSystemCodeType();
            sunatRetentionSystemCode.setValue(transaction.getRET_Regimen());
            retentionType.setSunatRetentionSystemCode(sunatRetentionSystemCode);

            /* Agregar <Retention><sac:SUNATRetentionPercent> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATRetentionPercent - TASA DE RETENCION xxxxxxxxxxxxxxxxxxx");
            }
            SUNATRetentionPercentType sunatRetentionPercent = new SUNATRetentionPercentType();
            sunatRetentionPercent.setValue(transaction.getRET_Tasa());
            retentionType.setSunatRetentionPercent(sunatRetentionPercent);

            /* Agregar <Retention><cbc:Note> */
            retentionType.setNote(getNote(transaction.getObservaciones()));

            /* Agregar <Retention><cbc:TotalInvoiceAmount> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx TotalInvoiceAmount - IMPORTE TOTAL RETENIDO xxxxxxxxxxxxxxxxxxx");
            }
            retentionType.setTotalInvoiceAmount(getTotalInvoiceAmount(transaction.getDOC_MontoTotal(), transaction.getDOC_MON_Codigo()));

            /* Agregar <Retention><sac:SUNATTotalPaid> */
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATTotalPaid - IMPORTE TOTAL PAGADO xxxxxxxxxxxxxxxxxxx");
            }
            SUNATTotalPaid sunatTotalPaid = new SUNATTotalPaid();
            sunatTotalPaid.setValue(transaction.getImportePagado().setScale(2, BigDecimal.ROUND_HALF_UP));
            sunatTotalPaid.setCurrencyID(CurrencyCodeContentType.valueOf(transaction.getMonedaPagado()).value());
            retentionType.setTotalPaid(sunatTotalPaid);

            /* Agregar <Retention><sac:SUNATRetentionDocumentReference> */
            retentionType.getSunatRetentionDocumentReference().addAll(getAllSUNATRetentionDocumentReferences(transaction.getTransaccionComprobantePagoList()));
            if (logger.isInfoEnabled()) {
                logger.info("generateRetentionType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx SUNATRetentionDocumentReferences(" + retentionType.getSunatRetentionDocumentReference().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (UBLDocumentException e) {
            logger.error("generateRetentionType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateRetentionType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_352, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateRetentionType() [" + this.identifier + "]");
        }
        return retentionType;
    } //generateRetentionType

    public DespatchAdviceType generateDespatchAdviceType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateDespatchAdviceType() [" + this.identifier + "]");
        }
        DespatchAdviceType despatchAdviceType = null;

        /** JOSE LUIS <3*/

        try {
            /* Instanciar el objeto DespatchAdviceType para la GUIA DE REMISION */
            despatchAdviceType = new DespatchAdviceType();
            /* Agregar <DespatchAdvice><ext:UBLExtensions> */
            despatchAdviceType.setUBLExtensions(getUBLExtensionsSigner());
            /* Agregar <<DespatchAdvice><cbc:UBLVersionID> */
            despatchAdviceType.setUBLVersionID(getUBLVersionID_2_1());
            /* Agregar <<DespatchAdvice><cbc:CustomizationID> */
            despatchAdviceType.setCustomizationID(getCustomizationID_2_0());
            /* Agregar <DespatchAdvice><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateDespatchAdviceType() [" + this.identifier + "] Agregando DOC_Id: " + transaction.getDOC_Id());
            }
            despatchAdviceType.setID(getID(transaction.getDOC_Id()));
            /* Agregar <DespatchAdvice><cbc:IssueDate> */
            despatchAdviceType.setIssueDate(getIssueDate(transaction.getDOC_FechaEmision()));
            /* Agregar <DespatchAdvice><cbc:IssueTime> */
            despatchAdviceType.setIssueTime(getIssueTimeDefault());

            despatchAdviceType.setNote(getDespatchAdviceNote(transaction));
            /* Agregar <DespatchAdvice><cbc:DespatchAdviceTypeCode> */
            despatchAdviceType.setDespatchAdviceTypeCode(getDespatchAdviceTypeCode(transaction.getDOC_Codigo()));
            /* Agregar <DespatchAdvice><cbc:NoteType> */


            /***/

            /* Agregar <DespatchAdvice><cac:DespatchSupplierParty> */
            despatchAdviceType.setDespatchSupplierParty(getDespatchSupplierParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial()));

            /* Agregar <DespatchAdvice><cac:DeliveryCustomerParty> */
            if (transaction.getTransaccionGuiaRemision().getCodigoMotivo() != null && "02".equals(transaction.getTransaccionGuiaRemision().getCodigoMotivo())) {/*Motivo de Venta: Compra - 02*/
                despatchAdviceType.setDeliveryCustomerParty(getDeliveryCustomerParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial()));
            } else {
                despatchAdviceType.setDeliveryCustomerParty(getDeliveryCustomerParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial()));
            }

            /* Agregar <DespatchAdvice><cac:AdditionalDocumentReference> */
            if (transaction.getTransaccionGuiaRemision().getTipoDocRelacionadoTrans() != null && transaction.getTransaccionGuiaRemision().getTipoDocRelacionadoTrans().equals("50")) {
                despatchAdviceType.getAdditionalDocumentReference().add(insertarDocRefers(transaction.getTransaccionGuiaRemision(), transaction));
            }

            /* Agregar <DespatchAdvice><cac:SellerSupplierParty> */
            if (transaction.getTransaccionGuiaRemision().getCodigoMotivo() != null && (transaction.getTransaccionGuiaRemision().getCodigoMotivo().equals("02") || transaction.getTransaccionGuiaRemision().getCodigoMotivo().equals("07") ||
                    transaction.getTransaccionGuiaRemision().getCodigoMotivo().equals("13"))) {
                despatchAdviceType.setSellerSupplierParty(getSellerSupplierParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial()));
            }

            /* Agregar <DespatchAdvice><cac:BuyerCustomerParty> */
            if (transaction.getTransaccionGuiaRemision().getCodigoMotivo() != null && (transaction.getTransaccionGuiaRemision().getCodigoMotivo().equals("03") || transaction.getTransaccionGuiaRemision().getCodigoMotivo().equals("13")))
                despatchAdviceType.setBuyerCustomerParty(getBuyerCustomerParty(transaction.getSN_DocIdentidad_Nro(), transaction.getSN_DocIdentidad_Tipo(), transaction.getSN_RazonSocial()));
            /***/


            /* Agregar <DespatchAdvice><cac:Shipment> */
            despatchAdviceType.setShipment(getShipment(transaction.getTransaccionGuiaRemision(), transaction));

            /* Agregar <DespatchAdvice><cac:DespatchLine> */
            despatchAdviceType.getDespatchLine().addAll(getAllDespatchLines(transaction.getTransaccionLineasList()));


            if (logger.isInfoEnabled()) {
                logger.info("generateDespatchAdviceType() [" + this.identifier + "] xxxxxxxxxxxxxxxxxxx DespatchLines(" + despatchAdviceType.getDespatchLine().size() + ") xxxxxxxxxxxxxxxxxxx");
            }
        } catch (Exception e) {
            logger.error("generateDespatchAdviceType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_369, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-generateDespatchAdviceType() [" + this.identifier + "]");
        }
        return despatchAdviceType;
    } //generateDespatchAdviceType

    private DocumentReferenceType insertarDocRefers(TransaccionGuiaRemision guia, Transaccion transaccion) {

        DocumentReferenceType documentReferenceType = new DocumentReferenceType();

        DocumentTypeCodeType codeType = new DocumentTypeCodeType();
        codeType.setListAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
        codeType.setListName(IUBLConfig.DOCUMENT_REL);
        codeType.setListURI(IUBLConfig.URI_CATALOG_61);
        codeType.setValue(guia.getTipoDocRelacionadoTrans());
        documentReferenceType.setDocumentTypeCode(codeType);

        DocumentTypeType documentType = new DocumentTypeType();
        documentType.setValue(guia.getTipoDocRelacionadoTransDesc());
        documentReferenceType.setDocumentType(documentType);

        if (guia.getDocumentoRelacionadoTrans() != null && !guia.getDocumentoRelacionadoTrans().isEmpty()) {
            IDType idTypeRelacionado = new IDType();
            idTypeRelacionado.setValue(guia.getDocumentoRelacionadoTrans());
            documentReferenceType.setID(idTypeRelacionado);
        }

        PartyType issuerParty = new PartyType();
        PartyIdentificationType partyIdentificationParty = new PartyIdentificationType();
        IDType idIdentification = new IDType();
        idIdentification.setSchemeID(transaccion.getDocIdentidad_Tipo());
        idIdentification.setSchemeAgencyName(IUBLConfig.SCHEME_AGENCY_NAME_PE_SUNAT);
        idIdentification.setSchemeURI(IUBLConfig.URI_CATALOG_06);
        idIdentification.setValue(transaccion.getDocIdentidad_Nro());
        partyIdentificationParty.setID(idIdentification);
        issuerParty.getPartyIdentification().add(partyIdentificationParty);
        documentReferenceType.setIssuerParty(issuerParty);

        return documentReferenceType;
    }

    public List<NoteType> getDespatchAdviceNote(Transaccion transaccion) {
        List<NoteType> noteTypeList = new ArrayList<>();
        //
        NoteType noteType = new NoteType();
        boolean texto = false;
        for (int i = 0; i < transaccion.getTransaccionContractdocrefList().size(); i++) {
            if ("guia_comentarios".equals(transaccion.getTransaccionContractdocrefList().get(i).getUsuariocampos().getNombre())) {
                TransaccionContractdocref objecto = transaccion.getTransaccionContractdocrefList().get(i);
                if (objecto != null && !objecto.getValor().isEmpty()) {
                    noteType.setValue(objecto.getValor());
                    noteTypeList.add(noteType);
                    texto = true;
                    break;
                }
            }
        }
        return noteTypeList;
    }
    private List<InvoiceLineType> getAllInvoiceLines(Transaccion transaccion, List<TransaccionLineas> transaccionLineasList, List<TransaccionPropiedades> transaccionPropiedadesList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllInvoiceLines() [" + this.identifier + "] transaccionLineasList: " + transaccionLineasList + " currencyCode: " + currencyCode);
        }
        if (null == transaccionLineasList || transaccionLineasList.isEmpty()) {
            logger.error("getAllInvoiceLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<InvoiceLineType> invoiceLineList = new ArrayList<>();

        transaccionLineasList.sort(Comparator.comparing(linea -> linea.getTransaccionLineasPK().getNroOrden()));

        try {
            for (TransaccionLineas transaccionLinea : transaccionLineasList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Extrayendo informacion del item...");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "]\n" + "NroOrden: " + transaccionLinea.getTransaccionLineasPK().getNroOrden() + "\n" + "Cantidad: " + transaccionLinea.getCantidad() + "\tUnidad: " + transaccionLinea.getUnidad() + "\tUnidadSunat: " + transaccionLinea.getUnidadSunat() + "\tTotalLineaSinIGV: " + transaccionLinea.getTotalLineaSinIGV() + "\n" + "PrecioRefCodigo: " + transaccionLinea.getPrecioRefCodigo() + "\tPrecioIGV: " + transaccionLinea.getPrecioIGV() + "\tPrecioRefMonto: " + transaccionLinea.getPrecioRefMonto() + "\n" + "DCTOMonto: " + transaccionLinea.getDSCTOMonto() + "\tDescripcion: " + transaccionLinea.getDescripcion() + "\tCodArticulo: " + transaccionLinea.getCodArticulo());
                }
                InvoiceLineType invoiceLine = new InvoiceLineType();
                /* Agregar <cac:InvoiceLine><cbc:ID> */
                invoiceLine.setID(getID(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));
                /*
                 * Agregar UNIDAD DE MEDIDA segun SUNAT
                 * <cac:InvoiceLine><cbc:InvoicedQuantity>
                 */
                invoiceLine.setInvoicedQuantity(getInvoicedQuantity(transaccionLinea.getCantidad(), transaccionLinea.getUnidadSunat()));
                /*
                 * Agregar UNIDAD DE MEDIDA segun VENTURA
                 * <cac:InvoiceLine><cbc:Note>
                 */
                if (StringUtils.isNotBlank(transaccionLinea.getUnidad())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando UNIDAD DE MEDIDA (VENTURA).");
                    }
                    invoiceLine.getNote().add(getNote(transaccionLinea.getUnidad()));
                }
                /* Agregar <cac:InvoiceLine><cbc:LineExtensionAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando TOTAL_LINEA_SIN_IGV(" + transaccionLinea.getTotalLineaSinIGV() + ") - TAG LineExtensionAmount.");
                }
                invoiceLine.setLineExtensionAmount(getLineExtensionAmount(transaccionLinea.getTotalLineaSinIGV(), currencyCode));
                /*
                 * Op. Onerosa:     tiene precio unitario
                 * Op. No Onerosa:  tiene valor referencial
                 *
                 * Agregar PRECIO UNITARIO / VALOR REFERENCIAL
                 * <cac:InvoiceLine><cac:PricingReference>
                 */
                if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando PRECIO_IGV(" + transaccionLinea.getPrecioIGV() + ") - TAG PricingReference.");
                    }
                    invoiceLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                } else if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando PRECIO_REF_MONTO(" + transaccionLinea.getPrecioRefMonto() + ") - TAG PricingReference.");
                    }
                    invoiceLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_UNIT_VALUE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                } else if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REGULATED_RATES)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando Otro Precio de venta(" + transaccionLinea.getPrecioRefCodigo() + ") - TAG PricingReference.");
                    }
                    invoiceLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_UNIT_VALUE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                }
                /*
                 * Agregar DETRACCIONES
                 *
                 * SERVICIO DE TRANSPORTE DE CARGA
                 */
                if (transaccionLinea.getTransaccion().getCodigoDetraccion() != null &&
                        transaccionLinea.getTransaccion().getCodigoDetraccion().equals("027")) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DETRACCION - Servicio de Transporte de Carga.");
                    }
                    invoiceLine.getDelivery().add(getDeliveryForLine(transaccionLinea.getCodUbigeoDestino(), transaccionLinea.getDirecDestino(), transaccionLinea.getCodUbigeoOrigen(), transaccionLinea.getDirecOrigen(), transaccionLinea.getDetalleViaje(), transaccionLinea.getValorCargaEfectiva(), transaccionLinea.getValorCargaUtil(), transaccionLinea.getValorTransporte(), transaccionLinea.getConfVehicular(), transaccionLinea.getCUtilVehiculo(), transaccionLinea.getCEfectivaVehiculo(), transaccionLinea.getValorRefTM(), transaccionLinea.getValorPreRef(), transaccionLinea.getFactorRetorno()));
                }

                /*
                 * Agregar DESCUENTO DE LINEA
                 *
                 * ChargeIndicatorType:
                 *      El valor FALSE representa que es un DESCUENTO DE LINEA
                 *
                 * <cac:InvoiceLine><cac:AllowanceCharge>
                 */
                if (null != transaccionLinea.getDSCTOMonto() && transaccionLinea.getDSCTOMonto().compareTo(BigDecimal.ZERO) == 1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DSCTO_MONTO(" + transaccionLinea.getDSCTOMonto() + ") DSCTO_PORCENTAJE(" + transaccionLinea.getDSCTOPorcentaje() + ") TOTAL_BRUTO(" + transaccionLinea.getTotalBruto() + ") - TAG AllowanceCharge.");
                    }
                    BigDecimal montoCero = new BigDecimal("0.0");
                    invoiceLine.getAllowanceCharge().add(getAllowanceCharge(transaccionLinea.getDSCTOMonto(), transaccionLinea.getDSCTOMonto(), false, transaccionLinea.getDSCTOPorcentaje(), transaccionLinea.getDSCTOMonto(), transaccionLinea.getTotalBruto(), currencyCode, "00", montoCero, montoCero));
                }

                /*
                 * Agregar IMPUESTOS DE LINEA
                 * <cac:InvoiceLine><cac:TaxTotal>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando IMPUESTOS DE LINEA.");
                }
                invoiceLine.getTaxTotal().add(getTaxTotalLineV21(transaccionLinea.getTransaccionLineaImpuestosList(), transaccionLinea.getLineaImpuesto(), currencyCode));

                /* Agregar <cac:InvoiceLine><cac:Item> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transaccionLinea.getDescripcion() + "] COD_ARTICULO[" + transaccionLinea.getCodArticulo() + "] COD_SUNAT[" + transaccionLinea.getCodSunat() + "] COD_PROD_GS1[" + transaccionLinea.getCodProdGS1() + "] - TAG Item.");
                }
                invoiceLine.setItem(getItemForLine(transaccion, transaccionLinea, transaccionLinea.getDescripcion(), transaccionLinea.getCodArticulo(), transaccionLinea.getCodSunat(), transaccionLinea.getCodProdGS1(), transaccionPropiedadesList));

                /*
                 * Agregar VALOR UNITARIO
                 * <cac:InvoiceLine><cac:Price>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando VALOR UNITARIO - TAG Price.");
                }
                invoiceLine.setPrice(getPriceForLine(transaccionLinea.getTransaccionLineasBillrefList(), currencyCode));
                invoiceLineList.add(invoiceLine);
            } //for
            for (int i = 0; i < IUBLConfig.lstImporteIGV.size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Totales con IGV: " + IUBLConfig.lstImporteIGV.get(i));
                }
            }
        } catch (UBLDocumentException e) {
            logger.error("getAllInvoiceLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllInvoiceLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllInvoiceLines() [" + this.identifier + "]");
        }
        return invoiceLineList;
    } //getAllInvoiceLines

    private List<InvoiceLineType> getAllBoletaLines(Transaccion transaccion, List<TransaccionLineas> transaccionLineasList, List<TransaccionPropiedades> transaccionPropiedadesList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllBoletaLines() [" + this.identifier + "] transaccionLineasList: " + transaccionLineasList + " currencyCode: " + currencyCode);
        }
        if (null == transaccionLineasList || transaccionLineasList.isEmpty()) {
            logger.error("getAllBoletaLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<InvoiceLineType> boletaLineList = new ArrayList<>(transaccionLineasList.size());
        try {
            for (int i = 0; i < transaccionLineasList.size(); i++) {
                IUBLConfig.lstImporteIGV.add(i, transaccionLineasList.get(i).getTotalLineaConIGV());
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] ImportesConIGV()" + IUBLConfig.lstImporteIGV.get(i));
                }
            }

            transaccionLineasList.sort(Comparator.comparing(linea -> linea.getTransaccionLineasPK().getNroOrden()));

            // IUBLConfig.lstImporteIGV = new ArrayList<BigDecimal>();
            for (TransaccionLineas transaccionLinea : transaccionLineasList) {

                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] Extrayendo informacion del item...");
                }
                String precioRefCodigo = transaccionLinea.getPrecioRefCodigo();
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "]\n" + "NroOrden: " + transaccionLinea.getTransaccionLineasPK().getNroOrden() + "\n" + "Cantidad: " + transaccionLinea.getCantidad() + "\tUnidad: " + transaccionLinea.getUnidad() + "\tUnidadSunat: " + transaccionLinea.getUnidadSunat() + "\tTotalLineaSinIGV: " + transaccionLinea.getTotalLineaSinIGV() + "\tTotalLineaConIGV: " + transaccionLinea.getTotalLineaConIGV() + "\n" + "PrecioRefCodigo: " + precioRefCodigo + "\tPrecioIGV: " + transaccionLinea.getPrecioIGV() + "\tPrecioRefMonto: " + transaccionLinea.getPrecioRefMonto() + "\n" + "DCTOMonto: " + transaccionLinea.getDSCTOMonto() + "\tDescripcion: " + transaccionLinea.getDescripcion() + "\tCodArticulo: " + transaccionLinea.getCodArticulo());
                }
                InvoiceLineType boletaLine = new InvoiceLineType();

                /* Agregar <cac:InvoiceLine><cbc:ID> */
                boletaLine.setID(getID(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));

                /*
                 * Agregar UNIDAD DE MEDIDA segun SUNAT
                 * <cac:InvoiceLine><cbc:InvoicedQuantity>
                 */
                boletaLine.setInvoicedQuantity(getInvoicedQuantity(transaccionLinea.getCantidad(), transaccionLinea.getUnidadSunat()));

                /*
                 * Agregar UNIDAD DE MEDIDA segun VENTURA
                 * <cac:InvoiceLine><cbc:Note>
                 */
                if (StringUtils.isNotBlank(transaccionLinea.getUnidad())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando UNIDAD DE MEDIDA (VENTURA).");
                    }
                    boletaLine.getNote().add(getNote(transaccionLinea.getUnidad()));
                }

                /* Agregar <cac:InvoiceLine><cbc:LineExtensionAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando TOTAL_LINEA_SIN_IGV(" + transaccionLinea.getTotalLineaSinIGV() + ") - TAG LineExtensionAmount.");
                }
                boletaLine.setLineExtensionAmount(getLineExtensionAmount(transaccionLinea.getTotalLineaSinIGV(), currencyCode));

                /*
                 * Op. Onerosa:     tiene precio unitario
                 * Op. No Onerosa:  tiene valor referencial
                 *
                 * Agregar PRECIO UNITARIO / VALOR REFERENCIAL
                 * <cac:InvoiceLine><cac:PricingReference>
                 */
                if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                    boletaLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                } else if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                    boletaLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                }
                /*
                 * Agregar DESCUENTO DE LINEA
                 *
                 * ChargeIndicatorType:
                 *      El valor FALSE representa que es un DESCUENTO DE LINEA
                 *
                 * <cac:InvoiceLine><cac:AllowanceCharge>
                 */
                if (null != transaccionLinea.getDSCTOMonto() && transaccionLinea.getDSCTOMonto().compareTo(BigDecimal.ZERO) == 1) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando DSCTO_MONTO(" + transaccionLinea.getDSCTOMonto() + ") DSCTO_PORCENTAJE(" + transaccionLinea.getDSCTOPorcentaje() + ") TOTAL_BRUTO(" + transaccionLinea.getTotalBruto() + ") - TAG AllowanceCharge.");
                    }
                    BigDecimal montoCero = new BigDecimal("00");
                    boletaLine.getAllowanceCharge().add(getAllowanceCharge(transaccionLinea.getDSCTOMonto(), transaccionLinea.getDSCTOMonto(), false, transaccionLinea.getDSCTOPorcentaje(), transaccionLinea.getDSCTOMonto(), transaccionLinea.getTotalBruto(), currencyCode, "00", montoCero, montoCero));

                    //ESTO FUE LO QUE SE ENCONTRO EN EL CODIGO ¡¡CONFIRMAR!!
//                    boletaLine.getAllowanceCharge().add(getAllowanceCharge(false, transaccionLinea.getDSCTOMonto(),
//                            transaccionLinea.getDSCTOMonto(),transaccionLinea.getDSCTOMonto(), currencyCode));
                }

                /*
                 * Agregar IMPUESTOS DE LINEA
                 * <cac:InvoiceLine><cac:TaxTotal>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando IMPUESTOS DE LINEA.");
                }
                boletaLine.getTaxTotal().add(getTaxTotalLineV21(transaccionLinea.getTransaccionLineaImpuestosList(), transaccionLinea.getLineaImpuesto(), currencyCode));

                /* Agregar <cac:InvoiceLine><cac:Item> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transaccionLinea.getDescripcion() + "] COD_ARTICULO[" + transaccionLinea.getCodArticulo() + "] COD_SUNAT[" + transaccionLinea.getCodSunat() + "] COD_PROD_GS1[" + transaccionLinea.getCodProdGS1() + "] - TAG Item.");
                }
                boletaLine.setItem(getItemForLine(transaccion, transaccionLinea, transaccionLinea.getDescripcion(), transaccionLinea.getCodArticulo(), transaccionLinea.getCodSunat(), transaccionLinea.getCodProdGS1(), transaccionPropiedadesList));

                /*
                 * Agregar VALOR UNITARIO
                 * <cac:InvoiceLine><cac:Price>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] Agregando VALOR UNITARIO - TAG Price.");
                }
                boletaLine.setPrice(getPriceForLine(transaccionLinea.getTransaccionLineasBillrefList(), currencyCode));
                boletaLineList.add(boletaLine);
            } //for
            for (int i = 0; i < IUBLConfig.lstImporteIGV.size(); i++) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllBoletaLines() [" + this.identifier + "] ImporteIGV: " + IUBLConfig.lstImporteIGV.get(i));
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
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllBoletaLines() [" + this.identifier + "]");
        }
        return boletaLineList;
    } //getAllBoletaLines

    private List<CreditNoteLineType> getAllCreditNoteLines(Transaccion transaccion, List<TransaccionLineas> transaccionLineasList, List<TransaccionPropiedades> transaccionPropiedadesList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllCreditNoteLines() [" + this.identifier + "] transaccionLineasList: " + transaccionLineasList + " currencyCode: " + currencyCode);
        }
        if (null == transaccionLineasList || transaccionLineasList.isEmpty()) {
            logger.error("getAllCreditNoteLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<CreditNoteLineType> creditNoteLineList = new ArrayList<CreditNoteLineType>(transaccionLineasList.size());
        transaccionLineasList.sort(Comparator.comparing(linea -> linea.getTransaccionLineasPK().getNroOrden()));
        try {
            for (TransaccionLineas transaccionLinea : transaccionLineasList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Extrayendo informacion del item...");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "]\n" + "NroOrden: " + transaccionLinea.getTransaccionLineasPK().getNroOrden() + "\n" + "Cantidad: " + transaccionLinea.getCantidad() + "\tUnidad: " + transaccionLinea.getUnidad() + "\tUnidadSunat: " + transaccionLinea.getUnidadSunat() + "\tTotalLineaSinIGV: " + transaccionLinea.getTotalLineaSinIGV() + "\n" + "PrecioRefCodigo: " + transaccionLinea.getPrecioRefCodigo() + "\tPrecioIGV: " + transaccionLinea.getPrecioIGV() + "\tPrecioRefMonto: " + transaccionLinea.getPrecioRefMonto() + "\n" + "DCTOMonto: " + transaccionLinea.getDSCTOMonto() + "\tDescripcion: " + transaccionLinea.getDescripcion() + "\tCodArticulo: " + transaccionLinea.getCodArticulo());
                }
                CreditNoteLineType creditNoteLine = new CreditNoteLineType();

                /* Agregar <cac:CreditNoteLine><cbc:ID> */
                creditNoteLine.setID(getID(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));

                /*
                 * Agregar UNIDAD DE MEDIDA segun SUNAT
                 * <cac:CreditNoteLine><cbc:CreditedQuantity>
                 */
                creditNoteLine.setCreditedQuantity(getCreditedQuantity(transaccionLinea.getCantidad(), transaccionLinea.getUnidadSunat()));

                /*
                 * Agregar UNIDAD DE MEDIDA segun VENTURA
                 * <cac:CreditNoteLine><cbc:Note>
                 */
                if (StringUtils.isNotBlank(transaccionLinea.getUnidad())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando UNIDAD DE MEDIDA (VENTURA).");
                    }
                    creditNoteLine.getNote().add(getNote(transaccionLinea.getUnidad()));
                }

                /* Agregar <cac:CreditNoteLine><cbc:LineExtensionAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando TOTAL_LINEA_SIN_IGV(" + transaccionLinea.getTotalLineaSinIGV() + ") - TAG LineExtensionAmount.");
                }
                creditNoteLine.setLineExtensionAmount(getLineExtensionAmount(transaccionLinea.getTotalLineaSinIGV(), currencyCode));

                /*
                 * Op. Onerosa:     tiene precio unitario
                 * Op. No Onerosa:  tiene valor referencial
                 *
                 * Agregar PRECIO UNITARIO / VALOR REFERENCIAL
                 * <cac:CreditNoteLine><cac:PricingReference>
                 */
                if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                    creditNoteLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                } else if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                    creditNoteLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                }

                /*
                 * Agregar IMPUESTOS DE LINEA
                 * <cac:CreditNoteLine><cac:TaxTotal>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando IMPUESTOS DE LINEA.");
                }
                creditNoteLine.getTaxTotal().add(getTaxTotalLineV21(transaccionLinea.getTransaccionLineaImpuestosList(), transaccionLinea.getLineaImpuesto(), currencyCode));

                /* Agregar <cac:CreditNoteLine><cac:Item> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transaccionLinea.getDescripcion() + "] COD_ARTICULO[" + transaccionLinea.getCodArticulo() + "] COD_SUNAT[" + transaccionLinea.getCodSunat() + "] COD_PROD_GS1[" + transaccionLinea.getCodProdGS1() + "] - TAG Item.");
                }
                creditNoteLine.setItem(getItemForLine(transaccion, transaccionLinea, transaccionLinea.getDescripcion(), transaccionLinea.getCodArticulo(), transaccionLinea.getCodSunat(), transaccionLinea.getCodProdGS1(), transaccionPropiedadesList));

                /*
                 * Agregar VALOR UNITARIO
                 * <cac:CreditNoteLine><cac:Price>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllCreditNoteLines() [" + this.identifier + "] Agregando VALOR UNITARIO - TAG Price.");
                }
                creditNoteLine.setPrice(getPriceForLine(transaccionLinea.getTransaccionLineasBillrefList(), currencyCode));
                creditNoteLineList.add(creditNoteLine);
            } //for
        } catch (UBLDocumentException e) {
            logger.error("getAllCreditNoteLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllCreditNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllCreditNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllCreditNoteLines() [" + this.identifier + "]");
        }
        return creditNoteLineList;
    } //getAllCreditNoteLines

    private List<DebitNoteLineType> getAllDebitNoteLines(Transaccion transaccion, List<TransaccionLineas> transaccionLineasList, List<TransaccionPropiedades> transaccionPropiedadesList, String currencyCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllDebitNoteLines() [" + this.identifier + "] transaccionLineasList: " + transaccionLineasList + " currencyCode: " + currencyCode);
        }
        if (null == transaccionLineasList || transaccionLineasList.isEmpty()) {
            logger.error("getAllDebitNoteLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<DebitNoteLineType> debitNoteLineList = new ArrayList<>();
        transaccionLineasList.sort(Comparator.comparing(linea -> linea.getTransaccionLineasPK().getNroOrden()));
        try {
            for (TransaccionLineas transaccionLinea : transaccionLineasList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Extrayendo informacion del item...");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "]\n" + "NroOrden: " + transaccionLinea.getTransaccionLineasPK().getNroOrden() + "\n" + "Cantidad: " + transaccionLinea.getCantidad() + "\tUnidad: " + transaccionLinea.getUnidad() + "\tUnidadSunat: " + transaccionLinea.getUnidadSunat() + "\tTotalLineaSinIGV: " + transaccionLinea.getTotalLineaSinIGV() + "\n" + "PrecioRefCodigo: " + transaccionLinea.getPrecioRefCodigo() + "\tPrecioIGV: " + transaccionLinea.getPrecioIGV() + "\tPrecioRefMonto: " + transaccionLinea.getPrecioRefMonto() + "\n" + "DCTOMonto: " + transaccionLinea.getDSCTOMonto() + "\tDescripcion: " + transaccionLinea.getDescripcion() + "\tCodArticulo: " + transaccionLinea.getCodArticulo());
                }
                DebitNoteLineType debitNoteLine = new DebitNoteLineType();

                /* Agregar <cac:DebitNoteLine><cbc:ID> */
                debitNoteLine.setID(getID(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));

                /*
                 * Agregar UNIDAD DE MEDIDA segun SUNAT
                 * <cac:DebitNoteLine><cbc:DebitedQuantity>
                 */
                debitNoteLine.setDebitedQuantity(getDebitedQuantity(transaccionLinea.getCantidad(), transaccionLinea.getUnidadSunat()));

                /*
                 * Agregar UNIDAD DE MEDIDA segun VENTURA
                 * <cac:DebitNoteLine><cbc:Note>
                 */
                if (StringUtils.isNotBlank(transaccionLinea.getUnidad())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando UNIDAD DE MEDIDA (VENTURA).");
                    }
                    debitNoteLine.getNote().add(getNote(transaccionLinea.getUnidad()));
                }

                /* Agregar <cac:DebitNoteLine><cbc:LineExtensionAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando TOTAL_LINEA_SIN_IGV(" + transaccionLinea.getTotalLineaSinIGV() + ") - TAG LineExtensionAmount.");
                }
                debitNoteLine.setLineExtensionAmount(getLineExtensionAmount(transaccionLinea.getTotalLineaSinIGV(), currencyCode));

                /*
                 * Op. Onerosa:     tiene precio unitario
                 * Op. No Onerosa:  tiene valor referencial
                 *
                 * Agregar PRECIO UNITARIO / VALOR REFERENCIAL
                 * <cac:DebitNoteLine><cac:PricingReference>
                 */
                if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_UNIT_PRICE)) {
                    debitNoteLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioIGV().setScale(IUBLConfig.DECIMAL_LINE_UNIT_PRICE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                } else if (transaccionLinea.getPrecioRefCodigo().equalsIgnoreCase(IUBLConfig.ALTERNATIVE_CONDICION_REFERENCE_VALUE)) {
                    debitNoteLine.setPricingReference(getPricingReference(transaccionLinea.getPrecioRefCodigo(), transaccionLinea.getPrecioRefMonto().setScale(IUBLConfig.DECIMAL_LINE_REFERENCE_VALUE, RoundingMode.HALF_UP), currencyCode, transaccionLinea));
                }

                /*
                 * Agregar IMPUESTOS DE LINEA
                 * <cac:DebitNoteLine><cac:TaxTotal>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando IMPUESTOS DE LINEA.");
                }
                debitNoteLine.getTaxTotal().add(getTaxTotalLineV21(transaccionLinea.getTransaccionLineaImpuestosList(), transaccionLinea.getLineaImpuesto(), currencyCode));

                /* Agregar <cac:DebitNoteLine><cac:Item> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transaccionLinea.getDescripcion() + "] COD_ARTICULO[" + transaccionLinea.getCodArticulo() + "] COD_SUNAT[" + transaccionLinea.getCodSunat() + "] COD_PROD_GS1[" + transaccionLinea.getCodProdGS1() + "] - TAG Item.");
                }
                debitNoteLine.setItem(getItemForLine(transaccion, transaccionLinea, transaccionLinea.getDescripcion(), transaccionLinea.getCodArticulo(), transaccionLinea.getCodSunat(), transaccionLinea.getCodProdGS1(), transaccionPropiedadesList));

                /*
                 * Agregar VALOR UNITARIO
                 * <cac:DebitNoteLine><cac:Price>
                 */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDebitNoteLines() [" + this.identifier + "] Agregando VALOR UNITARIO - TAG Price.");
                }
                debitNoteLine.setPrice(getPriceForLine(transaccionLinea.getTransaccionLineasBillrefList(), currencyCode));
                debitNoteLineList.add(debitNoteLine);
            } //for
        } catch (UBLDocumentException e) {
            logger.error("getAllDebitNoteLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllDebitNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllDebitNoteLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllDebitNoteLines() [" + this.identifier + "]");
        }
        return debitNoteLineList;
    } //getAllDebitNoteLines

    private List<SUNATPerceptionDocumentReferenceType> getAllSUNATPerceptionDocumentReferences(List<TransaccionComprobantePago> transaccionComprobantePagoList) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] transaccionComprobantePagoList: " + transaccionComprobantePagoList);
        }
        if (null == transaccionComprobantePagoList || transaccionComprobantePagoList.isEmpty()) {
            logger.error("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<SUNATPerceptionDocumentReferenceType> sunatPerceptionDocReferenceList = new ArrayList<SUNATPerceptionDocumentReferenceType>(transaccionComprobantePagoList.size());
        try {
            for (TransaccionComprobantePago transaccionComprobantePago : transaccionComprobantePagoList) {
                SUNATPerceptionDocumentReferenceType sunatPerceptionDocReference = new SUNATPerceptionDocumentReferenceType();

                /* Agregar <sac:SUNATPerceptionDocumentReference><cbc:ID> */
                sunatPerceptionDocReference.setId(getID(transaccionComprobantePago.getDOC_Numero(), transaccionComprobantePago.getDOC_Tipo()));

                /* Agregar <sac:SUNATPerceptionDocumentReference><cbc:IssueDate> */
                sunatPerceptionDocReference.setIssueDate(getIssueDate(transaccionComprobantePago.getDOC_FechaEmision()));

                /* Agregar <sac:SUNATPerceptionDocumentReference><cbc:TotalInvoiceAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando DOC_IMPORTE(" + transaccionComprobantePago.getDOC_Importe() + ") DOC_MONEDA(" + transaccionComprobantePago.getDOCMoneda() + ") - TAG TotalInvoiceAmount.");
                }
                sunatPerceptionDocReference.setTotalInvoiceAmount(getTotalInvoiceAmount(transaccionComprobantePago.getDOC_Importe(), transaccionComprobantePago.getDOCMoneda()));

                /* Agregar <sac:SUNATPerceptionDocumentReference><cac:Payment> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando PAGO_NUMERO(" + transaccionComprobantePago.getPagoNumero() + ") PAGO_IMPORTE_SR(" + transaccionComprobantePago.getImporte_Pago_Soles() + ") PAGO_MONEDA(" + transaccionComprobantePago.getPagoMoneda() + ") PAGO_FECHA(" + transaccionComprobantePago.getPagoFecha() + ") - TAG Payment.");
                }
                sunatPerceptionDocReference.setPayment(getPaymentForLine(transaccionComprobantePago.getPagoNumero(), transaccionComprobantePago.getImporte_Pago_Soles(), transaccionComprobantePago.getPagoMoneda(), transaccionComprobantePago.getPagoFecha()));

                /* Agregar <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation> */
                SUNATPerceptionInformationType sunatPerceptionInformation = new SUNATPerceptionInformationType();
                {
                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><sac:SUNATPerceptionAmount> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando CP_IMPORTE(" + transaccionComprobantePago.getCP_Importe() + ") CP_MONEDA(" + transaccionComprobantePago.getCPMoneda() + ") - TAG SUNATPerceptionInformation_SUNATPerceptionAmount.");
                    }
                    SUNATPerceptionAmountType sunatPerceptionAmount = new SUNATPerceptionAmountType();
                    sunatPerceptionAmount.setValue(transaccionComprobantePago.getCP_Importe().setScale(2, RoundingMode.HALF_UP));
                    sunatPerceptionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMoneda()).value());
                    sunatPerceptionInformation.setPerceptionAmount(sunatPerceptionAmount);

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><sac:SUNATPerceptionDate> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando CP_FECHA(" + transaccionComprobantePago.getCPFecha() + ") - TAG SUNATPerceptionInformation_SUNATPerceptionDate.");
                    }
                    sunatPerceptionInformation.setPerceptionDate(getSUNATPerceptionDate(transaccionComprobantePago.getCPFecha()));

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><sac:SUNATNetTotalCashed> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando CP_IMPORTE_TOTAL(" + transaccionComprobantePago.getCP_ImporteTotal() + ") CP_MONEDA_MONTO_NETO(" + transaccionComprobantePago.getCPMonedaMontoNeto() + ") - TAG SUNATPerceptionInformation_SUNATNetTotalCashed.");
                    }
                    SUNATNetTotalCashedType sunatNetTotalCashed = new SUNATNetTotalCashedType();
                    sunatNetTotalCashed.setValue(transaccionComprobantePago.getCP_ImporteTotal().setScale(2, RoundingMode.HALF_UP));
                    sunatNetTotalCashed.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMonedaMontoNeto()).value());
                    sunatPerceptionInformation.setSunatNetTotalCashed(sunatNetTotalCashed);

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><cac:ExchangeRate> */
                    ExchangeRateType exchangeRate = new ExchangeRateType();

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><cac:ExchangeRate><cbc:SourceCurrencyCode> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando TC_MONEDA_REF(" + transaccionComprobantePago.getTCMonedaRef() + ") - TAG SUNATPerceptionInformation_ExchangeRate_SourceCurrencyCode.");
                    }
                    SourceCurrencyCodeType sourceCurrencyCode = new SourceCurrencyCodeType();
                    sourceCurrencyCode.setValue(transaccionComprobantePago.getTCMonedaRef());
                    exchangeRate.setSourceCurrencyCode(sourceCurrencyCode);

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><cac:ExchangeRate><cbc:TargetCurrencyCode> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando TC_MONEDA_OBJ(" + transaccionComprobantePago.getTCMonedaObj() + ") - TAG SUNATPerceptionInformation_ExchangeRate_TargetCurrencyCode.");
                    }
                    TargetCurrencyCodeType targetCurrencyCode = new TargetCurrencyCodeType();
                    targetCurrencyCode.setValue(transaccionComprobantePago.getTCMonedaObj());
                    exchangeRate.setTargetCurrencyCode(targetCurrencyCode);

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><cac:ExchangeRate><cbc:CalculationRate> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando TC_FACTOR(" + transaccionComprobantePago.getTC_Factor() + ") - TAG SUNATPerceptionInformation_ExchangeRate_CalculationRate.");
                    }
                    CalculationRateType calculationRate = new CalculationRateType();
                    calculationRate.setValue(transaccionComprobantePago.getTC_Factor());
                    exchangeRate.setCalculationRate(calculationRate);

                    /* <sac:SUNATPerceptionDocumentReference><sac:SUNATPerceptionInformation><cac:ExchangeRate><cbc:Date> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Agregando TC_FECHA(" + transaccionComprobantePago.getTCFecha() + ") - TAG SUNATPerceptionInformation_ExchangeRate_Date.");
                    }
                    exchangeRate.setDate(getDate(transaccionComprobantePago.getTCFecha()));
                    sunatPerceptionInformation.setExchangeRate(exchangeRate);
                }
                sunatPerceptionDocReference.setSunatPerceptionInformation(sunatPerceptionInformation);
                sunatPerceptionDocReferenceList.add(sunatPerceptionDocReference);
            } //for
        } catch (UBLDocumentException e) {
            logger.error("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllSUNATPerceptionDocumentReferences() [" + this.identifier + "]");
        }
        return sunatPerceptionDocReferenceList;
    } //getAllSUNATPerceptionDocumentReferences

    public List<SUNATRetentionDocumentReferenceType> getAllSUNATRetentionDocumentReferences(List<TransaccionComprobantePago> transaccionComprobantePagoList) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] transaccionComprobantePagoList: " + transaccionComprobantePagoList);
        }
        if (null == transaccionComprobantePagoList || transaccionComprobantePagoList.isEmpty()) {
            logger.error("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<SUNATRetentionDocumentReferenceType> sunatRetentionDocReferenceList = new ArrayList<SUNATRetentionDocumentReferenceType>(transaccionComprobantePagoList.size());
        try {
            for (TransaccionComprobantePago transaccionComprobantePago : transaccionComprobantePagoList) {
                SUNATRetentionDocumentReferenceType sunatRetentionDocReference = new SUNATRetentionDocumentReferenceType();

                /* Agregar <sac:SUNATRetentionDocumentReference><cbc:ID> */
                sunatRetentionDocReference.setId(getID(transaccionComprobantePago.getDOC_Numero(), transaccionComprobantePago.getDOC_Tipo()));

                /* Agregar <sac:SUNATRetentionDocumentReference><cbc:IssueDate> */
                sunatRetentionDocReference.setIssueDate(getIssueDate(transaccionComprobantePago.getDOC_FechaEmision()));

                /* Agregar <sac:SUNATRetentionDocumentReference><cbc:TotalInvoiceAmount> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando DOC_IMPORTE(" + transaccionComprobantePago.getDOC_Importe() + ") DOC_MONEDA(" + transaccionComprobantePago.getDOCMoneda() + ") - TAG TotalInvoiceAmount.");
                }
                sunatRetentionDocReference.setTotalInvoiceAmount(getTotalInvoiceAmount(transaccionComprobantePago.getDOC_Importe(), transaccionComprobantePago.getDOCMoneda()));

                /* Agregar <sac:SUNATRetentionDocumentReference><cac:Payment> */
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando PAGO_NUMERO(" + transaccionComprobantePago.getPagoNumero() + ") PAGO_IMPORTE_SR(" + transaccionComprobantePago.getImporte_Pago_Soles() + ") PAGO_MONEDA(" + transaccionComprobantePago.getPagoMoneda() + ") PAGO_FECHA(" + transaccionComprobantePago.getPagoFecha() + ") - TAG Payment.");
                }
                sunatRetentionDocReference.setPayment(getPaymentForLine(transaccionComprobantePago.getPagoNumero(), transaccionComprobantePago.getImporte_Pago_Soles(), transaccionComprobantePago.getPagoMoneda(), transaccionComprobantePago.getPagoFecha()));

                /* Agregar <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation> */
                SUNATRetentionInformationType sunatRetentionInformation = new SUNATRetentionInformationType();
                {
                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><sac:SUNATRetentionAmount> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando CP_IMPORTE(" + transaccionComprobantePago.getCP_Importe() + ") CP_MONEDA(" + transaccionComprobantePago.getCPMoneda() + ") - TAG SUNATRetentionInformation_SUNATRetentionAmount.");
                    }
                    SUNATRetentionAmountType sunatRetentionAmount = new SUNATRetentionAmountType();
                    sunatRetentionAmount.setValue(transaccionComprobantePago.getCP_Importe().setScale(2, RoundingMode.HALF_UP));
                    sunatRetentionAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMoneda()).value());
                    sunatRetentionInformation.setSunatRetentionAmount(sunatRetentionAmount);

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><sac:SUNATRetentionDate> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando CP_FECHA(" + transaccionComprobantePago.getCPFecha() + ") - TAG SUNATRetentionInformation_SUNATRetentionDate.");
                    }
                    sunatRetentionInformation.setSunatRetentionDate(getSUNATRetentionDate(transaccionComprobantePago.getCPFecha()));

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><sac:SUNATNetTotalPaid> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando CP_IMPORTE_TOTAL(" + transaccionComprobantePago.getCP_ImporteTotal() + ") CP_MONEDA_MONTO_NETO(" + transaccionComprobantePago.getCPMonedaMontoNeto() + ") - TAG SUNATRetentionInformation_SUNATNetTotalPaid.");
                    }
                    SUNATNetTotalPaidType sunatNetTotalPaid = new SUNATNetTotalPaidType();
                    sunatNetTotalPaid.setValue(transaccionComprobantePago.getCP_ImporteTotal().setScale(2, RoundingMode.HALF_UP));
                    sunatNetTotalPaid.setCurrencyID(CurrencyCodeContentType.valueOf(transaccionComprobantePago.getCPMonedaMontoNeto()).value());
                    sunatRetentionInformation.setSunatNetTotalPaid(sunatNetTotalPaid);

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><cac:ExchangeRate> */
                    ExchangeRateType exchangeRate = new ExchangeRateType();

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><cac:ExchangeRate><cbc:SourceCurrencyCode> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando TC_MONEDA_REF(" + transaccionComprobantePago.getTCMonedaRef() + ") - TAG SUNATRetentionInformation_ExchangeRate_SourceCurrencyCode.");
                    }
                    SourceCurrencyCodeType sourceCurrencyCode = new SourceCurrencyCodeType();
                    sourceCurrencyCode.setValue(transaccionComprobantePago.getTCMonedaRef());
                    exchangeRate.setSourceCurrencyCode(sourceCurrencyCode);

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><cac:ExchangeRate><cbc:TargetCurrencyCode> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando TC_MONEDA_OBJ(" + transaccionComprobantePago.getTCMonedaObj() + ") - TAG SUNATRetentionInformation_ExchangeRate_TargetCurrencyCode.");
                    }
                    TargetCurrencyCodeType targetCurrencyCode = new TargetCurrencyCodeType();
                    targetCurrencyCode.setValue(transaccionComprobantePago.getTCMonedaObj());
                    exchangeRate.setTargetCurrencyCode(targetCurrencyCode);

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><cac:ExchangeRate><cbc:CalculationRate> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando TC_FACTOR(" + transaccionComprobantePago.getTC_Factor() + ") - TAG SUNATRetentionInformation_ExchangeRate_CalculationRate.");
                    }
                    CalculationRateType calculationRate = new CalculationRateType();
                    calculationRate.setValue(transaccionComprobantePago.getTC_Factor().setScale(3));
                    exchangeRate.setCalculationRate(calculationRate);

                    /* <sac:SUNATRetentionDocumentReference><sac:SUNATRetentionInformation><cac:ExchangeRate><cbc:Date> */
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Agregando TC_FECHA(" + transaccionComprobantePago.getTCFecha() + ") - TAG SUNATRetentionInformation_ExchangeRate_Date.");
                    }
                    exchangeRate.setDate(getDate(transaccionComprobantePago.getTCFecha()));
                    sunatRetentionInformation.setExchangeRate(exchangeRate);
                }
                sunatRetentionDocReference.setSunatRetentionInformation(sunatRetentionInformation);
                sunatRetentionDocReferenceList.add(sunatRetentionDocReference);
            } //for
        } catch (UBLDocumentException e) {
            logger.error("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllSUNATRetentionDocumentReferences() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllSUNATRetentionDocumentReferences() [" + this.identifier + "]");
        }
        return sunatRetentionDocReferenceList;
    } //getAllSUNATRetentionDocumentReferences

    private List<DespatchLineType> getAllDespatchLines(List<TransaccionLineas> transaccionLineasList) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllDespatchLines() [" + this.identifier + "] transaccionLineasList: " + transaccionLineasList);
        }
        if (null == transaccionLineasList || transaccionLineasList.isEmpty()) {
            logger.error("getAllDespatchLines() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_319.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_319);
        }
        List<DespatchLineType> despatchLineList = new ArrayList<DespatchLineType>(transaccionLineasList.size());

        transaccionLineasList.sort(Comparator.comparing(linea -> linea.getTransaccionLineasPK().getNroOrden()));
        try {
            for (TransaccionLineas transaccionLinea : transaccionLineasList) {
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllDespatchLines() [" + this.identifier + "] Extrayendo informacion del item...");
                }
                DespatchLineType despatchLine = new DespatchLineType();
                /* <cac:DespatchLine><cbc:ID> */
                despatchLine.setID(getID(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));
                /* <cac:DespatchLine><cbc:DeliveredQuantity> */
                despatchLine.setDeliveredQuantity(getDeliveredQuantity(transaccionLinea.getCantidad(), transaccionLinea.getUnidadSunat()));
                /* <cac:DespatchLine><cac:OrderLineReference> */
                despatchLine.getOrderLineReference().add(getOrderLineReference(String.valueOf(transaccionLinea.getTransaccionLineasPK().getNroOrden())));
                /* <cac:DespatchLine><cac:Item> */
                despatchLine.setItem(getItemForLineRest(transaccionLinea));
                despatchLineList.add(despatchLine);
                if (logger.isDebugEnabled()) {
                    logger.debug("getAllInvoiceLines() [" + this.identifier + "] Agregando DESCRIPCION[" + transaccionLinea.getDescripcion() + "] COD_ARTICULO[" + transaccionLinea.getCodArticulo() + "] - TAG Item.");
                }

            } //for
        } catch (Exception e) {
            logger.error("getAllDespatchLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllDespatchLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-getAllDespatchLines() [" + this.identifier + "]");
        }
        return despatchLineList;
    } //getAllDespatchLines

    private PaymentTermsType createPaymentTerms(Transaccion transaccion) {
        PaymentTermsType paymentTerms = new PaymentTermsType();
        IDType idType = new IDType();
        idType.setValue("Percepcion");
        paymentTerms.setID(idType);
        AmountType amountType = new AmountType();
        BigDecimal montoPercepcion = transaccion.getDOC_MonPercepcion();
        //BigDecimal importeTotal = transaccion.getDOCImporteTotal();
        BigDecimal importeTotal = transaccion.getDOC_SinPercepcion();
        BigDecimal totalPercepcion = montoPercepcion.add(importeTotal);
        amountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccion.getDOC_MON_Codigo()).value());
        amountType.setValue(totalPercepcion.setScale(2, RoundingMode.HALF_UP));
        paymentTerms.setAmount(amountType);
        return paymentTerms;
    }

    private InvoiceTypeCodeType createInvoiceTypeCode(Transaccion transaccion) {
        InvoiceTypeCodeType invoiceTypeCode = new InvoiceTypeCodeType();
        invoiceTypeCode.setName("Tipo de Operacion");
        invoiceTypeCode.setListURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01");
        invoiceTypeCode.setListSchemeURI("urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo51");
        invoiceTypeCode.setListName("Tipo de Documento");
        invoiceTypeCode.setListID("2001");
        invoiceTypeCode.setListAgencyName("PE:SUNAT");
        invoiceTypeCode.setValue("01");
        return invoiceTypeCode;
    }

    private AllowanceChargeType createAllowanceChargeType(Transaccion transaccion) {
        AllowanceChargeType allowanceCharge = new AllowanceChargeType();
        ChargeIndicatorType chargeIndicator = new ChargeIndicatorType();
        chargeIndicator.setValue(true);
        allowanceCharge.setChargeIndicator(chargeIndicator);
        AllowanceChargeReasonCodeType chargeReasonCode = new AllowanceChargeReasonCodeType();
        chargeReasonCode.setListURI(IUBLConfig.URI_CATALOG_53);
        chargeReasonCode.setListName(IUBLConfig.CARGO_DESCUENTO_TEXT);
        chargeReasonCode.setListAgencyName(IUBLConfig.LIST_AGENCY_NAME_PE_SUNAT);
        chargeReasonCode.setValue("51");
        allowanceCharge.setAllowanceChargeReasonCode(chargeReasonCode);
        MultiplierFactorNumericType multiplierFactorNumeric = new MultiplierFactorNumericType();
        multiplierFactorNumeric.setValue(transaccion.getDOC_PorPercepcion().setScale(5, RoundingMode.HALF_UP));
        allowanceCharge.setMultiplierFactorNumeric(multiplierFactorNumeric);
        AmountType amountType = new AmountType();
        amountType.setCurrencyID(CurrencyCodeContentType.valueOf(transaccion.getDOC_MON_Codigo()).value());
        amountType.setValue(transaccion.getDOC_MonPercepcion().setScale(2, RoundingMode.HALF_UP));
        allowanceCharge.setAmount(amountType);
        BaseAmountType baseAmount = new BaseAmountType();
        baseAmount.setCurrencyID(CurrencyCodeContentType.valueOf(transaccion.getDOC_MON_Codigo()).value());
        //baseAmount.setValue(transaccion.getDOCImporteTotal().setScale(2, BigDecimal.ROUND_HALF_UP));
        baseAmount.setValue(transaccion.getDOC_SinPercepcion().setScale(2, RoundingMode.HALF_UP));
        allowanceCharge.setBaseAmount(baseAmount);
        return allowanceCharge;
    }

    private NoteType createNote(String languageLocaleId, String value, String id) {
        final NoteType note = new NoteType();
        Optional.ofNullable(id).ifPresent(identificador -> note.setLanguageID(identificador));
        note.setLanguageLocaleID(languageLocaleId);
        note.setValue(value);
        return note;
    }


    public VoidedDocumentsType generateVoidedDocumentType(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateVoidedDocumentType() [" + this.identifier + "]");
        }
        VoidedDocumentsType voidedDocumentType = null;
        try {
            /* Instanciar el objeto VoidedDocumentsType para la COMUNICACION DE BAJA */
            voidedDocumentType = new VoidedDocumentsType();
            /* Agregar <VoidedDocuments><ext:UBLExtensions> */
            voidedDocumentType.setUBLExtensions(getUBLExtensionsSigner());
            /* Agregar <VoidedDocuments><cbc:UBLVersionID> */
            voidedDocumentType.setUBLVersionID(getUBLVersionID_2_0());
            /* Agregar <VoidedDocuments><cbc:CustomizationID> */
            voidedDocumentType.setCustomizationID(getCustomizationID_1_0());
            /* Agregar <VoidedDocuments><cbc:ID> */
            if (logger.isInfoEnabled()) {
                logger.info("generateVoidedDocumentType() [" + this.identifier + "] Agregando ANTICIPOId: " + transaction.getANTICIPO_Id());
            }
            voidedDocumentType.setID(getID(transaction.getANTICIPO_Id()));
            /* Agregar <VoidedDocuments><cbc:ReferenceDate> */
//            LocalDateTime date = LocalDateTime.now() /*LocalDateTime.of(2019, Month.OCTOBER, 3, 1, 1)*/;
            Date fecha = new Date();
            voidedDocumentType.setReferenceDate(getReferenceDate(transaction.getDOC_FechaEmision()));
            /* Agregar <VoidedDocuments><cbc:IssueDate> */
            voidedDocumentType.setIssueDate(getIssueDate(fecha));
            /* Agregar <VoidedDocuments><cac:Signature> */
            voidedDocumentType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));
            /* Agregar <VoidedDocuments><cac:AccountingSupplierParty> */
            SupplierPartyType accountingSupplierParty = getAccountingSupplierPartyV20(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial());
            voidedDocumentType.setAccountingSupplierParty(accountingSupplierParty);
            /* Agregar item <VoidedDocuments><sac:VoidedDocumentsLine> */
            {
                /*
                 * Para este caso solo se agrega un solo ITEM, porque se esta
                 * dando de BAJA una transaccion.
                 */
                VoidedDocumentsLineType voidedDocumentLine = new VoidedDocumentsLineType();
                /* Agregar <VoidedDocuments><sac:VoidedDocumentsLine><cbc:LineID> */
                LineIDType lineID = new LineIDType();
                lineID.setValue("1");
                voidedDocumentLine.setLineID(lineID);
                /* Agregar <VoidedDocuments><sac:VoidedDocumentsLine><cbc:DocumentTypeCode> */
                DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
                documentTypeCode.setValue(transaction.getDOC_Codigo());
                voidedDocumentLine.setDocumentTypeCode(documentTypeCode);
                /* Agregar <VoidedDocuments><sac:VoidedDocumentsLine><sac:DocumentSerialID> */
                IdentifierType documentSerialID = new IdentifierType();
                documentSerialID.setValue(transaction.getDOC_Serie());
                voidedDocumentLine.setDocumentSerialID(documentSerialID);
                /* Agregar <VoidedDocuments><sac:VoidedDocumentsLine><sac:DocumentNumberID> */
                IdentifierType documentNumberID = new IdentifierType();
                documentNumberID.setValue(transaction.getDOC_Numero());
                voidedDocumentLine.setDocumentNumberID(documentNumberID);
                /* Agregar <VoidedDocuments><sac:VoidedDocumentsLine><sac:VoidReasonDescription> */
                TextType voidReasonDescription = new TextType();
                voidReasonDescription.setValue(transaction.getFE_Comentario());
                voidedDocumentLine.setVoidReasonDescription(voidReasonDescription);
                voidedDocumentType.getVoidedDocumentsLine().add(voidedDocumentLine);
            }
        } catch (UBLDocumentException e) {
            logger.error("generateVoidedDocumentType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateVoidedDocumentType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_345, e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateVoidedDocumentType() [" + this.identifier + "]");
        }
        return voidedDocumentType;
    } //generateVoidedDocumentType

    public SummaryDocumentsType generateSummaryDocumentsTypeV2(Transaccion transaction, String signerName) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateSummaryDocumentsType() [" + this.identifier + "]");
        }
        SummaryDocumentsType summaryDocumentsType = null;
        String idTransaccion = transaction.getANTICIPO_Id().replace("RA","RC");
        try {
            /* Instanciar objeto SummaryDocumentsType para el resumen diario */
            summaryDocumentsType = new SummaryDocumentsType();
            /* Agregar <VoidedDocuments><ext:UBLExtensions> */
            if (logger.isDebugEnabled()) {
                logger.debug("generateSummaryDocumentsType() [" + this.identifier + "] Agregando TAG para colocar la FIRMA.");
            }
            UBLExtensionsType ublExtensions = new UBLExtensionsType();

            ublExtensions.getUBLExtension().add(getUBLExtensionSigner());
            summaryDocumentsType.setUBLExtensions(ublExtensions);

            /* Agregar <SummaryDocuments><cbc:UBLVersionID> */
            summaryDocumentsType.setUBLVersionID(getUBLVersionID());


            /* Agregar <SummaryDocuments><cbc:CustomizationID> */
            summaryDocumentsType.setCustomizationID(getCustomizationID11());

            /* Agregar <SummaryDocuments><cbc:ID> */
            if (logger.isDebugEnabled()) {
                logger.debug("generateSummaryDocumentsType() [" + this.identifier + "] Agregando IdTransaccion: " + idTransaccion);
            }
            IDType idDocIdentifier = new IDType();
            idDocIdentifier.setValue(idTransaccion);
            summaryDocumentsType.setID(idDocIdentifier);

            /* Agregar <SummaryDocuments><cbc:ReferenceDate> */
            System.out.println("*****************************************************************************************************************************");
            ReferenceDateType referenceDate2 = getReferenceDate(transaction.getDOC_FechaEmision());
            summaryDocumentsType.setReferenceDate(referenceDate2);

            Date fecha = new Date();
            summaryDocumentsType.setIssueDate(getIssueDate(fecha));
            System.out.println("*****************************************************************************************************************************");

            /* Agregar <VoidedDocuments><cac:Signature> */
            summaryDocumentsType.getSignature().add(getSignature(transaction.getDocIdentidad_Nro(), transaction.getRazonSocial(), signerName));

            /* Agregar <SummaryDocuments><cac:AccountingSupplierParty> */
            //SupplierPartyType accountingSupplierParty = generateAccountingSupplierParty(transaction.getNumeroRuc(), transaction.getDocIdentidadTipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIRDireccion(), transaction.getDIRDepartamento(), transaction.getDIRProvincia(), transaction.getDIRDistrito(), transaction.getDIRUbigeo(), transaction.getDIRPais(), transaction.getPersonContacto(), transaction.getEMail());
            //summaryDocumentsType.setAccountingSupplierParty(accountingSupplierParty);
            SupplierPartyType accountingSupplierParty = generateAccountingSupplierParty(transaction.getDocIdentidad_Nro(), transaction.getDocIdentidad_Tipo(), transaction.getRazonSocial(), transaction.getNombreComercial(), transaction.getDIR_Direccion(), transaction.getDIR_Departamento(), transaction.getDIR_Provincia(), transaction.getDIR_Distrito(), transaction.getDIR_Ubigeo(), transaction.getDIR_Pais(), transaction.getPersonContacto(), transaction.getEMail());
            summaryDocumentsType.setAccountingSupplierParty(accountingSupplierParty);

            /* Agregar <SummaryDocuments><sac:SummaryDocumentsLine> */
            summaryDocumentsType.getSummaryDocumentsLine().addAll(getAllSummaryDocumentLinesV2(transaction));

        } catch (UBLDocumentException e) {
            logger.error("generateSummaryDocumentsType() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("generateSummaryDocumentsType() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + e.getMessage());
            throw new UBLDocumentException("Error generación baja de documento");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateSummaryDocumentsType() [" + this.identifier + "]");
        }
        return summaryDocumentsType;
    } // generateSummaryDocumentsType

    private UBLVersionIDType getUBLVersionID() {
        UBLVersionIDType ublVersionID = new UBLVersionIDType();
        ublVersionID.setValue(IUBLConfig.UBL_VERSION_ID_2_0);

        return ublVersionID;
    } // getUBLVersionID

    private CustomizationIDType getCustomizationID11() {
        CustomizationIDType customizationID = new CustomizationIDType();
        customizationID.setValue(IUBLConfig.CUSTOMIZATION_ID1);
        return customizationID;
    } // getCustomizationID

    public SupplierPartyType generateAccountingSupplierParty(String identifier,
                                                             String identifierType, String socialReason, String commercialName,
                                                             String fiscalAddress, String department, String province,
                                                             String district, String ubigeo, String countryCode,
                                                             String contactName, String electronicMail)
            throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+generateAccountingSupplierParty() ["
                    + this.identifier + "]");
        }
        SupplierPartyType accountingSupplierParty = null;

        try {

            if (logger.isDebugEnabled()) {
                logger.debug("+CustomerAssignedAccountIDType() ["
                        + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cbc:CustomerAssignedAccountID> */
            CustomerAssignedAccountIDType customerAssignedAccountID = new CustomerAssignedAccountIDType();
            customerAssignedAccountID.setValue(identifier);

            if (logger.isDebugEnabled()) {
                logger.debug("+AdditionalAccountIDType() ["
                        + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cbc:AdditionalAccountID> */
            AdditionalAccountIDType additionalAccountID = new AdditionalAccountIDType();
            additionalAccountID.setValue(identifierType);

            if (logger.isDebugEnabled()) {
                logger.debug("+PartyType() ["
                        + this.identifier + "]");
            }

            /* <cac:AccountingSupplierParty><cac:Party> */
            PartyType party = generateParty(socialReason, commercialName,
                    fiscalAddress, department, province, district, ubigeo,
                    countryCode, contactName, electronicMail);

            /*
             * Armar el objeto con sus respectivos TAG's
             */
            accountingSupplierParty = new SupplierPartyType();
            accountingSupplierParty
                    .setCustomerAssignedAccountID(customerAssignedAccountID);
            accountingSupplierParty.getAdditionalAccountID().add(
                    additionalAccountID);
            accountingSupplierParty.setParty(party);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("-generateAccountingSupplierParty() ["
                        + this.identifier + "] accountingSupplierParty: "
                        + e.getMessage());
            }

            throw new UBLDocumentException(IVenturaError.ERROR_302);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-generateAccountingSupplierParty() ["
                    + this.identifier + "] accountingSupplierParty: "
                    + accountingSupplierParty);
        }
        return accountingSupplierParty;
    } // generateAccountingSupplierParty

    private PartyType generateParty(String socialReasonValue,
                                    String commercialNameValue, String fiscalAddressValue,
                                    String departmentValue, String provinceValue, String districtValue,
                                    String ubigeoValue, String countryCodeValue,
                                    String contactNameValue, String electronicMailValue)
            throws Exception {
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

            List<PartyLegalEntityType> listaEntityTypes = new ArrayList<>();
            listaEntityTypes.add(partyLegalEntity);

            party.setPartyLegalEntity(listaEntityTypes);
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


    private List<SummaryDocumentsLineType> getAllSummaryDocumentLinesV2(Transaccion transaccion) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+getAllSummaryDocumentLines() [" + this.identifier + "] ");
        }
        List<SummaryDocumentsLineType> summaryDocumentLineList = null;

        try {
            summaryDocumentLineList = new ArrayList<>(1);
            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Extrayendo informacion del item...");
            }

            SummaryDocumentsLineType summaryDocumentLine = new SummaryDocumentsLineType();

            /* Agregar <sac:SummaryDocumentsLine><cbc:LineID> */
            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Agregando el LINEID.");
            }
            LineIDType lineID = new LineIDType();
            lineID.setValue(String.valueOf(1));
            summaryDocumentLine.setLineID(lineID);

            /*
             * Agregar la SERIE a la que hace referencia los documentos
             * de esta LINEA.
             * <sac:SummaryDocumentsLine><sac:DocumentSerialID>
             */
            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Agregando la SERIE a la LINEA.");
            }

            IDType iDType3 = new IDType();
            iDType3.setValue(transaccion.getDOC_Serie() + "-" + transaccion.getDOC_Numero());
            summaryDocumentLine.setiD(iDType3);

            /*
             * Agregar el codigo del tipo de documento de la LINEA
             * <sac:SummaryDocumentsLine><cbc:DocumentTypeCode>
             */
            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Agregando el CODIGO del tipo de documento.");
            }
            DocumentTypeCodeType documentTypeCode = new DocumentTypeCodeType();
            documentTypeCode.setValue(transaccion.getDOC_Codigo());
            summaryDocumentLine.setDocumentTypeCode(documentTypeCode);

            CustomerPartyType customerPartyType = new CustomerPartyType();
            CustomerAssignedAccountIDType customerAssignedAccountIDType = new CustomerAssignedAccountIDType();
            customerAssignedAccountIDType.setValue(transaccion.getDocIdentidad_Nro());
            AdditionalAccountIDType additionalAccountIDType = new AdditionalAccountIDType();
            additionalAccountIDType.setValue(transaccion.getDocIdentidad_Tipo());
            customerPartyType.setCustomerAssignedAccountID(customerAssignedAccountIDType);
            customerPartyType.getAdditionalAccountID().add(additionalAccountIDType);
            summaryDocumentLine.setCustomerParty(customerPartyType);

            StatusType statusType = new StatusType();
            ConditionCodeType conditionCodeType = new ConditionCodeType();
            conditionCodeType.setValue("3");
            statusType.setConditionCode(conditionCodeType);
            summaryDocumentLine.setStatus(statusType);

            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Agregando el MONTO TOTAL de la LINEA.");
            }

            AmountType totalInvoiceAmountType = new AmountType();
            totalInvoiceAmountType.setValue(transaccion.getDOC_MontoTotal());
            totalInvoiceAmountType.setCurrencyID(transaccion.getDOC_MON_Codigo());
            summaryDocumentLine.setTotalAmount(totalInvoiceAmountType);

            summaryDocumentLine.getBillingPayment().add(getBillingPayment(transaccion));

            if (logger.isDebugEnabled()) {
                logger.debug("getAllSummaryDocumentLines() [" + this.identifier + "] Agregando sumatoria de OTROS CARGOS.");
            }
            if (transaccion.getDOC_OtrosCargos().compareTo(BigDecimal.ZERO) > 0) {
                summaryDocumentLine.getAllowanceCharge().add(getAllowanceCharge2(transaccion.getDOC_OtrosCargos(), transaccion.getDOC_MON_Codigo(),  true));
            }

            summaryDocumentLine.getTaxTotal().add(getTaxTotalForSummary(transaccion.getDOC_ImpuestoTotal(), transaccion.getDOC_MON_Codigo(), IUBLConfig.TAX_TOTAL_IGV_ID, IUBLConfig.TAX_TOTAL_IGV_NAME, IUBLConfig.TAX_TOTAL_IGV_CODE));
            summaryDocumentLineList.add(summaryDocumentLine);
        } catch (UBLDocumentException e) {
            logger.error("getAllSummaryDocumentLines() [" + this.identifier + "] UBLDocumentException - ERROR: " + e.getError().getId() + "-" + e.getError().getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getAllSummaryDocumentLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") - ERROR: " + IVenturaError.ERROR_320.getMessage());
            logger.error("getAllSummaryDocumentLines() [" + this.identifier + "] Exception(" + e.getClass().getName() + ") -->" + ExceptionUtils.getStackTrace(e));
            throw new UBLDocumentException(IVenturaError.ERROR_320);
        }
        return summaryDocumentLineList;
    }

    private TaxTotalType getTaxTotalForSummary(BigDecimal taxAmountValue, String currencyCode, String schemeID, String schemeName, String schemeCode) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getTaxTotalForSummary() [" + this.identifier + "] taxAmountValue: " + taxAmountValue + " schemeID: " + schemeID);
        }
        TaxTotalType taxTotal = null;
        try {
            taxTotal = new TaxTotalType();

            /* Agregar <cac:TaxTotal><cbc:TaxAmount> */
            TaxAmountType taxAmount = new TaxAmountType();
            taxAmount.setValue(taxAmountValue.setScale(2, RoundingMode.HALF_UP));
            taxAmount.setCurrencyID(currencyCode);
            TaxSubtotalType taxSubTotal = new TaxSubtotalType();
            /* Agregar <cac:TaxTotal><cac:TaxSubtotal><cbc:TaxAmount> */
            taxSubTotal.setTaxAmount(taxAmount);
            /* Agregar <cac:TaxTotal><cac:TaxSubtotal><cac:TaxCategory> */
            TaxCategoryType taxCategory = new TaxCategoryType();
            taxCategory.setTaxScheme(getTaxScheme(schemeID, schemeName, schemeCode));
            taxSubTotal.setTaxCategory(taxCategory);
            /*
             * Agregar los TAG's
             */
            taxTotal.setTaxAmount(taxAmount);
            taxTotal.getTaxSubtotal().add(taxSubTotal);
        } catch (Exception e) {
            logger.error("getTaxTotalForSummary() [" + this.identifier + "] ERROR: " + IVenturaError.ERROR_350.getMessage());
            throw new UBLDocumentException(IVenturaError.ERROR_350);
        }
        return taxTotal;
    } // getTaxTotalForSummary

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

    private PaymentType getBillingPayment(Transaccion transaccion ) throws UBLDocumentException {
        /*if (logger.isDebugEnabled()) {
            logger.debug("+-getBillingPayment() [" + this.identifier + "] amount: " + amount + " operationType: " + operationType);
        }*/
        PaymentType paymentType = null;

        try {
            /*
             * Agregar
             * <sac:SummaryDocumentsLine><sac:BillingPayment><cbc:PaidAmount>
             */
            PaidAmountType paidAmount = new PaidAmountType();
            paidAmount.setValue(transaccion.getDOC_Importe().setScale(2, RoundingMode.HALF_UP));
            paidAmount.setCurrencyID(transaccion.getDOC_MON_Codigo());

            /*
             * Agregar
             * <sac:SummaryDocumentsLine><sac:BillingPayment><cbc:InstructionID>
             */
            InstructionIDType instructionID = new InstructionIDType();
            instructionID.setValue("01");

            /*
             * Agregar los TAG's
             */
            paymentType = new PaymentType();
            paymentType.setPaidAmount(paidAmount);
            paymentType.setInstructionID(instructionID);
        } catch (Exception e) {
            throw new UBLDocumentException(IVenturaError.ERROR_349);
        }
        return paymentType;
    } // getBillingPayment
    private PaymentType getBillingPayment(BigDecimal amount, String currencyCode, String operationType) throws UBLDocumentException {
        if (logger.isDebugEnabled()) {
            logger.debug("+-getBillingPayment() [" + this.identifier + "] amount: " + amount + " operationType: " + operationType);
        }
        PaymentType paymentType = null;

        try {
            /*
             * Agregar
             * <sac:SummaryDocumentsLine><sac:BillingPayment><cbc:PaidAmount>
             */
            PaidAmountType paidAmount = new PaidAmountType();
            paidAmount.setValue(amount.setScale(2, RoundingMode.HALF_UP));
            paidAmount.setCurrencyID(currencyCode);

            /*
             * Agregar
             * <sac:SummaryDocumentsLine><sac:BillingPayment><cbc:InstructionID>
             */
            InstructionIDType instructionID = new InstructionIDType();
            instructionID.setValue(operationType);

            /*
             * Agregar los TAG's
             */
            paymentType = new PaymentType();
            paymentType.setPaidAmount(paidAmount);
            paymentType.setInstructionID(instructionID);
        } catch (Exception e) {
            throw new UBLDocumentException(IVenturaError.ERROR_349);
        }
        return paymentType;
    } // getBillingPayment

    private AllowanceChargeType getAllowanceCharge2(BigDecimal amountVal, String currencyCode, boolean chargeIndicatorVal) throws UBLDocumentException {
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
            amount.setCurrencyID(currencyCode);
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

}
