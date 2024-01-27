package service.cloud.request.clientRequest.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.Transaccion;

public interface GuiaRemisionRepository extends JpaRepository<Transaccion, String> {

}
