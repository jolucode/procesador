package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionResumenLineaAnexo;
import service.cloud.request.clientRequest.entity.TransaccionResumenLineaAnexoPK;

public interface TransaccionResumenLineaAnexoRepository extends JpaRepository<TransaccionResumenLineaAnexo, TransaccionResumenLineaAnexoPK> {

}
