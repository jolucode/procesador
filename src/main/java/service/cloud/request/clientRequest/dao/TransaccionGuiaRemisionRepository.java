package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionGuiaRemision;

public interface TransaccionGuiaRemisionRepository extends JpaRepository<TransaccionGuiaRemision, String> {

}
