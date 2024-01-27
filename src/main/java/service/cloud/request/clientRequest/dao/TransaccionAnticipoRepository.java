package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionAnticipo;
import service.cloud.request.clientRequest.entity.TransaccionAnticipoPK;

public interface TransaccionAnticipoRepository extends JpaRepository<TransaccionAnticipo, TransaccionAnticipoPK> {

}
