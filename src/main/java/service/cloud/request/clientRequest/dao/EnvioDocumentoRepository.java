package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.EnvioDocumento;

import java.util.Optional;

public interface EnvioDocumentoRepository extends JpaRepository<EnvioDocumento, String> {

    Optional<EnvioDocumento> findByIdDocumentoAndKeySociedad(String feId, String keySociedad);
}
