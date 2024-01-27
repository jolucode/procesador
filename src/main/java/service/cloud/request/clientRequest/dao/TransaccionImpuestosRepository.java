package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionImpuestos;
import service.cloud.request.clientRequest.entity.TransaccionImpuestosPK;

public interface TransaccionImpuestosRepository extends JpaRepository<TransaccionImpuestos, TransaccionImpuestosPK> {

}
