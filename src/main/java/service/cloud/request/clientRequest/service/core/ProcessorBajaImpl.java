package service.cloud.request.clientRequest.service.core;

import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ApplicationProperties;
import service.cloud.request.clientRequest.config.ClientProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.finalClass.ConfigData;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.FileHandler;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.LoggerTrans;
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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class ProcessorBajaImpl implements ProcessorBajaInterface {


    private final Logger logger = Logger.getLogger(ProcessorBajaImpl.class);
    private final String docUUID = "123123";
    @Autowired
    ClientProperties clientProperties;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    DocumentFormatInterface documentFormatInterface;

    @Override
    public TransaccionRespuesta consultVoidedDocument(TransacctionDTO transaction, String doctype) throws Exception {

        TransaccionRespuesta transactionResponse = null;

        /** Extrayendo la informacion del archivo de configuracion 'config.xml' */
        //Configuracion configuration = ApplicationConfiguration.getInstance().getConfiguration();
        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());

        /**UTILIZANDO HASH MAP DE ENTIDADES*/
        //String idSociedad = transaction.getKeySociedad();
        //ListaSociedades sociedad = VariablesGlobales.MapSociedades.get(idSociedad);

        /**genrar nombre DOCUMENTO DE BAJA*/
        String documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getANTICIPO_Id());


        /**Setear la ruta del directorio*/

        FileHandler fileHandler = FileHandler.newInstance(this.docUUID);
        String attachmentPath = applicationProperties.getRutaBaseDoc() + transaction.getDocIdentidad_Nro() + File.separator + ISunatConnectorConfig.INVOICE_PATH;
        fileHandler.setBaseDirectory(attachmentPath);

        /**obtener XML almacenado en DISCO*/
        String sep = File.separator;
        String rutaXML = attachmentPath + sep + "XML" + sep + transaction.getDocIdentidad_Nro() + sep + transaction.getSN_DocIdentidad_Nro() + sep + documentName + ISunatConnectorConfig.EE_XML;
        logger.info("Ruta documento XML : " + rutaXML);

        Path path = Paths.get(rutaXML);
        byte[] xml = Files.readAllBytes(path);

        ConfigData configuracion = ConfigData
                .builder()
                .usuarioSol(client.getUsuarioSol())
                .claveSol(client.getClaveSol())
                .integracionWs(client.getIntegracionWs())
                .ambiente(applicationProperties.getAmbiente())
                .mostrarSoap(client.getMostrarSoap())
                .pdfBorrador(client.getPdfBorrador())
                .impresionPDF(client.getImpresion())
                .rutaBaseDoc(applicationProperties.getRutaBaseDoc())
                .build();

        /**Se crea un nuevo objeto WS Consumidor*/
        //WSConsumerConsult wsConsumer = WSConsumerConsult.newInstance(docUUID);
        //wsConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion);

        /**obtener ticket de base datos, para consultar cdr documento baja*/
        String ticket = transaction.getTicket_Baja();
        LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] El numero de ticket es: {1}", new Object[]{this.docUUID, ticket});

        /*if (configuracion.getAmbiente().equals("SUNAT")) {
            System.out.println("Es Sunat");
            WSConsumerGR wsConsumerGR = WSConsumerGR.newInstance(this.docUUID);
            wsConsumerGR.setConfiguration(transaction.getDocIdentidadNro(), configuration, fileHandler, documentName);
            StatusResponse statusResponse = wsConsumerGR.getStatus(ticket, documentName);
            return processCDRResponseV2(statusResponse, documentName, configuration, transaction);
        }*/

        if (configuracion.getIntegracionWs().equals("OSE")) {
            System.out.println("Es OSE");
            //WSConsumer oseConsumer = WSConsumer.newInstance(transaction.getFE_Id());
            //oseConsumer.setConfiguration(transaction.getDocIdentidad_Nro(), client.getUsuarioSol(), client.getClaveSol(), configuracion);

            /**envia ticket a sunat para consultar zip*/
            //StatusResponse response = oseConsumer.getStatus(ticket, configuracion);

            /**precesa mensaje, cdr, y xml */
            TransaccionRespuesta transaccionRespuesta = null ;//processOseResponseBAJA(response.getContent(), transaction, documentName, configuracion);

            if (Optional.ofNullable(transaccionRespuesta).isPresent()) {
                return transaccionRespuesta;
            }
        }
        return transactionResponse;
    }


    private TransaccionRespuesta processOseResponseBAJA(byte[]  statusResponse, TransacctionDTO transaction, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());
        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {
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
                System.out.println(descripcionRespuesta);
                logger.info("Respuesta servicio consumido : " + descripcionRespuesta);
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

}
