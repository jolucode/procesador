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
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.wrapper.UBLDocumentWRP;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.handler.refactorPdf.service.impl.BaseDocumentService;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.model.model.ResponseDTO;
import service.cloud.request.clientRequest.model.model.ResponseDTOAuth;
import service.cloud.request.clientRequest.mongo.model.GuiaTicket;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.repo.IGuiaTicketRepo;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.core.ProcessorCoreInterface;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.ValidationHandler;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.PDFReportException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.despatchadvice_2.DespatchAdviceType;

import javax.activation.DataHandler;
import java.io.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class GuiaServiceImpl extends BaseDocumentService implements GuiaInterface {

    private static final Logger logger = Logger.getLogger(GuiaServiceImpl.class);

    //Estados
    private static final String ESTADO_NUEVO = "N";
    private static final String ESTADO_CONTINGENCIA = "C";
    private static final String VALOR_CONTINGENCIA = "Si";
    private static final String EXT_XML = "xml";
    private static final String EXT_ZIP = "zip";
    private static final String EMPTY = "";

    @Autowired
    private ClientProperties clientProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private ProcessorCoreInterface processorCoreInterface;

    @Autowired
    private DocumentFormatInterface documentFormatInterface;

    @Autowired
    private IGuiaTicketRepo guiaTicketRepo;

    private String docUUID;

    @Override
    public TransaccionRespuesta transactionRemissionGuideDocumentRest(TransacctionDTO transaction, String doctype) throws Exception {
        // Se inicializa el log con la fecha y algunos datos del emisor
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());

        TransaccionRespuesta transactionResponse = null;
        String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();

        // Determinar si es contingencia
        boolean isContingencia = false;
        for (Map<String, String> contractMap : transaction.getTransactionContractDocRefListDTOS()) {
            String valor = contractMap.get("cu31");
            if (valor != null && !valor.isEmpty()) {
                isContingencia = VALOR_CONTINGENCIA.equalsIgnoreCase(valor);
                break;
            }
        }

        // Validaciones iniciales
        ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);
        validationHandler.checkBasicInformation(
                transaction.getDOC_Id(),
                transaction.getDocIdentidad_Nro(),
                transaction.getDOC_FechaEmision(),
                transaction.getSN_EMail(),
                transaction.getEMail(),
                isContingencia);

        // Obtención de datos del cliente y el certificado digital
        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        String certificatePath = applicationProperties.getRutaBaseDocConfig() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
        byte[] certificado = CertificateUtils.getCertificateInBytes(certificatePath);

        String certiPassword = client.getCertificadoPassword();
        String ksProvider = applicationProperties.getSupplierCertificate();
        String ksType = applicationProperties.getKeystoreCertificateType();

        CertificateUtils.checkDigitalCertificateV2(certificado, certiPassword, ksProvider, ksType);

        // Generar la guía
        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        DespatchAdviceType despatchAdviceType = ublHandler.generateDespatchAdviceType(transaction, signerName);

        // Obtener el nombre del documento
        String documentName = DocumentNameHandler.getInstance().getRemissionGuideName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());


        // Manejo de archivos
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        Calendar fechaEmision = Calendar.getInstance();
        fechaEmision.setTime(transaction.getDOC_FechaEmision());
        int anio = fechaEmision.get(Calendar.YEAR);
        int mes = fechaEmision.get(Calendar.MONTH) + 1;
        int dia = fechaEmision.get(Calendar.DAY_OF_MONTH);

        String attachmentPath = applicationProperties.getRutaBaseDocAnexos() + transaction.getDocIdentidad_Nro() +
                File.separator + "anexo" + File.separator + anio + File.separator + mes + File.separator + dia + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
        fileHandler.setBaseDirectory(attachmentPath);

        // Guardar el documento XML en disco
        String documentPath = fileHandler.storeDocumentInDisk(despatchAdviceType, documentName);

        // Firmar el documento
        SignerHandler signerHandler = SignerHandler.newInstance();
        signerHandler.setConfiguration(certificado, certiPassword, ksType, ksProvider, signerName);
        File signedDocument = signerHandler.signDocument(documentPath, docUUID);

        // Obtener el documento firmado en memoria
        Object ublDocument = fileHandler.getSignedDocument(signedDocument, transaction.getDOC_Codigo());
        UBLDocumentWRP documentWRP = new UBLDocumentWRP();
        documentWRP.setTransaccion(transaction);
        documentWRP.setAdviceType((DespatchAdviceType) ublDocument);

        // Generar el PDF
        String fechaVenc = transaction.getDOC_FechaVencimiento();
        DespatchAdviceType guia = documentWRP.getAdviceType();

        // Generar XML firmado en memoria
        byte[] xmlDocument = DocumentConverterUtils.convertDocumentToBytes(despatchAdviceType);
        byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);

        // Guardar el XML firmado en disco
        try {
            UtilsFile.storeDocumentInDisk(signedXmlDocument, documentName, EXT_XML, attachmentPath);
            logger.info("Archivo firmado guardado exitosamente en: " + attachmentPath);
        } catch (IOException e) {
            logger.error("Error al guardar el archivo: " + e.getMessage());
        }

        // Comprimir el documento
        DataHandler zipDocument = UtilsFile.compressUBLDocument(signedXmlDocument, documentName + "." + EXT_XML);
        if (logger.isInfoEnabled()) {
            logger.info("transactionRemissionGuideDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP.");
        }
        logger.error("ERRROR +++++++++ ==========" + zipDocument.toString());

        // Configuración para conectarse a SUNAT / OSE
        ConfigData configuracion = ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .userNameSunatSunat(client.getUserNameSunatGuias())
                .passwordSunatSunat(client.getPasswordSunatGuias())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .rutaBaseConfig(applicationProperties.getRutaBaseDocConfig())
                .build();

        logger.info("Se esta apuntando al ambiente : " + configuracion.getAmbiente() + " - " + configuracion.getIntegracionWs());
        if ("OSE".equals(configuracion.getIntegracionWs())) {
            logger.info("Url Service: " + applicationProperties.getUrlOse());
        } else if ("SUNAT".equals(configuracion.getIntegracionWs())) {
            logger.info("Url Service: " + applicationProperties.getUrlSunatEmision());
        }
        logger.info("Usuario Sol: " + configuracion.getUserNameSunatSunat());
        logger.info("Clave Sol: " + configuracion.getPasswordSunatSunat());
        logger.info("Client id: " + configuracion.getClientId());
        logger.info("Client Secret: " + configuracion.getClientSecret());

        // Generar valores digest y barcode
        String digestValue = generateDigestValue(documentWRP.getAdviceType().getUBLExtensions());
        String barcodeValue = generateGuiaBarcodeInfoV2(
                guia.getID().getValue(),
                IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE,
                fechaVenc,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                guia.getDespatchSupplierParty(),
                guia.getDeliveryCustomerParty(),
                guia.getUBLExtensions()
        );

        // Log de fecha de invocación a servicio externo
        log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
        try {
            if (zipDocument != null) {
                String feEstado = transaction.getFE_Estado();
                if (ESTADO_NUEVO.equalsIgnoreCase(feEstado)) {
                    // Estado "N", se declara o se consulta si ya está aprobado
                    transactionResponse = processEstadoN(transaction, configuracion, fileHandler, signedDocument,
                            signedXmlDocument, documentName, documentWRP);
                } else if (ESTADO_CONTINGENCIA.equalsIgnoreCase(feEstado)) {
                    // Estado "C", se consulta ticket
                    transactionResponse = processEstadoC(transaction, configuracion, fileHandler, signedDocument,
                            documentName, documentWRP);
                }
            } else {
                logger.error("transactionRemissionGuideDocument() [" + this.docUUID + "] ERROR: " + IVenturaError.ERROR_457.getMessage());
                throw new NullPointerException(IVenturaError.ERROR_457.getMessage());
            }
        } catch (Exception e) {
            transactionResponse.setMensaje("Error : " + e.getMessage());
        }

        // Log de fecha de respuesta del servicio externo
        log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

        String errorPdf = "";
        // Generar PDF borrador
        if ("true".equals(client.getPdfBorrador())) {
            if (transactionResponse != null && transactionResponse.getPdf() != null) {
                transactionResponse.setPdfBorrador(transactionResponse.getPdf());
            } else {
                try {
                    byte[] pdf = processorCoreInterface.processCDRResponseContigencia(null,
                            fileHandler,
                            documentName,
                            transaction.getDOC_Codigo(),
                            documentWRP,
                            transaction,
                            configuracion);
                    transactionResponse.setPdfBorrador(pdf);
                } catch (Exception e) {
                    errorPdf = e.getMessage();
                }
            }
        }

        //guardar .zip
        if(transactionResponse.getZip()!= null)
            UtilsFile.storeDocumentInDisk(transactionResponse.getZip(), documentName, EXT_ZIP, attachmentPath);

        // Asignar valores de digest y barcode a la respuesta
        if (transactionResponse != null) {
            transactionResponse.setDigestValue(digestValue);
            transactionResponse.setBarcodeValue(barcodeValue);
            transactionResponse.setIdentificador(documentName);
        }

        // Log de detalles en DB
        log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
        log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        String messageResponse = (JsonUtils.toJson(transactionResponse.getSunat())).equals("null") ? transactionResponse.getMensaje() : (JsonUtils.toJson(transactionResponse.getSunat()));
        log.setResponse(messageResponse + " - " + transactionResponse.getTicketRest() + " " + errorPdf );
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        transactionResponse.setLogDTO(log);
        log.setPathBase(attachmentPath + "\\" + documentName + ".json");
        if (errorPdf != null)
            transactionResponse.setMensaje(transactionResponse.getMensaje() + " - " + errorPdf);
        else {
            transactionResponse.setMensaje(transactionResponse.getMensaje());
        }
        return transactionResponse;
    }

    private TransaccionRespuesta processEstadoN(TransacctionDTO transaction,
                                                ConfigData configuracion,
                                                FileHandler fileHandler,
                                                File signedDocument,
                                                byte[] documentPath,
                                                String documentName,
                                                UBLDocumentWRP documentWRP) throws InterruptedException, IOException {
        TransaccionRespuesta transactionResponse = null;
        GuiaTicket ticketMongoSave = guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaction.getDocIdentidad_Nro(), transaction.getFE_Id()
        ).block();

        if (ticketMongoSave == null) {
            logger.warn("No se encontró ticket previo en la BD para RUC: " + transaction.getDocIdentidad_Nro()
                    + " y FE_Id: " + transaction.getFE_Id() + " (estado N)");
        }

        // Ticket ya aprobado
        if (ticketMongoSave != null && !ticketMongoSave.getTicketSunat().isEmpty() && ("APROBADO".equals(ticketMongoSave.getEstadoTicket()) || "PROCESO".equals(ticketMongoSave.getEstadoTicket()))) {
            String ticketToUse = ticketMongoSave.getTicketSunat();
            if (ticketToUse != null && !ticketToUse.isEmpty()) {
                ResponseDTO responseDTO = consultarTicketEnSunat(ticketToUse, configuracion);
                // ✅ Actualizar/eliminar en base al resultado
                updateTicketStatusFromConsulta(transaction, responseDTO);
                transactionResponse = manejarRespuestaSunat(responseDTO, documentWRP, fileHandler, documentName, transaction, configuracion);
                if (transactionResponse != null) {
                    byte[] documentBytes = fileHandler.convertFileToBytes(signedDocument);
                    transactionResponse.setXml(documentBytes);
                }
            }
        } else {
            ResponseDTO responseDTOJWT = getJwtSunat(configuracion);
            if (responseDTOJWT.getStatusCode() == 400 || responseDTOJWT.getStatusCode() == 401) {
                return generateResponseRest(documentWRP, responseDTOJWT);
            }

            // DECLARE GENERA TICKET
            ResponseDTO responseDTO = declareSunat(documentName, documentPath, responseDTOJWT.getAccess_token());
            logger.info(transaction.getDocIdentidad_Nro() + "-" + transaction.getDOC_Codigo() + "-" + transaction.getDOC_Id() + " Ticket Guia : " + responseDTO.getNumTicket());

            logger.info("Ticket Guia: " + responseDTO.getNumTicket() +
                    "| Ruc-Tipo-Serie-Correlativo: " +
                    transaction.getDocIdentidad_Nro() + "-" +
                    transaction.getDOC_Codigo() + "-" +
                    transaction.getDOC_Serie() + "-" +
                    transaction.getDOC_Numero());

            // ✅ Persistir inmediatamente
            if (responseDTO.getNumTicket() != null && !responseDTO.getNumTicket().isEmpty()) {
                saveTicketRest(transaction, responseDTO);
            }

            // Manejo 401 (renovar token y volver a declarar)
            if (responseDTO.getStatusCode() == 401) {
                responseDTOJWT = getJwtSunat(configuracion);
                responseDTO = declareSunat(documentName, documentPath, responseDTOJWT.getAccess_token());

                // Persistir nuevamente si nuevo ticket
                if (responseDTO.getNumTicket() != null && !responseDTO.getNumTicket().isEmpty()) {
                    saveTicketRest(transaction, responseDTO);
                }
            }

            // Esperar 5 segundos para consultar el estado del ticket
            Thread.sleep(5000);

            // CONSULT
            responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());

            if (responseDTO.getStatusCode() == 401) {
                responseDTOJWT = getJwtSunat(configuracion);
                responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
            }

            AtomicInteger contador = new AtomicInteger(0);
            while ("98".equals(responseDTO.getCodRespuesta())) {
                Thread.sleep(5000);
                if (contador.incrementAndGet() == 5) break;
                responseDTO = consult(responseDTO.getNumTicket(), responseDTOJWT.getAccess_token());
            }

            // ✅ Actualizar/eliminar en base al resultado
            updateTicketStatusFromConsulta(transaction, responseDTO);

            // Manejar respuestas de SUNAT
            transactionResponse = manejarRespuestaSunat(responseDTO, documentWRP, fileHandler, documentName, transaction, configuracion);

            // Añadir XML firmado
            if (transactionResponse != null) {
                byte[] documentBytes = fileHandler.convertFileToBytes(signedDocument);
                transactionResponse.setTicketRest(responseDTO.getNumTicket());
                transactionResponse.setXml(documentBytes);
            }

            // ✅ SOLO guardar si fue aprobado
            if (transactionResponse != null && transactionResponse.getMensaje().contains("Documento aprobado")) {

                // Verificación final antes de guardar
                GuiaTicket yaExiste = guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                        transaction.getDocIdentidad_Nro(), transaction.getFE_Id()
                ).block();

                boolean yaExisteAprobado = yaExiste != null &&
                        "APROBADO".equalsIgnoreCase(yaExiste.getEstadoTicket()) &&
                        !yaExiste.getTicketSunat().isEmpty();

                if (!yaExisteAprobado) {
                    saveTicketRest(transaction, responseDTO);
                }
            }
        }
        return transactionResponse;
    }

    public void updateTicketStatusFromConsulta(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaccion.getDocIdentidad_Nro(),
                transaccion.getFE_Id()
        ).flatMap(existing -> {
            if (existing == null) {
                logger.warn("No se encontró ticket para actualizar luego de consulta SUNAT.");
                return Mono.empty();
            }

            String cod = responseDTO.getCodRespuesta();
            if ("99".equals(cod)) {
                logger.warn("Respuesta 99 en consulta SUNAT. Eliminando ticket...");
                return guiaTicketRepo.delete(existing)
                        .doOnSuccess(v -> logger.info("Ticket eliminado tras codRespuesta 99"));
            } else {
                if ("98".equals(cod))
                    existing.setEstadoTicket("PROCESO");
                else if ("0".equals(cod))
                    existing.setEstadoTicket("APROBADO");
                existing.setCreadoEn(DateUtils.formatDateToString(new Date()));
                return guiaTicketRepo.save(existing)
                        .doOnSuccess(v -> logger.info("Ticket actualizado a estado: " + existing.getEstadoTicket()));
            }
        }).subscribe();
    }


    private TransaccionRespuesta processEstadoC(TransacctionDTO transaction,
                                                ConfigData configuracion,
                                                FileHandler fileHandler,
                                                File signedDocument,
                                                String documentName,
                                                UBLDocumentWRP documentWRP) throws IOException {
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

        GuiaTicket ticketMongoSave = guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaction.getDocIdentidad_Nro(), transaction.getFE_Id()
        ).block();

        if (ticketMongoSave == null) {
            logger.warn("No se encontró ticket previo en la BD para RUC: " + transaction.getDocIdentidad_Nro()
                    + " y FE_Id: " + transaction.getFE_Id() + " (estado N)");
        }

        if (ticketMongoSave != null && !ticketMongoSave.getTicketSunat().isEmpty()) {
            ResponseDTO responseDTO = consultarTicketEnSunat(ticketMongoSave.getTicketSunat(), configuracion);

            // ✅ Actualizar/eliminar en base al resultado
            updateTicketStatusFromConsulta(transaction, responseDTO);
            transactionResponse = manejarRespuestaSunat(responseDTO, documentWRP, fileHandler, documentName, transaction, configuracion);

            if (transactionResponse != null) {
                byte[] documentBytes = fileHandler.convertFileToBytes(signedDocument);
                transactionResponse.setXml(documentBytes);
            }
        }

        if (ticketMongoSave != null && ticketMongoSave.getEstadoTicket() !=null) {
            logger.info("Ticket guia: " + ticketMongoSave.getEstadoTicket() +
                    "| Ruc-Tipo-Serie-Correlativo: " +
                    transaction.getDocIdentidad_Nro() + "-" +
                    transaction.getDOC_Codigo() + "-" +
                    transaction.getDOC_Serie() + "-" +
                    transaction.getDOC_Numero());
        } else {
            logger.error("El ticket que se quiere consultar no existe en la base datos");
        }


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

    public ResponseDTO declareSunat(String documentName, byte[] fileContent, String token) throws IOException {

        // 1. Comprimir los bytes en memoria para generar un .zip
        //    Suponiendo que 'documentName + ".xml"' es el nombre interno del archivo dentro del ZIP.
        byte[] zipBytes = compressToZip(fileContent, documentName + ".xml");


        String fileTo64 = zipDocumentTo64(zipBytes);
        String fileTo256Sha = zipDocumentTo265Sha(zipBytes);
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

    private byte[] compressToZip(byte[] fileContent, String internalFileName) throws IOException {
        if (fileContent == null || fileContent.length == 0) {
            return new byte[0];
        }

        // ByteArrayOutputStream para almacenar el ZIP en memoria
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Crear una entrada en el ZIP con el nombre interno
            ZipEntry entry = new ZipEntry(internalFileName);
            zos.putNextEntry(entry);

            // Escribir los bytes en la entrada
            zos.write(fileContent, 0, fileContent.length);
            zos.closeEntry();
            zos.finish();

            // Retornar todos los bytes del ZIP
            return baos.toByteArray();
        }
    }

    public String zipDocumentTo265Sha(byte[] documentContent) {
        if (documentContent == null || documentContent.length == 0) {
            return "";
        }
        // DigestUtils.sha256Hex procesa los bytes y retorna el hash en formato hexadecimal
        return DigestUtils.sha256Hex(documentContent);
    }

    public String zipDocumentTo64(byte[] fileContent) {
        if (fileContent == null || fileContent.length == 0) {
            return null;
        }
        // Codifica los bytes en Base64 y los convierte a String
        return new String(Base64.encodeBase64(fileContent));
    }

    public ResponseDTO getJwtSunat(ConfigData configuracion) throws IOException {

        HttpResponse<String> response = Unirest.post("https://api-seguridad.sunat.gob.pe/v1/clientessol/" + configuracion.getClientId() + "/oauth2/token/")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "TS019e7fc2=014dc399cbcad00473c65166bab99ef2e22263ae15b2a9e259ff0e5d972578fa54549fe8acccb61b76d241060b054cc73beff45ea3")
                .field("grant_type", "password")
                .field("scope", "https://api-cpe.sunat.gob.pe/")
                .field("client_id", configuracion.getClientId())
                .field("client_secret", configuracion.getClientSecret())
                .field("username", configuracion.getUserNameSunatSunat())
                .field("password", configuracion.getPasswordSunatSunat())
                .field("", "")
                .asString();

        ObjectMapper objectMapper = new ObjectMapper();
        ResponseDTO responseDTO = new ResponseDTO();
        ResponseDTOAuth responseDTO400;

        if (response.getStatus() == 400) {
            responseDTO400 = objectMapper.readValue(response.body(), new TypeReference<ResponseDTOAuth>() {});
            responseDTO.setResponseDTO400(responseDTO400);
        } else {
            responseDTO = objectMapper.readValue(response.body(), new TypeReference<ResponseDTO>() {});
        }
        responseDTO.setStatusCode(response.getStatus());
        return responseDTO;
    }

    public TransaccionRespuesta generateResponseRest(UBLDocumentWRP documentWRP, ResponseDTO responseDTO) {
        TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
        sunatResponse.setId(documentWRP.getTransaccion().getDOC_Serie() + "-" + documentWRP.getTransaccion().getDOC_Numero());

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 401) {
            // Error de token o credenciales
            if(responseDTO.getResponseDTO400() != null) {
                transactionResponse.setMensaje(responseDTO.getResponseDTO400().getError() + " - " + responseDTO.getResponseDTO400().getError_description());
            } else {
                transactionResponse.setMensaje("Error no especificado");
            }
        } else if (responseDTO.getStatusCode() == 404) {
            transactionResponse.setMensaje("El número de ticket para consultar no existe");
            sunatResponse.setAceptado(false);
            transactionResponse.setSunat(sunatResponse);
        } else {
            // Manejo de códigos de respuesta
            if ("0".equals(responseDTO.getCodRespuesta())) {
                // Aprobado
                String rptBase64 = responseDTO.getArcCdr();
                byte[] bytesZip = Base64.decodeBase64(rptBase64);
                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - " + "Documento aprobado");
                transactionResponse.setZip(bytesZip);
                sunatResponse.setAceptado(true);
                transactionResponse.setSunat(sunatResponse);

            } else if ("98".equals(responseDTO.getCodRespuesta())) {
                // En proceso
                transactionResponse.setMensaje(responseDTO.getCodRespuesta() + " - Documento en proceso, volver a consultar.");
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);

            } else if ("99".equals(responseDTO.getCodRespuesta())) {
                // Rechazado
                if ("1".equals(responseDTO.getIndCdrGenerado())) {
                    String rptBase64 = responseDTO.getArcCdr();
                    byte[] bytesZip = Base64.decodeBase64(rptBase64);
                    transactionResponse.setZip(bytesZip);
                }
                transactionResponse.setMensaje(responseDTO.getError().getNumError() + " - " + responseDTO.getError().getDesError());
                sunatResponse.setAceptado(false);
                transactionResponse.setSunat(sunatResponse);
            }
        }
        return transactionResponse;
    }

    public void saveTicketRest(TransacctionDTO transaccion, ResponseDTO responseDTO) {
        guiaTicketRepo.findGuiaTicketByRucEmisorAndFeId(
                transaccion.getDocIdentidad_Nro(),
                transaccion.getFE_Id()
        ).hasElement().subscribe(exists -> {
            if (!exists) {
                GuiaTicket guiaTicket = new GuiaTicket();
                guiaTicket.setRucEmisor(transaccion.getDocIdentidad_Nro());
                guiaTicket.setFeId(transaccion.getFE_Id());
                guiaTicket.setTicketSunat(responseDTO.getNumTicket());
                guiaTicket.setCreadoEn(DateUtils.formatDateToString(new Date()));

                guiaTicketRepo.save(guiaTicket).subscribe();
            } else {
                logger.warn("Ya existe ticket para esta guía, no se vuelve a guardar.");
            }
        });
    }

    private ResponseDTO consultarTicketEnSunat(String ticket, ConfigData configuracion) throws IOException {
        ResponseDTO responseDTOJWT = getJwtSunat(configuracion);
        ResponseDTO responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        if (responseDTO.getStatusCode() == 401) {
            // Renovar token y volver a intentar
            responseDTOJWT = getJwtSunat(configuracion);
            responseDTO = consult(ticket, responseDTOJWT.getAccess_token());
        }
        return responseDTO;
    }

    private TransaccionRespuesta manejarRespuestaSunat(ResponseDTO responseDTO,
                                                       UBLDocumentWRP documentWRP,
                                                       FileHandler fileHandler,
                                                       String documentName,
                                                       TransacctionDTO transaction,
                                                       ConfigData configuracion) {
        // Errores de credenciales o ticket
        if (responseDTO.getStatusCode() == 400 || responseDTO.getStatusCode() == 404) {
            return generateResponseRest(documentWRP, responseDTO);
        }

        TransaccionRespuesta transactionResponse = null;

        if (responseDTO.getCodRespuesta() != null) {
            switch (responseDTO.getCodRespuesta()) {
                case "0":
                    // Documento aprobado
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);

                    // Procesar ZIP y URL de guía
                    String rptBase64 = responseDTO.getArcCdr();
                    byte[] bytesZip = Base64.decodeBase64(rptBase64);
                    String sunatResponseUrlPdfGuia = SunatResponseUtils.proccessResponseUrlPdfGuia(bytesZip);
                    configuracion.setUrlGuias(sunatResponseUrlPdfGuia);

                    transactionResponse.setTicketRest(responseDTO.getNumTicket());
                    break;

                case "98":
                case "99":
                    // Documento en proceso o rechazado
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    break;

                default:
                    transactionResponse = generateResponseRest(documentWRP, responseDTO);
                    break;
            }

            // Generar PDF para todos los casos válidos
            if (transactionResponse != null) {
                byte[] pdf = null;


                try {
                    pdf = processorCoreInterface.processCDRResponseContigencia(null,
                            fileHandler,
                            documentName,
                            transaction.getDOC_Codigo(),
                            documentWRP,
                            transaction,
                            configuracion);
                } catch (PDFReportException e) {
                    transactionResponse.setErrorPdf(e.getMessage());
                    throw new RuntimeException(e);
                }
                transactionResponse.setPdf(pdf);
            }

            return transactionResponse;
        }

        return null;
    }

}
