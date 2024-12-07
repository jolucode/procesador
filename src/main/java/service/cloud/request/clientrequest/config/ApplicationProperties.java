package service.cloud.request.clientrequest.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApplicationProperties {

    @Value("${application.ambiente}")
    public String ambiente;

    @Value("${application.rutas.rutaBaseDoc}")
    public String rutaBaseDoc;

    @Value("${application.soap-client.sunat.base-url-emision}")
    public String urlSunatEmision;

    @Value("${application.soap-client.sunat.base-url-consulta}")
    public String urlSunatConsulta;

    @Value("${application.soap-client.ose.base-url}")
    public String urlOse;

}
