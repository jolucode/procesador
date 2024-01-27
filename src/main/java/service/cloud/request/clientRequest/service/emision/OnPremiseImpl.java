package service.cloud.request.clientRequest.service.emision;

import com.google.gson.Gson;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.service.emision.interfac.OnPremiseInterface;


@Service
public class OnPremiseImpl implements OnPremiseInterface {
    Logger logger = LoggerFactory.getLogger(OnPremiseImpl.class);

    public int anexarDocumentos(RequestPost request) {
        HttpResponse<String> response = null;
        try {
            Gson gson = new Gson();
            String jsonBody = gson.toJson(request);
            response = Unirest.post(request.getUrlOnpremise() + "anexar")
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .asString();
            logger.info("Se envió de manera correcta al servidor OnPremise los documentos");
        } catch (Exception e) {
            logger.error("Error capturado es el siguiente: " + e.getMessage());
        }
        logger.info("La ruta del servidor donde se esta dejando los documentos es : " + request.getUrlOnpremise());
        return response.getStatus();
    }
}
