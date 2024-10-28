package service.cloud.request.clientRequest.service.emision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.eclipse.persistence.internal.oxm.ByteArrayDataSource;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.dto.finalClass.Response;
import service.cloud.request.clientRequest.exception.ExceptionProxy;
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
import service.cloud.request.clientRequest.proxy.ose.IOSEClient;
import service.cloud.request.clientRequest.proxy.sunat.ISUNATClient;
import service.cloud.request.clientRequest.proxy.sunat.consulta.ISUNATClientConsult;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.*;
import service.cloud.request.clientRequest.utils.exception.ConfigurationException;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.exception.SignerDocumentException;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.xmlFormatSunat.uncefact.data.specification.corecomponenttypeschemamodule._2.TextType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.applicationresponse_2.ApplicationResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.DocumentResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.ResponseType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonaggregatecomponents_2.StatusType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.DescriptionType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.ResponseCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonCodeType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.commonbasiccomponents_2.StatusReasonType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.summarydocuments_1.SummaryDocumentsType;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
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
    DocumentFormatInterface documentFormatInterface;

    @Autowired
    IOSEClient ioseClient;

    @Autowired
    ISUNATClient isunatClient;

    @Autowired
    ISUNATClientConsult isunatClientConsult;


    @Autowired
    ITransaccionBajaRepository iTransaccionBajaRepository;


    @Override
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception {


        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getSN_RazonSocial());


        UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
        String attachmentPath = getAttachmentPath(transaction, doctype);
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);


        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        ConfigData configuracion = createConfigData(client);
        CdrStatusResponse cdrStatusResponse = null;

        String documentName = "";
        try {

            if (transaction.getFE_Estado().equals("C")/*!Utils.isNullOrTrimmedEmpty(transaction.getTicket_Baja())*/) {
                //retornar mensaje error si ticket viene null o vacio
                if (configuracion.getIntegracionWs().equals("OSE")) {
                    cdrStatusResponse = ioseClient.getStatus(transaction.getDocIdentidad_Nro(), transaction.getTicket_Baja());
                    System.out.println(Arrays.toString(cdrStatusResponse.getContent()));
                } else {
                    cdrStatusResponse = isunatClientConsult.getStatus(transaction.getDocIdentidad_Nro(), transaction.getTicket_Baja());
                    System.out.println(Arrays.toString(cdrStatusResponse.getContent()));
                }
                documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
                TransaccionRespuesta transaccionRespuesta = processOseResponseBAJA(cdrStatusResponse.getContent(), transaction, fileHandler,documentName, configuracion);
            } else {
                transaction.setANTICIPO_Id(generarIDyFecha(transaction));
                if (transaction.getFE_Comentario().isEmpty()) {

                    transactionResponse.setMensaje("Ingresar razón de anulación, y colocar APROBADO y volver a consultar");
                    return transactionResponse;
                }

                byte[] certificado = loadCertificate(client, transaction.getDocIdentidad_Nro());
                validateCertificate(certificado, client);



                String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
                ValidationHandler validationHandler = ValidationHandler.newInstance(this.docUUID);

                SignerHandler signerHandler = SignerHandler.newInstance();
                signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), client.getCertificadoTipoKeystore(), client.getCertificadoProveedor(), signerName);

                VoidedDocumentsType voidedDocumentType = ublHandler.generateVoidedDocumentType(transaction, signerName);
                validationHandler.checkBasicInformation2(transaction.getANTICIPO_Id(), transaction.getDocIdentidad_Nro(), transaction.getDOC_FechaEmision());
                byte[] xmlDocument = convertDocumentToBytes(voidedDocumentType);
                byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);

                documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
                DataHandler zipDocument = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml");


                if (null != zipDocument) {
                    LoggerTrans.getCDThreadLogger().log(Level.INFO, "[" + this.docUUID + "] Enviando WS sendSummary.");


                    if (configuracion.getIntegracionWs().equals("OSE")) {
                        String ticket = ioseClient.sendSummary(transaction.getDocIdentidad_Nro(), DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
                        TransaccionBaja trb = updateTicketBajaIfNull(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id(), ticket, transaction.getDOC_Id()).block();
                        Thread.sleep(1000);
                        cdrStatusResponse = ioseClient.getStatus(transaction.getDocIdentidad_Nro(), ticket);
                        System.out.println(Arrays.toString(cdrStatusResponse.getContent()));
                    } else {
                        String ticket = isunatClient.sendSummary(transaction.getDocIdentidad_Nro(), DocumentNameHandler.getInstance().getZipName(documentName), zipDocument);
                        TransaccionBaja trb = updateTicketBajaIfNull(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id(), ticket, transaction.getDOC_Id()).block();
                        Thread.sleep(1000);
                        cdrStatusResponse = isunatClientConsult.getStatus(transaction.getDocIdentidad_Nro(), ticket);
                        System.out.println(Arrays.toString(cdrStatusResponse.getContent()));
                    }

                    documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
                    TransaccionRespuesta transaccionRespuesta = processOseResponseBAJA(cdrStatusResponse.getContent(), transaction, fileHandler,documentName, configuracion);


                }
            }
        } catch (SOAPFaultException e) {

            if (client.getIntegracionWs().equals("SUNAT")) {
                String sErrorCodeSUNAT = e.getFault().getFaultCode() + " - " + e.getFault().getFaultString();
                cdrStatusResponse.setStatusMessage(sErrorCodeSUNAT);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            ExceptionProxy exceptionProxy = objectMapper.readValue(e.getFault().getDetail().getTextContent(), ExceptionProxy.class);
            Response.builder().errorCode(e.getMessage()).errorMessage(exceptionProxy.getDescripcion() + " " + exceptionProxy.getDescripcionAdicional().getDescripcion()).build();
            //cdrStatusResponse.setStatusMessage(exceptionProxy.getDescripcion());
        }

        log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        log.setResponse(new Gson().toJson(transactionResponse.getSunat()));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        log.setResponse(new Gson().toJson(transactionResponse.getMensaje()));
        return transactionResponse;
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

    private DataHandler compressUBLDocumentv2(byte[] document, String documentName) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("+compressUBLDocument() [" + this.docUUID + "]");
        }
        DataHandler zipDocument = null;
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(document)) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                    byte[] array = new byte[10000];
                    int read = 0;
                    zos.putNextEntry(new ZipEntry(documentName));
                    while ((read = bis.read(array, 0, array.length)) != -1) {
                        zos.write(array, 0, read);
                    }
                    zos.closeEntry();
                }
                /* Retornando el objeto DATAHANDLER */
                zipDocument = new DataHandler(new ByteArrayDataSource(bos.toByteArray(), "application/zip"));
                if (logger.isDebugEnabled()) {
                    logger.debug("compressUBLDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP correctamente.");
                }
            }
        } catch (Exception e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("-compressUBLDocument() [" + this.docUUID + "]");
        }
        return zipDocument;
    }

    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, FileHandler fileHandler, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            /**se realiza el anexo del documento de baja*/
            if (null != statusResponse && 0 < statusResponse.length) {
                fileHandler.storePDFDocumentInDisk(statusResponse, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
            }

            LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] El documento [{1}] fue APROBADO por SUNAT.", new Object[]{this.docUUID, documentName});
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);

        } else {
            LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] El documento [{1}] fue RECHAZADO por SUNAT.", new Object[]{this.docUUID, documentName});
            transactionResponse.setMensaje(sunatResponse.getMensaje());
            transactionResponse.setZip(statusResponse);
        }

        return transactionResponse;
    }

    public TransaccionRespuesta.Sunat proccessResponse(byte[] cdrConstancy, TransacctionDTO transaction, String
            sunatType) {
        try {
            String descripcionRespuesta = "";
            Optional<byte[]> unzipedResponse = documentFormatInterface.unzipResponse(cdrConstancy);
            int codigoObservacion = 0;
            int codigoRespuesta = 0;
            String identificador = Constants.IDENTIFICATORID_OSE;
            if (unzipedResponse.isPresent()) {
                StringBuilder descripcion = new StringBuilder();
                JAXBContext jaxbContext = JAXBContext.newInstance(ApplicationResponseType.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                JAXBElement<ApplicationResponseType> jaxbElement = unmarshaller.unmarshal(new ByteArraySource(unzipedResponse.get()), ApplicationResponseType.class);
                ApplicationResponseType applicationResponse = jaxbElement.getValue();
                List<DocumentResponseType> documentResponse = applicationResponse.getDocumentResponse();
                List<TransaccionRespuesta.Observacion> observaciones = new ArrayList<>();
                for (DocumentResponseType documentResponseType : documentResponse) {
                    ResponseType response = documentResponseType.getResponse();
                    ResponseCodeType responseCode = response.getResponseCode();
                    codigoRespuesta = Optional.ofNullable(responseCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                    List<DescriptionType> descriptions = response.getDescription();
                    for (DescriptionType description : descriptions) {
                        descripcion.append(description.getValue());
                    }
                    if (sunatType.equalsIgnoreCase(Constants.IDENTIFICATORID_OSE)) { //cambio aqui NUMA
                        identificador = documentResponseType.getDocumentReference().getID().getValue();
                    } else {
                        identificador = documentResponseType.getResponse().getReferenceID().getValue();
                    }
                    List<StatusType> statusTypes = response.getStatus();
                    for (StatusType statusType : statusTypes) {
                        List<StatusReasonType> statusReason = statusType.getStatusReason();
                        String mensajes = statusReason.parallelStream().map(TextType::getValue).collect(Collectors.joining("\n"));
                        StatusReasonCodeType statusReasonCode = statusType.getStatusReasonCode();
                        codigoObservacion = Optional.ofNullable(statusReasonCode.getValue()).map(s -> s.isEmpty() ? null : s).map(Integer::parseInt).orElse(0);
                        TransaccionRespuesta.Observacion observacion = new TransaccionRespuesta.Observacion();
                        observacion.setCodObservacion(codigoObservacion);
                        observacion.setMsjObservacion(mensajes);
                        observaciones.add(observacion);
                    }
                }
                descripcionRespuesta = descripcion.toString();
                logger.info(descripcionRespuesta);
                TransaccionRespuesta.Sunat sunatResponse = new TransaccionRespuesta.Sunat();
                sunatResponse.setListaObs(observaciones);
                sunatResponse.setId(identificador);
                sunatResponse.setCodigo(codigoRespuesta);
                sunatResponse.setMensaje(descripcionRespuesta);
                sunatResponse.setEmisor(transaction.getDocIdentidad_Nro());
                return sunatResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new TransaccionRespuesta.Sunat();
    }

    ////////////////////////
    private String getAttachmentPath(TransacctionDTO transaction, String doctype) {
        Calendar fecha = Calendar.getInstance();
        fecha.setTime(transaction.getDOC_FechaEmision());
        int anio = fecha.get(Calendar.YEAR);
        int mes = fecha.get(Calendar.MONTH) + 1;
        int dia = fecha.get(Calendar.DAY_OF_MONTH);

        return applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + "anexo" + File.separator + anio + File.separator + mes + File.separator + dia + File.separator + transaction.getSN_DocIdentidad_Nro() + File.separator + doctype;
    }

    private byte[] loadCertificate(Client client, String docIdentidadNuumero) throws ConfigurationException, FileNotFoundException {
        String certificatePath = applicationProperties.getRutaBaseDoc() + docIdentidadNuumero + File.separator + client.getCertificadoName();
        return CertificateUtils.getCertificateInBytes(certificatePath);
    }

    private void validateCertificate(byte[] certificate, Client client) throws SignerDocumentException {
        CertificateUtils.checkDigitalCertificateV2(certificate, client.getCertificadoPassword(), client.getCertificadoProveedor(), client.getCertificadoTipoKeystore());
    }

    // Método genérico para convertir cualquier tipo de documento a bytes
    private <T> byte[] convertDocumentToBytes(T document) {
        return convertToBytes(document, (Class<T>) document.getClass());
    }

    // Método privado para realizar la conversión a bytes
    private <T> byte[] convertToBytes(T document, Class<T> documentClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(documentClass);
            Marshaller marshaller = jaxbContext.createMarshaller();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(document, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir " + documentClass.getSimpleName() + " a byte[]", e);
        }
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
                .mostrarSoap(client.getMostrarSoap())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDoc())
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
            TransaccionBaja trb = iTransaccionBajaRepository.findFirstByRucEmpresaOrderByFechaDescIddDesc(tr.getDocIdentidad_Nro()).block();
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

    private String dateFormat(Long fecha) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(fecha));
    }

    private String generarNuevoId(String serie) {
        int indexOf = serie.lastIndexOf("-");
        String fin = serie.substring(indexOf + 1);
        int numero = Integer.parseInt(fin);
        numero++;
        return String.format("%05d", numero);
    }

    private void actualizarRegistro(TransaccionBaja trb, String fecha, String nuevoId) {
        trb.setFecha(fecha);
        trb.setId(nuevoId);
        trb.setSerie(construirSerie(fecha, nuevoId));
        iTransaccionBajaRepository.save(trb);
    }

    private TransaccionBaja crearNuevoRegistro(String rucEmpresa, Integer idd, String fecha, String serie) {
        TransaccionBaja nuevaBaja = new TransaccionBaja();
        nuevaBaja.setRucEmpresa(rucEmpresa);
        nuevaBaja.setFecha(fecha);
        nuevaBaja.setIdd(++idd);
        nuevaBaja.setSerie(serie);
        return nuevaBaja;//iTransaccionBajaRepository.save(nuevaBaja).block();
    }


    private String construirSerie(String fecha, String nuevoId) {
        return "RA-" + fecha + "-" + nuevoId;
    }

}
