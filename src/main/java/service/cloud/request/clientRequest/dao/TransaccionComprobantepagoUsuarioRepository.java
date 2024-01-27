package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.TransaccionComprobantepagoUsuario;
import service.cloud.request.clientRequest.entity.TransaccionComprobantepagoUsuarioPK;

public interface TransaccionComprobantepagoUsuarioRepository extends JpaRepository<TransaccionComprobantepagoUsuario, TransaccionComprobantepagoUsuarioPK> {

}
