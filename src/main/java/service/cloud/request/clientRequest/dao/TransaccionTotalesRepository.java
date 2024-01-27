package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionTotales;
import service.cloud.request.clientRequest.entity.TransaccionTotalesPK;

public interface TransaccionTotalesRepository extends JpaRepository<TransaccionTotales, TransaccionTotalesPK> {

}
