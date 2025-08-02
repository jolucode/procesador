package service.cloud.request.clientRequest.service.emision;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import service.cloud.request.clientRequest.dto.dto.TransacctionDTO;
import service.cloud.request.clientRequest.mongo.model.TransaccionBaja;
import service.cloud.request.clientRequest.service.emision.ServiceBaja;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ServiceBajaTest {

    @Autowired
    private ServiceBaja serviceBaja;

    private static final String RUC = "20131872233";

    private final Set<String> seriesGeneradas = Collections.synchronizedSet(new HashSet<>());

    @BeforeEach
    public void reset() {
        seriesGeneradas.clear();
        // Opcional: limpiar la colección si deseas testear desde cero
        // reactiveMongoTemplate.remove(Query.query(Criteria.where("rucEmpresa").is(RUC)), TransaccionBaja.class).block();
    }

    @Test
    public void testGeneracionConcurrenteRC_Reactive() throws InterruptedException {
        int NUM_HILOS = 10;
        CountDownLatch latch = new CountDownLatch(NUM_HILOS);
        Set<String> seriesGeneradas = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < NUM_HILOS; i++) {
            int finalI = i;

            TransacctionDTO dto = new TransacctionDTO();
            dto.setDocIdentidad_Nro("20131872233");
            dto.setDOC_Codigo("03");
            dto.setDOC_Serie("B001");
            dto.setDOC_Id("DOC" + finalI);

            serviceBaja.generarIDyFecha(dto)
                    .doOnNext(baja -> {
                        assertNotNull(baja.getSerie());
                        seriesGeneradas.add(baja.getSerie());
                        System.out.println("RC generado: " + baja.getSerie());
                    })
                    .doOnError(e -> e.printStackTrace())
                    .doFinally(sig -> latch.countDown())
                    .subscribe();
        }

        latch.await(); // Esperar a que terminen todos
        assertEquals(NUM_HILOS, seriesGeneradas.size(), "No todos los RC generados fueron únicos");
    }

}
