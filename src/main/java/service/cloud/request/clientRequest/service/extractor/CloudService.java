package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import org.apache.log4j.PropertyConfigurator;
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
import service.cloud.request.clientRequest.service.emision.OnPremiseImpl;
import service.cloud.request.clientRequest.service.emision.interfac.GuiaInterface;
import service.cloud.request.clientRequest.service.emision.interfac.ServiceInterface;
import service.cloud.request.clientRequest.service.publicar.PublicacionManager;
import service.cloud.request.clientRequest.utils.LoggerTrans;
import service.cloud.request.clientRequest.utils.exception.ConectionSunatException;
import service.cloud.request.clientRequest.utils.exception.SoapFaultException;
import service.cloud.request.clientRequest.utils.exception.ValidationException;
import service.cloud.request.clientRequest.utils.exceptions.VenturaExcepcion;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Service
public class CloudService implements CloudInterface {

    Logger logger = LoggerFactory.getLogger(CloudService.class);

    @Autowired
    GuiaInterface guiaInterface;

    @Autowired
    ServiceInterface serviceInterface;

    @Autowired
    ProviderProperties providerProperties;

    @Autowired
    PublicacionManager publicacionManager;

    @Override
    public ResponseEntity<RequestPost> proccessDocument(String stringRequestOnpremise) {
        TransacctionDTO[] transacctionDTO = null;
        RequestPost responseProcesor = null;
        String datePattern = "(\"\\w+\":\\s*\"\\d{4}-\\d{2}-\\d{2}) \\d{2}:\\d{2}:\\d{2}\\.\\d\"";
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");
        try {
            Gson gson = new Gson();
            transacctionDTO = gson.fromJson(updatedJson, TransacctionDTO[].class);
            responseProcesor = procesarTransaccion(transacctionDTO[0], updatedJson);
        } catch (Exception e) {
            logger.info("SE GENERO UN ERROR : " + e.getMessage());
        }
        return ResponseEntity.ok(responseProcesor);
    }


    /**
     * @return la lista de trnsacciones pendientes de envio. Es decir las que
     * tengan el estado [N]uevo,[C]orregido,[E]nviado
     */

