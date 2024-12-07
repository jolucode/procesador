package service.cloud.request.clientrequest.mongo.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import service.cloud.request.clientrequest.mongo.model.Log;

public interface ILogRepo extends ReactiveMongoRepository<Log, String> {
}
