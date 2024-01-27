package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionUsucampos;
import service.cloud.request.clientRequest.entity.TransaccionUsucamposPK;

public interface TransaccionUsucamposRepository extends JpaRepository<TransaccionUsucampos, TransaccionUsucamposPK> {

}
