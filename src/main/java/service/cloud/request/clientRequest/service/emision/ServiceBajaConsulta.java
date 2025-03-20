package service.cloud.request.clientRequest.service.emision;

import com.google.gson.Gson;
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
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.handler.document.DocumentNameHandler;
import service.cloud.request.clientRequest.model.Client;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.service.core.DocumentFormatInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.utils.SunatResponseUtils;
import service.cloud.request.clientRequest.utils.exception.DateUtils;
import service.cloud.request.clientRequest.utils.files.UtilsFile;
import service.cloud.request.clientRequest.utils.exception.error.IVenturaError;

import java.util.Date;

@Service
public class ServiceBajaConsulta implements IServiceBaja {

    Logger logger = LoggerFactory.getLogger(ServiceBajaConsulta.class);

    @Autowired
    ClientProperties clientProperties;

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    DocumentBajaQueryService documentBajaQueryService;

    @Override
    public TransaccionRespuesta transactionVoidedDocument(TransacctionDTO transaction, String doctype) {

        /***/
        LogDTO log = new LogDTO();
        log.setRequestDate(DateUtils.formatDateToString(new Date()));
        log.setRuc(transaction.getDocIdentidad_Nro());
        log.setBusinessName(transaction.getRazonSocial());
        /***/

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();

        String attachmentPath = UtilsFile.getAttachmentPath(transaction, doctype, applicationProperties.getRutaBaseDoc());

        Client client = clientProperties.listaClientesOf(transaction.getDocIdentidad_Nro());
        ConfigData configuracion = createConfigData(client);

        String documentName = "";
        try {

            FileRequestDTO soapRequest = new FileRequestDTO();
            String urlClient = applicationProperties.obtenerUrl(client.getIntegracionWs(), transaction.getFE_Estado(), transaction.getFE_TipoTrans(), transaction.getDOC_Codigo());
            soapRequest.setService(urlClient);
            soapRequest.setUsername(configuracion.getUsuarioSol());
            soapRequest.setPassword(configuracion.getClaveSol());
            soapRequest.setTicket(transaction.getTicket_Baja());

            log.setThirdPartyServiceInvocationDate(DateUtils.formatDateToString(new Date()));
            Mono<FileResponseDTO> fileResponseDTOMono = documentBajaQueryService.processAndSaveFile(soapRequest.getService(), soapRequest);
            FileResponseDTO fileRequestDTO = fileResponseDTOMono.block();
            log.setThirdPartyServiceResponseDate(DateUtils.formatDateToString(new Date()));

            documentName = DocumentNameHandler.getInstance().getVoidedDocumentName(transaction.getDocIdentidad_Nro(), transaction.getDOC_Id());
            transactionResponse = processOseResponseBAJA(fileRequestDTO.getContent(), transaction, attachmentPath, documentName, configuracion);

        } catch (Exception e) {
            logger.error("El error capturado es : " + e.getMessage());
        }

        log.setPathThirdPartyRequestXml(attachmentPath + "\\" + documentName + ".xml");
        log.setPathThirdPartyResponseXml(attachmentPath + "\\" + documentName + ".zip");
        log.setObjectTypeAndDocEntry(transaction.getFE_ObjectType() + " - " + transaction.getFE_DocEntry());
        log.setSeriesAndCorrelative(documentName);
        log.setResponse((new Gson().toJson(transactionResponse.getSunat())).equals("null") ? transactionResponse.getMensaje() : (new Gson().toJson(transactionResponse.getSunat())));
        log.setResponseDate(DateUtils.formatDateToString(new Date()));
        transactionResponse.setLogDTO(log);
        log.setPathBase(attachmentPath + "\\" + documentName + ".json");

        return transactionResponse;
    }

    private TransaccionRespuesta processOseResponseBAJA(byte[] statusResponse, TransacctionDTO transaction, String baseDirectory, String documentName, ConfigData configuracion) {
        TransaccionRespuesta.Sunat sunatResponse = SunatResponseUtils.proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());//proccessResponse(statusResponse, transaction, configuracion.getIntegracionWs());

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        if ((IVenturaError.ERROR_0.getId() == sunatResponse.getCodigo()) || (4000 <= sunatResponse.getCodigo())) {

            if (null != statusResponse && 0 < statusResponse.length) {
                UtilsFile.storePDFDocumentInDisk(statusResponse, baseDirectory, documentName + "_SUNAT_CDR_BAJA", ISunatConnectorConfig.EE_ZIP);
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
}
