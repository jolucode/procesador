package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import service.cloud.request.clientRequest.config.ProviderProperties;
import service.cloud.request.clientRequest.dto.TransaccionRespuesta;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.mongo.model.Log;
import service.cloud.request.clientRequest.mongo.model.LogDTO;
import service.cloud.request.clientRequest.mongo.service.ILogService;
import service.cloud.request.clientRequest.service.emision.ServiceBaja;
import service.cloud.request.clientRequest.service.emision.ServiceBajaConsulta;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.Constants;


import java.util.*;

import static java.math.BigDecimal.valueOf;

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
    ServiceBaja iServiceBaja;

    @Autowired
    IServiceEmision iServiceEmision;

    @Autowired
    ServiceBajaConsulta serviceBajaConsulta;

    @Autowired
    private ILogService logEntryService;

    @Autowired
    @Qualifier("defaultMapper")
    private ModelMapper mapper;

    @Override
    public Mono<ResponseEntity<Object>>   proccessDocument(String stringRequestOnpremise) {
        String datePattern = Constants.PATTERN_ARRAY_TRANSACTION;
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");

        return Mono.fromCallable(() -> {
                    Gson gson = new Gson();
                    return gson.fromJson(updatedJson, TransacctionDTO[].class);
                })
                .flatMapMany(Flux::fromArray) // Convertir el array en un flujo
                .flatMap(transaccion ->
                                processTransaction(transaccion, stringRequestOnpremise) // Procesar cada transacción
                                        .subscribeOn(Schedulers.boundedElastic()) // Ejecución en hilos aptos para operaciones bloqueantes
                                        .doOnError(error -> logger.error("Error procesando transacción: {}", error.getMessage()))
                        , 100) // Paralelismo: procesa hasta 5 transacciones simultáneamente
                .onErrorContinue((error, obj) -> logger.warn("Continuando tras error: {} en transacción: {}", error.getMessage(), obj))
                .then() // Completar después de procesar todas las transacciones
                .map(ignored -> ResponseEntity.ok().build()) // Devolver ResponseEntity<Void>
                .onErrorResume(error -> {
                    logger.error("Error general al procesar documentos: {}", error.getMessage());
                    return Mono.just(ResponseEntity.<Void>status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

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

    private Mono<RequestPost> processTransaction(TransacctionDTO transaccion, String requestOnPremise) {
        return Mono.fromCallable(() -> {
            TransaccionRespuesta tr = enviarTransaccion(transaccion);
            RequestPost request = generateDataRequestHana(transaccion, tr);
            anexarDocumentos(request);

            logger.info("Ruc: " + request.getRuc() + " DocObject: " + request.getDocObject() + " DocEntry: " + request.getDocEntry());
            logger.info("Nombre Documento: " + request.getDocumentName());
            logger.info("Se realizo de manera exitosa la actualizacion del documento :" + transaccion.getFE_Id());
            logger.info("Se anexo de manera correcta los documentos en SAP");
            logger.info("===============================================================================");

            if (tr.getLogDTO() != null) {
                tr.getLogDTO().setRequest(requestOnPremise);
                logEntryService.saveLogEntryToMongoDB(convertToEntity(tr.getLogDTO())).subscribe();
            }

            return request;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Log convertToEntity(LogDTO dto) {
        return mapper.map(dto, Log.class);
    }

    public TransaccionRespuesta enviarTransaccion(TransacctionDTO transaction) throws Exception {
        if (transaction == null) {
            return new TransaccionRespuesta();
        }

        String tipoTransaccion = transaction.getFE_TipoTrans();
        String codigoDocumento = transaction.getDOC_Codigo();

        switch (tipoTransaccion.toUpperCase()) {
            case ISunatConnectorConfig.FE_TIPO_TRANS_EMISION:
                if (IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE.equalsIgnoreCase(codigoDocumento) || IUBLConfig.DOC_SENDER_CARRIER_GUIDE_CODE.equalsIgnoreCase(codigoDocumento)) {
                    return iServiceEmisionGuia.transactionRemissionGuideDocumentRest(transaction, IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                }

                return iServiceEmision.transactionDocument(transaction, codigoDocumento);
            case ISunatConnectorConfig.FE_TIPO_TRANS_BAJA:
                if (transaction.getFE_Estado().equals("C"))
                    return serviceBajaConsulta.transactionVoidedDocument(transaction, codigoDocumento);
                else return iServiceBaja.transactionVoidedDocument(transaction, codigoDocumento);
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


            request.setUrlOnpremise(providerProperties.getUrlOnpremise(tc.getDocIdentidad_Nro()));

            if (tr.getMensaje().contains("ha sido aceptad") || tr.getMensaje().contains("aprobado")) {
                logger.info(tr.getMensaje());
                tr.setPdf(tr.getPdfBorrador());
                tr.setEstado("V"); //Aprobado

                Map<String, Data.ResponseDocument> listMapDocuments = new HashMap<>();
                if (tr.getMensaje().contains("Baja") || tr.getMensaje().contains("El Resumen diario RC-")) {
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
                logger.warn(tr.getMensaje());
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
