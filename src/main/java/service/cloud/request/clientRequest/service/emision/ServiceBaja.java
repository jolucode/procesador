package service.cloud.request.clientRequest.service.emision;

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
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;
import service.cloud.request.clientRequest.proxy.model.CdrStatusResponse;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.*;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;
import service.cloud.request.clientRequest.utils.files.CertificateUtils;
import service.cloud.request.clientRequest.utils.files.DocumentConverterUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.xmlFormatSunat.xsd.voideddocuments_1.VoidedDocumentsType;

import javax.activation.DataHandler;
import java.io.*;
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

        transaction.setANTICIPO_Id(generarIDyFecha(transaction));


        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDoc());
        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        fileHandler.setBaseDirectory(attachmentPath);


        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        ConfigData configuracion = createConfigData(client);
        CdrStatusResponse cdrStatusResponse = new CdrStatusResponse(); //null;


        try {

            if (transaction.getFE_Comentario().isEmpty()) {
                transactionResponse.setMensaje("Ingresar razón de anulación, y colocar APROBADO y volver a consultar");
                return transactionResponse;
            }

            String certificatePath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + client.getCertificadoName();

            byte[] certificado = CertificateUtils.loadCertificate(certificatePath);
            CertificateUtils .validateCertificate(certificado,client.getCertificadoPassword(), client.getCertificadoProveedor(), client.getCertificadoTipoKeystore());

            String signerName = ISignerConfig.SIGNER_PREFIX + transaction.getDocIdentidad_Nro();
            SignerHandler signerHandler = SignerHandler.newInstance();
            signerHandler.setConfiguration(certificado, client.getCertificadoPassword(), client.getCertificadoTipoKeystore(), client.getCertificadoProveedor(), signerName);

            UBLDocumentHandler ublHandler = UBLDocumentHandler.newInstance(this.docUUID);
            VoidedDocumentsType voidedDocumentType = ublHandler.generateVoidedDocumentType(transaction, signerName);

            byte[] xmlDocument = DocumentConverterUtils.convertDocumentToBytes(voidedDocumentType);
            byte[] signedXmlDocument = signerHandler.signDocumentv2(xmlDocument, docUUID);
            String documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());
            byte[] zipBytes = compressUBLDocumentv2(signedXmlDocument, documentName + ".xml");
            String base64Content = convertToBase64(zipBytes);

            FileRequestDTO soapRequest = new FileRequestDTO();
            soapRequest.setService("https://proy.ose.tci.net.pe/ol-ti-itcpe-2/ws/billService?wsdl");
            soapRequest.setUsername(configuracion.getUsuarioSol());
            soapRequest.setPassword(configuracion.getClaveSol());
            soapRequest.setFileName(DocumentNameHandler.getInstance().getZipName(documentName));
            soapRequest.setContentFile(base64Content);

            if (null != zipBytes) {

                Mono<FileResponseDTO> fileResponseDTOMono = documentBajaService.processBajaRequest(soapRequest.getService(), soapRequest);
                String ticket = fileResponseDTOMono.block().getTicket();
                TransaccionBaja trb = updateTicketBajaIfNull(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id(), ticket, transaction.getDOC_Id()).block();
                Thread.sleep(1000);

                soapRequest.setTicket(ticket);
                Mono<FileResponseDTO> fileResponseDTOMono2 = documentBajaQueryService.processAndSaveFile(soapRequest.getService(), soapRequest);

                FileResponseDTO fileResponseDTO = fileResponseDTOMono2.block();
                cdrStatusResponse.setContent(fileResponseDTO.getContent());
                cdrStatusResponse.setStatusMessage(fileResponseDTO.getMessage());

                documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
                transactionResponse = processOseResponseBAJA(cdrStatusResponse.getContent(), transaction, documentName, configuracion);
                transactionResponse.setTicketRest(ticket);
            }
        } catch (Exception e) {
            logger.error("El error capturado es : " + e.getMessage());
        }
        return transactionResponse;
    }

    // Método para extraer los bytes de DataHandler
    private byte[] extractBytesFromDataHandler(DataHandler dataHandler) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = dataHandler.getInputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    // Método para convertir los bytes a base64
    private String convertToBase64(byte[] content) {
        return Base64.getEncoder().encodeToString(content);
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
        if (logger.isDebugEnabled()) {
            logger.debug("+compressUBLDocument() [" + this.docUUID + "]");
        }

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
            zipDocument = bos.toByteArray();  // Devolver directamente los bytes comprimidos

            if (logger.isDebugEnabled()) {
                logger.debug("compressUBLDocument() [" + this.docUUID + "] El documento UBL fue convertido a formato ZIP correctamente.");
            }
        } catch (Exception e) {
            logger.error("compressUBLDocument() [" + this.docUUID + "] " + e.getMessage());
            throw new IOException(IVenturaError.ERROR_455.getMessage());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("-compressUBLDocument() [" + this.docUUID + "]");
        }
        return zipDocument; // Devuelve los bytes comprimidos directamente
    }


    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            /**se realiza el anexo del documento de baja*/
            if (null != statusResponse && 0 < statusResponse.length) {
                UtilsFile.storePDFDocumentInDisk(statusResponse, applicationProperties.getRutaBaseDoc(), documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);//fileHandler.storePDFDocumentInDisk(statusResponse, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
            }
            transactionResponse.setMensaje(sunatResponse.getMensaje());
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
