package service.cloud.request.clientrequest.estela.builder;

import org.springframework.stereotype.Component;
import service.cloud.request.clientrequest.estela.dto.FileRequestDTO;

@Component
public class DocumentBuilder {

    // Método para generar la estructura SOAP con valores dinámicos
    public String emisionBuildSoapRequest(FileRequestDTO requestDTO) {
        // Estructura SOAP parametrizada
        return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <S:Header>\n" +
                "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
                "      <wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                "        <wsse:Username>" + requestDTO.getUsername() + "</wsse:Username>\n" +
                "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + requestDTO.getPassword() + "</wsse:Password>\n" +
                "      </wsse:UsernameToken>\n" +
                "    </wsse:Security>\n" +
                "  </S:Header>\n" +
                "  <S:Body>\n" +
                "    <ns2:sendBill xmlns:ns2=\"http://service.sunat.gob.pe\">\n" +
                "      <fileName>" + requestDTO.getFilename() + "</fileName>\n" +
                "      <contentFile>" + requestDTO.getContentFile() + "</contentFile>\n" +
                "    </ns2:sendBill>\n" +
                "  </S:Body>\n" +
                "</S:Envelope>";
    }

    // Método para generar la estructura SOAP con valores dinámicos
    public String consultaBuildSoapRequest(FileRequestDTO requestDTO) {
        // Estructura SOAP parametrizada para la consulta del estado del comprobante
        return "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <S:Header>\n" +
                "    <wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">\n" +
                "      <wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">\n" +
                "        <wsse:Username>" + requestDTO.getUsername() + "</wsse:Username>\n" +
                "        <wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + requestDTO.getPassword() + "</wsse:Password>\n" +
                "      </wsse:UsernameToken>\n" +
                "    </wsse:Security>\n" +
                "  </S:Header>\n" +
                "  <S:Body>\n" +
                "    <ns2:getStatusCdr xmlns:ns2=\"http://service.sunat.gob.pe\">\n" +
                "      <rucComprobante>" + requestDTO.getRucComprobante() + "</rucComprobante>\n" +
                "      <tipoComprobante>" + requestDTO.getTipoComprobante() + "</tipoComprobante>\n" +
                "      <serieComprobante>" + requestDTO.getSerieComprobante() + "</serieComprobante>\n" +
                "      <numeroComprobante>" + requestDTO.getNumeroComprobante() + "</numeroComprobante>\n" +
                "    </ns2:getStatusCdr>\n" +
                "  </S:Body>\n" +
                "</S:Envelope>";
    }

}
