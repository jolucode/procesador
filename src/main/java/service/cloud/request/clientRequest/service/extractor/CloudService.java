package service.cloud.request.clientRequest.service.extractor;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.dto.request.RequestPost;
import service.cloud.request.clientRequest.service.emision.interfac.InterfacePrincipal;

import java.util.logging.Logger;

@Service
public class CloudService implements CloudInterface {

    private final Logger logger = Logger.getLogger(String.valueOf(CloudService.class));

    @Autowired
    InterfacePrincipal interfacePrincipal;

    @Override
    public ResponseEntity<RequestPost> proccessDocument(String stringRequestOnpremise) {
        TransacctionDTO[] transacctionDTO = null;
        RequestPost responseProcesor = null;
        String datePattern = "(\"\\w+\":\\s*\"\\d{4}-\\d{2}-\\d{2}) \\d{2}:\\d{2}:\\d{2}\\.\\d\"";
        String updatedJson = stringRequestOnpremise.replaceAll(datePattern, "$1\"");
        try {
            Gson gson = new Gson();
            transacctionDTO = gson.fromJson(updatedJson, TransacctionDTO[].class);
            for (int i = 0; i < transacctionDTO.length; i++) {
                responseProcesor = interfacePrincipal.EnviarTransacciones(transacctionDTO[i], updatedJson);
            }
        } catch (Exception e) {
            logger.info("SE GENERO UN ERROR : " + e.getMessage());
        }
        return ResponseEntity.ok(responseProcesor);
    }

}
