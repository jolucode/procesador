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
import service.cloud.request.clientRequest.dto.dto.TransactionImpuestosDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionLineasDTO;
import service.cloud.request.clientRequest.dto.dto.TransactionLineasImpuestoDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.dto.response.Data;
import service.cloud.request.clientRequest.extras.ISunatConnectorConfig;
import service.cloud.request.clientRequest.extras.IUBLConfig;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.mongo.repo.ITransaccionBajaRepository;
import service.cloud.request.clientRequest.mongo.service.ILogService;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceBaja;
import service.cloud.request.clientRequest.service.emision.interfac.IServiceEmision;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.Constants;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.Utils;


import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

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
    IServiceBaja iServiceBaja;

    @Autowired
    IServiceEmision iServiceEmision;

    @Autowired
    ITransaccionBajaRepository iTransaccionBajaRepository;

    @Autowired
    private ILogService logEntryService;

    @Override
    public ResponseEntity<RequestPost> proccessDocument(String stringRequestOnpremise) {
        TransacctionDTO[] transacctionDTO = null;
        RequestPost responseProcesor = null;
        String datePattern = "(\"\\w+\":\\s*\"\\d{4}-\\d{2}-\\d{2}) \\d{2}:\\d{2}:\\d{2}\\.\\d\"";
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");

        try {
            Gson gson = new Gson();
            transacctionDTO = gson.fromJson(updatedJson, TransacctionDTO[].class);
            responseProcesor = procesarTransaccion(insertarImpuestoBolsa(transacctionDTO[0]), stringRequestOnpremise);

            System.out.println("*******************************************************************************************************************************************************************************");
        } catch (Exception e) {
            logger.info("SE GENERO UN ERROR : " + e.getMessage());
        }
        return ResponseEntity.ok(responseProcesor);
    }

    private TransacctionDTO insertarImpuestoBolsa(TransacctionDTO transaccion) {

        transaccion.getTransactionLineasDTOList().forEach(linea -> {
            if (linea.getItmBolsa() == null || linea.getItmBolsa().isEmpty()) {
                linea.setItmBolsa("N"); // Asignar 'N' si itmBolsa es null o vacío
            }
        });

        String impuestoBolsa = "I", itemBolsa = "A";
        Optional<TransactionLineasDTO> lineasOptional = transaccion.getTransactionLineasDTOList().stream().filter(linea -> linea.getItmBolsa().equals(itemBolsa)).findAny();

        lineasOptional.ifPresent(lineaBolsa -> {
            Optional<TransactionLineasDTO> impuestoBolsaOptional = transaccion.getTransactionLineasDTOList().stream().filter(linea -> linea.getItmBolsa().equals(impuestoBolsa)).findAny();

            TransactionImpuestosDTO transaccionImpuesto = new TransactionImpuestosDTO();
            transaccionImpuesto.setAbreviatura(Constants.TAX_TOTAL_OTH_CODE);
            transaccionImpuesto.setMoneda(transaccion.getDOC_MON_Codigo());
            BigDecimal precioRefMonto = impuestoBolsaOptional.map(TransactionLineasDTO::getPrecioRef_Monto).orElseGet(lineaBolsa::getPrecioRef_Monto);
            BigDecimal totalBruto = impuestoBolsaOptional.map(TransactionLineasDTO::getTotalBruto).orElseGet(lineaBolsa::getTotalBruto);
            transaccionImpuesto.setMonto(precioRefMonto);
            transaccionImpuesto.setValorVenta(totalBruto);
            transaccionImpuesto.setPorcentaje(BigDecimal.valueOf(100));
            transaccionImpuesto.setTipoTributo(Constants.TAX_TOTAL_BPT_ID);
            transaccionImpuesto.setCodigo("C");
            transaccionImpuesto.setNombre(Constants.TAX_TOTAL_BPT_NAME);




            transaccion.getTransactionImpuestosDTOList().add(transaccionImpuesto);

            Optional<TransactionImpuestosDTO> impuestosOptional = transaccion.getTransactionImpuestosDTOList().stream().filter(impuestoTotal -> impuestoTotal.getNombre().isEmpty()).findAny();
            impuestosOptional.ifPresent(transaccion.getTransactionImpuestosDTOList()::remove);

            TransactionLineasImpuestoDTO transaccionLineaImpuesto = new TransactionLineasImpuestoDTO();
            transaccionLineaImpuesto.setAbreviatura(Constants.TAX_TOTAL_OTH_CODE);
            transaccionLineaImpuesto.setMoneda(transaccion.getDOC_MON_Codigo());
            transaccionLineaImpuesto.setMonto(precioRefMonto);
            transaccionLineaImpuesto.setValorVenta(totalBruto);
            transaccionLineaImpuesto.setPorcentaje(BigDecimal.valueOf(100));
            transaccionLineaImpuesto.setTipoTributo(Constants.TAX_TOTAL_BPT_ID);
            transaccionLineaImpuesto.setCodigo("C");
            transaccionLineaImpuesto.setNombre(Constants.TAX_TOTAL_BPT_NAME);

            // *** Aquí se añade la cantidad de la línea ***
            transaccionLineaImpuesto.setCantidad(lineaBolsa.getCantidad());
            transaccionLineaImpuesto.setUnidadSunat(lineaBolsa.getUnidadSunat());

            lineaBolsa.getTransactionLineasImpuestoListDTO().add(transaccionLineaImpuesto);
            impuestoBolsaOptional.ifPresent(transaccion.getTransactionLineasDTOList()::remove);
            Predicate<TransactionImpuestosDTO> predicate = impuesto -> impuesto.getPorcentaje().compareTo(valueOf(100)) == 0 && impuesto.getValorVenta().compareTo(totalBruto) == 0 && !Constants.TAX_TOTAL_BPT_NAME.equalsIgnoreCase(impuesto.getNombre());
            ArrayList<TransactionImpuestosDTO> transaccionImpuestos = new ArrayList<>(transaccion.getTransactionImpuestosDTOList());
            Optional<TransactionImpuestosDTO> optional = transaccionImpuestos.stream().filter(predicate).findAny();
            optional.ifPresent(transaccion.getTransactionImpuestosDTOList()::remove);
        });
        return transaccion;
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

    public RequestPost procesarTransaccion(TransacctionDTO transaccion, String requestOnPremise) throws Exception {
        System.out.println("*******************************************************************************************************************************************************************************");
        logger.info("Ruc: " + transaccion.getDocIdentidad_Nro() + " DocObject: " + transaccion.getFE_ObjectType() + " DocEntry: " + transaccion.getFE_DocEntry());

        TransaccionRespuesta tr = enviarTransaccion(transaccion);
        RequestPost request = new RequestPost();
        request = generateDataRequestHana(transaccion, tr);
        //anexarDocumentos(request);

        logger.info("Ruc: " + request.getRuc() + " DocObject: " + request.getDocObject() + " DocEntry: " + request.getDocEntry());
        logger.info("Nombre Documento: " + request.getDocumentName());

        if (request.getResponseRequest() != null && request.getResponseRequest().getServiceResponse() != null) {
            logger.info("Mensaje Documento: " + request.getResponseRequest().getServiceResponse());
        }

        if(tr.getLogDTO()!=null) {
            tr.getLogDTO().setRequest(requestOnPremise);
            //logEntryService.saveLogEntryToMongoDB(convertToEntity(tr.getLogDTO())).subscribe();
        }

        logger.info("Se realizo de manera exitosa la actualizacion del documento :" + transaccion.getFE_Id());
        logger.info("Se anexo de manera correcta los documentos en SAP");
        logger.info("===============================================================================");

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


            request.setUrlOnpremise(providerProperties.getUrlOnpremise(tc.getDocIdentidad_Nro()));

            if (tr.getMensaje().contains("ha sido aceptad") || tr.getMensaje().contains("aprobado")) {

                tr.setPdf(tr.getPdfBorrador());

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
