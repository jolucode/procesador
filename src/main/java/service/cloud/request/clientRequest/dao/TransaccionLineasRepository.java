package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionLineas;
import service.cloud.request.clientRequest.entity.TransaccionLineasPK;

public interface TransaccionLineasRepository extends JpaRepository<TransaccionLineas, TransaccionLineasPK> {

}
