package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.PDFBasicGenerateHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.model.model.ResponseDTO;
import service.cloud.request.clientRequest.model.model.ResponseDTOAuth;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;

import javax.activation.DataHandler;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

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

    @Autowired
    DocumentFormatInterface documentFormatInterface;

    @Override
    public TransaccionRespuesta transactionRemissionGuideDocumentRest(TransacctionDTO transaction, String doctype) throws Exception {

        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());

        TransaccionRespuesta transactionResponse = null;

        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        boolean isContingencia = false;
        /*List<TransaccionContractdocref> contractdocrefs = transaction.getTransaccionContractdocrefList();
        for (TransaccionContractdocref contractdocref : contractdocrefs) {
            if ("cu31".equalsIgnoreCase(contractdocref.getUsuariocampos().getNombre())) {
                isContingencia = "Si".equalsIgnoreCase(contractdocref.getValor());
                break;
            }
        }*/
        /*List<Map<String, String>> contractdocrefs = transaction.getTransactionContractDocRefListDTOS();
        for (Map<String, String> contractdocref : contractdocrefs) {
            if ("cu31".equalsIgnoreCase(contractdocref.get("nombre"))) {
                isContingencia = "Si".equalsIgnoreCase(contractdocref.get("valor"));
                break;
            }
        }*/


        Optional<Map<String, String>> optional = transaction.getTransactionContractDocRefListDTOS().parallelStream()
                .filter(docRef -> docRef.containsKey("cu31")) //
                .findAny();

        if (optional.isPresent()) {
            String value = optional.get().get("cu31"); //
            if (value != null) {
                isContingencia = "Si".equalsIgnoreCase(value);
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

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        byte[] certificado = CertificateUtils.getCertificateInBytes(applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator
                + client.getCertificadoName());

        String certiPassword = client.getCertificadoPassword();
        String ksProvider = client.getCertificadoProveedor();
        String ksType = client.getCertificadoTipoKeystore();

        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        DespatchAdviceType despatchAdviceType = null;
        despatchAdviceType = ublHandler.generateDespatchAdviceType(transaction, signerName);


        validationHandler.checkRemissionGuideDocument(despatchAdviceType);

        String documentName = DocumentNameHandler.getInstance().getRemissionGuideName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
        if (logger.isDebugEnabled()) {
            logger.debug("transactionRemissionGuideDocument() [" + this.docUUID + "] El nombre del documento: " + documentName);
        }

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);

        /**Ruta completa donde se dejara el documento*/
        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDoc());
        fileHandler.setBaseDirectory(attachmentPath);

        String documentPath = null;

        documentPath = fileHandler.storeDocumentInDisk(despatchAdviceType, documentName);

        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);

        File signedDocument = null;
        signedDocument = signerHandler.signDocument(documentPath, docUUID);

        Object ublDocument = fileHandler.getSignedDocument(signedDocument, transaction.getDOC_Codigo());

        UBLDocumentWRP documentWRP = new UBLDocumentWRP();
        documentWRP.setTransaccion(transaction);
        documentWRP.setAdviceType((DespatchAdviceType) ublDocument);

        PDFBasicGenerateHandler db = new PDFBasicGenerateHandler(docUUID);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = simpleDateFormat.format(transaction.getDOC_FechaVencimiento());
        DespatchAdviceType guia = documentWRP.getAdviceType();

        byte[] xmlDocument = DocumentConverterUtils.convertDocumentToBytes(despatchAdviceType);
        byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
        DataHandler zipDocument = UtilsFile.compressUBLDocument(signedXmlDocument, documentName + ".xml");

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
        if (configuracion.getIntegracionWs().equals("OSE"))
            logger.info("Url Service: " + applicationProperties.getUrlOse());
        else if (configuracion.getIntegracionWs().equals("SUNAT"))
            logger.info("Url Service: " + applicationProperties.getUrlSunatEmision());
        logger.info("Usuario Sol: " + configuracion.getUserNameSunatSunat());
        logger.info("Clave Sol: " + configuracion.getPasswordSunatSunat());
        logger.info("Client id: " + configuracion.getClientId());
        logger.info("Client Secret: " + configuracion.getClientSecret());
        logger.info("Scope: " + configuracion.getScope());

        String digestValue = db.generateDigestValue(documentWRP.getAdviceType().getUBLExtensions());
        String barcodeValue = db.generateGuiaBarcodeInfoV2(guia.getID().getValue(), IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE, fecha, BigDecimal.ZERO, BigDecimal.ZERO,
                guia.getDespatchSupplierParty(), guia.getDeliveryCustomerParty(), guia.getUBLExtensions());

        log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
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

                    byte[] pdfBytes = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
                    transactionResponse.setPdf(pdfBytes);

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

                if (transaction.getTransactionGuias().getTicketRest() != null && !transaction.getTransactionGuias().getTicketRest().isEmpty()) {

                    //CONSULT
                    ResponseDTO responseDTOJWT = getJwtSunat(configuracion);

                    ResponseDTO responseDTO = consult(transaction.getTransactionGuias().getTicketRest(), responseDTOJWT.getAccess_token());
                    if (responseDTO.getStatusCode() == 401) {
                        responseDTOJWT = getJwtSunat(configuracion);
                        responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
                    }

                    if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 404) {
                        return generateResponseRest(documentWRP, responseDTO);
                    }


                    //responseDTO.setCodRespuesta("0");
                    if (responseDTO.getCodRespuesta() != null && responseDTO.getCodRespuesta().equals("0")) {
                        transactionResponse = generateResponseRest(documentWRP, responseDTO);
                        byte[] pdf = documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion);
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

        log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));
        if (logger.isDebugEnabled()) {
            logger.debug("-transactionRemissionGuideDocument() [" + this.docUUID + "]");
        }

        if (client.getPdfBorrador().equals("true")) {
            transactionResponse.setPdfBorrador(documentFormatInterface.createPDFDocument(documentWRP, transaction, configuracion));
        }

        transactionResponse.setDigestValue(digestValue);
        transactionResponse.setBarcodeValue(barcodeValue);
        transactionResponse.setIdentificador(documentName);

        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        log.setResponse(new Gson().toJson(transactionResponse.getSunat()));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        log.setResponse(new Gson().toJson(transactionResponse.getMensaje()));
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
}
