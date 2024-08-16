package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;

@Component
public interface InterfacePrincipal {

    RequestPost EnviarTransacciones(TransacctionDTO transaccion, String stringRequestOnpremise) throws Exception;
}
