package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.config.ProviderProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.Utils;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Service
public class CloudService implements CloudInterface {

    Logger logger = LoggerFactory.getLogger(CloudService.class);



    @Autowired
    ProviderProperties providerProperties;

    @Autowired
    PublicacionManager publicacionManager;

    @Autowired
    GuiaInterface iServiceEmisionGuia;

    @Autowired
    IServiceBaja iServiceBaja;

    @Autowired
    IServiceEmision iServiceEmision;

    @Autowired
    ITransaccionBajaRepository iTransaccionBajaRepository;

    @Override
    public ResponseEntity<RequestPost> proccessDocument(String stringRequestOnpremise) {
        TransacctionDTO[] transacctionDTO = null;
        RequestPost responseProcesor = null;
        String datePattern = "(\"\\w+\":\\s*\"\\d{4}-\\d{2}-\\d{2}) \\d{2}:\\d{2}:\\d{2}\\.\\d\"";
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");

        try {
            Gson gson = new Gson();
            transacctionDTO = gson.fromJson(updatedJson, TransacctionDTO[].class);
            responseProcesor = procesarTransaccion(transacctionDTO[0]);

            System.out.println("*******************************************************************************************************************************************************************************");
        } catch (Exception e) {
            logger.info("SE GENERO UN ERROR : " + e.getMessage());
        }
        return ResponseEntity.ok(responseProcesor);
    }


    /**
     * @return la lista de trnsacciones pendientes de envio. Es decir las que
     * tengan el estado [N]uevo,[C]orregido,[E]nviado
     */

    /*public RequestPost procesarTransaccion(TransacctionDTO transaccion, String stringRequestOnpremise) throws Exception {
        RequestPost request = new RequestPost();

        //request.getLogMdb().setRequest(new Gson().toJson(transaccion));
        logger.info("Documento extraido de la intermedia es : " + transaccion.getDocIdentidad_Nro() + " - " + transaccion.getDOC_Id());
        try {

            TransaccionRespuesta tr = EnviarTransaccion(transaccion);
            OnPremiseImpl clientHanaService = new OnPremiseImpl();
            request = generateDataRequestHana(transaccion, tr);
            //anexarDocumentos(request);
            logger.info("Ruc: " + request.getRuc() + " DocObject: " + request.getDocObject() + " DocEntry: " + request.getDocEntry());
            logger.info("Nombre Documento: " + request.getDocumentName());

            if (request.getResponseRequest() != null && request.getResponseRequest().getServiceResponse() != null) {
                logger.info("Mensaje Documento: " + request.getResponseRequest().getServiceResponse());
            }

            //logEntryService.saveLogEntryToMongoDB(convertToEntity(tr.getLogDTO())).subscribe();

            logger.info("Se realizo de manera exitosa la actualizacion del documento :" + transaccion.getFE_Id());
            logger.info("Se anexo de manera correcta los documentos en SAP");
            logger.info("===============================================================================");
        } catch (VenturaExcepcion e) {
            throw new RuntimeException(e);
        }
        return request;
    }*/

    public int anexarDocumentos(RequestPost request) {
        HttpResponse<String> response = null;
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(request);
            response = Unirest.post(request.getUrlOnpremise() + "anexar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();
            logger.info("Se envió de manera correcta al servidor OnPremise los documentos");
        } catch (Exception e) {
            logger.error("Error conexion con servidor destino: " + e.getMessage());
        }
        logger.info("La ruta del servidor donde se esta dejando los documentos es : " + request.getUrlOnpremise());
        return response.getStatus();
    }

    public RequestPost procesarTransaccion(TransacctionDTO transaccion) throws Exception {
        System.out.println("*******************************************************************************************************************************************************************************");
        logger.info("Ruc: " + transaccion.getDocIdentidad_Nro() + " DocObject: " + transaccion.getFE_ObjectType() + " DocEntry: " + transaccion.getFE_DocEntry());

        TransaccionRespuesta tr = enviarTransaccion(transaccion);
        RequestPost request = new RequestPost();
        request = generateDataRequestHana(transaccion, tr);
        return request;
    }


