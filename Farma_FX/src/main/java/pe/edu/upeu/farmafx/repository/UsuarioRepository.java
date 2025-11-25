package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends ICrudGenericRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.username = :username")
    Usuario buscarUsuario(@Param("username") String username);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.perfil WHERE u.username = :username")
    Optional<Usuario> findByUsername(@Param("username") String username);

    boolean existsByUsername(String username);
}
