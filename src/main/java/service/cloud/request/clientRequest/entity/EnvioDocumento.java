package service.cloud.request.clientRequest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "ENVIO_DOCUMENTO")
public class EnvioDocumento {

    @Id
    @NonNull
    @Column(name = "FE_Id")
    private String idDocumento;

    @Column(name = "Cantidad_Envio")
    private Integer cantidadEnvio;

    //@NotNull(message = "La clave de la sociedad no puede ser nula.")
    @Column(name = "key_sociedad")
    private String keySociedad;

    @Override
    public String toString() {
        return "EnvioDocumento{" +
                "fEId='" + idDocumento + '\'' +
                ", fEErrores=" + cantidadEnvio +
                ", keySociedad='" + keySociedad + '\'' +
                '}';
    }
}
