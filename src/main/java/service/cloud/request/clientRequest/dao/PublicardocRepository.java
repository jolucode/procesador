package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import service.cloud.request.clientRequest.entity.PublicardocWs;

import java.util.List;

public interface PublicardocRepository extends JpaRepository<PublicardocWs, String> {

    @Query(value = "SELECT p FROM PublicardocWs p WHERE p.estadoPublicacion =:estadoPublicacion")
    List<PublicardocWs> listarHabilitadas(Character estadoPublicacion);
}
