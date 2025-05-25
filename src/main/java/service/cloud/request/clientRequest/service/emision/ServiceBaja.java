package service.cloud.request.clientRequest.service.emision;

import com.google.gson.Gson;
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
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.*;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.activation.DataHandler;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ServiceBaja implements IServiceBaja {

    Logger logger = LoggerFactory.getLogger(ServiceBaja.class);

    @Autowired
    ClientProperties clientProperties;

    @Autowired
    ApplicationProperties applicationProperties;

    private final String docUUID = "123123";

    @Autowired
    ITransaccionBajaRepository iTransaccionBajaRepository;

    @Autowired
    DocumentBajaQueryService documentBajaQueryService;

    @Autowired
    DocumentBajaService documentBajaService;


    @Override
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception {

        /***/
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getRazonSocial());
        /***/

        // 1. Validación inicial de entrada
        if (transaction == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula.");
        }

        transaction.setANTICIPO_Id(generarIDyFecha(transaction));

        // 2. Obtener ruta y manejar archivos
        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDocAnexos());
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());

        if (client == null) {
            transactionResponse.setMensaje("Cliente no encontrado.");
            return transactionResponse;
        }

        ConfigData configuracion = createConfigData(client);
        CdrStatusResponse cdrStatusResponse = new CdrStatusResponse();
        String documentName = "";
        try {
            // 3. Validar comentario
            if (transaction.getFE_Comentario() == null || transaction.getFE_Comentario().isEmpty()) {
                transactionResponse.setMensaje("Ingresar razón de anulación, y colocar APROBADO y volver a consultar.");
                return transactionResponse;
            }

            // 4. Cargar y validar certificado
            String certificatePath = applicationProperties.getRutaBaseDocConfig() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();
            byte[] certificado = CertificateUtils.loadCertificate(certificatePath);
            CertificateUtils.validateCertificate(certificado, client.getCertificadoPassword(), applicationProperties.getSupplierCertificate(), applicationProperties.getKeystoreCertificateType());

            // 5. Configurar firma
            String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), applicationProperties.getKeystoreCertificateType(), applicationProperties.getSupplierCertificate(), signerName);

            // 6. Generar documento UBL
            UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
            documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
            byte[] xmlDocument;

            if (transaction.getDOC_Serie().startsWith("B")) {
                documentName = documentName.replace("RA", "RC");
                SummaryDocumentsType summaryVoidedDocumentType = ublHandler.generateSummaryDocumentsTypeV2(transaction, signerName);
                xmlDocument = DocumentConverterUtils.convertDocumentToBytes(summaryVoidedDocumentType);
                fileHandler.storeDocumentInDisk(summaryVoidedDocumentType, documentName);
            } else {
                VoidedDocumentsType voidedDocumentType = ublHandler.generateVoidedDocumentType(transaction, signerName);
                xmlDocument = DocumentConverterUtils.convertDocumentToBytes(voidedDocumentType);
                fileHandler.storeDocumentInDisk(voidedDocumentType, documentName);
            }

            // 7. Firmar documento
            byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);

            // 8. Guardar archivo firmado
            try {
                UtilsFile.storeDocumentInDisk(signedXmlDocument, documentName, "xml", attachmentPath);
                logger.info("Archivo firmado guardado exitosamente en: " + attachmentPath);
            } catch (IOException e) {
                logger.error("Error al guardar el archivo: " + e.getMessage(), e);
                transactionResponse.setMensaje("Error al guardar el archivo firmado.");
                return transactionResponse;
            }

            // 9. Comprimir y codificar en Base64
            byte[] zipBytes = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml");
            if (zipBytes == null) {
                transactionResponse.setMensaje("Error al comprimir el documento.");
                return transactionResponse;
            }
            String base64Content = convertToBase64(zipBytes);

            // 10. Configurar solicitud SOAP
            FileRequestDTO soapRequest = new FileRequestDTO();
            String urlClient = applicationProperties.obtenerUrl(client.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
            soapRequest.setService(urlClient);
            soapRequest.setUsername(configuracion.getUsuarioSol());
            soapRequest.setPassword(configuracion.getClaveSol());
            soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(documentName));
            soapRequest.setContentFile(base64Content);

            // 11. Enviar a SUNAT y obtener ticket
            log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
            Mono<FileResponseDTO> fileResponseDTOMono = documentBajaService.processBajaRequest(soapRequest.getService(), soapRequest);
            FileResponseDTO fileResponseDTO = fileResponseDTOMono.block();
            log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

            if (fileResponseDTO == null || fileResponseDTO.getTicket() == null) {
                transactionResponse.setMensaje("No se recibió ticket de SUNAT.");
                return transactionResponse;
            }

            String ticket = fileResponseDTO.getTicket();
            try {
                updateTicketBajaIfNull(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id(), ticket, transaction.getDOC_Id()).block();
            } catch (Exception e) {
                logger.error("Error al actualizar el ticket: " + e.getMessage(), e);
            }



            // 12. Consultar estado del ticket
            soapRequest.setTicket(ticket);
            Mono<FileResponseDTO> fileResponseDTOMono2 = Mono.defer(() ->
                            Mono.delay(Duration.ofSeconds(5)) // Delay inicial
                                    .flatMap(t -> documentBajaQueryService.processAndSaveFile(soapRequest.getService(), soapRequest))
                                    .flatMap(response -> {
                                        if (!response.getMessage().contains("98")) {
                                            //System.out.println("reintento ahora if" + new Date());
                                            //System.out.println("Response es : " + response);
                                            return Mono.just(response); // Ya no es 98, terminamos
                                        } else {
                                            //System.out.println("reintento ahora else" + new Date());
                                            return Mono.error(new IllegalStateException("SUNAT  aún está procesando (código 98)"));
                                        }
                                    })
                    )
                    .retryWhen(
                            Retry.fixedDelay(5, Duration.ofSeconds(5)) // 4 reintentos adicionales = 5 intentos en total
                                    .filter(ex -> ex.getMessage().contains("98"))
                                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                            new RuntimeException("SUNAT no procesó el ticket después de varios intentos."))
                    );

            FileResponseDTO fileResponseDTO2 = fileResponseDTOMono2.block();

            if (fileResponseDTO2 == null) {
                transactionResponse.setMensaje("Error al consultar el estado del ticket.");
                return transactionResponse;
            }

            cdrStatusResponse.setContent(fileResponseDTO2.getContent());
            cdrStatusResponse.setStatusMessage(fileResponseDTO2.getMessage());

            // 13. Procesar respuesta final
            if(cdrStatusResponse.getContent()!=null) {
                transactionResponse = processOseResponseBAJA(cdrStatusResponse.getContent(), transaction, documentName, configuracion);
            } else {
                transactionResponse.setMensaje(cdrStatusResponse.getStatusMessage());
            }

            transactionResponse.setIdentificador(documentName);
            transactionResponse.setTicketRest(ticket);

        } catch (Exception e) {
            logger.error("Error en transactionVoidedDocument: " + e.getMessage(), e);
            transactionResponse.setMensaje("Ocurrió un error en el proceso de anulación.");
        }

        log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
        log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        log.setResponse((JsonUtils.toJson(transactionResponse.getSunat())).equals("null") ? transactionResponse.getMensaje() : (JsonUtils.toJson(transactionResponse.getSunat())));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        transactionResponse.setLogDTO(log);
        log.setPathBase(attachmentPath + "\\" + documentName + ".json");

        return transactionResponse;
    }

    // Método para convertir los bytes a base64
    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
    }
    private boolean isCodigo98(FileResponseDTO response) {
        if (response == null || response.getMessage() == null) return false;
        return response.getMessage().toString().contains("código") &&
                response.getMessage().toString().contains("98");
    }

    public Mono<TransaccionBaja> updateTicketBajaIfNull(String rucEmpresa, String serie, String newTicketBaja, String docId) {
        return iTransaccionBajaRepository.findFirstByRucEmpresaAndSerie(rucEmpresa, serie)
                .filter(transaccionBaja -> (transaccionBaja.getTicketBaja() == null || transaccionBaja.getTicketBaja().isEmpty())) // Verifica si ticketBaja es null
                .flatMap(transaccionBaja -> {
                    transaccionBaja.setTicketBaja(newTicketBaja);
                    transaccionBaja.setDocId(docId);// Actualiza el campo ticketBaja
                    return iTransaccionBajaRepository.save(transaccionBaja); // Guarda el documento actualizado
                });
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
                mensaje = mensaje.replace("El Resumen diario", "El Resumen de Boletas");
            }

            transactionResponse.setMensaje(mensaje);
            transactionResponse.setZip(statusResponse);

        } else {
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);
        }

        return transactionResponse;
    }

    private ConfigData createConfigData(Client client) {
        /*String valor = transaction.getTransaccionContractdocrefList().stream()
                .filter(x -> x.getUsuariocampos().getNombre().equals("pdfadicional"))
                .map(x -> x.getValor())
                .findFirst()
                .orElse("No");*/
        return ConfigData.builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDocAnexos())
                .build();
    }


    public String generarIDyFecha(TransacctionDTO tr) {
        String serie = "";
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
            TransaccionBaja trb = trbb.block();

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
                trb.setTicketBaja(tr.getTicket_Baja());
            } else {
                // Crear el primer registro para la empresa
                serie = Utils.construirSerie(prefijo, fechaActual, "00001");
                trb = crearNuevoRegistro(tr.getDocIdentidad_Nro(), 0, fechaActual, serie);
            }

            tr.setANTICIPO_Id(serie);
            trb.setDocId(tr.getDOC_Id());
            trb = iTransaccionBajaRepository.save(trb).block();
            //iTransaccionBajaRepository.save(trb);

        } catch (Exception ex) {
            // Manejo de excepciones
            System.err.println(ex.getMessage());
        }
        return serie;
    }

    private String generarNuevoId(String serie) {
        int indexOf = serie.lastIndexOf("-");
        String fin = serie.substring(indexOf + 1);
        int numero = Integer.parseInt(fin);
        numero++;
        return String.format("%05d", numero);
    }

    private TransaccionBaja crearNuevoRegistro(String rucEmpresa, Integer idd, String fecha, String serie) {
        TransaccionBaja nuevaBaja = new TransaccionBaja();
        nuevaBaja.setRucEmpresa(rucEmpresa);
        nuevaBaja.setFecha(fecha);
        nuevaBaja.setIdd(++idd);
        nuevaBaja.setSerie(serie);
        return nuevaBaja;
    }

}