    public TransaccionRespuesta enviarTransaccion(TransacctionDTO transaction) throws Exception {
        if (transaction == null) {
            return new TransaccionRespuesta();  // Retorna una respuesta vacía si la transacción es nula
        }

        String tipoTransaccion = transaction.getFE_TipoTrans();
        String codigoDocumento = transaction.getDOC_Codigo();

        switch (tipoTransaccion.toUpperCase()) {
            case ISunatConnectorConfig.FE_TIPO_TRANS_EMISION:
                if (IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE.equalsIgnoreCase(codigoDocumento)) {
                    return iServiceEmisionGuia.transactionRemissionGuideDocumentRest(transaction, IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                }
                return iServiceEmision.transactionDocument(transaction, codigoDocumento);

            case ISunatConnectorConfig.FE_TIPO_TRANS_BAJA:
                return iServiceBaja.transactionVoidedDocument(transaction, codigoDocumento);

            default:
                return new TransaccionRespuesta();
        }
    }


    public RequestPost generateDataRequestHana(TransacctionDTO tc, TransaccionRespuesta tr) {
        RequestPost request = new RequestPost();
        try {

            request.setRuc(tc.getDocIdentidad_Nro());
            request.setDocType(tc.getDOC_Codigo());
            request.setDocEntry(tc.getFE_DocEntry().toString());
            request.setDocObject(tc.getFE_ObjectType());
            request.setDocumentName(tr.getIdentificador());
            request.setTicketBaja(tr.getTicketRest());


            request.setDbName(tc.getDbName());
            request.setUrlOnpremise(providerProperties.getUrlOnpremise(tc.getDocIdentidad_Nro()));

            if (tr.getMensaje().contains("ha sido aceptad") || tr.getMensaje().contains("aprobado")) {

                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                if (tr.getMensaje().contains("Baja")) {
                    Data.ResponseDocument document4 = new Data.ResponseDocument("zip", tr.getZip());
                    listMapDocuments.put("cdr_baja", document4);
                } else {
                    Data.ResponseDocument document1 = new Data.ResponseDocument("pdf", tr.getPdf());
                    Data.ResponseDocument document2 = new Data.ResponseDocument("xml", tr.getXml());
                    Data.ResponseDocument document3 = new Data.ResponseDocument("zip", tr.getZip());

                    listMapDocuments.put("pdf", document1);
                    listMapDocuments.put("xml", document2);
                    listMapDocuments.put("zip", document3);
                    if (tr.getPdfBorrador() != null) {
                        Data.ResponseDocument document4 = new Data.ResponseDocument("pdf", tr.getPdfBorrador());
                        listMapDocuments.put("pdf_borrador", document4);
                    }
                }

                request.setDigestValue(tr.getDigestValue());
                request.setBarcodeValue(tr.getBarcodeValue());

                Data.ResponseRequest responseRequest = new Data.ResponseRequest();
                responseRequest.setServiceResponse(tr.getMensaje());
                responseRequest.setListMapDocuments(listMapDocuments);
                request.setResponseRequest(responseRequest);
                request.setStatus(200);
                publicacionManager.publicarDocumento(tc, tc.getFE_Id(), tr);

            } else {
                if (tr.getPdfBorrador() != null) {
                    Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                    Data.ResponseDocument document1 = new Data.ResponseDocument("pdf", tr.getPdfBorrador());
                    listMapDocuments.put("pdf_borrador", document1);
                    Data.ResponseRequest responseRequest = new Data.ResponseRequest();
                    responseRequest.setServiceResponse(tr.getMensaje());
                    responseRequest.setListMapDocuments(listMapDocuments);
                    request.setResponseRequest(responseRequest);
                }

                Data.Error error = new Data.Error();
                Data.ErrorRequest errorRequest = new Data.ErrorRequest();
                errorRequest.setCode("500");
                errorRequest.setDescription(tr.getMensaje());
                error.setErrorRequest(errorRequest);
                request.setResponseError(error);
                request.setStatus(500);
            }
        } catch (Exception e) {
            logger.error("Error : " + e.getMessage());
        }
        return request;
    }




}
