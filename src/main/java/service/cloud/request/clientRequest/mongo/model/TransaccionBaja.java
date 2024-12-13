package service.cloud.request.clientRequest.mongo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "transaccion_baja")
public class TransaccionBaja {

    @Id
    @EqualsAndHashCode.Include
    private String id; // Identificador único del documento en MongoDB
    private String rucEmpresa;
    private String fecha; // Fecha en formato yyyyMMdd
    private Integer idd; // Último número usado
    private String serie;
    private String ticketBaja;
    private String docId;

}