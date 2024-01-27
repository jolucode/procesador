package service.cloud.request.clientRequest.dto.finalClass;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class ConfigData {

    private String usuarioSol;
    private String claveSol;

    private String integracionWs;
    private String ambiente;
    private String mostrarSoap;
    private String pdfBorrador;
    //private String logoSociedad;
    private String impresionPDF;
    private String rutaBaseDoc;


    private String impresion;

    private String scope;
    private String clientId;
    private String clientSecret;
    private String userNameSunatSunat;
    private String passwordSunatSunat;


    private String urlService;



}
