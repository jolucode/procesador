package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionResumenLinea;
import service.cloud.request.clientRequest.entity.TransaccionResumenLineaPK;

public interface TransaccionResumenLineaRepository extends JpaRepository<TransaccionResumenLinea, TransaccionResumenLineaPK> {

}
