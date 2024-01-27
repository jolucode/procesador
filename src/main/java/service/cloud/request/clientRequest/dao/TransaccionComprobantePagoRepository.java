package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionComprobantePago;
import service.cloud.request.clientRequest.entity.TransaccionComprobantePagoPK;

public interface TransaccionComprobantePagoRepository extends JpaRepository<TransaccionComprobantePago, TransaccionComprobantePagoPK> {

}
