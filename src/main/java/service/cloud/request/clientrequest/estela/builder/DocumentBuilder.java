package service.cloud.request.clientrequest.estela.builder;

import org.springframework.stereotype.Component;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;

@Component
public class DocumentBuilder {

    // Método para generar la estructura SOAP de emisión con valores dinámicos
    public String buildEmissionSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:sendBill xmlns:ns2="http://service.sunat.gob.pe">
                              <fileName>%s</fileName>
                              <contentFile>%s</contentFile>
                            </ns2:sendBill>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getFilename(),
                requestDTO.getContentFile());
    }

    // Método para generar la estructura SOAP de consulta con valores dinámicos
    public String buildConsultaSoapRequest(FileRequestDTO requestDTO) {
        return String.format("""
                        <S:Envelope xmlns:S="http://schemas.xmlsoap.org/soap/envelope/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                          <S:Header>
                            <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                              <wsse:UsernameToken xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
                                <wsse:Username>%s</wsse:Username>
                                <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">%s</wsse:Password>
                              </wsse:UsernameToken>
                            </wsse:Security>
                          </S:Header>
                          <S:Body>
                            <ns2:getStatusCdr xmlns:ns2="http://service.sunat.gob.pe">
                              <rucComprobante>%s</rucComprobante>
                              <tipoComprobante>%s</tipoComprobante>
                              <serieComprobante>%s</serieComprobante>
                              <numeroComprobante>%s</numeroComprobante>
                            </ns2:getStatusCdr>
                          </S:Body>
                        </S:Envelope>
                        """,
                requestDTO.getUsername(),
                requestDTO.getPassword(),
                requestDTO.getRucComprobante(),
                requestDTO.getTipoComprobante(),
                requestDTO.getSerieComprobante(),
                requestDTO.getNumeroComprobante());
    }

}
