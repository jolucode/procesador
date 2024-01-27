package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionLineasUsucampos;
import service.cloud.request.clientRequest.entity.TransaccionLineasUsucamposPK;

public interface TransaccionLineasUsucamposRepository extends JpaRepository<TransaccionLineasUsucampos, TransaccionLineasUsucamposPK> {

}
