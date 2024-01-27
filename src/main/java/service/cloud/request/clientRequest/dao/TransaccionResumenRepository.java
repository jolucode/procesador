package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import service.cloud.request.clientRequest.entity.TransaccionResumen;

import java.util.List;

public interface TransaccionResumenRepository extends JpaRepository<TransaccionResumen, String> {

    @Query(value = "SELECT t FROM TransaccionResumen t WHERE t.estado IN('G','K','D','A')")
    List<TransaccionResumen> findPendientes();

    List<TransaccionResumen> findByEstado(String estado);
}
