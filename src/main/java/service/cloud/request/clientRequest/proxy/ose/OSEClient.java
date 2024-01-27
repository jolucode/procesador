package service.cloud.request.clientRequest.proxy.ose;

import org.apache.log4j.Logger;
import service.cloud.request.clientRequest.proxy.object.CdrStatusResponse;
import service.cloud.request.clientRequest.proxy.ose.object.StatusResponse;

import javax.activation.DataHandler;

public class OSEClient extends OSEBasicClient {

    private final Logger logger = Logger.getLogger(OSEClient.class);


    public OSEClient(String clientType) {
        super(clientType);
    } //OSEClient


    @Override
    public byte[] sendBill(String fileName, DataHandler contentFile) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+sendBill() fileName[" + fileName + "], contentFile[" + contentFile + "]");
        }

        byte[] response = getSecurityPort().sendBill(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendBill() TIME[" + (System.currentTimeMillis() - initTime) + "]");
        }
        return response;
    } //sendBill

    @Override
    public String sendSummary(String fileName, DataHandler contentFile) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+sendSummary() fileName[" + fileName + "], contentFile[" + contentFile + "]");
        }
        String response = getSecurityPort().sendSummary(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendSummary() TIME[" + (System.currentTimeMillis() - initTime) + "]");
        }
        return response;
    } //sendSummary

    @Override
    public String sendPack(String fileName, DataHandler contentFile) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+sendPack() fileName[" + fileName + "], contentFile[" + contentFile + "]");
        }

        String response = getSecurityPort().sendPack(fileName, contentFile);

        if (logger.isDebugEnabled()) {
            logger.debug("-sendPack() TIME[" + (System.currentTimeMillis() - initTime) + "]");
        }
        return response;
    } //sendPack

    @Override
    public StatusResponse getStatus(String ticket) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+getStatus() ticket[" + ticket + "]");
        }

        StatusResponse response = getSecurityPort().getStatus(ticket);

        if (logger.isDebugEnabled()) {
            logger.debug("-getStatus() TIME[" + (System.currentTimeMillis() - initTime) + "]");
        }
        return response;
    } //getStatus

    @Override
    public CdrStatusResponse getStatusCDR(String rucComprobante, String tipoComprobante, String serieComprobante,
                                          Integer numeroComprobante) throws Exception {
        long initTime = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("+getStatusCDR() rucComprobante[" + rucComprobante + "], tipoComprobante[" + tipoComprobante + "], serieComprobante[" + serieComprobante + "], numeroComprobante[" + numeroComprobante + "]");
        }

        CdrStatusResponse response = getSecurityPort().getStatusCdr(rucComprobante, tipoComprobante, serieComprobante, numeroComprobante);

        if (logger.isDebugEnabled()) {
            logger.debug("-getStatusCDR() TIME[" + (System.currentTimeMillis() - initTime) + "]");
        }
        return response;
    } //getStatusCDR

} //OSEClient
