package service.cloud.request.clientRequest.service.emision.interfac;

import org.springframework.stereotype.Component;
import service.cloud.request.clientRequest.dto.request.RequestPost;

@Component
public interface OnPremiseInterface {

    public int anexarDocumentos(RequestPost request);

}
