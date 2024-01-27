package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionError;

public interface TransaccionErrorRepository extends JpaRepository<TransaccionError, String> {

}
