package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionGuias;

public interface TransaccionGuiasRepository extends JpaRepository<TransaccionGuias, String> {

}
