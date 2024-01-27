package service.cloud.request.clientRequest.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import service.cloud.request.clientRequest.entity.Transaccion;
import service.cloud.request.clientRequest.entity.TransaccionBaja;

import java.util.List;

public interface TransaccionBajaRepository extends JpaRepository<TransaccionBaja, Long> {

    @Query(value = "SELECT * FROM TRANSACCION_BAJA ORDER BY fecha DESC LIMIT 1;", nativeQuery = true)
    TransaccionBaja getLastRow();




}
