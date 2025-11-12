package pe.edu.upeu.sysventas.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.sysventas.model.Usuario;

public interface UsuarioRepository extends ICrudGenericRepository<Usuario,Long>{

    @Query(value = "SELECT u.* FROM upeu_usuario u WHERE u.user=:userx ", nativeQuery = true)
    Usuario buscarUsuario(@Param("userx") String userx);

    @Query(value = "SELECT u.* FROM upeu_usuario u WHERE u.user=:user and u.clave=:clave", nativeQuery = true)
    Usuario loginUsuario(@Param("user") String user, @Param("clave") String  clave);

}
