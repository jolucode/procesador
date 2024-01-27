package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import service.cloud.request.clientRequest.entity.Transaccion;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, String> {

    @Query(value = "SELECT t FROM Transaccion t WHERE t.FE_Estado IN ('R','P','S') ORDER BY t.FE_MaxSalto ASC")
    List<Transaccion> findPendientes();

    @Query(value = "SELECT t FROM Transaccion t WHERE t.FE_Estado IN ('N','E','C','G','W') ORDER BY t.FE_MaxSalto ASC")
    List<Transaccion> findDisponibles();

    @Query(value = "SELECT t FROM Transaccion t WHERE t.TicketBaja IS NOT NULL AND LENGTH(t.TicketBaja) > 0 ORDER BY t.FE_MaxSalto ASC")
    List<Transaccion> findBajas();


    /*@Modifying
    @Transactional
    @Query("UPDATE Transaccion t SET t.aNTICIPOId = :anticipo WHERE (t.fEId = :feId)")
    void actualizarTransaccion(
            @Param("anticipo") String anticipo,
            @Param("feId") String feId
    );*/
}
