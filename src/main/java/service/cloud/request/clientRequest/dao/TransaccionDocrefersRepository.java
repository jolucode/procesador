package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionDocrefers;
import service.cloud.request.clientRequest.entity.TransaccionDocrefersPK;

public interface TransaccionDocrefersRepository extends JpaRepository<TransaccionDocrefers, TransaccionDocrefersPK> {

}
