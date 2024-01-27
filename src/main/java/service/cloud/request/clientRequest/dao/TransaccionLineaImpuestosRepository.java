package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionLineaImpuestos;
import service.cloud.request.clientRequest.entity.TransaccionLineaImpuestosPK;

public interface TransaccionLineaImpuestosRepository extends JpaRepository<TransaccionLineaImpuestos, TransaccionLineaImpuestosPK> {

}
