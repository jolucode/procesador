package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionLineasBillref;
import service.cloud.request.clientRequest.entity.TransaccionLineasBillrefPK;

public interface TransaccionLineasBillrefRepository extends JpaRepository<TransaccionLineasBillref, TransaccionLineasBillrefPK> {

}
