package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.entity.TransaccionContractdocref;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.extras.pdf.IPDFCreatorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.PDFBasicGenerateHandler;
import service.cloud.request.clientRequest.handler.PDFGenerateHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.prueba.Client;
import service.cloud.request.clientRequest.prueba.model.ResponseDTO;
import service.cloud.request.clientRequest.prueba.model.ResponseDTOAuth;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.utils.CertificateUtils;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.TaxTotalType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonextensioncomponents_2.UBLExtensionsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;

import javax.activation.DataHandler;
import java.io.*;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Service
public class GuiaServiceImpl implements GuiaInterface {

    private final Logger logger = Logger.getLogger(GuiaServiceImpl.class);

    @Autowired
    ClientProperties clientProperties;

    private String docUUID;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    ProcessorCoreInterface processorCoreInterface;

    @Override
    public TransaccionRespuesta transactionRemissionGuideDocumentRest(Transaccion transaction, String doctype) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("+transactionRemissionGuideDocument() ["
                    + this.docUUID + "] DOC_Id: "
                    + transaction.getDOC_Id() + " DocIdentidad_Nro: "
                    + transaction.getDocIdentidad_Nro());
        }
        TransaccionRespuesta transactionResponse = null;

        /* Extrayendo la informacion del archivo de configuracion 'config.xml' */
        //Configuracion configuration = ApplicationConfiguration.getInstance().getConfiguration();

        /* Generando el nombre del firmante */
        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        /*
         * Validando informacion basica
         * - Serie y correlativo
         * - RUC del emisor
         * - Fecha de emision
         */
        boolean isContingencia = false;
        List<TransaccionContractdocref> contractdocrefs = transaction.getTransaccionContractdocrefList();
        for (TransaccionContractdocref contractdocref : contractdocrefs) {
            if ("cu31".equalsIgnoreCase(contractdocref.getUsuariocampos().getNombre())) {
                isContingencia = "Si".equalsIgnoreCase(contractdocref.getValor());
                break;
            }
        }
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(
                transaction.getDOC_Id(),
                transaction.getDocIdentidad_Nro(),
                transaction.getDOC_FechaEmision(),
                transaction.getSN_EMail(),
                transaction.getEMail(),
                isContingencia);

        //UTILIZANDO HASH MAP DE ENTIDADES
        //String idSociedad = transaction.getKeySociedad();
        //ListaSociedades sociedad = VariablesGlobales.MapSociedades.get(idSociedad);

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        byte[] certificado = CertificateUtils.getCertificateInBytes(applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator
                + client.getCertificadoName());

        String certiPassword = client.getCertificadoPassword();
        String ksProvider = client.getCertificadoProveedor();
        String ksType = client.getCertificadoTipoKeystore();

        //GUIAS
        //String tipoTransaccion = client.getTipoIntegracionGuias();
        String clientId = client.getClientId();
        String secretId = client.getClientSecret();
        String usuarioGuias = client.getPasswordSunatGuias();
        String passwordGuias = client.getPasswordSunatGuias();
        String scope = client.getScope();


        if (logger.isDebugEnabled()) {
            logger.debug("transactionRemissionGuideDocument() [" + this.docUUID + "] Certificado en bytes: " + certificado);
        }

        /*
         * Validando el Certificado Digital
         * Se valida:
         * 	- Certificado nulo o vacio
         * 	- La contrase�a del certificado pueda abrir el certificado.
         */
        ///CertificateUtils.checkDigitalCertificateV2(certificate, certPassword, ksProvider, ksType);
        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        /* Generando el objeto DespatchAdviceType para la GUIA DE REMISION */
        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        DespatchAdviceType despatchAdviceType = null;
        despatchAdviceType = ublHandler.generateDespatchAdviceType(transaction, signerName);

        /*
         * Validar la información necesaria para la RETENCIÓN
         */
        validationHandler.checkRemissionGuideDocument(despatchAdviceType);

        /*
         * Se genera el nombre del documento de tipo GUIA DE REMISION
         */
        String documentName = DocumentNameHandler.getInstance().getRemissionGuideName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
        if (logger.isDebugEnabled()) {
            logger.debug("transactionRemissionGuideDocument() [" + this.docUUID + "] El nombre del documento: " + documentName);
        }

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);

        /*
         * Setear la ruta del directorio
         */
        String attachmentPath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() +
                File.separator + "anexo" + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
        fileHandler.setBaseDirectory(attachmentPath);
        if (logger.isDebugEnabled()) {
            logger.debug("transactionCreditNoteDocument() [" + this.docUUID + "] Ruta para los archivos adjuntos: " + attachmentPath);
        }
        fileHandler.setBaseDirectory(attachmentPath);
        if (logger.isDebugEnabled()) {
            logger.debug("transactionRemissionGuideDocument() [" + this.docUUID + "] Ruta para los archivos adjuntos: " + attachmentPath);
        }

        /*
         * Guardando el documento UBL en DISCO
         */
        String documentPath = null;
        documentPath = fileHandler.storeDocumentInDisk(despatchAdviceType, documentName);

        if (logger.isInfoEnabled()) {
            logger.info("transactionRemissionGuideDocument() [" + this.docUUID + "] El documento [" + documentName + "] fue guardado en DISCO en: " + documentPath);
        }


        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);

        File signedDocument = null;
        signedDocument = signerHandler.signDocument(documentPath, docUUID);

        if (logger.isInfoEnabled()) {
            logger.info("transactionRemissionGuideDocument() [" + this.docUUID + "] El documento [" + documentName + "] fue firmado correctamente en: " + signedDocument.getAbsolutePath());
        }
        LoggerTrans.getCDThreadLogger().log(Level.INFO, "[" + this.docUUID + "] El documento [" + documentName + "] fue firmado correctamente en: " + signedDocument.getAbsolutePath());

        Object ublDocument = fileHandler.getSignedDocument(signedDocument, transaction.getDOC_Codigo());

        UBLDocumentWRP documentWRP = UBLDocumentWRP.getInstance();
        documentWRP.setTransaccion(transaction);
        documentWRP.setAdviceType((DespatchAdviceType) ublDocument);

        PDFBasicGenerateHandler db = new PDFBasicGenerateHandler(docUUID);

        /* Agregar código de Barra */
        Date docFechaVencimiento = transaction.getDOC_FechaVencimiento();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = simpleDateFormat.format(docFechaVencimiento);
        DespatchAdviceType guia = documentWRP.getAdviceType();

        DataHandler zipDocument = fileHandler.compressUBLDocument(signedDocument, documentName, transaction.getSN_DocIdentidad_Nro(), transaction.getDocIdentidad_Nro());

        if (logger.isInfoEnabled()) {
            logger.info("transactionRemissionGuideDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP.");
        }

        logger.error("ERRROR +++++++++ ==========" + zipDocument.toString());

        ConfigData configuracion = ConfigData
                .builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .mostrarSoap(client.getMostrarSoap())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .scope(client.getScope())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .userNameSunatSunat(client.getUserNameSunatGuias())
                .passwordSunatSunat(client.getPasswordSunatGuias())
                .rutaBaseDoc(applicationProperties.getRutaBaseDoc())
                .build();

        logger.info("Se esta apuntando al ambiente : " + configuracion.getAmbiente() + " - " + configuracion.getIntegracionWs());
        if(configuracion.getIntegracionWs().equals("OSE")) logger.info("Url Service: "+ applicationProperties.getUrlOse());
        else if(configuracion.getIntegracionWs().equals("SUNAT")) logger.info("Url Service: "+ applicationProperties.getUrlSunat());
        logger.info("Usuario Sol: " + configuracion.getUserNameSunatSunat());
        logger.info("Clave Sol: " + configuracion.getPasswordSunatSunat());
        logger.info("Client id: " + configuracion.getClientId());
        logger.info("Client Secret: " + configuracion.getClientSecret());
        logger.info("Scope: " + configuracion.getScope());

        String digestValue = db.generateDigestValue(documentWRP.getAdviceType().getUBLExtensions());
        String barcodeValue = db.generateGuiaBarcodeInfoV2(guia.getID().getValue(), IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, fecha, BigDecimal.ZERO, BigDecimal.ZERO,
                guia.getDespatchSupplierParty(), guia.getDeliveryCustomerParty(), guia.getUBLExtensions());

        if (null != zipDocument) {
            if (transaction.getFE_Estado().equalsIgnoreCase("N")) {

                ResponseDTO responseDTOJWT = getJwtSunat(configuracion);

                if (responseDTOJWT.getStatusCode() == 400 || responseDTOJWT.getStatusCode() == 401) {
                    return generateResponseRest(documentWRP, responseDTOJWT);
                }

                //DECLARE
                ResponseDTO responseDTO = declareSunat(documentName, documentPath.replace("xml", "zip"), responseDTOJWT.getAccess_token());

                //DECLARE
                if (responseDTO.getStatusCode() == 401) {
                    responseDTOJWT = getJwtSunat(configuracion);
                    responseDTO = declareSunat(documentName, documentPath.replace("xml", "zip"), responseDTOJWT.getAccess_token());
                }

                //sendQR(transaction.getDOCCodigo(), documentWRP);
                Thread.sleep(5000);

                //CONSULT
                responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
                if (responseDTO.getStatusCode() == 401) {
                    responseDTOJWT = getJwtSunat(configuracion);
                    responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
                }

                int contador = 0;
                while (responseDTO.getCodRespuesta().equals("98")) {
                    Thread.sleep(5000);
                    if (contador == 5) break;
                    responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
                    contador++;
                }

                //EXTRAER ZIP
                if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("0")) {
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);

                    byte[] pdf = processorCoreInterface.processCDRResponseContigencia(null, signedDocument, fileHandler, documentName,
                            transaction.getDOC_Codigo(), documentWRP, transaction, configuracion);
                    transactionResponse.setPdf(pdf);

                    //AGREGAR A SAP CAMPO
                    logger.info("CODIGO TIKCET GUIAS SUNAT - [0]" + responseDTO.getNumTicket());
                    transactionResponse.setTicketRest(responseDTO.getNumTicket());

                } else if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("98")) {
                    logger.info("CODIGO TIKCET GUIAS SUNAT - [98]" + responseDTO.getNumTicket());
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    transactionResponse.setTicketRest(responseDTO.getNumTicket());
                } else if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("99")) {
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    transactionResponse.setTicketRest(responseDTO.getNumTicket());
                }
                byte[] documentBytes = fileHandler.convertFileToBytes(signedDocument);
                transactionResponse.setXml(documentBytes);

            } else if (transaction.getFE_Estado().equalsIgnoreCase("C")) {

                if (transaction.getTransaccionGuiaRemision().getTicketRest() != null && !transaction.getTransaccionGuiaRemision().getTicketRest().isEmpty()) {

                    //CONSULT
                    ResponseDTO responseDTOJWT = getJwtSunat(configuracion);

                    ResponseDTO responseDTO = consult(transaction.getTransaccionGuiaRemision().getTicketRest(), responseDTOJWT.getAccess_token());
                    if (responseDTO.getStatusCode() == 401) {
                        responseDTOJWT = getJwtSunat(configuracion);
                        responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
                    }

                    if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 404) {
                        return generateResponseRest(documentWRP, responseDTO);
                    }

                    //sendQR(transaction.getDOCCodigo(), documentWRP);

                    //responseDTO.setCodRespuesta("0");
                    if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("0")) {
                        transactionResponse = generateResponseRest(documentWRP, responseDTO);
                        byte[] pdf = processorCoreInterface.processCDRResponseContigencia(null, signedDocument, fileHandler, documentName,
                                transaction.getDOC_Codigo(), documentWRP, transaction, configuracion);
                        transactionResponse.setPdf(pdf);

                        //AGREGAR A SAP CAMPO
                        transactionResponse.setTicketRest(responseDTO.getNumTicket());
                    } else if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("98")) {
                        transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    } else if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("99")) {
                        transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    }
                    byte[] documentBytes = fileHandler.convertFileToBytes(signedDocument);
                    transactionResponse.setXml(documentBytes);

                }
            }
        } else {
            logger.error("transactionRemissionGuideDocument() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_457.getMessage());
            throw new NullPointerException(IVenturaError.ERROR_457.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-transactionRemissionGuideDocument() [" + this.docUUID + "]");
        }

        transactionResponse.setDigestValue(digestValue);
        transactionResponse.setBarcodeValue(barcodeValue);
        transactionResponse.setIdentificador(documentName);
        return transactionResponse;
    }


    public ResponseDTO consult(String numTicket, String token) throws IOException {

        HttpResponse<String> response = Unirest.get("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/envios/" + numTicket)
                .header("Authorization", "Bearer " + token)
                .asString();
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
        });
        responseDTO.setStatusCode(response.getStatus());
        responseDTO.setNumTicket(numTicket);
        return responseDTO;

    }

    public ResponseDTO declareSunat(String documentName, String documentPath, String token) throws IOException {

        String fileTo64 = zipDocumentTo64(documentPath.replace("xml", "zip"));
        String fileTo256Sha = zipDocumentTo265Sha(documentPath.replace("xml", "zip"));
        String nombreArchivo = documentName + ".zip";

        HttpResponse<String> response = Unirest.post("https://api-cpe.sunat.gob.pe/v1/contribuyente/gem/comprobantes/" + documentName)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body("{\r\n    \"archivo\" : {\r\n        \"nomArchivo\" : \"" + nombreArchivo + "\",\r\n        \"arcGreZip\" : \"" + fileTo64 + "\",\r\n        \"hashZip\" : \"" + fileTo256Sha + "\"\r\n    }\r\n}")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
        });
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    public String zipDocumentTo265Sha(String documentPath) {
        String checksumSHA256 = "";
        try {
            checksumSHA256 = DigestUtils.sha256Hex(new FileInputStream(documentPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return checksumSHA256;
    }

    public String zipDocumentTo64(String documentPath) {
        File originalFile = new File(documentPath);
        String encodedBase64 = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(originalFile);
            byte[] bytes = new byte[(int) originalFile.length()];
            fileInputStreamReader.read(bytes);
            encodedBase64 = new String(Base64.encodeBase64(bytes));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedBase64;
    }

    public ResponseDTO getJwtSunat(ConfigData configuracion) throws IOException {


        HttpResponse<String> response = Unirest.post("https://api-seguridad.sunat.gob.pe/v1/clientessol/" + configuracion.getClientId() + "/oauth2/token/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "TS019e7fc2=014dc399cbcad00473c65166bab99ef2e22263ae15b2a9e259ff0e5d972578fa54549fe8acccb61b76d241060b054cc73beff45ea3")
                .field("grant_type", "password")
                .field("scope", configuracion.getScope())
                .field("client_id", configuracion.getClientId())
                .field("client_secret", configuracion.getClientSecret())
                .field("username", configuracion.getUserNameSunatSunat())
                .field("password", configuracion.getPasswordSunatSunat())
                .field("", "")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = new ResponseDTO();
        ResponseDTOAuth responseDTO400 = new ResponseDTOAuth();

        if (response.getStatus() == 400) {
            responseDTO400 = objectMapper.readValue(response.body(), new TypeReference<ResponseDTOAuth>() {
            });
            responseDTO.setResponseDTO400(responseDTO400);
        } else {
            responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {
            });
        }
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;

    }

    public TransaccionRespuesta generateResponseRest(UBLDocumentWRP documentWRP, ResponseDTO responseDTO) {

        TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
        sunatResponse.setId(documentWRP.getTransaccion().getDOC_Serie() + "-" + documentWRP.getTransaccion().getDOC_Numero());

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 401) {
            transactionResponse.setMensaje(responseDTO.getResponseDTO400().getError() + " - " + responseDTO.getResponseDTO400().getError_description());
        } else if (responseDTO.getStatusCode() == 404) {
            transactionResponse.setMensaje("El número de ticket para consultar no existe");
            sunatResponse.setAceptado(false);
            transactionResponse.setSunat(sunatResponse);
        } else {
            if (responseDTO.getCodRespuesta().equals("0")) {

                String rptBase64 = responseDTO.getArcCdr();
                byte[] bytesZip = Base64.decodeBase64(rptBase64);
                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - " + "Documento aprobado");
                transactionResponse.setZip(bytesZip);
                sunatResponse.setAceptado(true);
                transactionResponse.setSunat(sunatResponse);

            } else if (responseDTO.getCodRespuesta().equals("98")) {

                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - " + "Documento en proceso, volver a consultar.");
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);

            } else if (responseDTO.getCodRespuesta().equals("99")) {

                if (responseDTO.getIndCdrGenerado().equals("1")) { //con CDR
                    String rptBase64 = responseDTO.getArcCdr();
                    byte[] bytesZip = Base64.decodeBase64(rptBase64);
                    transactionResponse.setZip(bytesZip);
                }
                //transactionResponse.setCodigo(TransaccionRespuesta.RSP_EMITIDO_RECHAZADO_REST);
                transactionResponse.setMensaje(responseDTO.getError().getNumError() + " - " + responseDTO.getError().getDesError());
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);

            }
        }

        //transactionResponse.setCodigoWS(Integer.parseInt(responseDTO.getCodRespuesta()));
        return transactionResponse;
    }


    public String generateBarCodeInfoString(String RUC_emisor_electronico,
                                            String documentType, String serie,
                                            String correlativo, List<TaxTotalType> taxTotalList,
                                            String issueDate, String Importe_total_venta,
                                            String Tipo_documento_adquiriente, String Numero_documento_adquiriente,
                                            UBLExtensionsType ublExtensions) throws PDFReportException {
        String barcodeValue = "";
        try {

            String digestValue = getDigestValue(ublExtensions);
            String Sumatoria_IGV = getTaxTotalValueV21(taxTotalList).toString();
            barcodeValue = MessageFormat.format(IPDFCreatorConfig.BARCODE_PATTERN, RUC_emisor_electronico,
                    documentType, serie, correlativo, Sumatoria_IGV, Importe_total_venta, issueDate,
                    Tipo_documento_adquiriente, Numero_documento_adquiriente, digestValue);

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

    private String getDigestValue(UBLExtensionsType ublExtensions)
            throws PDFReportException, Exception {
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

    protected BigDecimal getTaxTotalValueV21(List<TaxTotalType> taxTotalList) {

        if (taxTotalList != null) {
            for (int i = 0; i < taxTotalList.size(); i++) {
                for (int j = 0; j < taxTotalList.get(i).getTaxSubtotal().size(); j++) {
                    if (taxTotalList.get(i).getTaxSubtotal().get(j).getTaxCategory().getTaxScheme().getID().getValue().equalsIgnoreCase("1000")) {
                        return taxTotalList.get(i).getTaxAmount().getValue();
                    }
                }

            }
        }

        return BigDecimal.ZERO;
    }

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
            java.util.logging.Logger.getLogger(PDFGenerateHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
    }
}
