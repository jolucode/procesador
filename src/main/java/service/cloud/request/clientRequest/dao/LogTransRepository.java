package service.cloud.request.clientRequest.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.LogTrans;

public interface LogTransRepository extends JpaRepository<LogTrans, Integer> {

}
