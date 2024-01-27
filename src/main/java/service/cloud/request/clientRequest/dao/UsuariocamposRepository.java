package service.cloud.request.clientRequest.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import service.cloud.request.clientRequest.entity.Usuariocampos;

import java.util.Optional;

public interface UsuariocamposRepository extends JpaRepository<Usuariocampos, Integer> {

    Optional<Usuariocampos> findByNombre(String nombre);

}
