package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.entity.Transaccion;

@Component
public interface InterfacePrincipal {

    RequestPost EnviarTransacciones(Transaccion transaccion, String stringRequestOnpremise) throws Exception;
}
