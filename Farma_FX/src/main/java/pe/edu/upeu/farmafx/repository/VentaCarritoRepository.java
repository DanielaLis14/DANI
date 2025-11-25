package pe.edu.upeu.farmafx.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.farmafx.model.VentaCarrito;

import java.util.List;

public interface VentaCarritoRepository extends ICrudGenericRepository<VentaCarrito, Long> {

    @Query("SELECT vc FROM VentaCarrito vc "
            + "JOIN FETCH vc.productoCarrito "
            + "JOIN FETCH vc.usuarioCarrito "
            + "WHERE vc.usuarioCarrito.idUsuario = :idUsuario "
            + "AND vc.estadoCarrito = true "
            + "ORDER BY vc.idVentaCarrito")
    List<VentaCarrito> listarPorUsuario(@Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("DELETE FROM VentaCarrito vc WHERE vc.usuarioCarrito.idUsuario = :idUsuario")
    void eliminarPorUsuario(@Param("idUsuario") Long idUsuario);
}
