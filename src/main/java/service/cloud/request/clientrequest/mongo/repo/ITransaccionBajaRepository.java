package service.cloud.request.clientrequest.mongo.repo;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import service.cloud.request.clientrequest.mongo.model.TransaccionBaja;

public interface ITransaccionBajaRepository extends ReactiveMongoRepository<TransaccionBaja, String> {


    Mono<TransaccionBaja> findFirstByRucEmpresaOrderByFechaDescIddDesc(String rucEmpresa);

    Mono<TransaccionBaja> findFirstByRucEmpresaAndSerie(String rucEmpresa, String serie);


}