    public RequestPost procesarTransaccion(TransacctionDTO transaccion, String stringRequestOnpremise) throws Exception {
        RequestPost request = new RequestPost();

        //request.getLogMdb().setRequest(new Gson().toJson(transaccion));
        logger.info("Documento extraido de la intermedia es : " + transaccion.getDocIdentidad_Nro() + " - " + transaccion.getDOC_Id());
        try {

            TransaccionRespuesta tr = EnviarTransaccion(transaccion);
            OnPremiseImpl clientHanaService = new OnPremiseImpl();
            request = generateDataRequestHana(transaccion, tr);
            //clientHanaService.anexarDocumentos(request);
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
    }

    public TransaccionRespuesta EnviarTransaccion(TransacctionDTO transaction) throws VenturaExcepcion {

        String largePath = System.getProperty("user.dir") + File.separator + "log4j.properties";
        File f = new File(largePath);
        if (f.exists() && !f.isDirectory()) {
            PropertyConfigurator.configure(largePath);
        }

        TransaccionRespuesta transactionResponse = new TransaccionRespuesta();
        String docUUID = transaction.getFE_Id();

        try {
            if (null != transaction) {
                if (transaction.getFE_TipoTrans().equalsIgnoreCase(ISunatConnectorConfig.FE_TIPO_TRANS_EMISION)) {
                    String codeTypeDocument = transaction.getDOC_Codigo();
                    if (codeTypeDocument.equalsIgnoreCase(IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE)) {
                        LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] La transaccion es de EMISION de una GUIA DE REMISIÓN.", docUUID);
                        transactionResponse = guiaInterface.transactionRemissionGuideDocumentRest(transaction, IUBLConfig.DOC_SENDER_REMISSION_GUIDE_CODE);
                    } else {
                        long startTime = System.nanoTime();
                        transactionResponse = serviceInterface.transactionDocument(transaction, codeTypeDocument);
                        long endTime = System.nanoTime();
                        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
                        System.out.println("El método EnviarTransaccion tomó " + elapsedTime + " segundos para ejecutarse.");
                    }
                } else if (transaction.getFE_TipoTrans().equalsIgnoreCase(ISunatConnectorConfig.FE_TIPO_TRANS_BAJA)) {
                    transaction.setANTICIPO_Id(GenerarIDyFecha(transaction));
                    LoggerTrans.getCDThreadLogger().log(Level.INFO, "[{0}] Se genera la baja de un documento tipo: {1}", new Object[]{docUUID, transaction.getDOC_Codigo()});
                    transactionResponse = serviceInterface.transactionVoidedDocument(transaction, transaction.getDOC_Codigo());
                }
            }
        } catch (ConectionSunatException | SoapFaultException | ValidationException e) {
            Optional<String> optional = Optional.ofNullable(e.getLocalizedMessage());
            String mensaje = Optional.ofNullable(optional.orElse(e.getMessage())).orElse("No se pudo obtener la respuesta del servidor.");
            transactionResponse.setMensaje(mensaje);
        } catch (Exception e) {
            //transaccionRepository.delete(transaction);
            transactionResponse.setMensaje(e.getMessage());
        }
        return transactionResponse;
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

                //transaccionRepository.delete(tc);
                //PublicardocWs transPubli = documentService.crearObjectPublicardocWs(tc, null, true);
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
                //transaccionRepository.delete(tc);
            }
        } catch (Exception e) {
            logger.error("Error : " + e.getMessage());
        }
        return request;
    }

    public String GenerarIDyFecha(TransacctionDTO tr) {
        String serie = "";
        try {
            if (tr.getFE_TipoTrans().compareTo("E") == 0) {
                return "";
            }
            /*TransaccionBaja trb = transaccionBajaRepository.getLastRow();
            LocalDateTime date = LocalDateTime.now();
            Date fecha = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            TransaccionBaja nuevaBaja = new TransaccionBaja();

            if (trb != null) {
                int indexOf = trb.getSerie().lastIndexOf("-");
                String fin = trb.getSerie().substring(indexOf + 1);
                if (simpleDateFormat.format(fecha).equals(trb.getFecha().toString())) {
                    int numero = Integer.parseInt(fin);
                    numero++;
                    String nuevoId = String.format("%05d", numero);
                    serie = "RA-" + simpleDateFormat.format(fecha) + "-" + nuevoId;
                    tr.setANTICIPO_Id(serie);

                    trb.setFecha(Long.valueOf(simpleDateFormat.format(fecha)));
                    trb.setId(numero);
                    trb.setSerie(serie);
                    //transaccionBajaRepository.saveAndFlush(trb);
                } else {
                    String nuevoId = String.format("%05d", 1);
                    serie = "RA-" + simpleDateFormat.format(fecha) + "-" + nuevoId;

                    TransaccionBaja nuevaBaja2 = new TransaccionBaja();
                    nuevaBaja2.setFecha(Long.valueOf(simpleDateFormat.format(fecha)));
                    nuevaBaja2.setId(1);
                    nuevaBaja2.setSerie(serie);

                    //transaccionBajaRepository.saveAndFlush(nuevaBaja2);
                }
            } else {
                int numero = 0;
                numero++;
                String nuevoId = String.format("%05d", numero);
                serie = "RA-" + simpleDateFormat.format(fecha) + "-" + nuevoId;
                tr.setANTICIPO_Id(serie);

                nuevaBaja.setSerie(serie);
                nuevaBaja.setFecha(Long.valueOf(simpleDateFormat.format(fecha)));
                nuevaBaja.setId(numero);
            }
            tr.setANTICIPO_Id(serie);*/
            //transaccionRepository.save(tr);

        } catch (Exception ex) {
            LoggerTrans.getCDThreadLogger().log(Level.SEVERE, ex.getMessage());
        }
        return serie;
    }


}
