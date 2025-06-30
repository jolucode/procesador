package service.cloud.request.clientRequest.service.emision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.estela.dto.FileRequestDTO;
import service.cloud.request.clientRequest.estela.dto.FileResponseDTO;
import service.cloud.request.clientRequest.estela.service.DocumentBajaQueryService;
import service.cloud.request.clientRequest.estela.service.DocumentBajaService;
import service.cloud.request.clientRequest.extras.ISignerConfig;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.UBLDocumentHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.handler.document.SignerHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.JsonUtils;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.Utils;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServiceBaja implements IServiceBaja {

    private static final Logger logger = LoggerFactory.getLogger(ServiceBaja.class);

    @Autowired private ClientProperties clientProperties;
    @Autowired private ApplicationProperties applicationProperties;
    @Autowired private ITransaccionBajaRepository iTransaccionBajaRepository;
    @Autowired private DocumentBajaQueryService documentBajaQueryService;
    @Autowired private DocumentBajaService documentBajaService;

    private static final String docUUID = "123123";

    @Override
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) {
        if (transaction == null) throw new IllegalArgumentException("La transacción no puede ser nula.");

        LogDTO log = inicializarLog(transaction);
        TransaccionRespuesta response = new TransaccionRespuesta();

        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDocAnexos());
        FileHandler fileHandler = FileHandler.newInstance(docUUID);
        fileHandler.setBaseDirectory(attachmentPath);

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        if (client == null) return mensajeError("Cliente no encontrado.", response);

        ConfigData config = createConfigData(client);
        CdrStatusResponse cdrResponse = new CdrStatusResponse();
        String documentName = "";

        try {
            TransaccionBaja transBaja = findByRucEmpresaAndDocId(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            FileRequestDTO soapRequest = buildSoapRequest(transaction, client, config);

            String ticket = obtenerOTramitarTicket(transBaja, transaction, client, fileHandler, config, soapRequest, attachmentPath);
            soapRequest.setTicket(ticket);

            log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
            FileResponseDTO sunatResponse = consultarEstadoConReintentos(soapRequest).block();
            log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

            if (sunatResponse == null) return mensajeError("Error al consultar el estado del ticket.", response);

            cdrResponse.setContent(sunatResponse.getContent());
            cdrResponse.setStatusMessage(sunatResponse.getMessage());
            response.setTicketRest(ticket);

            logger.info("Ticket baja: {}| Ruc-Tipo-Serie-Correlativo: {}-{}-{}-{}", ticket,
                    transaction.getDocIdentidad_Nro(), transaction.getDOC_Codigo(),
                    transaction.getDOC_Serie(), transaction.getDOC_Numero());

            if (cdrResponse.getContent() != null) {
                documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(
                        transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
                response = processOseResponseBAJA(cdrResponse.getContent(), transaction, documentName, config);
            } else {
                response.setMensaje(cdrResponse.getStatusMessage());
            }

            response.setIdentificador(documentName);
            response.setTicketRest(ticket);
        } catch (Exception ex) {
            logger.error("Error en transactionVoidedDocument", ex);
            response.setMensaje("Ocurrió un error en el proceso de anulación: " + ex.getMessage());
        }

        completarLog(log, response, transaction, attachmentPath);
        response.setLogDTO(log);
        return response;
    }

    // Métodos auxiliares que refactorizan responsabilidades del método principal:

    private LogDTO inicializarLog(TransacctionDTO tx) {
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(tx.getDocIdentidad_Nro());
        log.setBusinessName(tx.getRazonSocial());
        return log;
    }

    private TransaccionRespuesta mensajeError(String msg, TransaccionRespuesta response) {
        response.setMensaje(msg);
        return response;
    }

    private FileRequestDTO buildSoapRequest(TransacctionDTO tx, Client client, ConfigData config) {
        FileRequestDTO request = new FileRequestDTO();
        String serviceUrl = applicationProperties.obtenerUrl(
                client.getIntegracionWs(), tx.getFE_Estado(), tx.getFE_TipoTrans(), tx.getDOC_Codigo()
        );
        request.setService(serviceUrl);
        request.setUsername(config.getUsuarioSol());
        request.setPassword(config.getClaveSol());
        //request.setFileName(DocumentNameHandler.getInstance().getZipName(""));
        return request;
    }

    private String obtenerOTramitarTicket(TransaccionBaja transBaja, TransacctionDTO tx, Client client,
                                          FileHandler fileHandler, ConfigData config, FileRequestDTO soapRequest, String attachmentPath) throws Exception {
        if (transBaja != null && transBaja.getTicketBaja() != null && !transBaja.getTicketBaja().isEmpty()) {
            return transBaja.getTicketBaja();
        }

        if (tx.getFE_Comentario() == null || tx.getFE_Comentario().isEmpty()) {
            throw new IllegalArgumentException("Ingresar razón de anulación, y colocar APROBADO y volver a consultar.");
        }

        TransaccionBaja transaccionBaja = generarIDyFecha(tx);
        tx.setANTICIPO_Id(transaccionBaja.getSerie());

        String certPath = applicationProperties.getRutaBaseDocConfig() + tx.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
        byte[] certificado = CertificateUtils.loadCertificate(certPath);
        CertificateUtils.validateCertificate(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());

        String signerName = ISignerConfig.SIGNER_PREFIX + tx.getDocIdentidad_Nro();
        SignerHandler signer = SignerHandler.newInstance();
        signer.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);

        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(docUUID);
        String docNameRaw = DocumentNameHandler.getInstance().getVoidedDocumentName(tx.getDocIdentidad_Nro(), tx.getANTICIPO_Id());
        String docNameFinal = tx.getDOC_Serie().startsWith("B") ? docNameRaw.replace("RA", "RC") : docNameRaw;
        byte[] xmlDocument;

        if (tx.getDOC_Serie().startsWith("B") || tx.getDOC_Codigo().equals("03")) {
            SummaryDocumentsType summary = ublHandler.generateSummaryDocumentsTypeV2(tx, signerName);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(summary);
            fileHandler.storeDocumentInDisk(summary, docNameFinal);
        } else {
            VoidedDocumentsType voided = ublHandler.generateVoidedDocumentType(tx, signerName);
            xmlDocument = DocumentConverterUtils.convertDocumentToBytes(voided);
            fileHandler.storeDocumentInDisk(voided, docNameFinal);
        }

        // Firmar y almacenar
        byte[] signed = signer.signDocumentv2(xmlDocument, docUUID);
        UtilsFile.storeDocumentInDisk(signed, docNameFinal, "xml", attachmentPath);

        // Comprimir y codificar
        byte[] zipBytes = compressUBLDocumentv2(signed, docNameFinal + ".xml");
        soapRequest.setContentFile(convertToBase64(zipBytes));

        // IMPORTANTE: ahora sí, con nombre correcto
        soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(docNameFinal));

        FileResponseDTO fileResponse = documentBajaService.processBajaRequest(soapRequest.getService(), soapRequest).block();
        if (fileResponse == null || fileResponse.getTicket() == null) {
            throw new IllegalStateException("No se recibió ticket de SUNAT.");
        }

        transaccionBaja.setTicketBaja(fileResponse.getTicket());
        iTransaccionBajaRepository.save(transaccionBaja).block();

        return fileResponse.getTicket();
    }

    private void completarLog(LogDTO log, TransaccionRespuesta txResp, TransacctionDTO tx, String path) {
        String docName = txResp.getIdentificador();
        log.setPathThirdPartyRequestXml(path + "\\" + docName + ".xml");
        log.setPathThirdPartyResponseXml(path + "\\" + docName + ".zip");
        log.setPathBase(path + "\\" + docName + ".json");
        log.setObjectTypeAndDocEntry(tx.getFE_ObjectType() + " - " + tx.getFE_DocEntry());
        log.setSeriesAndCorrelative(docName);
        log.setResponse((JsonUtils.toJson(txResp.getSunat())).equals("null") ? txResp.getMensaje() : JsonUtils.toJson(txResp.getSunat()));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
    }

    // Mantén los métodos adicionales ya existentes como consultarEstadoConReintentos(), generarIDyFecha(), etc.
    private ConfigData createConfigData(Client client) {
        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .rutaBaseConfig(applicationProperties.getRutaBaseDocConfig())
                .build();
    }

    public TransaccionBaja findByRucEmpresaAndDocId(String rucEmpresa, String docId) {
        return iTransaccionBajaRepository.findFirstByRucEmpresaAndDocId(rucEmpresa, docId).block();
    }

    public Mono<FileResponseDTO> consultarEstadoConReintentos(FileRequestDTO soapRequest) {
        return Mono.defer(() ->
                Mono.delay(Duration.ofSeconds(5))
                        .flatMap(t -> documentBajaQueryService.processAndSaveFile(soapRequest.getService(), soapRequest))
                        .flatMap(response -> {
                            if (!response.getMessage().contains("98")) {
                                return Mono.just(response);
                            } else {
                                return Mono.error(new IllegalStateException("SUNAT  aún está procesando (código 98)"));
                            }
                        })
        ).retryWhen(
                Retry.fixedDelay(5, Duration.ofSeconds(5))
                        .filter(ex -> ex.getMessage().contains("98"))
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                new RuntimeException("SUNAT no procesó el ticket después de varios intentos."))
        );
    }

    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            /**se realiza el anexo del documento de baja*/
            if (null != statusResponse && 0 < statusResponse.length) {
                UtilsFile.storePDFDocumentInDisk(statusResponse, applicationProperties.getRutaBaseDocAnexos(), documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);//fileHandler.storePDFDocumentInDisk(statusResponse, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
            }

            String mensaje = sunatResponse.getMensaje();

            if (mensaje != null) {
                mensaje = mensaje.replace("La Comunicacion de baja", "La Comunicación de Baja");
                mensaje = mensaje.replace("El Resumen diario", "El Resumen de Boletas Baja" );
                mensaje = mensaje.replace("El resumen de reversion", "La Comunicación de Baja");
            }

            transactionResponse.setMensaje(mensaje);
            transactionResponse.setZip(statusResponse);

        } else {
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);
        }

        return transactionResponse;
    }

    public TransaccionBaja generarIDyFecha(TransacctionDTO tr) {
        String serie = "";
        TransaccionBaja trb = new TransaccionBaja();
        try {

            // Determinar prefijo basado en el valor de DOC_Codigo
            String prefijo;
            if (Arrays.asList("20", "40").contains(tr.getDOC_Codigo())) {
                prefijo = "RR-";  // Si DOC_Codigo es 20 o 40, el prefijo es "RR-"
            } else {
                prefijo = "RA-";  // Para otros tipos de documentos, el prefijo es "RA-"
            }

            // Obtener el último registro para la empresa especificada
            Mono<TransaccionBaja> trbb = iTransaccionBajaRepository.findFirstByRucEmpresaOrderByFechaDescIddDesc(tr.getDocIdentidad_Nro());
            trb = trbb.block();

            LocalDateTime date = LocalDateTime.now();
            String fechaActual = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            if (trb != null) {
                String fechaUltimoRegistro = trb.getFecha();
                if (fechaActual.equals(fechaUltimoRegistro)) {
                    // Actualizar el último registro
                    String nuevoId = generarNuevoId(trb.getSerie());
                    serie = Utils.construirSerie(prefijo, fechaActual, nuevoId);

                    //actualizarRegistro(trb, fechaActual, nuevoId);
                    trb = crearNuevoRegistro(tr.getDocIdentidad_Nro(), trb.getIdd(), fechaActual, serie);
                } else {
                    // Crear un nuevo registro
                    serie = Utils.construirSerie(prefijo, fechaActual, "00001");
                    trb.setIdd(0);
                    trb = crearNuevoRegistro(tr.getDocIdentidad_Nro(), trb.getIdd(), fechaActual, serie);
                }
                //trb.setTicketBaja(tr.getTicket_Baja());
            } else {
                // Crear el primer registro para la empresa
                serie = Utils.construirSerie(prefijo, fechaActual, "00001");
                trb = crearNuevoRegistro(tr.getDocIdentidad_Nro(), 0, fechaActual, serie);
            }

            tr.setANTICIPO_Id(serie);
            trb.setDocId(tr.getDOC_Id());
            //trb = iTransaccionBajaRepository.save(trb).block();
            //iTransaccionBajaRepository.save(trb);

        } catch (Exception ex) {
            // Manejo de excepciones
            System.err.println(ex.getMessage());
        }
        return trb;
    }

    private TransaccionBaja crearNuevoRegistro(String rucEmpresa, Integer idd, String fecha, String serie) {
        TransaccionBaja nuevaBaja = new TransaccionBaja();
        nuevaBaja.setRucEmpresa(rucEmpresa);
        nuevaBaja.setFecha(fecha);
        nuevaBaja.setIdd(++idd);
        nuevaBaja.setSerie(serie);
        return nuevaBaja;
    }

    private String generarNuevoId(String serie) {
        int indexOf = serie.lastIndexOf("-");
        String fin = serie.substring(indexOf + 1);
        int numero = Integer.parseInt(fin);
        numero++;
        return String.format("%05d", numero);
    }

    private byte[] compressUBLDocumentv2(byte[] document, String documentName) throws IOException {
        byte[] zipDocument = null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(document);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            byte[] array = new byte[10000];
            int read;
            zos.putNextEntry(new ZipEntry(documentName));

            while ((read = bis.read(array)) != -1) {
                zos.write(array, 0, read);
            }

            zos.closeEntry();
            zos.finish(); // Forzar el cierre de la entrada ZIP antes de obtener los bytes
            zipDocument = bos.toByteArray();  // Devolver directamente los bytes comprimidos

        } catch (Exception e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage());
        }

        return zipDocument; // Devuelve los bytes comprimidos directamente
    }

    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }
}
