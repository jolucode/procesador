package service.cloud.request.clientrequest.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Client {

    private String razonSocial;

    private String wsUsuario;
    private String wsClave;
    private String wsLocation;

    private String emisorElecRs;



    private String certificadoName;
    private String certificadoPassword;
    private String certificadoProveedor;
    private String certificadoTipoKeystore;

    private String usuarioSol;
    private String claveSol;

    private String integracionWs;
    private String mostrarSoap;
    private String pdfBorrador;
    private String impresion;

    private String urlOnpremise;

    /**agregar
     * clientId
     * clientSecret
     * otros
      */

    private String scope;
    private String clientId;
    private String clientSecret;
    private String UserNameSunatGuias;
    private String passwordSunatGuias;


}