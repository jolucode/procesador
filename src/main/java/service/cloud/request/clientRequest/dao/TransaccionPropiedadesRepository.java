package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionPropiedades;
import service.cloud.request.clientRequest.entity.TransaccionPropiedadesPK;

public interface TransaccionPropiedadesRepository extends JpaRepository<TransaccionPropiedades, TransaccionPropiedadesPK> {

}
